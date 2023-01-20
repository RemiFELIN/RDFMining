package com.i3s.app.rdfminer.evolutionary.fitness.novelty;

import com.i3s.app.rdfminer.entity.Entity;
import com.i3s.app.rdfminer.entity.axiom.Axiom;
import com.i3s.app.rdfminer.expression.Expression;
import com.i3s.app.rdfminer.expression.ExpressionFactory;
import com.i3s.app.rdfminer.sparql.corese.CoreseEndpoint;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;

public class Similarity {

    private static final Logger logger = Logger.getLogger(Similarity.class.getName());

    /**
     * Compute the similarity between an axiom &phi<sub>1</sub> and &phi<sub>2</sub> using the Jaccord similarity such as:<br><br>
     * <center>sim<sub>j</sub>(&phi<sub>1</sub>, &phi<sub>2</sub>) = <sup>&#8741 [<var>A</var>]&cap;[<var>B</var>] &cup; [<var>C</var>]&cap;[<var>D</var>] &#8741
     * </sup> &frasl; <sub>&#8741 [<var>A</var>] &cup; [<var>C</var>] &#8741</sub></center><br>
     * @param endpoint an instance of CoreseEndpoint
     * @param phi1 an instance of Axiom where &phi<sub>1</sub> = A &#8849; B
     * @param phi2 another instance of Axiom to compare with &phi<sub>1</sub>, where &phi<sub>2</sub> = C &#8849; D
     * @return the similarity value: <var>sim<sub>j</sub></var> (&phi<sub>1</sub>, &phi<sub>2</sub>) &isin <var>[0, 1]</var>
     */
    public static double getJaccardSimilarity(CoreseEndpoint endpoint, Entity phi1, Entity phi2) throws URISyntaxException, IOException {
        // This version support similarities for OWL Axioms
        // i.e. argumentClasses is not defined for Shapes
        // we set the similarity at 0 and so the similarity doesn't impact the whole process
        if(phi1.argumentClasses == null || phi2.argumentClasses == null) {
            logger.warn("This version support similarities for OWL Axioms !");
            return 0;
        }
        // create a graph pattern for each axioms
        Expression phi1SubClass = ExpressionFactory.createClass(phi1.argumentClasses.get(0));
        Expression phi1SuperClass = ExpressionFactory.createClass(phi1.argumentClasses.get(1));
        Expression phi2SubClass = ExpressionFactory.createClass(phi2.argumentClasses.get(0));
        Expression phi2SuperClass = ExpressionFactory.createClass(phi2.argumentClasses.get(1));
        // if the two axioms are equivalent, we set sim(phi1, phi2) = 1
        if(Objects.equals(phi1SubClass.graphPattern, phi2SubClass.graphPattern) &&
                Objects.equals(phi1SuperClass.graphPattern, phi2SuperClass.graphPattern)) {
            logger.info("The axioms are equivalent !");
            return 1;
        }
        String simNumSparql = "{ " + phi1SubClass.graphPattern + phi1SuperClass.graphPattern + " } UNION { " +
                phi2SubClass.graphPattern + phi2SuperClass.graphPattern + " } ";
        String simDenSparql = "{ " + phi1SubClass.graphPattern + " } UNION { " + phi2SubClass.graphPattern + " }";
        double similarityNumerator = endpoint.count(simNumSparql);
        double similarityDenominator = endpoint.count(simDenSparql);
//        System.out.println(simNumSparql + "\nGives: " + similarityNumerator);
//        System.out.println(simDenSparql + "\nGives: " + similarityDenominator);
        return similarityNumerator / similarityDenominator;
    }

}
