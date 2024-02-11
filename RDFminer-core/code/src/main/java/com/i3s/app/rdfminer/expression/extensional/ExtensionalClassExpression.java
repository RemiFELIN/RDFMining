/**
 * 
 */
package com.i3s.app.rdfminer.expression.extensional;

import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.evolutionary.geva.Mapper.Symbol;
import com.i3s.app.rdfminer.sparql.RDFNodePair;
import com.i3s.app.rdfminer.sparql.virtuoso.VirtuosoEndpoint;
import org.apache.jena.rdf.model.AnonId;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;

import java.util.List;
import java.util.TreeSet;

/**
 * A class expression of the form ObjectOneOf(...).
 * 
 * @author Andrea G. B. Tettamanzi & Rémi FELIN
 *
 */
public class ExtensionalClassExpression extends ExtensionalExpression {

	/**
	 * Creates a new extensional class expression based on the provided
	 * functional-style syntax.
	 * <p>
	 * This expression will be the set of the nominals or literals given as its
	 * subexpressions.
	 * </p>
	 * <p>
	 * The extension of this expression is pre-compiled based on the arguments of
	 * the <code>ObjectOneOf</code> constructor.
	 * </p>
	 * 
	 * @param syntax an expression in OWL 2 functional-style syntax.
	 */
	public ExtensionalClassExpression(List<List<Symbol>> syntax) {
		super();
		rootSymbol = "ObjectOneOf (";
		extension = new TreeSet<>();
		VirtuosoEndpoint endpoint = new VirtuosoEndpoint(Global.SPARQL_ENDPOINT, Global.PREFIXES);
		Model m = endpoint.tdb;

		for (List<Symbol> symbols : syntax) {
			RDFNode r;
			Symbol sym = symbols.get(0);
			rootSymbol += " " + sym;
			// We create an RDF node from the symbol, by removing the "<" and ">"
			// delimiters, unless it is a blank node
			String s = sym.getSymbolString();
			if (s.startsWith("<"))
				r = m.createResource(s.substring(1, s.length() - 1));
			else
				r = m.createResource(new AnonId(s));
			extension.add(new RDFNodePair(r, null));
		}
		
		rootSymbol += " )";
		graphPattern = createGraphPattern("?x", "?y");
	}

	/**
	 * Instantiates the graph pattern for this extensional class expression.
	 * 
	 * @param subject the expression replacing the <tt>?x</tt> SPARQL variable
	 * @param object  this expression is ignored
	 * @return the SPARQL graph pattern for this expression
	 */
	@Override
	public String createGraphPattern(String subject, String object) {
		String s = getFreshVariableName();
		String p1 = getFreshVariableName();
		String p2 = getFreshVariableName();
		String o = getFreshVariableName();
		return " { " + subject + " " + p1 + " " + o + " } UNION { " + s + " " + p2 + " " + subject + " } .\n"
				+ getFilter(subject, false) + " .\n";
	}

}
