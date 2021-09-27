package com.i3s.app.rdfminer.grammar.evolutionary;

import com.i3s.app.rdfminer.grammar.evolutionary.fitness.FitnessEvaluation;
import com.i3s.app.rdfminer.grammar.evolutionary.individual.GEIndividual;

public class Crowding {

	protected int size;
	protected int distanceP1ToC1;
	protected int distanceP1ToC2;
	protected int distanceP2ToC1;
	protected int distanceP2ToC2;
	protected GEIndividual parent1;
	protected GEIndividual parent2;
	protected GEIndividual child1;
	protected GEIndividual child2;

	public Crowding(int size, GEIndividual parent1, GEIndividual parent2, GEIndividual child1, GEIndividual child2) {
		this.size = size;
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
			survivals[0] = compare(parent1, child1);
			survivals[1] = compare(parent2, child2);
		} else {
			survivals[0] = compare(parent1, child2);
			survivals[1] = compare(parent2, child1);
		}
		return survivals;
	}

	int distance(GEIndividual a, GEIndividual b) {
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
					int min = replace > insert ? insert : replace;
					min = delete > min ? min : delete;
					dp[i + 1][j + 1] = min;
				}
			}
		}
		return dp[len1][len2];
	}

//	int distance2(GEIndividual a, GEIndividual b) {
//		int[] a1 = ((GEChromosome) a.getGenotype().get(0)).toArray();
//		int[] b1 = ((GEChromosome) b.getGenotype().get(0)).toArray();
//		int d = 0;
//		for (int i = 0; i < a1.length; i++) {
//			for (int j = 0; j < a1.length; j++) {
//				if (b1[j] == a1[i]) {
//					d++;
//				}
//			}
//		}
//		return d;
//	}

	GEIndividual compare(GEIndividual parent, GEIndividual child) {
		// if parent don't have any value for fitness, we need to compute its value
		if(parent.getFitness() == null) {
			parent = FitnessEvaluation.updateIndividual(parent);
		}
		child = FitnessEvaluation.updateIndividual(child);
		// we can compare parent and child
		if (parent.getFitness().getDouble() <= child.getFitness().getDouble()) {
			return child;
		} else {
			return parent;
		}
	}

}
