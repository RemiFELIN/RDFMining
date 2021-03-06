/**
 * 
 */
package com.i3s.app.rdfminer.grammar.evolutionary.selection;

import java.util.ArrayList;
import java.util.List;

import com.i3s.app.rdfminer.grammar.evolutionary.individual.GEIndividual;

import Individuals.Individual;

/**
 * Selection of the 'elite' population : a population composed of the 'best'
 * individuals which have the best values of fitness.
 * 
 * @author NGUYEN Thu Huong
 *
 */
public class EliteSelection extends EliteOperationSelection {

	protected ArrayList<GEIndividual> elitedPopulation;

	public EliteSelection(int size) {
		super(size);
	}

	/**
	 * set the elitism population from a given candidate population
	 * 
	 * @param canPop a given population
	 */
	public void setParentsSelectionElitism(ArrayList<GEIndividual> canPop) {
		List<Individual> candidates = new ArrayList<Individual>();
		for (int i = 0; i < canPop.size(); i++)
			candidates.add(canPop.get(i));
		doOperation(candidates);
		ArrayList<GEIndividual> selectedPopulation = new ArrayList<GEIndividual>();
		for (int i = 0; i < getSelectedPopulation().size(); i++) {
			selectedPopulation.add((GEIndividual) getSelectedPopulation().get(i));
		}
		setElitedPopulation(selectedPopulation);
	}

	/**
	 * set the selected population from a given population
	 * @param candidatePopulation a given population
	 * @return the selected population
	 */
	public ArrayList<GEIndividual> setupSelectedPopulation(ArrayList<GEIndividual> candidatePopulation) {

		ArrayList<GEIndividual> selectedPopulation = new ArrayList<GEIndividual>(
				candidatePopulation.size() - elitedPopulation.size());
		
		for (int i = 0; i < candidatePopulation.size(); i++) {
			int tmp = 0;
			for (int k = 0; k < elitedPopulation.size(); k++) {
				String elite = elitedPopulation.get(k).getGenotype().get(0).toString();
				String can = candidatePopulation.get(i).getGenotype().get(0).toString();
				if (elite.equals(can)) {
					tmp++;
				}
			}
			if (tmp == 0) {
				selectedPopulation.add(candidatePopulation.get(i));
			}
		}
		return selectedPopulation;
	}

	void setElitedPopulation(ArrayList<GEIndividual> ElitedPopulation) {
		this.elitedPopulation = ElitedPopulation;
	}

	public ArrayList<GEIndividual> getElitedPopulation() {
		return elitedPopulation;
	}

}
