package com.i3s.app.rdfminer.output;

import com.i3s.app.rdfminer.entity.Entity;
import com.i3s.app.rdfminer.evolutionary.tools.EATools;
import com.i3s.app.rdfminer.launcher.GrammaticalEvolution;
import com.i3s.app.rdfminer.statistics.Statistics;

import java.util.ArrayList;

/**
 * 
 * This class is used to map all results about a generation of GE on a object and generate a {@link org.json.JSONObject}
 * of it
 * 
 * @author Rémi FELIN
 *
 */
public class Generation {

	private int generation;
	private double populationDevelopmentRate;
	private double diversityCoefficient;
	private double averageFitness;
	private ArrayList<Double> fitnesses = new ArrayList<>();
	private long numIndividualsWithNonNullFitness;
//	public double averageSumDistance;
	private long computationTime;
	private ArrayList<Long> computationTimes = new ArrayList<>();
	private int numRecombinaison;
	private int numCrossover;
	private int numMutation;

	public Generation(ArrayList<Entity> originalPopulation, ArrayList<Entity> newPopulation, int curGeneration) {
//		Statistics stat = new Statistics();
		this.generation = curGeneration;
		// fill computation times with elapsed time value of new individuals
		newPopulation.stream().map(entity -> entity.elapsedTime).forEach(elapsedTime -> this.computationTimes.add(elapsedTime));
		// compute the sum of all computation time
		this.computationTime = this.computationTimes.stream().mapToLong(a -> a).sum();
		// fill fitnesses with fitness value of new individuals
		newPopulation.stream().map(entity -> entity.individual.getFitness().getDouble()).forEach(fitness -> this.fitnesses.add(fitness));
		this.populationDevelopmentRate = EATools.getPopulationDevelopmentRate(originalPopulation, newPopulation);
		this.diversityCoefficient = (double) EATools.getDistinctGenotypesPopulation(newPopulation).size() / newPopulation.size();
		this.averageFitness = Statistics.computeAverageFitness(newPopulation);
		this.numIndividualsWithNonNullFitness = Statistics.getEntitiesWithNonNullFitness(newPopulation);
		this.numRecombinaison = GrammaticalEvolution.nRecombinaison;
		this.numCrossover = GrammaticalEvolution.nCrossover;
		this.numMutation = GrammaticalEvolution.nMutation;
//		if(parameters.isUseNoveltySearch()) {
//			this.averageSumDistance = Statistics.getAverageSumDistance(newPopulation);
//		}
	}

	public int getGeneration() {
		return generation;
	}

	public void setGeneration(int generation) {
		this.generation = generation;
	}

	public double getPopulationDevelopmentRate() {
		return populationDevelopmentRate;
	}

	public void setPopulationDevelopmentRate(double populationDevelopmentRate) {
		this.populationDevelopmentRate = populationDevelopmentRate;
	}

	public double getDiversityCoefficient() {
		return diversityCoefficient;
	}

	public void setDiversityCoefficient(double diversityCoefficient) {
		this.diversityCoefficient = diversityCoefficient;
	}

	public double getAverageFitness() {
		return averageFitness;
	}

	public void setAverageFitness(double averageFitness) {
		this.averageFitness = averageFitness;
	}

	public ArrayList<Double> getFitnesses() {
		return fitnesses;
	}

	public void setFitnesses(ArrayList<Double> fitnesses) {
		this.fitnesses = fitnesses;
	}

	public long getNumIndividualsWithNonNullFitness() {
		return numIndividualsWithNonNullFitness;
	}

	public void setNumIndividualsWithNonNullFitness(long numIndividualsWithNonNullFitness) {
		this.numIndividualsWithNonNullFitness = numIndividualsWithNonNullFitness;
	}

	public long getComputationTime() {
		return computationTime;
	}

	public void setComputationTime(long computationTime) {
		this.computationTime = computationTime;
	}

	public ArrayList<Long> getComputationTimes() {
		return computationTimes;
	}

	public void setComputationTimes(ArrayList<Long> computationTimes) {
		this.computationTimes = computationTimes;
	}

//	public JSONObject toJSON() {
//		JSONObject json = new JSONObject();
//		json.put(GENERATION, this.generation);
//		json.put(COMPUTATION_TIME_ARRAY, new JSONArray(this.computationTimeArray));
//		json.put(COMPUTATION_TIME, this.computationTimeArray.stream().mapToLong(a -> a).sum());
////		json.put(FITNESS, new JSONArray(this.fitness));
//		json.put(POPULATION_DEVELOPMENT_RATE, this.populationDevelopmentRate);
//		json.put(DIVERSITY_COEFFICIENT, this.diversityCoefficient);
//		json.put(AVERAGE_FITNESS, this.averageFitness);
//		json.put(NUM_INDIVIDUALS_WITH_NON_NULL_FITNESS, this.numIndividualsWithNonNullFitness);
//		json.put(NUM_RECOMBINAISON, this.numRecombinaison);
//		json.put(NUM_CROSSOVER, this.numCrossover);
//		json.put(NUM_MUTATION, this.numMutation);
////		json.put(AVERAGE_SUM_DISTANCE,
////				(parameters.isUseNoveltySearch() ? this.averageSumDistance : JSONObject.NULL));
//		return json;
//	}

}
