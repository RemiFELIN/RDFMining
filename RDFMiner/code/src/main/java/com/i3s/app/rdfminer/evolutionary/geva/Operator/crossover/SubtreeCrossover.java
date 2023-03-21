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

package com.i3s.app.rdfminer.evolutionary.geva.Operator.crossover;

import com.i3s.app.rdfminer.evolutionary.geva.Individuals.GEChromosome;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.GEIndividual;
import com.i3s.app.rdfminer.evolutionary.geva.Mapper.DerivationNode;
import com.i3s.app.rdfminer.evolutionary.geva.Mapper.DerivationTree;
import com.i3s.app.rdfminer.evolutionary.geva.Util.GenotypeHelper;
import com.i3s.app.rdfminer.evolutionary.geva.Util.Random.RandomNumberGenerator;
import com.i3s.app.rdfminer.launcher.GrammaticalEvolution;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Properties;

/**
 * Preform a crossover where the branch of one tree crosses with the branch of
 * another. The two branches must be of the same type so the chromosome's
 * codons will build the same branch on both trees. The crossover chromosome,
 * because it potentially can insert a small unique group of codons into a
 * wrapped chromosome, unwraps the chromosomes as they are crossed-over. This
 * also changes the size of the chromosome, even if no unwrapping occurs,
 * shrinking the chromosome
 *
 * @author eliott bartley
 */
public class SubtreeCrossover extends CrossoverOperation {

    private static final Logger logger = Logger.getLogger(SubtreeCrossover.class.getName());

    public SubtreeCrossover() { super(); }

    public SubtreeCrossover(RandomNumberGenerator rng, Properties p) {
        super(rng, p);
    }

    public SubtreeCrossover(double prob, RandomNumberGenerator rng) {
        super(prob, rng);
    }

    public SubtreeCrossover(RandomNumberGenerator rng, double prob) {
        super(prob, rng);
    }

    @Override
    public void doOperation(List<GEIndividual> operands) {
        assert operands.size() >= 2 : operands.size();
        GEIndividual i1 = operands.get(0);
        GEIndividual i2 = operands.get(1);
        // Only crossover based on a probability that a crossover should occur
        if (this.rand.nextDouble() >= this.probability)
            return;

        // increase crossover counter
        GrammaticalEvolution.nCrossover++;
//        logger.info("~ perform crossover between " + i1.getGenotype() + " and " + i2.getGenotype());
        // Turn the genotype into a tree. The tree nodes will state which
        //  codons each branch used to determine its child production
        DerivationTree tree1 = GenotypeHelper.buildDerivationTree(i1);
        DerivationTree tree2 = GenotypeHelper.buildDerivationTree(i2);

        // Don't operate on invalids
        if (tree1 == null || tree2 == null)
            return;

        // Helper: get the chromosomes
        GEChromosome chromosome1 = (GEChromosome) i1.getGenotype().get(0);
        GEChromosome chromosome2 = (GEChromosome) i2.getGenotype().get(0);

        // Prepare to pick one of the branches in individual 1
        boolean[] wasPicked = new boolean[chromosome1.getUsedGenes()];
        int pickCount = wasPicked.length;
        DerivationNode node1, node2 = null;
        int point1 = 0;
        int point2 = 0;
        int length1 = 0;
        int length2 = 0;

        // Loop until a node is found in both individuals that can be swapped.
        //  This should eventually find something always, because they all share
        //  the root (even if the root uses no codons to go to a single child,
        //  then they'll all share that same child)
        // node2 == null means a branch found in node1 was not found in node2
        // length1 == 0
        while (node2 == null) {

            assert pickCount > 0 && pickCount <= chromosome1.getUsedGenes()
                    : pickCount;

            // Pick random points from the genotype for crossover
            //  The random point chosen from individual 2 is a only a starting
            //  point from which a matching type will be searched. The search
            //  runs right and left of point2 evenly on both sides to find the
            //  nearest type that matches

            //OLD
            //point1 = super.getRNG().nextInt(pickCount);
            // OLD
            //point2 = super.getRNG().nextInt(chromosome2.getUsedGenes());
            int maxPoint1 = GenotypeHelper.getMaxDTIndex(tree1);
            if (maxPoint1 == 0) {
                point1 = 0;
            } else {
                if (maxPoint1 < pickCount) {
                    point1 = super.getRNG().nextInt(maxPoint1);
                } else {
                    point1 = super.getRNG().nextInt(pickCount);
                }
            }

            int maxPoint2 = GenotypeHelper.getMaxDTIndex(tree2);
            if (maxPoint2 == 0) {
                point2 = 0;
            } else {
                point2 = super.getRNG().nextInt(maxPoint2);
            }

            assert point1 >= 0 && point1 < pickCount : point1;
            assert point2 >= 0 && point2 < chromosome2.getUsedGenes() : point2;

            // Only pick new codons from the individual each loop around
            //  Each time a codon is chosen, the total number of codons which
            //  can be chosen is decreased and the chosen codon is flagged as
            //  used. The chosen codon index will always be in the range
            //  [0..pickCount) but the codons themselves will be split across
            //  the range of all codons. This loop maps from 0..pickCount to
            //  actual codon by skipping over previously picked codons
            for (int pickIndex = 0; pickIndex < wasPicked.length; pickIndex++)
                if (!wasPicked[pickIndex])
                    if (point1 == 0) {
                        wasPicked[pickIndex] = true;
                        point1 = pickIndex;
                        pickIndex = wasPicked.length;
                    } else
                        point1--;
            pickCount--;

            assert point1 >= 0 && point1 < chromosome1.getUsedGenes() : point1;

            // Find the tree-node related to the chosen crossover point. As the
            //  point was chosen from the list of used codons, this will always
            //  find the node
            node1 = GenotypeHelper.findNodeFromCodonIndex(tree1,
                    point1);

            // Find the tree-node nearest the chosen crossover point that has
            //  the same type, so that the crossover will map into it
            node2 = findRelatedNode(tree2,
                    point2,
                    maxPoint2,
                    node1);

            // If the point is found that is the same type..
            if (node2 != null) {   // The chosen point is probably wrong, change it to the correct
                //  value
                point2 = node2.getCodonIndex();
                // Calculate how many codons make up the sub-tree of the chosen
                //  branch
                length1 = GenotypeHelper.calcNodeLength(node1);
                length2 = GenotypeHelper.calcNodeLength(node2);
            }

        }

        assert point1 + length1 <= chromosome1.getUsedGenes()
                : point1 + "+" + length1 + ":" + chromosome1.getUsedGenes();
        assert point2 + length2 <= chromosome2.getUsedGenes()
                : point2 + "+" + length2 + ":" + chromosome2.getUsedGenes();

        // Do the crossover, creating new chromosomes in the process
        chromosome1 = GenotypeHelper.makeNewChromosome(i1, point1, length1, i2, point2, length2);
        chromosome2 = GenotypeHelper.makeNewChromosome(i2, point2, length2, i1, point1, length1);

        // Update the individuals with the new crossed-over chromosomes
        i1.getGenotype().set(0, chromosome1);
        i2.getGenotype().set(0, chromosome2);
        i1.getMapper().setGenotype(chromosome1);
        i2.getMapper().setGenotype(chromosome2);

        ((GEIndividual) i1).invalidate();
        ((GEIndividual) i2).invalidate();

        i1.map(0);
        i2.map(0);
    }

// All my debugging stuff pulled out of the actual code, in case I need it again
//        String head = String.format("**DEBUG ((%d, %d), (%d, %d))", point1, length1, point2, length2);
//        dump("old", "1: " + head, chromosome1.toString());
//        dump("new", "1: " + head, chromosome1.toString());
//        dump("old", "2: " + head, chromosome2.toString());
//        dump("new", "2: " + head, chromosome2.toString());
//        dump("old", "1: " + head, tree1.toString());
//        tree1 = buildDerivationTree(i1);
//        dump("new", "1: " + head, tree1.toString());
//        dump("old", "2: " + head, tree2.toString());
//        tree2 = buildDerivationTree(i2);
//        dump("new", "2: " + head, tree2.toString());
//    private void dump(String name, String head, String output)
//    {   try {
//        java.io.FileWriter writer = new FileWriter("c:/" + name, true);
//        writer.write("\r\n\r\n" + head + "\r\n\r\n");
//        writer.write(output.replaceAll("\n", "\r\n"));
//        writer.close();
//        } catch(IOException e) {}
//    }

    public void doOperation(GEIndividual operand) {
    }


    /**
     * Given a derivation tree, a start search index, and total search range,
     * and a node, find a node in the tree, nearest to the start index (nearest
     * based on the codon index, not nearest in the tree), that matches the
     * node (based on their symbol). The search alternates between right and
     * left of the start index incrementing away either side up until the total
     * range (right) or 0 (left) is reached, then only searching the remaining
     * unsearched indexes on the other side
     *
     * @param tree        The tree to search
     * @param codonIndex  The index of the codon to begin the search at
     * @param codonTotal  The total number of useful codons
     * @param relatedNode The node whose symbol is to be matched in the tree
     * @return null if no node is found that has the same symbol as the
     * relatedNode
     */
    private DerivationNode findRelatedNode(DerivationTree tree,
                                           int codonIndex,
                                           int codonTotal,
                                           DerivationNode relatedNode) {

        DerivationNode node;
        int offset = 0;
        boolean Continue = true;

        while (Continue) {

            Continue = false;
            if (codonIndex + offset < codonTotal) {
                node = GenotypeHelper.findNodeFromCodonIndex(tree,
                        codonIndex + offset);
                if (node.getData().equals(relatedNode.getData()))
                    return node;
                Continue = true;
            }

            if (offset != 0 && codonIndex - offset >= 0) {
                node = GenotypeHelper.findNodeFromCodonIndex(tree,
                        codonIndex - offset);
                if (node.getData().equals(relatedNode.getData()))
                    return node;
                Continue = true;
            }

            if (codonIndex + offset == 0 && codonTotal == 0) {
                node = GenotypeHelper.findNodeFromCodonIndex(tree, 0);
                if (node.getData().equals(relatedNode.getData()))
                    return node;
                Continue = true;
            }

            offset++;
        }

        return null;
    }

}
