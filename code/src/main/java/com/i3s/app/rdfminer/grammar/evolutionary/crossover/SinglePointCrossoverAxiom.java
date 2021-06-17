package com.i3s.app.rdfminer.grammar.evolutionary.crossover;

import Individuals.GEChromosome;
import Operator.Operations.SinglePointCrossover;
import Util.Random.RandomNumberGenerator;

public class SinglePointCrossoverAxiom extends SinglePointCrossover {

	public SinglePointCrossoverAxiom(double prob, RandomNumberGenerator m) {
		super(prob, m);
		// TODO Auto-generated constructor stub
	}

	GEChromosome[] Crossover(GEChromosome c1, GEChromosome c2) {
		makeNewChromosome(c1, c2, c1.size(), c2.size());
		GEChromosome[] ListChromosome = new GEChromosome[2];
		ListChromosome[0] = c1;
		ListChromosome[1] = c2;
		return ListChromosome;
	}

}
