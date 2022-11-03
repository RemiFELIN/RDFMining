package com.i3s.app.rdfminer.grammar.evolutionary;

import com.i3s.app.rdfminer.entity.axiom.Axiom;
import com.i3s.app.rdfminer.grammar.evolutionary.fitness.AxiomFitnessEvaluation;
import com.i3s.app.rdfminer.grammar.evolutionary.individual.GEIndividual;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class Crowding {

	private static final Logger logger = Logger.getLogger(Crowding.class.getName());

//	protected int size;
	protected int distanceP1ToC1;
	protected int distanceP1ToC2;
	protected int distanceP2ToC1;
	protected int distanceP2ToC2;
	protected Axiom axiomParent1;
	protected Axiom axiomParent2;
	protected Axiom axiomChild1;
	protected Axiom axiomChild2;
	protected ArrayList<Axiom> axioms;
	protected GEIndividual shapeParent1;
	protected GEIndividual shapeParent2;
	protected GEIndividual shapeChild1;
	protected GEIndividual shapeChild2;

	public Crowding(ArrayList<Axiom> axioms, Axiom axiomParent1, Axiom axiomParent2, Axiom axiomChild1, Axiom axiomChild2) {
//		this.size = size;
		this.axioms = axioms;
		this.axiomParent1 = axiomParent1;
		this.axiomParent2 = axiomParent2;
		this.axiomChild1 = axiomChild1;
		this.axiomChild2 = axiomChild2;
		this.distanceP1ToC1 = this.distance(this.axiomParent1.individual, this.axiomChild1.individual);
		this.distanceP2ToC2 = this.distance(this.axiomParent2.individual, this.axiomChild2.individual);
		this.distanceP1ToC2 = this.distance(this.axiomParent1.individual, this.axiomChild2.individual);
		this.distanceP2ToC1 = this.distance(this.axiomParent2.individual, this.axiomChild1.individual);
	}

	public Crowding(GEIndividual shapeParent1, GEIndividual shapeParent2, GEIndividual shapeChild1, GEIndividual shapeChild2) {
		this.shapeParent1 = shapeParent1;
		this.shapeParent2 = shapeParent2;
		this.shapeChild1 = shapeChild1;
		this.shapeChild2 = shapeChild2;
		this.distanceP1ToC1 = this.distance(this.shapeParent1, this.shapeChild1);
		this.distanceP2ToC2 = this.distance(this.shapeParent2, this.shapeChild2);
		this.distanceP1ToC2 = this.distance(this.shapeParent1, this.shapeChild2);
		this.distanceP2ToC1 = this.distance(this.shapeParent2, this.shapeChild1);
	}

	public GEIndividual[] getShapesSurvivalSelection() {
		GEIndividual[] survivals = new GEIndividual[2];
		if (distanceP1ToC1 + distanceP2ToC2 >= distanceP1ToC2 + distanceP2ToC1) {
			survivals[0] = compareShapes(shapeParent1, shapeChild1);
			survivals[1] = compareShapes(shapeParent2, shapeChild2);
		} else {
			survivals[0] = compareShapes(shapeParent1, shapeChild2);
			survivals[1] = compareShapes(shapeParent2, shapeChild1);
		}
		return survivals;
	}

	public Axiom[] getAxiomsSurvivalSelection() throws URISyntaxException, IOException {
		Axiom[] survivals = new Axiom[2];
		if (distanceP1ToC1 + distanceP2ToC2 >= distanceP1ToC2 + distanceP2ToC1) {
			survivals[0] = compareAxioms(axiomParent1, axiomChild1, axioms);
			survivals[1] = compareAxioms(axiomParent2, axiomChild2, axioms);
		} else {
			survivals[0] = compareAxioms(axiomParent1, axiomChild2, axioms);
			survivals[1] = compareAxioms(axiomParent2, axiomChild1, axioms);
		}
		return survivals;
	}

	public static Axiom compareAxioms(Axiom parent, Axiom child, ArrayList<Axiom> axioms) throws URISyntaxException, IOException {
		// if parent don't have any value for fitness, we need to compute its value
		if (parent.individual.getFitness() == null) {
			logger.info("parent fitness is null, assess it !");
			parent = AxiomFitnessEvaluation.updateIndividual(parent, axioms);
		}
		child = AxiomFitnessEvaluation.updateIndividual(child, axioms);
//		logger.info("Parent: " + parent.individual.getFitness().getDouble() + " <= child: " + child.individual.getFitness().getDouble() + " ?");
		if (parent.individual.getFitness().getDouble() < child.individual.getFitness().getDouble()) {
			logger.info("child is choosen !");
			return child;
		} else {
			logger.info("keep the parent !");
			return parent;
		}
	}

	public static GEIndividual compareShapes(GEIndividual parent, GEIndividual child) {
		if (parent.getFitness().getDouble() <= child.getFitness().getDouble())
			return child;
		return parent;
	}

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

}
