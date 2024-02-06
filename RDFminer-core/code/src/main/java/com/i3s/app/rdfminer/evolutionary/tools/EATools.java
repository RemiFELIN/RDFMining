package com.i3s.app.rdfminer.evolutionary.tools;

import com.i3s.app.rdfminer.entity.Entity;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.FitnessPackage.BasicFitness;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.GEChromosome;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.GEIndividual;
import com.i3s.app.rdfminer.generator.Generator;
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
	public static ArrayList<Entity> getDistinctGenotypesPopulation(ArrayList<Entity> canPop) {
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

	public static ArrayList<Entity> bindIndividualsWithEntities(ArrayList<GEIndividual> individuals, ArrayList<Entity> entities) {
		ArrayList<Entity> newEntities = new ArrayList<>();
//		logger.debug("bindIndividualsWithEntities: replacementSize(" + individuals.size() + ")");
		for(GEIndividual individual : individuals) {
			for(Entity entity : entities) {
				if(Objects.equals(individual.getGenotype().toString(), entity.individual.getGenotype().toString())) {
					newEntities.add(entity);
					break;
				}
			}
		}
		logger.info(newEntities.size() + " individuals has been binded with existing entities...");
		return newEntities;
	}

	public static ArrayList<Entity> getTimeCappedEntities(ArrayList<Entity> entities, List<Entity> evaluatedEntities) {
		ArrayList<Entity> notEvaluated = new ArrayList<>(entities);
		for(Entity evaluated : evaluatedEntities) {
			for(Entity entity : entities) {
				if(evaluated.individual.getPhenotype() == entity.individual.getPhenotype()) {
//					logger.debug("remove " + entity.individual.getPhenotype().getStringNoSpace() + " ...");
					notEvaluated.remove(entity);
					break;
				}
			}
		}
		// for each not evaluated entities, we will set their fitness to 0
		logger.debug(notEvaluated.size() + " individual(s) have not been evaluated because of time-cap limit");
		logger.debug("Their fitness score are equal to 0");
		for(Entity entity : notEvaluated) {
//			logger.warn(entity.individual.getPhenotype().getStringNoSpace() + " will be considered as a candidate to reject");
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

	public static boolean compareIndividuals(GEIndividual parent, GEIndividual offspring) {
		return Objects.equals(parent.getGenotype().get(0).toString(), offspring.getGenotype().get(0).toString());
	}


	public static ArrayList<GEIndividual> getCoupleInPopulation(ArrayList<GEIndividual> population, Generator generator) {
		Random rand = new Random();
		ArrayList<GEIndividual> couple = new ArrayList<>();
		int firstIdx = rand.nextInt(population.size());
		int secondIdx = firstIdx;
		while (secondIdx == firstIdx) {
			secondIdx = rand.nextInt(population.size());
		}
		// rebuild individuals to apply operators on it without any side effects
		GEChromosome chromParent1 = population.get(firstIdx).getChromosomes();
		GEChromosome chromParent2 = population.get(secondIdx).getChromosomes();
		couple.add(generator.getIndividualFromChromosome(chromParent1));
		couple.add(generator.getIndividualFromChromosome(chromParent2));
		return couple;
	}

}
