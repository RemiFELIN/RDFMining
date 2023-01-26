package com.i3s.app.rdfminer.evolutionary.tools;

import Individuals.FitnessPackage.BasicFitness;
import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.entity.Entity;
import com.i3s.app.rdfminer.evolutionary.fitness.novelty.NoveltySearch;
import com.i3s.app.rdfminer.evolutionary.fitness.novelty.Similarity;
import com.i3s.app.rdfminer.generator.Generator;
import com.i3s.app.rdfminer.evolutionary.fitness.Fitness;
import com.i3s.app.rdfminer.evolutionary.individual.GEIndividual;
import com.i3s.app.rdfminer.sparql.corese.CoreseEndpoint;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Objects;

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
		logger.debug("# parent 1: " + parent1.individual.getPhenotype().getStringNoSpace());
		if(Objects.equals(child1.getGenotype().toString(), parent1.individual.getGenotype().toString())) {
			logger.debug("No differences observed between the parent and its child ...");
			this.child1 = this.parent1;
		} else {
			this.child1 = getEntityFromIndividual(child1);
		}
		logger.debug("# parent 2: " + parent2.individual.getPhenotype().getStringNoSpace());
		if(Objects.equals(child2.getGenotype().toString(), parent2.individual.getGenotype().toString())) {
			logger.debug("No differences observed between the parent and its child ...");
			this.child2 = this.parent2;
		} else {
			this.child2 = getEntityFromIndividual(child2);
		}
		// compute similarities between individuals
		this.similarityP1ToC1 = this.distance(this.parent1, this.child1);
		this.similarityP2ToC2 = this.distance(this.parent2, this.child2);
		this.similarityP1ToC2 = this.distance(this.parent1, this.child2);
		this.similarityP2ToC1 = this.distance(this.parent2, this.child1);
	}

	public ArrayList<Entity> getSurvivalSelection() throws URISyntaxException, IOException {
		ArrayList<Entity> survivals = new ArrayList<>();
		// for each couple, we will minimize the similarities betweem them
		if (similarityP1ToC1 <= similarityP1ToC2) survivals.add(compare(parent1, child1));
		else survivals.add(compare(parent1, child2));
		if (similarityP2ToC1 <= similarityP2ToC2) survivals.add(compare(parent2, child1));
		else survivals.add(compare(parent2, child2));
		logger.debug("Survival selection done !");
		return survivals;
	}

	public double distance(Entity a, Entity b) throws URISyntaxException, IOException {
		return Similarity.getJaccardSimilarity(new CoreseEndpoint(Global.CORESE_IP, Global.TRAINING_SPARQL_ENDPOINT, Global.PREFIXES),
				a, b);
	}

	public Entity compare(Entity parent, Entity child) throws URISyntaxException, IOException {
		// if the parent is not evaluated
		if (parent.individual.getFitness() == null) {
			logger.warn("Compute parent fitness !");
			parent = Fitness.computeEntity(parent.individual, this.generator);
		}
		// if the child is not evaluated
		if (child.individual.getFitness() == null) {
			logger.warn("Compute child fitness !");
			child = Fitness.computeEntity(child.individual, this.generator);
		}
		// we can compare parent and child
//		logger.info("update fitness of parent and child");
		parent.fitness = NoveltySearch.updateFitness(parent);
		child.fitness = NoveltySearch.updateFitness(child);
		parent.individual.setFitness(new BasicFitness(parent.fitness, parent.individual));
		child.individual.setFitness(new BasicFitness(child.fitness, child.individual));
		// compare their fitness
		if(Objects.equals(child1.individual.getGenotype().toString(), parent1.individual.getGenotype().toString())) {
			logger.debug("Nothing to compare ...");
			return parent;
		} else if (parent.individual.getFitness().getDouble() <= child.individual.getFitness().getDouble()) {
			logger.debug("child " + child.individual.getPhenotype().getStringNoSpace() + " is choosen instead of " +
					parent.individual.getPhenotype().getStringNoSpace());
			return child;
		} else {
			logger.debug("keep the parent " + parent.individual.getPhenotype().getStringNoSpace() + " instead of " +
					child.individual.getPhenotype().getStringNoSpace());
			return parent;
		}
	}

	private Entity getEntityFromIndividual(GEIndividual individual) throws URISyntaxException, IOException {
		logger.debug("Assess " + individual.getPhenotype().getStringNoSpace() +  " ...");
		// compute fitness for the current child
		return Fitness.computeEntity(individual, this.generator);
	}

}
