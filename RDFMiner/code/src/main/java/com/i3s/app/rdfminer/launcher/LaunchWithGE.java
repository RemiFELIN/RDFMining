package com.i3s.app.rdfminer.launcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.i3s.app.rdfminer.axiom.Type;
import com.i3s.app.rdfminer.generator.Generator;
import com.i3s.app.rdfminer.generator.shacl.RandomShapeGenerator;
import com.i3s.app.rdfminer.shacl.ShapesManager;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.RDFMiner;
import com.i3s.app.rdfminer.generator.axiom.RandomAxiomGenerator;
import com.i3s.app.rdfminer.grammar.evolutionary.EATools;
import com.i3s.app.rdfminer.grammar.evolutionary.fitness.FitnessEvaluation;
import com.i3s.app.rdfminer.grammar.evolutionary.individual.CandidatePopulation;
import com.i3s.app.rdfminer.grammar.evolutionary.individual.GEIndividual;
import com.i3s.app.rdfminer.grammar.evolutionary.selection.EliteSelection;
import com.i3s.app.rdfminer.grammar.evolutionary.selection.TruncationSelection;
import com.i3s.app.rdfminer.grammar.evolutionary.selection.TypeSelection;
import com.i3s.app.rdfminer.output.GenerationJSON;
import com.i3s.app.rdfminer.output.ResultsJSON;
import com.i3s.app.rdfminer.output.StatJSON;
import com.i3s.app.rdfminer.parameters.CmdLineParameters;
import com.i3s.app.rdfminer.statistics.Statistics;

import Individuals.GEChromosome;

public class LaunchWithGE {

	private static final Logger logger = Logger.getLogger(LaunchWithGE.class.getName());

	/**
	 * The second version of RDFMiner launcher, with Grammar Evolutionary
	 * 
	 * @param parameters all parameters given in the execution of JAR
	 */
	public void run(CmdLineParameters parameters) throws Exception {

		// ShutDownHook
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			logger.warn("Shutting down RDFMiner ...");
			// Save results in output file
			writeAndFinish();
		}));

		RDFMiner.results = new ResultsJSON();
		RDFMiner.axioms = new ArrayList<>();
		RDFMiner.stats = new StatJSON();

		Generator generator = null;

		if (parameters.axiomFile == null) {
			if (parameters.useRandomAxiomGenerator) {
				// if a randomly generated Axiom already exists then continue
				// to generate a new Axioms based on BNF
				logger.info("Initializing the random axiom generator with grammar " + parameters.grammarFile + "...");
				generator = new RandomAxiomGenerator(parameters.grammarFile, true);
			} else if (parameters.useRandomShaclShapesGenerator) {
				// launch random SHACL Shapes generator
				logger.info("Initializing the random SHACL Shapes generator with grammar " + parameters.grammarFile + "...");
				generator = new RandomShapeGenerator(parameters.grammarFile);
			}
		}
		// Create the results file
		try {
			RDFMiner.output = new FileWriter(RDFMiner.outputFolder + Global.RESULTS_FILENAME);
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}

		/* GRAMMATICAL EVOLUTIONARY */
		/* Parameters as the inputs of GE */
		logger.info("========================================================");
		logger.info("PARAMETERS AS THE INPUTS OF GE");
		logger.info("========================================================");
		logger.info("POPULATION SIZE : " + parameters.populationSize);
		logger.info("TOTAL EFFORT : " + parameters.kBase);
		logger.info("MAX GENERATION NUMBER: " + (parameters.kBase / parameters.populationSize));
		logger.info("INITIALIZED LENGTH CHROMOSOME: " + parameters.initLenChromosome);
		logger.info("MAXIMUM WRAPPING: " + parameters.maxWrapp);
		logger.info("CROSSOVER PROBABILITY: " + parameters.proCrossover);
		logger.info("MUTATION PROBABILITY: " + parameters.proMutation);
		logger.info("TIME-CAP: " + parameters.timeOut + " secondes");
		logger.info("========================================================");
		logger.info("NUMBER OF THREAD(S) USED: " + Global.NB_THREADS);
		logger.info("========================================================");

		GEChromosome[] chromosomes = new GEChromosome[parameters.populationSize];

		ArrayList<GEIndividual> candidatePopulation;
		ArrayList<GEIndividual> etilismPopulation = null;
		Statistics stat = new Statistics();

		int sizeElite = (int) (parameters.sizeElite * parameters.populationSize);
		int sizeSelection = (int) (parameters.sizeSelection * parameters.populationSize);

		int curCheckpoint;
		int curGeneration;
		boolean flag = false;
		Reader buffer;
		File tempFile = new File(RDFMiner.outputFolder + "buffer_size" + parameters.populationSize + ".txt");
		if (!tempFile.exists()) {
			buffer = null;
			curCheckpoint = curGeneration = 1;
		} else {
			FileInputStream reader = new FileInputStream(
					RDFMiner.outputFolder + "buffer_size" + parameters.populationSize + ".txt");
			buffer = new InputStreamReader(reader, StandardCharsets.UTF_8);
			int intch;
			StringBuilder st = new StringBuilder();
			while ((intch = reader.read()) != '\n') {
				st.append((char) intch);
			}
			curGeneration = Integer.parseInt(st.toString());
			st = new StringBuilder();
			while ((intch = reader.read()) != '\n') {
				st.append((char) intch);
			}
			curCheckpoint = Integer.parseInt(st.toString());
			logger.info("Buffer file founded ! starting from gen." + curGeneration + " ...");
		}
		logger.info("Initializing candidate population in generation " + curGeneration + "...");
		CandidatePopulation canPop = new CandidatePopulation(parameters.populationSize, generator,
				parameters.typeInitialization, chromosomes, parameters.initLenChromosome, parameters.maxValCodon,
				parameters.maxWrapp);
		candidatePopulation = canPop.initialize(buffer, curGeneration);

		logger.info("--------- [TMP] phenotype generated :");
		for(GEIndividual ind : candidatePopulation) {
			logger.info(ind.getPhenotype());
		}

		// test shapesManager
		logger.info("--------- [TMP] ShapesManager : cleaned population :");
		ShapesManager shapesManager = new ShapesManager(candidatePopulation);
		shapesManager.printPopulation();

		// Fill the 'stats' part of the JSON output
		RDFMiner.stats.populationSize = parameters.populationSize;
		RDFMiner.stats.maxLengthChromosome = parameters.initLenChromosome;
		RDFMiner.stats.maxWrapping = parameters.maxWrapp;
		RDFMiner.stats.crossoverProbability = parameters.proCrossover;
		RDFMiner.stats.mutationProbability = parameters.proMutation;
		RDFMiner.stats.timeOut = (int) parameters.timeOut;

		if (parameters.elitism == 1) {
			RDFMiner.stats.elitismSelection = true;
			RDFMiner.stats.eliteSize = parameters.sizeElite;
		} else {
			RDFMiner.stats.elitismSelection = false;
		}

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

		while (curCheckpoint <= parameters.checkpoint) {
			System.out.println("\n--------------------------------------------------------\n");
			logger.info("Generation: " + curGeneration);
			FitnessEvaluation fit = new FitnessEvaluation();
			// First step of the grammatical evolution
			if ((curGeneration == 1) || ((buffer != null) && (!flag))) {
				// if1
				logger.info("Begin evaluating individuals...");
				logger.info("Evaluating axioms against to the RDF Data of the minimized DBPedia");
				fit.updatePopulation(candidatePopulation, curGeneration, false, null);
			}
			// Checkpoint reached, this is a code to evaluate and save axioms in output file
			if (parameters.populationSize * curGeneration == parameters.kBase * curCheckpoint) {

				List<JSONObject> axioms = new ArrayList<>();

				fit.display(candidatePopulation, axioms, curGeneration);

				ArrayList<GEIndividual> candidatePopulation2 = new ArrayList<>();
				for (GEIndividual geIndividual : candidatePopulation) {
					GEIndividual indivi = new GEIndividual();
					indivi.setMapper(geIndividual.getMapper());
					indivi.setGenotype(geIndividual.getGenotype());
					indivi.setPhenotype(geIndividual.getPhenotype());
					indivi.setMapped(geIndividual.isMapped());
					candidatePopulation2.add(indivi);
				}
				logger.info("Evaluating axioms against to the RDF Data of the whole DBPedia.");
				fit.updatePopulation(candidatePopulation2, curGeneration, true, axioms);
				RDFMiner.axioms.addAll(axioms);
				curCheckpoint++;
			}

			ArrayList<GEIndividual> distinctCandidatePopulation = EATools.getDistinctPopulation(candidatePopulation);
			ArrayList<GEIndividual> distinctGenotypeCandidatePopulation = EATools
					.getDistinctGenotypePopulation(candidatePopulation);

			GenerationJSON generation = new GenerationJSON();
			generation.idGeneration = curGeneration;
			generation.numSuccessMapping = stat.getCountSuccessMapping(distinctCandidatePopulation);
			generation.diversityCoefficient = (double) distinctCandidatePopulation.size() / candidatePopulation.size();
			generation.genotypeDiversityCoefficient = (double) distinctGenotypeCandidatePopulation.size()
					/ candidatePopulation.size();
			generation.averageFitness = stat.computeAverageFitness(distinctCandidatePopulation);
			generation.numComplexAxiom = stat.getCountComplexAxiom(distinctCandidatePopulation);
			generation.numComplexAxiomSpecial = stat.getCountComplexAxiomSpecial(distinctCandidatePopulation);
			RDFMiner.stats.generations.add(generation.toJSON());

			if (curGeneration * parameters.populationSize < parameters.kBase * parameters.checkpoint) {
				// if4
				// STEP 3 - SELECTION OPERATION - Reproduce Selection - Parent Selection
				ArrayList<GEIndividual> crossoverPopulation, selectedPopulation;
				if (parameters.elitism == 1) {
					// Elitism method, which copies the best chromosome( or a few best
					// chromosome) to new population. The rest done classical way. it
					// prevents losing the best found solution
					logger.info("Selecting elite individuals...");
					logger.info("Selecting + " + (int) (parameters.sizeElite * 100)
							+ "% elite individuals for the new population");
					logger.info("The size of elite population: " + sizeElite);
					EliteSelection elite = new EliteSelection(sizeElite);
					elite.setParentsSelectionElitism(distinctCandidatePopulation);
					selectedPopulation = elite.setupSelectedPopulation(distinctCandidatePopulation);
					etilismPopulation = elite.getElitedPopulation();

				} else {
					selectedPopulation = distinctCandidatePopulation;
					sizeElite = 0;
				}

				switch (parameters.typeSelect) {
				case TypeSelection.ROULETTE_WHEEL:
					// Roulette wheel method
					// if6
					logger.info("Type selection: Roulette Wheel");
					crossoverPopulation = EATools.rouletteWheel(selectedPopulation);
					break;
				case TypeSelection.TRUNCATION:
					// Truncation selection method
					// if7
					logger.info("Type selection: Truncation");
					TruncationSelection truncation = new TruncationSelection(sizeSelection);
					truncation.setParentsSelectionElitism(selectedPopulation);
					crossoverPopulation = truncation.setupSelectedPopulation(selectedPopulation,
							parameters.populationSize - sizeElite);
					break;
				case TypeSelection.TOURNAMENT:
					// Tournament method
					// if8
					logger.info("Type selection: Tournament");
					crossoverPopulation = EATools.tournament(selectedPopulation);
					break;
				default:
					// Normal crossover way - All individual of the current generation
					// will be selected for crossover operation to create the new
					// population
					// if6
					logger.info("Type selection: Normal");
					crossoverPopulation = candidatePopulation;
					break;
				}

				/* STEP 4 - CROSSOVER OPERATION */
				// Crossover single point between 2 individuals of the selected population
				ArrayList<GEIndividual> crossoverList = new ArrayList<>(crossoverPopulation);
				// shuffle populations before crossover & mutation
				java.util.Collections.shuffle(crossoverList);

				// Add new population on a new list of individuals
				ArrayList<GEIndividual> newPopulation = EATools.computeGeneration(crossoverList,
						parameters.proCrossover, parameters.proMutation, curGeneration, generator,
						parameters.diversity);

				candidatePopulation = canPop.renew(newPopulation, curGeneration, etilismPopulation);
				// Turn to the next generation
				curGeneration++;

				// Write to buffer file
				PrintWriter writer = new PrintWriter(
						RDFMiner.outputFolder + "buffer_size" + parameters.populationSize + ".txt", StandardCharsets.UTF_8);
				writer.println(curGeneration);
				writer.println(curCheckpoint);
				flag = true;
				for (GEIndividual geIndividual : candidatePopulation) {
					writer.println(geIndividual.getGenotype().toString().substring(22,
							geIndividual.getGenotype().toString().length() - 1));
				}
				writer.close();
			} else {
				logger.info("Evolutionary process is done...");
				break;
			}
		}
		System.exit(0);
	}

	public static void writeAndFinish() {
		try {
			RDFMiner.results.stats = RDFMiner.stats.toJSON();
			RDFMiner.results.type = RDFMiner.type;
			// sort axioms (by ARI or Generality) using type of axioms
			RDFMiner.axioms.sort(Comparator.comparingDouble(j -> {
				// if we have disjoint classes axioms, we need to sort using generality
				if(RDFMiner.type == Type.DISJOINT_CLASSES)
					return j.getInt("generality");
				return j.getDouble("ari");
			}));
			RDFMiner.results.axioms = RDFMiner.axioms;
			RDFMiner.output.write(RDFMiner.results.toJSON().toString());
			RDFMiner.output.close();
		} catch (IOException e) {
			logger.error("I/O error while closing JSON writer: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}

}
