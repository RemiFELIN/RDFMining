package com.i3s.app.rdfminer.output;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class AxiomTestJSON {

	public int referenceCardinality;
	public int numConfirmations;
	public int numExceptions;
	public double possibility;
	public double necessity;
	public long elapsedTime; // the time it took to test the axiom, in ms.
	public boolean isTimeout;
	public List<String> exceptions, confirmations;
	
	public AxiomTestJSON() {
		referenceCardinality = numConfirmations = numExceptions = 0;
		possibility = necessity = 0.0;
		elapsedTime = 0L;
		exceptions = confirmations = null;
		isTimeout = false;
	}
	
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		json.put("referenceCardinality", referenceCardinality);
		json.put("numConfirmations", numConfirmations);
		json.put("numExceptions", numExceptions);
		json.put("possibility", possibility);
		json.put("necessity", necessity);
		json.put("elapsedTime", elapsedTime);
		json.put("isTimeout", isTimeout);
		// if exceptions or confirmations list are not empty, they are defined and so they are not null
		if(exceptions != null) json.put("exceptions", new JSONArray(exceptions));
		if(confirmations != null) json.put("confirmations", new JSONArray(confirmations));
		return json;
	}
	
}
