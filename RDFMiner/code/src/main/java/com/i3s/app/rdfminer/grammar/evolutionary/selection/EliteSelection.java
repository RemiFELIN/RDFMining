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
//		System.out.println("canPop size: " + canPop.size());
		/*for(GEIndividual ind : canPop) {
			System.out.println("[IND] " + ind.getGenotype() + "\n -> Fitness: " + ind.getFitness().getDouble());
		}*/
		List<Individual> candidates = new ArrayList<>(canPop);
		doOperation(candidates);
		ArrayList<GEIndividual> selectedPopulation = new ArrayList<>();
//		System.out.println("getSelectedPopulation size: " + getSelectedPopulation().size());
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

		ArrayList<GEIndividual> selectedPopulation = new ArrayList<>(
				candidatePopulation.size() - elitedPopulation.size());

		for (GEIndividual individual : candidatePopulation) {
			int tmp = 0;
			for (GEIndividual elitedIndividual : elitedPopulation) {
				String elite = elitedIndividual.getGenotype().get(0).toString();
				String can = individual.getGenotype().get(0).toString();
				if (elite.equals(can)) {
					tmp++;
				}
			}
			if (tmp == 0) {
				selectedPopulation.add(individual);
			}
		}
		return selectedPopulation;
	}

	void setElitedPopulation(ArrayList<GEIndividual> elitedPopulation) {
		this.elitedPopulation = elitedPopulation;
	}

	public ArrayList<GEIndividual> getElitedPopulation() {
		return elitedPopulation;
	}

}
