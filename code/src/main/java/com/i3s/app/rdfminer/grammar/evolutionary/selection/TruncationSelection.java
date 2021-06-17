/**
 * 
 */
package com.i3s.app.rdfminer.grammar.evolutionary.selection;

import java.util.ArrayList;

import com.i3s.app.rdfminer.grammar.evolutionary.individual.GEIndividual;

//import Individuals.Individual;
//import Individuals.GEIndividual;
//import Individuals.Populations.SimplePopulation;

/**
 * @author NGUYEN Thu Huong
 *
 * 
 */
public class TruncationSelection extends EliteSelection {

	/**
	 * 
	 */
	public TruncationSelection(int size) {
		// TODO Auto-generated constructor stub
		super(size);
	}

	public ArrayList<GEIndividual> SetupSelectedPopulation(ArrayList<GEIndividual> CandidatePopulation, int Popsize) {
		ArrayList<GEIndividual> SelectedPopulation = new ArrayList<GEIndividual>();

		int i = 0;
		int k;
		// int Popsize= CandidatePopulation.size();
		int ElitePopsize = ElitedPopulation.size();
		while (i < Popsize) {
			k = 0;
			while (k < ElitePopsize && i < Popsize) {
				SelectedPopulation.add((GEIndividual) ElitedPopulation.get(k));
				k++;
				i++;
			}

		}

		return SelectedPopulation;
	}
}
