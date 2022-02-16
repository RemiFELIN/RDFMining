/**
 * 
 */
package com.i3s.app.rdfminer.generator.axiom;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import Individuals.GEChromosome;
import com.i3s.app.rdfminer.grammar.evolutionary.individual.GEIndividual;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.log4j.Logger;

import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.RDFMiner;
import com.i3s.app.rdfminer.expression.Expression;
import com.i3s.app.rdfminer.expression.ExpressionFactory;
import com.i3s.app.rdfminer.sparql.RDFNodePair;
import com.i3s.app.rdfminer.sparql.virtuoso.SparqlEndpoint;

//import com.hp.hpl.jena.query.QuerySolution;
//import com.hp.hpl.jena.rdf.model.RDFNode;

import Individuals.Phenotype;
import Mapper.Production;
import Mapper.Rule;
import Mapper.Symbol;
import Util.Enums;

/**
 * An exhaustive generator of SubClassOf axioms with atomic left- and right-hand
 * side, written for the ISWC 2014 paper.
 * <p>
 * For all classes in the "Class" production, retrieve their instances, and
 * consider as a candidate superclass all the types of those instances. The
 * axioms are returned one by one by the {@link CandidateAxiomGenerator#nextAxiom()} method.
 * </p>
 * <p>
 * A generator of this class saves its status in a file in the current
 * directory, with the conventional name "<tt>AxiomGenerator.status</tt>", so
 * that it will be able to resume from where it left in case of crash. The
 * status file consists of two lines: the first line contains the name of the
 * last sub-class and the second line contains the name of the last super-class
 * for which an axiom was generated (an tested).
 * </p>
 * 
 * @author Andrea G. B. Tettamanzi & Rémi FELIN
 *
 */
public class CandidateAxiomGenerator extends AxiomGenerator {

	private static final Logger logger = Logger.getLogger(CandidateAxiomGenerator.class.getName());

	/**
	 * An iterator on the classes to be used as the sub-class of the candidate
	 * axiom.
	 */
	protected Iterator<Production> subClassIterator;

	/**
	 * An iterator on the classes to be used as the super-class of the candidate
	 * axiom.
	 */
	protected Iterator<RDFNodePair> superClassIterator;

	/**
	 * The production containing the subClass currently used as left-hand side of
	 * the axiom.
	 */
	protected Production subClass;

	/**
	 * The last node used as superClass.
	 */
	protected RDFNode lastNode = null;

	/**
	 * The name of a file used to make the generator status persistent, so that in
	 * case of crash, the process may resume from where it left.
	 * <p>
	 * The file name has a <code>.status</code> extension to make it recognizable.
	 * Furthermore, to allow running different experiments in parallel, it has a
	 * suffix based on the time-out used for axiom test.
	 * </p>
	 */
	protected final String statusFileName = "AxiomGenerator-"
			+ (RDFMiner.parameters.timeOut > 0 ? "-" + RDFMiner.parameters.timeOut : "") + ".status";

	/**
	 * Constructs a new axiom generator for the language described by the given
	 * grammar.
	 * 
	 * @param fileName the name of the file containing the grammar.
	 */
	public CandidateAxiomGenerator(String fileName, boolean v2) {
		super(fileName, v2);
		logger.warn("Grammar Successfully Initialized");

		Rule rule = grammar.findRule("Class");
		subClassIterator = rule.iterator();
		// We assign an empty iterator to the super-class iterator:
		superClassIterator = (new TreeSet<RDFNodePair>()).iterator();

		try {
			// Try to read the status file:
			BufferedReader status = new BufferedReader(new FileReader(statusFileName));
			String subClassName = status.readLine();
			subClass = new Production();
			subClass.add(new Symbol(subClassName, Enums.SymbolType.TSymbol));
			String superClassName = status.readLine();
			status.close();
			// Find the position of the subClass production in the rule for "Class":
			int subClassIndex = rule.size();
			while (--subClassIndex >= 0) {
				if (subClassName.equals(rule.get(subClassIndex).get(0).toString()))
					break;
			}
			if (subClassIndex < 0) {
				logger.error("Sub class " + subClassName + " not found in rule:");
				logger.error(rule.toString());
				throw new IOException(subClassName);
			}
			// Extract the sub-list from the next one till the end, and get its iterator:
			subClassIterator = rule.subList(subClassIndex + 1, rule.size()).iterator();

			// Now that we have the sub-class iterator correctly positioned...
			// We have to position the iterator to the super-class.
			Expression expr = ExpressionFactory.createClass(subClass);
			Set<RDFNodePair> types = getNodes(
					"DISTINCT ?class WHERE { " + expr.createGraphPattern("?x", "?y") + "\n" + "?x a ?class . }");
			superClassIterator = types.iterator();
			// Unroll the super class iterator up to the saved super class:
			while (superClassIterator.hasNext()) {
				lastNode = superClassIterator.next().x;
				logger.info("Comparing " + superClassName + " to " + lastNode);
				if (superClassName.equals(lastNode.toString()))
					break;
			}

		} catch (IOException e) {
			logger.warn("No previously saved status found. Starting from scratch...");
		}
		logger.warn("Axiom Generator Initialized");
	}

	/**
	 * Query the SPARQL endpoint.
	 * 
	 * @param sparql a SPARQL query
	 * @return the results of the query
	 */
	protected Set<RDFNodePair> getNodes(String sparql) {
		Set<RDFNodePair> classes = new TreeSet<RDFNodePair>();
		logger.warn("Querying DBpedia with query " + sparql);
		SparqlEndpoint endpoint = new SparqlEndpoint(Global.VIRTUOSO_REMOTE_SPARQL_ENDPOINT, Global.VIRTUOSO_REMOTE_PREFIXES);
		ResultSet result = endpoint.select(sparql, 0);
		
		while (result.hasNext()) {
			QuerySolution solution = result.next();
			RDFNode x = solution.get("class");
			RDFNode y = solution.get("y");
			if (!Expression.sparqlEncode(x).equals(subClass.get(0).toString()))
				classes.add(new RDFNodePair(x, y));
		}
		return classes;
	}

	@Override
	public Phenotype nextAxiom() {
		// First of all, save the previous status to file:
		if (subClass != null) // ... except for the first time
		{
			try {
				PrintStream status = new PrintStream(statusFileName);
				status.println(subClass.get(0).toString());
				status.println(lastNode.toString()); // Or should we SPARQL-encode it?
				status.close();
			} catch (IOException e) {
				logger.warn("Could not save status.");
			}
		}

		// now, construct the next axiom:
		while (!superClassIterator.hasNext()) {
			logger.debug("Switching to the next subclass");
			if (!subClassIterator.hasNext())
				return null;
			Set<RDFNodePair> types = null;
			// do // Uncomment this to skip classes having fewer than 30 super-classes.
			// {
			subClass = subClassIterator.next();
			logger.debug("Subclass is now " + subClass);
			Expression expr = ExpressionFactory.createClass(subClass);
			types = getNodes(
					"DISTINCT ?class WHERE { " + expr.createGraphPattern("?x", "?y") + "\n" + "?x a ?class . }");
			logger.debug("Found " + types.size() + " classes!");
			// }
			// while(/* types.size()>=30 || */ types.size()<30);
			superClassIterator = types.iterator();
		}

		Phenotype axiom = new Phenotype();
		axiom.add(new Symbol("SubClassOf", Enums.SymbolType.TSymbol));
		axiom.add(new Symbol("(", Enums.SymbolType.TSymbol));
		axiom.addAll(subClass);
		axiom.add(new Symbol(" ", Enums.SymbolType.TSymbol));
		lastNode = superClassIterator.next().x;
		Symbol superClass = new Symbol(Expression.sparqlEncode(lastNode), Enums.SymbolType.TSymbol);
		axiom.add(superClass);
		axiom.add(new Symbol(")", Enums.SymbolType.TSymbol));
		return axiom;
	}

}
