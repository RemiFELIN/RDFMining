/**
 * 
 */
package com.i3s.app.rdfminer.entity.axiom;

import com.i3s.app.rdfminer.entity.Entity;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.FitnessPackage.BasicFitness;
import com.i3s.app.rdfminer.sparql.corese.CoreseEndpoint;

import java.io.IOException;
import java.net.URISyntaxException;

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
	 * The number of instances linked between both part of a OWL 2 Axioms if it involve two parts such as :
	 * <code>subClassOf</code>; <code>DisjointClasses</code>; ...
	 */
	public int numIntersectingClasses = 0;

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
			this.individual.setFitness(new BasicFitness(this.possibility().doubleValue() * this.generality, this.individual));
		} else {
			this.individual.setFitness(new BasicFitness(this.referenceCardinality *
					((this.possibility().doubleValue() + this.necessity().doubleValue()) / 2), this.individual));
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
	 * The updating of the counts is performed by issuing three SPARQL queries of
	 * the form <code>SELECT count(DISTINCT ?x) AS</code> <var>n</var>
	 * <code>WHERE</code>. If the number of confirmations or exceptions is not too
	 * large (currently, below 100), they are downloaded from the SPARQL endpoint
	 * and stored in a list.
	 * </p>
	 */
	public void update(CoreseEndpoint endpoint) throws URISyntaxException, IOException {
	}

}
