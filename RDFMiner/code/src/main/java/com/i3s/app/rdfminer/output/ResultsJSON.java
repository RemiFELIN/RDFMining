package com.i3s.app.rdfminer.output;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 
 * This class is used to map all results on a object and generate a {@link org.json.JSONObject}
 * of it
 * 
 * @author Rémi FELIN
 *
 */
public class ResultsJSON {

	public JSONObject stats;
	public List<JSONObject> axioms;
	
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		json.put("statistics", stats);
		json.put("axioms", new JSONArray(axioms));
		return json;
	}
	
}
