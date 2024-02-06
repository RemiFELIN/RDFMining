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
 * ReplacementOperation.java
 *
 * Created on March 15, 2007, 4:38 PM
 *
 */
package com.i3s.app.rdfminer.evolutionary.geva.Operator.Operations;

import com.i3s.app.rdfminer.evolutionary.geva.Individuals.FitnessPackage.Fitness;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.GEIndividual;
import com.i3s.app.rdfminer.evolutionary.geva.Util.Constants;

import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * EliteReplacementOperation removes the worst
 * @author Blip
 */
public class EliteReplacementOperation implements Operation {

    private int eliteSize;

    /** Creates a new instance of ReplacementOperation
     * @param size size
     */
    public EliteReplacementOperation(int size) {
        this.eliteSize = size;
    }

    /** Creates a new instance of ReplacementOperation
     * @param p properties
     */
    public EliteReplacementOperation(Properties p) {
        this.setProperties(p);
    }

    /**
     * Set properties
     *
     * @param p object containing properties
     */
    public void setProperties(Properties p) {
        int value = 0;
        String key = Constants.ELITE_SIZE;
        value = Integer.parseInt(p.getProperty(key, "0"));
        if (value == -1) {//-1 indicates elites is turned off
            value = 0;
        }
        this.eliteSize = value;
    }

    public void doOperation(GEIndividual operand) {
    }

    /**
     * Sort ascending and remove the worst individuals
     * @param operand individuals to sort
     */
    public void doOperation(List<GEIndividual> operand) {
        Collections.sort(operand);
        int cnt = operand.size();
        while (cnt > this.eliteSize && cnt > 0) {
            cnt--;
            operand.remove(cnt);
        }
    }

    public int getEliteSize() {
        return eliteSize;
    }

    public void printHelp(Fitness[] fA) {
        StringBuffer s = new StringBuffer();
        for (Fitness aFA : fA) {
            s.append(aFA.getDouble());
	    s.append(",");
        }
        System.out.println("sorted elites:" + s);
    //    System.out.println("best_fit:"+fA[0].getDouble()+" "+fA[0].getIndividual().getPhenotypeString(0));
    }
}
