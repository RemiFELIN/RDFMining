package com.i3s.app.rdfminer.output;

import com.i3s.app.rdfminer.RDFMiner;
import com.i3s.app.rdfminer.entity.Entity;
import com.i3s.app.rdfminer.evolutionary.tools.EATools;
import com.i3s.app.rdfminer.launcher.GrammaticalEvolution;
import com.i3s.app.rdfminer.statistics.Statistics;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * 
 * This class is used to map all results about a generation of GE on a object and generate a {@link org.json.JSONObject}
 * of it
 * 
 * @author Rémi FELIN
 *
 */
public class GenerationJSON {

	public final static String GENERATION = "generation";
	public final static String POPULATION_DEVELOPMENT_RATE = "populationDevelopmentRate";
	public final static String DIVERSITY_COEFFICIENT = "diversityCoefficient";
	public final static String FITNESS = "fitness";
	public final static String AVERAGE_FITNESS = "averageFitness";
	public final static String NUM_INDIVIDUALS_WITH_NON_NULL_FITNESS = "numIndividualsWithNonNullFitness";
	public final static String AVERAGE_SUM_DISTANCE = "averageSumDistance";
	public final static String COMPUTATION_TIME = "computationTime";
	public final static String COMPUTATION_TIME_ARRAY = "computationTimeArray";
	public final static String NUM_RECOMBINAISON = "numRecombinaison";
	public final static String NUM_CROSSOVER = "numCrossover";
	public final static String NUM_MUTATION = "numMutation";

	public int generation;
	public double populationDevelopmentRate;
	public double diversityCoefficient;
	public double averageFitness;
	public ArrayList<Double> fitness = new ArrayList<>();
	public long numIndividualsWithNonNullFitness;
	public double averageSumDistance;
	public ArrayList<Long> durations;
	public int numRecombinaison;
	public int numCrossover;
	public int numMutation;

	public GenerationJSON(ArrayList<Entity> originalPopulation, ArrayList<Entity> newPopulation, int curGeneration, ArrayList<Long> durations) {
//		Statistics stat = new Statistics();
		this.generation = curGeneration;
		this.durations = durations;
		for(Entity entity : newPopulation) {
			this.fitness.add(entity.individual.getFitness().getDouble());
		}
		this.populationDevelopmentRate = EATools.getPopulationDevelopmentRate(originalPopulation, newPopulation);
		this.diversityCoefficient = (double) EATools.getDistinctGenotypesPopulation(newPopulation).size() / newPopulation.size();
		this.averageFitness = Statistics.computeAverageFitness(newPopulation);
		this.numIndividualsWithNonNullFitness = Statistics.getEntitiesWithNonNullFitness(newPopulation);
		this.numRecombinaison = GrammaticalEvolution.nRecombinaison;
		this.numCrossover = GrammaticalEvolution.nCrossover;
		this.numMutation = GrammaticalEvolution.nMutation;
		if(RDFMiner.parameters.useNoveltySearch) {
			this.averageSumDistance = Statistics.getAverageSumDistance(newPopulation);
		}
	}

	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		json.put(GENERATION, this.generation);
		json.put(COMPUTATION_TIME_ARRAY, new JSONArray(this.durations));
		json.put(COMPUTATION_TIME, this.durations.stream().mapToLong(a -> a).sum());
		json.put(FITNESS, new JSONArray(this.fitness));
		json.put(POPULATION_DEVELOPMENT_RATE, this.populationDevelopmentRate);
		json.put(DIVERSITY_COEFFICIENT, this.diversityCoefficient);
		json.put(AVERAGE_FITNESS, this.averageFitness);
		json.put(NUM_INDIVIDUALS_WITH_NON_NULL_FITNESS, this.numIndividualsWithNonNullFitness);
		json.put(NUM_RECOMBINAISON, this.numRecombinaison);
		json.put(NUM_CROSSOVER, this.numCrossover);
		json.put(NUM_MUTATION, this.numMutation);
		json.put(AVERAGE_SUM_DISTANCE,
				(RDFMiner.parameters.useNoveltySearch ? this.averageSumDistance : JSONObject.NULL));
		return json;
	}

}
