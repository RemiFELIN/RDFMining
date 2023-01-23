package com.i3s.app.rdfminer.evolutionary.tools;

import com.i3s.app.rdfminer.entity.Entity;
import com.i3s.app.rdfminer.generator.Generator;
import com.i3s.app.rdfminer.evolutionary.fitness.Fitness;
import com.i3s.app.rdfminer.evolutionary.individual.GEIndividual;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class Crowding {

	private static final Logger logger = Logger.getLogger(Crowding.class.getName());

	protected int distanceP1ToC1;
	protected int distanceP1ToC2;
	protected int distanceP2ToC1;
	protected int distanceP2ToC2;
	protected Entity parent1;
	protected Entity parent2;
	protected GEIndividual child1;
	protected GEIndividual child2;
	protected Generator generator;

	public Crowding(Entity parent1, Entity parent2, GEIndividual child1, GEIndividual child2, Generator generator) {
		this.generator = generator;
		this.parent1 = parent1;
		this.parent2 = parent2;
//		logger.info("parent1->\n   fitness: "+parent1.fitness+"\n   ind.getPheno: "+
//				parent1.individual.getPhenotype().getStringNoSpace()+"\n   ind.getGeno: "+
//				parent1.individual.getGenotype()+"\n   ind.getFitness: "+parent1.individual.getFitness().getDouble());
//		logger.info("parent2->\n   fitness: "+parent2.fitness+"\n   ind.getPheno: "+
//				parent2.individual.getPhenotype().getStringNoSpace()+"\n   ind.getGeno: "+
//				parent2.individual.getGenotype());
		logger.info("parent1: " + parent1.individual.getPhenotype().getStringNoSpace());
		logger.info("parent2: " + parent2.individual.getPhenotype().getStringNoSpace());
		logger.info("child1: " + child1.getPhenotype().getStringNoSpace());
		logger.info("child2: " + child2.getPhenotype().getStringNoSpace());
		this.child1 = child1;
		this.child2 = child2;
		this.distanceP1ToC1 = this.distance(this.parent1, this.child1);
		this.distanceP2ToC2 = this.distance(this.parent2, this.child2);
		this.distanceP1ToC2 = this.distance(this.parent1, this.child2);
		this.distanceP2ToC1 = this.distance(this.parent2, this.child1);
	}

	public ArrayList<Entity> getSurvivalSelection() throws URISyntaxException, IOException {
		ArrayList<Entity> survivals = new ArrayList<>();
		if (distanceP1ToC1 + distanceP2ToC2 >= distanceP1ToC2 + distanceP2ToC1) {
			survivals.add(compare(parent1, child1));
			survivals.add(compare(parent2, child2));
		} else {
			survivals.add(compare(parent1, child2));
			survivals.add(compare(parent2, child1));
		}
		return survivals;
	}

	public int distance(Entity a, GEIndividual b) {
		String word1 = a.individual.getPhenotype().toString();
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

	public Entity compare(Entity parent, GEIndividual child) throws URISyntaxException, IOException {
		// if the parent is not evaluated
		if (parent.individual.getFitness() == null) {
			logger.info("Compute parent fitness !");
			parent = Fitness.computeEntity(parent.individual, this.generator);
		}
		// compute fitness for the current child
		Entity childAsEntity = Fitness.computeEntity(child, this.generator);
		// we can compare parent and child
		if (parent.individual.getFitness().getDouble() <= child.getFitness().getDouble()) {
			return childAsEntity;
		} else {
			return parent;
		}
	}

}
