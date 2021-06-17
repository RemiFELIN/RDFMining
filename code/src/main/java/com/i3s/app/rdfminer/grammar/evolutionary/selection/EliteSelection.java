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

	protected ArrayList<GEIndividual> ElitedPopulation;

	EliteSelection(int size) {
		super(size);

	}

	void ParentsSelection_Elitism(ArrayList<GEIndividual> CandidatePopulation) {

		List<Individual> listCandidate = new ArrayList<Individual>();
		for (int i = 0; i < CandidatePopulation.size(); i++)
			listCandidate.add(CandidatePopulation.get(i));

		doOperation(listCandidate);
		// int SelectedPopsize= getSelectedPopulation().size();
		/*
		 * for (int k=1; k<= SelectedPopsize; k++) { logger.info("individual " + k );
		 * logger.info(getSelectedPopulation().get(k-1).getGenotype());
		 * logger.info(getSelectedPopulation().get(k-1).getFitness().getDouble());
		 * logger.info(getSelectedPopulation().get(k-1)); logger.info(
		 * "-----------------------------------------------------------------------------------------"
		 * ); }
		 */

		ArrayList<GEIndividual> SelectedPopulation = new ArrayList<GEIndividual>();
		for (int i = 0; i < getSelectedPopulation().size(); i++) {
			SelectedPopulation.add((GEIndividual) getSelectedPopulation().get(i));
		}
		setElitedPopulation(SelectedPopulation);

	}

	public ArrayList<GEIndividual> SetupSelectedPopulation(ArrayList<GEIndividual> CandidatePopulation)

	{
		ArrayList<GEIndividual> SelectedPopulation = new ArrayList<GEIndividual>(
				CandidatePopulation.size() - ElitedPopulation.size());
		int tmp = 0;

		String elite;
		String can;
		int PopSize = CandidatePopulation.size();
		int ElitePopSize = ElitedPopulation.size();
		for (int i = 0; i < PopSize; i++) {
			tmp = 0;
			for (int k = 0; k < ElitePopSize; k++) {
				elite = ElitedPopulation.get(k).getGenotype().get(0).toString();
				can = CandidatePopulation.get(i).getGenotype().get(0).toString();
				if (elite.equals(can))
					tmp++;
			}
			if (tmp == 0) {
				SelectedPopulation.add(CandidatePopulation.get(i));
			}
		}

		return SelectedPopulation;
	}

	void setElitedPopulation(ArrayList<GEIndividual> ElitedPopulation) {
		this.ElitedPopulation = ElitedPopulation;
	}

	ArrayList<GEIndividual> getElitedPopulation() {
		return ElitedPopulation;
	}

}
