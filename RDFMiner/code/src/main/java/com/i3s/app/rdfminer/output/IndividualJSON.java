package com.i3s.app.rdfminer.output;

import org.json.JSONObject;

public class IndividualJSON {

    public final static String GENOTYPE = "genotype";
    public final static String FITNESS = "fitness";

    private JSONObject json;
    public String genotype;
    public double fitness;

    public IndividualJSON(String genotype, double fitness) {
        json = new JSONObject();
        this.genotype = genotype;
        this.fitness = fitness;
    }

    public JSONObject toJSON() {
        this.json.put(GENOTYPE, this.genotype);
        this.json.put(FITNESS, this.fitness);
        return this.json;
    }

    public String getGenotype() {
        return this.json.getString(GENOTYPE);
    }

    public String getFitness() {
        return this.json.getString(FITNESS);
    }

}
