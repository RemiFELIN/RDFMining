/**
 * 
 */
package com.i3s.app.rdfminer.entity.axiom.type;

import Mapper.Symbol;
import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.RDFMiner;
import com.i3s.app.rdfminer.entity.axiom.Axiom;
import com.i3s.app.rdfminer.expression.Expression;
import com.i3s.app.rdfminer.expression.ExpressionFactory;
import com.i3s.app.rdfminer.expression.complement.ComplementClassExpression;
import com.i3s.app.rdfminer.sparql.corese.CoreseEndpoint;
import com.i3s.app.rdfminer.sparql.corese.Format;
import com.i3s.app.rdfminer.sparql.corese.ResultParser;
import com.i3s.app.rdfminer.sparql.virtuoso.VirtuosoEndpoint;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * A class that represents a <code>SubClassOf</code> axiom.
 * 
 * @author Andrea G. B. Tettamanzi & Rémi FELIN
 *
 */
public class SubClassOfAxiom extends Axiom {

	private static final Logger logger = Logger.getLogger(SubClassOfAxiom.class.getName());

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
			CoreseEndpoint endpoint) {
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
		} catch (IllegalStateException | URISyntaxException | IOException e) {
			// This is the conventional unchecked exception thrown by the
			// Sparql endpoint if an HTTP 504 Gateway Time-out occurs.
			// In that case, we try a slower, but safer, naive update as the last resort:
//			logger.warn("Trying a naive update: this is going to take some time...");
//			naiveUpdate(endpoint);
			logger.warn("Error during the update phase : " + e.getMessage());
			logger.warn("The naive update is not avalaible ...");
		}
		// set elapsedTime as a CPU usage time
		logger.info("ARI = " + ari);
		elapsedTime = getProcessCPUTime() - t0;
		// set fitness
		computeFitness();
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
	 * This method, which is provided as a fallback in case {@link #update(VirtuosoEndpoint)} does
	 * not work, uses a much slower, but hopefully safer, way of updating the counts
	 * than the {@link #update(VirtuosoEndpoint)} method. The extension of the sub-class is
	 * retrieved, than each individual instance is checked for membership in the
	 * super-class and in the complement of the super-class by issuing two ASK
	 * queries.
	 * </p>
	 * 
	 */
	// TODO: 7/11/22 to update
//	public void naiveUpdate(CoreseEndpoint endpoint) throws URISyntaxException, IOException {
//		referenceCardinality = numConfirmations = numExceptions = 0;
//		confirmations = new ArrayList<>();
//		exceptions = new ArrayList<>();
//		Set<String> extension = subClass.extension(endpoint);
//		String nicAsJson = endpoint.select(Format.JSON, endpoint.buildFederatedQuery("SELECT (count(distinct ?D) as ?n) WHERE { " + subClass.graphPattern + " ?x a ?D . }"));
//		int numIntersectingClasses = Integer.parseInt(ResultParser.getResultsfromVariable("n", nicAsJson).get(0));
//		timePredictor = (long) referenceCardinality * numIntersectingClasses;
//
//		for (String rdfNodePair : extension) {
//			referenceCardinality++;
//			if (superClass.contains(rdfNodePair, endpoint)) {
//				numConfirmations++;
//				confirmations.add(Expression.sparqlEncode(rdfNodePair.x));
//			}
//			// The following is correct, but not optimized (a lot of duplicated tests)
//			else if (superClassComplement.contains(rdfNodePair, endpoint)) {
//				numExceptions++;
//				exceptions.add(Expression.sparqlEncode(rdfNodePair.x));
//			}
//			// A better idea would be to issue a SPARQL query
//			// and let the SPARQL endpoint do the work: see the method below...
//		}
//		logger.info("Reference cardinality: " + referenceCardinality);
//		logger.info("Number of confirmation(s): " + numConfirmations);
//		logger.info("Number of exception(s): " + numExceptions);
//
//	}

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
	 */
	@Override
	public void update(CoreseEndpoint endpoint) throws URISyntaxException, IOException {
		// First of all, we verify if a such assumption does not already exists
		// Only simple OWL 2 subClassOf axioms are considered in this case
		// This checking part is an temporary solution
		// TODO: in the future, we will consider all existing axioms as knowledge to improve OWL 2 Axioms mining (in GE, ...)
		if(!complex && endpoint.askFederatedQuery(subClass + " rdfs:subClassOf " + superClass)) {
			// in this case, we set pos = nec = 1.0 as consequence to its existance in ontology
			logger.info("This axiom is defined in the ontology ...");
			referenceCardinality = numConfirmations = endpoint.count(subClass.graphPattern);
			numExceptions = 0;
			ari = ARI();
			return;
		}
		// If it does not exists, we need to evaluate it
		confirmations = new ArrayList<>();
		exceptions = new ArrayList<>();
		long timeSpent;
		// The reference cardinality will count all the instances involved by the current axiom
		referenceCardinality = endpoint.count(subClass.graphPattern);
		logger.info("Reference cardinality = " + referenceCardinality);
		// The number of instances linked with the subClass of the given axiom
		numIntersectingClasses = endpoint.count(subClass.graphPattern + " ?x a ?D . ");
		logger.info("No. of Intersecting Classes = " + numIntersectingClasses);
//		timePredictor = (long) referenceCardinality * numIntersectingClasses;
		numConfirmations = endpoint.count(subClass.graphPattern + "\n" + superClass.graphPattern);
		if (numConfirmations > 0) {
			logger.info(numConfirmations + " confirmation(s) found ...");
			if(numConfirmations < 100) {
				logger.info("retrieving in collection ...");
				// query the confirmations and add it in the confirmations list
				confirmations.addAll(
						endpoint.selectFederatedQuery("x", "SELECT DISTINCT ?x WHERE { " + subClass.graphPattern + "\n" + superClass.graphPattern + " }")
				);
			}
		}
		// Now, let's compute the exceptions for this axiom 
		if (numConfirmations == referenceCardinality) {
			// No need to count the exceptions: there can't be any!
			numExceptions = 0;
			// set the ARI of axiom
			ari = ARI();
			return;
			
		} else if (RDFMiner.parameters.timeOut > 0 || RDFMiner.parameters.dynTimeOut != 0.0) {
			logger.info("compute the number of exceptions with a timeout ...");
			// Since the query to count exception is complex and may take very long to
			// execute, we execute it with the user-supplied time out.
			// we need to instanciate a new endpoint which will consider the desired timeout
			CoreseEndpoint endpointWithTimeout = new CoreseEndpoint(endpoint.url, endpoint.service, endpoint.prefixes, RDFMiner.parameters.timeOut);
			numExceptions = endpointWithTimeout.count(subClass.graphPattern + "\n" + superClassComplement.graphPattern);
			if ( numExceptions == -1 ) {
				logger.warn("we take the reference cardinality minus the number of confirmations as the conventional number of exceptions");
				// If the query times out, it is very likely that it would end up
				// having a large number of exceptions. Therefore, we take the reference
				// cardinality minus the number of confirmations as the conventional
				// number of exceptions in this case.
				numExceptions = referenceCardinality - numConfirmations;
				// Specify isTimeout for this axiom
				isTimeout = true;
			}
			
		} else {
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
				getExceptions(endpoint);
			}
		}
		// We don't need to compute exceptions if we get a timeout from exceptions SPARQL request
		if (numExceptions > 0 && numExceptions < 100 && !isTimeout && RDFMiner.parameters.timeOut > 0) {
			logger.info(numExceptions + " exception(s) found ! retrieving in collection ...");
			// retrieve the exceptions
			exceptions.addAll(
					endpoint.selectFederatedQuery("x", "SELECT distinct ?x WHERE { " + subClass.graphPattern + "\n" + superClassComplement.graphPattern + " }")
			);
		}
		ari = ARI();
	}

	public void getExceptionsUsingCoreseLoop() throws URISyntaxException, IOException {
		logger.info("Compute the number of exceptions with a proposal optimization and loop operator from Corese ...");
		CoreseEndpoint endpoint = new CoreseEndpoint(Global.CORESE_SPARQL_ENDPOINT, Global.TARGET_SPARQL_ENDPOINT, null);
		// Writing the query using loop operator, we will ask our Virtuoso server from the Corese server as a SERVICE
		String getTypesQuery = endpoint.buildSelectAllQuery(
				endpoint.addFederatedQueryWithLoop("SELECT distinct ?t WHERE { " + subClass.graphPattern + " ?x a ?t }", 1000) + "\n" +
						endpoint.addFederatedQuery("values ?t {undef}\nFILTER NOT EXISTS { " + superClass.graphPattern + " ?x a ?t }")
		);
		String typesAsJson = endpoint.query(Format.JSON, getTypesQuery);
		List<String> types = ResultParser.getResultsFromVariable("t", typesAsJson);
		if(types.size() != 0)
			logger.info(types.size() + " type(s) where we don't observe a link with the superClass ...");
		// truncate list of types and execute the last request
		List<String> excpt = new ArrayList<>();
		// define variables used for truncation
		int i = 0;
		int k = 50;
		// define the limit
		int limit = 10000;
		while(i != types.size()) {
			int end = Math.min(i + k, types.size());
			StringBuilder body = new StringBuilder(subClass.graphPattern +
					"?x a ?t values (?t) { ");
			for(String type : types.subList(i, end)) {
				body.append("(").append(type).append(") ");
			}
			body.append("} ");
			// build federated query
			String getInstancesQuery = endpoint.buildSelectAllQuery(
					endpoint.addFederatedQueryWithLoop("SELECT distinct ?x WHERE { " + body + " }", limit)
			);
			String instancesAsJson = endpoint.query(Format.JSON, getInstancesQuery);
			List<String> instances = ResultParser.getResultsFromVariable("x", instancesAsJson);
			for(String instance : instances) {
				// to remove duplicated ?x (cause of truncation)
				// if a given ?x is not on a list , we add it
				if(!excpt.contains(instance))
					excpt.add(instance);
			}
			i += Math.min(types.size() - i, k);
		}
		logger.info(excpt.size() + " exception(s) found ...");
		numExceptions = excpt.size();
		// retrieve the exceptions
		if (numExceptions > 0 && numExceptions < 100) exceptions = excpt;
	}

	public void getExceptions(CoreseEndpoint endpoint) throws URISyntaxException, IOException {
		logger.info("Compute the number of exceptions with a proposal optimization ...");
		int offset = 0;
		List<String> types = new ArrayList<>();
		// get all types related to the subClassExpression for which it does not exists any ?z of this type and superClassExpression
		while (offset != numIntersectingClasses) {
			String getTypesQuery = endpoint.buildSelectAllQuery(
					endpoint.addFederatedQuery("SELECT * WHERE { " +
					"{ " +
						"SELECT * WHERE { " +
							"{ " +
								"SELECT distinct ?t WHERE { " +
									subClass.graphPattern + " ?x a ?t " +
								"} ORDER BY ?t " +
							"} " +
						"} LIMIT 1000 OFFSET " + offset + " " +
					"} " +
					"FILTER NOT EXISTS { " +
						superClass.graphPattern + " ?x a ?t" +
					"} } ")
			);
			String resultsAsJson = endpoint.query(Format.JSON, getTypesQuery);
			List<String> results = ResultParser.getResultsFromVariable("t", resultsAsJson);
			if (results != null)
				types.addAll(results);
			offset += Math.min(numIntersectingClasses - offset, 1000);
		}
		if (types.size() != 0)
			logger.info(types.size() + " type(s) where we don't observe a link with the superClass ...");
		// truncate query
		// for each types in the list, we will search any instances such as :
		int i = 0;
		int k = 50;
		// set the LIMIT ... OFFSET ... values
		int limit = 10000;
		List<String> instances = new ArrayList<>();
		while (i != types.size()) {
			offset = 0;
			int end = Math.min(i + k, types.size());
			StringBuilder body = new StringBuilder(subClass.graphPattern +
					"?x a ?t values (?t) { ");
			for(String type : types.subList(i, end)) {
				body.append("(").append(type).append(") ");
			}
			body.append("} ");
			while (true) {
				String getInstancesQuery = endpoint.buildSelectAllQuery(
						endpoint.addFederatedQuery("SELECT distinct ?x where { " + body + "} LIMIT " + limit + " OFFSET " + offset)
				);
				String resultsAsJson = endpoint.query(Format.JSON, getInstancesQuery);
				List<String> results = ResultParser.getResultsFromVariable("x", resultsAsJson);
				// to remove duplicated ?x (cause of truncation)
				// if a given ?x is not on a list , we add it
				assert results != null;
				for (String result : results) {
					if (!instances.contains(result)) {
						instances.add(result);
					}
				}
				if (results.size() < limit) {
					break;
				} else {
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

}
