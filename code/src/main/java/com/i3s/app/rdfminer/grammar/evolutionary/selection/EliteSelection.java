/**
 * 
 */
package com.i3s.app.rdfminer.grammar.evolutionary.selection;

import java.util.ArrayList;
import java.util.List;

import com.i3s.app.rdfminer.grammar.evolutionary.individual.GEIndividual;

import Individuals.Individual;

/**
 * @author NGUYEN Thu Huong
 *
 * 
 */
public class EliteSelection extends EliteOperationSelection {

	protected ArrayList<GEIndividual> elitedPopulation;

	public EliteSelection(int size) {
		super(size);
	}

	public void setParentsSelectionElitism(ArrayList<GEIndividual> candidatePopulation) {
		List<Individual> listCandidate = new ArrayList<Individual>();
		for (int i = 0; i < candidatePopulation.size(); i++)
			listCandidate.add(candidatePopulation.get(i));
		doOperation(listCandidate);
		ArrayList<GEIndividual> SelectedPopulation = new ArrayList<GEIndividual>();
		for (int i = 0; i < getSelectedPopulation().size(); i++) {
			SelectedPopulation.add((GEIndividual) getSelectedPopulation().get(i));
		}
		setElitedPopulation(SelectedPopulation);
	}

	public ArrayList<GEIndividual> setupSelectedPopulation(ArrayList<GEIndividual> candidatePopulation) {
		ArrayList<GEIndividual> selectedPopulation = new ArrayList<GEIndividual>(
				candidatePopulation.size() - elitedPopulation.size());
		int tmp = 0;
		String elite;
		String can;
		int PopSize = candidatePopulation.size();
		int ElitePopSize = elitedPopulation.size();
		for (int i = 0; i < PopSize; i++) {
			tmp = 0;
			for (int k = 0; k < ElitePopSize; k++) {
				elite = elitedPopulation.get(k).getGenotype().get(0).toString();
				can = candidatePopulation.get(i).getGenotype().get(0).toString();
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
