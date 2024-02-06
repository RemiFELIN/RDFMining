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

package com.i3s.app.rdfminer.evolutionary.geva.Operator.Operations;

import com.i3s.app.rdfminer.evolutionary.geva.Exceptions.BadParameterException;
import com.i3s.app.rdfminer.evolutionary.geva.Exceptions.InitializationException;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.*;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.FitnessPackage.BasicFitness;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.FitnessPackage.Fitness;
import com.i3s.app.rdfminer.evolutionary.geva.Mapper.GEGrammar;
import com.i3s.app.rdfminer.evolutionary.geva.Mapper.Production;
import com.i3s.app.rdfminer.evolutionary.geva.Mapper.Rule;
import com.i3s.app.rdfminer.evolutionary.geva.Mapper.Symbol;
import com.i3s.app.rdfminer.evolutionary.geva.Util.Constants;
import com.i3s.app.rdfminer.evolutionary.geva.Util.Enums;
import com.i3s.app.rdfminer.evolutionary.geva.Util.Random.RandomNumberGenerator;
import com.i3s.app.rdfminer.evolutionary.geva.Util.Random.Stochastic;
import com.i3s.app.rdfminer.evolutionary.geva.Util.Structures.NimbleTree;
import com.i3s.app.rdfminer.evolutionary.geva.Util.Structures.TreeNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * Class for growing individuals to the maximum derrivationTree size of maxDepth
 *
 * @author erikhemberg
 */
public class GrowInitialiser implements CreationOperation, Stochastic {

    protected Genotype genotype;
    protected GEChromosome chromosome;
    protected RandomNumberGenerator rng;
    protected int maxDepth;
    protected int minDepth;
    protected GEGrammar grammar;
    protected int initChromSize;
    private double tailSize;

    /**
     * New instance
     *
     * @param rng       random number generator
     * @param gegrammar grammatical evolution grammar
     * @param maxDepth  max growth depth of tree
     */
    public GrowInitialiser(RandomNumberGenerator rng, GEGrammar gegrammar, int maxDepth) {
        this.grammar = gegrammar;
        this.maxDepth = maxDepth;
        this.minDepth = 0;
        this.initChromSize = 100; //Default initial chrom size
        this.rng = rng;
    }

    /**
     * New instance
     *
     * @param rng       random number generator
     * @param gegrammar grammatical evolution grammar
     * @param p         properties
     */
    public GrowInitialiser(RandomNumberGenerator rng, GEGrammar gegrammar, Properties p) {
        this.grammar = gegrammar;
        this.initChromSize = 100; //Default initial chrom size
        setProperties(p);
        this.minDepth = 0;
        this.rng = rng;
    }

    public void setRNG(RandomNumberGenerator m) {
        this.rng = m;
    }

    public RandomNumberGenerator getRNG() {
        return this.rng;
    }

    public void setProperties(Properties p) {
        maxDepth = Integer.parseInt(p.getProperty(Constants.MAX_DEPTH, "-1"));
        if (maxDepth == -1)
            throw new BadParameterException(Constants.MAX_DEPTH, getClass().getName());

        tailSize = Double.parseDouble(p.getProperty(Constants.TAIL_PERCENTAGE, "-1.0"));
        if (tailSize < 0)
            throw new BadParameterException(Constants.TAIL_PERCENTAGE, getClass().getName());
    }

    /**
     * Creates an Individuals
     */
    public GEIndividual createIndividual() {
        GEGrammar gram = GEGrammar.getGrammar(this.grammar);
        Phenotype phenotype = new Phenotype();
        int[] codons = new int[this.initChromSize];
        GEChromosome chrom = new GEChromosome(this.initChromSize, codons);
        // If the given max derivation tree depth is less than the max depth of
        // the tree, set the max derivation tree depth to the max depth of the
        // the tree.
        if (gram.getMaxDerivationTreeDepth() < this.maxDepth) {
            gram.setMaxDerivationTreeDepth(this.maxDepth);
        }
        chrom.setMaxChromosomeLength(gram.getMaxChromosomeLengthByDepth());
        Genotype geno = new Genotype(1, chrom);
        Fitness fitness = new BasicFitness();
        return new GEIndividual(gram, phenotype, geno, fitness);
    }

    /**
     * Get minimum depth of tree
     *
     * @return minimum depth
     */
    public int getMinDepth() {
        return minDepth;
    }

    /**
     * Set minimum depth
     *
     * @param minDepth minumum depth
     */
    public void setMinDepth(int minDepth) {
        this.minDepth = minDepth;
    }

    /**
     * Set maximum depth of tree
     *
     * @param i max depth
     */
    public void setMaxDepth(int i) {
        this.maxDepth = i;
    }

    /**
     * Get max depth of tree
     *
     * @return max depth
     */
    public int getMaxDepth() {
        return this.maxDepth;
    }

    public void doOperation(GEIndividual operand) {
        operand.setGenotype(this.getGenotype(((GEChromosome) operand.getGenotype().get(0)).getMaxChromosomeLength()));
    }

    // Implement
    public void doOperation(List<GEIndividual> operands) {
    }

    /**
     * Creates a genotype by building a tree to the most maxDepth for one branch.
     * WHAT TO DO IF SIZE IS LARGER THAN MAX_LENGTH*WRAPS??
     *
     * @return A valid Genotype
     **/
    public Genotype getGenotype(int maxLength) {
        genotype = new Genotype();
        chromosome = new GEChromosome(this.initChromSize);
        chromosome.setMaxChromosomeLength(maxLength);
        genotype.add(chromosome);
        // Initialise and grow derrivationTree
        NimbleTree<Symbol> dt = new NimbleTree<Symbol>(grammar.getStartSymbol());
        grow(dt);

        //Add tail
        int extraCodons = (int) Math.ceil(chromosome.getLength() * tailSize);
        for (; extraCodons > 0; extraCodons--)
            chromosome.add(rng.nextInt(Integer.MAX_VALUE));

        return genotype;
    }

    /**
     * Recursively builds a tree.
     *
     * @param dt Tree to grow on
     * @return If the tree is valid
     **/
    public boolean grow(NimbleTree<Symbol> dt) {
        Rule rule;
        Iterator<Production> prodIt;
        ArrayList<Integer> possibleRules;
        Production prod;
        int prodVal;
        boolean result;

        try {
            //Check if we have over grown *shouldn't happen
            if (dt.getDepth() > this.maxDepth)
                return false;
            //Recursive base case
            if (dt.getCurrentNode().getData().getType() == Enums.SymbolType.TSymbol)
                return true;
            //Get the Rule for the current symbol
            rule = grammar.findRule(dt.getCurrentNode().getData());
            if (rule != null) {
                possibleRules = getPossibleRules(dt, rule);
                //If there are no valid productions, fail
                if (possibleRules.isEmpty()) {
                    return false;
                }
                //Generate a codon value for a randomly chosen production
                //"Unmod" the value
                prodVal = this.rng.nextInt(possibleRules.size());
                int modVal = possibleRules.get(prodVal);
                int tmp1 = this.rng.nextInt((Integer.MAX_VALUE - rule.size()));
                int tmp;
                int mod = tmp1 % rule.size();
                int diff;
                if (mod > modVal) {
                    diff = mod - modVal;
                    tmp = tmp1 - diff;
                } else {
                    diff = modVal - mod;
                    tmp = tmp1 + diff;
                }
                //Throw exception if the "unmodding" fails
                int newMod = tmp % rule.size();
                if (newMod != modVal) {
                    throw new InitializationException("Error calculating mod value:\nmodVal:" + modVal + " tmp1:" +
                            tmp1 + " mod:" + mod + " tmp:" + tmp + " rule.size():" + rule.size() + " newMod:" + newMod);
                }
                //Store the codon value only if there are more than one production in the Rule
                if (rule.size() > 1) {
                    chromosome.add(tmp);
                    prod = rule.get(possibleRules.get(prodVal));
                } else {
                    prod = rule.get(0);
                }
                //Recurse through each symbol in the chosen production
                result = true;
                for (Symbol symbol : prod) {
                    dt.addChild(symbol);
                    TreeNode<Symbol> parent = dt.getCurrentNode();
                    dt.setCurrentNode(parent.getEnd());
                    result &= grow(dt);
                    dt.setCurrentNode(parent);
                }
                chromosome.setValid(result);
                return result;
            } else {
                //If rule == null, i.e. there is no rule for that symbol
                if (!checkGECodonValue(dt)) {
                    throw new InitializationException("Non-existent rule, maybe GECODON not yet impelemnted. Could not find" +
                            dt.getCurrentNode().getData().getSymbolString());
                }
            }
        } catch (InitializationException e) {
            System.out.println(e);
            e.printStackTrace();
        }

        //Will only get here if rule == null or if an exception is thrown/caught
        return false;
    }

    public ArrayList<Integer> getPossibleRules(NimbleTree<Symbol> dt, Rule rule) {
        ArrayList<Integer> possibleRules = new ArrayList<Integer>();

        //Iterate through each possible production and store indices to the usable ones
        int i = 0;
        for (Production p : rule) {
            if ((dt.getCurrentNode().getDepth() + 1 + p.getMinimumDepth()) <= this.maxDepth)
                possibleRules.add(i);
            i++;
        }

        return possibleRules;
    }

    /**
     * Check if it is a GECodonValue. Sapecific construct for inserting informatino into the grammar
     *
     * @param dt tree
     * @return if it is a GECodonValue
     */
    @SuppressWarnings({"BooleanMethodIsAlwaysInverted"})
    protected boolean checkGECodonValue(NimbleTree<Symbol> dt) {
        boolean ret = false;
        if (dt.getCurrentNode().getData().getSymbolString().contains(Constants.GE_CODON_VALUE)) {
            this.chromosome.add(this.rng.nextInt(Integer.MAX_VALUE));
            ret = true;
        }
        return ret;
    }
}
