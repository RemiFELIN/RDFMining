package com.i3s.app.rdfminer.output;

import java.util.ArrayList;
import java.util.List;

import com.i3s.app.rdfminer.output.Results;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 
 * This class is used to map all results about statistics of GE on a object and generate a {@link org.json.JSONObject}
 * of it
 * 
 * @author Rémi FELIN
 *
 */
public class StatJSON {

	public int populationSize;
	public int maxLengthChromosome;
	public int maxWrapping;
	public int timeOut;
	public double crossoverProbability;
	public double mutationProbability;
	public boolean elitismSelection;
	public double eliteSize;
	public String selectionMethod;
	public double selectionSize;
	public List<JSONObject> generations = new ArrayList<>();

	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		json.put("populationSize", populationSize);
		json.put("maxLengthChromosome", maxLengthChromosome);
		json.put("maxWrapping", maxWrapping);
		json.put("crossoverProbability", crossoverProbability);
		json.put("mutationProbability", mutationProbability);
		json.put("elitismSelection", elitismSelection);
		json.put("eliteSize", eliteSize);
		json.put("selectionMethod", selectionMethod);
		json.put("selectionSize", selectionSize);
		json.put("generations", new JSONArray(generations));
		json.put("timeOut", (timeOut == 0 ? JSONObject.NULL : timeOut));
		return json;
	}

}
