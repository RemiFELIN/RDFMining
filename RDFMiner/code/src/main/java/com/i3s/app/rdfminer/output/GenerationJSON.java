package com.i3s.app.rdfminer.output;

import com.i3s.app.rdfminer.RDFMiner;
import com.i3s.app.rdfminer.entity.Entity;
import com.i3s.app.rdfminer.evolutionary.tools.EATools;
import com.i3s.app.rdfminer.statistics.Statistics;
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
	public final static String AVERAGE_FITNESS = "averageFitness";
	public final static String NUM_INDIVIDUALS_WITH_NON_NULL_FITNESS = "numIndividualsWithNonNullFitness";
	public final static String AVERAGE_SUM_DISTANCE = "averageSumDistance";
	public final static String COMPUTATION_TIME = "computationTime";

	public int generation;
	public double populationDevelopmentRate;
	public double diversityCoefficient;
	public double averageFitness;
	public long numIndividualsWithNonNullFitness;
	public double averageSumDistance;
	public long computationTime;

	public void setGenerationJSON(ArrayList<Entity> originalPopulation, ArrayList<Entity> newPopulation, int curGeneration, long computationTime) {
//		Statistics stat = new Statistics();
		this.generation = curGeneration;
		this.computationTime = computationTime;
		this.populationDevelopmentRate = EATools.getPopulationDevelopmentRate(originalPopulation, newPopulation);
		this.diversityCoefficient = (double) EATools.getDistinctPhenotypePopulation(newPopulation).size() / newPopulation.size();
		this.averageFitness = Statistics.computeAverageFitness(newPopulation);
		this.numIndividualsWithNonNullFitness = Statistics.getEntitiesWithNonNullFitness(newPopulation);
		if(RDFMiner.parameters.useNoveltySearch) {
			this.averageSumDistance = Statistics.getAverageSumDistance(newPopulation);
		}
	}

	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		json.put(GENERATION, this.generation);
		json.put(COMPUTATION_TIME, this.computationTime);
		json.put(POPULATION_DEVELOPMENT_RATE, this.populationDevelopmentRate);
		json.put(DIVERSITY_COEFFICIENT, this.diversityCoefficient);
		json.put(AVERAGE_FITNESS, this.averageFitness);
		json.put(NUM_INDIVIDUALS_WITH_NON_NULL_FITNESS, this.numIndividualsWithNonNullFitness);
		json.put(AVERAGE_SUM_DISTANCE,
				(RDFMiner.parameters.useNoveltySearch ? this.averageSumDistance : JSONObject.NULL));
		return json;
	}

}
