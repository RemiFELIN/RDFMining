package com.i3s.app.rdfminer.launcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.Executors;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.RDFMiner;
import com.i3s.app.rdfminer.Statistics;
import com.i3s.app.rdfminer.axiom.RandomAxiomGenerator;
import com.i3s.app.rdfminer.grammar.evolutionary.EATools;
import com.i3s.app.rdfminer.grammar.evolutionary.fitness.FitnessEvaluation;
import com.i3s.app.rdfminer.grammar.evolutionary.individual.CandidatePopulation;
import com.i3s.app.rdfminer.grammar.evolutionary.individual.GEIndividual;
import com.i3s.app.rdfminer.grammar.evolutionary.selection.EliteSelection;
import com.i3s.app.rdfminer.grammar.evolutionary.selection.TruncationSelection;
import com.i3s.app.rdfminer.grammar.evolutionary.selection.TypeSelection;
import com.i3s.app.rdfminer.parameters.CmdLineParameters;
import com.i3s.app.rdfminer.sparql.SparqlEndpoint;

import Individuals.GEChromosome;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

public class LaunchWithGE {

	private static Logger logger = Logger.getLogger(LaunchWithGE.class.getName());

	final public static String PREFIXES = "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
			+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"
			+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
			+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
			+ "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" + "PREFIX dc: <http://purl.org/dc/elements/1.1/>\n"
			+ "PREFIX dbr: <http://dbpedia.org/resource/>\n" + "PREFIX dbp: <http://dbpedia.org/property/>\n"
			+ "PREFIX : <http://dbpedia.org/resource/>\n" + "PREFIX dbpedia2: <http://dbpedia.org/property/>\n"
			+ "PREFIX dbpedia: <http://dbpedia.org/>\n" + "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n"
			+ "PREFIX dbo: <http://dbpedia.org/ontology/>\n" + "PREFIX ex:    <http://example.org/demo#> \n"
			+ "PREFIX rdfs:  <http://www.w3.org/2000/01/rdf-schema#> \n" + "PREFIX sp:    <http://spinrdf.org/sp#> \n";

	/**
	 * The second version of RDFMiner launcher, with Grammar Evolutionary
	 * @throws Exception 
	 * @throws SQLException 
	 * @throws JAXBException 
	 */
	public static void run(CmdLineParameters parameters, FileWriter output) throws JAXBException, SQLException, Exception {

		RDFMiner.endpoint = new SparqlEndpoint(Global.LOCAL_SPARQL_ENDPOINT, PREFIXES);

		RandomAxiomGenerator generator = null;

		WritableWorkbook writeWorkbook = null;
		WritableSheet sheet1 = null;
		String FileAxioms = parameters.FileAxioms;

		if (parameters.axiomFile == null) {
			if (parameters.useRandomAxiomGenerator) {
				// if a randomly generated Axiom already exists then continue
				// to generate a new Axioms based on BNF
				logger.info("Initializing the random axiom generator with grammar " + parameters.grammarFile + "...");
				generator = new RandomAxiomGenerator(parameters.grammarFile);
			}
		} 
		/* else {
			// if there exists Axiom in the file
			logger.info("Reading axioms from file " + parameters.axiomFile + "...");
			try {
				// Try to read the status file
				BufferedReader axiomFile = null;
				axiomFile = new BufferedReader(new FileReader(parameters.axiomFile));
			} catch (IOException e) {
				logger.error("Could not open file " + parameters.axiomFile);
				return;
			}
		} */

		RDFMiner.executor = Executors.newSingleThreadExecutor();

		/* GRAMMATICAL EVOLUTIONARY */
		/* Parameters as the inputs of GE */
		logger.info("========================================================");
		logger.info("PARAMETERS AS THE INPUTS OF GE");
		logger.info("========================================================");
		logger.info("POPULATION SIZE : " + parameters.populationsize);
		logger.info("========================================================");
		int maxnumGeneration = parameters.numGeneration;
		logger.info("MAX GENERATION NUMBER: " + maxnumGeneration);
		logger.info("GENERATION NUMBER: " + parameters.numGeneration);
		logger.info("========================================================");
		logger.info("INITIALIZED LENGTH CHROMOSOME: " + parameters.initlenChromosome);
		logger.info("MAXIMUM WRAPPING: " + parameters.maxWrapp);
		logger.info("CROSSOVER PROBABILITY: " + parameters.proCrossover);
		logger.info("MUTATION PROBABILITY: " + parameters.proMutation);

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
		boolean exists = tempFile.exists();
		if (!exists) {
			buffer = null;
			curCheckpoint = 1;
			curGeneration = 1;
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
		logger.info("==================================================================");
		logger.info("INITIALIZING CANDIDATE POPULATION IN GENERATION: " + curGeneration);
		CandidatePopulation canPop = new CandidatePopulation(parameters.populationsize, generator,
				parameters.typeInitialization, chromosomes, parameters.initlenChromosome, parameters.maxvalCodon,
				parameters.maxWrapp);
		candidatePopulation = canPop.initialize(buffer, curGeneration);

		XSSFWorkbook workbook;
		Row row;
		XSSFSheet sheet;
		int rowNum = 0;
		File fileStatisticsResult = new File(parameters.StatisticsResult);

		while (curCheckpoint <= parameters.checkpoint) {
			FitnessEvaluation fit = new FitnessEvaluation();
			if ((curGeneration == 1) || ((buffer != null) && (flag == 0))) {
				// if1
				logger.info("==================================================================");
				logger.info("BEGIN EVALUATING INDIVIDUALS...");
				logger.info("CALLING FitnessEvaluation -------------------->");
				fit.update(candidatePopulation, curGeneration, parameters.numGeneration, null);
			}
			if (parameters.populationsize * curGeneration == parameters.k_base * curCheckpoint) {
				// if2
				try {
					File file = new File(FileAxioms + "_k" + parameters.k_base * curCheckpoint + "_size"
							+ parameters.populationsize + ".xlsx");
					file.createNewFile();
					writeWorkbook = Workbook.createWorkbook(file);

					sheet1 = writeWorkbook.createSheet("k= " + parameters.k_base * curCheckpoint, 0);
					sheet1.addCell(new Label(0, 0, "Axiom"));
					sheet1.addCell(new Label(1, 0, "Possibilitty"));
					sheet1.addCell(new Label(2, 0, "U_phi"));
					sheet1.addCell(new Label(3, 0, "Generality"));
					sheet1.addCell(new Label(4, 0, "Complexity_Penalty"));
					sheet1.addCell(new Label(5, 0, "Fitness"));
					sheet1.addCell(new Label(6, 0, "Mapped"));
					sheet1.addCell(new Label(7, 0, "Possibilitty_DBpedia"));
					sheet1.addCell(new Label(8, 0, "U_phi_DBpedia"));
					sheet1.addCell(new Label(9, 0, "Generality_DBpedia"));
				}
				catch (IOException e) {
					e.printStackTrace();

				}
				fit.display(candidatePopulation, curGeneration, sheet1);

				ArrayList<GEIndividual> candidatePopulation2 = new ArrayList<GEIndividual>();
				for (int l = 0; l < candidatePopulation.size(); l++) {
					GEIndividual indivi = new GEIndividual();
					indivi.setMapper(candidatePopulation.get(l).getMapper());
					indivi.setGenotype(candidatePopulation.get(l).getGenotype());
					indivi.setPhenotype(candidatePopulation.get(l).getPhenotype());
					indivi.setMapped(candidatePopulation.get(l).isMapped());
					candidatePopulation2.add(indivi);
				}

				fit.update(candidatePopulation2, curGeneration, parameters.numGeneration, sheet1);
				writeWorkbook.write();
				writeWorkbook.close();
				curCheckpoint++;
			}
			else {
				fit.display(candidatePopulation, curGeneration, null);

			}
			// Write in the Statistical Result File
			boolean checkexists = fileStatisticsResult.exists();

			if (!checkexists) {
				workbook = new XSSFWorkbook();
				sheet = workbook.createSheet("Statistics Result");
				Row firstRow = sheet.createRow(rowNum++);
				Cell firstCell = firstRow.createCell(0);
				firstCell.setCellValue("PARAMETER SETTINGS IN GE");

				row = sheet.createRow(rowNum++);
				row.createCell(0).setCellValue("POPULATION SIZE");
				row.createCell(1).setCellValue(parameters.populationsize);

				row = sheet.createRow(rowNum++);
				row.createCell(0).setCellValue("MAXIMUM LENGTH CHROMOSOME");
				row.createCell(1).setCellValue(parameters.initlenChromosome);

				row = sheet.createRow(rowNum++);
				row.createCell(0).setCellValue("MAXIMUM WRAPPING");
				row.createCell(1).setCellValue(parameters.maxWrapp);

				row = sheet.createRow(rowNum++);
				row.createCell(0).setCellValue("CROSSOVER PROBABILITY");
				row.createCell(1).setCellValue(parameters.proCrossover);

				row = sheet.createRow(rowNum++);
				row.createCell(0).setCellValue("MUTATION PROBABILITY");
				row.createCell(1).setCellValue(parameters.proMutation);
				String typeSelection = "";

				row = sheet.createRow(rowNum++);
				row.createCell(0).setCellValue("ELITISM SELECTION");
				if (parameters.elitism == 1) {
					row.createCell(1).setCellValue("YES");
					row = sheet.createRow(rowNum++);
					row.createCell(0).setCellValue("ELITE SIZE");
					row.createCell(1).setCellValue(parameters.sizeElite);
				} else
					row.createCell(1).setCellValue("NO");

				switch (parameters.typeselect) {
				case 1:
					typeSelection = "Roulette Wheel selection method";
					break;
				case 2:
					typeSelection = "Truncation selection method";
					break;
				case 3:
					typeSelection = "Tournament selection method";
					break;
				case 4:
					typeSelection = "Normal selection method";
					break;
				}
				row = sheet.createRow(rowNum++);
				row.createCell(0).setCellValue("SELECTION METHOD");
				row.createCell(1).setCellValue(typeSelection);

				if (parameters.typeselect == 2) {
					row = sheet.createRow(rowNum++);
					row.createCell(0).setCellValue("SELECTION SIZE");
					row.createCell(1).setCellValue(parameters.sizeSelection);
				}
				row = sheet.createRow(rowNum++);
				row = sheet.createRow(rowNum++);
				row.createCell(0).setCellValue("STATISTICAL RESULTS");
				row = sheet.createRow(rowNum++);
				row.createCell(0).setCellValue("Generation");
				row.createCell(1).setCellValue("Successful mapping");
				row.createCell(2).setCellValue("Diversity coefficient");
				row.createCell(3).setCellValue("Genotype Diversity coefficient");
				row.createCell(4).setCellValue("Average fitness");
				row.createCell(5).setCellValue("Complex axioms");
				row.createCell(6).setCellValue("Complex axioms containing ObjectSomeValuesOf or ObjectAllValuesOf");
			}

			else {

				FileInputStream Inputstream = new FileInputStream(fileStatisticsResult);
				workbook = new XSSFWorkbook(Inputstream);
				sheet = workbook.getSheetAt(0);
				rowNum = sheet.getLastRowNum() + 1;
			}
			row = sheet.createRow(rowNum++);
			row.createCell(0).setCellValue(curGeneration);
			ArrayList<GEIndividual> distinctCandidatePopulation = EATools.getDistinctPopulation(candidatePopulation);
			double success = stat.getCountSuccessMapping(distinctCandidatePopulation);
			row.createCell(1).setCellValue(success);
			double pheDiversity = (double) distinctCandidatePopulation.size() / candidatePopulation.size();
			row.createCell(2).setCellValue(pheDiversity);
			ArrayList<GEIndividual> distinctGenotypeCandidatePopulation = EATools.getDistinctGenotypePopulation(
					candidatePopulation);
			double genDiversity = (double) distinctGenotypeCandidatePopulation.size() / candidatePopulation.size();
			row.createCell(3).setCellValue(genDiversity);

			double avgFitness = stat.computeAverageFitness(distinctCandidatePopulation);
			row.createCell(4).setCellValue(avgFitness);
			double compAxiom = stat.getCountComplexAxiomNumber(distinctCandidatePopulation);
			row.createCell(5).setCellValue(compAxiom);
			double compAxiom2 = stat.getCountComplexAxiomNumber2(distinctCandidatePopulation);
			row.createCell(6).setCellValue(compAxiom2);
			// Write to the Statistics Result file
			try {
				FileOutputStream outputStream = new FileOutputStream(parameters.StatisticsResult);
				workbook.write(outputStream);
				workbook.close();
				outputStream.flush();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (curGeneration * parameters.populationsize < parameters.k_base * parameters.checkpoint) {
				// if4
				// STEP 3 - SELECTION OPERATION - Reproduce Selection - Parent Selection
				ArrayList<GEIndividual> crossoverPopulation = new ArrayList<GEIndividual>();
				ArrayList<GEIndividual> selectedPopulation = new ArrayList<GEIndividual>();
				if (parameters.elitism == 1) {
					// Elitism method, which copies the best chromosome( or a few best
					// chromosome) to new population. The rest done classical way. it
					// prevents losing the best found solution
					// if5
					logger.info("========================================================================");
					logger.info("SELECTING ELITE INDIVIDUALS>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
					logger.info("==================================================================");
					logger.info(" SELECTING + " + (int) (parameters.sizeElite * 100)
							+ " % ELITE INDIVIDUALS FOR THE NEW POPULATION ");
					logger.info("The size of elite population: " + sizeElite);
					logger.info("..................................................");
					EliteSelection elite = new EliteSelection(sizeElite);
					elite.setParentsSelectionElitism(distinctCandidatePopulation);
					EATools.setPopulation(selectedPopulation, elite.setupSelectedPopulation(distinctCandidatePopulation));
					etilismPopulation = elite.getElitedPopulation();
					for (int i = 0; i < etilismPopulation.size(); i++) {
						logger.info(etilismPopulation.get(i).getPhenotype().toString());
						logger.info(etilismPopulation.get(i).getGenotype().toString());
						logger.info(etilismPopulation.get(i).getFitness().getDouble());
					}
				} else {
					EATools.setPopulation(selectedPopulation, candidatePopulation);
					sizeElite = 0;
				}
				
				switch (parameters.typeselect) {
					case TypeSelection.ROULETTE_WHEEL:
						// Roulette wheel method 
						// if6
						EATools.setPopulation(crossoverPopulation, EATools.rouletteWheel(selectedPopulation));
						break;
					case TypeSelection.TRUNCATION:
						// Truncation selection method
						// if7
						TruncationSelection truncation = new TruncationSelection(sizeSelection);
						truncation.setParentsSelectionElitism(selectedPopulation);
						crossoverPopulation = truncation.setupSelectedPopulation(selectedPopulation,
								parameters.populationsize - sizeElite);
						logger.info("==================================================================");
						logger.info("Truncation population " + crossoverPopulation.size());
						for (int t1 = 0; t1 < crossoverPopulation.size(); t1++) {
							logger.info("axiom:" + crossoverPopulation.get(t1).getPhenotype().toString());
							logger.info("phenotype:" + crossoverPopulation.get(t1).getGenotype().toString());
							logger.info("fitness:" + crossoverPopulation.get(t1).getFitness().getDouble());
						}
						break;
					case TypeSelection.TOURNAMENT:
						// Tournament method
						// if8
						EATools.setPopulation(crossoverPopulation, EATools.tournament(selectedPopulation));
					default:
						// Normal crossover way - All individual of the current generation
						// will be selected for crossover operation to create the new
						// population
						// if6
						EATools.setPopulation(crossoverPopulation, candidatePopulation);
						break;
				}
				
				/* STEP 4 - CROSSOVER OPERATION */
				// Crossover single point between 2 individuals of the selected population
				ArrayList<GEIndividual> crossoverList = new ArrayList<GEIndividual>();
				for (int i = 0; i < crossoverPopulation.size(); i++) {
					crossoverList.add((GEIndividual) crossoverPopulation.get(i));
				}
				java.util.Collections.shuffle(crossoverList);
				logger.info("========================================================================");
				logger.info("PERFORMING CROSSOVER & MUTATION>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
				ArrayList<GEIndividual> listCrossover = new ArrayList<GEIndividual>();
				EATools.setResultList(listCrossover, EATools.crossover(crossoverList, parameters.proCrossover, parameters.proMutation,
						curGeneration, generator, parameters.diversity, parameters.numGeneration));
				logger.info("=========================================================");
				logger.info("List after Crossover & Mutation:");
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
				logger.info("EVOLUTIONARY PROCESS IS DONE!");
				break;
			}
		}
		System.exit(0);
	}

}
