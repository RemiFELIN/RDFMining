package com.i3s.app.rdfminer.grammar.evolutionary.individual;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.i3s.app.rdfminer.axiom.RandomAxiomGenerator;

import Individuals.Chromosome;
import Individuals.GEChromosome;
//import Individuals.GEIndividual;
import Individuals.Genotype;
//import Individuals.Individual;
import Individuals.Phenotype;
import Individuals.Populations.SimplePopulation;
import Util.Random.MersenneTwisterFast;
import Util.Random.RandomNumberGenerator;

/*
 * 
 *  @author NGUYEN Thu Huong 
 *
 *
 *
*/
public class CandidatePopulation {

	private static Logger logger = Logger.getLogger(CandidatePopulation.class.getName());

	protected int size;
	protected int generation;
	protected RandomAxiomGenerator generator;
	protected GEChromosome[] chromosomes;
	protected int maxlenChromosome;
	protected int maxWrapp;
	protected int maxvalCodon;
	protected int typeInitialization;
	protected int initlenChromosome;

	public CandidatePopulation(int size, RandomAxiomGenerator generator, int typeInitialization,
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

	public ArrayList<GEIndividual> initialize(Reader buffer, int curGeneration)
			throws NumberFormatException, IndexOutOfBoundsException, IOException {
		GEChromosome chromosome;
		GEIndividual individual;
		// RandomNumberGenerator random;
	    // TODO developed only in the case of random initialization - typeInitialization=1 ... Later need to develop other type of initialization.
		if (buffer != null) {
			int intch;
			String st = "";
			char ch;
			int m = 0;
			ArrayList<Integer> chr = new ArrayList<Integer>();
			while ((intch = buffer.read()) != -1) {
				ch = (char) intch;
				if (ch != '\n') {
					if (ch != ',') {
						st += ch;
					} else {
						chr.add(Integer.parseInt(st));
						st = "";
					}
				} else {
					chromosome = new GEChromosome(chr.size());
					chromosome.setMaxCodonValue(maxvalCodon);
					chromosome.setMaxChromosomeLength(1000);
					for (int i = 0; i < chr.size(); i++)
						chromosome.add(chr.get(i));
					chromosomes[m] = chromosome;
					chr = new ArrayList<Integer>();
					m++;
				}
			}
		}
		else {
			this.chromosomes = initializeListChromosome();
		}
		logger.info("number chromosome created: " + chromosomes.length);
		ArrayList<GEIndividual> CandidatePopulation = new ArrayList<GEIndividual>(size);

		int j = 0;
		while (j < size) {
			if (generator != null) {
				individual = generator.axiomIndividual(chromosomes[j], curGeneration);
				CandidatePopulation.add(individual);
				Phenotype axiom = CandidatePopulation.get(j).getPhenotype();
				Genotype gp = CandidatePopulation.get(j).getGenotype();
				Chromosome chro = gp.get(0);
				if (axiom == null)
					break;
				logger.info("Generation: " + CandidatePopulation.get(j).getAge());
				logger.info("Individual: " + CandidatePopulation.get(j));
				logger.info("Chromosome: " + chro);
				logger.info("Used wraps: " + individual.getUsedWraps());
				logger.info("------------------------------------------------------------------------------");
				j++;
			}
		}
		logger.info("number of individual created: " + CandidatePopulation.size());
		return CandidatePopulation;
	}

	public GEChromosome[] getListChromosome() {
		return chromosomes;
	}

	public ArrayList<GEIndividual> renew(ArrayList<GEIndividual> listCrossover, int curGeneration,
			ArrayList<GEIndividual> etilismPopulation) {
		ArrayList<GEIndividual> newPopulation = new ArrayList<GEIndividual>();
		if (etilismPopulation != null) {
			for (int i1 = 0; i1 < etilismPopulation.size(); i1++) {
				etilismPopulation.get(i1).setAge(curGeneration + 1);
				newPopulation.add(etilismPopulation.get(i1));
			}
		}
		for (int i2 = 0; i2 < listCrossover.size(); i2++) {
			listCrossover.get(i2).setAge(curGeneration + 1);
			newPopulation.add(listCrossover.get(i2));
		}
		return newPopulation;
	}

	public void addBestIndividual(SimplePopulation etilismPopulation) {
		int i = 0;
		while (i < etilismPopulation.size()) {
			chromosomes[i] = (GEChromosome) etilismPopulation.get(i).getGenotype().get(0);
			i++;
		}
	}

	public GEChromosome[] initializeListChromosome() {
		RandomNumberGenerator random;
		random = new MersenneTwisterFast(System.currentTimeMillis() & 0xFFFFFFFF);
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
			logger.info(chromosome);
			n++;
		}
		return chromosomes;
	}

	public GEIndividual createNewAxiom(GEChromosome chromosome) {
		GEIndividual individual = new GEIndividual();
		RandomNumberGenerator random;
		random = new MersenneTwisterFast(System.currentTimeMillis() & 0xFFFFFFFF);
		int maxLenChromosome = 1000;
		chromosome = new GEChromosome(initlenChromosome);
		chromosome.setMaxCodonValue(maxvalCodon);
		chromosome.setMaxChromosomeLength(maxLenChromosome);
		for (int i = 0; i < initlenChromosome; i++) {
			// typeInitialization = 1
			chromosome.add(Math.abs(random.nextInt(maxvalCodon)));
		}
		if (generator != null) {
			individual = generator.axiomIndividual(chromosome, generation);
		}
		return individual;
	}

	public GEChromosome CreateChromosome() {
		RandomNumberGenerator random;
		random = new MersenneTwisterFast(System.currentTimeMillis() & 0xFFFFFFFF);
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
