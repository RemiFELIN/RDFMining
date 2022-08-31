package com.i3s.app.rdfminer.statistics;

import javax.xml.bind.annotation.XmlRootElement;

import com.i3s.app.rdfminer.grammar.evolutionary.individual.GEIndividual;

import Individuals.Populations.SimplePopulation;
import java.util.ArrayList;

/**
 * This class is used to compute some statistics about results founded
 * 
 * @author NGUYEN Thu Huong, Rémi FELIN
 */
@XmlRootElement
public class Statistics {

	public double computeAverageFitness(ArrayList<GEIndividual> individuals) {
		double sumFitness = 0;
		for (int i = 0; i < individuals.size(); i++) {
			sumFitness += individuals.get(i).getFitness().getDouble();
		}
		if (individuals.size() != 0) {
			return sumFitness / individuals.size();
		}
		return 0;
	}

	public double getCountSuccessMapping(ArrayList<GEIndividual> individuals) {
		int count = 0;
		int sizeList = individuals.size();
		for (int i = 0; i < sizeList; i++) {
			if (individuals.get(i).isMapped()) {
				count++;
			}
		}
		return count / sizeList;
	}

	public long getIndividualsWithNonNullFitness(ArrayList<GEIndividual> individuals) {
		return individuals.stream().filter(individual -> individual.getFitness().getDouble() != 0).count();
	}

	double getCountTrueGoldStandard(ArrayList<GEIndividual> individuals) {
		int count = 0;
		int sizeList = individuals.size();
		for (GEIndividual individual : individuals) {
			if (individual.isEvaluated()) {
				count++;
			}
		}
		return count / sizeList;
	}

	public double getCountComplexAxiom(ArrayList<GEIndividual> individuals) {
		int count = 0;
		int sizeList = individuals.size();
		for (GEIndividual individual : individuals) {
			if (individual.getPhenotype().toString().contains("ObjectUnionOf")
					|| individual.getPhenotype().toString().contains("ObjectIntersectionOf")
					|| individual.getPhenotype().toString().contains("ObjectAllValuesFrom")
					|| individual.getPhenotype().toString().contains("ObjectSomeValuesFrom")) {
				count++;
			}
		}
		return count / sizeList;
	}

	public double getCountComplexAxiomSpecial(ArrayList<GEIndividual> individuals) {
		int count = 0;
		int sizeList = individuals.size();
		for (GEIndividual individual : individuals) {
			if (individual.getPhenotype().toString().contains("ObjectAllValuesFrom")
					|| individual.getPhenotype().toString().contains("ObjectSomeValuesFrom")) {
				count++;
			}
		}
		return count / sizeList;
	}

	int computeFitnessGreaterThreshold(double thresholdFitness, SimplePopulation canPop) {
		int count = 0;
		for (int i = 0; i < canPop.size(); i++) {
			if (canPop.get(i).getFitness().getDouble() > thresholdFitness) {
				count++;
			}
		}
		return count;
	}


}
