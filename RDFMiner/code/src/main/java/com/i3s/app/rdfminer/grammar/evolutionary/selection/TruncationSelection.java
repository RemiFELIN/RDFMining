/**
 * 
 */
package com.i3s.app.rdfminer.grammar.evolutionary.selection;

import java.util.ArrayList;

import com.i3s.app.rdfminer.grammar.evolutionary.individual.GEIndividual;

/**
 * @author NGUYEN Thu Huong
 *
 * 
 */
public class TruncationSelection extends EliteSelection {

	public TruncationSelection(int size) {
		super(size);
	}

	public ArrayList<GEIndividual> setupSelectedPopulation(ArrayList<GEIndividual> candidatePopulation, int popSize) {
		ArrayList<GEIndividual> selectedPopulation = new ArrayList<GEIndividual>();
		int i = 0;
		int k;
		int elitePopsize = elitedPopulation.size();
		while (i < popSize) {
			k = 0;
			while (k < elitePopsize && i < popSize) {
				selectedPopulation.add((GEIndividual) elitedPopulation.get(k));
				k++;
				i++;
			}
		}
		return selectedPopulation;
	}
}
