package com.i3s.app.rdfminer.grammar.evolutionary;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.jena.query.ResultSet;
import org.apache.log4j.Logger;

import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.RDFMiner;
import com.i3s.app.rdfminer.axiom.RandomAxiomGenerator;
import com.i3s.app.rdfminer.grammar.evolutionary.crossover.SinglePointCrossoverAxiom;
import com.i3s.app.rdfminer.grammar.evolutionary.crossover.SubtreeCrossoverAxioms;
import com.i3s.app.rdfminer.grammar.evolutionary.crossover.TwoPointCrossover;
import com.i3s.app.rdfminer.grammar.evolutionary.crossover.TypeCrossover;
import com.i3s.app.rdfminer.grammar.evolutionary.individual.GEIndividual;
import com.i3s.app.rdfminer.grammar.evolutionary.mutation.IntFlipMutation;
import com.i3s.app.rdfminer.grammar.evolutionary.selection.ProportionalRouletteWheel;
import com.i3s.app.rdfminer.sparql.SparqlEndpoint;

import Individuals.GEChromosome;
import Individuals.Genotype;
import Individuals.Phenotype;
import Individuals.FitnessPackage.Fitness;
import Individuals.Populations.Population;
import Individuals.Populations.SimplePopulation;
import Operator.Operations.TournamentSelect;
import Util.Random.MersenneTwisterFast;
import Util.Random.RandomNumberGenerator;

/**
 * This class is used to deployed all EA tools like crossover, mutation, ...
 * 
 * @author Thu Huong NGUYEN & Rémi FELIN
 */
public class EATools {

	private static Logger logger = Logger.getLogger(EATools.class.getName());

	/**
	 * delete twins from a given array of {@link GEChromosome chromosomes}
	 * 
	 * @param chromosomes a given array
	 * @param n
	 */
	public static void deleteTwins(GEChromosome[] chromosomes, int n) {
		// Let's go to the same phantom
		for (int i = 0; i < n - 1; i++) {
			for (int k = i + 1; k < n; k++) {
				if (chromosomes[k] == chromosomes[i]) {
					for (int j = k; j < n - 1; j++) {
						chromosomes[j] = chromosomes[j + 1];
					}
					n--;
					k--;
				}
			}
		}
	}

	/**
	 * Remove the duplicate(s) individual(s) from a given list and returns the
	 * filtered list
	 * 
	 * @param canPop a given list to be filtered
	 * @return the filtered list
	 */
	public static ArrayList<GEIndividual> getDistinctPopulation(ArrayList<GEIndividual> canPop) {
		ArrayList<GEIndividual> individuals = new ArrayList<GEIndividual>();
		Set<Phenotype> phenotypes = new HashSet<Phenotype>();
		for (GEIndividual item : canPop) {
			if (phenotypes.add(item.getPhenotype())) {
				individuals.add(item);
			}
		}
		return individuals;
	}

	/**
	 * Remove the duplicate(s) genotype(s) from a given list and returns the
	 * filtered list
	 * 
	 * @param canPop a given list to be filtered
	 * @return the filtered list
	 */
	public static ArrayList<GEIndividual> getDistinctGenotypePopulation(ArrayList<GEIndividual> canPop) {
		ArrayList<GEIndividual> individuals = new ArrayList<GEIndividual>();
		Set<Genotype> genotypes = new HashSet<Genotype>();
		for (GEIndividual item : canPop) {
			if (genotypes.add(item.getGenotype())) {
				individuals.add(item);
			}
		}
		return individuals;
	}

	public static SimplePopulation distincPhenotypePopulation(SimplePopulation canPop) {
		SimplePopulation distinctPopulation = new SimplePopulation();
		ArrayList<GEIndividual> individuals = new ArrayList<>();
		int n = canPop.size();
		int k, m = 0;
		for (int i = 0; i < n; i++) {
			individuals.add((GEIndividual) canPop.get(i));
		}
		for (int i = 1; i < individuals.size() - m; i++) {
			for (int j = 0; j < i; j++) {
				String ai = individuals.get(i).getPhenotype().toString();
				String aj = individuals.get(j).getPhenotype().toString();
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

	public static SimplePopulation asymmetricPopulation(SimplePopulation canPop) {
		SimplePopulation distinctPopulation = new SimplePopulation();
		ArrayList<GEIndividual> individuals = new ArrayList<>();
		int i, j, k, m;
		int n = canPop.size();
		k = m = 0;
		for (i = 0; i < n; i++) {
			individuals.add((GEIndividual) canPop.get(i));
		}
		for (i = 1; i < individuals.size() - m; i++) {
			for (j = 0; j < i; j++) {
				String ai = individuals.get(i).getPhenotype().toString();
				String aj = individuals.get(j).getPhenotype().toString();
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

	/**
	 * To compute all tasks about crossover and mutation phasis of genetical
	 * algorithm
	 * 
	 * @param canPop          the candidate population
	 * @param proCrossover    the probability to make a crossover on individual
	 * @param proMutation     the probability to make a mutation on individual
	 * @param curGeneration   the current generation
	 * @param rd              an instance of {@link RandomAxiomGenerator axioms
	 *                        generator}
	 * @param diversity       the coefficient of diversity
	 * @return a new population
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public static ArrayList<GEIndividual> crossover(ArrayList<GEIndividual> canPop, double proCrossover,
			double proMutation, int curGeneration, RandomAxiomGenerator rd, int diversity)
			throws InterruptedException, ExecutionException {

		ArrayList<GEIndividual> individuals = new ArrayList<GEIndividual>();
		int index = canPop.size() - 1;
		int m = 0;

		// We have a set of threads to compute each tasks
		ExecutorService executor = Executors.newFixedThreadPool(Global.NB_THREADS);
		Set<Callable<Void>> callables = new HashSet<Callable<Void>>();

		while (m <= index - 1) {
			final int idx = m;
			callables.add(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					
					RandomNumberGenerator rand = new MersenneTwisterFast();
					GEIndividual parent1 = ((GEIndividual) canPop.get(idx));
					GEIndividual parent2 = (GEIndividual) canPop.get(idx + 1);
					GEIndividual child1, child2;
					GEChromosome[] chromosomes;
					GEChromosome c1, c2;

					switch (RDFMiner.parameters.typeCrossover) {
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

					try {
						child1 = mutation.doOperation(child1, rd, curGeneration, child1.getMutationPoints());
						child2 = mutation.doOperation(child2, rd, curGeneration, child2.getMutationPoints());
					} catch (IOException | InterruptedException e) {
						e.printStackTrace();
					}
					// if using crowding method in survival selection
					if (diversity == 1) {
						Crowding crowd = new Crowding(4, canPop.get(idx), canPop.get(idx + 1), child1, child2);
						individuals.add(crowd.SurvivalSelection()[0]);
						individuals.add(crowd.SurvivalSelection()[1]);
					} else {
						// if choosing children for the new population
						individuals.add(child1);
						individuals.add(child2);
					}
					return null;
				}
			});
			m = m + 2;
		}
		logger.info(callables.size() + " tasks ready to be launched !");
		// Submit tasks
		executor.invokeAll(callables);
		// Shutdown the service
		executor.shutdown();
		executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
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

	public static String[][] setTablesPredicates(Logger logger, SparqlEndpoint endpoint) {
		ArrayList<String> predicates = new ArrayList<String>();
		String sparql = "distinct ?p where {?s ?p ?o}";
		String p, gp;
		ResultSet rs = endpoint.select(sparql, 0);
		int i = 0;
		while (rs.hasNext()) {
			p = rs.next().get("p").toString();
			logger.info("predicate is added: " + p);
			predicates.add(i, p);
			i++;
		}
		String[][] arr = new String[i + 1][3];
		int j = 0;
		logger.info("size of predicates list: " + predicates.size());
		while (j < predicates.size()) {
			p = predicates.get(j);
			gp = "?s <" + p + "> ?o";
			logger.info("p= " + p);
			arr[j][0] = p;
			int c = endpoint.count("?s", gp, 0);
			int d = 0;
			arr[j][1] = String.valueOf(c);
			arr[j][2] = String.valueOf(d);
			j++;
		}
		return arr;
	}

//	public static void dem(Logger logger, List<Individual> a, int n) {
//		// show off the display of the chromosomes
//		int[] fr1 = new int[n];
//		int i, j, bienDem;
//		String ai, aj;
//		for (i = 0; i < n; i++) {
//			fr1[i] = -1;
//		}
//		// logger.info("Count the number of occurrences of each chromosome");
//		// logger.info("===");
//		for (i = 0; i < n; i++) {
//			bienDem = 1;
//			for (j = i + 1; j < n; j++) {
//				ai = a.get(i).getGenotype().toString();
//				aj = a.get(j).getGenotype().toString();
//				if (ai.equals(aj)) {
//					bienDem++;
//					fr1[j] = 0;
//				}
//			}
//			if (fr1[i] != 0) {
//				fr1[i] = bienDem;
//			}
//		}
//		// Dissolve the phenomenon of flipping chromosomes in
//		for (i = 0; i < n; i++) {
//			if (fr1[i] != 0) {
//				logger.info(
//						"Chromosome '" + a.get(i).getGenotype().toString() + "' show off " + fr1[i] + " occurrences");
//			}
//		}
//	}

}
