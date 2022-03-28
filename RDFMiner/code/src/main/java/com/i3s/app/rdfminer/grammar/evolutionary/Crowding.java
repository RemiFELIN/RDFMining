package com.i3s.app.rdfminer.grammar.evolutionary;

import Individuals.FitnessPackage.BasicFitness;
import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.grammar.evolutionary.fitness.AxiomFitnessEvaluation;
import com.i3s.app.rdfminer.grammar.evolutionary.individual.GEIndividual;
import com.i3s.app.rdfminer.mode.Mode;
import com.i3s.app.rdfminer.shacl.Shape;
import com.i3s.app.rdfminer.shacl.ShapesManager;
import com.i3s.app.rdfminer.shacl.ValidationReport;
import com.i3s.app.rdfminer.sparql.corese.CoreseEndpoint;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;

public class Crowding {

	private static final Logger logger = Logger.getLogger(Crowding.class.getName());

//	protected int size;
	protected int distanceP1ToC1;
	protected int distanceP1ToC2;
	protected int distanceP2ToC1;
	protected int distanceP2ToC2;
	protected GEIndividual parent1;
	protected GEIndividual parent2;
	protected GEIndividual child1;
	protected GEIndividual child2;
	protected Mode mode;

	public Crowding(GEIndividual parent1, GEIndividual parent2, GEIndividual child1, GEIndividual child2, Mode mode) {
//		this.size = size;
		this.mode = mode;
		this.parent1 = parent1;
		this.parent2 = parent2;
		this.child1 = child1;
		this.child2 = child2;
		this.distanceP1ToC1 = this.distance(this.parent1, this.child1);
		this.distanceP2ToC2 = this.distance(this.parent2, this.child2);
		this.distanceP1ToC2 = this.distance(this.parent1, this.child2);
		this.distanceP2ToC1 = this.distance(this.parent2, this.child1);
	}

	GEIndividual[] getSurvivalSelection() {
		int d1, d2;
		GEIndividual[] survivals = new GEIndividual[2];
		d1 = distanceP1ToC1 + distanceP2ToC2;
		d2 = distanceP1ToC2 + distanceP2ToC1;
		if (d1 >= d2) {
			survivals[0] = compare(parent1, child1, mode);
			survivals[1] = compare(parent2, child2, mode);
		} else {
			survivals[0] = compare(parent1, child2, mode);
			survivals[1] = compare(parent2, child1, mode);
		}
		return survivals;
	}

//	public static ArrayList<GEIndividual> getSurvivalSelectionFromList(ArrayList<HashMap<GEIndividual, GEIndividual>> hashMapArrayList) {
//		ArrayList<GEIndividual> evaluatedIndividuals = new ArrayList<>();
//		// for each couple of parents and their child
//		for(HashMap<GEIndividual, GEIndividual> parChild : hashMapArrayList) {
//			// define individuals
//			GEIndividual parent1 = (GEIndividual) parChild.keySet().toArray()[0];
//			GEIndividual parent2 = (GEIndividual) parChild.keySet().toArray()[1];
//			GEIndividual child1 = parChild.get(parent1);
//			GEIndividual child2 = parChild.get(parent2);
//			int distanceP1ToC1 = distance(parent1, child1);
//			this.distanceP2ToC2 = this.distance(this.parent2, this.child2);
//			this.distanceP1ToC2 = this.distance(this.parent1, this.child2);
//			this.distanceP2ToC1 = this.distance(this.parent2, this.child1);
//			// apply get survival selection
//			if()
//		}
//		return evaluatedIndividuals;
//	}

	public int distance(GEIndividual a, GEIndividual b) {
		String word1 = a.getPhenotype().toString();
		String word2 = b.getPhenotype().toString();
		int len1 = word1.length();
		int len2 = word2.length();
		int[][] dp = new int[len1 + 1][len2 + 1];

		for (int i = 0; i <= len1; i++) {
			dp[i][0] = i;
		}
		for (int j = 0; j <= len2; j++) {
			dp[0][j] = j;
		}
		// iterate though, and check last char
		for (int i = 0; i < len1; i++) {
			char c1 = word1.charAt(i);
			for (int j = 0; j < len2; j++) {
				char c2 = word2.charAt(j);
				// if last two chars equal
				if (c1 == c2) {
					// update dp value for +1 length
					dp[i + 1][j + 1] = dp[i][j];
				} else {
					int replace = dp[i][j] + 1;
					int insert = dp[i][j + 1] + 1;
					int delete = dp[i + 1][j] + 1;
					int min = Math.min(replace, insert);
					min = Math.min(delete, min);
					dp[i + 1][j + 1] = min;
				}
			}
		}
		return dp[len1][len2];
	}

	public static GEIndividual compare(GEIndividual parent, GEIndividual child, Mode mode) {
		if(mode.isAxiomMode()) {
			AxiomFitnessEvaluation fit = new AxiomFitnessEvaluation();
			// if parent don't have any value for fitness, we need to compute its value
			if (parent.getFitness() == null) {
				parent = fit.updateIndividual(parent);
			}
			child = fit.updateIndividual(child);
		}
		// we can compare parent and child
		if (parent.getFitness().getDouble() <= child.getFitness().getDouble()) {
			return child;
		} else {
			return parent;
		}
	}

}
