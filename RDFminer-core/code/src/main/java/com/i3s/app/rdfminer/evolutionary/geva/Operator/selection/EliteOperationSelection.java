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

package com.i3s.app.rdfminer.evolutionary.geva.Operator.selection;

import com.i3s.app.rdfminer.evolutionary.geva.Individuals.FitnessPackage.Fitness;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.GEIndividual;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.Individual;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.Populations.SimplePopulation;
import com.i3s.app.rdfminer.evolutionary.geva.Util.Constants;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Class for selection of elites.
 **/
public class EliteOperationSelection extends SelectionOperation {

    private boolean evaluateElites;

    public EliteOperationSelection() {
        super();
    }

    /**
     * New instance
     *
     * @param size size of elites
     */
    public EliteOperationSelection(int size) {
        super(size);
    }

    /**
     * New instance
     *
     * @param p properties
     */
    public EliteOperationSelection(Properties p) {
        super();
        setProperties(p);
    }

    @Override
    public void setProperties(Properties p) {
        String key = Constants.ELITE_SIZE;
        int value = Integer.parseInt(p.getProperty(key, "0"));
        if (value == -1) {//-1 indicates elites is turned off
            value = 0;
        }
        this.size = value;
        String valueS;
        key = Constants.EVALUATE_ELITES;
        try {
            valueS = p.getProperty(key);
            if (valueS == null) {
                valueS = Constants.FALSE;
            }
        } catch (Exception e) {
            valueS = Constants.FALSE;
            System.out.println(this.getClass().getName() + ".setProperties " + e + " using default: " + valueS);
        }
        this.evaluateElites = valueS.equals(Constants.TRUE);
        super.selectedPopulation = new SimplePopulation(this.size);
    }

    public boolean isEvaluateElites() {
        return this.evaluateElites;
    }

    public void doOperation(GEIndividual operand) {
    }

    /**
     * Ranks the population. Takes out size number of individuals and adds
     * to the selectedPopulation.
     *
     * @param operands Individuals to select from
     **/
    public void doOperation(List<GEIndividual> operands) {
        Fitness[] fA = rankPopulation(operands);
        int cnt = 0;
        while (cnt < this.size) {
//            System.out.println("cnt in doOperation: " + cnt);
            //Avoid duplicates
//            final boolean valid = fA[cnt].getIndividual().isValid();
            Individual ind = fA[cnt].getIndividual();
//            System.out.println(ind.getGenotype());
            // not distinct individuals in the selected population
//            if (valid) { // !this.selectedPopulation.contains(ind) && valid
            //Set individual as valid
            if (!this.evaluateElites) {
                ind.setEvaluated(fA[cnt].getIndividual().isEvaluated());
                ind.setValid(fA[cnt].getIndividual().isValid());
                ind.setAge(fA[cnt].getIndividual().getAge());
                ind.setFitness(fA[cnt].getIndividual().getFitness());
                ((GEIndividual) ind).setMapped(((GEIndividual) (fA[cnt].getIndividual())).isMapped());
                ((GEIndividual) ind).setUsedCodons(((GEIndividual) (fA[cnt].getIndividual())).getUsedCodons());
                // this individual is selected
                ((GEIndividual) ind).isPartOfElite = true;
            }
            this.selectedPopulation.add(ind);
//            }
            cnt++;
        }
        //
        //System.out.println("E:"+this.selectedPopulation);
    }

    public void setEvaluateElites(boolean evaluateElites) {
        this.evaluateElites = evaluateElites;
    }

    /**
     * Helper function to rank the poulation in ascending order.
     *
     * @param operands List of Individuals to rank
     * @return An ordered Fitness array
     **/
    Fitness[] rankPopulation(List<GEIndividual> operands) {
        Fitness[] fAt = new Fitness[operands.size()];
        for (int i = 0; i < fAt.length; i++) {
            // reset elite indicator for each individuals
            operands.get(i).isPartOfElite = false;
            fAt[i] = operands.get(i).getFitness();
        }
        //Sort descending
        Arrays.sort(fAt, Collections.reverseOrder());
        return fAt;
    }

}
