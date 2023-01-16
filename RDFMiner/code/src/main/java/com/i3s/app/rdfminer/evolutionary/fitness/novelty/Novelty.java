package com.i3s.app.rdfminer.evolutionary.fitness.novelty;

import com.i3s.app.rdfminer.entity.axiom.Axiom;

public class Novelty {

//    private static final Logger logger = Logger.getLogger(NoveltyFitness.class.getName());

    /**
     * compute the fitness value <var>f</var>(&phi) in the Novelty Search context using this formula:<br><br>
     * <center> ((&radic&#8741 &phi &#8741) &times (&Pi(&phi) + N(&phi)) &frasl; 2) &times
     *                    (1 &frasl (1 + &sum sim<sub>j</sub>(&phi)) </center>
     * @return the value of based novelty fitness <var>f</var>(&phi)
     */
    public static void updateFitness(Axiom phi) {
        phi.fitness *= 1 / (1 + phi.similarities.stream().mapToDouble(x -> x).sum());
    }

}
