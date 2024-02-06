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

package com.i3s.app.rdfminer.evolutionary.geva.Mapper;

import com.i3s.app.rdfminer.evolutionary.geva.Exceptions.BadParameterException;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.GEChromosome;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.Phenotype;
import com.i3s.app.rdfminer.evolutionary.geva.Parameter.ParameterI;
import com.i3s.app.rdfminer.evolutionary.geva.Util.Constants;
import com.i3s.app.rdfminer.evolutionary.geva.Util.Enums;
import com.i3s.app.rdfminer.evolutionary.geva.Util.Random.MersenneTwisterFast;

import java.io.File;
import java.util.Properties;
import java.util.Stack;

/**
 * Class for GEGrammar.
 * GEGrammar uses a GEChromosome to map out a Phenotype
 * @version 2006.1013
 * @author EHemberg
 */
public class GEGrammar extends ContextFreeGrammar implements ParameterI {

    private int PRCSetSize;
    private int PRCRange;
    private int PRCPrecision;
    GEChromosome genotype;
    Phenotype phenotype;
    int maxWraps;
    int usedWraps;
    int usedCodons;
    String name;
    DerivationTree dT;
    int maxDerivationTreeDepth;
    int maxCurrentTreeDepth;
    int dTNodeCount;
    private String derivationTreeType;

    /** Creates a new instance of GEGrammar */
    public GEGrammar() {
        super();
        this.maxWraps = 1;
    }

    /**
     * New instance
     * @param file file to read grammar from
     */
    public GEGrammar(String file) {
        super();
        this.readBNFFile(file);
        this.maxWraps = 1;
    }

    /**
     * New instance
     * @param p properties
     */
    public GEGrammar(Properties p) {
        super();
        this.setProperties(p);
    }

    /**
     * Copy constructor. Does not copy the genotype and phenotype
     * @param copy grammar to copy
     */
    public GEGrammar(GEGrammar copy) {
        super(copy);
        this.derivationTreeType = copy.getDerivationString();
        this.setMaxWraps(copy.getMaxWraps());
        this.maxWraps = copy.maxWraps;
        // A copy of the derivation tree should be here
        this.maxDerivationTreeDepth = copy.getMaxDerivationTreeDepth();
        this.maxCurrentTreeDepth = copy.getMaxCurrentTreeDepth();
        if (dT != null) {
            this.dT = new DerivationTree(copy.dT);
        }
        this.dTNodeCount = copy.getDTNodeCount();
        this.name = copy.name;
        this.maxDerivationTreeDepth = copy.getMaxDerivationTreeDepth();
        this.maxCurrentTreeDepth = copy.getMaxCurrentTreeDepth();
    }

    /**
     * Set properties
     *
     * @param p object containing properties
     */
    public void setProperties(Properties p) {
        String file = null;
        String key;
        try {
            key = Constants.GRAMMAR_FILE;
            file = p.getProperty(key);
            if (file == null) {
                throw new BadParameterException(key);
            }
        } catch (BadParameterException e) {
            System.out.println(e + " No default grammar");
        }
        File f = new File(file);
        if (!f.exists()) { // try classloader
            this.readBNFFile(file);
        } else {
            this.readBNFFileFromFilesystem(file);
        }
        int value;
        try {
            key = Constants.MAX_WRAPS;
            value = Integer.parseInt(p.getProperty(key));
            if (value < 0) {
                throw new BadParameterException(key);
            }
        } catch (BadParameterException e) {
            value = 0;
            System.out.println(e + " default wraps:" + value);
        }
        this.maxWraps = value + 1;
        try {
            this.PRCSetSize = Integer.parseInt(p.getProperty(Constants.PRC_SETSIZE, Constants.DEFAULT_PRC_SETSIZE));
            this.PRCRange = Integer.parseInt(p.getProperty(Constants.PRC_RANGE, Constants.DEFAULT_PRC_RANGE));
            this.PRCPrecision = Integer.parseInt(p.getProperty(Constants.PRC_PRECISION, Constants.DEFAULT_PRC_PRECISION));

            if (PRCSetSize < 0) {
                throw new BadParameterException(Constants.PRC_SETSIZE);
            }
            if (PRCRange < 0) {
                throw new BadParameterException(Constants.PRC_RANGE);
            }
            if ((PRCPrecision < 0) || (PRCPrecision > 32)) {
                throw new BadParameterException(Constants.PRC_PRECISION);
            }
        } catch (BadParameterException e) {
            value = 0;
            System.out.println(e + " constant range:" + value);
        }
        if (PRCSetSize > 0) {
            generatePRC();
        }

        this.maxWraps = value + 1;
        this.maxDerivationTreeDepth = Integer.parseInt(p.getProperty(Constants.MAX_DERIVATION_TREE_DEPTH, Constants.DEFAULT_MAX_DERIVATION_TREE_DEPTH));
        this.derivationTreeType = p.getProperty(Constants.DERIVATION_TREE);
        if(this.maxWraps>1 && this.derivationTreeType.equals("Mapper.ContextualDerivationTree"))
        {
            throw new IllegalArgumentException("Wrapping must be turned off for Context Sensitive trees");
        }
    }

    /**
     * Map input to output
     * @return validity of mapping
     */
    public boolean genotype2Phenotype() {
        return genotype2Phenotype(false);
    }

    /**
     * Map output to input
     * @return boolean Is the mapping valid
     */
    public boolean phenotype2Genotype() {
        return true;
    }

    /**
     * Replaces constants in the grammar with an array of randomly generated constants
     */
    public void generatePRC() {
        Rule oldRule = findRule("<prc>");

        if (oldRule != null) {
            System.out.println("<prc> rule found in grammar, generating constants");
            MersenneTwisterFast m = new MersenneTwisterFast();
            // creating rule to store constants
            Rule newRule = new Rule(PRCSetSize);
            newRule.setLHS(oldRule.getLHS());
            Symbol newSymbol = new Symbol();
            newSymbol.setType(Enums.SymbolType.TSymbol);

            for (int i = 0; i < PRCSetSize; i++) {
                //setting the precision and range to be used
                int divisor = (int) Math.pow(10, PRCPrecision);
                float constant = Math.abs((float) m.nextInt());
                constant = constant % (PRCRange * divisor);    //set the Mod first
                constant = (constant / divisor);
                //converting to string
                StringBuffer strConst = new StringBuffer();
                strConst.append(constant);
                newSymbol.setSymbolString(strConst.toString());

                Production np = new Production();
                np.add(new Symbol(newSymbol));
                newRule.add(np);
            }

            //replacing old rule with new one.
            int x = rules.indexOf(oldRule);
            rules.remove(x);
            rules.add(x, newRule);
            System.out.println("PRC SetSize: " + PRCSetSize + " PRC Range: " + PRCRange +
                    " PRC Precision: " + PRCPrecision);
            System.out.println("elements in the rule:" + newRule.toString());
        } else {
            System.out.println("Warning, PRC attributes specified but no <prc> rule in the grammar, no constants generated");
        }
    }

    /**
     * Clear the grammar
     */
    public void clear() {
        this.setValidGrammar(false);
        this.rules.clear();
        this.setStartSymbol(0);
        this.rules.clear();
    }

    /** Instanciates a derivation tree and calls buildDerivationTree() if
     * b is true else sets valid map to true 
     * @param b if tree should be built
     * @return validity of mapping
     */
    @SuppressWarnings({"UnusedAssignment"})
    public boolean genotype2Phenotype(boolean b) {
        boolean validMap;
        if (b) {
            this.phenotype.clear();
            this.dT = TreeMill.getDerivationTree(this);
            validMap = dT.derive();
//            System.out.println("after derive: phen= " + this.phenotype.getStringNoSpace());
            setDerivationTree(dT);
            this.usedCodons = dT.getGeneCnt();
            this.usedWraps = dT.getWrapCount();
            this.maxCurrentTreeDepth = dT.getDepth();
            this.name = generateNameFromTree(dT);
            dT = null;//remove reference
        } else {
            validMap = true;
        }
        return validMap;
    }

    String generateNameFromTree(DerivationTree tree) {
        StringBuilder builder = new StringBuilder();
        Stack<DerivationNode> nodeStack = new Stack<DerivationNode>();
        nodeStack.push((DerivationNode) tree.getRoot());
        while (nodeStack.empty() == false) {
            DerivationNode nodes = nodeStack.pop();
            if (nodes != null) {
                if (nodes.getCodonIndex() != -1) {
                    builder.append(nodes.getCodonPick());
                }
                if (nodes.size() != 0) {
                    builder.append('[');
                    nodeStack.push(null);
                    for (int i = nodes.size(); i > 0; i--) {
                        nodeStack.push((DerivationNode) nodes.get(i - 1));
                    }
                }
            } else {
                builder.append(']');
            }
        }
//        System.out.println("generateNameFromTree: " + builder.toString());
        return builder.toString();
    }

    /**
     * Get a grammar of the right type
     * @param g grammar to create new instance of;
     * @return a new grammar object of the right type
     */
    public static GEGrammar getGrammar(final GEGrammar g) {
        final GEGrammar grammar;
	grammar = new GEGrammar(g);
        return grammar;
    }

    /**
     *  Get the max depth of the derivation tree built by the grammar
     * @return max derivation tree depth
     */
    public int getMaxCurrentTreeDepth() {
        return maxCurrentTreeDepth;
    }

    /**
     * Set the max depth of the derriation tree that was built by this grammar
     * @param maxCurrentTreeDepth
     */
    public void setMaxCurrentTreeDepth(int maxCurrentTreeDepth) {
        this.maxCurrentTreeDepth = maxCurrentTreeDepth;
    }

    /**
     * Get max wraps allowed
     * @return max wraps
     */
    public int getMaxWraps() {
        return maxWraps;
    }

    /**
     * Set max wraps
     * @param i max wraps
     */
    public void setMaxWraps(int i) {
        this.maxWraps = i;
    }

    /**
     * Set input
     * @param genotype input
     */
    public void setGenotype(GEChromosome genotype) {
        this.genotype = genotype;
    }

    /**
     * Set output
     * @param phenotype output
     */
    public void setPhenotype(Phenotype phenotype) {
        this.phenotype = phenotype;
    }

    public Phenotype getPhenotype() {
        return phenotype;
    }

    public GEChromosome getGenotype() {
        return this.genotype;
    }

    public void setPhenotype(Object p) {
        this.phenotype = (Phenotype) p;

    }

    public void setGenotype(Object g) {
        this.genotype = (GEChromosome) g;

    }

    /**
     * Get used inputs (codons in the genotype)
     * @return number of used inputs
     */
    public int getUsedCodons() {
        return this.usedCodons;
    }

    /**
     * Get used wraps (Number of times the input was reread from the start)
     * @return number of wraps
     */
    public int getUsedWraps() {
        return this.usedWraps;
    }

    public int getMaxDerivationTreeDepth() {
        return maxDerivationTreeDepth;
    }

    public void setMaxDerivationTreeDepth(int maxDerivationTreeDepth) {
        this.maxDerivationTreeDepth = maxDerivationTreeDepth;
    }

    /**
     * Set the derivation tree
     * @param dT Derivation Tree
     */
    public void setDerivationTree(DerivationTree dT) {
        this.dT = dT;
    }

    public DerivationTree getDerivationTree() {
        return dT;
    }

    /**
     * This method calculates an upper bound for the number of inputs
     * needed to build a derivation tree according to the specified
     * maxDerivationTreeDepth and the max number of non terminals in a
     * production of the grammars rules.
     * 
     * The upper bound is calculated as the sum of the max non
     * terminals at each depth of the tree.  
     * (length = Sum_i=0^D a^i, 
     * D=max depth, 
     * a = max non terminals in a production)
     * 
     * @return int Max number of inputs
     */
    public int getMaxChromosomeLengthByDepth() {
        int len = Integer.MAX_VALUE;
        if (this.maxDerivationTreeDepth < Integer.MAX_VALUE) {
            int maxNTProd = 0;
            // Get the maximum number of Non terminals in a production among the rules
            for (Rule r : rules) {
                for (Production p : r) {
                    if (maxNTProd < p.getNTSymbols()) {
                        maxNTProd = p.getNTSymbols();
                    }
                }
            }
            // Sum for each depth
            len = 0;
            for (int i = 0; i < this.maxDerivationTreeDepth; i++) {
                len += Math.pow(maxNTProd, i);
            }
        }
        return len;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final GEGrammar that = (GEGrammar) obj;
        if (name != that.name && (name == null || name.equals(that.name) == false)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        if (name != null) {
            return name.hashCode();
        }
        return 0;
    }

    public int getDTNodeCount() {
        return this.dTNodeCount;
    }

    public void setDerivationTreeType(String derivationTreeType) {
        this.derivationTreeType = derivationTreeType;
    }

    @Override
    public String getDerivationString() {
        return this.derivationTreeType;
    }
}

