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

package com.i3s.app.rdfminer.evolutionary.geva.Util;

import com.i3s.app.rdfminer.evolutionary.geva.Individuals.GEChromosome;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.GEIndividual;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.Individual;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.Phenotype;
import com.i3s.app.rdfminer.evolutionary.geva.Mapper.*;
import com.i3s.app.rdfminer.evolutionary.geva.Util.Structures.TreeNode;

import java.util.*;

/**
 * A collection of useful functions for manipulating genotype and such
 *
 * @author eliott bartley
 */
public class GenotypeHelper {

    private GenotypeHelper() {
    }

    /**
     * Build a derivation tree rooted at the specified node for a
     * given individual. If node is null, the tree's root is used
     *
     * @return null if the tree is invalid
     */
    public static DerivationTree buildDerivationTree(GEIndividual individual, DerivationNode node) {
        assert individual.getGenotype().get(0) instanceof GEChromosome : individual.getGenotype().get(0).getClass().getName();
        assert individual.getMapper() instanceof GEGrammar : individual.getMapper().getClass().getName();
        // Build the derivation tree
        GEGrammar grammar = (GEGrammar) individual.getMapper();
        if (node != null)
            grammar.setStartSymbol(node.getData());
        grammar.getPhenotype().clear();
        DerivationTree tree = TreeMill.getDerivationTree(grammar);
        if (tree.buildDerivationTree())
            return tree;
        // Don't return invalids
        return null;
    }

    /**
     * Build derivation trees for invalid individuals for repair
     * operators.
     *
     * @param individual Individual
     * @return DerivationTree The valid or invalid derivation tree
     */
    public static DerivationTree buildInvalidTree(Individual individual, DerivationNode node) {
        assert individual.getGenotype().get(0) instanceof GEChromosome : individual.getGenotype().get(0).getClass().getName();
        assert individual.getMapper() instanceof GEGrammar : individual.getMapper().getClass().getName();
        GEGrammar grammar = (GEGrammar) individual.getMapper();
        if (node != null) {
            grammar.setStartSymbol(node.getData());
        }
        grammar.getPhenotype().clear();
        DerivationTree tree = TreeMill.getDerivationTree(grammar);
        tree.buildDerivationTree();
        return tree;
    }


    /**
     * Build a derivation tree for a given individual
     *
     * @return invalid or valid derivation tree
     */
    public static DerivationTree buildInvalidTree(Individual individual) {
        return buildInvalidTree(individual, null);
    }

    /**
     * Build a derivation tree for a given individual
     *
     * @return null if the tree is invalid
     */
    public static DerivationTree buildDerivationTree(GEIndividual individual) {
        return buildDerivationTree(individual, null);
    }

    /**
     * Given a derivation tree and a codon index, return the tree node that
     * makes use of the codon at that index. e.g., if codonIndex was 0, the
     * root branch decides which production is picked based on the value of the
     * codon at index 0, so the root is returned in this case
     */
    public static DerivationNode findNodeFromCodonIndex(DerivationTree tree, int codonIndex) {
        return findNodeFromCodonIndex(tree, codonIndex, -1);
    }

    public static DerivationNode findNodeFromCodonIndex(DerivationTree tree, int codonIndex, int usedGenes) {
        Stack<DerivationNode> nodeStack = new Stack<DerivationNode>();
        nodeStack.push((DerivationNode) tree.getRoot());

        while (!nodeStack.empty()) {
            DerivationNode nodes = nodeStack.pop();
            if (nodes.getCodonIndex() == codonIndex)
                return nodes;
            for (TreeNode<Symbol> node : nodes)
                nodeStack.push((DerivationNode) node);
        }
        throw new AssertionError("Indexed node not found");
    }

    /**
     * Calculate how may codons are used to build a specified branch of a tree.
     * This works by searching the tree itself, and counting up which branches
     * have a codon related to them. When a branch uses a codon, it gets the
     * index of that codon attached to it by giving it a 'getCodonIndex' value
     * other that -1
     */
    public static int calcNodeLength(DerivationNode nodes) {
        Stack<DerivationNode> nodeStack = new Stack<DerivationNode>();
        nodeStack.push(nodes);
        int size = 0;
        while (!nodeStack.empty()) {
            nodes = nodeStack.pop();
            if (nodes.getCodonIndex() != -1)
                size++;
            for (TreeNode<Symbol> node : nodes)
                nodeStack.push((DerivationNode) node);
        }
        // This method must only ever be called on branches (not leaves), so
        //  must always return a size > 0
        assert size > 0 : size;
        return size;
    }

    /**
     * Given two individuals, whoses chromosomes are split into three codon
     * groups running from [0..point1), [point1..point1+length), and
     * [point1+length..usedGenes), named head, body, and tail respectively,
     * return a chromosome with a head and tail made up of individual-one's
     * chromosome, and individual-two's body, i.e., head(i1)+body(i2)+tail(i1).
     * The returned chromosome will be unwrapped and therefor may be larger
     * than either or both the two individuals.
     *
     * @param i1      The individual whose genotype will be the head and tail of the
     *                new chromosome
     * @param point1  The index of the body (excluded from new chromosome)
     * @param length1 The length of the body (excluded from new chromosome)
     * @param i2      The individual whose genotype will be the body of the new
     *                chromosome
     * @param point2  The index of the body (included from new chromosome)
     * @param length2 The length of the body (included from new chromosome)
     * @return A new chromosome made up of i1's head, i2's body, and i1's tail
     */
    public static GEChromosome makeNewChromosome(Individual i1, int point1, int length1, Individual i2,
                                                 int point2, int length2) {
        return makeNewChromosome(i1, point1, length1, i2, point2, length2, null);
    }

    public static GEChromosome makeNewChromosome(Individual i1, int point1, int length1, Individual i2,
                                                 int point2, int length2, DerivationTree tree) {
        //System.out.println("Making a new chromosome.");
        GEChromosome c1 = (GEChromosome) i1.getGenotype().get(0);
        GEChromosome c2 = (GEChromosome) i2.getGenotype().get(0);
        i1.map(0);
        i2.map(0);
        //System.out.println("c1: " + c1);
        //System.out.println("c2: " + c2);

        //System.out.println("From chrome 1: 0 to " + point1);
        //System.out.println("From chrome 2: " + point2 + " to " + (point2 + length2));
        //System.out.println("From chrome 1: " + point1 + " to " + (point1 + length1));

        // Create a chromosome big enough to take the head (point1),
        //  body (length2), and tail (c1.getUsedGenes() - (point1 + length1))
        int total = ((GEIndividual) i1).getUsedCodons() - length1 + length2;

        //System.out.println("total: " + total);

        //System.out.println("Length1: " + length1);
        //System.out.println("Length2: " + length2);
        //System.out.println("i1 used codons: " + ((GEIndividual)i1).getUsedCodons());
        //System.out.println("i2 used codons: " + ((GEIndividual)i2).getUsedCodons());

        GEChromosome c = new GEChromosome(total);
        c.setMaxChromosomeLength(c1.getMaxChromosomeLength());

        // Copy the start of c1, up to the removed body.
        //  Note. this unwraps the chromosome from c1
        for (int i = 0; i < point1; i++)
            c.add(c1.get(i % c1.size()));

        // Copy the body of c2.
        //  Note. this unwraps the chromosome from c2
        for (int i = point2; i < point2 + length2; i++)
            c.add(c2.get(i % c2.size()));

        // Copy the end of c1, from the removed body
        //  Note. this unwraps the chromosome from c1
        for (int i = point1 + length1; i < ((GEIndividual) i1).getUsedCodons(); i++)
            c.add(c1.get(i % c1.size()));

        // Update the number of used genes to match the copied protion
        c.setUsedGenes(total);

        /*
        Individual i = i1.clone();
        i.getGenotype().set(0, c);
        DerivationTree validTree = GenotypeHelper.buildDerivationTree(i);
        if(validTree != null)
            if(((GEChromosome)i.getGenotype().get(0)).getUsedGenes() != total)
            {   System.err.println("*** Mismatch ***" + ((GEChromosome)i.getGenotype().get(0)).getUsedGenes() + ", " + total + " :: " + c1.getUsedGenes() + " - " + length1 + " + " + length2);
                System.err.println(validTree);
                System.err.println("-------------");
                if(tree != null)
                    System.err.println(tree);
                System.err.println("-------------");
                System.err.println(c1);
                System.err.println(c);
                System.err.println(c2);
                System.err.println(point1 + ", " + point2 + ", " + length1 + ", " + length2);
                System.err.println("-------------");
                System.exit(0);
            }
        */

        assert c.size() == c.allocationSize() && c.size() == total
                : c.size() + "==" + c.allocationSize() + "==" + total;

//        System.out.format("c1: %d; c2: %d; c: %d, %d;%n", c1.allocationSize(), c2.allocationSize(), c.allocationSize(), c.size());

        return c;

    }

    /**
     * Given two individuals, whose chromosomes are split into three
     * codon groups running from [0..point1),
     * [point1..point1+length), and [point1+length..usedGenes), named
     * head, body, and tail respectively, return a chromosome with a
     * head and tail made up of individual-one's chromosome, and all
     * of individual-two, i.e., head(i1)+all(i2)+tail(i1).  The
     * returned chromosome will be unwrapped and therefor may be
     * larger than either or both the two individuals.
     *
     * @param i1     The individual whose genotype will be the head and tail of the
     *               new chromosome
     * @param point  The index of the body (excluded from new chromosome)
     * @param length The length of the body (excluded from new chromosome)
     * @param i2     The individual whose genotype will be the body of the new
     *               chromosome
     * @return A new chromosome made up of i1's head, all of i2, and i1's tail
     */
    public static GEChromosome makeNewChromosome(Individual i1, int point, int length, Individual i2,
                                                 DerivationTree tree) {
        i2.map(0);
        return makeNewChromosome(i1, point, length, i2, 0, ((GEIndividual) i2).getUsedCodons(), tree);
    }

    /**
     * Method to return the maximum codon index value in a derivation tree
     */
    public static int getMaxDTIndex(DerivationTree dT) {
        Iterator<TreeNode<Symbol>> nodeItr;
        TreeNode<Symbol> currItr = dT.getRoot();
        List<TreeNode<Symbol>> visitNodes = new LinkedList<TreeNode<Symbol>>();
        visitNodes.add(currItr);
        int maxIndex = 0;

        while (visitNodes.size() > 0) {
            currItr = visitNodes.remove(0);
            DerivationNode DN = (DerivationNode) currItr;

            if (DN.getCodonIndex() > maxIndex) {
                maxIndex = DN.getCodonIndex();
            }

            nodeItr = currItr.iterator();

            while (nodeItr.hasNext()) {
                visitNodes.add(nodeItr.next());
            }
        }
        return maxIndex;
    }

    /**
     * Compare the derivation trees for equality. I couldn't find a
     * method for this anywhere so I wrote on here. This is also
     * probably not the most efficient way of doing this. Maybe there
     * should be a .equals() method in DerivationNode or
     * DerivationTree?
     *
     * @return boolean Are the trees equal?
     */
    public static boolean derivationTreesEqual(DerivationTree t1, DerivationTree t2) {
        Iterator<TreeNode<Symbol>> nodeItr1;
        Iterator<TreeNode<Symbol>> nodeItr2;
        TreeNode<Symbol> currItr1 = t1.getRoot();
        TreeNode<Symbol> currItr2 = t2.getRoot();
        List<TreeNode<Symbol>> visitNodes1 = new LinkedList<TreeNode<Symbol>>();
        List<TreeNode<Symbol>> visitNodes2 = new LinkedList<TreeNode<Symbol>>();
        visitNodes1.add(currItr1);
        visitNodes2.add(currItr2);

        while (visitNodes1.size() > 0 && visitNodes2.size() > 0) {

            // If the length of the node lists are not the same,
            // return false
            if (visitNodes1.size() != visitNodes2.size()) {
                return false;
            }

            currItr1 = visitNodes1.remove(0);
            currItr2 = visitNodes2.remove(0);
            Symbol s1 = currItr1.getData();
            Symbol s2 = currItr2.getData();

            // It the tree symbols are not the same,
            // the derivation trees are not equal
            if (!s1.equals(s2)) {
                return false;
            }

            nodeItr1 = currItr1.iterator();
            nodeItr2 = currItr2.iterator();

            while (nodeItr1.hasNext()) {
                visitNodes1.add(nodeItr1.next());
            }
            while (nodeItr2.hasNext()) {
                visitNodes2.add(nodeItr2.next());
            }
        }

        return true;
    }

    /**
     * Return an array with all the indeces of recursive derivation
     * tree nodes.
     *
     * @return int[] The indeces of the recursive nodes
     */
    public static int[] getRecursiveNodeIndeces(DerivationTree dt, GEGrammar gram) {
        Iterator<TreeNode<Symbol>> nodeItr;
        TreeNode<Symbol> currItr = dt.getRoot();
        List<TreeNode<Symbol>> visitNodes = new LinkedList<TreeNode<Symbol>>();
        visitNodes.add(currItr);
        ArrayList<Integer> recIndexList = new ArrayList<Integer>();
        int[] recIndex;

        while (visitNodes.size() > 0) {

            currItr = visitNodes.remove(0);
            Symbol currentSym = currItr.getData();
            Rule currentRule = gram.findRule(currentSym);
            DerivationNode dN = (DerivationNode) currItr;
            nodeItr = currItr.iterator();

            // Don't check leaf nodes
            if (nodeItr.hasNext()
                    && currentSym.getType().equals(Enums.SymbolType.NTSymbol)) {

                // If the current node is recursive, it is an ancestor to the
                // unmapped non-terminal
                if (dN.getCodonIndex() != -1) {
                    if (currentRule.getRecursive()) {
                        recIndexList.add(dN.getCodonIndex());
                    }
                }
            }

            while (nodeItr.hasNext()) {
                visitNodes.add(nodeItr.next());
            }
        }

        recIndex = new int[recIndexList.size()];
        for (int i = 0; i < recIndex.length; i++) {
            recIndex[i] = (int) recIndexList.get(i);
        }

        return recIndex;
    }

    /**
     * @param indiv       Individual to extend
     * @param giver       GEChromosome used to extend the indiv
     * @param giverSym    Symbol of the node in the derivation tree that
     *                    marks the beginning of the chromosome being given
     * @param mutatePoint Point on indiv's chromosome that is getting
     *                    the new information
     * @return Individual Updated individual
     **/
    public static Individual extendChromeAtRecursiveNode(GEIndividual indiv, GEChromosome giver, Symbol giverSym,
                                                         int mutatePoint) {

        GEChromosome receiver = (GEChromosome) indiv.getGenotype().get(0);
        GEGrammar gram = (GEGrammar) indiv.getMapper();
        int receiverUsedCodons = receiver.getUsedGenes();
        int giverUsedCodons = giver.getUsedGenes();
        int[] receiverArr = receiver.toArray();
        int[] giverArr = giver.toArray();
        int[] newArr = new int[receiverUsedCodons + giverUsedCodons];

        if (mutatePoint == 0) {
            System.arraycopy(giverArr, 0, newArr, 0, giverUsedCodons);
            System.arraycopy(receiverArr, 0, newArr, giverUsedCodons, receiverUsedCodons);
        } else {
            System.arraycopy(receiverArr, 0, newArr, 0, mutatePoint);
            System.arraycopy(giverArr, 0, newArr, mutatePoint, giverUsedCodons);
            System.arraycopy(receiverArr, mutatePoint, newArr, (mutatePoint + giverUsedCodons),
                    (receiverUsedCodons - mutatePoint));
        }

        receiver.setAll(newArr);
        indiv.getGenotype().set(0, receiver);
        indiv.map(0);
        int tempCodon;

        if (numRecNodes(buildDerivationTree(indiv), gram) <= 1) {
            tempCodon = extendRecursiveNode(newArr[mutatePoint],
                    indiv,
                    giverSym);

            // After the chromosome has been remade,
            // make sure the right codon changed.
            if (tempCodon != newArr[mutatePoint]) {
                newArr[mutatePoint] = tempCodon;

                // Reset the chromosome with the changed codon
                receiver.setAll(newArr);
            }
        } else {
            int lastRecIndex = findLastRecIndex(indiv);
            tempCodon = extendRecursiveNode(newArr[lastRecIndex],
                    indiv,
                    giverSym);

            // After the chromosome has been remade,
            // make sure the right codon changed.
            if (tempCodon != newArr[lastRecIndex]) {
                newArr[lastRecIndex] = tempCodon;

                // Reset the chromosome with the changed codon
                receiver.setAll(newArr);
            }
        }

        indiv.getGenotype().set(0, receiver);
        return indiv;
    }

    /**
     * This method takes and individual and returns the codon index of
     * the last recurisve node in the derivation tree.
     *
     * @param indiv The individual to search
     * @return int Index of the last recursive derivation tree node
     */
    public static int findLastRecIndex(GEIndividual indiv) {
        GEGrammar gram = (GEGrammar) indiv.getMapper();
        DerivationTree tree = GenotypeHelper.buildDerivationTree(indiv);
        DerivationNode root = (DerivationNode) tree.getRoot();

        return findLastRecIndex(root, gram);
    }

    /**
     * This method takes a derivation node and a grammar and returns
     * the codon index of the last recurisve node in the derivation
     * tree. There may be recursive nodes after the returned index,
     * but they will be unmapped and have no index to return since
     * they didn't use a codon.
     *
     * @param dNode The derivation node to search
     * @param gram  The grammar to use for information about the rules
     * @return int Index of the last recursive derivation tree node
     */
    public static int findLastRecIndex(DerivationNode dNode, GEGrammar gram) {
        int lastRecIndex = -1;

        Iterator<TreeNode<Symbol>> nodeItr;
        TreeNode<Symbol> currItr = dNode;
        List<TreeNode<Symbol>> visitNodes = new LinkedList<TreeNode<Symbol>>();
        visitNodes.add(currItr);

        while (visitNodes.size() > 0) {
            currItr = visitNodes.remove(0);
            Symbol currSym = currItr.getData();

            if (currSym.getType().equals(Enums.SymbolType.NTSymbol)) {
                Rule currRule = gram.findRule(currSym);

                if (currRule.getRecursive()) {
                    DerivationNode node = (DerivationNode) currItr;

                    if (node.getCodonIndex() != -1) {
                        lastRecIndex = node.getCodonIndex();
                    }
                }
            }

            nodeItr = currItr.iterator();

            while (nodeItr.hasNext()) {
                visitNodes.add(nodeItr.next());
            }
        }
        return lastRecIndex;
    }

    /**
     * This method takes the codon that's going to change and
     * determines how it needs to change in order to map to the
     * correct production.
     *
     * @param codonToChange The original codon value
     * @param indiv         The parent individual that will be the basis for
     *                      remapping
     * @param childRootSym  The root symbol of the child that needs to
     *                      be picked up
     * @return int The new value of the codon so it will map to a
     * recursive production
     */
    public static int extendRecursiveNode(int codonToChange, Individual indiv,
                                          Symbol childRootSym) {

        GEGrammar gram = (GEGrammar) indiv.getMapper();
        Rule recRule = gram.findRule(childRootSym);
        boolean foundRecursiveRule = false;

        for (int i = 0; i < recRule.size(); i++) {

            Production p = recRule.get(i);
            Iterator<Symbol> symIt = p.iterator();

            while (symIt.hasNext()) {
                Symbol newSym = symIt.next();

                // Only worry about non terminals
                if (newSym.getType().equals(Enums.SymbolType.NTSymbol)) {
                    Rule newRule = gram.findRule(newSym);

                    // If the current symbol in the production is the same as the
                    // given recursive rule
                    if (newSym.equals(childRootSym)) {
                        foundRecursiveRule = true;
                        break;
                    }
                    // OR if the current symbol is a different recursive rule
                    else if (newRule.getRecursive()) {
                        foundRecursiveRule = true;
                        break;
                    }
                }
            }

            if (foundRecursiveRule) {
                int modValDiff = Math.abs((codonToChange % recRule.size()) - i);

                if (codonToChange - modValDiff > 0) {
                    codonToChange -= modValDiff;
                } else {
                    codonToChange += modValDiff;
                }

                return codonToChange;
            }
        }
        return codonToChange;
    }

    /**
     * This method takes a derivation tree and a grammar and returns
     * the number of recursive nodes in that derivation tree.
     *
     * @param tree Derivation tree to search
     * @param gram Grammar corresponding to the derivation tree
     * @return int Number of recursive nodes in the derivation tree
     */
    public static int numRecNodes(DerivationTree tree, GEGrammar gram) {
        int numRecNodes = 0;
        Iterator<TreeNode<Symbol>> nodeItr;
        TreeNode<Symbol> currItr = tree.getRoot();
        List<TreeNode<Symbol>> visitNodes = new LinkedList<TreeNode<Symbol>>();
        visitNodes.add(currItr);

        while (visitNodes.size() > 0) {
            currItr = visitNodes.remove(0);
            Symbol currSym = currItr.getData();

            if (currSym.getType().equals(Enums.SymbolType.NTSymbol)) {
                Rule currRule = gram.findRule(currSym);

                if (currRule.getRecursive()) {
                    numRecNodes++;
                }
            }

            nodeItr = currItr.iterator();

            while (nodeItr.hasNext()) {
                visitNodes.add(nodeItr.next());
            }
        }
        return numRecNodes;
    }

    /**
     * This method is used to compare two different derivation nodes
     * to see at which index in the tree, they differ. Returns the
     * index of the derivation node in n1 where the nodes are
     * different and -1 if the nodes are equal.
     */
    public static int nodesDifferentAtIndex(DerivationNode n1, DerivationNode n2) {
        int diffIndex = -1;

        Iterator<TreeNode<Symbol>> nodeIt1;
        Iterator<TreeNode<Symbol>> nodeIt2;
        TreeNode<Symbol> currIt1 = n1;
        TreeNode<Symbol> currIt2 = n2;
        List<TreeNode<Symbol>> visitNodes1 = new LinkedList<TreeNode<Symbol>>();
        List<TreeNode<Symbol>> visitNodes2 = new LinkedList<TreeNode<Symbol>>();
        visitNodes1.add(currIt1);
        visitNodes2.add(currIt2);

        while (visitNodes1.size() > 0 && visitNodes2.size() > 0) {
            currIt1 = visitNodes1.remove(0);
            currIt2 = visitNodes2.remove(0);

            Symbol currSym1 = currIt1.getData();
            Symbol currSym2 = currIt2.getData();

            System.out.println(currSym1 + " ---> " + currSym2);

            if (currSym1.equals(currSym2)) {
                diffIndex = ((DerivationNode) currIt1).getCodonIndex();
                System.out.println("Diff index: " + diffIndex);
            } else {
                return diffIndex;
            }

            nodeIt1 = currIt1.iterator();
            nodeIt2 = currIt2.iterator();

            while (nodeIt1.hasNext()) {
                visitNodes1.add(nodeIt1.next());
            }
            while (nodeIt2.hasNext()) {
                visitNodes2.add(nodeIt2.next());
            }
        }

        return diffIndex;
    }

    public static ArrayList<Integer> getDerivationTreeIndeces(DerivationTree dT) {
        ArrayList<Integer> indeces = new ArrayList<Integer>();

        Iterator<TreeNode<Symbol>> nodeIt;
        TreeNode<Symbol> currIt = dT.getRoot();
        List<TreeNode<Symbol>> visitNodes = new LinkedList<TreeNode<Symbol>>();
        visitNodes.add(currIt);

        while (visitNodes.size() > 0) {

            currIt = visitNodes.remove(0);
            int index = ((DerivationNode) currIt).getCodonIndex();

            if (index > -1)
                indeces.add(index);

            nodeIt = currIt.iterator();
            while (nodeIt.hasNext()) {
                visitNodes.add(nodeIt.next());
            }
        }

        return indeces;
    }

    public static Phenotype getNodePhenotype(DerivationNode node) {
        Phenotype phen = new Phenotype();

        Stack<DerivationNode> nodes = new Stack<DerivationNode>();
        nodes.push(node);
        DerivationNode dN;

        while (!nodes.empty()) {

            dN = nodes.pop();
            Symbol sym = dN.getData();

            if (sym.getType().equals(Enums.SymbolType.TSymbol))
                phen.add(sym);

            final ListIterator<TreeNode<Symbol>> nodeIt = dN.listIterator(dN.size());
            while (nodeIt.hasPrevious()) {
                final DerivationNode currNode = (DerivationNode) nodeIt.previous();
            }
        }

        return phen;
    }
}