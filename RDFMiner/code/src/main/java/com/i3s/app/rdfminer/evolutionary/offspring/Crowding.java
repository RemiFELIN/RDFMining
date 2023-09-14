package com.i3s.app.rdfminer.evolutionary.offspring;

import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.RDFMiner;
import com.i3s.app.rdfminer.entity.Entity;
import com.i3s.app.rdfminer.evolutionary.fitness.Fitness;
import com.i3s.app.rdfminer.evolutionary.fitness.novelty.NoveltySearch;
import com.i3s.app.rdfminer.evolutionary.fitness.novelty.Similarity;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.FitnessPackage.BasicFitness;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.GEIndividual;
import com.i3s.app.rdfminer.evolutionary.tools.EATools;
import com.i3s.app.rdfminer.generator.Generator;
import com.i3s.app.rdfminer.launcher.GrammaticalEvolution;
import com.i3s.app.rdfminer.sparql.corese.CoreseEndpoint;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class Crowding {

	private static final Logger logger = Logger.getLogger(Crowding.class.getName());

	protected ArrayList<Entity> entities;
	protected double similarityP1ToC1;
	protected double similarityP1ToC2;
	protected double similarityP2ToC1;
	protected double similarityP2ToC2;
	protected Entity parent1;
	protected Entity parent2;
	protected Entity child1;
	protected Entity child2;
	protected Generator generator;

	public Crowding(Entity parent1, Entity parent2, GEIndividual child1, GEIndividual child2,
					ArrayList<Entity> entities, Generator generator) throws URISyntaxException, IOException {
		this.generator = generator;
		this.entities = entities;
		this.parent1 = parent1;
		this.parent2 = parent2;
		logger.debug("gen(child1) == gen(parent1) ? " + EATools.compareIndividuals(parent1.individual, child1));
//		logger.debug("# parent 1: " + parent1.individual.getPhenotype().getStringNoSpace());
		if(EATools.compareIndividuals(parent1.individual, child1)) {
//			logger.debug("No differences observed between the parent and its child ...");
			this.child1 = this.parent1;
			this.similarityP1ToC1 = 1;
			this.similarityP2ToC1 = this.distance(this.parent2, this.child1);
		} else {
			this.child1 = getEntityFromIndividual(child1);
			this.similarityP1ToC1 = this.distance(this.parent1, this.child1);
			this.similarityP2ToC1 = this.distance(this.parent2, this.child1);
		}
//		logger.debug("# parent 2: " + parent2.individual.getPhenotype().getStringNoSpace());
		logger.debug("gen(child2) == gen(parent2) ? " + EATools.compareIndividuals(parent2.individual, child2));
		if(EATools.compareIndividuals(parent2.individual, child2)) {
//			logger.debug("No differences observed between the parent and its child ...");
			this.child2 = this.parent2;
			this.similarityP1ToC2 = this.distance(this.parent1, this.child2);
			this.similarityP2ToC2 = 1;
		} else {
			this.child2 = getEntityFromIndividual(child2);
			this.similarityP1ToC2 = this.distance(this.parent1, this.child2);
			this.similarityP2ToC2 = this.distance(this.parent2, this.child2);
		}
	}

	public ArrayList<Entity> getSurvivalSelection() throws URISyntaxException, IOException {
		ArrayList<Entity> survivals = new ArrayList<>();
		// for each couple, we will minimize the similarities betweem them
		if (similarityP1ToC1 <= similarityP1ToC2) survivals.add(compare(parent1, child1));
		else survivals.add(compare(parent1, child2));
		if (similarityP2ToC2 <= similarityP2ToC1) survivals.add(compare(parent2, child2));
		else survivals.add(compare(parent2, child1));
		logger.debug("Survival selection done !");
		for(Entity ent : survivals) {
			logger.debug("survival: " + ent.individual.getPhenotype().getStringNoSpace());
		}
		return survivals;
	}

	private double distance(Entity phi1, Entity phi2) throws URISyntaxException, IOException {
		if(RDFMiner.parameters.useNoveltySearch) {
			return similarityDistance(phi1, phi2);
		} else {
			return levenshteinDistance(phi1, phi2);
		}
	}

	private double similarityDistance(Entity phi1, Entity phi2) throws URISyntaxException, IOException {
		if(RDFMiner.similarityMap.get(phi1, phi2) != null) {
//			logger.debug("get similarity value from similarity map ...");
			return RDFMiner.similarityMap.get(phi1, phi2);
		} else {
			Similarity sim = new Similarity(phi1, phi2);
			CoreseEndpoint endpoint = new CoreseEndpoint(Global.TRAINING_SPARQL_ENDPOINT, Global.PREFIXES);
			return sim.getModifiedSimilarity(endpoint);
		}
	}

	private double levenshteinDistance(Entity a, Entity b) {
		String word1 = a.individual.getPhenotype().toString();
		String word2 = b.individual.getPhenotype().toString();
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
		if (dp[len1][len2] == 0) return 0;
		return 1 / dp[len1][len2];
	}

	private Entity compare(Entity parent, Entity child) throws URISyntaxException, IOException {
		// if the parent is not evaluated
		if (parent.individual.getFitness() == null) {
//			logger.warn("Compute parent fitness !");
			parent = Fitness.computeEntity(parent.individual, this.generator);
		}
		// if the child is not evaluated
		if (child.individual.getFitness() == null) {
//			logger.warn("Compute child fitness !");
			child = Fitness.computeEntity(child.individual, this.generator);
		}
		// we can compare parent and child
//		logger.info("update fitness of parent and child");
		parent.individual.setFitness(new BasicFitness(NoveltySearch.updateFitness(parent), parent.individual));
		child.individual.setFitness(new BasicFitness(NoveltySearch.updateFitness(child), child.individual));
		// log if the offspring is different from its parent and if its fitness is upper to parent's fitness

		if(child.individual.getGenotype() != parent.individual.getGenotype() &&
				child.individual.getFitness().getDouble() > parent.individual.getFitness().getDouble()) {
			logger.info("A better offspring has been found !");
		}
		// compare their fitness
		if(parent.individual.getFitness().getDouble() < child.individual.getFitness().getDouble())
			GrammaticalEvolution.nBetterIndividual++;
		if(parent.individual.getFitness().getDouble() <= child.individual.getFitness().getDouble()) {
			return child;
		} else {
//			logger.debug("Keep the parent alive ...");
			return parent;
		}
	}

	private Entity getEntityFromIndividual(GEIndividual individual) throws URISyntaxException, IOException {
//		logger.debug("Assess " + individual.getPhenotype().getStringNoSpace() +  " ...");
		// compute fitness for the current child
		return Fitness.computeEntity(individual, this.generator);
	}

}
