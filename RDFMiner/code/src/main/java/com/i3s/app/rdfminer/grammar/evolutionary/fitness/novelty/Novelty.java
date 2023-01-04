package com.i3s.app.rdfminer.grammar.evolutionary.fitness.novelty;

import com.i3s.app.rdfminer.entity.axiom.Axiom;

public class Novelty {

    private Axiom phi;

    public Novelty(Axiom phi) {
        this.phi = phi;
    }

    /**
     * compute the fitness value in the Novelty Search context using this formula:<br><br>
     * <center> <var>f(&phi)</var> = ((&radic&#8741 &phi &#8741) &times (&Pi(&phi) + N(&phi)) &frasl; 2) &times
     *                    &sum sim<sub>j</sub>(&phi) </center>
     * @return the value of based novelty fitness <var>f(&phi)</var>
     */
    public double getFitness() {
        double objectiveFitness = Math.sqrt(phi.referenceCardinality) * (phi.possibility().doubleValue() + phi.necessity().doubleValue()) / 2;
        return objectiveFitness * phi.similarities.stream().mapToDouble(x -> x).sum();
    }

}
