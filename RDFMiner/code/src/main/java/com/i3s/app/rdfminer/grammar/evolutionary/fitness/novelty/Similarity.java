package com.i3s.app.rdfminer.grammar.evolutionary.fitness.novelty;

import com.i3s.app.rdfminer.axiom.Axiom;
import com.i3s.app.rdfminer.sparql.corese.CoreseEndpoint;

import java.io.IOException;
import java.net.URISyntaxException;

public class Similarity {

    /**
     * Compute the similarity between an axiom &phi<sub>1</sub> and &phi<sub>2</sub> using the Jaccord similarity such as:<br><br>
     * <center>sim<sub>j</sub>(&phi<sub>1</sub>, &phi<sub>2</sub>) = <sup>&#8741 [<var>A</var>]&cap;[<var>B</var>] &cup; [<var>C</var>]&cap;[<var>D</var>] &#8741
     * </sup> &frasl; <sub>&#8741 [<var>A</var>] &cup; [<var>C</var>] &#8741</sub></center><br>
     * @param endpoint an instance of CoreseEndpoint
     * @param phi1 an instance of Axiom where &phi<sub>1</sub> = A &#8849; B
     * @param phi2 another instance of Axiom to compare with &phi<sub>1</sub>, where &phi<sub>2</sub> = C &#8849; D
     * @return the similarity value: <var>sim<sub>j</sub></var> (&phi<sub>1</sub>, &phi<sub>2</sub>) &isin <var>[0, 1]</var>
     */
    public static double getJaccardSimilarity(CoreseEndpoint endpoint, Axiom phi1, Axiom phi2) throws URISyntaxException, IOException {
        double similarityNumerator = endpoint.count(
                "{ ?x a " + phi1.argumentClasses.get(0).get(0) + " . ?x a " + phi1.argumentClasses.get(1).get(0) + "} " +
                "UNION { ?x a " + phi2.argumentClasses.get(0).get(0) + " . ?x a " + phi2.argumentClasses.get(1).get(0) + "} ");
        double similarityDenominator = endpoint.count(
                "{ ?x a " + phi1.argumentClasses.get(0).get(0) + " . } UNION { ?x a " + phi2.argumentClasses.get(0).get(0) + " . }");
        return similarityNumerator / similarityDenominator;
    }

}
