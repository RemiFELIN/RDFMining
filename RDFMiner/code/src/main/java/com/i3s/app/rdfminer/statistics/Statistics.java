package com.i3s.app.rdfminer.statistics;

import javax.xml.bind.annotation.XmlRootElement;

import com.i3s.app.rdfminer.RDFMiner;
import com.i3s.app.rdfminer.entity.axiom.Axiom;
import com.i3s.app.rdfminer.entity.shacl.Shape;
import com.i3s.app.rdfminer.grammar.evolutionary.individual.GEIndividual;

import Individuals.Populations.SimplePopulation;
import com.i3s.app.rdfminer.parameters.CmdLineParameters;

import java.util.ArrayList;

/**
 * This class is used to compute some statistics about results founded
 * 
 * @author NGUYEN Thu Huong, Rémi FELIN
 */
@XmlRootElement
public class Statistics {

	public static void setParameters(CmdLineParameters parameters) {
		RDFMiner.stats.populationSize = parameters.populationSize;
		RDFMiner.stats.maxLengthChromosome = parameters.initLenChromosome;
		RDFMiner.stats.maxWrapping = parameters.maxWrapp;
		RDFMiner.stats.crossoverProbability = parameters.proCrossover;
		RDFMiner.stats.mutationProbability = parameters.proMutation;
		RDFMiner.stats.timeOut = (int) parameters.timeOut;
		// Ellitism
		if (parameters.elitism == 1) {
			RDFMiner.stats.elitismSelection = true;
			RDFMiner.stats.eliteSize = parameters.sizeElite;
		} else {
			RDFMiner.stats.elitismSelection = false;
		}
		// Type select
		switch (parameters.typeSelect) {
			case 1:
				RDFMiner.stats.selectionMethod = "Roulette Wheel selection method";
				break;
			default:
			case 2:
				RDFMiner.stats.selectionMethod = "Truncation selection method";
				RDFMiner.stats.selectionSize = parameters.sizeSelection;
				break;
			case 3:
				RDFMiner.stats.selectionMethod = "Tournament selection method";
				break;
			case 4:
				RDFMiner.stats.selectionMethod = "Normal selection method";
				break;
		}
	}

	public double computeAverageFitnessFromAxioms(ArrayList<Axiom> axioms) {
		double sumFitness = 0;
		for(Axiom axiom : axioms) {
			sumFitness += axiom.fitness;
		}
		return sumFitness / axioms.size();
	}

	public double computeAverageFitnessFromShapes(ArrayList<Shape> shapes) {
		double sumFitness = 0;
		for(Shape shape : shapes) {
			sumFitness += shape.fitness.doubleValue();
		}
		return sumFitness / shapes.size();
	}

	public double getSuccessMappingRateFromAxioms(ArrayList<Axiom> axioms) {
		int count = 0;
		for(Axiom axiom : axioms) {
			if(axiom.individual.isMapped())
				count++;
		}
		return count / axioms.size();
	}

	public double getSuccessMappingRateFromShapes(ArrayList<Shape> shapes) {
		int count = 0;
		for(Shape shape : shapes) {
			if(shape.individual.isMapped())
				count++;
		}
		return count / shapes.size();
	}

	public long getAxiomsWithNonNullFitness(ArrayList<Axiom> axioms) {
		return axioms.stream().filter(axiom -> axiom.fitness != 0).count();
	}

	public long getShapesWithNonNullFitness(ArrayList<Shape> shapes) {
		return shapes.stream().filter(shape -> shape.fitness.doubleValue() != 0).count();
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

	public double getCountComplexAxioms(ArrayList<Axiom> axioms) {
		int count = 0;
		for (Axiom axiom : axioms) {
			if (axiom.individual.getPhenotype().toString().contains("ObjectUnionOf")
					|| axiom.individual.getPhenotype().toString().contains("ObjectIntersectionOf")
					|| axiom.individual.getPhenotype().toString().contains("ObjectAllValuesFrom")
					|| axiom.individual.getPhenotype().toString().contains("ObjectSomeValuesFrom")) {
				count++;
			}
		}
		return count / axioms.size();
	}

//	public double getCountComplexAxiomSpecial(ArrayList<Axiom> axioms) {
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
