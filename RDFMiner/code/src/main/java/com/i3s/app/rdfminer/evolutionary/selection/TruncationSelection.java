/**
 * 
 */
package com.i3s.app.rdfminer.evolutionary.selection;

import com.i3s.app.rdfminer.evolutionary.geva.Individuals.GEIndividual;

import java.util.ArrayList;


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
		int i = 0;
		while (i < popSize) {
			int k = 0;
			while (k < elitedPopulation.size() && i < popSize) {
				selectedPopulation.add(elitedPopulation.get(k));
				k++;
				i++;
			}
		}
		return selectedPopulation;
	}
}
