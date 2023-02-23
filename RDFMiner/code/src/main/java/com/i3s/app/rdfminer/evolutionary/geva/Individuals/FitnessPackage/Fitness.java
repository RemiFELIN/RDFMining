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
 * Fitness.java
 *
 * Created on March 1, 2007, 5:08 PM
 *
 */

package com.i3s.app.rdfminer.evolutionary.geva.Individuals.FitnessPackage;


import com.i3s.app.rdfminer.evolutionary.geva.Individuals.Individual;

/**
 * Interface for Fitness. Contains methods for getting and setting the max and min
 * fitness measurements for Integer and Double types.
 * @author Blip
 */
public interface Fitness extends Comparable<Fitness> {

    /**
     * Get the individual that is refered to by the fitness
     * @return individual to which the fitness belongs
     */
    public Individual getIndividual();

    /**
     * Set individual to which the fitness belongs
     * @param i individual to which the fitness belongs
     */
    public void setIndividual(Individual i);
    
    public double getDouble();
    public void setDouble(double f);
    
    public int getInt();
    public void setInt(int f);

    public double getMaxDoubleFitness();
    public double getMinDoubleFitness();
    public int getMaxIntFitness();
    public int getMinIntFitness();

    /**
     * Set the default value of fitness. This can be given to
     * unevaluated individuals, such as newly created or invalids
     */
    public void setDefault();
    //GET DEFAULT??!!
    
}
