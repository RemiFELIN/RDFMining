package com.i3s.app.rdfminer.output.axiom;

import com.i3s.app.rdfminer.output.Results;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/**
 * 
 * This class is used to map all results from DBPedia on a object and generate a {@link org.json.JSONObject}
 * of it
 * 
 * @author Rémi FELIN
 *
 */
public class FullDBResultsJSON extends Results {

	public double possibility;
	public double necessity;
	public int referenceCardinality;
	public double generality;
	public double ari;
	public long elapsedTime;
	public boolean isTimeOut;
	public List<String> confirmations;
	public List<String> exceptions;



	@Override
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		json.put("referenceCardinality", referenceCardinality);
		json.put("exceptions", new JSONArray(exceptions));
		json.put("confirmations", new JSONArray(confirmations));
		json.put("possibility", possibility);
		json.put("necessity", necessity);
		json.put("ari", ari);
		json.put("generality", generality);
		json.put("isTimeOut", isTimeOut);
		json.put("elapsedTime", elapsedTime);
		return json;
	}

}
