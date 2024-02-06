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

package com.i3s.app.rdfminer.evolutionary.geva.Operator.mutation;

import com.i3s.app.rdfminer.evolutionary.geva.Individuals.GEChromosome;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.GEIndividual;
import com.i3s.app.rdfminer.evolutionary.geva.Mapper.ContextualDerivationTree;
import com.i3s.app.rdfminer.evolutionary.geva.Mapper.GEGrammar;
import com.i3s.app.rdfminer.evolutionary.geva.Util.Random.RandomNumberGenerator;
import com.i3s.app.rdfminer.launcher.GrammaticalEvolution;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * This will find if the chromosome contains a expandable leaf node
 * and will mutate it depending on the mutation probability
 */
public class NodalMutation extends MutationOperation {

    private static final Logger logger = Logger.getLogger(NodalMutation.class.getName());

    public NodalMutation() { super(); }

    public NodalMutation(RandomNumberGenerator rng, Properties p) {
        super(rng, p);
    }

    public NodalMutation(double prob, RandomNumberGenerator rng) {
        super(prob, rng);
    }

    public NodalMutation(RandomNumberGenerator rng, double prob) {
        super(prob, rng);
    }

    @Override
    public void doOperation(GEIndividual operand) {
        GEChromosome chromosome = (GEChromosome) operand.getGenotype().get(0);
        ContextualDerivationTree tree = new ContextualDerivationTree((GEGrammar) operand.getMapper(), chromosome);
        tree.buildDerivationTree();
        // this is to check that the individual is not invalid
        // This vector contains the index values for all the leaf node codons
        ArrayList<Integer> nodeCodonList = new ArrayList(tree.getNodeCodonList());
        //iterate through the leaf Node codons and mutate depending on probability
        for (int codonIndex : nodeCodonList) {
            if (this.rng.nextBoolean(this.probability)) {
//                logger.info("~ perform mutation");
                // increase mutation counter
                GrammaticalEvolution.nMutation++;
                chromosome.set(codonIndex-1, Math.abs(rng.nextInt()));
            }
        }
        operand.invalidate();
    }

    @Override
    public void doOperation(List<GEIndividual> operands) {
        for (GEIndividual operand : operands)
            doOperation(operand);
    }
}
