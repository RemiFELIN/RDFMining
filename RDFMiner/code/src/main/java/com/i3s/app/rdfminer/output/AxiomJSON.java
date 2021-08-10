package com.i3s.app.rdfminer.output;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 
 * This class is used to map all results for a axiom on a object and generate a {@link org.json.JSONObject}
 * of it
 * 
 * @author Rémi FELIN
 *
 */
public class AxiomJSON extends JSONObject {

	// v1.0
	public String axiom;
	public int referenceCardinality = 0;
	public int numConfirmations = 0;
	public int numExceptions = 0;
	public double possibility = 0.0;
	public double necessity;
	public long elapsedTime = 0L; // the time it took to test the axiom, in ms.
	public boolean isTimeout;
	public List<String> exceptions, confirmations = new ArrayList<>();
	// v1.3
	public int k;
	// public double uPhi;
	public double generality;
	public double fitness;
	public boolean isMapped;
	// public JSONObject resultsFromDBPedia;

	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		// v1.0
		json.put("axiom", axiom);
		json.put("referenceCardinality", referenceCardinality);
		json.put("numConfirmations", numConfirmations);
		json.put("numExceptions", numExceptions);
		json.put("possibility", possibility);
		json.put("necessity", necessity);
		json.put("elapsedTime", elapsedTime);
		json.put("isTimeout", isTimeout);
		// if exceptions or confirmations list are not empty, they are defined and so
		// they are not null
		// if (exceptions != null)
		json.put("exceptions", new JSONArray(exceptions));
		// if (confirmations != null)
		json.put("confirmations", new JSONArray(confirmations));
		// v1.3
		json.put("k", k);
		// json.put("uPhi", uPhi);
		json.put("fitness", fitness);
		json.put("generality", generality);
		json.put("isMapped", isMapped);
		// json.put("resultsFromDBPedia", resultsFromDBPedia);
		return json;
	}

}
