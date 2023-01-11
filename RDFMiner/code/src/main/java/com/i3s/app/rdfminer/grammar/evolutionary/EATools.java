package com.i3s.app.rdfminer.grammar.evolutionary;

import Individuals.GEChromosome;
import Individuals.Genotype;
import Individuals.Phenotype;
import Individuals.Populations.Population;
import Operator.Operations.TournamentSelect;
import Util.Random.MersenneTwisterFast;
import Util.Random.RandomNumberGenerator;
import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.RDFMiner;
import com.i3s.app.rdfminer.entity.Entity;
import com.i3s.app.rdfminer.entity.shacl.Shape;
import com.i3s.app.rdfminer.generator.Generator;
import com.i3s.app.rdfminer.grammar.evolutionary.crossover.SinglePointCrossoverAxiom;
import com.i3s.app.rdfminer.grammar.evolutionary.crossover.SubtreeCrossoverAxioms;
import com.i3s.app.rdfminer.grammar.evolutionary.crossover.TwoPointCrossover;
import com.i3s.app.rdfminer.grammar.evolutionary.crossover.TypeCrossover;
import com.i3s.app.rdfminer.grammar.evolutionary.individual.GEIndividual;
import com.i3s.app.rdfminer.grammar.evolutionary.mutation.IntFlipMutation;
import com.i3s.app.rdfminer.grammar.evolutionary.selection.ProportionalRouletteWheel;
import com.i3s.app.rdfminer.grammar.evolutionary.selection.TruncationSelection;
import com.i3s.app.rdfminer.grammar.evolutionary.selection.TypeSelection;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

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
	 * Remove the duplicate(s) phenotype(s) from a given list and returns the
	 * filtered list
	 * 
	 * @param canPop a given list to be filtered
	 * @return the filtered list
	 */
	public static ArrayList<GEIndividual> getDistinctPhenotypePopulationFromAxioms(ArrayList<GEIndividual> canPop) {
		ArrayList<GEIndividual> individuals = new ArrayList<>();
		Set<Phenotype> phenotypes = new HashSet<>();
		for (GEIndividual item : canPop) {
			if (phenotypes.add(item.getPhenotype())) {
				individuals.add(item);
			}
		}
		return individuals;
	}

	/**
	 * Remove the duplicate(s) genotype(s) from a given list and returns the
	 * filtered list
	 * @param canPop a given list to be filtered
	 * @return the filtered list
	 */
	public static ArrayList<Entity> getDistinctGenotypePopulation(ArrayList<Entity> canPop) {
		ArrayList<Entity> entities = new ArrayList<>();
		Set<Genotype> genotypes = new HashSet<>();
		for (Entity entity : canPop) {
			if (genotypes.add(entity.individual.getGenotype())) {
				entities.add(entity);
			}
		}
		return entities;
	}

	public static ArrayList<Shape> getDistinctGenotypePopulationFromShapes(ArrayList<Shape> shapes) {
		ArrayList<Shape> distinctShapes = new ArrayList<>();
		Set<Genotype> genotypes = new HashSet<>();
		for (Shape shape : shapes) {
			if (genotypes.add(shape.individual.getGenotype())) {
				distinctShapes.add(shape);
			}
		}
		return distinctShapes;
	}


	public static ArrayList<GEIndividual> getTypeSelection(int type, ArrayList<GEIndividual> selectedPopulation, int sizeElite, int sizeSelection) {
		switch (type) {
			case TypeSelection.ROULETTE_WHEEL:
				// Roulette wheel method
				// if6
				logger.info("Type selection: Roulette Wheel");
				return EATools.rouletteWheel(selectedPopulation);
			case TypeSelection.TRUNCATION:
				// Truncation selection method
				// if7
				logger.info("Type selection: Truncation");
				TruncationSelection truncation = new TruncationSelection(sizeSelection);
				truncation.setParentsSelectionElitism(selectedPopulation);
				return truncation.setupSelectedPopulation(RDFMiner.parameters.populationSize - sizeElite);
			case TypeSelection.TOURNAMENT:
				// Tournament method
				// if8
				logger.info("Type selection: Tournament");
				return EATools.tournament(selectedPopulation);
			default:
				// Normal crossover way - All individual of the current generation
				// will be selected for crossover operation to create the new
				// population
				// if6
				logger.info("Type selection: Normal");
				return null;
		}
	}

	/**
	 * To compute all tasks about crossover, mutation and evaluation phasis of
	 * genetical algorithm
	 * 
	 * @param canEntities        the candidate population
	 * @param curGeneration the current generation
	 * @param generator     an instance of {@link Generator Generator}
	 * @return a new population
	 */
	public static ArrayList<Entity> computeGeneration(ArrayList<Entity> canEntities, int curGeneration, Generator generator)
			throws InterruptedException, ExecutionException {

		ArrayList<Entity> evaluatedIndividuals = new ArrayList<>();
		// We have a set of threads to compute each tasks
		ExecutorService executor = Executors.newFixedThreadPool(Global.NB_THREADS);
		Set<Callable<ArrayList<Entity>>> entitiesCallables = new HashSet<>();
		logger.info("The entities will be evaluated using the following SPARQL Endpoint : " + Global.TRAINING_SPARQL_ENDPOINT);
		logger.info("Performing crossover and mutation ...");
//		List<Crowding> shapesToEvaluate = new ArrayList<>();
		int m = 0;

		while (m <= canEntities.size() - 2) {
			RandomNumberGenerator rand = new MersenneTwisterFast();
			// get the two individuals which are neighbours
			GEIndividual parent1 = canEntities.get(m).individual;
			GEIndividual parent2 = canEntities.get(m + 1).individual;
			GEIndividual child1, child2;
			GEChromosome[] chromosomes;
			/* CROSSOVER PHASIS */
			switch (RDFMiner.parameters.typeCrossover) {
				case TypeCrossover.SINGLE_POINT_CROSSOVER:
					// Single-point crossover
					SinglePointCrossoverAxiom spc = new SinglePointCrossoverAxiom(RDFMiner.parameters.proCrossover,
							rand, generator, curGeneration);
					spc.setFixedCrossoverPoint(true);
					child1 = parent1;
					child2 = parent2;
					GEIndividual[] childs = spc.doOperation(child1, child2);
					child1 = childs[0];
					child2 = childs[1];
					logger.info("---");
					break;
				case TypeCrossover.SUBTREE_CROSSOVER:
					// subtree crossover
					SubtreeCrossoverAxioms sca = new SubtreeCrossoverAxioms(RDFMiner.parameters.proCrossover, rand);
					GEIndividual[] inds = sca.crossoverTree(parent1, parent2);
					child1 = inds[0];
					child2 = inds[1];
					break;
				default:
					// Two point crossover
					TwoPointCrossover tpc = new TwoPointCrossover(RDFMiner.parameters.proCrossover, rand);
					tpc.setFixedCrossoverPoint(true);
					chromosomes = tpc.crossover(
							new GEChromosome((GEChromosome) parent1.getGenotype().get(0)),
							new GEChromosome((GEChromosome) parent2.getGenotype().get(0))
					);
					child1 = generator.getIndividualFromChromosome(chromosomes[0], curGeneration);
					child2 = generator.getIndividualFromChromosome(chromosomes[1], curGeneration);
					break;
			}

			/* MUTATION PHASIS */
//			RandomNumberGenerator rand1 = new MersenneTwisterFast();
			IntFlipMutation mutation = new IntFlipMutation(RDFMiner.parameters.proMutation, new MersenneTwisterFast());
			// make mutation and return new childs from it
			GEIndividual newChild1 = mutation.doOperation(child1, generator, curGeneration, child1.getMutationPoints());
			GEIndividual newChild2 = mutation.doOperation(child2, generator, curGeneration, child2.getMutationPoints());
			// if using crowding method in survival selection
			if (RDFMiner.parameters.diversity == 1) {
				// if crowding is chosen, we need to compute and return the individuals chosen
				// (between parents and childs) in function of their fitness
//				logger.info("CROWDING diversity method used ...");
				// fill callables of crowding to compute
				final int idx = m;
				entitiesCallables.add(() -> new Crowding(canEntities.get(idx), canEntities.get(idx + 1), newChild1, newChild2, generator)
						.getSurvivalSelection());
			}
			m = m + 2;
		}
		logger.info("Crossover & Mutation done");
		logger.info(entitiesCallables.size() + " tasks ready to be launched !");
		// Submit tasks
		List<Future<ArrayList<Entity>>> futures = executor.invokeAll(entitiesCallables);
		// fill the evaluated individuals
		for (Future<ArrayList<Entity>> future : futures) {
			evaluatedIndividuals.addAll(future.get());
		}
		// Shutdown the service
		executor.shutdown();
		executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		// return the modified individuals
		return evaluatedIndividuals;
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
				etilism.individual.setAge(curGeneration);
				newEntities.add(etilism);
			}
		}
		// add others entities
		for (Entity entity : entities) {
			// set generation
			if (entity.generation == null) {
				entity.generation = curGeneration;
			}
			entity.individual.setAge(curGeneration);
			newEntities.add(entity);
		}
		return newEntities;
	}

	public static ArrayList<Entity> bindIndividualsWithEntities(ArrayList<GEIndividual> individuals, ArrayList<Entity> entities) {
		ArrayList<Entity> newEntities = new ArrayList<>();
		for(GEIndividual individual : individuals) {
			for(Entity entity : entities) {
				if(individual.getGenotype() == entity.individual.getGenotype()) {
					newEntities.add(entity);
				}
			}
		}
		return newEntities;
	}

	public static ArrayList<GEIndividual> rouletteWheel(ArrayList<GEIndividual> selectedPopulation) {
		// RouletteWheel
		int size = selectedPopulation.size();
		RandomNumberGenerator random;
		random = new MersenneTwisterFast(System.currentTimeMillis());
		ProportionalRouletteWheel rl = new ProportionalRouletteWheel(size, random);
		rl.doOperation(((Population) selectedPopulation).getAll());
		return new ArrayList<>(selectedPopulation);
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<GEIndividual> tournament(ArrayList<GEIndividual> selectedPopulation) {
		// Tournament
		int size = selectedPopulation.size();
		RandomNumberGenerator random;
		random = new MersenneTwisterFast(System.currentTimeMillis());
		TournamentSelect tn = new TournamentSelect(size, size / 10, random);
		tn.selectFromTour();
		return (ArrayList<GEIndividual>) tn.getSelectedPopulation();
	}

}
