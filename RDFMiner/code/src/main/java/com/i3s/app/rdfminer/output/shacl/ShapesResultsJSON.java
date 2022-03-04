package com.i3s.app.rdfminer.output.shacl;

import com.i3s.app.rdfminer.output.Results;
import org.json.JSONArray;
import org.json.JSONObject;

public class ShapesResultsJSON extends Results {

    @Override
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("statistics", stats);
        json.put("shapes", new JSONArray(content));
        return json;
    }

}
