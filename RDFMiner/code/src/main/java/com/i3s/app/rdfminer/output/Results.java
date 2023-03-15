package com.i3s.app.rdfminer.output;

import com.i3s.app.rdfminer.RDFMiner;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to map all results on an object and generate a {@link org.json.JSONObject} of it.
 * @author Rémi FELIN
 */
public class Results {

    public List<JSONObject> content;
    public JSONObject stats;

    public Results() {
        // set the content part of the results
        // i.e. a set of assessed OWL Axioms or SHACL Shapes
        RDFMiner.content = new ArrayList<>();
        // set statistics
        // i.e. parameters used; statistics over generations; ...
        RDFMiner.stats = new Stat();
    }

    /**
     * Convert a results to an instance of {@link JSONObject}
     * @return a JSONObject
     */
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("statistics", stats);
        json.put("entities", new JSONArray(content));
        return json;
    }

}
