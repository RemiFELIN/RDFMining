/**
 * 
 */
package com.i3s.app.rdfminer.axiom;

import Mapper.Symbol;
import com.i3s.app.rdfminer.axiom.type.DisjointClassesAxiom;
import com.i3s.app.rdfminer.fuzzy.TruthDegree;
import com.i3s.app.rdfminer.grammar.evolutionary.fitness.AxiomFitnessEvaluation;
import com.i3s.app.rdfminer.grammar.evolutionary.individual.GEIndividual;
import com.i3s.app.rdfminer.output.Results;
import com.i3s.app.rdfminer.sparql.corese.CoreseEndpoint;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

/**
 * An abstract class at the top of the hierarchy of OWL 2 axioms.
 * <p>
 * Each concrete subclass of this class must support a way to test to what
 * degree the axiom is possible (in the sense of Possibility Theory) by querying
 * an RDF fact repository, such as DBpedia, through a SPARQL endpoint. The
 * details of how the SPARQL query is constructed and how its solutions are
 * processed varies according to the type of the axiom. This is the main reason
 * for having different subclasses for different types of axioms.
 * </p>
 * 
 * @author Andrea G. B. Tettamanzi & NGUYEN Thu Huong & Rémi FELIN
 *
 */
public abstract class Axiom extends Results {

	/**
	 * Title of the axiom
	 */
	public String axiomId;

	/**
	 * The individual build with GEVA, corresponding to the current axiom
	 */
	public GEIndividual individual;

	/**
	 * A service native method to query for CPU usage.
	 * <p>The name and implementation of this method are adapted from
	 * <a href="http://www.javaworld.com/article/2077361/learn-java/profiling-cpu-usage-from-within-a-java-application.html">this
	 * 2002 blog post</a>.</p>
	 * <p>The implementation in C language of this native method is contained in the two source files
	 * <code>rdfminer_axiom_Axiom.h</code> and <code>rdfminer_axiom_Axiom.c</code>.</p>
	 *
	 * @return the number of milliseconds of CPU time used by the current process so far
	 */
	public static native long getProcessCPUTime();

	/**
	 * The cardinality of the universe of discourse for this axiom.
	 * <p>
	 * The <em><a href="http://en.wikipedia.org/wiki/Domain_of_discourse">domain of
	 * discourse</a></em>, also called the <em>universe of discourse</em> (or simply
	 * <em>universe</em>), is the set of entities over which certain variables of
	 * interest in some formal treatment may range.
	 * </p>
	 * <p>
	 * Here, the universe of discourse is the set of all objects or properties which
	 * are relevant for testing whether this axiom is possible or necessary or not.
	 * <p>
	 */
	public List<List<Symbol>> argumentClasses;

	/**
	 * The generality of the axiom, its value is defined when we cannot compute the
	 * {@link Axiom#necessity() necessity} of the current axiom (for instance, in a
	 * case of a {@link DisjointClassesAxiom})
	 */
	public double generality = 0;

	/**
	 * The fitness of the axiom correspond to its value evaluated by a function, see
	 * the {@link AxiomFitnessEvaluation#setFitness(Axiom) evaluation} function used
	 */
	public double fitness = 0.0;

	/**
	 * The reference cardinality will count all the instances involved by the
	 * current axiom
	 */
	public int referenceCardinality = 0;

	/**
	 * The number of instances linked between both part of a OWL 2 Axioms if it involve two parts such as :
	 * <code>subClassOf</code>; <code>DisjointClasses</code>; ...
	 */
	public int numIntersectingClasses = 0;

	/**
	 * The current ID of generation where this axiom has been found
	 */
	public int generation;

	/**
	 * The number of facts in the RDF store that explicitly support/confirm the
	 * axiom.
	 */
	public int numConfirmations = 0;

	/**
	 * the time it took to test the axiom, in ms.
	 */
	public long elapsedTime = 0L;

	/**
	 * The number of facts in the RDF store that explicitly contradict the axiom.
	 */
	public int numExceptions = 0;

	/**
	 * A list of facts in the RDF store that explicitly corroborate the axiom.
	 * <p>
	 * For the time being, facts are represented as strings. This is the most
	 * general and flexible representation if we are just interested in reporting
	 * them to a human user.However, the possibility cannot be ruled out that, in
	 * future, a different representation will have to be adopted, e.g., a record of
	 * a SPARQL query result.
	 * </p>
	 */
	public List<String> confirmations = null;

	/**
	 * A list of facts in the RDF store that explicitly contradict the axiom.
	 * <p>
	 * For the time being, facts are represented as strings. This is the most
	 * general and flexible representation if we are just interested in reporting
	 * them to a human user.However, the possibility cannot be ruled out that, in
	 * future, a different representation will have to be adopted, e.g., a record of
	 * a SPARQL query result.
	 * </p>
	 */
	public List<String> exceptions = null;

	/**
	 * Specify if the exception query reached the timeout given by "-t" parameter
	 */
	public boolean isTimeout = false;

	/**
	 * The ARI "Acceptance/Rejection Index" of an axiom is computed as follow :
	 * ARI = {@link Axiom#possibility() possibility} + {@link Axiom#necessity() necessity} - 1
	 */
	public double ari = 0.0;

//	/* The structure of GP in axiom */
//	protected int numUnionOperators = 0;
//	protected int numFilterOperators = 0;
//	protected int numTriples = 0;
//	protected int numVariables = 0;
//	protected int numInstancesPredicates = 0;

	//	abstract public void update();

	/**
	 * Computes the possibility degree of the axiom, based on the known facts.
	 * 
	 * @return the possibility degree of the axiom.
	 */
	public TruthDegree possibility() {
		double y = 1.0;

		if (referenceCardinality > 0) {
			double x = ((double) referenceCardinality - (double) numExceptions) / ((double) referenceCardinality);
			y = 1.0 - Math.sqrt(1.0 - x * x);
		}
		return new TruthDegree(y);
	}

	/**
	 * Computes the necessity degree of the axiom, based on the known facts.
	 */
	public TruthDegree necessity() {
		double y = 0.0;

		if (referenceCardinality > 0 && numExceptions == 0) {
			double x = ((double) referenceCardinality - (double) numConfirmations) / ((double) referenceCardinality);
			y = Math.sqrt(1.0 - x * x);
		}
		return new TruthDegree(y);
	}

	/**
	 * Define an ARI of the current axiom
	 */
	public double ARI() {
		this.ari = this.possibility().doubleValue() + this.necessity().doubleValue() - 1;
		return ari;
	}

//	/**
//	 * Compute the cost of the GP
//	 */
//	public double costGP() {
//		if (generality > 0) {
//			return Math
//					.sqrt(Math.sqrt((2 * numUnionOperators + numFilterOperators + 1) * (numVariables + 1) * numTriples))
//					* Math.sqrt(Math.sqrt(numInstancesPredicates));
//		} else {
//			return 1;
//		}
//	}

	/**
	 * Get the individual involved by the current axiom
	 * 
	 * @return individual the current individual
	 */
	public GEIndividual getIndividual() {
		return individual;
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
	public void update(CoreseEndpoint endpoint) throws URISyntaxException, IOException {
	}

	@Override
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		json.put("axiom", axiomId);
		json.put("referenceCardinality", referenceCardinality);
		json.put("numConfirmations", numConfirmations);
		json.put("numExceptions", numExceptions);
		json.put("possibility", possibility().doubleValue());
		json.put("necessity", necessity().doubleValue());
		json.put("elapsedTime", elapsedTime);
		json.put("isTimeOut", isTimeout);
		json.put("exceptions", new JSONArray(exceptions));
		json.put("confirmations", new JSONArray(confirmations));
		json.put("generation", generation);
		json.put("fitness", fitness);
		json.put("generality", generality);
		json.put("ari", ari);
		if(individual != null)
			json.put("isMapped", individual.isMapped());
		else
			json.put("isMapped", JSONObject.NULL);
		return json;
	}

}
