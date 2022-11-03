package com.i3s.app.rdfminer.grammar.evolutionary;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import Individuals.FitnessPackage.BasicFitness;
import com.i3s.app.rdfminer.entity.axiom.Axiom;
import com.i3s.app.rdfminer.entity.axiom.AxiomFactory;
import com.i3s.app.rdfminer.generator.Generator;
import com.i3s.app.rdfminer.grammar.evolutionary.selection.EliteSelection;
import com.i3s.app.rdfminer.grammar.evolutionary.selection.TruncationSelection;
import com.i3s.app.rdfminer.grammar.evolutionary.selection.TypeSelection;
import com.i3s.app.rdfminer.mode.Mode;
import com.i3s.app.rdfminer.entity.shacl.Shape;
import com.i3s.app.rdfminer.entity.shacl.ShapesManager;
import com.i3s.app.rdfminer.entity.shacl.ValidationReport;
import com.i3s.app.rdfminer.parameters.CmdLineParameters;
import com.i3s.app.rdfminer.sparql.corese.CoreseEndpoint;
import com.i3s.app.rdfminer.sparql.corese.CoreseService;
import org.apache.jena.query.ResultSet;
import org.apache.log4j.Logger;

import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.RDFMiner;
import com.i3s.app.rdfminer.grammar.evolutionary.crossover.SinglePointCrossoverAxiom;
import com.i3s.app.rdfminer.grammar.evolutionary.crossover.SubtreeCrossoverAxioms;
import com.i3s.app.rdfminer.grammar.evolutionary.crossover.TwoPointCrossover;
import com.i3s.app.rdfminer.grammar.evolutionary.crossover.TypeCrossover;
import com.i3s.app.rdfminer.grammar.evolutionary.fitness.AxiomFitnessEvaluation;
import com.i3s.app.rdfminer.grammar.evolutionary.individual.GEIndividual;
import com.i3s.app.rdfminer.grammar.evolutionary.mutation.IntFlipMutation;
import com.i3s.app.rdfminer.grammar.evolutionary.selection.ProportionalRouletteWheel;
import com.i3s.app.rdfminer.sparql.virtuoso.VirtuosoEndpoint;
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

	private static final Logger logger = Logger.getLogger(EATools.class.getName());

	/**
	 * delete twins from a given array of {@link GEChromosome chromosomes}
	 * 
	 * @param chromosomes a given array
	 */
	public static void deleteTwins(GEChromosome[] chromosomes, int n) {
		// Let's go to the same phantom
		for (int i = 0; i < n - 1; i++) {
			for (int k = i + 1; k < n; k++) {
				if (chromosomes[k] == chromosomes[i]) {
					if (n - 1 - k >= 0) System.arraycopy(chromosomes, k + 1, chromosomes, k, n - 1 - k);
					n--;
					k--;
				}
			}
		}
	}

	/**
	 * Remove the duplicate(s) phenotype(s) from a given list and returns the
	 * filtered list
	 * 
	 * @param canPop a given list to be filtered
	 * @return the filtered list
	 */
	public static ArrayList<GEIndividual> getDistinctPhenotypePopulationFromAxioms(ArrayList<GEIndividual> canPop) {
		ArrayList<GEIndividual> individuals = new ArrayList<>();
		Set<Phenotype> phenotypes = new HashSet<>();
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
	 * @param axioms a given list to be filtered
	 * @return the filtered list
	 */
	public static ArrayList<Axiom> getDistinctGenotypePopulationFromAxioms(ArrayList<Axiom> axioms) {
		ArrayList<Axiom> distinctAxioms = new ArrayList<>();
		Set<Genotype> genotypes = new HashSet<>();
		for (Axiom axiom : axioms) {
			if (genotypes.add(axiom.individual.getGenotype())) {
				distinctAxioms.add(axiom);
			}
		}
		return distinctAxioms;
	}

	public static ArrayList<Shape> getDistinctGenotypePopulationFromShapes(ArrayList<Shape> shapes) {
		ArrayList<Shape> distinctShapes = new ArrayList<>();
		Set<Genotype> genotypes = new HashSet<>();
		for (Shape shape : shapes) {
			if (genotypes.add(shape.individual.getGenotype())) {
				distinctShapes.add(shape);
			}
		}
		return distinctShapes;
	}


	public static ArrayList<GEIndividual> getTypeSelection(int type, ArrayList<GEIndividual> selectedPopulation, int sizeElite, int sizeSelection) {
		switch (type) {
			case TypeSelection.ROULETTE_WHEEL:
				// Roulette wheel method
				// if6
				logger.info("Type selection: Roulette Wheel");
				return EATools.rouletteWheel(selectedPopulation);
			case TypeSelection.TRUNCATION:
				// Truncation selection method
				// if7
				logger.info("Type selection: Truncation");
				TruncationSelection truncation = new TruncationSelection(sizeSelection);
				truncation.setParentsSelectionElitism(selectedPopulation);
				return truncation.setupSelectedPopulation(RDFMiner.parameters.populationSize - sizeElite);
			case TypeSelection.TOURNAMENT:
				// Tournament method
				// if8
				logger.info("Type selection: Tournament");
				return EATools.tournament(selectedPopulation);
			default:
				// Normal crossover way - All individual of the current generation
				// will be selected for crossover operation to create the new
				// population
				// if6
				logger.info("Type selection: Normal");
				return null;
		}
	}

	/**
	 * Renew a given axioms population
	 * @param axioms a given population
	 * @param curGeneration the current generation
	 * @param etilismAxioms a etilism population
	 * @return a renewed population
	 */
	public static ArrayList<Axiom> renewAxioms(int curGeneration, ArrayList<Axiom> axioms, ArrayList<Axiom> etilismAxioms) {
		ArrayList<Axiom> newAxioms = new ArrayList<>();
		if (etilismAxioms != null) {
			for (Axiom etilismAxiom : etilismAxioms) {
				logger.info("Elitism axiom: " + etilismAxiom.axiomId + "\n genotype: " + etilismAxiom.individual.getGenotype() + " fitness = " + etilismAxiom.individual.getFitness().getDouble());
				etilismAxiom.individual.setAge(curGeneration);
				newAxioms.add(etilismAxiom);
			}
		}
		for (Axiom axiom : axioms) {
			axiom.individual.setAge(curGeneration);
			newAxioms.add(axiom);
		}
		return newAxioms;
	}

	/**
	 * Renew a given SHACL Shapes population
	 * @param shapes a given population
	 * @param curGeneration the current generation
	 * @param etilismShapes a etilism population
	 * @return a renewed population
	 */
	public static ArrayList<Shape> renewShapes(int curGeneration, ArrayList<Shape> shapes, ArrayList<Shape> etilismShapes) {
		ArrayList<Shape> newShapes = new ArrayList<>();
		if (etilismShapes != null) {
			for (Shape etilismShape : etilismShapes) {
				etilismShape.individual.setAge(curGeneration);
				newShapes.add(etilismShape);
			}
		}
		for (Shape shape : shapes) {
			shape.individual.setAge(curGeneration);
			newShapes.add(shape);
		}
		return newShapes;
	}

	public static SimplePopulation distinctPhenotypePopulation(SimplePopulation canPop) {
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
		for (GEIndividual individual : individuals) {
			distinctPopulation.add(individual);
		}
		return distinctPopulation;
	}

	public static SimplePopulation asymmetricPopulation(SimplePopulation canPop) {
		SimplePopulation distinctPopulation = new SimplePopulation();
		ArrayList<GEIndividual> individuals = new ArrayList<>();
		int k, m = 0;
		int n = canPop.size();
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
		for (GEIndividual individual : individuals) {
			distinctPopulation.add(individual);
		}
		return distinctPopulation;
	}

	/**
	 * To compute all tasks about crossover, mutation and evaluation phasis of
	 * genetical algorithm
	 * 
	 * @param axioms        the candidate population
	 * @param proCrossover  the probability to make a crossover on individual
	 * @param proMutation   the probability to make a mutation on individual
	 * @param curGeneration the current generation
	 * @param generator     an instance of {@link Generator Generator}
	 * @param diversity     the coefficient of diversity
	 * @return a new population
	 */
	public static ArrayList<Axiom> computeAxiomsGeneration(ArrayList<Axiom> axioms, double proCrossover,
															double proMutation, int curGeneration, Generator generator, int diversity)
			throws InterruptedException, ExecutionException, IOException, URISyntaxException {

		ArrayList<Axiom> newAxioms = new ArrayList<>();
		// We have a set of threads to compute each tasks
		ExecutorService executor = Executors.newFixedThreadPool(Global.NB_THREADS);
		Set<Callable<Axiom[]>> axiomsCallables = new HashSet<>();
		logger.info("Performing crossover and mutation ...");
		logger.info("The axioms will be evaluated using the following SPARQL Endpoint : " + Global.SPARQL_ENDPOINT);
		// In this version, we only consider OWL axioms
		if(RDFMiner.parameters.useNoveltySearch) {
			NoveltySearch noveltySearch = new NoveltySearch(new CoreseEndpoint(Global.CORESE_SPARQL_ENDPOINT, Global.TRAINING_SPARQL_ENDPOINT, Global.PREFIXES));
			axioms = noveltySearch.updateSimilarities(axioms);
		}

		int m = 0;
		while (m <= axioms.size() - 2) {

			RandomNumberGenerator rand = new MersenneTwisterFast();
			// get the two individuals which are neighbours
			GEIndividual parent1 = axioms.get(m).individual;
			GEIndividual parent2 = axioms.get(m + 1).individual;
			GEIndividual child1, child2;
			GEChromosome[] chromosomes;

			/* CROSSOVER PHASIS */
			switch (RDFMiner.parameters.typeCrossover) {
				case TypeCrossover.SINGLE_POINT_CROSSOVER:
					// Single-point crossover
					SinglePointCrossoverAxiom spc = new SinglePointCrossoverAxiom(proCrossover, rand, generator, curGeneration);
					spc.setFixedCrossoverPoint(true);
					child1 = parent1;
					child2 = parent2;
//					logger.
					GEIndividual[] childs = spc.doOperation(child1, child2);
					child1 = childs[0];
					child2 = childs[1];
					logger.info("---");
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
					chromosomes = tpc.crossover(
							new GEChromosome((GEChromosome) parent1.getGenotype().get(0)),
							new GEChromosome((GEChromosome) parent2.getGenotype().get(0))
					);
					child1 = generator.getIndividualFromChromosome(chromosomes[0], curGeneration);
					child2 = generator.getIndividualFromChromosome(chromosomes[1], curGeneration);
					break;
			}

			/* MUTATION PHASIS */
//			RandomNumberGenerator rand1 = new MersenneTwisterFast();
			IntFlipMutation mutation = new IntFlipMutation(proMutation, new MersenneTwisterFast());
			// make mutation and return new childs from it
			GEIndividual newChild1 = mutation.doOperation(child1, generator, curGeneration, child1.getMutationPoints());
			GEIndividual newChild2 = mutation.doOperation(child2, generator, curGeneration, child2.getMutationPoints());
			// if using crowding method in survival selection
			if (diversity == 1) {
				// fill callables of crowding to compute
				ArrayList<Axiom> parentAxioms = axioms;
				int finalM = m;
				axiomsCallables.add(() -> new Crowding(
						parentAxioms,
						parentAxioms.get(finalM),
						parentAxioms.get(finalM +1),
						AxiomFactory.create(newChild1, newChild1.getPhenotype(), new CoreseEndpoint(Global.CORESE_SPARQL_ENDPOINT, Global.SPARQL_ENDPOINT, Global.PREFIXES)),
						AxiomFactory.create(newChild2, newChild2.getPhenotype(), new CoreseEndpoint(Global.CORESE_SPARQL_ENDPOINT, Global.SPARQL_ENDPOINT, Global.PREFIXES)))
						.getAxiomsSurvivalSelection());
			}
			m = m + 2;
		}

		logger.info("Crossover & Mutation done");
		logger.info("CROWDING diversity method used ...");
		logger.info(axiomsCallables.size() + " tasks ready to be launched !");
		// Submit tasks
		List<Future<Axiom[]>> futures = executor.invokeAll(axiomsCallables);
		// fill the evaluated individuals
		for (Future<Axiom[]> future : futures) {
			newAxioms.addAll(Arrays.asList(future.get()));
		}
		// Shutdown the service
		executor.shutdown();
		executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		return newAxioms;
	}

	/**
	 * To compute all tasks about crossover, mutation and evaluation phasis of
	 * genetical algorithm
	 *
	 * @param shapes        the candidate population
	 * @param proCrossover  the probability to make a crossover on individual
	 * @param proMutation   the probability to make a mutation on individual
	 * @param curGeneration the current generation
	 * @param generator     an instance of {@link Generator Generator}
	 * @param diversity     the coefficient of diversity
	 * @return a new population
	 */
	public static ArrayList<Shape> computeShapesGeneration(ArrayList<Shape> shapes, double proCrossover,
														   double proMutation, int curGeneration, Generator generator, int diversity)
			throws InterruptedException, IOException, URISyntaxException {

		ArrayList<GEIndividual> evaluatedIndividuals = new ArrayList<>();

		logger.info("Performing crossover and mutation ...");
//		logger.info("The SHACL Shapes will be evaluated using the following SPARQL Endpoint : " + Global.SPARQL_ENDPOINT);
		List<Crowding> shapesToEvaluate = new ArrayList<>();

		int m = 0;
		while (m <= shapes.size() - 2) {

			RandomNumberGenerator rand = new MersenneTwisterFast();
			// get the two individuals which are neighbours
			GEIndividual parent1 = shapes.get(m).individual;
			GEIndividual parent2 = shapes.get(m + 1).individual;
			GEIndividual child1, child2;
			GEChromosome[] chromosomes;
			GEChromosome c1, c2;

			/* CROSSOVER PHASIS */
			switch (RDFMiner.parameters.typeCrossover) {
				case TypeCrossover.SINGLE_POINT_CROSSOVER:
					// Single-point crossover
					// TODO
//					SinglePointCrossoverAxiom spc = new SinglePointCrossoverAxiom(proCrossover, rand);
//					spc.setFixedCrossoverPoint(false);
					child1 = parent1;
					child2 = parent2;
//					logger.
//					spc.doOperation(List.of(child1, child2));
//					logger.info("");
//					c1 = new GEChromosome((GEChromosome) parent1.getGenotype().get(0));
//					c2 = new GEChromosome((GEChromosome) parent2.getGenotype().get(0));
//					chromosomes = spc.crossover(c1, c2);
//					child1 = generator.getIndividualFromChromosome(chromosomes[0], curGeneration);
//					child2 = generator.getIndividualFromChromosome(chromosomes[1], curGeneration);
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
					child1 = generator.getIndividualFromChromosome(chromosomes[0], curGeneration);
					child2 = generator.getIndividualFromChromosome(chromosomes[1], curGeneration);
					break;
			}

			/* MUTATION PHASIS */
//			RandomNumberGenerator rand1 = new MersenneTwisterFast();
			IntFlipMutation mutation = new IntFlipMutation(proMutation, new MersenneTwisterFast());
			// make mutation and return new childs from it
			GEIndividual newChild1 = mutation.doOperation(child1, generator, curGeneration, child1.getMutationPoints());
			GEIndividual newChild2 = mutation.doOperation(child2, generator, curGeneration, child2.getMutationPoints());
			// if using crowding method in survival selection
			if (diversity == 1) {
				logger.info("CROWDING diversity method used ...");
				shapesToEvaluate.add(new Crowding(parent1, parent2, newChild1, newChild2));
			}
			m = m + 2;
		}
		logger.info("Crossover & Mutation done");
		logger.info(shapesToEvaluate.size() + " couples of new shapes (childs) to evaluate ...");
		// for each child in crowding method
		ArrayList<GEIndividual> childs = new ArrayList<>();
		for(Crowding crowding : shapesToEvaluate) {
			childs.add(crowding.shapeChild1);
			childs.add(crowding.shapeChild2);
		}
//				logger.info("Size crowding list: " + shapesToEvaluate.size());
		// evaluate them
		ShapesManager shapesManager = new ShapesManager(childs);
		// launch evaluation
		CoreseEndpoint endpoint = new CoreseEndpoint(Global.SPARQL_ENDPOINT, Global.PREFIXES);
		logger.info("Launch evaluation report for new childs ...");
		String report = endpoint.getValidationReportFromServer(shapesManager.file, CoreseService.PROBABILISTIC_SHACL_EVALUATION);
		// read evaluation report
//				logger.info("[DEBUG] report :\n" + report);
		ValidationReport validationReport = new ValidationReport(report);
		// set each values finded for each child
		ArrayList<Shape> childShapes = new ArrayList<>();
		for(Shape shape : shapesManager.population) {
			shape.fillParamFromReport(validationReport);
			// add new childs in a list
			childShapes.add(shape);
			// modify crowding with updated childs
			for(Crowding crowding : shapesToEvaluate) {
				if(crowding.shapeChild1 == shape.individual) {
					// set the fitness of the child
					BasicFitness fit = new BasicFitness((Double) shape.fitness, crowding.shapeChild1);
					fit.setIndividual(crowding.shapeChild1);
					fit.getIndividual().setValid(true);
					crowding.shapeChild1.setFitness(fit);
				} else if(crowding.shapeChild2 == shape.individual) {
					// set the fitness of the child
					BasicFitness fit = new BasicFitness((Double) shape.fitness, crowding.shapeChild2);
					fit.setIndividual(crowding.shapeChild2);
					fit.getIndividual().setValid(true);
					crowding.shapeChild2.setFitness(fit);
				}
			}
		}
		// launch survival selection and save it in evaluatedIndividuals list
		for(Crowding crowding : shapesToEvaluate) {
			evaluatedIndividuals.addAll(List.of(crowding.getShapesSurvivalSelection()));
		}

		ArrayList<Shape> newShapes = new ArrayList<>();
		for(GEIndividual survival : evaluatedIndividuals) {
			for(Shape shape : shapes) {
				if(survival.getGenotype() == shape.individual.getGenotype()) {
					newShapes.add(shape);
				}
			}
			for(Shape shape : childShapes) {
				if(survival.getGenotype() == shape.individual.getGenotype()) {
					newShapes.add(shape);
				}
			}
		}
		// return the modified individuals
		return newShapes;

	}

	public static ArrayList<GEIndividual> rouletteWheel(ArrayList<GEIndividual> selectedPopulation) {
		// RouletteWheel
		int size = selectedPopulation.size();
		RandomNumberGenerator random;
		random = new MersenneTwisterFast(System.currentTimeMillis());
		ProportionalRouletteWheel rl = new ProportionalRouletteWheel(size, random);
		rl.doOperation(((Population) selectedPopulation).getAll());
		return new ArrayList<>(selectedPopulation);
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<GEIndividual> tournament(ArrayList<GEIndividual> selectedPopulation) {
		// Tournament
		int size = selectedPopulation.size();
		RandomNumberGenerator random;
		random = new MersenneTwisterFast(System.currentTimeMillis());
		TournamentSelect tn = new TournamentSelect(size, size / 10, random);
		tn.selectFromTour();
		return (ArrayList<GEIndividual>) tn.getSelectedPopulation();
	}

	public static List<GEIndividual> resizeList(List<GEIndividual> individuals1, List<GEIndividual> individuals2) {
		List<GEChromosome> chromosomes = new ArrayList<>();
		for (GEIndividual individual : individuals2) {
			GEChromosome temp = (GEChromosome) individual.getGenotype().get(0);
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
		ArrayList<GEIndividual> list = new ArrayList<>();
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
				ind.setMapped(((GEIndividual) (fit[cnt].getIndividual())).isMapped());
				ind.setUsedCodons(((GEIndividual) (fit[cnt].getIndividual())).getUsedCodons());
				list.add(ind);
			}
			cnt++;
		}
		return list;
	}

	public static String[][] setTablesPredicates(Logger logger, VirtuosoEndpoint endpoint) {
		ArrayList<String> predicates = new ArrayList<>();
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

	public static ArrayList<Axiom> bindIndividualsWithAxioms(ArrayList<GEIndividual> individuals, ArrayList<Axiom> axioms) {
		ArrayList<Axiom> newAxioms = new ArrayList<>();
		for(GEIndividual individual : individuals) {
			for(Axiom axiom : axioms) {
				if(individual.getGenotype() == axiom.individual.getGenotype()) {
					newAxioms.add(axiom);
				}
			}
		}
		return newAxioms;
	}

	public static ArrayList<Shape> bindIndividualsWithShapes(ArrayList<GEIndividual> individuals, ArrayList<Shape> shapes) {
		ArrayList<Shape> newShapes = new ArrayList<>();
		for(GEIndividual individual : individuals) {
			for(Shape shape : shapes) {
				if(individual.getGenotype() == shape.individual.getGenotype()) {
					newShapes.add(shape);
				}
			}
		}
		return newShapes;
	}

	public static ArrayList<GEIndividual> getIndividualsFromAxioms(ArrayList<Axiom> axioms) {
		ArrayList<GEIndividual> individuals = new ArrayList<>();
		for(Axiom axiom : axioms) {
			individuals.add(axiom.individual);
		}
		return individuals;
	}

	public static ArrayList<GEIndividual> getIndividualsFromShapes(ArrayList<Shape> shapes) {
		ArrayList<GEIndividual> individuals = new ArrayList<>();
		for(Shape shape : shapes) {
			individuals.add(shape.individual);
		}
		return individuals;
	}

}
