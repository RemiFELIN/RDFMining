package com.i3s.app.rdfminer.grammar.evolutionary.fitness.objectives;

import com.i3s.app.rdfminer.axiom.Axiom;

public class ObjectivesFitness {

    private Axiom phi;

    public ObjectivesFitness(Axiom phi) {
        this.phi = phi;
    }

    /**
     * Compute the fitness of a given axiom by using generality (if it's not equal
     * to 0) or the {@link Axiom#possibility() possibility} and
     * {@link Axiom#necessity() necessity} values.
     * @return the value of fitness
     */
    public double getFitness() {
        // Evaluate axioms with generality formula or (initial) formula with necessity
        if (this.phi.generality != 0) {
            this.phi.fitness = this.phi.possibility().doubleValue() * this.phi.generality;
            return this.phi.possibility().doubleValue() * this.phi.generality;
        } else {
            this.phi.fitness = this.phi.referenceCardinality
                    * ((this.phi.possibility().doubleValue() + this.phi.necessity().doubleValue()) / 2);
            return this.phi.referenceCardinality
                    * ((this.phi.possibility().doubleValue() + this.phi.necessity().doubleValue()) / 2);
        }
    }

}
