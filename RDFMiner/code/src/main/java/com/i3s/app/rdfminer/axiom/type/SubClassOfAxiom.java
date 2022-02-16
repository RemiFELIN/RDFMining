/**
 * 
 */
package com.i3s.app.rdfminer.axiom.type;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
import com.i3s.app.rdfminer.sparql.virtuoso.SparqlEndpoint;
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
	 * Create a new <code>SubClassOf</code> object expression axiom from the two
	 * given concept expressions.
	 * 
	 * @param subClassExpression   the functional-style expression of the subclass
	 * @param superClassExpression the functional-style expression of the superclass
	 * @param endpoint             the sparql endpoint used for the queries
	 */
	public SubClassOfAxiom(List<Symbol> subClassExpression, List<Symbol> superClassExpression,
			SparqlEndpoint endpoint) {
//		logger.error("subClass: " + subClassExpression);
//		logger.error("superClass: " + superClassExpression);
		subClass = ExpressionFactory.createClass(subClassExpression);
		superClass = ExpressionFactory.createClass(superClassExpression);
		if (superClass instanceof ComplementClassExpression)
			// Handle the double negation in an optimized way:
			superClassComplement = superClass.subExpressions.get(0);
		else
			superClassComplement = new ComplementClassExpression(superClass);
//		logger.info("------\nsubClass: " + subClass.getGraphPattern() + "\n---\nsuperClass: " + superClass.getGraphPattern() + "\n------");
		try {
			update(endpoint);
		} catch (IllegalStateException e) {
			// This is the conventional unchecked exception thrown by the
			// Sparql endpoint if an HTTP 504 Gateway Time-out occurs.
			// In that case, we try a slower, but safer, naive update as the last resort:
			logger.warn("Trying a naive update: this is going to take some time...");
			naiveUpdate(endpoint);
		}
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
	public void naiveUpdate(SparqlEndpoint endpoint) {
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
	 * The {@link #naiveUpdate(SparqlEndpoint) naiveUpdate} method provides a slower, but hopefully safer,
	 * way of updating the counts.
	 * </p>
	 */
	@Override
	public void update(SparqlEndpoint endpoint) {
		confirmations = new ArrayList<String>();
		exceptions = new ArrayList<String>();
		long timeSpent = 0;
		referenceCardinality = endpoint.count("?x", subClass.graphPattern, 0);
		logger.info("referenceCardinality = " + referenceCardinality);
		int numIntersectingClasses = endpoint.count("?D", subClass.graphPattern + " ?x a ?D . ", 0);
		logger.info("No. of Intersecting Classes = " + numIntersectingClasses);
		timePredictor = (long) referenceCardinality * numIntersectingClasses;
		numConfirmations = endpoint.count("?x", subClass.graphPattern + "\n" + superClass.graphPattern, 0);
		
		if (numConfirmations > 0 && numConfirmations < 100) {
			logger.info(numConfirmations + " confirmation(s) found ! retrieving in collection ...");
			// query the confirmations
			ResultSet cfs = endpoint.select("DISTINCT ?x WHERE { " + subClass.graphPattern + "\n" + superClass.graphPattern + " }", 0);
			while (cfs.hasNext()) {
				QuerySolution solution = cfs.next();
				RDFNode x = solution.get("x");
				confirmations.add(Expression.sparqlEncode(x));
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
			// Since the query to count exception is complex and may take very long to
			// execute,
			// we execute it with the user-supplied time out.
			// Compute the time-out (in seconds):
			long timeOut = RDFMiner.parameters.timeOut;
			timeOut += (long) Math.round(RDFMiner.parameters.dynTimeOut * timePredictor);
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
			Timer timer = new Timer();
			timer.startTimer();
			// logger.warn("Time Out = 0 s");
			// This is the EKAW 2014 version, without time-out:
//			numExceptions = endpoint.count("?x",
//					subClass.graphPattern + "\n" + superClassComplement.graphPattern, 0);
			getExceptions(endpoint);
			timeSpent = timer.endTimer();
			// Log the response time
			logger.info("Exceptions query finished - time spent: " + timeSpent + "ms.");
		}
		// We don't need to compute exceptions if we get a timeout from exceptions SPARQL request
//		if (numExceptions > 0 && numExceptions < 100 && !isTimeout) {
//			logger.info(numExceptions + " exception(s) found ! retrieving in collection ...");
//			// retrieve the exceptions
//			ResultSet exc = endpoint.select("DISTINCT ?x WHERE { " + subClass.graphPattern + "\n"
//					+ superClassComplement.graphPattern + " }", 0);
//			while (exc.hasNext()) {
//				QuerySolution solution = exc.next();
//				RDFNode x = solution.get("x");
//				exceptions.add(Expression.sparqlEncode(x));
//			}
//		}
		// set the time spent for the computation of exceptions
		elapsedTime = timeSpent;
		// logger.info("Possibility = " + possibility().doubleValue());
		// logger.info("Necessity = " + necessity().doubleValue());
		// set the ARI of axiom
		ari = ARI();
		logger.info("ARI = " + ari);
	}

	public void getExceptions(SparqlEndpoint endpoint) {
		// sub correspond to the graph pattern without the "." char at the end
		String sub = subClass.graphPattern.substring(0, subClass.graphPattern.length() - 1);
		// SELECT count(DISTINCT(?t)) WHERE { ?x a <http://dbpedia.org/ontology/SoccerClub> , ?t }
		int nTypes = endpoint.count("?t", sub + ", ?t", 0);
		if(nTypes != 0)
			logger.info(nTypes + " additionnal type(s) linked to it subClass ...");
		// truncate query :
		// get all types related to the subClassExpression for which it does not exists any ?z of this type and superClassExpression
		int offset = 0;
		int size = 1000;
		List<String> types = new ArrayList<>();
//		logger.info("find all types exception ...");
		while(offset != nTypes) {
			ResultSet cfs = endpoint.select("distinct(?t) WHERE { " +
					"{ " +
						"SELECT ?t WHERE { " +
							"{ " +
								"SELECT distinct(?t) WHERE { " +
									sub + ", ?t " +
								"} ORDER BY ?t " +
							"} " +
						"} LIMIT " + size + " OFFSET " + offset + " " +
					"} " +
					"FILTER NOT EXISTS { " +
						"?z a ?t, " + superClass + " " +
					"} } ", 0);
			while (cfs.hasNext()) {
				QuerySolution solution = cfs.next();
				RDFNode t = solution.get("t");
				types.add(Expression.sparqlEncode(t));
			}
			offset += Math.min(nTypes - offset, size);
//			logger.info("offset=" + offset);
		}
		if(types.size() != 0)
			logger.info(types.size() + " type(s) where we don't observe a link with the superClass ...");
		// truncate query
		// for each types in the list, we will search any instances such as :
		int i = 0;
		int k = 100;
		List<String> instances = new ArrayList<>();
		while(i != types.size()) {
			int end = Math.min(i + k, types.size());
			StringBuilder body = new StringBuilder(subClass.graphPattern +
					"?x a ?t values (?t) { ");
			for(String type : types.subList(i, end)) {
				body.append("(").append(type).append(") ");
			}
			body.append("} ");
//			logger.info("body=" + body);
			ResultSet cfs = endpoint.select("distinct ?x where { " + body + "} ", 0);
			while (cfs.hasNext()) {
				QuerySolution solution = cfs.next();
				RDFNode x = solution.get("x");
				// to remove duplicated ?x (cause of truncation)
				// if a given ?x is not on a list , we add it
				if(!instances.contains(Expression.sparqlEncode(x)))
					instances.add(Expression.sparqlEncode(x));
			}
			i += Math.min(types.size() - i, k);
//			logger.info("i=" + i);
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
