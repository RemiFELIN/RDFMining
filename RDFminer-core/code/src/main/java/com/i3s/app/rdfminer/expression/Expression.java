/**
 * 
 */
package com.i3s.app.rdfminer.expression;

import com.i3s.app.rdfminer.sparql.RDFNodePair;
import com.i3s.app.rdfminer.sparql.virtuoso.VirtuosoEndpoint;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.AnonId;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

/**
 * This abstract class represent an OWL 2 expression.
 * <p>
 * There are seven categories of expressions, namely:
 * </p>
 * <ul>
 * <li>classes;</li>
 * <li>object properties;</li>
 * <li>data properties;</li>
 * <li>named individuals;</li>
 * <li>datatypes;</li>
 * <li>literals;</li>
 * <li>facets.</li>
 * </ul>
 * <p>
 * For each type of of the above expression categories, a corresponding subclass
 * of this class exists.
 * </p>
 * 
 * @author Andrea G. B. Tettamanzi
 *
 */
public abstract class Expression {

	private static Logger logger = Logger.getLogger(Expression.class.getName());

	/**
	 * A (possibly empty) list of subexpressions.
	 */
	public List<Expression> subExpressions;

	/**
	 * A variable to cache the extension of this expression.
	 * <p>
	 * A <code>null</code> value means the extension has not yet been computed.
	 * <p>
	 * <p>
	 * For uniformity reasons, the extension of every expression is represented as a
	 * set of &lang;<var>x</var>, <var>y</var>&rang; pairs; depending on the type of
	 * expression, the <var>y</var> may be <code>null</code>, meaning it is unbound.
	 * </p>
	 */
	public Set<RDFNodePair> extension;

	/**
	 * The root symbol of this expression.
	 * <p>
	 * The root symbol is an IRI for atomic class, named individual, or property
	 * expressions, a literal for a literal expression, and a functor for complex
	 * expressions.
	 * </p>
	 */
	protected String rootSymbol;

	/**
	 * A SPARQL graph pattern that binds the ?x and ?y variables to all individuals
	 * in the extension of this expression.
	 * <p>
	 * The expression is constructed recursively by incorporating the SPARQL graph
	 * patterns for the sub-expressions, until an atomic expression is encountered.
	 * </p>
	 * 
	 * @see #createGraphPattern(String, String)
	 */
	public String graphPattern;

	/**
	 * A unique identifier used to create new fresh variable names.
	 */
	static private int freshVarId = 0;

	/**
	 * Creates an empty expression of an undetermined type.
	 * <p>
	 * This constructor is intended to be called by subclasses from within their
	 * constructors.
	 * </p>
	 */
	protected Expression() {
		subExpressions = new ArrayList<>();
		extension = null;
		graphPattern = null;
		rootSymbol = null;
	}

	/**
	 * Instantiates the SPARQL graph pattern for this expression based on the given
	 * SPARQL expressions.
	 * <p>
	 * The two expressions given, which may include the initial <tt>?</tt> or
	 * <tt>$</tt> symbol, in which case they are query variables, or may be the name
	 * of a resource or a literal, are used to substitute the two variables,
	 * conventionally denoted <tt>?x</tt> and <tt>?y</tt>, which are to be bound by
	 * the solution of the graph pattern.
	 * </p>
	 * <p>
	 * Furthermore, during this instantiation process, any "local" variable, i.e.,
	 * any SPARQL variable other than <tt>?x</tt> or <tt>?y</tt> occurring within
	 * the graph pattern, is replaced with a "fresh" unique variable not used
	 * elsewhere.
	 * </p>
	 * <p>
	 * A SPARQL query variable name must comply with the syntax
	 * </p>
	 * <quote>
	 * <tt>( PN_CHARS_U | [0-9] ) ( PN_CHARS_U | [0-9] | #x00B7 | [#x0300-#x036F] | [#x203F-#x2040] )*</tt>
	 * </quote>
	 * <p>
	 * i.e., an alphanumeric character including underscore, followed by 0 or more
	 * extended alphanumeric characters.
	 * </p>
	 * <p>
	 * Implementations of this method are expected to use the static method
	 * {@link #getFreshVariableName()} to automatically generate such fresh variable
	 * names.
	 * </p>
	 * 
	 * @param subject the expression replacing the <tt>?x</tt> SPARQL variable
	 * @param object  the expression replacing the <tt>?y</tt> SPARQL variable
	 * @return the SPARQL graph pattern for this expression with the variables
	 *         <tt>?x</tt> and <tt>?y</tt> substituted by the given expressions
	 * @see #graphPattern
	 * @see #getFreshVariableName()
	 */
	public abstract String createGraphPattern(String subject, String object);

	/**
	 * Creates a fresh (i.e., not yet used) query variable name.
	 */
	public static String getFreshVariableName() {
		return "?z_" + (freshVarId++);
	}

	/**
	 * Generate a cache file name from a SPARQL query, so that each file has a
	 * different name.
	 * <p>
	 * The cache name should be unique and short, but at the same time recognizable
	 * (for debugging purposes). Therefore, it is composed of a first part,
	 * consisting of the root symbol of the expression, without the prefix, if any;
	 * and a second part, consisting of the 32-bit hash of the expression itself in
	 * hexadecimal notation, to ensure (virtual) uniqueness.
	 * </p>
	 * <b>Updated: return null since RDFminer v1.5</b>
	 */
	protected String cacheName() {
		// Take the root symbol as the base of the name,
		// but remove the prefix and the closing ">":
//		String[] s = rootSymbol.split("/");
//		String name = s[s.length - 1];
//		return String.format(Global.CACHE_FOLDER + "%s-%08x.cache", name.substring(0, name.length() - 1),
//				toString().hashCode());
		return null;
	}

	/**
	 * Retrieves the extension of this expression by querying a SPARQL endpoint.
	 * <p>
	 * The extension of an expression contains all &lang;<var>x</var>,
	 * <var>y</var>&rang; pairs pairs such that <var>x</var> is an instance of the
	 * class it represents, if it is a class expression, or that &lang;<var>x</var>,
	 * <var>y</var>&rang; is in the relation it represents, if it is a property
	 * expression.
	 * </p>
	 * <p>
	 * In order to circumvent the limit currently enforced by DBpedia's SPARQL
	 * endpoint of 50,000 results, the query is split into 16 subqueries using a
	 * technique based on filtering the results based on the first hexadecimal digit
	 * of their MD5 hash. Furthermore, the extension is cached to a local file, so
	 * that the query will not have to be repeated should the extension of the same
	 * expression be needed again in the future.
	 * </p>
	 */
	public Set<RDFNodePair> extension(VirtuosoEndpoint endpoint) {
		if (extension == null) {
			extension = new TreeSet<>();

			if (graphPattern != null) {
				try {
					// Try to read the productions from a cache file named after the query:
					BufferedReader cache = new BufferedReader(new FileReader(cacheName()));
					while (true) {
						String s = cache.readLine();
						if (s == null)
							break;

						String[] solution = s.split("\t");
						RDFNode x = sparqlDecode(solution[0], endpoint);
						RDFNode y = solution.length > 1 ? sparqlDecode(solution[1], endpoint) : null;
						extension.add(new RDFNodePair(x, y));
					}
					cache.close();
				} catch (IOException ioe) {
					logger.warn("Cache for " + rootSymbol + " not found. Querying SPARQL endpoint...");
					try {
						PrintStream cache = new PrintStream(cacheName());
						for (int hexDigit = 0; hexDigit < 0x10; hexDigit++) {
							String h = String.format("\"%x\"", hexDigit);
							// SparqlEndpoint endpoint = new SparqlEndpoint(Global.REMOTE_SPARQL_ENDPOINT,
							// 		Global.REMOTE_PREFIXES);
							ResultSet result = endpoint.select("DISTINCT ?x WHERE { " + graphPattern
									+ " FILTER( strStarts(MD5(str(?x)), " + h + ") ) }", 0);

							while (result.hasNext()) {
								QuerySolution solution = result.next();
								RDFNode x = solution.get("x");
								RDFNode y = solution.get("y");
								extension.add(new RDFNodePair(x, y));
								cache.println(sparqlEncode(x) + "\t" + sparqlEncode(y));
							}
						}
						cache.close();
					} catch (Exception e) {
						logger.error(e.getMessage());
						e.printStackTrace();
						System.exit(1);
					}
				}
			}
			logger.info("Found " + extension.size() + " RDF node pairs.");
		}
		return extension;
	}

	/**
	 * Encode an RDF node as a string in a format suitable for embedding in a SPARQL
	 * query.
	 * <p>
	 * Resources are all represented by absolute IRIs, which are surrounded by the
	 * '<' and '>' delimiters.
	 * </p>
	 * <p>
	 * The general syntax for literals is a string (enclosed in either double
	 * quotes, "...", or single quotes, '...'), with either an optional language tag
	 * (introduced by @) or an optional datatype IRI or prefixed name (introduced by
	 * ^^).
	 * </p>
	 * <p>
	 * As a convenience, integers can be written directly (without quotation marks
	 * and an explicit datatype IRI) and are interpreted as typed literals of
	 * datatype <tt>xsd:integer</tt>; decimal numbers for which there is '.' in the
	 * number but no exponent are interpreted as <tt>xsd:decimal</tt>; and numbers
	 * with exponents are interpreted as <tt>xsd:double</tt>. Values of type
	 * <tt>xsd:boolean</tt> can also be written as <code>true</code> or
	 * <code>false</code>.
	 * </p>
	 * 
	 * @param node an RDF node
	 * @return the SPARQL encoding of the given RDF node
	 */
	public static String sparqlEncode(RDFNode node) {
		// If the node is null, return the empty string...
		if (node == null)
			return "";
		// If the node is blank, treat it as a variable, whose name will be based on the
		// identifier:
		if (node.isAnon())
			return "?_" + node.asResource().getId().getLabelString().substring(2);
		if (node.isResource())
			return "<" + node + ">";
		// Otherwise, the node is a literal:
		Literal lit = (Literal) node;
		String lang = lit.getLanguage();
		String dt = lit.getDatatypeURI();
		String s = "\"" + lit.getLexicalForm() + "\"";
		if (lang.length() > 0)
			s += "@" + lang;
		else if (dt != null)
			s += "^^<" + dt + ">";
		// System.out.println("SPARQL Literal: " + s);
		return s;
	}

	/**
	 * Create an RDF node from its SPARQL encoding.
	 * <p>
	 * This is the dual operation of the {@link #sparqlEncode(RDFNode)} method.
	 * 
	 * @param s a SPARQL encoding of an RDF node
	 * @return the corresponding RDF node
	 */
	public static RDFNode sparqlDecode(String s, VirtuosoEndpoint endpoint) {
		// SparqlEndpoint endpoint = new SparqlEndpoint(Global.REMOTE_SPARQL_ENDPOINT, Global.REMOTE_PREFIXES);
		Model m = endpoint.tdb;
		RDFNode r;
		if (s.startsWith("<"))
			r = m.createResource(s.substring(1, s.length() - 1));
		else if (s.startsWith("?_"))
			r = m.createResource(new AnonId(s.substring(2)));
		else
			r = m.createLiteral(s);
		return r;
	}

	/**
	 * Asks the SPARQL endpoint whether this expression contains the given pair of
	 * RDF nodes.
	 * <p>
	 * If either of the nodes is <code>null</code>, it is replaced by a variable in
	 * the query that is submitted to the SPARQL endpoint.
	 * </p>
	 * 
	 * @param pair a pair of RDF nodes.
	 * @return true if the given pair of RDF nodes is a member of the extension of
	 *         this expression.
	 */
	public boolean contains(RDFNodePair pair, VirtuosoEndpoint endpoint) {
		String x = pair.x != null ? sparqlEncode(pair.x) : "?x";
		String y = pair.y != null ? sparqlEncode(pair.y) : "?y";
		// SparqlEndpoint endpoint = new SparqlEndpoint(Global.REMOTE_SPARQL_ENDPOINT, Global.REMOTE_PREFIXES);
		return endpoint.ask(createGraphPattern(x, y), 0);
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder(rootSymbol);
		Iterator<Expression> i = subExpressions.iterator();
		if (i.hasNext()) {
			s.append(" (");
			while (i.hasNext()) {
				s.append(" ").append(i.next());
			}
			s.append(" )");
		}
		return s.toString();
	}

	public String getGraphPattern() {
		return graphPattern;
	}

}
