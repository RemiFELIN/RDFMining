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

package com.i3s.app.rdfminer.grammar.evolutionary.selection;

import Individuals.FitnessPackage.Fitness;
//import Individuals.GEIndividual;
import Individuals.Individual;
import Individuals.Populations.SimplePopulation;
import Operator.Operations.SelectionOperation;
import Util.Constants;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import com.i3s.app.rdfminer.grammar.evolutionary.individual.GEIndividual;

import java.util.Collections;
import java.util.Comparator;

/**
 * Class for selection of elites.
 **/
public class EliteOperationSelection extends SelectionOperation {

	private boolean evaluateElites;

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
		int value = 0;
		String key = Constants.ELITE_SIZE;
		value = Integer.parseInt(p.getProperty(key, "0"));
		if (value == -1) {// -1 indicates elites is turned off
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

	public void doOperation(Individual operand) {
	}

	public void doOperation(List<Individual> operands) {
		operands.sort(Comparator.comparing(Individual::getFitness).reversed());
		int cnt = 0;
		while (cnt < this.size && cnt < operands.size()) {
			this.selectedPopulation.add(operands.get(cnt));
			cnt++;
		}
	}

	public void setEvaluateElites(boolean evaluateElites) {
		this.evaluateElites = evaluateElites;
	}

	/**
	 * Helper function to rank the poulation in ascending order.
	 * 
	 * @param operands  List of Individuals to rank
	 * @param ascending true if you choose a ascending sort for the fitness list,
	 *                  else a descending sort.
	 * @return An ordered Fitness array
	 **/
	Fitness[] rankAndSortPopulation(List<Individual> operands, boolean ascending) {
		Fitness[] fAt = new Fitness[operands.size()];
		for (int i = 0; i < fAt.length; i++) {
			fAt[i] = operands.get(i).getFitness();
		}
		// Sort ascending
		if (ascending) {
			Arrays.sort(fAt);
		} else {
			// Sort descending
			Arrays.sort(fAt, Collections.reverseOrder());
		}
		return fAt;
	}

	public void doOperation2(List<Individual> operands) {
		Fitness[] fA = rankAndSortPopulation(operands, true);
		int cnt = 0;
		while (cnt < this.size && cnt < operands.size()) {
			// Avoid duplicates
			final boolean valid = fA[cnt].getIndividual().isValid();
			final boolean duplicate = this.selectedPopulation.contains(fA[cnt].getIndividual());
			if (!duplicate && valid) {
				Individual ind = fA[cnt].getIndividual().clone();
				// Set individual as valid
				if (!this.evaluateElites) {
					ind.setEvaluated(fA[cnt].getIndividual().isEvaluated());
					ind.setValid(fA[cnt].getIndividual().isValid());
					ind.setAge(fA[cnt].getIndividual().getAge());
					((GEIndividual) ind).setMapped(((GEIndividual) (fA[cnt].getIndividual())).isMapped());
					((GEIndividual) ind).setUsedCodons(((GEIndividual) (fA[cnt].getIndividual())).getUsedCodons());
				}
				this.selectedPopulation.add(ind);
			}
			cnt++;
		}
	}

}
