package com.i3s.app.rdfminer;

import javax.xml.bind.annotation.XmlRootElement;

import com.i3s.app.rdfminer.grammar.evolutionary.individual.GEIndividual;

import Individuals.Populations.SimplePopulation;
import java.util.ArrayList;

/**
 * @author NGUYEN Thu Huong Dec 21, 2017
 */
@XmlRootElement
public class Statistics {
	/*
	 * private int Count_Threshold; //count the finess > threshold private double
	 * Average_Fitness; private double Count_SuccessMapping; private double
	 * Count_TrueGoldStandard; private double Count_ComplexAxiomNumber; private
	 * double AverageFitness; private int Count_threshold; private double
	 * KoefPhenotypeDiversity; private double KoefGenotypeDiversity; public
	 * ArrayList<Double> ListAverageFitness; public ArrayList<Integer>
	 * ListCount_threshold; public ArrayList<Double> ListDiversity; public
	 * ArrayList<Double> ListGenotypeDiversity;
	 */

	public Statistics() {
		/*
		 * this.Count_Threshold=0; this.Average_Fitness=0.0;
		 * this.Count_SuccessMapping=0.0; this.AverageFitness=0.0;
		 * this.Count_threshold=0; this.KoefPhenotypeDiversity=0.0;
		 * this.KoefGenotypeDiversity=0.0; this.ListAverageFitness=new
		 * ArrayList<Double>(); this.ListCount_threshold=new ArrayList<Integer>();
		 * this.ListDiversity=new ArrayList<Double>(); this.ListGenotypeDiversity= new
		 * ArrayList<Double>();
		 */
	}

	/*
	 * void SetAverageFitness (double averageFitness) {
	 * this.Average_Fitness=averageFitness; }
	 * 
	 * void SetCountThreshold (int Count_Threshold) {
	 * this.Count_Threshold=Count_Threshold; }
	 * 
	 * int getCountThreshold() { return this.Count_threshold; } double
	 * getAverageFitness() { return this.AverageFitness; } double
	 * getKoefPhenotypeDiversity() { return this.KoefPhenotypeDiversity; } double
	 * getKoefGenotypeDiversity() { return this.KoefGenotypeDiversity; }
	 */
	double Compute_AverageFitness(ArrayList<GEIndividual> ListIndivi) {
		double averageFitness = 0;
		double sumFitness = 0;
		for (int i = 0; i < ListIndivi.size(); i++) {
			sumFitness += ListIndivi.get(i).getFitness().getDouble();

		}
		if (ListIndivi.size() != 0)
			averageFitness = sumFitness / ListIndivi.size();

		return averageFitness;
	}

	double getCount_SuccessMapping(ArrayList<GEIndividual> ListIndivi) {
		int count = 0;
		int sizeList = ListIndivi.size();
		for (int i = 0; i < sizeList; i++) {
			if (ListIndivi.get(i).isMapped()) {
				count++;
			}
		}
		double ProSuccessMapping = (double) count / sizeList;
		return ProSuccessMapping;

	}

	double getCount_TrueGoldStandard(ArrayList<GEIndividual> ListIndivi) {
		int count = 0;
		int sizeList = ListIndivi.size();
		for (int i = 0; i < sizeList; i++) {
			if (ListIndivi.get(i).isEvaluated()) {
				count++;
			}
		}
		double ProSuccessGoldStandard = (double) count / sizeList;
		return ProSuccessGoldStandard;

	}

	double getCount_ComplexAxiomNumber(ArrayList<GEIndividual> ListIndivi) {
		int count = 0;
		int sizeList = ListIndivi.size();
		for (int i = 0; i < sizeList; i++) {
			if (ListIndivi.get(i).getPhenotype().toString().contains("ObjectUnionOf")
					|| ListIndivi.get(i).getPhenotype().toString().contains("ObjectIntersectionOf")
					|| ListIndivi.get(i).getPhenotype().toString().contains("ObjectAllValuesFrom")
					|| ListIndivi.get(i).getPhenotype().toString().contains("ObjectSomeValuesFrom")) {
				count++;
			}
		}
		double ProComplexAxiom = (double) count / sizeList;
		return ProComplexAxiom;

	}

	double getCount_ComplexAxiomNumber2(ArrayList<GEIndividual> ListIndivi) {
		int count = 0;
		int sizeList = ListIndivi.size();
		for (int i = 0; i < sizeList; i++) {
			if (ListIndivi.get(i).getPhenotype().toString().contains("ObjectAllValuesFrom")
					|| ListIndivi.get(i).getPhenotype().toString().contains("ObjectSomeValuesFrom")) {
				count++;
			}
		}
		double ProComplexAxiom = (double) count / sizeList;
		return ProComplexAxiom;

	}

	int Compute_Fitness_greater_threshold(double threshold_fitness, SimplePopulation CanPop) {
		int count = 0;
		for (int i = 0; i < CanPop.size(); i++) {
			if (CanPop.get(i).getFitness().getDouble() > threshold_fitness) {
				count++;
			}
		}
		// this.AddListCountThreshold(count, curGeneration);
		return count;
	}

	/*
	 * double Compute_Coefficient_Phenotype_Distinct(ArrayList<GEIndividual> CanPop)
	 * {
	 * 
	 * int i,j,n,k,m; int n1= CanPop.size(); String ai,aj;
	 * 
	 * ArrayList<Individual> arrListIndividual = new ArrayList<>(); k=0; m=0;
	 * 
	 * for (i = 0; i < n1; i++) { arrListIndividual.add(CanPop.get(i)); } int
	 * sizeList= arrListIndividual.size(); for (i = 1; i <sizeList -m; i++) { for (j
	 * = 0; j < i; j++) { ai= arrListIndividual.get(i).getPhenotype().toString();
	 * aj= arrListIndividual.get(j).getPhenotype().toString();
	 * 
	 * if (ai.equals(aj)) { m++; for (k = i; k < sizeList-m-1; k++) { //ak=
	 * array.get(k); arrListIndividual.set(k,arrListIndividual.get(k+1));
	 * 
	 * } arrListIndividual.remove(k); arrListIndividual.trimToSize(); i--; } } } int
	 * finalsizeList=arrListIndividual.size(); double kof = (double)finalsizeList/n1
	 * ; // this.AddListDiversity(kof, curGeneration); return kof;
	 * 
	 * }
	 */
	/*
	 * double Compute_Coefficient_Genotype_Distinct(ArrayList<GEIndividual> CanPop)
	 * {
	 * 
	 * int i,j,n,k,m; int n1= CanPop.size(); String ai,aj;
	 * 
	 * ArrayList<Individual> arrListIndividual = new ArrayList<>(); k=0; m=0;
	 * 
	 * for (i = 0; i < n1; i++) { arrListIndividual.add(CanPop.get(i)); } int
	 * sizeList= arrListIndividual.size(); for (i = 1; i <sizeList -m; i++) { for (j
	 * = 0; j < i; j++) { ai= arrListIndividual.get(i).getGenotype().toString(); aj=
	 * arrListIndividual.get(j).getGenotype().toString();
	 * 
	 * if (ai.equals(aj)) { m++; for (k = i; k < sizeList-m-1; k++) { //ak=
	 * array.get(k); arrListIndividual.set(k,arrListIndividual.get(k+1));
	 * 
	 * } arrListIndividual.remove(k); arrListIndividual.trimToSize(); i--; } } } int
	 * finalsizeList=arrListIndividual.size(); double kof = (double)finalsizeList/n1
	 * ; // this.AddListDiversity(kof, curGeneration); return kof;
	 * 
	 * }
	 */
}
