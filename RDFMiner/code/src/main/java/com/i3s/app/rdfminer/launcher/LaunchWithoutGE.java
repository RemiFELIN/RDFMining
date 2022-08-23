package com.i3s.app.rdfminer.launcher;

import Individuals.Phenotype;
import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.RDFMiner;
import com.i3s.app.rdfminer.axiom.Axiom;
import com.i3s.app.rdfminer.axiom.AxiomFactory;
import com.i3s.app.rdfminer.generator.axiom.AxiomGenerator;
import com.i3s.app.rdfminer.generator.axiom.CandidateAxiomGenerator;
import com.i3s.app.rdfminer.generator.axiom.IncreasingTimePredictorAxiomGenerator;
import com.i3s.app.rdfminer.generator.axiom.RandomAxiomGenerator;
import com.i3s.app.rdfminer.parameters.CmdLineParameters;
import com.i3s.app.rdfminer.shacl.Shape;
import com.i3s.app.rdfminer.shacl.ShapesManager;
import com.i3s.app.rdfminer.shacl.ValidationReport;
import com.i3s.app.rdfminer.sparql.corese.CoreseEndpoint;
import com.i3s.app.rdfminer.sparql.corese.CoreseService;
import org.apache.jena.shared.JenaException;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.apache.log4j.Logger;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

public class LaunchWithoutGE {

	private static final Logger logger = Logger.getLogger(LaunchWithoutGE.class.getName());
	
	/**
	 * The first version of RDFMiner launcher
	 */
	public void runAxiomEvaluation(CmdLineParameters parameters) throws InterruptedException, ExecutionException, URISyntaxException, IOException {
		
		AxiomGenerator generator = null;
		BufferedReader axiomFile = null;

		// Create an empty JSON object which will be fill with our results
		RDFMiner.axiomsList = new JSONArray();
		
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
			RDFMiner.axiomsList.put(axiom.toJSON());
		}

		logger.info("Done testing axioms. Exiting.");
		System.exit(0);
	}

	/**
	 * The first version of RDFMiner launcher
	 */
	public void runShapeEvaluation(CmdLineParameters parameters) throws URISyntaxException, IOException {

		BufferedReader shapeFile = null;

		// Create an empty JSON object which will be fill with our results
		RDFMiner.axiomsList = new JSONArray();

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
		Runtime.getRuntime().addShutdownHook(new Thread(this::writeAndFinish));

		// create output file
		try {
			RDFMiner.output = new FileWriter(RDFMiner.outputFolder + Global.RESULTS_FILENAME);
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}

		ShapesManager shapesManager = new ShapesManager(parameters.shapeFile);
		// launch evaluation
		CoreseEndpoint endpoint = new CoreseEndpoint(Global.CORESE_SPARQL_ENDPOINT, Global.SPARQL_ENDPOINT, Global.PREFIXES);
		String report;
		if(RDFMiner.parameters.useClassicShaclMode) {
			report = endpoint.getValidationReportFromServer(shapesManager.file, CoreseService.SHACL_EVALUATION);
			ValidationReport validationReport = new ValidationReport(report);
			RDFMiner.output.write(validationReport.prettifyPrint());
		} else {
			report = endpoint.getValidationReportFromServer(shapesManager.file, CoreseService.PROBABILISTIC_SHACL_EVALUATION);
			ValidationReport validationReport = new ValidationReport(report);
			for(Shape shape : shapesManager.getPopulation()) {
				shape.fillParamFromReport(validationReport);
				// Save a JSON report of the test
				RDFMiner.axiomsList.put(shape.toJSON());
			}
		}

		logger.info("Done testing shape. Exiting.");
		System.exit(0);
	}
	
	public void writeAndFinish() {
		try {
			logger.warn("Shutting down RDFMiner ...");
			if(!RDFMiner.parameters.useClassicShaclMode)
				RDFMiner.output.write(RDFMiner.axiomsList.toString(2));
			RDFMiner.output.close();
		} catch (IOException e) {
			logger.error("I/O error while closing JSON writer: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}

}
