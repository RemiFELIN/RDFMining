package com.i3s.app.rdfminer.evolutionary.crossover;

import Individuals.GEChromosome;
import Operator.Operations.SinglePointCrossover;
import Util.Random.RandomNumberGenerator;
import com.i3s.app.rdfminer.generator.Generator;
import com.i3s.app.rdfminer.evolutionary.individual.GEIndividual;
import org.apache.log4j.Logger;

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
		int[] xoPoints = this.makeNewChromosome(chrome1, chrome2, chrome1MaxXOPoint, chrome2MaxXOPoint);
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

//	public static void main(String[] args) {
//		// Configure the log4j loggers:
//		PropertyConfigurator.configure("/home/rfelin/projects/RDFMining/RDFMiner/code/resources/log4j.properties");
//		RDFMiner.parameters.initLenChromosome = 2;
//		RandomNumberGenerator rand = new MersenneTwisterFast();
//		logger.info("rand= " + rand.nextInt());
//		SinglePointCrossoverAxiom test = new SinglePointCrossoverAxiom(0.8, new MersenneTwisterFast());
//		GEChromosome c1 = new GEChromosome(2);
//		GEChromosome c2 = new GEChromosome(2);
//		c1.add(1);
//		c1.add(2);
//		c2.add(3);
//		c2.add(4);
//		GEIndividual ge1 = new GEIndividual();
//		GEIndividual ge2 = new GEIndividual();
//		test.doOperation(List.of(ge1, ge2));
////		test.crossover(c1, c2);
//		logger.info("c1: " + c1 + " / c2: " + c2);
//	}

}
