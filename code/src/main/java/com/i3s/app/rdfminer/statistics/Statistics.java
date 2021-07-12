package com.i3s.app.rdfminer.statistics;

import javax.xml.bind.annotation.XmlRootElement;

import com.i3s.app.rdfminer.grammar.evolutionary.individual.GEIndividual;

import Individuals.Populations.SimplePopulation;
import java.util.ArrayList;

/**
 * @author NGUYEN Thu Huong Dec 21, 2017
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

	double getCountTrueGoldStandard(ArrayList<GEIndividual> individuals) {
		int count = 0;
		int sizeList = individuals.size();
		for (int i = 0; i < sizeList; i++) {
			if (individuals.get(i).isEvaluated()) {
				count++;
			}
		}
		return count / sizeList;
	}

	public double getCountComplexAxiomNumber(ArrayList<GEIndividual> individuals) {
		int count = 0;
		int sizeList = individuals.size();
		for (int i = 0; i < sizeList; i++) {
			if (individuals.get(i).getPhenotype().toString().contains("ObjectUnionOf")
					|| individuals.get(i).getPhenotype().toString().contains("ObjectIntersectionOf")
					|| individuals.get(i).getPhenotype().toString().contains("ObjectAllValuesFrom")
					|| individuals.get(i).getPhenotype().toString().contains("ObjectSomeValuesFrom")) {
				count++;
			}
		}
		return count / sizeList;
	}

	public double getCountComplexAxiomNumber2(ArrayList<GEIndividual> individuals) {
		int count = 0;
		int sizeList = individuals.size();
		for (int i = 0; i < sizeList; i++) {
			if (individuals.get(i).getPhenotype().toString().contains("ObjectAllValuesFrom")
					|| individuals.get(i).getPhenotype().toString().contains("ObjectSomeValuesFrom")) {
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
