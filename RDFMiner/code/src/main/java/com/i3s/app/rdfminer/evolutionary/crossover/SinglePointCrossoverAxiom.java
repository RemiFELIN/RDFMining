package com.i3s.app.rdfminer.evolutionary.crossover;

import Individuals.GEChromosome;
import Operator.Operations.SinglePointCrossover;
import Util.Random.MersenneTwisterFast;
import Util.Random.RandomNumberGenerator;
import com.i3s.app.rdfminer.RDFMiner;
import com.i3s.app.rdfminer.evolutionary.individual.CandidatePopulation;
import com.i3s.app.rdfminer.generator.Generator;
import com.i3s.app.rdfminer.evolutionary.individual.GEIndividual;
import com.i3s.app.rdfminer.generator.axiom.RandomAxiomGenerator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class SinglePointCrossoverAxiom extends SinglePointCrossover {

	private static final Logger logger = Logger.getLogger(SinglePointCrossoverAxiom.class.getName());

	public Generator generator;

	public int curGeneration;

	public SinglePointCrossoverAxiom(double prob, RandomNumberGenerator m, Generator generator, int curGeneration) {
		super(prob, m);
		this.generator = generator;
		this.curGeneration = curGeneration;
	}

	protected int getMaxXOPoint(GEIndividual i) {
		int chromsomeSize = i.getGenotype().get(0).getLength();
		int maxXOpoint = i.getPreviouslyUsedCodons() % chromsomeSize;
		if (maxXOpoint <= 0 || !this.codonsUsedSensitive) {
			maxXOpoint = chromsomeSize;
		}
		return maxXOpoint;
	}

	public GEIndividual[] doOperation(GEIndividual p1, GEIndividual p2) {
		if (this.rand.nextDouble() < this.probability) {
			logger.info("Crossover observed !");
			GEChromosome chrom1 = (GEChromosome)p1.getGenotype().get(0);
			GEChromosome chrom2 = (GEChromosome)p2.getGenotype().get(0);
			int p1maxXOPoint = this.getMaxXOPoint(p1);
			int p2maxXOPoint = this.getMaxXOPoint(p2);
			return this.performCrossover(chrom1, chrom2, p1maxXOPoint, p2maxXOPoint);
		} else {
			GEIndividual[] individuals = new GEIndividual[2];
			individuals[0] = p1;
			individuals[1] = p2;
			return individuals;
		}
	}

	public GEIndividual[] performCrossover(GEChromosome chrome1, GEChromosome chrome2, int chrome1MaxXOPoint, int chrome2MaxXOPoint) {
//		System.out.println("BEFORE chrom1: " + chrome1 + " and chrom2: " + chrome2);
//		System.out.println("chrome1MaxXOPoint=" + chrome1MaxXOPoint);
//		System.out.println("chrome2MaxXOPoint=" + chrome2MaxXOPoint);
		int[] xoPoints = new int[2];
		// avoid useless crossover where we didn't observe any modification
		// because of a 0 or max lenght size of chromosome value for crossover
		while (xoPoints[0] == 0 || xoPoints[0] == chrome1MaxXOPoint) {
//			logger.info("Not good !");
			xoPoints = this.makeNewChromosome(chrome1, chrome2, chrome1MaxXOPoint, chrome2MaxXOPoint);
		}
//		System.out.println("xoPoints[0 and 1]=" + xoPoints[0]);
//		System.out.println("AFTER chrom1: " + chrome1 + " and chrom2: " + chrome2);
		GEIndividual indiv1 = generator.getIndividualFromChromosome(chrome1, curGeneration);
		GEIndividual indiv2 = generator.getIndividualFromChromosome(chrome2, curGeneration);
		indiv1.getParentUIDs().clear();
		indiv2.getParentUIDs().clear();
		if (xoPoints[0] == 0) {
			indiv1.getParentUIDs().add(indiv2.getUID());
		} else {
			indiv1.getParentUIDs().add(indiv1.getUID());
			indiv1.getParentUIDs().add(indiv2.getUID());
		}

		if (xoPoints[1] == 0) {
			indiv2.getParentUIDs().add(indiv1.getUID());
		} else {
			indiv2.getParentUIDs().add(indiv2.getUID());
			indiv2.getParentUIDs().add(indiv1.getUID());
		}
		GEIndividual[] individuals = new GEIndividual[2];
		individuals[0] = indiv1;
		individuals[1] = indiv2;
		return individuals;
	}

	public static void main(String[] args) {
		// Configure the log4j loggers:
		PropertyConfigurator.configure("/home/rfelin/projects/RDFMining/RDFMiner/code/resources/log4j.properties");
		RDFMiner.parameters.initLenChromosome = 2;
		RDFMiner.parameters.populationSize = 2;
//		RandomNumberGenerator rand = new MersenneTwisterFast();
//		logger.info("rand= " + rand.nextInt());
		SinglePointCrossoverAxiom test = null;
		Generator generator = null;
		try {
			generator = new RandomAxiomGenerator("/user/rfelin/home/projects/RDFMining/IO/OWL2Axiom-subclassof.bnf", true);
			test = new SinglePointCrossoverAxiom(1.0, new MersenneTwisterFast(), generator, 1);
			test.setFixedCrossoverPoint(true);
		} catch (URISyntaxException | IOException e) {
			e.printStackTrace();
		};
		CandidatePopulation canPop = new CandidatePopulation(generator);
		GEChromosome[] chroms = canPop.initializeChromosomes();
		ArrayList<GEIndividual> population = new ArrayList<>();
		for(GEChromosome chrom : chroms) {
			System.out.println(chrom);
			GEIndividual ind = generator.getIndividualFromChromosome(chrom, 1);
			System.out.println("This individual: " + ind.getGenotype() + "\nas " + ind.getPhenotype().getStringNoSpace() + "\n");
			population.add(ind);
		}
		// test crossover
		GEIndividual[] newInd = test.doOperation(population.get(0), population.get(1));
		for(GEIndividual n : newInd) {
			System.out.println("This individual: " + n.getGenotype() + "\nas " + n.getPhenotype().getStringNoSpace() + "\n");
		}
//		GEChromosome c1 = new GEChromosome(2);
//		GEChromosome c2 = new GEChromosome(2);
//		c1.add(1);
//		c1.add(2);
//		c2.add(3);
//		c2.add(4);
//		GEIndividual ge1 = new GEIndividual();
//		GEIndividual ge2 = new GEIndividual();
//		test.doOperation(List.of(ge1, ge2));
//		test.crossover(c1, c2);
//		logger.info("c1: " + c1 + " / c2: " + c2);
	}

}
