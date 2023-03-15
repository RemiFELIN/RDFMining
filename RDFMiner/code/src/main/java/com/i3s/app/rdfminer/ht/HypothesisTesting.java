package com.i3s.app.rdfminer.ht;

import com.i3s.app.rdfminer.RDFMiner;
import com.i3s.app.rdfminer.entity.shacl.Shape;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;

public class HypothesisTesting {

    public Shape shape;
    private Double X2;
    public boolean isAccepted;

    public HypothesisTesting(Shape shape) {
        this.shape = shape;
        this.X2 = null;
        // X^2 computation
        double nExcTheo = shape.referenceCardinality * Double.parseDouble(RDFMiner.parameters.probShaclP);
        double nConfTheo = shape.referenceCardinality - nExcTheo;
        if(shape.numExceptions <= nExcTheo) {
            // if observed error is lower, accept the shape
            this.isAccepted = true;
        } else if (nExcTheo >= 5 && nConfTheo >= 5) {
            // apply statistic test X2
            this.X2 = (Math.pow(shape.numExceptions - nExcTheo, 2) / nExcTheo) +
                    (Math.pow(shape.numConfirmations - nConfTheo, 2) / nConfTheo);
            double critical = new ChiSquaredDistribution(1).inverseCumulativeProbability(1 - RDFMiner.parameters.alpha);
            // test value and accept it if it's lower than critical value
            this.isAccepted = X2 <= critical;
        } else {
            // rejected !
            this.isAccepted = false;
        }
    }

    public String getAcceptanceTriple() {
        return this.shape.uri + " ex:acceptance \""+ this.isAccepted + "\"^^xsd:boolean .\n";
    }

    public String getX2ValueTriple() {
        return this.shape.uri + " ex:pvalue \"" + this.X2 + "\"^^xsd:double .\n";
    }

    public Double getX2() {
        return X2;
    }
}
