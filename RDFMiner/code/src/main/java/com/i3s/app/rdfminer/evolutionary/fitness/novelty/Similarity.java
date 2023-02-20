package com.i3s.app.rdfminer.evolutionary.fitness.novelty;

import com.i3s.app.rdfminer.entity.Entity;
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
        // compute sparql queries
        String simNumSparql = "{ " + phi1SubClass.graphPattern + phi1SuperClass.graphPattern + " } UNION { " +
                phi2SubClass.graphPattern + phi2SuperClass.graphPattern + " } ";
        String simDenSparql = "{ " + phi1SuperClass.graphPattern + " } UNION { " + phi2SuperClass.graphPattern + " }";
        double similarityNumerator = endpoint.count(simNumSparql);
//        logger.info("numerator query : " + simNumSparql);
//        logger.info("result = " + similarityNumerator);
        double similarityDenominator = endpoint.count(simDenSparql);
//        logger.info("denominator query : " + simDenSparql);
//        logger.info("result = " + similarityDenominator);
        // avoid NaN value returned by a zero-denominator
        if(similarityDenominator == 0)  return 0;
        // else return value
        return similarityNumerator / similarityDenominator;
    }

    /**
     * Compute the normalized similarity between an axiom &phi<sub>1</sub> and &phi<sub>2</sub> using the Jaccord similarity such as:<br><br>
     * <center><sup>sim<sub>j</sub>(&phi<sub>1</sub>, &phi<sub>2</sub>)</sup>
     * &frasl;
     * <sub><var>max(</var> sim<sub>j</sub>(&phi<sub>1</sub>, &phi<sub>1</sub> , sim<sub>j</sub>(&phi<sub>2</sub>, &phi<sub>2</sub>) <var>)</var>
     * </sub></center><br>
     * @param endpoint an instance of CoreseEndpoint
     * @param phi1 an instance of Axiom where &phi<sub>1</sub> = A &#8849; B
     * @param phi2 another instance of Axiom to compare with &phi<sub>1</sub>, where &phi<sub>2</sub> = C &#8849; D
     * @return the similarity value: <var>sim<sub>j</sub></var> (&phi<sub>1</sub>, &phi<sub>2</sub>) &isin <var>[0, 1]</var>
     */
    public static double getNormalizedSimilarity(CoreseEndpoint endpoint, Entity phi1, Entity phi2) throws URISyntaxException, IOException {
        double simJphi1phi2 = getJaccardSimilarity(endpoint, phi1, phi2);
//        logger.info("sim_j_phi1_phi2 = " + simJphi1phi2);
        double simJphi1phi1 = getJaccardSimilarity(endpoint, phi1, phi1);
//        logger.info("sim_j_phi1_phi1 = " + simJphi1phi1);
        double simJphi2phi2 = getJaccardSimilarity(endpoint, phi2, phi2);
//        logger.info("sim_j_phi2_phi2 = " + simJphi2phi2);
        // avoid NaN value returned by a zero-denominator
        if(simJphi1phi1 == 0 && simJphi2phi2 == 0) return 0;
        // compute normalized similarity
        return simJphi1phi2 / Math.max(simJphi1phi1, simJphi2phi2);
    }

}












