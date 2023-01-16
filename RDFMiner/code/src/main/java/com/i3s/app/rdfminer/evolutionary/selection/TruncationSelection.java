/**
 * 
 */
package com.i3s.app.rdfminer.evolutionary.selection;

import java.util.ArrayList;

import com.i3s.app.rdfminer.evolutionary.individual.GEIndividual;

/**
 * {@link TypeSelection#TRUNCATION TRUNCATION} selection.
 * 
 * @author NGUYEN Thu Huong
 * 
 */
public class TruncationSelection extends EliteSelection {

	public TruncationSelection(int size) {
		super(size);
	}

	public ArrayList<GEIndividual> setupSelectedPopulation(int popSize) {
		ArrayList<GEIndividual> selectedPopulation = new ArrayList<GEIndividual>();
		int i = 0, k;
		int elitePopsize = elitedPopulation.size();
		while (i < popSize) {
			k = 0;
			while (k < elitePopsize && i < popSize) {
				selectedPopulation.add(elitedPopulation.get(k));
				k++;
				i++;
			}
		}
		return selectedPopulation;
	}
}
