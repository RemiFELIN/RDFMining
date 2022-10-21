package com.i3s.app.rdfminer.output.axiom;

import com.i3s.app.rdfminer.output.Results;
import com.i3s.app.rdfminer.shacl.ValidationReport;
import org.json.JSONObject;

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
	public double numComplexAxiomSpecial;
	public long numIndividualsWithNonNullFitness;

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
		json.put("numComplexAxiomSpecial", numComplexAxiomSpecial);
		return json;
	}

}
