package com.i3s.app.rdfminer.launcher;

import Individuals.Phenotype;
import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.RDFMiner;
import com.i3s.app.rdfminer.entity.axiom.Axiom;
import com.i3s.app.rdfminer.entity.axiom.AxiomFactory;
import com.i3s.app.rdfminer.generator.axiom.AxiomGenerator;
import com.i3s.app.rdfminer.generator.axiom.CandidateAxiomGenerator;
import com.i3s.app.rdfminer.generator.axiom.IncreasingTimePredictorAxiomGenerator;
import com.i3s.app.rdfminer.generator.axiom.RandomAxiomGenerator;
import com.i3s.app.rdfminer.parameters.CmdLineParameters;
import com.i3s.app.rdfminer.entity.shacl.Shape;
import com.i3s.app.rdfminer.entity.shacl.ShapesManager;
import com.i3s.app.rdfminer.entity.shacl.ValidationReport;
import com.i3s.app.rdfminer.sparql.corese.CoreseEndpoint;
import com.i3s.app.rdfminer.sparql.corese.CoreseService;
import com.i3s.app.rdfminer.statistics.Statistics;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.stat.inference.ChiSquareTest;
import org.apache.jena.shared.JenaException;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

public class Evaluator {

	private static final Logger logger = Logger.getLogger(Evaluator.class.getName());
	
	/**
	 * The first version of RDFMiner launcher
	 */
	public static void runAxiomEvaluation(CmdLineParameters parameters) throws InterruptedException, ExecutionException, URISyntaxException, IOException {
		
		AxiomGenerator generator = null;
		BufferedReader axiomFile = null;

		// Create an empty JSON array which will be fill with our results
		RDFMiner.evaluatedEntities = new JSONArray();
		
		if (parameters.axiomFile == null) {
			if (parameters.singleAxiom == null) {
				if (parameters.useRandomAxiomGenerator) {
					logger.info(
							"Initializing the random axiom generator with grammar " + parameters.grammarFile + "...");
					generator = new RandomAxiomGenerator(parameters.grammarFile, false);
				} else if (parameters.subClassList != null) {
					logger.info("Initializing the increasing TP axiom generator...");
					generator = new IncreasingTimePredictorAxiomGenerator(parameters.subClassList);
				} else {
					logger.info("Initializing the candidate axiom generator...");
					generator = new CandidateAxiomGenerator(parameters.grammarFile, false);
				}
			} else {
				logger.info("launch test on a single axiom");
			}
		} else {
			logger.info("Reading axioms from file " + parameters.axiomFile + "...");
			try {
				// Try to read the status file:
				axiomFile = new BufferedReader(new FileReader(parameters.axiomFile));
			} catch (IOException e) {
				logger.error("Could not open file " + parameters.axiomFile);
				return;
			}
		}
		
		// ShutDownHook
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			// Save results in output file
			if (parameters.singleAxiom == null)
				writeAndFinish();
		}));

		if (parameters.singleAxiom == null) {
			// as the test of a single axiom is return on standard output, we don't need to
			// write file of the results
			try {
				RDFMiner.output = new FileWriter(RDFMiner.outputFolder + Global.RESULTS_FILENAME);
			} catch (IOException e) {
				logger.error(e.getMessage());
				e.printStackTrace();
				System.exit(1);
			}
		}

		// Prepare multi-threading
		// We have a set of threads to compute each axioms
		ExecutorService executor = Executors.newFixedThreadPool(Global.NB_THREADS);
		Set<Callable<Axiom>> callables = new HashSet<>();
		List<Axiom> axiomList = new ArrayList<>();

		// assessment of axioms
		while (true) {

			// Axiom a = null;
			String axiomName = null;

			if (generator != null) {
				Phenotype axiom = generator.nextAxiom();
				if (axiom == null)
					break;
				axiomName = axiom.getStringNoSpace();
				// create a callable and add it on list of callables
				String finalAxiomName = axiomName;
				callables.add(() -> {
					try {
						logger.info("Testing axiom: " + finalAxiomName);
						Axiom a = AxiomFactory.create(null, axiom, new CoreseEndpoint(Global.CORESE_SPARQL_ENDPOINT, Global.SPARQL_ENDPOINT, Global.PREFIXES));
						a.axiomId = finalAxiomName;
						return a;
					} catch (QueryExceptionHTTP httpError) {
						logger.error("HTTP Error " + httpError.getMessage() + " making a SPARQL query.");
						httpError.printStackTrace();
					} catch (JenaException jenaException) {
						logger.error("Jena Exception " + jenaException.getMessage() + " making a SPARQL query.");
						jenaException.printStackTrace();
					}
					return null;
				});

			} else {
				try {
					if (axiomFile == null && parameters.singleAxiom != null) {
						axiomName = parameters.singleAxiom;
					} else if (axiomFile != null && parameters.singleAxiom == null) {
						axiomName = axiomFile.readLine();
					} else {
						logger.error("The options -a and -sa are used at the same time ...");
						System.exit(1);
					}
					if (axiomName == null || axiomName.isEmpty()) {
						logger.info("No more axioms to evaluate ...");
						break;
					}
					String finalAxiomName = axiomName;
					callables.add(() -> {
						logger.info("Testing axiom: " + finalAxiomName);
						Axiom a = AxiomFactory.create(null, finalAxiomName, new CoreseEndpoint(Global.CORESE_SPARQL_ENDPOINT, Global.SPARQL_ENDPOINT, Global.PREFIXES));
						a.axiomId = finalAxiomName;
						if (parameters.singleAxiom != null) {
							logger.info("Axiom evaluated !");
							logger.info("Result (using JSON format) :\n" + a.toJSON().toString(2));
						}
						return a;
					});
					if (parameters.singleAxiom != null)
						break;
				} catch (IOException e) {
					logger.error("Could not read the next axiom.");
					e.printStackTrace();
					writeAndFinish();
					System.exit(1);
				}
			}

		}

		// get all callables and execute it
		logger.info(callables.size() + " tasks are ready ...");
		// Submit tasks
		List<Future<Axiom>> futureAxioms = executor.invokeAll(callables);
		// We recover our axioms
		for (Future<Axiom> axiom : futureAxioms) {
			axiomList.add(axiom.get());
		}
		// Shut down the executor
		executor.shutdown();
		executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

		for(Axiom axiom : axiomList) {
			// Save a JSON report of the test
			RDFMiner.evaluatedEntities.put(axiom.toJSON());
		}

		logger.info("Done testing axioms. Exiting.");
		System.exit(0);
	}

	/**
	 * The first version of RDFMiner launcher
	 */
	public static void runShapeEvaluation(CmdLineParameters parameters) throws URISyntaxException, IOException {

		BufferedReader shapeFile = null;

		// Create an empty JSON object which will be fill with our results
		RDFMiner.evaluatedEntities = new JSONArray();

		if (parameters.shapeFile != null) {
			logger.info("Reading SHACL Shapes from file " + parameters.shapeFile + "...");
			try {
				// Try to read the status file:
				shapeFile = new BufferedReader(new FileReader(parameters.shapeFile));
			} catch (IOException e) {
				logger.error("Could not open file " + parameters.shapeFile);
				return;
			}
		} else {
			logger.error("No SHACL file specified !");
			System.exit(1);
		}

		// ShutDownHook
		// Save results in output file
		//			logger.warn("Shutting down RDFMiner ...");
		// Save results in output file
		Runtime.getRuntime().addShutdownHook(new Thread(Evaluator::writeAndFinish));

		// create output file
		try {
			RDFMiner.output = new FileWriter(RDFMiner.outputFolder + Global.SHACL_VALIDATION_REPORT_FILENAME);
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}

		ShapesManager shapesManager = new ShapesManager(parameters.shapeFile);
		// launch evaluation
		CoreseEndpoint endpoint = new CoreseEndpoint(Global.CORESE_SPARQL_ENDPOINT, Global.SPARQL_ENDPOINT, Global.PREFIXES);
		// Launch SHACL evaluation from the Corese server and get the result in turtle
		String report = endpoint.getValidationReportFromServer(shapesManager.file, CoreseService.PROBABILISTIC_SHACL_EVALUATION);
//		ValidationReport validationReport = new ValidationReport(report);
		String pretiffyReport = report.replace(".@", ".\n@")
				.replace(".<", ".\n\n<")
				.replace(";sh", ";\nsh")
				.replace(";psh", ";\npsh")
				.replace(";r", ";\nr")
				.replace("._", ".\n\n_");
		logger.info("Writting validation report in " + RDFMiner.outputFolder + Global.SHACL_VALIDATION_REPORT_FILENAME + " ...");
		RDFMiner.output.write(pretiffyReport);
		RDFMiner.output.close();
		// Hypothesis test
		ValidationReport validationReport = new ValidationReport(report);
		// save the result of statistic test into RDF triples
		// in order to put it in Corese graph and get its value into STTL Transformation and HTML result
		// In the same way, we note the acceptance (or not) of a given shape using proportion or hypothesis testing
		logger.info("Writting hypothesis test results in " + Global.SHACL_HYPOTHESIS_TEST_FILENAME);
		FileWriter hypothesisTestFw = new FileWriter(RDFMiner.outputFolder + Global.SHACL_HYPOTHESIS_TEST_FILENAME);
		hypothesisTestFw.write(Global.PREFIXES + "\n");
		logger.info(validationReport.reportedShapes.size() + " shapes has been evaluated !");
//		logger.info("[DEBUG] validationReport.reportedShapes.get(0) = " + validationReport.reportedShapes.get(0));
		for(Shape shape : shapesManager.getPopulation()) {
//			logger.info("[DEBUG] shape.id = " + shape.id);
			if(validationReport.reportedShapes.contains(shape.id.replace("<", "").replace(">", ""))) {
				// get shapes with metrics
				shape.fillParamFromReport(validationReport);
				// X^2 computation
				double nExcTheo = shape.referenceCardinality.doubleValue() * Double.parseDouble(RDFMiner.parameters.probShaclP);
				double nConfTheo = shape.referenceCardinality.doubleValue() - nExcTheo;
				// if observed error is lower, accept the shape
				if(shape.numException.doubleValue() <= nExcTheo) {
					hypothesisTestFw.write(shape.id + " ex:acceptance \"true\"^^xsd:boolean .\n");
				}  else if (nExcTheo >= 5 && nConfTheo >= 5) {
					// apply statistic test X2
					Double X2 = (Math.pow(shape.numException.doubleValue() - nExcTheo, 2) / nExcTheo) +
							(Math.pow(shape.numConfirmation.doubleValue() - nConfTheo, 2) / nConfTheo);
//				logger.info("p-value = " + X2);
					hypothesisTestFw.write(shape.id + " ex:pvalue \"" + X2 + "\"^^xsd:double .\n");
					double critical = new ChiSquaredDistribution(1).inverseCumulativeProbability(1 - RDFMiner.parameters.alpha);
					if (X2 <= critical) {
						// Accepted !
						hypothesisTestFw.write(shape.id + " ex:acceptance \"true\"^^xsd:boolean .\n");
					} else {
						// rejected !
						hypothesisTestFw.write(shape.id + " ex:acceptance \"false\"^^xsd:boolean .\n");
					}
				} else {
					// rejected !
					hypothesisTestFw.write(shape.id + " ex:acceptance \"false\"^^xsd:boolean .\n");
				}
			} else {
				logger.warn("Shape to remove: " + shape.id);
			}
		}
		hypothesisTestFw.close();
		// send hypothesis result to Corese graph
		endpoint.sendFileToServer(new File(RDFMiner.outputFolder + Global.SHACL_HYPOTHESIS_TEST_FILENAME), Global.SHACL_HYPOTHESIS_TEST_FILENAME);
		endpoint.sendRDFDataToDB(endpoint.getFilePathFromServer(Global.SHACL_HYPOTHESIS_TEST_FILENAME));
		// Send the SHACL Validation Report and shapes graph into Corese graph in order to
		// perform a HTML report with STTL transformation
		endpoint.sendFileToServer(new File(RDFMiner.outputFolder + Global.SHACL_VALIDATION_REPORT_FILENAME), Global.SHACL_VALIDATION_REPORT_FILENAME);
		endpoint.sendRDFDataToDB(endpoint.getFilePathFromServer(Global.SHACL_VALIDATION_REPORT_FILENAME));
		// load shapes graph in corese DB
		endpoint.sendRDFDataToDB(endpoint.getFilePathFromServer(Global.SHACL_SHAPES_FILENAME));
		// STTL Transformation
		// load template
		logger.info("Perform STTL Transformation ...");
		String sttl = Files.readString(Path.of(Global.PROBABILISTIC_STTL_TEMPLATE), StandardCharsets.UTF_8);
		// perform sttl query
		String sttl_result = endpoint.getHTMLResultFromSTTLTransformation(sttl);
		// write results in output file
		logger.info("Writting results in " + Global.PROBABILISTIC_STTL_RESULT_AS_HTML);
		FileWriter fw = new FileWriter(RDFMiner.outputFolder + Global.PROBABILISTIC_STTL_RESULT_AS_HTML);
		fw.write(sttl_result);
		fw.close();
		logger.info("Done testing shape. Exiting.");
		System.exit(0);
	}
	
	public static void writeAndFinish() {
		logger.warn("Shutting down RDFMiner ...");
	}

	public static void main(String[] args) {
		System.out.println( new ChiSquaredDistribution(1).inverseCumulativeProbability(1 - RDFMiner.parameters.alpha) );
	}
}




















