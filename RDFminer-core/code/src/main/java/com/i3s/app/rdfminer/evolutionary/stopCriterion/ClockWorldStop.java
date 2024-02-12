package com.i3s.app.rdfminer.evolutionary.stopCriterion;

import com.i3s.app.rdfminer.Parameters;

public class ClockWorldStop implements StopCriterion {

    public long maxTime;
    public long chrono;
    public int curGeneration;

    @Override
    public boolean isFinish() {
        if (this.chrono > this.maxTime) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void start() {
        Parameters parameters = Parameters.getInstance();
        // start chrono
        this.chrono = System.currentTimeMillis();
        // set current generation
        this.curGeneration = 1;
        // Max time to spent for GE
        // convert time: min to ms
        this.maxTime = chrono + parameters.getMaxMiningTime() * 60000L;
    }

    @Override
    public void update() {
        // update chrono
        this.chrono = System.currentTimeMillis();
        // update current generation
        this.curGeneration++;
    }

    @Override
    public int getCurGeneration() {
        return this.curGeneration;
    }

}
