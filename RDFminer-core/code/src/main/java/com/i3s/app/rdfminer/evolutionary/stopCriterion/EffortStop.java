package com.i3s.app.rdfminer.evolutionary.stopCriterion;

import com.i3s.app.rdfminer.RDFMiner;

public class EffortStop implements StopCriterion {

    public int curGeneration;

    @Override
    public boolean isFinish() {
        return RDFMiner.parameters.populationSize * this.curGeneration >= RDFMiner.parameters.effort;
    }

    @Override
    public void start() {
        // set current generation
        this.curGeneration = 1;
    }

    @Override
    public void update() {
        // update current generation
        this.curGeneration++;
    }

    @Override
    public int getCurGeneration() {
        return this.curGeneration;
    }
}
