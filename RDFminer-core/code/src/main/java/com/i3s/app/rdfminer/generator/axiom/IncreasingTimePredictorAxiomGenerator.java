/**
 * 
 */
package com.i3s.app.rdfminer.generator.axiom;

import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.Parameters;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.Phenotype;
import com.i3s.app.rdfminer.evolutionary.geva.Mapper.Production;
import com.i3s.app.rdfminer.evolutionary.geva.Mapper.Rule;
import com.i3s.app.rdfminer.evolutionary.geva.Mapper.Symbol;
import com.i3s.app.rdfminer.evolutionary.geva.Util.Enums;
import com.i3s.app.rdfminer.expression.Expression;
import com.i3s.app.rdfminer.expression.ExpressionFactory;
import com.i3s.app.rdfminer.sparql.corese.CoreseEndpoint;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.util.*;

/**
 * An generator of SubClassOf axioms with atomic left- and right-hand side,
 * which constructs axioms from a list of subclasses.
 * <p>
 * For all classes in the list, retrieve their instances, and consider as a
 * candidate superclass all the types of those instances. The axioms are
 * returned one by one by the {@link AxiomGenerator#nextAxiom()} method.
 * </p>
 * <p>
 * A generator of this class saves its status in a file in the current
 * directory, with the conventional name
 * "<tt>IncreasingTPAxiomGenerator.status</tt>", so that it will be able to
 * resume from where it left in case of crash. The status file consists of two
 * lines: the first line contains the name of the last sub-class and the second
 * line contains the name of the last super-class for which an axiom was
 * generated (an tested).
 * </p>
 * <p>
 * This axiom generator was written for the K-Cap 2015 paper. The reason for its
 * name is that it has been used to generate axioms in increasing order of
 * time-predictor value.
 * </p>
 * 
 * @author Andrea G. B. Tettamanzi & Rémi FELIN
 *
 */
public class IncreasingTimePredictorAxiomGenerator extends AxiomGenerator {

	private static final Logger logger = Logger.getLogger(IncreasingTimePredictorAxiomGenerator.class.getName());

	Parameters parameters = Parameters.getInstance();

	/**
	 * An iterator on the classes to be used as the sub-class of the candidate
	 * axiom.
	 */
	protected Iterator<Production> subClassIterator;

	/**
	 * An iterator on the classes to be used as the super-class of the candidate
	 * axiom.
	 */
	protected Iterator<String> superClassIterator;

	/**
	 * The production containing the subClass currently used as left-hand side of
	 * the axiom.
	 */
	protected Production subClass;

	/**
	 * The last node used as superClass.
	 */
	protected String lastNode = null;

	/**
	 * The name of a file used to make the generator status persistent, so that in
	 * case of crash, the process may resume from where it left.
	 * <p>
	 * The file name has a <code>.status</code> extension to make it recognizable.
	 * Furthermore, to allow running different experiments in parallel, it has a
	 * suffix based on the time-out used for axiom test.
	 * </p>
	 */
	protected final String statusFileName = "IncreasingTPAxiomGenerator-"
			+ (parameters.getSparqlTimeOut() > 0 ? "-" + parameters.getSparqlTimeOut() : "") + ".status";

	/**
	 * Constructs a new axiom generator from a list of subclasses.
	 * 
	 * @param fileName the name of the file containing the list of subclasses.
	 */
	public IncreasingTimePredictorAxiomGenerator(String fileName) {
		super(); // there is no grammar

		Rule rule = new Rule();
		try {
			// Try to read the file with the subclass list:
			BufferedReader file = new BufferedReader(new FileReader(fileName));
			do {
				String subClassName = file.readLine();
				if (subClassName == null)
					break;
				Production prod = new Production();
				prod.add(new Symbol(subClassName, Enums.SymbolType.TSymbol));
				rule.add(prod);
			} while (true);
			file.close();
		} catch (IOException e) {
			logger.error("No subclass list found.");
		}

		subClassIterator = rule.iterator();
		// We assign an empty iterator to the super-class iterator:
		superClassIterator = Collections.emptyIterator();

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
			Set<String> types = getNodes(expr.createGraphPattern("?x", "?y") + "\n" + "?x a ?class . ");
			superClassIterator = types.iterator();
			// Unroll the super class iterator up to the saved super class:
			while (superClassIterator.hasNext()) {
				lastNode = superClassIterator.next();
				logger.info("Comparing " + superClassName + " to " + lastNode);
				if (superClassName.equals(lastNode.toString()))
					break;
			}

		} catch (IOException e) {
			logger.warn("No previously saved status found. Starting from scratch...");
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		logger.warn("Axiom Generator Initialized");
	}

	/**
	 * Query the SPARQL endpoint.
	 * 
	 * @param sparql a SPARQL query
	 * @return the results of the query
	 */
	protected Set<String> getNodes(String sparql) throws URISyntaxException, IOException {
		logger.warn("Querying DBpedia with query " + sparql);
		CoreseEndpoint endpoint = new CoreseEndpoint(Global.SPARQL_ENDPOINT, Global.PREFIXES);
		List<String> results = endpoint.select("?class", sparql, false);
		return new TreeSet<>(results);
	}

	@Override
	public Phenotype nextAxiom() throws URISyntaxException, IOException {
		// First, save the previous status to file:
		if (subClass != null) {
			// ... except for the first time
			try {
				PrintStream status = new PrintStream(statusFileName);
				status.println(subClass.get(0).toString());
				status.println(lastNode); // Or should we SPARQL-encode it?
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
			Set<String> types;
			// do // Uncomment this to skip classes having fewer than 30 super-classes.
			// {
			subClass = subClassIterator.next();
			logger.debug("Subclass is now " + subClass);
			Expression expr = ExpressionFactory.createClass(subClass);
			types = getNodes(expr.createGraphPattern("?x", "?y") + "\n" + "?x a ?class . ");
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
		lastNode = superClassIterator.next();
		Symbol superClass = new Symbol(lastNode, Enums.SymbolType.TSymbol);
		axiom.add(superClass);
		axiom.add(new Symbol(")", Enums.SymbolType.TSymbol));
		return axiom;
	}

}
