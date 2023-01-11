package com.i3s.app.rdfminer.statistics;

import com.i3s.app.rdfminer.RDFMiner;
import com.i3s.app.rdfminer.entity.Entity;

import java.util.ArrayList;

/**
 * This class is used to compute some statistics about results founded
 * 
 * @author NGUYEN Thu Huong, Rémi FELIN
 */
public class Statistics {

	public double computeAverageFitness(ArrayList<Entity> entities) {
		double sumFitness = 0;
		for (Entity entity : entities) {
//			System.out.println("Individual: " + entity.individual.getPhenotype());
//			System.out.println("Fitness: " + entity.fitness);
			sumFitness += entity.fitness;
		}
		if (entities.size() != 0) {
			return sumFitness / entities.size();
		}
		return 0;
	}

	public double getCountSuccessMapping(ArrayList<Entity> entities) {
		int count = 0;
		for (Entity entity : entities) {
			if (entity.individual.isMapped()) {
				count++;
			}
		}
		return count / entities.size();
	}

	public long getEntitiesWithNonNullFitness(ArrayList<Entity> entities) {
		return entities.stream().filter(entity -> entity.individual.getFitness().getDouble() != 0).count();
	}

	public double getCountComplexAxiom(ArrayList<Entity> entities) {
		int count = 0;
		for (Entity entity : entities) {
			if (entity.individual.getPhenotype().toString().contains("ObjectUnionOf")
					|| entity.individual.getPhenotype().toString().contains("ObjectIntersectionOf")
					|| entity.individual.getPhenotype().toString().contains("ObjectAllValuesFrom")
					|| entity.individual.getPhenotype().toString().contains("ObjectSomeValuesFrom")) {
				count++;
			}
		}
		return count / entities.size();
	}

}
