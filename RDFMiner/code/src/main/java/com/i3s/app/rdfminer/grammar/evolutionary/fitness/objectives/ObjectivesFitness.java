package com.i3s.app.rdfminer.grammar.evolutionary.fitness.objectives;

import com.i3s.app.rdfminer.entity.axiom.Axiom;

public class ObjectivesFitness {
//
//    private Axiom phi;
//
//    public ObjectivesFitness(Axiom phi) {
//        this.phi = phi;
//    }

    /**
     * Compute the fitness of a given axiom by using generality (if it's not equal
     * to 0) or the {@link Axiom#possibility() possibility} and
     * {@link Axiom#necessity() necessity} values following this formula:<br><br>
     *
     * <center> <var>f</var>(&phi) = ((&radic&#8741 &phi &#8741) &times (&Pi(&phi) + N(&phi)) &frasl; 2) if {@link Axiom#generality generality}=0 </center>
     * <center> <var>f</var>(&phi) = <var>g_</var>&phi &times &Pi(&phi) otherwise</center>
     *
     * @return the value of fitness
     */
    public static void setFitness(Axiom phi) {
        // Evaluate axioms with generality formula or (initial) formula with necessity
        if (phi.generality != 0) {
            phi.fitness = phi.possibility().doubleValue() * phi.generality;
        } else {
//            phi.fitness = phi.referenceCardinality * ((phi.possibility().doubleValue() + phi.necessity().doubleValue()) / 2);
            // Experiments sqrt(refCard)
            phi.fitness = Math.sqrt(phi.referenceCardinality) * ((phi.possibility().doubleValue() + phi.necessity().doubleValue()) / 2);
        }
    }

}
