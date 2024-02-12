package com.i3s.app.rdfminer.evolutionary.individual;

import com.i3s.app.rdfminer.Parameters;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.GEChromosome;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.GEIndividual;
import com.i3s.app.rdfminer.evolutionary.geva.Util.Random.MersenneTwisterFast;
import com.i3s.app.rdfminer.evolutionary.geva.Util.Random.RandomNumberGenerator;
import com.i3s.app.rdfminer.generator.Generator;
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
		Parameters parameters = Parameters.getInstance();
		this.generator = generator;
		this.chromosomes = new ArrayList<>(parameters.getPopulationSize());
	}

	/**
	 * Initialize a set of individuals
	 * @return a new candidate population
	 */
	public ArrayList<GEIndividual> initialize()
			throws NumberFormatException, IndexOutOfBoundsException {
		Parameters parameters = Parameters.getInstance();
		GEChromosome chromosome;
		GEIndividual individual;
		ArrayList<Double> fitnessList = new ArrayList<>(parameters.getPopulationSize());
		// TODO developed only in the case of random initialization -
		// typeInitialization=1 ... Later need to develop other type of initialization.
//		if (cache != null) {
//			int i = 0;
//			// for each individuals, we will set an GEInidividual
//			for(JSONObject ind : cache.individualsJSON) {
//				chromosome = new GEChromosome(cache.lenChromosomes);
//				chromosome.setMaxCodonValue(parameters.maxValCodon);
//				chromosome.setMaxChromosomeLength(1000);
//				for(String integer : ind.getString(IndividualJSON.GENOTYPE).split(",")) {
//					chromosome.add(Integer.parseInt(integer));
//				}
//				chromosomes.add(i, chromosome);
//				fitnessList.add(i, ind.getDouble(IndividualJSON.FITNESS));
//				i++;
//			}
//		} else {
//			logger.info("Buffer file does not exists, initializing chromosomes ...");
		this.chromosomes = initializeChromosomes();
//		}
		logger.info("Number of chromosomes created: " + chromosomes.size());
		ArrayList<GEIndividual> population = new ArrayList<>(parameters.getPopulationSize());
		for (GEChromosome geChromosome : chromosomes) {
			if (generator != null) {
				// init individual
				individual = generator.getIndividualFromChromosome(geChromosome);
				// set its fitness
//				if(cache != null) {
//					individual.setFitness(new BasicFitness(fitnessList.get(i), individual));
//				}
				population.add(individual);
			} else {
				logger.error("Generator is null ...");
				// System.exit(0);
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
		Parameters parameters = Parameters.getInstance();
		RandomNumberGenerator random;
		random = new MersenneTwisterFast(System.currentTimeMillis());
		GEChromosome chromosome;
		int maxLenChromosome = 1000;
		int n = 0;
		while (n < parameters.getPopulationSize()) {
			chromosome = new GEChromosome(parameters.getSizeChromosome());
			chromosome.setMaxCodonValue(parameters.maxValCodon);
			chromosome.setMaxChromosomeLength(maxLenChromosome);
			for (int i = 0; i < parameters.getSizeChromosome(); i++) {
				// typeInitialization = 1
				chromosome.add(Math.abs(random.nextInt(parameters.maxValCodon)));
			}
			chromosomes.add(chromosome);
			n++;
		}
		return chromosomes;
	}

}
