package com.i3s.app.rdfminer.ht;

import com.i3s.app.rdfminer.Parameters;
import com.i3s.app.rdfminer.entity.shacl.Shape;
import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;

public class HypothesisTesting {

    public Shape shape;
    private Double X2;
    public boolean isAccepted;

    public HypothesisTesting() {}

    public HypothesisTesting(Shape shape) {
        Parameters parameters = Parameters.getInstance();
        this.shape = shape;
        this.X2 = null;
        // X^2 computation
        double nExcTheo = shape.referenceCardinality * parameters.getProbShaclP();
        double nConfTheo = shape.referenceCardinality - nExcTheo;
        if(shape.numExceptions <= nExcTheo) {
            // if observed error is lower, accept the shape
            this.isAccepted = true;
        } else if (nExcTheo >= 5 && nConfTheo >= 5) {
            // apply statistic test X2
            this.X2 = (Math.pow(shape.numExceptions - nExcTheo, 2) / nExcTheo) +
                    (Math.pow(shape.numConfirmations - nConfTheo, 2) / nConfTheo);
            double critical = new ChiSquaredDistribution(1).inverseCumulativeProbability(1 - parameters.getProbShaclAlpha());
            // test value and accept it if it's lower than critical value
            this.isAccepted = X2 <= critical;
        } else {
            // rejected !
            this.isAccepted = false;
        }
    }

    public void eval(Shape shape) {
        Parameters parameters = Parameters.getInstance();
//        logger.info("\n~~~" +
//                "\nshape.referenceCardinality= " + shape.referenceCardinality +
//                "\nshape.numExceptions= " + shape.numExceptions +
//                "\nshape.numConfirmations= " + shape.numConfirmations +
//                "\n~~~");
        double nExcTheo = shape.referenceCardinality * parameters.getProbShaclP();
        double nConfTheo = shape.referenceCardinality - nExcTheo;
        if(shape.numExceptions <= nExcTheo) {
            // if observed error is lower, accept the shape
            shape.accepted = true;
        } else if (nExcTheo >= 5 && nConfTheo >= 5) {
            // apply statistic test X2
            shape.pValue = (Math.pow(shape.numExceptions - nExcTheo, 2) / nExcTheo) +
                    (Math.pow(shape.numConfirmations - nConfTheo, 2) / nConfTheo);
            // logger.info("Hypothesis testing of " + shape.absoluteIri + ": pVal=" + shape.pValue);
            double critical = new ChiSquaredDistribution(1).inverseCumulativeProbability(1 - parameters.getProbShaclAlpha());
            // test value and accept it if it's lower than critical value
            shape.accepted = shape.pValue <= critical;
        } else {
            // rejected !
            shape.accepted = false;
        }
//        logger.info("Acceptance of " + shape.absoluteIri + ": " + shape.accepted);
    }

    public String getAcceptanceTriple() {
        return this.shape.relativeIri + " ex:acceptance \""+ this.isAccepted + "\"^^xsd:boolean .\n";
    }

    public String getX2ValueTriple() {
        return this.shape.relativeIri + " ex:pvalue \"" + this.X2 + "\"^^xsd:double .\n";
    }

    public Double getX2() {
        return X2;
    }

    public double getMaxMassFunction(Shape shape) {
        Parameters parameters = Parameters.getInstance();
        BinomialDistribution bd = new BinomialDistribution(shape.referenceCardinality, parameters.getProbShaclP());
        return bd.probability((int) Math.floor(bd.getNumericalMean()));
    }

    public static void main(String[] args) {
        System.out.println((int) Math.floor(2.2));
        System.out.println((int) Math.floor(2.9));
        BinomialDistribution bd = new BinomialDistribution(310, 0.5);
        System.out.println(bd.getNumericalMean());
        System.out.println(bd.probability(155));
    }
}
