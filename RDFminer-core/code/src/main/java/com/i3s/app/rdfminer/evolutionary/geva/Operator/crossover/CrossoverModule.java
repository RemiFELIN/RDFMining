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
 * CrossoverModule.java
 *
 * Created on March 12, 2007, 3:16 PM
 *
 */

package com.i3s.app.rdfminer.evolutionary.geva.Operator.crossover;

import com.i3s.app.rdfminer.evolutionary.geva.Individuals.GEIndividual;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.Individual;
import com.i3s.app.rdfminer.evolutionary.geva.Operator.Operations.Operation;
import com.i3s.app.rdfminer.evolutionary.geva.Operator.OperatorModule;
import com.i3s.app.rdfminer.evolutionary.geva.Util.Random.RandomNumberGenerator;

import java.util.ArrayList;

/**
 * CrossoverModule performs crossover on an entire population. This will typically 
 * be the portion of the main population that has been selected for reproduction. 
 * The specifics of the crossover depend on the particular operation used.  
 * 
 * @author Blip
 */
public class CrossoverModule extends OperatorModule {
    
    private CrossoverOperation op;
    private ArrayList<GEIndividual> alI;

    /** Creates a new instance of CrossoverModule
     * @param m random number generator
     * @param xOver crossover operation
     */
    public CrossoverModule(RandomNumberGenerator m, CrossoverOperation xOver){
        super(m);
        this.op = xOver;
        this.alI = new ArrayList<>(2);
    }

    /**
     * Crossover is performed on in pairs. The neighbouring individuals will be crossed over
     **/
    public void perform() {
        GEIndividual i1;
        GEIndividual i2;
        int length = this.population.size() - this.population.size()%2;
        //System.out.println("xo:"+this.population);
        for(int i=0;i<length;i=i+2) {
            alI.clear();
            i1 = (GEIndividual) this.population.get(i);
            i2 = (GEIndividual) this.population.get(i+1);
            alI.add(i1);
            alI.add(i2);
            this.op.doOperation(alI);
        }
        //This handles the case when the selection size is an odd number
        if(this.population.size()%2!=0) {
            int i =this.population.size()-1;
            i1 = (GEIndividual) this.population.get(i);
            i2 = (GEIndividual) this.getRandomNotThis(this.population.get(i));
            alI.add(i1);
            alI.add(i2);
            this.op.doOperation(alI);
        }
    }
    
    public Operation getOperation() {
        return this.op;
    }
    
    public void setOperation(Operation op) {
        this.op = (CrossoverOperation) op;
    }

    /**
     * Get a random individual that is not the argument individual
     * @param me Individual not to be returned
     * @return Individual that is not argument individual
     **/
    Individual getRandomNotThis(Individual me) {
        this.population.remove(me);
        Individual notMe;
        notMe = this.population.get(rng.nextInt(this.population.size()));
        this.population.add(me);
        return notMe;
    }
    
}
