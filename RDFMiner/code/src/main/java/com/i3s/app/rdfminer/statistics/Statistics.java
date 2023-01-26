package com.i3s.app.rdfminer.statistics;

import com.i3s.app.rdfminer.entity.Entity;
import org.apache.log4j.Logger;

import java.util.ArrayList;

/**
 * This class is used to compute some statistics about results founded
 * 
 * @author NGUYEN Thu Huong, Rémi FELIN
 */
public class Statistics {

	private static final Logger logger = Logger.getLogger(Statistics.class.getName());

	public double computeAverageFitness(ArrayList<Entity> entities) {
		logger.debug("entities.size = " + entities.size());
		double sumFitness = 0;
		for (Entity entity : entities) {
			logger.debug("# entity.fitness = " + entity.fitness);
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

	public double getAverageSumDistance(ArrayList<Entity> entities) {
		double sumDistances = 0.0;
		for(Entity entity : entities) {
			double distance = 0.0;
			for(double similarity : entity.similarities) {
				distance += similarity;
			}
			sumDistances += distance;
		}
		return sumDistances / entities.size();
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
