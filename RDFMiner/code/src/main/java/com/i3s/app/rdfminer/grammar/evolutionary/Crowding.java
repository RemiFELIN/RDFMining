package com.i3s.app.rdfminer.grammar.evolutionary;

import com.i3s.app.rdfminer.grammar.evolutionary.individual.GEIndividual;

import Individuals.GEChromosome;
//import Individuals.GEIndividual;

public class Crowding {

	protected int size;
	protected int distance_p1_c1;
	protected int distance_p2_c2;
	protected int distance_p1_c2;
	protected int distance_p2_c1;
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
		this.distance_p1_c1 = this.distance(this.parent1, this.child1);
		this.distance_p2_c2 = this.distance(this.parent2, this.child2);
		this.distance_p1_c2 = this.distance(this.parent1, this.child2);
		this.distance_p2_c1 = this.distance(this.parent2, this.child1);

	}

	GEIndividual[] SurvivalSelection() {
		int d1, d2;
		GEIndividual[] ListSurvival = new GEIndividual[2];
		d1 = distance_p1_c1 + distance_p2_c2;
		d2 = distance_p1_c2 + distance_p2_c1;

		if (d1 >= d2) {
			ListSurvival[0] = compare(parent1, child1);
			ListSurvival[1] = compare(parent2, child2);

		} else {
			ListSurvival[0] = compare(parent1, child2);
			ListSurvival[1] = compare(parent2, child1);
		}
		return ListSurvival;
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

	int distance2(GEIndividual a, GEIndividual b) {
		GEChromosome chromosome_a = (GEChromosome) a.getGenotype().get(0);
		GEChromosome chromosome_b = (GEChromosome) b.getGenotype().get(0);
		int[] a1 = chromosome_a.toArray();
		int[] b1 = chromosome_b.toArray();
		int n = a1.length;
		int i, j, d;
		d = 0;
		for (i = 0; i < n; i++) {
			for (j = 0; j < n; j++) {
				if (b1[j] == a1[i]) {
					d++;
				}

			}

		}
		return d;
	}

	GEIndividual compare(GEIndividual parent, GEIndividual child) {

		if (parent.getFitness().getDouble() <= child.getFitness().getDouble()) {

			return child;
		} else
			return parent;

	}

}
