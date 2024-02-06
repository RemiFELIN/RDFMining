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

package com.i3s.app.rdfminer.evolutionary.geva.Operator.Operations;

import com.i3s.app.rdfminer.evolutionary.geva.Mapper.GEGrammar;
import com.i3s.app.rdfminer.evolutionary.geva.Mapper.Production;
import com.i3s.app.rdfminer.evolutionary.geva.Mapper.Rule;
import com.i3s.app.rdfminer.evolutionary.geva.Mapper.Symbol;
import com.i3s.app.rdfminer.evolutionary.geva.Util.Random.RandomNumberGenerator;
import com.i3s.app.rdfminer.evolutionary.geva.Util.Structures.NimbleTree;

import java.util.ArrayList;
import java.util.Properties;

/**
 * Create a genotype by growing a tree to maxDepth for all leaves
 * @author erikhemberg
 */
public class FullInitialiser extends GrowInitialiser {

    /**
     * New instance
     * @param rng random number generator
     * @param gegrammar grammatical evolution grammar (GEGrammar)
     * @param maxDepth max growth depth of tree
     */
    public FullInitialiser(RandomNumberGenerator rng, GEGrammar gegrammar, int maxDepth) {
        super(rng, gegrammar, maxDepth);
    }

    /**
     * New instance
     * @param rng random number generator
     * @param gegrammar grammatical evolution grammar (GEGrammar)
     * @param p properties
     */
    public FullInitialiser(RandomNumberGenerator rng, GEGrammar gegrammar, Properties p) {
        super(rng, gegrammar, p);
    }
    

    public ArrayList<Integer> getPossibleRules(NimbleTree<Symbol> dt, Rule rule) {
        ArrayList<Integer> possibleRules = new ArrayList<Integer>();
        boolean recursiveRules = false;
        //Iterate through each possible production and store indices to the usable ones
        int i = 0;
        for (Production p : rule) {
            if ((dt.getCurrentNode().getDepth() + 1 + p.getMinimumDepth()) <= this.maxDepth) {
                if(!recursiveRules && p.getRecursive()) {
                    recursiveRules = true;
                    // Only recursive rules allowed? What about non-recursive rules with the proper length??
                    possibleRules.clear();
                }
                if(!recursiveRules || p.getRecursive()) {
                    possibleRules.add(i);
                }
            }
            i++;
        }
        return possibleRules;
    }

}
