package com.i3s.app.rdfminer.output.axiom;

import com.i3s.app.rdfminer.entity.axiom.Axiom;
import com.i3s.app.rdfminer.entity.shacl.Shape;
import com.i3s.app.rdfminer.grammar.evolutionary.EATools;
import com.i3s.app.rdfminer.grammar.evolutionary.individual.GEIndividual;
import com.i3s.app.rdfminer.output.Results;
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
public class GenerationJSON extends Results {

	public int idGeneration;
	public double numSuccessMapping;
	public double diversityCoefficient;
	public double genotypeDiversityCoefficient;
	public double averageFitness;
	public double numComplexAxiom;
//	public double numComplexAxiomSpecial;
	public long numIndividualsWithNonNullFitness;

	public void setGenerationJSONFromAxioms(ArrayList<Axiom> axioms, ArrayList<Axiom> distinctAxioms, int curGeneration) {
		Statistics stat = new Statistics();
		this.idGeneration = curGeneration;
		this.numSuccessMapping = stat.getSuccessMappingRateFromAxioms(distinctAxioms);
		this.diversityCoefficient = (double) distinctAxioms.size() / axioms.size();
		this.genotypeDiversityCoefficient = (double) EATools.getDistinctGenotypePopulationFromAxioms(axioms).size()
				/ axioms.size();
		this.averageFitness = stat.computeAverageFitnessFromAxioms(distinctAxioms);
		this.numComplexAxiom = stat.getCountComplexAxioms(distinctAxioms);
//		this.numComplexAxiomSpecial = stat.getCountComplexAxiomSpecial(distinctAxioms);
		this.numIndividualsWithNonNullFitness = stat.getAxiomsWithNonNullFitness(distinctAxioms);
	}

	public void setGenerationJSONFromShapes(ArrayList<Shape> shapes, ArrayList<Shape> distinctShapes, int curGeneration) {
		Statistics stat = new Statistics();
		this.idGeneration = curGeneration;
		this.numSuccessMapping = stat.getSuccessMappingRateFromShapes(distinctShapes);
		this.diversityCoefficient = (double) distinctShapes.size() / shapes.size();
		this.genotypeDiversityCoefficient = (double) EATools.getDistinctGenotypePopulationFromShapes(shapes).size()
				/ shapes.size();
		this.averageFitness = stat.computeAverageFitnessFromShapes(distinctShapes);
		// In this version, we don't consider the complex concept in the context of SHACL Shapes mining
		this.numComplexAxiom = 0;
//		this.numComplexAxiomSpecial = stat.getCountComplexAxiomSpecial(distinctAxioms);
		this.numIndividualsWithNonNullFitness = stat.getShapesWithNonNullFitness(distinctShapes);
	}

	@Override
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		json.put("idGeneration", idGeneration);
		json.put("numSuccessMapping", numSuccessMapping);
		json.put("diversityCoefficient", diversityCoefficient);
		json.put("genotypeDiversityCoefficient", genotypeDiversityCoefficient);
		json.put("averageFitness", averageFitness);
		json.put("numIndividualsWithNonNullFitness", numIndividualsWithNonNullFitness);
		json.put("numComplexAxiom", numComplexAxiom);
//		json.put("numComplexAxiomSpecial", numComplexAxiomSpecial);
		return json;
	}

}
