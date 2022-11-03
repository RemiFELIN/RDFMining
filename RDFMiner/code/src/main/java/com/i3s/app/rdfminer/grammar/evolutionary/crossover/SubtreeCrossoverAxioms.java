package com.i3s.app.rdfminer.grammar.evolutionary.crossover;

import com.i3s.app.rdfminer.grammar.evolutionary.GenotypeHelper;
import com.i3s.app.rdfminer.grammar.evolutionary.individual.GEIndividual;

import Individuals.GEChromosome;
import Mapper.DerivationNode;
import Mapper.DerivationTree;
import Operator.Operations.ContextSensitiveOperations.SubtreeCrossover;
//import Util.GenotypeHelper;
import Util.Random.RandomNumberGenerator;

/**
 * 
 * @author NGUYEN Thu Huong
 *
 */
public class SubtreeCrossoverAxioms extends SubtreeCrossover {

	public SubtreeCrossoverAxioms(double prob, RandomNumberGenerator rng) {
		super(prob, rng);
	}

	public GEIndividual[] crossoverTree(GEIndividual parent1, GEIndividual parent2) {
		GEIndividual i1, i2;
		i1 = new GEIndividual(parent1);
		i2 = new GEIndividual(parent2);

		GEIndividual[] childs = new GEIndividual[2];

		// Turn the genotype into a tree. The tree nodes will state which
		// codons each branch used to determine its child production
		DerivationTree tree1 = GenotypeHelper.buildDerivationTree(i1);
		DerivationTree tree2 = GenotypeHelper.buildDerivationTree(i2);
		// System.out.println("Tree1 " + tree1);
		// System.out.println("Tree2 " + tree2);
		// Don't operate on invalids
		if (tree1 != null && tree2 != null && (this.rand.nextDouble() < this.probability)) {
			// Only crossover based on a probability that a crossover should occur
			// Helper: get the chromosomes
			GEChromosome chromosome1 = (GEChromosome) i1.getGenotype().get(0);
			GEChromosome chromosome2 = (GEChromosome) i2.getGenotype().get(0);

			// Prepare to pick one of the branches in individual 1
			boolean[] wasPicked = new boolean[chromosome1.getUsedGenes()];
			int pickCount = wasPicked.length;
			DerivationNode node1;
			DerivationNode node2 = null;
			int point1 = 0;
			int point2 = 0;
			int length1 = 0;
			int length2 = 0;

			// Loop until a node is found in both individuals that can be swapped.
			// This should eventually find something always, because they all share
			// the root (even if the root uses no codons to go to a single child,
			// then they'll all share that same child)
			// node2 == null means a branch found in node1 was not found in node2
			// length1 == 0
			while (node2 == null) {

				assert pickCount > 0 && pickCount <= chromosome1.getUsedGenes() : pickCount;
				// Pick random points from the genotype for crossover
				// The random point chosen from individual 2 is a only a starting
				// point from which a matching type will be searched. The search
				// runs right and left of point2 evenly on both sides to find the
				// nearest type that matches

				// OLD
				// point1 = super.getRNG().nextInt(pickCount);
				// OLD
				// point2 = super.getRNG().nextInt(chromosome2.getUsedGenes());
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
				// Each time a codon is chosen, the total number of codons which
				// can be chosen is decreased and the chosen codon is flagged as
				// used. The chosen codon index will always be in the range
				// [0..pickCount) but the codons themselves will be split across
				// the range of all codons. This loop maps from 0..pickCount to
				// actual codon by skipping over previously picked codons
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
				// point was chosen from the list of used codons, this will always
				// find the node
				node1 = GenotypeHelper.findNodeFromCodonIndex(tree1, point1);

				// Find the tree-node nearest the chosen crossover point that has
				// the same type, so that the crossover will map into it
				node2 = findRelatedNode(tree2, point2, maxPoint2, node1);

				// If the point is found that is the same type..
				if (node2 != null) { // The chosen point is probably wrong, change it to the correct
										// value
					point2 = node2.getCodonIndex();
					// Calculate how many codons make up the sub-tree of the chosen
					// branch
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

			i1.invalidate();
			i2.invalidate();
		}

		// System.out.println("i2= " + ListChild[1]);
		int[] Mutationpoint1 = new int[1];
		int[] Mutationpoint2 = new int[1];
		if (tree1 != null) {
			i1.map(0);
			// DerivationTree tree3 = GenotypeHelper.buildDerivationTree(i1);
			// System.out.println("Tree3 " + tree3);
		} else {
			Mutationpoint1[0] = (int) i2.getGenotype().get(0).getLength() / 2;
			// System.out.println("fail tree!!!");
		}
		i1.setMutationPoints(Mutationpoint1);

		if (tree2 != null) {
			i2.map(0);
			// DerivationTree tree4 = GenotypeHelper.buildDerivationTree(i2);
			// System.out.println("Tree4 " + tree4);
		} else {
			Mutationpoint2[0] = i2.getGenotype().get(0).getLength() / 2;

		}
		i2.setMutationPoints(Mutationpoint2);

		childs[0] = i1;
		// System.out.println("ListChild i1= " + ListChild[0]);
		childs[1] = i2;
		/*
		 * System.out.println("Child 1 mapped " + Boolean.toString(i1.isMapped()));
		 * System.out.println("Child 2 mapped " + Boolean.toString(i2.isMapped()));
		 * System.out.println("i1= " + i1); System.out.println("i2= " + i2);
		 */
		return childs;

	}

	private DerivationNode findRelatedNode(DerivationTree tree, int codonIndex, int codonTotal,
			DerivationNode relatedNode) {

		DerivationNode node;
		int offset = 0;
		boolean Continue = true;

		while (Continue) {

			Continue = false;
			if (codonIndex + offset < codonTotal) {
				node = GenotypeHelper.findNodeFromCodonIndex(tree, codonIndex + offset);
				if (node.getData().equals(relatedNode.getData()))
					return node;
				Continue = true;
			}

			if (offset != 0 && codonIndex - offset >= 0) {
				node = GenotypeHelper.findNodeFromCodonIndex(tree, codonIndex - offset);
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
