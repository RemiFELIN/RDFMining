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
Grammatical Evolution in Java
Release: GEVA-v1.0.zip
Copyright (C) 2008 Michael O'Neill, Erik Hemberg, Anthony Brabazon, Conor Gilligan 
Contributors Patrick Middleburgh, Eliott Bartley, Jonathan Hugosson, Jeff Wrigh

Separate licences for asm, bsf, antlr, groovy, jscheme, commons-logging, jsci is included in the lib folder. 
Separate licence for rieps is included in src/com folder.

This licence refers to GEVA-v1.0.

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
*/

/*
 * IntFlipMutation.java
 *
 * Created on 15 March 2007, 20:05
 *
 */

package com.i3s.app.rdfminer.evolutionary.geva.Operator.Operations;

import com.i3s.app.rdfminer.evolutionary.geva.Individuals.GEChromosome;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.GEIndividual;
import com.i3s.app.rdfminer.evolutionary.geva.Operator.mutation.MutationOperation;
import com.i3s.app.rdfminer.evolutionary.geva.Util.Random.RandomNumberGenerator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * IntFlipMutation does integer mutation. This class also record the
 * average fitness change for the mutation at a chromosomal level
 * @author Conor
 */
public class IntFlipFitness extends MutationOperation {
    
    /** Creates a new instance of IntFlipMutation
     * @param prob mutation probability
     * @param rng random number generator
     */
    public IntFlipFitness(double prob, RandomNumberGenerator rng) {
        super(prob, rng);
    }

    /**
     * New instance
     * @param rng random number generator
     * @param p properties
     */
    public IntFlipFitness(RandomNumberGenerator rng, Properties p) {
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
    public void doOperation(GEIndividual operand) {
        //doMutation((GEChromosome)operand.getGenotype().get(0));        
	    doMutation(operand);
	    operand.invalidate();
    }
    
    /**
     * According to this.probability a codon in the chromosome is  
     * replaced with a new randomly chosen integer
     */
    private void doMutation(GEIndividual operand) {
        GEChromosome c = (GEChromosome)operand.getGenotype().get(0);
        operand.setMapped(false);
        operand.map(0); //map original genome
        fitnessFunction.getFitness(operand);
	    double fitnessBefore =operand.getFitness().getDouble();
	    //System.out.println("fitnessBefore: "+fitnessBefore);
	    // PERFORM MUTATION
        for(int i=0;i<c.getLength();i++) {
            if(this.rng.nextBoolean(this.probability)) {
                c.set(i, Math.abs(rng.nextInt()));
            }
        }
	    // Record change from mutation
        operand.setMapped(false);
	    operand.map(0); //map mutated chromosome
	    fitnessFunction.getFitness(operand); //get fitness of mutated child
	    double fitnessAfter =operand.getFitness().getDouble();
        double fitnessChange = 0;
        int fitnessImprovement =0;
	    //System.out.println("fitnessAfter: "+fitnessAfter);
        // If the individual is not invalid
        if ((fitnessAfter != 100000000) && (fitnessBefore != 100000000)) {
	        if (fitnessBefore < fitnessAfter) fitnessImprovement = -1;
	        else if (fitnessBefore > fitnessAfter) fitnessImprovement = 1;
            fitnessChange = Math.abs(fitnessBefore - fitnessAfter);
        } else if((fitnessAfter == 100000000)) {
            fitnessChange = 0;
            fitnessImprovement = -2;
        }
        else {
            fitnessChange = 0;
            fitnessImprovement = -3;
        }
        //write it out to a file
	    try {
            BufferedWriter out = new BufferedWriter(new FileWriter("intflipFitnessChanges.dat",true));
            out.write(fitnessChange +", "+ fitnessImprovement);
            out.newLine();
            out.close();
	    } catch (IOException ignored) {}
    }
    
    
}
