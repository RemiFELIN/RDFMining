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
 * MutationOperator.java
 *
 * Created on March 30, 2007, 5:35 PM
 *
 */

package com.i3s.app.rdfminer.evolutionary.geva.Operator.Operations;

import com.i3s.app.rdfminer.evolutionary.geva.Individuals.GEIndividual;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.Individual;
import com.i3s.app.rdfminer.evolutionary.geva.Operator.OperatorModule;
import com.i3s.app.rdfminer.evolutionary.geva.Operator.mutation.MutationOperation;
import com.i3s.app.rdfminer.evolutionary.geva.Util.Random.RandomNumberGenerator;

/**
 * MutationOperator has a MutationOperation.
 * @author erikhemberg
 */
public class MutationOperator extends OperatorModule {
    
    protected MutationOperation operation;
    
    /** Creates a new instance of MutationOperator
     * @param rng random number generator
     * @param op opertion
     */
    public MutationOperator(RandomNumberGenerator rng, MutationOperation op) {
        super(rng);
        this.operation = op;
    }
    
    public Operation getOperation() {
        return this.operation;
    }

    public void perform() {
        for(Individual i : this.population.getAll()) {
            operation.doOperation((GEIndividual) i);
        }
    }
    
    public void setOperation(Operation op) {
        this.operation = (MutationOperation)op;
    }
    
}
