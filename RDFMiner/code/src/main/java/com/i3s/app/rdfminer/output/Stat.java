package com.i3s.app.rdfminer.output;

import com.i3s.app.rdfminer.RDFMiner;
import com.i3s.app.rdfminer.evolutionary.types.TypeCrossover;
import com.i3s.app.rdfminer.evolutionary.types.TypeMutation;
import com.i3s.app.rdfminer.evolutionary.types.TypeSelection;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * This class is used to map all results about statistics of GE on a object and generate a {@link org.json.JSONObject}
 * of it
 * 
 * @author Rémi FELIN
 *
 */
public class Stat {

	public int populationSize;
	public int maxLengthChromosome;
	public int maxWrapping;
	public long timeOut;
	public long timeCap;
	public double crossoverProbability;
	public double mutationProbability;
//	public boolean elitismSelection;
//	public double eliteSize;
	public String selectionMethod;
	public String crossoverMethod;
	public String mutationMethod;
	public double selectionSize;
	public List<JSONObject> generations = new ArrayList<>();

	public Stat() {
		this.populationSize = RDFMiner.parameters.populationSize;
		this.maxLengthChromosome = RDFMiner.parameters.initLenChromosome;
		this.maxWrapping = RDFMiner.parameters.maxWrapp;
		this.timeOut = RDFMiner.parameters.sparqlTimeOut;
		this.timeCap = RDFMiner.parameters.timeCap;
		this.crossoverProbability = RDFMiner.parameters.proCrossover;
		this.mutationProbability = RDFMiner.parameters.proMutation;
		this.selectionSize = (int) (RDFMiner.parameters.sizeSelection * RDFMiner.parameters.populationSize);
		this.selectionMethod = TypeSelection.getLabel(RDFMiner.parameters.typeSelection);
		this.crossoverMethod = TypeCrossover.getLabel(RDFMiner.parameters.typeCrossover);
		this.mutationMethod = TypeMutation.getLabel(RDFMiner.parameters.typeMutation);
	}

	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		json.put("populationSize", this.populationSize);
		json.put("maxLengthChromosome", this.maxLengthChromosome);
		json.put("maxWrapping", this.maxWrapping);
		json.put("crossoverProbability", this.crossoverProbability);
		json.put("mutationProbability", this.mutationProbability);
		json.put("selectionMethod", this.selectionMethod);
		json.put("mutationMethod", this.mutationMethod);
		json.put("crossoverMethod", this.crossoverMethod);
		json.put("selectionSize", this.selectionSize);
		json.put("generations", new JSONArray(this.generations));
		json.put("sparqlTimeout", (this.timeOut == 0 ? JSONObject.NULL : this.timeOut));
		json.put("timeCap", (this.timeCap == 0 ? JSONObject.NULL : this.timeCap));
		return json;
	}

}
