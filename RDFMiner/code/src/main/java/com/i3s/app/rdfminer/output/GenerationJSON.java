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

	public int generation;
//	public double numSuccessMapping;
	public double populationDevelopmentRate;
	public double diversityCoefficient;
//	public double genotypeDiversityCoefficient;
	public double averageFitness;
	public long numIndividualsWithNonNullFitness;
	public double averageSumDistance;

	public void setGenerationJSON(ArrayList<Entity> originalPopulation, ArrayList<Entity> newPopulation, int curGeneration) {
		Statistics stat = new Statistics();
		this.generation = curGeneration;
		this.populationDevelopmentRate = EATools.getPopulationDevelopmentRate(originalPopulation, newPopulation);
		this.diversityCoefficient = (double) EATools.getDistinctGenotypePopulation(newPopulation).size() / newPopulation.size();
//		this.genotypeDiversityCoefficient = (double) EATools.getDistinctGenotypePopulation(entities).size()
//				/ entities.size();
		this.averageFitness = stat.computeAverageFitness(newPopulation);
		this.numIndividualsWithNonNullFitness = stat.getEntitiesWithNonNullFitness(newPopulation);
		if(RDFMiner.parameters.useNoveltySearch) {
			this.averageSumDistance = stat.getAverageSumDistance(newPopulation);
		}
	}

	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		json.put("generation", generation);
		json.put("populationDevelopmentRate", populationDevelopmentRate);
		json.put("diversityCoefficient", diversityCoefficient);
//		json.put("genotypeDiversityCoefficient", genotypeDiversityCoefficient);
		json.put("averageFitness", averageFitness);
		json.put("numIndividualsWithNonNullFitness", numIndividualsWithNonNullFitness);
		json.put("averageSumDistance",
				(RDFMiner.parameters.useNoveltySearch ? this.averageSumDistance : JSONObject.NULL));
		return json;
	}

}
