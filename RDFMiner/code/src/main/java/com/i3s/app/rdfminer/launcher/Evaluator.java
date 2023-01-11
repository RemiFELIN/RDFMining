package com.i3s.app.rdfminer.launcher;

import Individuals.Phenotype;
import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.RDFMiner;
import com.i3s.app.rdfminer.entity.axiom.Axiom;
import com.i3s.app.rdfminer.entity.axiom.AxiomFactory;
import com.i3s.app.rdfminer.entity.shacl.ShapesManager;
import com.i3s.app.rdfminer.entity.shacl.Shape;
import com.i3s.app.rdfminer.entity.shacl.ValidationReport;
import com.i3s.app.rdfminer.generator.axiom.AxiomGenerator;
import com.i3s.app.rdfminer.generator.axiom.CandidateAxiomGenerator;
import com.i3s.app.rdfminer.generator.axiom.IncreasingTimePredictorAxiomGenerator;
import com.i3s.app.rdfminer.generator.axiom.RandomAxiomGenerator;
import com.i3s.app.rdfminer.launcher.evaluator.ExtendedShacl;
import com.i3s.app.rdfminer.sparql.corese.CoreseEndpoint;
import com.i3s.app.rdfminer.sparql.corese.CoreseService;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.jena.shared.JenaException;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.apache.jena.vocabulary.RDF;
import org.apache.log4j.Logger;
import org.json.JSONArray;

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

	public Evaluator()
			throws URISyntaxException, IOException, ExecutionException, InterruptedException {
		// special case where -af and -sf are used in the same time
		if(RDFMiner.parameters.axiomFile != null && RDFMiner.parameters.shapeFile != null) {
			logger.error("(--axioms-file) and (--shapes-file) are used in the same time !");
			System.exit(1);
		} else if(RDFMiner.parameters.axiomFile != null) {
			// launch axioms evaluator
			runAxiomEvaluation();
		} else if(RDFMiner.parameters.shapeFile != null) {
			// launch shapes evaluator
			runShapeEvaluation();
		} else {
			logger.error("No files provided !");
			logger.warn("use (--axioms-file) to assess an OWL axioms file OR (--shapes-file) to assess a SHACL shapes file");
			System.exit(1);
		}
	}
	
	/**
	 * The first version of RDFMiner launcher
	 */
	public void runAxiomEvaluation() throws InterruptedException, ExecutionException, URISyntaxException, IOException {
		
		AxiomGenerator generator = null;
		BufferedReader axiomFile = null;

		// Create an empty JSON array which will be fill with our results
		RDFMiner.evaluatedEntities = new JSONArray();
		
		if (RDFMiner.parameters.axiomFile == null) {
			if (RDFMiner.parameters.singleAxiom == null) {
				if (RDFMiner.parameters.useRandomAxiomGenerator) {
					logger.info(
							"Initializing the random axiom generator with grammar " + RDFMiner.parameters.grammarFile + "...");
					generator = new RandomAxiomGenerator(RDFMiner.parameters.grammarFile, false);
				} else if (RDFMiner.parameters.subClassList != null) {
					logger.info("Initializing the increasing TP axiom generator...");
					generator = new IncreasingTimePredictorAxiomGenerator(RDFMiner.parameters.subClassList);
				} else {
					logger.info("Initializing the candidate axiom generator...");
					generator = new CandidateAxiomGenerator(RDFMiner.parameters.grammarFile, false);
				}
			} else {
				logger.info("launch test on a single axiom");
			}
		} else {
			logger.info("Reading axioms from file " + RDFMiner.parameters.axiomFile + "...");
			try {
				// Try to read the status file:
				axiomFile = new BufferedReader(new FileReader(RDFMiner.parameters.axiomFile));
			} catch (IOException e) {
				logger.error("Could not open file " + RDFMiner.parameters.axiomFile);
				return;
			}
		}
		
		// ShutDownHook
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			// Save results in output file
			if (RDFMiner.parameters.singleAxiom == null)
				writeAndFinish();
		}));

		if (RDFMiner.parameters.singleAxiom == null) {
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
						Axiom a = AxiomFactory.create(null, axiom, new CoreseEndpoint(Global.CORESE_SPARQL_ENDPOINT, Global.TARGET_SPARQL_ENDPOINT, Global.PREFIXES));
						a.setEntityAsString(finalAxiomName);
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
					if (axiomFile == null && RDFMiner.parameters.singleAxiom != null) {
						axiomName = RDFMiner.parameters.singleAxiom;
					} else if (axiomFile != null && RDFMiner.parameters.singleAxiom == null) {
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
						Axiom a = AxiomFactory.create(null, finalAxiomName, new CoreseEndpoint(Global.CORESE_SPARQL_ENDPOINT, Global.TARGET_SPARQL_ENDPOINT, Global.PREFIXES));
						a.setEntityAsString(finalAxiomName);
						if (RDFMiner.parameters.singleAxiom != null) {
							logger.info("Axiom evaluated !");
							logger.info("Result (using JSON format) :\n" + a.toJSON().toString(2));
						}
						return a;
					});
					if (RDFMiner.parameters.singleAxiom != null)
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
	public void runShapeEvaluation() throws URISyntaxException, IOException {

		BufferedReader shapeFile = null;

		// Create an empty JSON object which will be fill with our results
		RDFMiner.evaluatedEntities = new JSONArray();

		if (RDFMiner.parameters.shapeFile != null) {
			logger.info("Reading SHACL Shapes from file " + RDFMiner.parameters.shapeFile + "...");
			try {
				// Try to read the status file:
				shapeFile = new BufferedReader(new FileReader(RDFMiner.parameters.shapeFile));
			} catch (IOException e) {
				logger.error("Could not open file " + RDFMiner.parameters.shapeFile);
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

		ShapesManager shapesManager = new ShapesManager(Path.of(RDFMiner.parameters.shapeFile));
		CoreseEndpoint endpoint = new CoreseEndpoint(Global.CORESE_SPARQL_ENDPOINT, Global.TARGET_SPARQL_ENDPOINT, Global.PREFIXES);
		String report;

		if (RDFMiner.parameters.useProbabilisticShaclMode) {
			// run extended SHACL eval
			ExtendedShacl.run(shapesManager);
		} else {
			if(!RDFMiner.parameters.useClassicShaclMode) {
				logger.warn("No validation mode specified !");
				logger.warn("By default, the standard SHACL validation will be used");
			}
			report = endpoint.getValidationReportFromServer(shapesManager.getFile(), CoreseService.SHACL_EVALUATION);
			ValidationReport validationReport = new ValidationReport(report);
			RDFMiner.output.write(validationReport.prettifyPrint());
		}

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




















