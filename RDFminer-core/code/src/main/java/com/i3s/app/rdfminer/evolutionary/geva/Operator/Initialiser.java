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
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.Populations.Population;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.Populations.SimplePopulation;
import com.i3s.app.rdfminer.evolutionary.geva.Operator.Operations.CreationOperation;
import com.i3s.app.rdfminer.evolutionary.geva.Operator.Operations.Operation;
import com.i3s.app.rdfminer.evolutionary.geva.Util.Constants;
import com.i3s.app.rdfminer.evolutionary.geva.Util.Random.RandomNumberGenerator;

import java.util.Properties;


/**
 * Initialiser has a CreationOperation and is used to create a population.
 * The population is created by init(). 
 * perform() calls the operation to add codons to the population.
 * The constructor calls createIndividual() to generate a population.
 */
public class Initialiser extends SourceModule implements Creator{
    
    protected CreationOperation operation;
    
    /**
     * Initialiser creates the population
     * @param rng random number generator
     * @param size size
     * @param op creation operation
     */
    public Initialiser(RandomNumberGenerator rng, int size, CreationOperation op) {
        super(rng, size);
        this.operation = op;
        this.init();
    }
    
    /**
     * Initialiser creates the population
     * @param rng random number generator
     * @param op creation operation
     * @param p properties
     */
        public Initialiser(RandomNumberGenerator rng, CreationOperation op, Properties p) {
        super(rng, p);
        this.operation = op;
        this.init();
    }

    /** Creat ne instance */
    public Initialiser() {
        super();
    }
    
    /**
     * Creates the population and the individuals
     **/
    public void init() {
        this.population = new SimplePopulation();
        for(int i=0; i<size; i++) {
            this.population.add(this.operation.createIndividual());
        }
    }
    
    public void setProperties(Properties p) {
        int value  = Integer.parseInt(Constants.DEFAULT_POPULATION_SIZE);
        String key = Constants.POPULATION_SIZE;
        try {
            value = Integer.parseInt(p.getProperty(key));
            if(value < 1) {
                throw new BadParameterException(key);
            }
        } catch(Exception e) {            
            p.setProperty(key, Constants.DEFAULT_POPULATION_SIZE);
            System.out.println(e+" using default: "+Constants.DEFAULT_POPULATION_SIZE);
        }
        setSize(value);
    }
    
    public Population getPopulation() {
        return this.population;
    }

    /**
     * Calls the operation to add codons to the individuals in the population
     **/
    public void perform() {
        for(Individual i : this.population.getAll()) {
            this.operation.doOperation((GEIndividual) i);
        }
    }
    
    public void setOperation(Operation op) {
        this.operation = (CreationOperation)op;
    }
    
    public Operation getOperation() {
        return this.operation;
    }
}