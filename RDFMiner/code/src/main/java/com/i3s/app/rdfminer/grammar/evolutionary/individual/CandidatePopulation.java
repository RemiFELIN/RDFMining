package com.i3s.app.rdfminer.grammar.evolutionary.individual;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

import com.i3s.app.rdfminer.generator.Generator;
import org.apache.log4j.Logger;

import com.i3s.app.rdfminer.generator.axiom.RandomAxiomGenerator;

import Individuals.GEChromosome;
import Individuals.Phenotype;
import Individuals.Populations.SimplePopulation;
import Util.Random.MersenneTwisterFast;
import Util.Random.RandomNumberGenerator;

/**
 *  Map a set of candidate individual in population
 *  @author NGUYEN Thu Huong 
 *
*/
public class CandidatePopulation {

	private static final Logger logger = Logger.getLogger(CandidatePopulation.class.getName());

	protected int size;
	protected int generation;
	protected Generator generator;
	protected GEChromosome[] chromosomes;
	protected int maxWrapp;
	protected int maxvalCodon;
	protected int typeInitialization;
	protected int initlenChromosome;

	public CandidatePopulation(int size, Generator generator, int typeInitialization,
							   GEChromosome[] chromosomes, int initlenChromosome, int maxvalCodon, int maxWrapp) {
		// size of the population
		this.size = size;
		this.generator = generator;
		this.chromosomes = chromosomes;
		this.maxvalCodon = maxvalCodon;
		this.maxWrapp = maxWrapp;
		this.typeInitialization = typeInitialization;
		this.initlenChromosome = initlenChromosome;
	}

	/**
	 * Initialize a set of individuals 
	 * @param buffer a given file used as a buffer of individuals
	 * @param curGeneration the current generation
	 * @return a new candidate population
	 * @throws NumberFormatException
	 * @throws IndexOutOfBoundsException
	 * @throws IOException
	 */
	public ArrayList<GEIndividual> initialize(Reader buffer, int curGeneration)
			throws NumberFormatException, IndexOutOfBoundsException, IOException {
		GEChromosome chromosome;
		GEIndividual individual;
		// TODO developed only in the case of random initialization -
		// typeInitialization=1 ... Later need to develop other type of initialization.
		if (buffer != null) {
			int intch;
			StringBuilder st = new StringBuilder();
			char ch;
			int m = 0;
			ArrayList<Integer> chr = new ArrayList<Integer>();
			while ((intch = buffer.read()) != -1) {
				ch = (char) intch;
				if (ch != '\n') {
					if (ch != ',') {
						st.append(ch);
					} else {
						chr.add(Integer.parseInt(st.toString()));
						st = new StringBuilder();
					}
				} else {
					chromosome = new GEChromosome(chr.size());
					chromosome.setMaxCodonValue(maxvalCodon);
					chromosome.setMaxChromosomeLength(1000);
					for (Integer integer : chr) chromosome.add(integer);
					chromosomes[m] = chromosome;
					chr = new ArrayList<>();
					m++;
				}
			}
		} else {
			logger.info("Buffer file does not exists, initializing chromosomes ...");
			this.chromosomes = initializeChromosomes();
		}
		logger.info("Number of chromosomes created: " + chromosomes.length);
		ArrayList<GEIndividual> population = new ArrayList<GEIndividual>(size);

		int j = 0;
		while (j < size) {
			if (generator != null) {
				individual = generator.getIndividualFromChromosome(chromosomes[j], curGeneration);
				population.add(individual);
				Phenotype axiom = population.get(j).getPhenotype();
				if (axiom == null)
					break;
				j++;
			} else {
				logger.error("RandomAxiomGenerator is null");
				System.exit(0);
			}
		}
		logger.info("Number of individuals created: " + population.size());
		return population;
	}

	/**
	 * Renew a given population
	 * @param population a given population
	 * @param curGeneration the current generation
	 * @param etilismPopulation a etilism population
	 * @return a renewed population
	 */
	public ArrayList<GEIndividual> renew(ArrayList<GEIndividual> population, int curGeneration,
			ArrayList<GEIndividual> etilismPopulation) {
		ArrayList<GEIndividual> newPopulation = new ArrayList<>();
		if (etilismPopulation != null) {
			for (GEIndividual individual : etilismPopulation) {
				individual.setAge(curGeneration + 1);
				newPopulation.add(individual);
			}
		}
		for (GEIndividual individual : population) {
			individual.setAge(curGeneration + 1);
			newPopulation.add(individual);
		}
		return newPopulation;
	}

	//	public void addBestIndividuals(SimplePopulation etilismPopulation) {
//		int i = 0;
//		while (i < etilismPopulation.size()) {
//			chromosomes[i] = (GEChromosome) etilismPopulation.get(i).getGenotype().get(0);
//			i++;
//		}
//	}

	/**
	 * Initialize a set of random chromosomes
	 * @return an array of chromosomes
	 */
	public GEChromosome[] initializeChromosomes() {
		RandomNumberGenerator random;
		random = new MersenneTwisterFast(System.currentTimeMillis());
		GEChromosome chromosome;
		int maxLenChromosome = 1000;
		int n = 0;
		while (n < size) {
			chromosome = new GEChromosome(initlenChromosome);
			chromosome.setMaxCodonValue(maxvalCodon);
			chromosome.setMaxChromosomeLength(maxLenChromosome);
			for (int i = 0; i < initlenChromosome; i++) {
				// typeInitialization = 1
				chromosome.add(Math.abs(random.nextInt(maxvalCodon)));
			}
			chromosomes[n] = chromosome;
			n++;
		}
		return chromosomes;
	}

	/**
	 * generate a new axiom from a given chromosome
	 * @param chromosome a given {@link GEChromosome chromosome}
	 * @return a new {@link GEIndividual individual}
	 */
	public GEIndividual createNewAxiom(GEChromosome chromosome) {
		GEIndividual individual = new GEIndividual();
		RandomNumberGenerator random;
		random = new MersenneTwisterFast(System.currentTimeMillis());
		int maxLenChromosome = 1000;
		chromosome = new GEChromosome(initlenChromosome);
		chromosome.setMaxCodonValue(maxvalCodon);
		chromosome.setMaxChromosomeLength(maxLenChromosome);
		for (int i = 0; i < initlenChromosome; i++) {
			// typeInitialization = 1
			chromosome.add(Math.abs(random.nextInt(maxvalCodon)));
		}
		if (generator != null) {
			individual = generator.getIndividualFromChromosome(chromosome, generation);
		}
		return individual;
	}

	/**
	 * create a random chromosome
	 * @return a new {@link GEChromosome chromosome}
	 */
	public GEChromosome createChromosome() {
		RandomNumberGenerator random;
		random = new MersenneTwisterFast(System.currentTimeMillis());
		int maxLenChromosome = 1000;
		GEChromosome chromosome;
		chromosome = new GEChromosome(initlenChromosome);
		chromosome.setMaxCodonValue(maxvalCodon);
		chromosome.setMaxChromosomeLength(maxLenChromosome);
		for (int i = 0; i < initlenChromosome; i++) {
			// typeInitialization = 1
			chromosome.add(Math.abs(random.nextInt(maxvalCodon)));
		}
		return chromosome;
	}

}
