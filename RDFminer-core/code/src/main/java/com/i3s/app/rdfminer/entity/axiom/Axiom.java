/**
 * 
 */
package com.i3s.app.rdfminer.entity.axiom;

import com.i3s.app.rdfminer.entity.Entity;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.FitnessPackage.BasicFitness;

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

}
