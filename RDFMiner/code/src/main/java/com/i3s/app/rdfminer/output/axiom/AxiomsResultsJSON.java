package com.i3s.app.rdfminer.output.axiom;

import com.i3s.app.rdfminer.RDFMiner;
import com.i3s.app.rdfminer.output.Results;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * 
 * This class is used to map all results on an object and generate a {@link org.json.JSONObject}
 * of it
 * 
 * @author Rémi FELIN
 *
 */
public class AxiomsResultsJSON extends Results {

	public AxiomsResultsJSON() {
		// set the content part of the results
		// i.e. a set of assessed OWL Axioms or SHACL Shapes
		RDFMiner.content = new ArrayList<>();
		// set statistics
		// i.e. parameters used; statistics over generations; ...
		RDFMiner.stats = new StatJSON();
	}

	@Override
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		json.put("statistics", stats);
		json.put("axioms", new JSONArray(content));
		return json;
	}

}
