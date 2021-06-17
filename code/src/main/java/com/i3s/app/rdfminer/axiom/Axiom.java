/**
 * 
 */
package com.i3s.app.rdfminer.axiom;

import java.util.List;

import com.i3s.app.rdfminer.fuzzy.TruthDegree;

import Mapper.Symbol;

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
 * @author Andrea G. B. Tettamanzi
 *
 */
public abstract class Axiom {
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
	public int generality = 0;
	public int referenceCardinality = 0;

	/**
	 * The number of facts in the RDF store that explicitly support/confirm the
	 * axiom.
	 */
	public int numConfirmations = 0;

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

	/* The structure of GP in axiom */
	protected int num_UNION_Operators = 0;
	protected int num_FILTER_Operators = 0;
	protected int num_Triples = 0;
	protected int num_Variables = 0;
	protected int num_Instances_predicates = 0;

	/**
	 * Updates the counts used to compute the possibility and necessity degrees.
	 */
	abstract public void update();

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
	 * 
	 * @return
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
	 * Compute the cost of the GP
	 * 
	 * @return
	 */
	public double costGP() {
		if (generality > 0) {
			return Math.sqrt(
					Math.sqrt((2 * num_UNION_Operators + num_FILTER_Operators + 1) * (num_Variables + 1) * num_Triples))
					* Math.sqrt(Math.sqrt(num_Instances_predicates));
		} else {
			return 1;
		}
	}
}
