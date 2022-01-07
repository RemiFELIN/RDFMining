package com.i3s.app.rdfminer.output;

import org.json.JSONObject;

/**
 * 
 * This class is used to map all results from DBPedia on a object and generate a {@link org.json.JSONObject}
 * of it
 * 
 * @author Rémi FELIN
 *
 */
public class DBPediaJSON {

	public double possibility;
	public double necessity;
	public int referenceCardinality;
	public double generality;
	public double ari;
	public long elapsedTime;
	public boolean isTimeOut;
	
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		json.put("possibility", possibility);
		json.put("necessity", necessity);
		json.put("ari", ari);
		json.put("referenceCardinality", referenceCardinality);
		json.put("generality", generality);
		json.put("isTimeOut", isTimeOut);
		json.put("elapsedTime", elapsedTime);
		return json;
	}
	
}
