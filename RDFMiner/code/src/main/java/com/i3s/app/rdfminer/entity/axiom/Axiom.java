/**
 * 
 */
package com.i3s.app.rdfminer.entity.axiom;

import Mapper.Symbol;
import com.i3s.app.rdfminer.entity.Entity;
import com.i3s.app.rdfminer.fuzzy.TruthDegree;
import com.i3s.app.rdfminer.grammar.evolutionary.individual.GEIndividual;
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
public abstract class Axiom extends Entity {

	/**
	 * Title of the axiom
	 */
	public String axiomId;

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
	 * The number of instances linked between both part of a OWL 2 Axioms if it involve two parts such as :
	 * <code>subClassOf</code>; <code>DisjointClasses</code>; ...
	 */
	public int numIntersectingClasses = 0;

	/**
	 * Specify if the exception query reached the timeout given by "-t" parameter
	 */
	public boolean isTimeout = false;

	/**
	 * The ARI "Acceptance/Rejection Index" of an axiom is computed as follow :
	 * ARI = {@link Axiom#possibility() possibility} + {@link Axiom#necessity() necessity} - 1
	 */
	public double ari = 0.0;

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

	/**
	 * Compute the fitness of a given axiom by using generality (if it's not equal
	 * to 0) or the {@link Axiom#possibility() possibility} and
	 * {@link Axiom#necessity() necessity} values.
	 */
	public void computeFitness() {
		// Evaluate axioms with generality formula or (initial) formula with necessity
		if (this.generality != 0) {
			this.fitness = this.possibility().doubleValue() * this.generality;
		} else {
			this.fitness = this.referenceCardinality *
					((this.possibility().doubleValue() + this.necessity().doubleValue()) / 2);
		}
	}

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
