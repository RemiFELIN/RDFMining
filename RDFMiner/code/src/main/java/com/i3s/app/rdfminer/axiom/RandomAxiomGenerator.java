/**
 * 
 */

// import org.apache.log4j.Logger;
package com.i3s.app.rdfminer.axiom;

import com.i3s.app.rdfminer.grammar.evolutionary.individual.GEIndividual;

import Individuals.GEChromosome;
import Individuals.Genotype;
import Individuals.Phenotype;
import Mapper.GEGrammar;
import Util.Random.MersenneTwisterFast;
import Util.Random.RandomNumberGenerator;

/**
 * A generator of random axioms for a given logical language.
 * <p>
 * The syntax of the logical language from which the axioms are to be randomly
 * extracted is given by a functional-style grammar expressed in the <a href=
 * "http://www.w3.org/TR/2012/REC-owl2-syntax-20121211/#BNF_Notation">extended
 * BNF notation</a> used by the <a href="http://www.w3.org/">W3C</a>.
 * </p>
 * 
 * @author Andrea G. B. Tettamanzi
 *
 */
public class RandomAxiomGenerator extends AxiomGenerator {
	// private static Logger logger =
	// Logger.getLogger(RandomAxiomGenerator.class.getName());

	/**
	 * The random number generator used to generate axioms.
	 */
	protected RandomNumberGenerator random;

	/**
	 * Constructs a new axiom generator for the language described by the given
	 * grammar.
	 * 
	 * @param fileName the name of the file containing the grammar.
	 */
	public RandomAxiomGenerator(String fileName, boolean v2) {
		super(fileName, v2);

		// Set up a pseudo-random number generator
		random = new MersenneTwisterFast(System.currentTimeMillis() & 0xFFFFFFFF);
	}

	/**
	 * Generate the next random axiom.
	 * 
	 * @return a random axiom
	 */
	public Phenotype nextAxiom() {
		GEChromosome chromosome;
		boolean valid;
		do {
			chromosome = new GEChromosome(1000);
			chromosome.setMaxCodonValue(Integer.MAX_VALUE);
			chromosome.setMaxChromosomeLength(1000);
			for (int i = 0; i < 1000; i++)
				chromosome.add(Math.abs(random.nextInt()));
			grammar.setGenotype(chromosome);
			grammar.setPhenotype(new Phenotype());
			valid = grammar.genotype2Phenotype(true);
		} while (!valid);
		return grammar.getPhenotype();
	}

	
	public GEIndividual axiomIndividual(GEChromosome chromosome, int generation) {
		GEIndividual individual;
		boolean valid;
		int i = 1;
		do {
			grammar.setGenotype(chromosome);
			grammar.setPhenotype(new Phenotype());
			valid = grammar.genotype2Phenotype(true);
			i++;
		} while ((!valid) && (i < grammar.getMaxWraps()));
		Genotype gp = new Genotype(1, chromosome);
		GEGrammar gr = (GEGrammar) grammar;
		individual = new GEIndividual();
		individual.setMapper(gr);
		individual.setGenotype(gp);
		individual.setPhenotype(grammar.getPhenotype());
		individual.setValid(true);
		individual.setUsedCodons(chromosome.getUsedGenes());
		individual.setUsedWraps(grammar.getUsedWraps() - 1);
		individual.setAge(generation);
		if (valid == true) {
			individual.setMapped(true);
		}
		return individual;
	}

}
