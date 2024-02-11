//package com.i3s.app.rdfminer.output;
//
//import com.i3s.app.rdfminer.Parameters;
//import com.i3s.app.rdfminer.evolutionary.types.TypeCrossover;
//import com.i3s.app.rdfminer.evolutionary.types.TypeMutation;
//import com.i3s.app.rdfminer.evolutionary.types.TypeSelection;
//import org.json.JSONArray;
//import org.json.JSONObject;
//
///**
// *
// * This class is used to map all results about statistics of GE on a object and generate a {@link org.json.JSONObject}
// * of it
// *
// * @author Rémi FELIN
// *
// */
//public class Stat {
//
//	Parameters parameters = Parameters.getInstance();
//
//	public int populationSize;
//	public int maxLengthChromosome;
//	public int maxWrapping;
////	public long timeOut;
//	public long timeCap;
//	public double crossoverProbability;
//	public double mutationProbability;
//	public String selectionMethod;
//	public String crossoverMethod;
//	public String mutationMethod;
//	public double eliteSelectionSize;
//	public double selectionSize;
//	public JSONArray generations;
//
//	public Stat() {
//		this.populationSize = parameters.getPopulationSize();
//		this.maxLengthChromosome = parameters.getSizeChromosome();
//		this.maxWrapping = parameters.getMaxWrap();
////		this.timeOut = parameters.sparqlTimeOut;
//		this.timeCap = parameters.timeCap;
//		this.crossoverProbability = parameters.getProCrossover();
//		this.mutationProbability = parameters.getProMutation();
//		this.eliteSelectionSize = (int) (parameters.getEliteSelectionRate() * parameters.getPopulationSize());
//		this.selectionSize = (int) (parameters.getSelectionRate() * parameters.getPopulationSize());
//		this.selectionMethod = TypeSelection.getLabel(parameters.getSelectionType());
//		this.crossoverMethod = TypeCrossover.getLabel(parameters.getCrossoverType());
//		this.mutationMethod = TypeMutation.getLabel(parameters.getMutationType());
//		this.generations = new JSONArray();
//	}
//
//	public JSONObject toJSON() {
//		JSONObject json = new JSONObject();
//		json.put("populationSize", this.populationSize);
//		json.put("maxLengthChromosome", this.maxLengthChromosome);
//		json.put("maxWrapping", this.maxWrapping);
//		json.put("crossoverProbability", this.crossoverProbability);
//		json.put("mutationProbability", this.mutationProbability);
//		json.put("selectionMethod", this.selectionMethod);
//		json.put("mutationMethod", this.mutationMethod);
//		json.put("crossoverMethod", this.crossoverMethod);
//		json.put("eliteSelectionSize", this.eliteSelectionSize);
//		json.put("selectionSize", this.selectionSize);
//		json.put("generations", this.generations);
////		json.put("sparqlTimeout", (this.timeOut == 0 ? JSONObject.NULL : this.timeOut));
//		json.put("timeCap", (this.timeCap == 0 ? JSONObject.NULL : this.timeCap));
//		return json;
//	}
//
//}
