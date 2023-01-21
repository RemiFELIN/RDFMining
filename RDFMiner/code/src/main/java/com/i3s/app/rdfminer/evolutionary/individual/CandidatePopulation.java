package com.i3s.app.rdfminer.evolutionary.individual;

import Individuals.GEChromosome;
import Util.Random.MersenneTwisterFast;
import Util.Random.RandomNumberGenerator;
import com.i3s.app.rdfminer.RDFMiner;
import com.i3s.app.rdfminer.generator.Generator;
import com.i3s.app.rdfminer.output.Cache;
import org.apache.log4j.Logger;

import java.util.ArrayList;

/**
 *  Map a set of candidate individual in population
 *  @author NGUYEN Thu Huong 
 *
*/
public class CandidatePopulation {

	private static final Logger logger = Logger.getLogger(CandidatePopulation.class.getName());

	protected int generation;
	protected Generator generator;
	protected ArrayList<GEChromosome> chromosomes;

	public CandidatePopulation(Generator generator) {
		this.generator = generator;
		this.chromosomes = new ArrayList<>(RDFMiner.parameters.populationSize);
	}

	/**
	 * Initialize a set of individuals 
	 * @param cache a given file used as a cache file
	 * @param curGeneration the current generation
	 * @return a new candidate population
	 */
	public ArrayList<GEIndividual> initialize(Cache cache, int curGeneration)
			throws NumberFormatException, IndexOutOfBoundsException {
		GEChromosome chromosome;
		GEIndividual individual;
		// TODO developed only in the case of random initialization -
		// typeInitialization=1 ... Later need to develop other type of initialization.
		if (cache != null) {
			// for each chromosomes, we will set an GEInidividual
			for(String genotype : cache.genotypes) {
				chromosome = new GEChromosome(cache.lenChromosomes);
				chromosome.setMaxCodonValue(RDFMiner.parameters.maxValCodon);
				chromosome.setMaxChromosomeLength(1000);
				for(String integer : genotype.split(",")) chromosome.add(Integer.parseInt(integer));
				chromosomes.add(chromosome);
			}
		} else {
			logger.info("Buffer file does not exists, initializing chromosomes ...");
			this.chromosomes = initializeChromosomes();
		}
		logger.info("Number of chromosomes created: " + chromosomes.size());
		ArrayList<GEIndividual> population = new ArrayList<>(RDFMiner.parameters.populationSize);

		for(GEChromosome chrom : chromosomes) {
			if (generator != null) {
				individual = generator.getIndividualFromChromosome(chrom, curGeneration);
				population.add(individual);
			} else {
				logger.error("Generator is null");
				System.exit(0);
			}
		}
		logger.info("Number of individuals created: " + population.size());
		return population;
	}

	/**
	 * Initialize a set of random chromosomes
	 * @return an array of chromosomes
	 */
	public ArrayList<GEChromosome> initializeChromosomes() {
		RandomNumberGenerator random;
		random = new MersenneTwisterFast(System.currentTimeMillis());
		GEChromosome chromosome;
		int maxLenChromosome = 1000;
		int n = 0;
		while (n < RDFMiner.parameters.populationSize) {
			chromosome = new GEChromosome(RDFMiner.parameters.initLenChromosome);
			chromosome.setMaxCodonValue(RDFMiner.parameters.maxValCodon);
			chromosome.setMaxChromosomeLength(maxLenChromosome);
			for (int i = 0; i < RDFMiner.parameters.initLenChromosome; i++) {
				// typeInitialization = 1
				chromosome.add(Math.abs(random.nextInt(RDFMiner.parameters.maxValCodon)));
			}
			chromosomes.add(chromosome);
			n++;
		}
		return chromosomes;
	}

}
