package com.i3s.app.rdfminer.evolutionary.fitness.novelty;

import com.i3s.app.rdfminer.entity.Entity;
import com.i3s.app.rdfminer.expression.Expression;
import com.i3s.app.rdfminer.expression.ExpressionFactory;
import com.i3s.app.rdfminer.sparql.corese.CoreseEndpoint;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;

public class Similarity {

    private static final Logger logger = Logger.getLogger(Similarity.class.getName());

    Entity phi1;
    Entity phi2;
    Expression phi1SubClass;
    Expression phi1SuperClass;
    Expression phi2SubClass;
    Expression phi2SuperClass;

    public Similarity(Entity phi1, Entity phi2) {
        // This version support similarities for OWL Axioms
        // e.g. argumentClasses are not defined for SHACL shapes
        if(phi1.argumentClasses == null || phi2.argumentClasses == null) {
            logger.warn("This version support similarities for OWL Axioms !");
        }
        this.phi1 = phi1;
        this.phi2 = phi2;
        // create a graph pattern for each axioms
        this.phi1SubClass = ExpressionFactory.createClass(phi1.argumentClasses.get(0));
        this.phi1SuperClass = ExpressionFactory.createClass(phi1.argumentClasses.get(1));
        this.phi2SubClass = ExpressionFactory.createClass(phi2.argumentClasses.get(0));
        this.phi2SuperClass = ExpressionFactory.createClass(phi2.argumentClasses.get(1));
    }

    /**
     * Compute the similarity between an axiom &phi<sub>1</sub> and &phi<sub>2</sub> using the Jaccord similarity such as:<br><br>
     * <center>sim<sub>j</sub>(&phi<sub>1</sub>, &phi<sub>2</sub>) = <sup>&#8741 [<var>A</var>]&cap;[<var>B</var>] &cup; [<var>C</var>]&cap;[<var>D</var>] &#8741
     * </sup> &frasl; <sub>&#8741 [<var>A</var>] &cup; [<var>C</var>] &#8741</sub></center><br>
     * @param endpoint an instance of CoreseEndpoint
     * @return the similarity value: <var>sim<sub>j</sub></var> (&phi<sub>1</sub>, &phi<sub>2</sub>) &isin <var>[0, 1]</var>
     */
    public double getJaccardSimilarity(CoreseEndpoint endpoint) throws URISyntaxException, IOException {
        // compute sparql queries
        String simNumSparql = "{ " + this.phi1SubClass.graphPattern + this.phi1SuperClass.graphPattern + " } UNION { " +
                phi2SubClass.graphPattern + phi2SuperClass.graphPattern + " } ";
        String simDenSparql = "{ " + this.phi1SuperClass.graphPattern + " } UNION { " + this.phi2SuperClass.graphPattern + " }";
        double similarityNumerator = endpoint.count("?x", simNumSparql);
//        logger.info("numerator query : " + simNumSparql);
//        logger.info("result = " + similarityNumerator);
        double similarityDenominator = endpoint.count("?x", simDenSparql);
//        logger.info("denominator query : " + simDenSparql);
//        logger.info("result = " + similarityDenominator);
        // avoid NaN value returned by a zero-denominator
        if(similarityDenominator == 0)  return 0;
        // else return value
        return similarityNumerator / similarityDenominator;
    }

    public double getModifiedSimilarity(CoreseEndpoint endpoint) throws URISyntaxException, IOException {
        // compute sparql queries
        String simNumSparql = "{{ " + this.phi1SubClass.graphPattern + "} UNION { " + this.phi1SuperClass.graphPattern + " }}" +
                "{{ " + this.phi2SubClass.graphPattern + "} UNION { " + this.phi2SuperClass.graphPattern + " }}";
        String simDenSparql = "{ " + this.phi1SubClass.graphPattern + " } UNION { " + this.phi1SuperClass.graphPattern + " }" +
                " UNION { " + this.phi2SubClass.graphPattern + " } UNION { " + this.phi2SuperClass.graphPattern + " }";
//        logger.info("numerator query : " + simNumSparql);
        int similarityNumerator = endpoint.count("?x", simNumSparql);
//        logger.info("result numerator = " + similarityNumerator);
        int similarityDenominator = endpoint.count("?x", simDenSparql);
//        logger.info("denominator query : " + simDenSparql);
//        logger.info("result denominator = " + similarityDenominator);
//        System.out.println("final result: " + ((float)similarityNumerator / similarityDenominator));
        // avoid NaN value returned by a zero-denominator
        if(similarityDenominator == 0)  return 0;
        // else return value
        return ((float) similarityNumerator / similarityDenominator);
    }



    /**
     * Compute the normalized similarity between an axiom &phi<sub>1</sub> and &phi<sub>2</sub> using the Jaccord similarity such as:<br><br>
     * <center><sup>sim<sub>j</sub>(&phi<sub>1</sub>, &phi<sub>2</sub>)</sup>
     * &frasl;
     * <sub><var>max(</var> sim<sub>j</sub>(&phi<sub>1</sub>, &phi<sub>1</sub> , sim<sub>j</sub>(&phi<sub>2</sub>, &phi<sub>2</sub>) <var>)</var>
     * </sub></center><br>
     * @param endpoint an instance of CoreseEndpoint
     * @return the similarity value: <var>sim<sub>j</sub></var> (&phi<sub>1</sub>, &phi<sub>2</sub>) &isin <var>[0, 1]</var>
     */
    public double getNormalizedSimilarity(CoreseEndpoint endpoint) throws URISyntaxException, IOException {
        double simJphi1phi2 = this.getJaccardSimilarity(endpoint);
//        logger.info("sim_j_phi1_phi2 = " + simJphi1phi2);
        double simJphi1phi1 = this.getJaccardSimilarity(endpoint);
//        logger.info("sim_j_phi1_phi1 = " + simJphi1phi1);
        double simJphi2phi2 = this.getJaccardSimilarity(endpoint);
//        logger.info("sim_j_phi2_phi2 = " + simJphi2phi2);
        // avoid NaN value returned by a zero-denominator
        if(simJphi1phi1 == 0 && simJphi2phi2 == 0) return 0;
        // compute normalized similarity
        return simJphi1phi2 / Math.max(simJphi1phi1, simJphi2phi2);
    }

}












