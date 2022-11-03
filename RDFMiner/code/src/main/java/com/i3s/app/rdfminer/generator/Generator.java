package com.i3s.app.rdfminer.generator;

import Individuals.GEChromosome;
import Individuals.Genotype;
import Individuals.Phenotype;
import Mapper.ContextFreeGrammar;
import Mapper.DerivationTree;
import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.RDFMiner;
import com.i3s.app.rdfminer.grammar.DLGEGrammar;
import com.i3s.app.rdfminer.grammar.evolutionary.individual.GEIndividual;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * An abstract class of generator in RDFMiner
 * It can be an Axioms generator or a SHACL Shapes generator
 *
 * @author Rémi FELIN
 */
public abstract class Generator {

    /**
     * The grammar defining the logical language of the axioms.
     */
    protected DLGEGrammar grammar;

    /**
     * @return the grammar used
     */
    public ContextFreeGrammar getGrammar() {
        return grammar;
    }

    /**
     * Load a given file path as a grammar to follow for our future rules
     *
     * @param filePath the path of grammar file
     */
    public Generator(String filePath) {
        // Set up the grammar to be used for generating the axioms:
        if (filePath == null) {
            grammar = null;
        } else {
            grammar = new DLGEGrammar(filePath);
            grammar.setDerivationTreeType(DerivationTree.class.getName());
            // grammar.setDerivationTreeType(ContextualDerivationTree.class.getName());
            grammar.setMaxDerivationTreeDepth(100);
            // set max wrapp
            grammar.setMaxWraps(RDFMiner.parameters.maxWrapp);
        }
    }

    /**
     * Dynamically generates the productions for the rule corresponding to the given
     * symbol using the given SPARQL query.
     * <p>
     * If a rule for the given symbol does not exist, it is created; if it exists,
     * the dynamically-generated productions are simply added to the static
     * productions defined in the grammar.
     * </p>
     *
     * @param symbol the name of a non-terminal symbol for which the productions are
     *               to be generated!
     * @param sparql a <code>SELECT</code> SPARQL query
     * @throws URISyntaxException Error concerning the syntax of the given URL
     * @throws IOException        Error concerning the execution of the GET request
     */
    protected abstract void generateProductions(String symbol, String sparql) throws URISyntaxException, IOException;

    /**
     * Take a GEChromosome and build an instance of GEInidividual, in order to use it in EA
     *
     * @param chromosome instance of GEChromosome
     * @param generation ID generation
     * @return an individual well formed from chromosome
     */
    public GEIndividual getIndividualFromChromosome(GEChromosome chromosome, int generation) {
        GEIndividual individual;
        boolean valid;
        int i = 1;
        do {
            grammar.setGenotype(chromosome);
            grammar.setPhenotype(new Phenotype());
            valid = grammar.genotype2Phenotype(true);
            i++;
        } while (!valid && i < grammar.getMaxWraps());

        individual = new GEIndividual();
        individual.setMapper(grammar);
        individual.setGenotype(new Genotype(1, chromosome));
        individual.setPhenotype(grammar.getPhenotype());
        individual.setValid(true);
        individual.setUsedCodons(chromosome.getUsedGenes());
        individual.setUsedWraps(grammar.getUsedWraps() - 1);
        individual.setAge(generation);
        // set a random mutation point
        int value = (int) Math.round(Math.random() * individual.getGenotype().get(0).getLength());
        int[] arr = new int[]{value};
        individual.setMutationPoints(arr);
        // set a random
        if (valid) individual.setMapped(true);

        return individual;
    }

    /**
     * Generate a cache file name from a SPARQL query, so that each file has a
     * different name.
     */
    public static String cacheName(String symbol, String sparql) {
        return String.format(Global.CACHE_PATH + "%s%08x.cache", symbol, sparql.hashCode());
    }

}
