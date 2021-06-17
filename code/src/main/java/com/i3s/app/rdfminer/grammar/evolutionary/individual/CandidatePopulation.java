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
	protected GEChromosome[] ListChromosome;
	protected int maxlenChromosome;
	protected int maxWrapp;
	protected int maxvalCodon;
	protected int typeInitialization;
	protected int initlenChromosome;

	public CandidatePopulation(int size, RandomAxiomGenerator generator, int typeInitialization,
			GEChromosome[] ListChromosome, int initlenChromosome, int maxvalCodon, int maxWrapp) {
		this.size = size; // size of the population
		// this.generation=generation; //current generation
		this.generator = generator;
		this.ListChromosome = ListChromosome;
		// this.maxlenChromosome=maxlenChromosome;
		this.maxvalCodon = maxvalCodon;
		this.maxWrapp = maxWrapp;
		this.typeInitialization = typeInitialization;
		this.initlenChromosome = initlenChromosome;
	}

	public ArrayList<GEIndividual> Initialize(Reader buffer, int curGeneration)
			throws NumberFormatException, IndexOutOfBoundsException, IOException {
		GEChromosome chromosome;

		GEIndividual individual;
		// RandomNumberGenerator random;

		/*
		 * developed only in the case of random initialization - typeInitialization=1
		 * Later need to develop other type of initialization.
		 */

		if (buffer != null) {
			int intch;
			String st = "";
			char ch;
			int m = 0;
			ArrayList<Integer> chr = new ArrayList<Integer>();

			while ((intch = buffer.read()) != -1) {
				ch = (char) intch;
				if (ch != '\n') {
					if (ch != ',')
						st += ch;
					else {

						chr.add(Integer.parseInt(st));
						st = "";
					}
				} else {
					chromosome = new GEChromosome(chr.size());
					chromosome.setMaxCodonValue(maxvalCodon);
					chromosome.setMaxChromosomeLength(1000);
					for (int i = 0; i < chr.size(); i++)
						chromosome.add(chr.get(i));
					ListChromosome[m] = chromosome;
					chr = new ArrayList<Integer>();
					m++;
				}
			}
		}

		else {

			this.ListChromosome = InitializeListChromosome();
		}
		logger.info("number chromosome created: " + ListChromosome.length);
		ArrayList<GEIndividual> CandidatePopulation = new ArrayList<GEIndividual>(size);

		int j = 0;
		while (j < size) {
			if (generator != null)

			{
				individual = generator.axiomIndividual(ListChromosome[j], curGeneration);
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

	GEChromosome[] GetListChromosome() {

		return ListChromosome;
	}

	public ArrayList<GEIndividual> Renew(ArrayList<GEIndividual> ListCrossover, int curGeneration,
			ArrayList<GEIndividual> etilism_Population) {

		ArrayList<GEIndividual> NewPopulation = new ArrayList<GEIndividual>();
		if (etilism_Population != null)

			for (int i1 = 0; i1 < etilism_Population.size(); i1++) {

				etilism_Population.get(i1).setAge(curGeneration + 1);
				NewPopulation.add(etilism_Population.get(i1));
			}

		for (int i2 = 0; i2 < ListCrossover.size(); i2++) {
			ListCrossover.get(i2).setAge(curGeneration + 1);
			NewPopulation.add(ListCrossover.get(i2));

		}

		/*
		 * for (int i3=0; i3<ListMutation.size(); i3++) {
		 * ListCrossover.get(i3).setAge(curGeneration+1);
		 * NewPopulation.add(ListMutation.get(i3));
		 * 
		 * }
		 */

		// CandidatePopulation CanPop = new
		// CandidatePopulation(j,curGeneration,generator,typeInitialization,
		// ListChromosome,initlenChromosome, maxlenChromosome,maxvalCodon,maxWrapp);

		// SimplePopulation NewCandidatePopulation =CanPop.create();
		return NewPopulation;
	}

	public void AddBestIndividual(SimplePopulation Etilism_Population) {
		int i = 0;
		while (i < Etilism_Population.size()) {
			ListChromosome[i] = (GEChromosome) Etilism_Population.get(i).getGenotype().get(0);
			i++;
		}
	}

	public GEChromosome[] InitializeListChromosome() {
		RandomNumberGenerator random;
		random = new MersenneTwisterFast(System.currentTimeMillis() & 0xFFFFFFFF);
		GEChromosome chromosome;
		int maxLenChromosome = 1000;
		int n = 0;
		while (n < size)

		{
			chromosome = new GEChromosome(initlenChromosome);
			chromosome.setMaxCodonValue(maxvalCodon);
			chromosome.setMaxChromosomeLength(maxLenChromosome);
			for (int i = 0; i < initlenChromosome; i++) {
				chromosome.add(Math.abs(random.nextInt(maxvalCodon)));// typeInitialization=1

			}
			ListChromosome[n] = chromosome;
			logger.info(chromosome);
			n++;
		}
		return ListChromosome;
	}

	public GEIndividual CreateNewAxiom(GEChromosome chromosome) {

		GEIndividual individual = new GEIndividual();
		RandomNumberGenerator random;
		random = new MersenneTwisterFast(System.currentTimeMillis() & 0xFFFFFFFF);
		int maxLenChromosome = 1000;
		chromosome = new GEChromosome(initlenChromosome);
		chromosome.setMaxCodonValue(maxvalCodon);
		chromosome.setMaxChromosomeLength(maxLenChromosome);
		for (int i = 0; i < initlenChromosome; i++) {
			chromosome.add(Math.abs(random.nextInt(maxvalCodon)));// typeInitialization=1
		}
		if (generator != null)

		{
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
			chromosome.add(Math.abs(random.nextInt(maxvalCodon)));// typeInitialization=1
		}
		return chromosome;
	}

}
