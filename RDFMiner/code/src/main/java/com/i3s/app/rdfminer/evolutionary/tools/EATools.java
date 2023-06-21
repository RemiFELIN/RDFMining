package com.i3s.app.rdfminer.evolutionary.tools;

import com.i3s.app.rdfminer.entity.Entity;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.FitnessPackage.BasicFitness;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.GEChromosome;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.GEIndividual;
import com.i3s.app.rdfminer.grammar.DLFactory;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * This class is used to deployed all EA tools like crossover, mutation, ...
 * 
 * @author Thu Huong NGUYEN & Rémi FELIN
 */
public class EATools {

	private static final Logger logger = Logger.getLogger(EATools.class.getName());

	/**
	 * delete twins from a given array of {@link GEChromosome chromosomes}
	 * 
	 * @param chromosomes a given array
	 */
	public static void deleteTwins(GEChromosome[] chromosomes, int n) {
		// Let's go to the same phantom
		for (int i = 0; i < n - 1; i++) {
			for (int k = i + 1; k < n; k++) {
				if (chromosomes[k] == chromosomes[i]) {
					if (n - 1 - k >= 0) System.arraycopy(chromosomes, k + 1, chromosomes, k, n - 1 - k);
					n--;
					k--;
				}
			}
		}
	}

	/**
	 * Remove the duplicate(s) genotype(s) from a given list and returns the
	 * filtered list
	 * @param canPop a given list to be filtered
	 * @return the filtered list
	 */
	public static ArrayList<Entity> getDistinctGenotypePopulation(ArrayList<Entity> canPop) {
		ArrayList<Entity> entities = new ArrayList<>();
		Set<String> genotypes = new HashSet<>();
		for (Entity entity : canPop) {
//			System.out.println("genotype: " + entity.individual.getGenotype());
			if (genotypes.add(entity.individual.getGenotype().toString())) {
				entities.add(entity);
			}
		}
		return entities;
	}

	public static double getPopulationDevelopmentRate(ArrayList<Entity> originalPopulation, ArrayList<Entity> newPopulation) {
		ArrayList<String> phenotypes = new ArrayList<>();
		for (Entity entity : originalPopulation) {
			phenotypes.add(entity.individual.getPhenotype().getStringNoSpace());
		}
		int count = 0;
		for (Entity entity : newPopulation) {
			if (!phenotypes.contains(entity.individual.getPhenotype().getStringNoSpace())) {
				count++;
			}
		}
		return (double) count / newPopulation.size();
	}

	/**
	 * Renew a given axioms population
	 * @param entities a given population
	 * @param curGeneration the current generation
	 * @param elitismEntities an elitism population
	 * @return a renewed population
	 */
	public static ArrayList<Entity> renew(int curGeneration, ArrayList<Entity> entities, ArrayList<Entity> elitismEntities) {
		ArrayList<Entity> newEntities = new ArrayList<>();
		// add elitism entities
		if (elitismEntities != null) {
			for (Entity etilism : elitismEntities) {
				// set generation
				if (etilism.generation == null) {
					etilism.generation = curGeneration;
				}
				etilism.individual.setAge(etilism.generation);
				newEntities.add(etilism);
			}
		}
		// add others entities
		for (Entity entity : entities) {
			// set generation
			if (entity.generation == null) {
				entity.generation = curGeneration;
			}
			entity.individual.setAge(entity.generation);
			newEntities.add(entity);
		}
		return newEntities;
	}

	public static ArrayList<Entity> bindIndividualsWithEntities(ArrayList<GEIndividual> individuals, ArrayList<Entity> entities) {
		ArrayList<Entity> newEntities = new ArrayList<>();
		for(GEIndividual individual : individuals) {
//			logger.debug("# individual: " + individual);
			for(Entity entity : entities) {
//				logger.debug("### Entity: " + entity.individual);
				if(Objects.equals(individual.getPhenotype().getStringNoSpace(), entity.individual.getPhenotype().getStringNoSpace())) {
					newEntities.add(entity);
					break;
				}
			}
		}
		// Special case with duplicates individuals due to their fitness equal to 0
		// common case at the beginning of GE.
		// Since they are "bad" candidates, we just remove last entities from new entities to return
		// in order to do not unbalance the not selected candidates population
//		int notSelectedPopSize = (int) ( (1 - RDFMiner.parameters.sizeSelection) * RDFMiner.parameters.populationSize);
//		int i = 0;
//		while(newEntities.size() > notSelectedPopSize) {
//			newEntities.remove(newEntities.size() - 1);
//			i++;
//		}
//		if (i != 0)
//			logger.debug(i + " entities has been removed due to duplicates individuals into selected population");
		return newEntities;
	}

	public static ArrayList<Entity> getTimeCappedEntities(ArrayList<Entity> oldEntities, List<Entity> evaluatedEntities) {
		ArrayList<Entity> notEvaluated = new ArrayList<>(oldEntities);
		for(Entity evaluated : evaluatedEntities) {
			for(Entity entity : oldEntities) {
				if(evaluated == entity) {
//					logger.debug("remove " + entity.individual.getPhenotype().getStringNoSpace() + " ...");
					notEvaluated.remove(entity);
					break;
				}
			}
		}
		// for each not evaluated entities, we will set their fitness to 0
		for(Entity entity : notEvaluated) {
			logger.warn(entity.individual.getPhenotype().getStringNoSpace() + " will be considered as a candidate to reject");
			// set fitness
			entity.individual.setFitness(new BasicFitness(0, entity.individual));
		}
		return notEvaluated;
	}

	public static ArrayList<Entity> getTimeCappedIndividuals(ArrayList<GEIndividual> individuals, List<Entity> evaluatedEntities) {
		ArrayList<GEIndividual> notEvaluated = new ArrayList<>(individuals);
		ArrayList<Entity> toReturn = new ArrayList<>();
		for(Entity evaluated : evaluatedEntities) {
			for(GEIndividual individual : individuals) {
				if(evaluated.individual == individual) {
//					logger.debug("remove " + individual.getPhenotype().getStringNoSpace() + " ...");
					notEvaluated.remove(individual);
					break;
				}
			}
		}
		// for each not evaluated individuals, we will set an Entity with their individual with a fitness = 0
		for(GEIndividual notEval : notEvaluated) {
			logger.warn(notEval.getPhenotype().getStringNoSpace() + " will be considered as a candidate to reject");
			Entity entity = new Entity();
			// for SubClassOf axioms, we will set sub and super class
			entity.argumentClasses = DLFactory.parseArguments(notEval.getPhenotype());
			entity.setIndividual(notEval);
			// set fitness
			entity.individual.setFitness(new BasicFitness(0, entity.individual));
			toReturn.add(entity);
		}
		return toReturn;
	}

}
