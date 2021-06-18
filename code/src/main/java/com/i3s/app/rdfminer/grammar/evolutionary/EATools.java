package com.i3s.app.rdfminer.grammar.evolutionary;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.query.ResultSet;
import org.apache.log4j.Logger;

import com.i3s.app.rdfminer.RDFMiner;
import com.i3s.app.rdfminer.axiom.RandomAxiomGenerator;
import com.i3s.app.rdfminer.grammar.evolutionary.crossover.SinglePointCrossoverAxiom;
import com.i3s.app.rdfminer.grammar.evolutionary.crossover.SubtreeCrossoverAxioms;
import com.i3s.app.rdfminer.grammar.evolutionary.crossover.TwoPointCrossover;
import com.i3s.app.rdfminer.grammar.evolutionary.crossover.TypeCrossover;
import com.i3s.app.rdfminer.grammar.evolutionary.individual.GEIndividual;
import com.i3s.app.rdfminer.grammar.evolutionary.mutation.IntFlipMutation;
import com.i3s.app.rdfminer.grammar.evolutionary.selection.ProportionalRouletteWheel;

import Individuals.GEChromosome;
import Individuals.Genotype;
import Individuals.Individual;
import Individuals.Phenotype;
import Individuals.FitnessPackage.Fitness;
import Individuals.Populations.Population;
import Individuals.Populations.SimplePopulation;
import Operator.Operations.TournamentSelect;
import Util.Random.MersenneTwisterFast;
import Util.Random.RandomNumberGenerator;

/**
 * This class is used to deployed all EA tools like crossover, mutation, ...
 */
public class EATools {

	public static void deleteTwins(GEChromosome[] a, int n) {
		// Let's go to the same phantom
		for (int i = 0; i < n - 1; i++) {
			for (int k = i + 1; k < n; k++) {
				if (a[k] == a[i]) {
					for (int j = k; j < n - 1; j++) {
						a[j] = a[j + 1];
					}
					n--;
					k--;
				}
			}
		}
	}

	public static void dem(Logger logger, List<Individual> a, int n) {
		// show off the display of the chromosomes
		int[] fr1 = new int[n];
		int i, j, bienDem;
		String ai, aj;
		for (i = 0; i < n; i++) {
			fr1[i] = -1;
		}
		logger.info("Count the number of occurrences of each chromosome");
		logger.info("================================================================");
		for (i = 0; i < n; i++) {
			bienDem = 1;
			for (j = i + 1; j < n; j++) {
				ai = a.get(i).getGenotype().toString();
				aj = a.get(j).getGenotype().toString();
				if (ai.equals(aj)) {
					bienDem++;
					fr1[j] = 0;
				}
			}
			if (fr1[i] != 0) {
				fr1[i] = bienDem;
			}
		}
		// Dissolve the phenomenon of flipping chromosomes in
		for (i = 0; i < n; i++) {
			if (fr1[i] != 0) {
				logger.info(
						"Chromosome '" + a.get(i).getGenotype().toString() + "' show off " + fr1[i] + " occurrences");
			}
		}
	}

	public static ArrayList<GEIndividual> getDistinctPopulation(ArrayList<GEIndividual> canPop) {
		ArrayList<GEIndividual> individuals = new ArrayList<GEIndividual>();
		Set<Phenotype> ListPhenoType = new HashSet<Phenotype>();
		for (GEIndividual item : canPop) {
			if (ListPhenoType.add(item.getPhenotype())) {
				individuals.add(item);
			}
		}
		return individuals;
	}

	public static ArrayList<GEIndividual> getDistinctGenotypePopulation(ArrayList<GEIndividual> canPop) {
		ArrayList<GEIndividual> individuals = new ArrayList<GEIndividual>();
		Set<Genotype> ListGenoType = new HashSet<Genotype>();
		for (GEIndividual item : canPop) {
			if (ListGenoType.add(item.getGenotype())) {
				individuals.add(item);
			}
		}
		return individuals;
	}

	public static SimplePopulation distincPhenotypetPopulation(SimplePopulation canPop) {
		SimplePopulation distinctPopulation = new SimplePopulation();
		ArrayList<GEIndividual> arrListIndividual = new ArrayList<>();
		String ai, aj;
		int n, k, m;
		n = canPop.size();
		k = m = 0;
		for (int i = 0; i < n; i++) {
			arrListIndividual.add((GEIndividual) canPop.get(i));
		}
		for (int i = 1; i < arrListIndividual.size() - m; i++) {
			for (int j = 0; j < i; j++) {
				ai = arrListIndividual.get(i).getPhenotype().toString();
				aj = arrListIndividual.get(j).getPhenotype().toString();
				if (ai.equals(aj)) {
					m++;
					for (k = i; k < arrListIndividual.size() - m - 1; k++) {
						arrListIndividual.set(k, arrListIndividual.get(k + 1));
					}
					arrListIndividual.remove(k);
					arrListIndividual.trimToSize();
					i--;
				}
			}
		}
		for (int t = 0; t < arrListIndividual.size(); t++) {
			distinctPopulation.add(arrListIndividual.get(t));
		}
		return distinctPopulation;
	}

	public static SimplePopulation asymmetricPopulation(SimplePopulation canPop) {
		SimplePopulation distinctPopulation = new SimplePopulation();
		ArrayList<GEIndividual> individuals = new ArrayList<>();
		String ai, aj;
		int i, j, n, k, m;
		n = canPop.size();
		k = m = 0;
		for (i = 0; i < n; i++) {
			individuals.add((GEIndividual) canPop.get(i));
		}
		for (i = 1; i < individuals.size() - m; i++) {
			for (j = 0; j < i; j++) {
				ai = individuals.get(i).getPhenotype().toString();
				aj = individuals.get(j).getPhenotype().toString();
				if (ai.equals(aj)) {
					m++;
					for (k = i; k < individuals.size() - m - 1; k++) {
						individuals.set(k, individuals.get(k + 1));
					}
					individuals.remove(k);
					individuals.trimToSize();
					i--;
				}
			}
		}
		for (int t = 0; t < individuals.size(); t++) {
			distinctPopulation.add(individuals.get(t));
		}
		return distinctPopulation;
	}

	public static ArrayList<GEIndividual> crossover(ArrayList<GEIndividual> canPop, double proCrossover,
			double proMutation, int curGeneration, RandomAxiomGenerator rd, int diversity, int totalGeneration)
			throws IOException, InterruptedException {

		ArrayList<GEIndividual> individuals = new ArrayList<GEIndividual>();
		int SizePop = canPop.size();
		int index = SizePop - 1;
		int m = 0;

		while (m <= index - 1) {
			RandomNumberGenerator rand = new MersenneTwisterFast();
			GEIndividual parent1 = ((GEIndividual) canPop.get(m));
			GEIndividual parent2 = (GEIndividual) canPop.get(m + 1);
			GEIndividual child1, child2;
			GEChromosome[] chromosomes;
			GEChromosome c1, c2;

			switch (RDFMiner.parameters.typecrossover) {
			case TypeCrossover.SINGLE_POINT_CROSSOVER:
				// Single-point crossover
				SinglePointCrossoverAxiom spc = new SinglePointCrossoverAxiom(proCrossover, rand);
				spc.setFixedCrossoverPoint(false);
				c1 = new GEChromosome((GEChromosome) parent1.getGenotype().get(0));
				c2 = new GEChromosome((GEChromosome) parent2.getGenotype().get(0));
				chromosomes = spc.crossover(c1, c2);
				child1 = rd.axiomIndividual(chromosomes[0], curGeneration);
				child2 = rd.axiomIndividual(chromosomes[1], curGeneration);
				break;
			case TypeCrossover.SUBTREE_CROSSOVER:
				// subtree crossover
				SubtreeCrossoverAxioms sca = new SubtreeCrossoverAxioms(proCrossover, rand);
				GEIndividual[] inds = sca.crossoverTree(parent1, parent2);
				child1 = inds[0];
				child2 = inds[1];
				break;
			default:
				// Two point crossover
				TwoPointCrossover tpc = new TwoPointCrossover(proCrossover, rand);
				tpc.setFixedCrossoverPoint(true);
				c1 = new GEChromosome((GEChromosome) parent1.getGenotype().get(0));
				c2 = new GEChromosome((GEChromosome) parent2.getGenotype().get(0));
				chromosomes = tpc.crossover(c1, c2);
				child1 = rd.axiomIndividual(chromosomes[0], curGeneration);
				child2 = rd.axiomIndividual(chromosomes[1], curGeneration);
				break;
			}

			RandomNumberGenerator rand1 = new MersenneTwisterFast();
			IntFlipMutation mutation = new IntFlipMutation(proMutation, rand1);

			child1 = mutation.doOperation(child1, rd, curGeneration, totalGeneration, child1.getMutationPoints());
			child2 = mutation.doOperation(child2, rd, curGeneration, totalGeneration, child2.getMutationPoints());
			// logger.info("child1 after mutation:" + child1 );
			// logger.info("child2 after mutation:" + child2 );
			if (diversity == 1) {
				// if using crowding method in survival selection
				// logger.info("After crowding");
				Crowding crowd = new Crowding(4, canPop.get(m), canPop.get(m + 1), child1, child2);
				individuals.add(crowd.SurvivalSelection()[0]);
				individuals.add(crowd.SurvivalSelection()[1]);
			} else {
				// if choosing children for the new population
				individuals.add(child1);
				individuals.add(child2);
			}
			m = m + 2;
		}
		return individuals;
	}

	public static ArrayList<GEIndividual> rouletteWheel(ArrayList<GEIndividual> selectedPopulation) {
		// RouletteWheel
		int size = (int) (selectedPopulation.size());
		ArrayList<GEIndividual> newSelectedPopulation = new ArrayList<GEIndividual>();
		RandomNumberGenerator random;
		random = new MersenneTwisterFast(System.currentTimeMillis() & 0xFFFFFFFF);
		ProportionalRouletteWheel rl = new ProportionalRouletteWheel(size, random);
		rl.doOperation(((Population) selectedPopulation).getAll());
		for (int i = 0; i < selectedPopulation.size(); i++)
			newSelectedPopulation.add(selectedPopulation.get(i));
		return newSelectedPopulation;
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<GEIndividual> tournament(ArrayList<GEIndividual> selectedPopulation) {
		// Tournament
		int size = (int) (selectedPopulation.size());
		RandomNumberGenerator random;
		random = new MersenneTwisterFast(System.currentTimeMillis() & 0xFFFFFFFF);
		TournamentSelect tn = new TournamentSelect(size, size / 10, random);
		tn.selectFromTour();
		return (ArrayList<GEIndividual>) tn.getSelectedPopulation();
	}

	public static List<GEIndividual> resizeList(List<GEIndividual> individuals1, List<GEIndividual> individuals2) {
		List<GEChromosome> chromosomes = new ArrayList<GEChromosome>();
		for (int k1 = 0; k1 < individuals2.size(); k1++) {
			GEChromosome temp = (GEChromosome) individuals2.get(k1).getGenotype().get(0);
			chromosomes.add(temp);
		}
		String Chr1;
		String Chr2;
		int size = individuals1.size();
		int size2 = chromosomes.size();
		int k = size2 - 1;
		while (k >= 0) {
			for (int i = 0; i < size; i++) {
				if (size2 > 0) {
					Chr2 = chromosomes.get(k).toString();
					Chr1 = individuals1.get(i).getGenotype().get(0).toString();
					if (Chr1.equals(Chr2)) {
						chromosomes.remove(k);
						individuals1.remove(i);
						size--;
						size2--;
						i = 0;
						k--;
					}
				}
			}
		}
		return individuals1;
	}

	public static Fitness[] sortDescending(List<GEIndividual> individuals) {
		Fitness[] fit = new Fitness[individuals.size()];
		for (int i = 0; i < fit.length; i++) {
			fit[i] = individuals.get(i).getFitness();
		}
		Arrays.sort(fit);
		return fit;
	}

	public static ArrayList<GEIndividual> getSelectedList(List<GEIndividual> individuals, int size) {
		ArrayList<GEIndividual> list = new ArrayList<GEIndividual>();
		Fitness[] fit = sortDescending(individuals);
		int cnt = 0;
		while (cnt < size && cnt < individuals.size()) {
			// Avoid duplicates
			final boolean valid = fit[cnt].getIndividual().isValid();
			final boolean duplicate = list.contains(fit[cnt].getIndividual());
			if (!duplicate && valid) {
				GEIndividual ind = (GEIndividual) fit[cnt].getIndividual().clone();
				ind.setEvaluated(fit[cnt].getIndividual().isEvaluated());
				ind.setValid(fit[cnt].getIndividual().isValid());
				ind.setAge(fit[cnt].getIndividual().getAge());
				((GEIndividual) ind).setMapped(((GEIndividual) (fit[cnt].getIndividual())).isMapped());
				((GEIndividual) ind).setUsedCodons(((GEIndividual) (fit[cnt].getIndividual())).getUsedCodons());
				list.add(ind);
			}
			cnt++;
		}
		return list;
	}

	public static void setResultList(ArrayList<GEIndividual> crossovers, ArrayList<GEIndividual> individuals) {
		for (int i = 0; i < individuals.size(); i++) {
			crossovers.add(individuals.get(i));
		}
	}

	public static void setPopulation(ArrayList<GEIndividual> pop1, ArrayList<GEIndividual> pop2) {
		for (int i = 0; i < pop2.size(); i++) {
			pop1.add(pop2.get(i));
		}
	}

	public static String[][] setTablesPredicates(Logger logger) {
		String sparql = "distinct ?p where {?s ?p ?o}";
		String p = "";
		String gp = "";
		RDFMiner.endpoint.select(sparql);
		ResultSet rs = RDFMiner.endpoint.getResultset();
		ArrayList<String> predicates = new ArrayList<String>();

		int i = 0;
		while (rs.hasNext()) {
			p = rs.next().get("p").toString();
			logger.info("predicate is added: " + p);
			predicates.add(i, p);
			i++;
		}
		String[][] arr = new String[i + 1][3];
		int j = 0;
		logger.info("size list " + predicates.size());
		while (j < predicates.size()) {
			p = predicates.get(j);
			gp = "?s <" + p + "> ?o";
			logger.info("p= " + p);
			arr[j][0] = p;
			int c = RDFMiner.endpoint.count("?s", gp);
			int d = 0;
			arr[j][1] = String.valueOf(c);
			arr[j][2] = String.valueOf(d);
			j++;
		}
		return arr;
	}

}
