package com.i3s.app.rdfminer.evolutionary.individual;

import Individuals.GEChromosome;
import Individuals.Phenotype;
import Util.Random.MersenneTwisterFast;
import Util.Random.RandomNumberGenerator;
import com.i3s.app.rdfminer.RDFMiner;
import com.i3s.app.rdfminer.generator.Generator;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

/**
 *  Map a set of candidate individual in population
 *  @author NGUYEN Thu Huong 
 *
*/
public class CandidatePopulation {

	private static final Logger logger = Logger.getLogger(CandidatePopulation.class.getName());

//	protected int size;
	protected int generation;
	protected Generator generator;
	protected GEChromosome[] chromosomes;
//	protected int maxWrapp;
//	protected int maxvalCodon;
//	protected int typeInitialization;
//	protected int initlenChromosome;

	public CandidatePopulation(Generator generator) {
		// size of the population
//		this.size = size;
		this.generator = generator;
		this.chromosomes = new GEChromosome[RDFMiner.parameters.populationSize];
//		this.maxvalCodon = maxvalCodon;
//		this.maxWrapp = maxWrapp;
//		this.typeInitialization = typeInitialization;
//		this.initlenChromosome = initlenChromosome;
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
			ArrayList<Integer> chr = new ArrayList<>();
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
					chromosome.setMaxCodonValue(RDFMiner.parameters.maxValCodon);
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
//		for(GEChromosome chrom: this.chromosomes) {
//			System.out.println(chrom);
//		}
		ArrayList<GEIndividual> population = new ArrayList<>(RDFMiner.parameters.populationSize);

		int j = 0;
		while (j < RDFMiner.parameters.populationSize) {
			if (generator != null) {
				individual = generator.getIndividualFromChromosome(chromosomes[j], curGeneration);
//				System.out.println(individual.getPhenotype().getStringNoSpace() + " created !");
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
		while (n < RDFMiner.parameters.populationSize) {
			chromosome = new GEChromosome(RDFMiner.parameters.initLenChromosome);
			chromosome.setMaxCodonValue(RDFMiner.parameters.maxValCodon);
			chromosome.setMaxChromosomeLength(maxLenChromosome);
			for (int i = 0; i < RDFMiner.parameters.initLenChromosome; i++) {
				// typeInitialization = 1
				chromosome.add(Math.abs(random.nextInt(RDFMiner.parameters.maxValCodon)));
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
		chromosome = new GEChromosome(RDFMiner.parameters.initLenChromosome);
		chromosome.setMaxCodonValue(RDFMiner.parameters.maxValCodon);
		chromosome.setMaxChromosomeLength(maxLenChromosome);
		for (int i = 0; i < RDFMiner.parameters.initLenChromosome; i++) {
			// typeInitialization = 1
			chromosome.add(Math.abs(random.nextInt(RDFMiner.parameters.maxValCodon)));
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
		chromosome = new GEChromosome(RDFMiner.parameters.initLenChromosome);
		chromosome.setMaxCodonValue(RDFMiner.parameters.maxValCodon);
		chromosome.setMaxChromosomeLength(maxLenChromosome);
		for (int i = 0; i < RDFMiner.parameters.initLenChromosome; i++) {
			// typeInitialization = 1
			chromosome.add(Math.abs(random.nextInt(RDFMiner.parameters.maxValCodon)));
		}
		return chromosome;
	}

}