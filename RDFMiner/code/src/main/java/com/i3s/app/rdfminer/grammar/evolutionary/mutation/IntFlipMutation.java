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
import org.apache.log4j.Logger;

/**
 * IntFlipMutation does integer mutation
 * 
 * @author Conor
 */
public class IntFlipMutation extends MutationOperation {

	private static final Logger logger = Logger.getLogger(IntFlipMutation.class.getName());

	/**
	 * Creates a new instance of IntFlipMutation
	 * @param prob mutation probability
	 * @param rng  random number generator
	 */
	public IntFlipMutation(double prob, RandomNumberGenerator rng) {
		super(prob, rng);
	}

	/**
	 * New instance
	 * @param rng random number generator
	 * @param p   properties
	 */
	public IntFlipMutation(RandomNumberGenerator rng, Properties p) {
		super(rng, p);
	}

	/**
	 * Calls doMutation(GEIndividual c) and then calls Individual.invalidate()
	 * @param operand operand to operate on
	 */
	public GEIndividual doOperation(GEIndividual operand, Generator generator, int curGeneration, int[] pos) {
		GEChromosome chr2 = new GEChromosome(
				doMutation(
						new GEChromosome((GEChromosome) operand.getGenotype().get(0)),
						pos
				)
		);
		operand.invalidate();
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
		for (int i = pos[0]; i < c.getLength(); i++) {
			if (this.rng.nextBoolean(this.probability)) {
				logger.info("Mutation observed !");
				final int nextInt = Math.abs(rng.nextInt());
				c.set(i, nextInt);
			}
		}
		return c;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * Operator.Operations.MutationOperation#doOperation(Individuals.Individual)
	 */
	@Override
	public void doOperation(Individual arg0) {
	}

	/*
	 * (non-Javadoc)
	 * @see Operator.Operations.MutationOperation#doOperation(java.util.List)
	 */
	@Override
	public void doOperation(List<Individual> arg0) {
	}

}
