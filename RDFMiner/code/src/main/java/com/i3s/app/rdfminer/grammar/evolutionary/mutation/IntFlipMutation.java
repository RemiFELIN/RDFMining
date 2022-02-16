/*
Grammatical Evolution in Java
Release: GEVA-v2.0.zip
Copyright (C) 2008 Michael O'Neill, Erik Hemberg, Anthony Brabazon, Conor Gilligan 
Contributors Patrick Middleburgh, Eliott Bartley, Jonathan Hugosson, Jeff Wrigh

Separate licences for asm, bsf, antlr, groovy, jscheme, commons-logging, jsci is included in the lib folder. 
Separate licence for rieps is included in src/com folder.

This licence refers to GEVA-v2.0.

This software is distributed under the terms of the GNU General Public License.


This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
/>.
*/

/*
 * IntFlipMutation.java
 *
 * Created on 15 March 2007, 20:05
 *
 */

package com.i3s.app.rdfminer.grammar.evolutionary.mutation;

import Individuals.GEChromosome;
import Individuals.Individual;
import Operator.Operations.MutationOperation;
import Util.Random.RandomNumberGenerator;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import com.i3s.app.rdfminer.generator.Generator;
import com.i3s.app.rdfminer.generator.axiom.RandomAxiomGenerator;
import com.i3s.app.rdfminer.grammar.evolutionary.individual.GEIndividual;

/**
 * IntFlipMutation does integer mutation
 * 
 * @author Conor
 */
public class IntFlipMutation extends MutationOperation {

	/**
	 * Creates a new instance of IntFlipMutation
	 * 
	 * @param prob mutation probability
	 * @param rng  random number generator
	 */
	public IntFlipMutation(double prob, RandomNumberGenerator rng) {
		super(prob, rng);
	}

	/**
	 * New instance
	 * 
	 * @param rng random number generator
	 * @param p   properties
	 */
	public IntFlipMutation(RandomNumberGenerator rng, Properties p) {
		super(rng, p);
	}

	/**
	 * Calls doMutation(GEIndividual c) and then calls Individual.invalidate()
	 * 
	 * @param operand operand to operate on
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public GEIndividual doOperation(GEIndividual operand, Generator generator, int curGeneration, int[] pos)
			throws IOException, InterruptedException {
		GEChromosome chr = new GEChromosome((GEChromosome) operand.getGenotype().get(0));
		GEChromosome chr2 = new GEChromosome(doMutation(chr, pos));
		((GEIndividual) operand).invalidate();
		operand = generator.getIndividualFromChromosome(chr2, curGeneration);
		return operand;
	}

	/**
	 * According to this.probability a codon in the chromosome is replaced with a
	 * new randomly chosen integer
	 * 
	 * @param c input to mutate
	 */
	private GEChromosome doMutation(GEChromosome c, int[] pos) {
		double probability_mut = this.probability;
		if (pos[0] != 0)
			probability_mut = 1.0;
		// System.out.println("pro_mut: " +probability_mut);

		for (int i = pos[0]; i < c.getLength(); i++) {

			if (this.rng.nextBoolean(probability_mut)) {
				final int nextInt = Math.abs(rng.nextInt());
				c.set(i, nextInt);
				/*
				 * System.out.println("Mutation position: " + i);
				 * System.out.println("Value mutation: " + nextInt);
				 * System.out.println("First Mutation position: " + pos[0]);
				 */
			}
		}

		return c;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * Operator.Operations.MutationOperation#doOperation(Individuals.Individual)
	 */
	@Override
	public void doOperation(Individual arg0) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Operator.Operations.MutationOperation#doOperation(java.util.List)
	 */
	@Override
	public void doOperation(List<Individual> arg0) {
	}

}
