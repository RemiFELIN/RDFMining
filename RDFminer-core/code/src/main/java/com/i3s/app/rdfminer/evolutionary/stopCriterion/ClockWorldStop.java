package com.i3s.app.rdfminer.evolutionary.stopCriterion;

import com.i3s.app.rdfminer.RDFMiner;
import org.apache.log4j.Logger;

public class ClockWorldStop implements StopCriterion {

    private static final Logger logger = Logger.getLogger(ClockWorldStop.class.getName());

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
        // start chrono
        this.chrono = System.currentTimeMillis();
        // set current generation
        this.curGeneration = 1;
        // Max time to spent for GE
        // convert time: min to ms
        this.maxTime = chrono + RDFMiner.parameters.maxTime * 60000L;
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
