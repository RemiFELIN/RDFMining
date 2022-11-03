package com.i3s.app.rdfminer.output;

import org.json.JSONObject;

import java.util.List;

public abstract class Results {

    public List<JSONObject> content;
    public JSONObject stats;

    /**
     * Convert a results to an instance of {@link JSONObject}
     * @return a JSONObject
     */
    public abstract JSONObject toJSON();

}
