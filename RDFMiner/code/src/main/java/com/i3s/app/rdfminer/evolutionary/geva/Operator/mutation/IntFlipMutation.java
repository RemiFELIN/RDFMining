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

package com.i3s.app.rdfminer.evolutionary.geva.Operator.mutation;

import com.i3s.app.rdfminer.evolutionary.geva.Individuals.GEChromosome;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.GEIndividual;
import com.i3s.app.rdfminer.evolutionary.geva.Util.Random.RandomNumberGenerator;
import com.i3s.app.rdfminer.launcher.GrammaticalEvolution;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Properties;

/**
 * IntFlipMutation does integer mutation
 * @author Conor
 */
public class IntFlipMutation extends MutationOperation {

    private static final Logger logger = Logger.getLogger(IntFlipMutation.class.getName());

    public IntFlipMutation() { super(); }

    /** Creates a new instance of IntFlipMutation
     * @param prob mutation probability
     * @param rng random number generator
     */
    public IntFlipMutation(double prob, RandomNumberGenerator rng) {
        super(prob, rng);
    }

    public IntFlipMutation(RandomNumberGenerator rng, double prob) {
        super(prob, rng);
    }

    /**
     * New instance
     * @param rng random number generator
     * @param p properties
     */
    public IntFlipMutation(RandomNumberGenerator rng, Properties p) {
        super(rng, p);
    }
    
    public void doOperation(List<GEIndividual> operands) {
        for (GEIndividual operand : operands) {
            this.doOperation(operand);
        }
    }
    
    /**
     * Calls doMutation(GEIndividual c) and then calls Individual.invalidate()
     * @param operand operand to operate on
     */
    public void doOperation(final GEIndividual operand) {
        doMutation((GEChromosome)operand.getGenotype().get(0));
        operand.invalidate();
    }
    
    /**
     * According to this.probability a codon in the chromosome is  
     * replaced with a new randomly chosen integer
     * @param c input to mutate
     */
    private void doMutation(final GEChromosome c) {
        for(int i=0;i<c.getLength();i++) {
            if(this.rng.nextBoolean(this.probability)) {
//                logger.info("~ perform mutation");
                // increase mutation counter
                GrammaticalEvolution.nMutation++;
                final int nextInt = Math.abs(rng.nextInt());
                c.set(i, nextInt);
            }
        }
    }
    
    
}
