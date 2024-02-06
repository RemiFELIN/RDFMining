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

package com.i3s.app.rdfminer.evolutionary.geva.Operator;

import com.i3s.app.rdfminer.evolutionary.geva.Exceptions.BadParameterException;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.GEIndividual;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.Individual;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.Populations.SimplePopulation;
import com.i3s.app.rdfminer.evolutionary.geva.Mapper.GEGrammar;
import com.i3s.app.rdfminer.evolutionary.geva.Operator.Operations.CreationOperation;
import com.i3s.app.rdfminer.evolutionary.geva.Operator.Operations.FullInitialiser;
import com.i3s.app.rdfminer.evolutionary.geva.Operator.Operations.GrowInitialiser;
import com.i3s.app.rdfminer.evolutionary.geva.Util.Constants;
import com.i3s.app.rdfminer.evolutionary.geva.Util.Random.RandomNumberGenerator;

import java.util.List;
import java.util.Properties;

/**
 * Class for performing ramped full and grow initialization. 
 * The population is divided(ramped) between minimum depth(shortest possible depth of a derivationTree)
 * and maximum depth(Maximum allowed depth of a derivationTree). 
 * For each individual the full or the grow operation is chosen.
 */
public class RampedHalfAndHalfInitialiser extends Initialiser {
    
    protected CreationOperation growInitialisation;
    protected double growProb;
    protected int currentDepth;
    protected int maxDepth;
    protected int depthInterval;

    /**
         * New instance
         * @param rng random number generator
         * @param size size
         * @param op operation
         * @param op2 operation
         * @param growProb probability of chosing grow operation
         * @param maxDepth max depth of initalised trees
         */
    public RampedHalfAndHalfInitialiser(RandomNumberGenerator rng, int size, CreationOperation op, CreationOperation op2, double growProb, int maxDepth) {
        super(rng, size, op);
        this.growProb = growProb;
        this.currentDepth = 0;
        this.maxDepth = maxDepth;
        this.population = new SimplePopulation();
        this.growInitialisation = op2;
        for(int i=0; i<size; i++) {
            this.population.add(this.operation.createIndividual());
        }
    }

    /**
     * New instance
     */
    public RampedHalfAndHalfInitialiser() {
        super();
    }

    @Override
    public void setProperties(Properties p) {
        super.setProperties(p);
        double value;
        String key = Constants.GROW_PROBABILITY;
        try {
            value = Double.parseDouble(p.getProperty(key));
            if(value < 0.0 || value > 1.0) {
                throw new BadParameterException(key);
            }
        } catch(Exception e) {
            value = 0.5;
            p.setProperty(key, Double.toString(value));
            System.out.println(e+" using default: "+value);
        }
        this.growProb = value;
	    int valueI  ;
        key = Constants.MAX_DEPTH;
        try {
            valueI = Integer.parseInt(p.getProperty(key));
            if(valueI < 1) {
                throw new BadParameterException(key);
            }
        } catch(Exception e) {
            valueI = 10;
            p.setProperty(key, Integer.toString(valueI));
            System.out.println(e+" using default: "+valueI);
        }
        this.maxDepth = valueI;
    }

    /**
     * The population is divided(ramped) between minimum depth(shortest possible depth of a derivationTree)
     * and maximum depth(Maximum allowed depth of a derivationTree). 
     * For each individual the full or the grow operation is chosen.
     */
    @Override
    public void perform() {
        this.depthInterval = getDepthInterval(this.size);
        int i = 0;
        GEIndividual individual;
        while(i<this.size) {
            individual = (GEIndividual) this.population.get(i);
            if(i%this.depthInterval==0) { //ramp up the depth
                ((FullInitialiser)operation).setMaxDepth(this.currentDepth);
                ((GrowInitialiser)growInitialisation).setMaxDepth(this.currentDepth);
                this.currentDepth++;
            }
            // Choose full or grow
            if(getRNG().nextDouble() <= growProb) {
                growInitialisation.doOperation(individual);
            } else {
                operation.doOperation(individual);
            }
            i++;
            //Insert check for not having identical individuals in initialisation!!??
            //System.out.println(individual.getGenotype().get(0));
        }
    }
    
    /**
     * Calculates how the population should be partitioned between the different
     * initialisation depths
     * @param populationSize Size of the population
     * @return The interval for each depth
     **/
    public int getDepthInterval(int populationSize) {
        Individual individual = this.population.get(0);
        int depthInt;
        if(currentDepth < ((GEGrammar)individual.getMapper()).findRule(((GEGrammar)individual.getMapper()).getStartSymbol()).getMinimumDepth()) {
            //System.out.println("Min depth to small, must be larger then the minDepth of start symbol.");
            this.currentDepth = ((GEGrammar)individual.getMapper()).findRule(((GEGrammar)individual.getMapper()).getStartSymbol()).getMinimumDepth();
        }
        if(currentDepth > maxDepth) {
            System.out.println("Max depth to small, must be larger then minDepth.");
            maxDepth = currentDepth;
        }
        depthInt = populationSize/(maxDepth - currentDepth + 1);
        //        System.out.println(depthInterval+"="+populationSize+"/("+maxDepth+"-"+currentDepth+"+1)");
        if(depthInt < 1) {
            System.out.println("Too small population for initialisation on all depths");
            depthInt = 1;
        }
        return depthInt;
    }

    /**
     * First item in collection is set as fullInitalion. 
     * Second item is set as growInitialisation.
     * @param ops List of the operations to be set
     */
    public void setOperations(List<CreationOperation> ops) {
        this.operation = ops.get(0);
        this.growInitialisation = ops.get(1);
    }

 }