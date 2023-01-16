package com.i3s.app.rdfminer.evolutionary.tools;

import Individuals.GEChromosome;
import Individuals.Genotype;
import Individuals.Phenotype;
import Individuals.Populations.Population;
import Operator.Operations.TournamentSelect;
import Util.Random.MersenneTwisterFast;
import Util.Random.RandomNumberGenerator;
import com.i3s.app.rdfminer.RDFMiner;
import com.i3s.app.rdfminer.entity.Entity;
import com.i3s.app.rdfminer.entity.shacl.Shape;
import com.i3s.app.rdfminer.evolutionary.individual.GEIndividual;
import com.i3s.app.rdfminer.evolutionary.selection.ProportionalRouletteWheel;
import com.i3s.app.rdfminer.evolutionary.selection.TruncationSelection;
import com.i3s.app.rdfminer.evolutionary.selection.TypeSelection;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

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
