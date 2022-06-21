/**
 * 
 */
package com.i3s.app.rdfminer.axiom.type;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.sparql.corese.CoreseEndpoint;
import com.i3s.app.rdfminer.sparql.corese.Format;
import com.i3s.app.rdfminer.sparql.corese.ResultParser;
import org.apache.jena.atlas.lib.Timer;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.log4j.Logger;

import com.i3s.app.rdfminer.RDFMiner;
import com.i3s.app.rdfminer.axiom.Axiom;
import com.i3s.app.rdfminer.expression.Expression;
import com.i3s.app.rdfminer.expression.ExpressionFactory;
import com.i3s.app.rdfminer.expression.complement.ComplementClassExpression;
import com.i3s.app.rdfminer.sparql.RDFNodePair;
import com.i3s.app.rdfminer.sparql.virtuoso.VirtuosoEndpoint;
import com.i3s.app.rdfminer.tools.TimeMap;

import Mapper.Symbol;

/**
 * A class that represents a <code>SubClassOf</code> axiom.
 * 
 * @author Andrea G. B. Tettamanzi & Rémi FELIN
 *
 */
public class SubClassOfAxiom extends Axiom {

	private static Logger logger = Logger.getLogger(SubClassOfAxiom.class.getName());

	/**
	 * The subclass expression.
	 */
	protected Expression subClass;

	/**
	 * The superclass expression.
	 */
	protected Expression superClass;

	/**
	 * The complement expression of the superclass.
	 */
	protected Expression superClassComplement;

	/**
	 * The time predictor for this axiom.
	 */
	protected long timePredictor;

	/**
	 * A map to hold the maximum test time observed so far for an accepted
	 * SubClassOf axiom.
	 */
	public static TimeMap maxTestTime = new TimeMap();

	/**
	 * The complexity of current axiom : if one (or both) of the part is composed of two or more URI
	 */
	public boolean complex = false;

	/**
	 * Create a new <code>SubClassOf</code> object expression axiom from the two
	 * given concept expressions.
	 * 
	 * @param subClassExpression   the functional-style expression of the subclass
	 * @param superClassExpression the functional-style expression of the superclass
	 * @param endpoint             the sparql endpoint used for the queries
	 */
	public SubClassOfAxiom(List<Symbol> subClassExpression, List<Symbol> superClassExpression,
			VirtuosoEndpoint endpoint) {
		// set a t0 using the CPU time
		long t0 = getProcessCPUTime();
		subClass = ExpressionFactory.createClass(subClassExpression);
		superClass = ExpressionFactory.createClass(superClassExpression);
		logger.info("subClass: " + subClass + " | superClass: " + superClass);
		// define if the current axiom is complex
		if(subClassExpression.size() > 1 || superClassExpression.size() > 1) {
			complex = true;
		}
		// Handle the double negation in an optimized way:
		if (superClass instanceof ComplementClassExpression)
			superClassComplement = superClass.subExpressions.get(0);
		else
			superClassComplement = new ComplementClassExpression(superClass);
		try {
			update(endpoint);
		} catch (IllegalStateException e) {
			// This is the conventional unchecked exception thrown by the
			// Sparql endpoint if an HTTP 504 Gateway Time-out occurs.
			// In that case, we try a slower, but safer, naive update as the last resort:
			logger.warn("Trying a naive update: this is going to take some time...");
			naiveUpdate(endpoint);
		}
		// set elapsedTime as a CPU usage time
		elapsedTime = getProcessCPUTime() - t0;
		logger.info("elapsed time = " + elapsedTime + " ms.");
	}

	/**
	 * Updates the counts used to compute the possibility and necessity degrees.
	 * <p>
	 * According to the model-theoretic semantics, an axiom of the form
	 * <code>SubClassOf(CE<sub>1</sub> CE<sub>2</sub>)</code> is satisfied if
	 * <i>(CE<sub>1</sub>)<sup>C</sup></i> &sube;
	 * <i>(CE<sub>2</sub>)<sup>C</sup></i>.
	 * </p>
	 * <p>
	 * Therefore,
	 * </p>
	 * <ul>
	 * <li>the universe of discourse is the extension of
	 * <code>CE<sub>1</sub></code>;</li>
	 * <li>confirmations are RDF nodes <var>x</var> such that <var>x</var> &in;
	 * (CE<sub>2</sub>)<sup>C</sup>;</li>
	 * <li>exceptions are RDF nodes <var>x</var> such that <var>x</var> &in;
	 * (<code>ComplementOf</code>(CE<sub>2</sub>))<sup>C</sup>.</li>
	 * </ul>
	 * <p>
	 * This method, which is provided as a fallback in case {@link #update()} does
	 * not work, uses a much slower, but hopefully safer, way of updating the counts
	 * than the {@link #update()} method. The extension of the sub-class is
	 * retrieved, than each individual instance is checked for membership in the
	 * super-class and in the complement of the super-class by issuing two ASK
	 * queries.
	 * </p>
	 * 
	 */
	public void naiveUpdate(VirtuosoEndpoint endpoint) {
		referenceCardinality = numConfirmations = numExceptions = 0;
		confirmations = new ArrayList<String>();
		exceptions = new ArrayList<String>();
		Set<RDFNodePair> extension = subClass.extension(endpoint);
		int numIntersectingClasses = endpoint.count("?D", subClass.graphPattern + " ?x a ?D . ", 0);
		timePredictor = (long) referenceCardinality * numIntersectingClasses;

		for (RDFNodePair rdfNodePair : extension) {
			referenceCardinality++;
			if (superClass.contains(rdfNodePair, endpoint)) {
				numConfirmations++;
				confirmations.add(Expression.sparqlEncode(rdfNodePair.x));
			}
			// The following is correct, but not optimized (a lot of duplicated tests)
			else if (superClassComplement.contains(rdfNodePair, endpoint)) {
				numExceptions++;
				exceptions.add(Expression.sparqlEncode(rdfNodePair.x));
//				logger.info("Found exception: " + pair);
			}
			// A better idea would be to issue a SPARQL query
			// and let the SPARQL endpoint do the work: see the method below...
		}
		logger.info("Reference cardinality: " + referenceCardinality);
		logger.info("Number of confirmation(s): " + numConfirmations);
		logger.info("Number of exception(s): " + numExceptions);
	}

	/**
	 * Updates the counts used to compute the possibility and necessity degrees.
	 * <p>
	 * According to the model-theoretic semantics, an axiom of the form
	 * <code>SubClassOf(CE<sub>1</sub> CE<sub>2</sub>)</code> is satisfied if
	 * <i>(CE<sub>1</sub>)<sup>C</sup></i> &sube;
	 * <i>(CE<sub>2</sub>)<sup>C</sup></i>.
	 * </p>
	 * <p>
	 * Therefore,
	 * </p>
	 * <ul>
	 * <li>the universe of discourse is the extension of
	 * <code>CE<sub>1</sub></code>;</li>
	 * <li>confirmations are RDF nodes <var>x</var> such that <var>x</var> &in;
	 * (CE<sub>2</sub>)<sup>C</sup>;</li>
	 * <li>exceptions are RDF nodes <var>x</var> such that <var>x</var> &in;
	 * (<code>ComplementOf</code>(CE<sub>2</sub>))<sup>C</sup>.</li>
	 * </ul>
	 * <p>
	 * The updating of the counts is performed by issuing three SPARQL queries of
	 * the form <code>SELECT count(DISTINCT ?x) AS</code> <var>n</var>
	 * <code>WHERE</code>. If the number of confirmations or exceptions is not too
	 * large (currently, below 100), they are downloaded from the SPARQL endpoint
	 * and stored in a list.
	 * </p>
	 * <p>
	 * The {@link #naiveUpdate(VirtuosoEndpoint) naiveUpdate} method provides a slower, but hopefully safer,
	 * way of updating the counts.
	 * </p>
	 */
	@Override
	public void update(VirtuosoEndpoint endpoint) {
		// First of all, we verify if a such assumption does not already exists
		// Only simple OWL 2 subClassOf axioms are considered in this case
		// This checking part is an temporary solution
		// TODO: in the future, we will consider all existing axioms as knowledge to improve OWL 2 Axioms mining (in GE, ...)
		if(!complex && endpoint.ask(subClass + " rdfs:subClassOf " + superClass, 0)) {
			// in this case, we set pos = nec = 1.0 as consequence to its existance in ontology
			logger.info("This axiom is defined in the ontology ...");
			referenceCardinality = numConfirmations = endpoint.count("?x", subClass.graphPattern, 0);
			numExceptions = 0;
			ari = ARI();
			return;
		}
		// If it does not exists, we need to evaluate it
		confirmations = new ArrayList<>();
		exceptions = new ArrayList<>();
		long timeSpent;
		// The reference cardinality will count all the instances involved by the current axiom
		referenceCardinality = endpoint.count("?x", subClass.graphPattern, 0);
		logger.info("Reference cardinality = " + referenceCardinality);
		// The number of instances linked with the subClass of the given axiom
		numIntersectingClasses = endpoint.count("?D", subClass.graphPattern + " ?x a ?D . ", 0);
		logger.info("No. of Intersecting Classes = " + numIntersectingClasses);
//		timePredictor = (long) referenceCardinality * numIntersectingClasses;
		numConfirmations = endpoint.count("?x", subClass.graphPattern + "\n" + superClass.graphPattern, 0);
		if (numConfirmations > 0) {
			logger.info(numConfirmations + " confirmation(s) found ...");
			if(numConfirmations < 100) {
				logger.info("retrieving in collection ...");
				// query the confirmations
				ResultSet cfs = endpoint.select("DISTINCT ?x WHERE { " + subClass.graphPattern + "\n" + superClass.graphPattern + " }", 0);
				while (cfs.hasNext()) {
					QuerySolution solution = cfs.next();
					RDFNode x = solution.get("x");
					confirmations.add(Expression.sparqlEncode(x));
				}
			}
		}
		// Now, let's compute the exceptions for this axiom 
		if (numConfirmations == referenceCardinality) {
			// No need to count the exceptions: there can't be any!
			numExceptions = 0;
			// set the ARI of axiom
			ari = ARI();
			logger.info("ARI = " + ari);
			return;
			
		} else if (RDFMiner.parameters.timeOut > 0 || RDFMiner.parameters.dynTimeOut != 0.0) {
			logger.info("compute the number of exceptions with a timeout ...");
			// Since the query to count exception is complex and may take very long to
			// execute,
			// we execute it with the user-supplied time out.
			// Compute the time-out (in seconds):
			long timeOut = RDFMiner.parameters.timeOut;
			timeOut += Math.round(RDFMiner.parameters.dynTimeOut * timePredictor);
			// Set a timer to compute the result time of each query
			Timer timer = new Timer();
			timer.startTimer();
			numExceptions = endpoint.count("?x", subClass.graphPattern + "\n" + superClassComplement.graphPattern, (int) timeOut);
			timeSpent = timer.endTimer();
			// If no exceptions are raised
			logger.info("Exceptions query finished - time spent: " + timeSpent + "ms.");
			
			if ( timeSpent > ((int) timeOut * 1000L)) {
				logger.warn("Timeout is reached");
				// If the query times out, it is very likely that it would end up
				// having a large number of exceptions. Therefore, we take the reference
				// cardinality minus the number of confirmations as the conventional
				// number of exceptions in this case.
				numExceptions = referenceCardinality - numConfirmations;
				// Specify isTimeout for this axiom
				isTimeout = true;
			}
			
		} else {
			// Set a timer to compute the result time of each query
//			Timer timer = new Timer();
//			timer.startTimer();
			// logger.warn("Time Out = 0 s");
			// This is the EKAW 2014 version, without time-out:
//			numExceptions = endpoint.count("?x",
//					subClass.graphPattern + "\n" + superClassComplement.graphPattern, 0);
			if (RDFMiner.parameters.loop) {
				try {
					getExceptionsUsingCoreseLoop();
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(1);
				}
			} else {
				getExceptions(endpoint, 1000);
			}
//			timeSpent = timer.endTimer();
			// Log the response time
//			logger.info("Exceptions query finished - time spent: " + timeSpent + "ms.");
		}
//		// We don't need to compute exceptions if we get a timeout from exceptions SPARQL request
		if (numExceptions > 0 && numExceptions < 100 && !isTimeout && RDFMiner.parameters.timeOut > 0) {
			logger.info(numExceptions + " exception(s) found ! retrieving in collection ...");
			// retrieve the exceptions
			ResultSet exc = endpoint.select("DISTINCT ?x WHERE { " + subClass.graphPattern + "\n"
					+ superClassComplement.graphPattern + " }", 0);
			while (exc.hasNext()) {
				QuerySolution solution = exc.next();
				RDFNode x = solution.get("x");
				exceptions.add(Expression.sparqlEncode(x));
			}
		}
		// set the time spent for the computation of exceptions
//		elapsedTime = timeSpent;
		// logger.info("Possibility = " + possibility().doubleValue());
		// logger.info("Necessity = " + necessity().doubleValue());
		// set the ARI of axiom
		ari = ARI();
		logger.info("ARI = " + ari);
	}

	public void getExceptionsUsingCoreseLoop() throws URISyntaxException, IOException {
		logger.info("Compute the number of exceptions with a proposal optimization and loop operator from Corese ...");
		CoreseEndpoint corese = new CoreseEndpoint(Global.CORESE_IP_ADDRESS, null);
		// Writing the query using loop operator, we will ask our Virtuoso server from the Corese server as a SERVICE
		String query = "@timeout 100000000\n" +
				"SELECT distinct ?x WHERE \n" +
				"{\n" +
				"\n" +
				"    SERVICE <http://134.59.130.136:9200/sparql?loop=true&limit=1000> {\n" +
				"        SELECT distinct ?t WHERE {\n" +
				"            " + subClass.graphPattern + " ?x a ?t\n" +
				"        }      \n" +
				"    }\n" +
				"    \n" +
				"    SERVICE <http://134.59.130.136:9200/sparql> {\n" +
				"        values ?t {undef}\n" +
				"        FILTER NOT EXISTS {\n" +
				"            " + superClass.graphPattern + " ?x a ?t \n" +
				"        }\n" +
				"    }\n" +
				"\n" +
				"    SERVICE <http://134.59.130.136:9200/sparql?loop=true&limit=10000> {\n" +
				"        " + subClass.graphPattern + " ?x a ?t\n" +
				"    }\n" +
				"    \n" +
				"}";
		String resultsAsJSON = corese.select(Format.JSON, query);
		List<String> instances = ResultParser.getResultsfromVariable("x", resultsAsJSON);
		logger.info(instances.size() + " exception(s) found ...");
		numExceptions = instances.size();
		if (numExceptions > 0 && numExceptions < 100) exceptions = instances;
	}

	public void getExceptions(VirtuosoEndpoint endpoint, int size) {
		logger.info("Compute the number of exceptions with a proposal optimization ...");
		int offset = 0;
		List<String> types = new ArrayList<>();
		// get all types related to the subClassExpression for which it does not exists any ?z of this type and superClassExpression
		while(offset != numIntersectingClasses) {
			ResultSet cfs = endpoint.select("distinct(?t) WHERE { " +
					"{ " +
						"SELECT ?t WHERE { " +
							"{ " +
								"SELECT distinct(?t) WHERE { " +
									subClass.graphPattern + " ?x a ?t " +
								"} ORDER BY ?t " +
							"} " +
						"} LIMIT " + size + " OFFSET " + offset + " " +
					"} " +
					"FILTER NOT EXISTS { " +
						superClass.graphPattern + " ?x a ?t" +
					"} } ", 0);
			while (cfs.hasNext()) {
				QuerySolution solution = cfs.next();
				RDFNode t = solution.get("t");
				types.add(Expression.sparqlEncode(t));
			}
			offset += Math.min(numIntersectingClasses - offset, size);
		}
		if(types.size() != 0)
			logger.info(types.size() + " type(s) where we don't observe a link with the superClass ...");
		// truncate query
		// for each types in the list, we will search any instances such as :
		int i = 0;
		int k = 50;
		// set the LIMIT ... OFFSET ... values
		int limit = 10000;
		List<String> instances = new ArrayList<>();
		while(i != types.size()) {
			offset = 0;
			int end = Math.min(i + k, types.size());
			StringBuilder body = new StringBuilder(subClass.graphPattern +
					"?x a ?t values (?t) { ");
			for(String type : types.subList(i, end)) {
				body.append("(").append(type).append(") ");
			}
			body.append("} ");
			while(true) {
//				logger.info("truncate request\n" + "DISTINCT ?x where { " + body + "} LIMIT " + limit + " OFFSET " + offset);
				ResultSet cfs = endpoint.select("DISTINCT ?x where { " + body + "} LIMIT " + limit + " OFFSET " + offset , 0);
				while (cfs.hasNext()) {
					QuerySolution solution = cfs.next();
					RDFNode x = solution.get("x");
					// to remove duplicated ?x (cause of truncation)
					// if a given ?x is not on a list , we add it
					if(!instances.contains(Expression.sparqlEncode(x)))
						instances.add(Expression.sparqlEncode(x));
				}
//				logger.info("[DEBUG] cfs.getRowNumber() = " + cfs.getRowNumber());
				if(cfs.getRowNumber() < limit) {
					break;
				} else {
//					logger.info("[DEBUG] Increment offset ...");
					offset += limit;
				}
			}

			i += Math.min(types.size() - i, k);
		}
		logger.info(instances.size() + " exception(s) found ...");
		numExceptions = instances.size();
		if (numExceptions > 0 && numExceptions < 100) exceptions = instances;
	}

	/**
	 * Return the time predictor for this axiom.
	 * 
	 * @return the value of the time predictor.
	 */
	public long timePredictor() {
		return timePredictor;
	}

	@Override
	public void update() {}

}
