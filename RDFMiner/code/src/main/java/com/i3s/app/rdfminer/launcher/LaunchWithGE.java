package com.i3s.app.rdfminer.launcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
//import java.util.concurrent.Executors;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
//import org.apache.poi.ss.usermodel.Cell;
//import org.apache.poi.ss.usermodel.Row;
//import org.apache.poi.xssf.usermodel.XSSFSheet;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONObject;

import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.RDFMiner;
import com.i3s.app.rdfminer.axiom.RandomAxiomGenerator;
import com.i3s.app.rdfminer.grammar.evolutionary.EATools;
import com.i3s.app.rdfminer.grammar.evolutionary.fitness.FitnessEvaluation;
import com.i3s.app.rdfminer.grammar.evolutionary.individual.CandidatePopulation;
import com.i3s.app.rdfminer.grammar.evolutionary.individual.GEIndividual;
import com.i3s.app.rdfminer.grammar.evolutionary.selection.EliteSelection;
import com.i3s.app.rdfminer.grammar.evolutionary.selection.TruncationSelection;
import com.i3s.app.rdfminer.grammar.evolutionary.selection.TypeSelection;
//import com.i3s.app.rdfminer.output.AxiomJSON;
import com.i3s.app.rdfminer.output.GenerationJSON;
import com.i3s.app.rdfminer.output.ResultsJSON;
import com.i3s.app.rdfminer.output.StatJSON;
import com.i3s.app.rdfminer.parameters.CmdLineParameters;
import com.i3s.app.rdfminer.sparql.SparqlEndpoint;
import com.i3s.app.rdfminer.statistics.Statistics;

import Individuals.GEChromosome;
//import jxl.Workbook;
//import jxl.write.Label;
//import jxl.write.WritableSheet;
//import jxl.write.WritableWorkbook;

public class LaunchWithGE {

	private static Logger logger = Logger.getLogger(LaunchWithGE.class.getName());

	/**
	 * The second version of RDFMiner launcher, with Grammar Evolutionary
	 * 
	 * @throws Exception
	 * @throws SQLException
	 * @throws JAXBException
	 */
	public void run(CmdLineParameters parameters) throws Exception {
		
		RDFMiner.LOCAL_ENDPOINT = new SparqlEndpoint(Global.LOCAL_SPARQL_ENDPOINT, Global.LOCAL_PREFIXES);
		RDFMiner.REMOTE_ENDPOINT = new SparqlEndpoint(Global.REMOTE_SPARQL_ENDPOINT, Global.REMOTE_PREFIXES);
		
		RDFMiner.results = new ResultsJSON();
		RDFMiner.axioms = new ArrayList<>();
		RDFMiner.stats = new StatJSON();
		
		RandomAxiomGenerator generator = null;
//		WritableWorkbook writeWorkbook = null;
//		WritableSheet sheet1 = null;
//		String FileAxioms = parameters.FileAxioms;

		if (parameters.axiomFile == null && parameters.useRandomAxiomGenerator) {
			// if a randomly generated Axiom already exists then continue
			// to generate a new Axioms based on BNF
			logger.info("Initializing the random axiom generator with grammar " + parameters.grammarFile + "...");
			generator = new RandomAxiomGenerator(parameters.grammarFile, true);
		}

		try {
			RDFMiner.output = new FileWriter(parameters.StatisticsResult);
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
		
		// ShutDownHook
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				logger.warn("Shutting down RDFMiner ...");
				// Save results in output file
				writeAndFinish();
			}
		});
		
//		RDFMiner.executor = Executors.newSingleThreadExecutor();

		/* GRAMMATICAL EVOLUTIONARY */
		/* Parameters as the inputs of GE */
		logger.info("========================================================");
		logger.info("PARAMETERS AS THE INPUTS OF GE");
		logger.info("========================================================");
		logger.info("POPULATION SIZE : " + parameters.populationsize);
		logger.info("TOTAL EFFORT : " + parameters.k_base);
//		logger.info("========================================================");
//		int maxnumGeneration = parameters.numGeneration;
		logger.info("MAX GENERATION NUMBER: " + (parameters.k_base / parameters.populationsize));
//		logger.info("GENERATION NUMBER: " + parameters.numGeneration);
//		logger.info("========================================================");
		logger.info("INITIALIZED LENGTH CHROMOSOME: " + parameters.initlenChromosome);
		logger.info("MAXIMUM WRAPPING: " + parameters.maxWrapp);
		logger.info("CROSSOVER PROBABILITY: " + parameters.proCrossover);
		logger.info("MUTATION PROBABILITY: " + parameters.proMutation);
		logger.info("TIME-CAP: " + parameters.timeOut + " secondes");
		logger.info("========================================================");

		GEChromosome[] chromosomes = new GEChromosome[parameters.populationsize];

		ArrayList<GEIndividual> candidatePopulation;
		ArrayList<GEIndividual> etilismPopulation = null;
		Statistics stat = new Statistics();

		/* STEP 1 - Initializing candidate population */
		int sizeElite = (int) (parameters.sizeElite * parameters.populationsize);
		int sizeSelection = (int) (parameters.sizeSelection * parameters.populationsize);

		int curCheckpoint;
		int curGeneration;
		int flag = 0;
		Reader buffer;
		File tempFile = new File(parameters.Bufferfile + "_size" + parameters.populationsize + ".txt");
		if (!tempFile.exists()) {
			buffer = null;
			curCheckpoint = curGeneration = 1;
		} else {
			FileInputStream reader = new FileInputStream(
					parameters.Bufferfile + "_size" + parameters.populationsize + ".txt");
			buffer = new InputStreamReader(reader, "UTF-8");
			int intch;
			String st = "";
			while ((intch = reader.read()) != '\n') {
				st += (char) intch;
			}
			curGeneration = Integer.parseInt(st);
			// logger.info("cur_Generation from buffer: " + curGeneration);
			st = "";
			while ((intch = reader.read()) != '\n') {
				st += (char) intch;
			}
			curCheckpoint = Integer.parseInt(st);
			// logger.info("cur_checkpoint from buffer: " + cur_checkpoint);
		}
		logger.info("Initializing candidate population in generation " + curGeneration + "...");
		CandidatePopulation canPop = new CandidatePopulation(parameters.populationsize, generator,
				parameters.typeInitialization, chromosomes, parameters.initlenChromosome, parameters.maxvalCodon,
				parameters.maxWrapp);
		candidatePopulation = canPop.initialize(buffer, curGeneration);

//		XSSFWorkbook workbook;
//		Row row;
//		XSSFSheet sheet;
//		int rowNum = 0;
//		File fileStatisticsResult = new File(parameters.StatisticsResult);
		
//		ResultsJSON results = new ResultsJSON();
//		StatJSON stats = null;
		boolean isDefine = false;
		
		// Write in the Statistical Result File
		if (!isDefine) {
//			RDFMiner.stats = new StatJSON();
			RDFMiner.stats.populationSize = parameters.populationsize;
			RDFMiner.stats.maxLengthChromosome = parameters.initlenChromosome;
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
			
			switch (parameters.typeselect) { 
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
			isDefine = true;
		}

		while (curCheckpoint <= parameters.checkpoint) {
			System.out.println("\n--------------------------------------------------------\n");
			logger.info("Generation: " + curGeneration);
			// logger.info("[DEBUG] parameters.checkpoint: " + parameters.checkpoint);
			FitnessEvaluation fit = new FitnessEvaluation();
			if ((curGeneration == 1) || ((buffer != null) && (flag == 0))) {
				// if1
				// logger.info("===");
				logger.info("Begin evaluating individuals...");
				// logger.info("CALLING FitnessEvaluation: ");
				logger.info("Evaluating axioms against to the RDF Data of the minimized DBPedia");
				fit.updatePopulation(candidatePopulation, curGeneration, false, null);
			}
			
			if (parameters.populationsize * curGeneration == parameters.k_base * curCheckpoint) {

				List<JSONObject> axioms = new ArrayList<>();
				
				fit.display(candidatePopulation, axioms, curGeneration);

				ArrayList<GEIndividual> candidatePopulation2 = new ArrayList<GEIndividual>();
				for (int l = 0; l < candidatePopulation.size(); l++) {
					GEIndividual indivi = new GEIndividual();
					indivi.setMapper(candidatePopulation.get(l).getMapper());
					indivi.setGenotype(candidatePopulation.get(l).getGenotype());
					indivi.setPhenotype(candidatePopulation.get(l).getPhenotype());
					indivi.setMapped(candidatePopulation.get(l).isMapped());
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
			generation.genotypeDiversityCoefficient = (double) distinctGenotypeCandidatePopulation.size() / candidatePopulation.size();
			generation.averageFitness = stat.computeAverageFitness(distinctCandidatePopulation);
			generation.numComplexAxiom = stat.getCountComplexAxiomNumber(distinctCandidatePopulation);
			generation.numComplexAxiomSpecial = stat.getCountComplexAxiomNumber2(distinctCandidatePopulation);
			RDFMiner.stats.generations.add(generation.toJSON());

			if (curGeneration * parameters.populationsize < parameters.k_base * parameters.checkpoint) {
				// if4
				// STEP 3 - SELECTION OPERATION - Reproduce Selection - Parent Selection
				ArrayList<GEIndividual> crossoverPopulation = new ArrayList<GEIndividual>();
				ArrayList<GEIndividual> selectedPopulation = new ArrayList<GEIndividual>();
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
					EATools.setPopulation(selectedPopulation,
							elite.setupSelectedPopulation(distinctCandidatePopulation));
					etilismPopulation = elite.getElitedPopulation();
					/*
					 * for (int i = 0; i < etilismPopulation.size(); i++) {
					 * logger.info(etilismPopulation.get(i).getPhenotype().toString());
					 * logger.info(etilismPopulation.get(i).getGenotype().toString());
					 * logger.info(etilismPopulation.get(i).getFitness().getDouble()); }
					 */
				} else {
					EATools.setPopulation(selectedPopulation, distinctCandidatePopulation);
					sizeElite = 0;
				}

				switch (parameters.typeselect) {
					case TypeSelection.ROULETTE_WHEEL:
						// Roulette wheel method
						// if6
						logger.info("Type selection: Roulette Wheel");
						EATools.setPopulation(crossoverPopulation, EATools.rouletteWheel(selectedPopulation));
						break;
					case TypeSelection.TRUNCATION:
						// Truncation selection method
						// if7
						logger.info("Type selection: Truncation");
						TruncationSelection truncation = new TruncationSelection(sizeSelection);
						truncation.setParentsSelectionElitism(selectedPopulation);
						crossoverPopulation = truncation.setupSelectedPopulation(selectedPopulation,
								parameters.populationsize - sizeElite);
						break;
					case TypeSelection.TOURNAMENT:
						// Tournament method
						// if8
						logger.info("Type selection: Tournament");
						EATools.setPopulation(crossoverPopulation, EATools.tournament(selectedPopulation));
						break;
					default:
						// Normal crossover way - All individual of the current generation
						// will be selected for crossover operation to create the new
						// population
						// if6
						logger.info("Type selection: Normal");
						EATools.setPopulation(crossoverPopulation, candidatePopulation);
						break;
				}

				/* STEP 4 - CROSSOVER OPERATION */
				// Crossover single point between 2 individuals of the selected population
				ArrayList<GEIndividual> crossoverList = new ArrayList<GEIndividual>();
				for (int i = 0; i < crossoverPopulation.size(); i++) {
					crossoverList.add((GEIndividual) crossoverPopulation.get(i));
				}
				// shuffle populations before crossover & mutation
				java.util.Collections.shuffle(crossoverList);
				
				logger.info("Performing Crossover & Mutation...");
				ArrayList<GEIndividual> listCrossover = new ArrayList<GEIndividual>();
				EATools.setResultList(listCrossover,
						EATools.crossover(crossoverList, parameters.proCrossover, parameters.proMutation, curGeneration,
								generator, parameters.diversity, parameters.numGeneration));
				logger.info("Crossover & Mutation done");
				// logger.info("List after Crossover & Mutation: ");
				candidatePopulation = canPop.renew(listCrossover, curGeneration, etilismPopulation);
				curGeneration++; // turn to the next generation

				// Write to buffer file
				PrintWriter writer = new PrintWriter(
						parameters.Bufferfile + "_size" + parameters.populationsize + ".txt", "UTF-8");
				writer.println(curGeneration);
				writer.println(curCheckpoint);
				flag = 1;
				for (int l = 0; l < candidatePopulation.size(); l++) {
					// System.out.println(CandidatePopulation.get(l).getGenotype().toString());
					writer.println(candidatePopulation.get(l).getGenotype().toString().substring(22,
							candidatePopulation.get(l).getGenotype().toString().length() - 1));
				}
				writer.close();
			} else {
				logger.info("Evolutionary process is done...");
				break;
			}
		}
//		try {
//			results.stats = stats.toJSON();
//			resultsFile.write(results.toJSON().toString());
//			resultsFile.close();
//		} catch (IOException e) {
//			logger.error("I/O error while closing JSON writer: " + e.getMessage());
//			e.printStackTrace();
//			System.exit(1);
//		}
//		writeAndFinish();
		System.exit(0);
	}
	
	public static void writeAndFinish() {
		try {
			RDFMiner.results.stats = RDFMiner.stats.toJSON();
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
