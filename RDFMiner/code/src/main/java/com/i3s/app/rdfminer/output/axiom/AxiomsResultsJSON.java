package com.i3s.app.rdfminer.output.axiom;

import java.util.List;

import com.i3s.app.rdfminer.output.Results;
import com.i3s.app.rdfminer.shacl.ValidationReport;
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
public class AxiomsResultsJSON extends Results {

	@Override
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		json.put("statistics", stats);
		json.put("axioms", new JSONArray(content));
		return json;
	}

}
