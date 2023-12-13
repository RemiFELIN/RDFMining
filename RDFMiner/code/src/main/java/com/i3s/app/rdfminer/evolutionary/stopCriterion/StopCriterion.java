package com.i3s.app.rdfminer.evolutionary.stopCriterion;

/**
 * This class is used to stop the grammatical evolution according to the criterion chosen by user
 */
public interface StopCriterion {

    public abstract boolean isFinish();

    public abstract void start();

    public abstract void update();

    public abstract int getCurGeneration();

}
