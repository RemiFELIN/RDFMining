/**
 * 
 */
package com.i3s.app.rdfminer.entity.axiom.type;

import com.i3s.app.rdfminer.Parameters;
import com.i3s.app.rdfminer.entity.axiom.Axiom;
import com.i3s.app.rdfminer.evolutionary.geva.Mapper.Symbol;
import com.i3s.app.rdfminer.expression.Expression;
import com.i3s.app.rdfminer.expression.ExpressionFactory;
import com.i3s.app.rdfminer.expression.complement.ComplementClassExpression;
import com.i3s.app.rdfminer.sparql.corese.CoreseEndpoint;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
		super();
		// set a t0 using the CPU time
		long t0 = getProcessCPUTime();
		subClass = ExpressionFactory.createClass(subClassExpression);
		superClass = ExpressionFactory.createClass(superClassExpression);
//		logger.info(subClass + " ~ " + superClass);
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
		elapsedTime = getProcessCPUTime() - t0;
		logger.info("ARI = " + ari + "; computed in " + elapsedTime + " ms.");
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
	 */
	@Override
	public void update(CoreseEndpoint endpoint) throws URISyntaxException, IOException {
		Parameters parameters = Parameters.getInstance();
		logger.debug("Candidate axiom: SubClassOf(" + subClass + " " + superClass + ")");
		// First of all, we verify if a such assumption does not already exists
		// Only simple OWL 2 subClassOf axioms are considered in this case
		// This checking part is an temporary solution
		// TODO: in the future, we will consider all existing axioms as knowledge to improve OWL 2 Axioms mining (in GE, ...)
		if(!complex && endpoint.ask(subClass + " rdfs:subClassOf " + superClass)) {
			// in this case, we set pos = nec = 1.0 as consequence to its existance in ontology
			logger.debug("This candidate is already a valid OWL SubClassOf axiom !");
			referenceCardinality = numConfirmations = endpoint.count("?x", subClass.graphPattern);
			numExceptions = 0;
			ari = ARI();
			return;
		}
		// eliminate duplicate sub and super classes
		if (Objects.equals(subClass.graphPattern, superClass.graphPattern)) {
			logger.debug("This candidate contains the same ressource for its subClass and superClass !");
			ari = ARI();
			// its fitness will be 0
			return;
		}
		// If it does not exists, we need to evaluate it
		confirmations = new ArrayList<>();
		exceptions = new ArrayList<>();
		long timeSpent;
		// The reference cardinality will count all the instances involved by the current axiom
		referenceCardinality = endpoint.count("?x", subClass.graphPattern);
		if(referenceCardinality == -1) {
			logger.warn("Timeout reached during the computation of the number of reference cardinality !");
			referenceCardinality = 0;
			isTimeout = true;
			return;
		}
//		logger.info("Reference cardinality = " + referenceCardinality);
		// The number of instances linked with the subClass of the given axiom
		numIntersectingClasses = endpoint.count("?x", subClass.graphPattern + " ?x a ?D . ");
		if(numIntersectingClasses == -1) {
			logger.warn("Timeout reached during the computation of the number of intersecting classes !");
			isTimeout = true;
			numIntersectingClasses = 0;
		}
		numConfirmations = endpoint.count("?x", subClass.graphPattern + "\n" + superClass.graphPattern);
		if (numConfirmations == -1) {
			logger.warn("Timeout reached during the computation of the number of confirmations !");
			numConfirmations = 0;
		} else if (numConfirmations > 0) {
//			logger.info(numConfirmations + " confirmation(s) found ...");
			if(numConfirmations < 100) {
//				logger.info("retrieving in collection ...");
				// query the confirmations and add it in the confirmations list
				List<String> conf = endpoint.select("?x", subClass.graphPattern + "\n" + superClass.graphPattern, true);
				if(conf != null) {
					confirmations.addAll(conf);
				} else {
					logger.warn("Timeout reached during the computation of confirmations !");
					isTimeout = true;
				}
			}
		}
		// Now, let's compute the exceptions for this axiom 
		if (numConfirmations == referenceCardinality) {
			// No need to count the exceptions: there can't be any!
			numExceptions = 0;
			// set the ARI of axiom
			ari = ARI();
			return;
			
		} else if (parameters.getSparqlTimeOut() > 0) {
//			logger.info("compute the number of exceptions with a timeout ...");
			// Since the query to count exception is complex and may take very long to
			// execute, we execute it with the user-supplied time out.
			// we need to instanciate a new endpoint which will consider the desired timeout
//			endpoint.setTimeout(RDFMiner.parameters.timeOut);
			boolean finish = getExceptions(endpoint);
			if ( !finish ) {
				logger.warn("Timeout reached during the computation of exceptions !");
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
			// This is the WI 2022 version: optimization using truncation
			this.getExceptions(endpoint);
		}
		ari = ARI();
	}

	public boolean getExceptions(CoreseEndpoint endpoint) throws URISyntaxException, IOException {
//		logger.info("Compute the number of exceptions with a proposal optimization ...");
		int offset = 0;
		List<String> types = new ArrayList<>();
		// get all types related to the subClassExpression for which it does not exists any ?z of this type and superClassExpression
		String type = "?t";
		while (offset != numIntersectingClasses) {
			String body = " { ";
			body += 	      " SELECT " + type + " WHERE { ";
			body += 		  	 " SELECT distinct " + type + " WHERE { ";
			body += 		  	 	subClass.graphPattern + " ?x a " + type;
			body += 		  	 " } ORDER BY " + type;
			// @todo set limit value with a parameters ?
			body += 	      " } LIMIT 1000 OFFSET " + offset;
			body += 	 " } ";
			// filter not exists part
			body +=		 " FILTER NOT EXISTS { " + superClass.graphPattern + " ?x a " + type + " } ";
//			System.out.println(getTypesQuery);
			List<String> results = endpoint.select(type, body, true);
			if (results != null)
				types.addAll(results);
			else {
				return false;
			}
			offset += Math.min(numIntersectingClasses - offset, 1000);
		}
		// truncate query
		// for each types in the list, we will search any instances such as :
		int i = 0;
		int k = 50;
		// set the LIMIT ... OFFSET ... values
		int limit = 10000;
		List<String> instances = new ArrayList<>();
		String exception = "?x";
		while (i != types.size()) {
			offset = 0;
			int end = Math.min(i + k, types.size());
			StringBuilder body = new StringBuilder(subClass.graphPattern + exception + " a " + type + " values (" + type + ") { ");
			for(String t : types.subList(i, end)) {
				body.append("(").append(t).append(") ");
			}
			body.append("} ");
			while (true) {
				List<String> results = endpoint.select(exception, body.toString(), true, limit, offset);
				// to remove duplicated ?x (cause of truncation)
				// if a given ?x is not on a list , we add it
				if (results == null)
					return false;
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
//		logger.info(instances.size() + " exception(s) found ...");
		numExceptions = instances.size();
		if (numExceptions > 0 && numExceptions < 100) exceptions = instances;
		return true;
	}

}
