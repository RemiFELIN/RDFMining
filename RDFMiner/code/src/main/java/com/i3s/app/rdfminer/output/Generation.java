package com.i3s.app.rdfminer.output;

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
public class Generation {

	public int generation;
	public double numSuccessMapping;
	public double diversityCoefficient;
	public double genotypeDiversityCoefficient;
	public double averageFitness;
	public long numIndividualsWithNonNullFitness;

	public void setGenerationJSON(ArrayList<Entity> entities, ArrayList<Entity> distinctEntities, int curGeneration) {
		Statistics stat = new Statistics();
		this.generation = curGeneration;
		this.numSuccessMapping = stat.getCountSuccessMapping(distinctEntities);
		this.diversityCoefficient = (double) distinctEntities.size() / entities.size();
		this.genotypeDiversityCoefficient = (double) EATools.getDistinctGenotypePopulation(entities).size()
				/ entities.size();
		this.averageFitness = stat.computeAverageFitness(distinctEntities);
		this.numIndividualsWithNonNullFitness = stat.getEntitiesWithNonNullFitness(distinctEntities);
	}

	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		json.put("generation", generation);
		json.put("numSuccessMapping", numSuccessMapping);
		json.put("diversityCoefficient", diversityCoefficient);
		json.put("genotypeDiversityCoefficient", genotypeDiversityCoefficient);
		json.put("averageFitness", averageFitness);
		json.put("numIndividualsWithNonNullFitness", numIndividualsWithNonNullFitness);
		return json;
	}

}
