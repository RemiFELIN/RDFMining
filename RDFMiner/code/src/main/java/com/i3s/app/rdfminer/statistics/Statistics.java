package com.i3s.app.rdfminer.statistics;

import javax.xml.bind.annotation.XmlRootElement;

import com.i3s.app.rdfminer.entity.Entity;
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

	public double computeAverageFitness(ArrayList<Entity> entities) {
		double sumFitness = 0;
		for (Entity individual : entities) {
			sumFitness += individual.individual.getFitness().getDouble();
		}
		if (entities.size() != 0) {
			return sumFitness / entities.size();
		}
		return 0;
	}

	public double getCountSuccessMapping(ArrayList<Entity> entities) {
		int count = 0;
		int sizeList = entities.size();
		for (Entity entity : entities) {
			if (entity.individual.isMapped()) {
				count++;
			}
		}
		return count / sizeList;
	}

	public long getEntitiesWithNonNullFitness(ArrayList<Entity> entities) {
		return entities.stream().filter(entity -> entity.individual.getFitness().getDouble() != 0).count();
	}

//	double getCountTrueGoldStandard(ArrayList<GEIndividual> individuals) {
//		int count = 0;
//		int sizeList = individuals.size();
//		for (GEIndividual individual : individuals) {
//			if (individual.isEvaluated()) {
//				count++;
//			}
//		}
//		return count / sizeList;
//	}

	public double getCountComplexAxiom(ArrayList<Entity> entities) {
		int count = 0;
		int sizeList = entities.size();
		for (Entity entity : entities) {
			if (entity.individual.getPhenotype().toString().contains("ObjectUnionOf")
					|| entity.individual.getPhenotype().toString().contains("ObjectIntersectionOf")
					|| entity.individual.getPhenotype().toString().contains("ObjectAllValuesFrom")
					|| entity.individual.getPhenotype().toString().contains("ObjectSomeValuesFrom")) {
				count++;
			}
		}
		return count / sizeList;
	}

//	public double getCountComplexAxiomSpecial(ArrayList<GEIndividual> individuals) {
//		int count = 0;
//		int sizeList = individuals.size();
//		for (GEIndividual individual : individuals) {
//			if (individual.getPhenotype().toString().contains("ObjectAllValuesFrom")
//					|| individual.getPhenotype().toString().contains("ObjectSomeValuesFrom")) {
//				count++;
//			}
//		}
//		return count / sizeList;
//	}

//	int computeFitnessGreaterThreshold(double thresholdFitness, SimplePopulation canPop) {
//		int count = 0;
//		for (int i = 0; i < canPop.size(); i++) {
//			if (canPop.get(i).getFitness().getDouble() > thresholdFitness) {
//				count++;
//			}
//		}
//		return count;
//	}


}
