package com.i3s.app.rdfminer.launcher.evaluator;

import com.github.jsonldjava.shaded.com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.i3s.app.rdfminer.Endpoint;
import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.Parameters;
import com.i3s.app.rdfminer.RDFMiner;
import com.i3s.app.rdfminer.entity.axiom.Axiom;
import com.i3s.app.rdfminer.entity.axiom.AxiomFactory;
import com.i3s.app.rdfminer.entity.shacl.Shape;
import com.i3s.app.rdfminer.entity.shacl.ShapesManager;
import com.i3s.app.rdfminer.ht.HypothesisTesting;
import com.i3s.app.rdfminer.output.Results;
import com.i3s.app.rdfminer.sparql.corese.CoreseEndpoint;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

public class Evaluator {

	private static final Logger logger = Logger.getLogger(Evaluator.class.getName());

	Parameters parameters = Parameters.getInstance();

	public Evaluator()
			throws URISyntaxException, IOException, ExecutionException, InterruptedException {
		// set results content as JSON object
		RDFMiner.results = new Results(true);
		// special case where -af and -sf are used in the same time
		if(parameters.getAxioms() != null && parameters.getShapes() != null) {
			logger.error("(--axioms-file) and (--shapes-file) are used in the same time !");
			System.exit(1);
		} else if(parameters.getAxioms() != null) {
			// launch axioms evaluator
			runAxiomEvaluation();
		} else if(parameters.getShapes() != null) {
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
	public void runAxiomEvaluation() throws InterruptedException, ExecutionException, IOException {

		BufferedReader axiomFile;

		// Create an empty JSON array which will be fill with our results
		RDFMiner.evaluatedEntities = new JSONArray();

		Global.AXIOMS_FILE = Global.OUTPUT_PATH + parameters.getAxioms();
		logger.info("Reading axioms from file " + parameters.getAxioms() + "...");
		try {
			// Try to read the status file:
			axiomFile = new BufferedReader(new FileReader(Global.AXIOMS_FILE));
		} catch (IOException e) {
			logger.error("Could not open file " + parameters.getAxioms());
			return;
		}

		try {
			RDFMiner.output = new FileWriter(RDFMiner.outputFolder + Global.RESULTS_FILENAME);
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}

		// Prepare multi-threading
		// We have a set of threads to compute each axioms
		ExecutorService executor = Executors.newFixedThreadPool(Global.NB_THREADS);
		Set<Callable<Axiom>> callables = new HashSet<>();
		List<Axiom> axiomList = new ArrayList<>();

		// assessment of axioms
		while (true) {

			// Axiom a = null;
			String phenotype;

			try {
				phenotype = axiomFile.readLine();
				if (phenotype == null || phenotype.isEmpty()) {
					logger.info("No more axioms to evaluate ...");
					break;
				}
				String finalPhenotype = phenotype;
				callables.add(() -> {
					logger.info("Testing axiom: " + finalPhenotype);
					Axiom a = AxiomFactory.create(null, finalPhenotype, new CoreseEndpoint(Global.TARGET_SPARQL_ENDPOINT, Global.PREFIXES));
					a.setEntityAsString(finalPhenotype);
					return a;
				});
			} catch (IOException e) {
				logger.error("Could not read the next axiom.");
				e.printStackTrace();
				writeAndFinish();
				System.exit(1);
			}

		}

		// get all callables and execute it
		logger.info(callables.size() + " tasks are ready ...");
		RDFMiner.results.setNumberEntities(callables.size());
		RDFMiner.results.saveResult();
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
			RDFMiner.content.add(axiom.toJSON());
			sendEntities();
		}

//		JSONObject jsonResults = new JSONObject();
//		jsonResults.put(Results.ENTITIES, RDFMiner.evaluatedEntities);
		RDFMiner.output.write(RDFMiner.results.toJSON().toString(2));

		// Force flushing the data to the files
		RDFMiner.output.flush();
		// Close it
		RDFMiner.output.close();

		logger.info("Done testing axioms. Exiting.");
		System.exit(0);
	}

	/**
	 * The first version of RDFMiner launcher
	 */
	public void runShapeEvaluation() throws IOException {

//		BufferedReader shapeFile = null;
		String shapesContent = null;

		// Create an empty JSON object which will be fill with our results
		RDFMiner.evaluatedEntities = new JSONArray();

		if (parameters.getShapes() != null) {
			Global.SHAPES_FILE = Global.OUTPUT_PATH + parameters.getShapes();
			logger.info("Reading SHACL Shapes from file " + parameters.getShapes() + "...");
			try {
				// Try to read the status file:
				shapesContent = Files.asCharSource(new File(Global.SHAPES_FILE), Charsets.UTF_8).read();
			} catch (IOException e) {
				logger.error("Could not open file " + parameters.getShapes());
				return;
			}
		} else {
			logger.error("No SHACL file specified !");
			System.exit(1);
		}

		// ShutDownHook
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

		CoreseEndpoint endpoint = new CoreseEndpoint(Global.CORESE_IP, Global.PREFIXES);
		ShapesManager shapesManager = new ShapesManager(shapesContent, false, endpoint);

		// filewriter: json file with all shapes as JSON
		FileWriter results = new FileWriter(RDFMiner.outputFolder + Global.RESULTS_FILENAME);
		HypothesisTesting ht = new HypothesisTesting();

		for (int i=0; i<shapesManager.getPopulation().size(); i++) {
			Shape shape = shapesManager.getPopulation().get(i);
			ht.eval(shape);
//			RDFMiner.evaluatedEntities.put(shape.toJSON());
			RDFMiner.content.add(shape.toJSON());
			sendEntities();
			if (i==0) RDFMiner.output.write(shape.validationReport.getContent(true));
			else RDFMiner.output.write(shape.validationReport.getContent(false));
		}

//		JSONObject jsonResults = new JSONObject();
//		jsonResults.put(Results.ENTITIES, RDFMiner.evaluatedEntities);
		results.write(RDFMiner.results.toJSON().toString(2));

		// Force flushing the data to the files
		RDFMiner.output.flush();
		results.flush();

		RDFMiner.output.close();
		results.close();

		logger.info("Done testing SHACL shape(s). Exiting.");
		System.exit(0);
	}
	
	public static void writeAndFinish() {
		logger.warn("Shutting down RDFMiner ...");
	}

	public void sendEntities() {
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			JSONObject toSend = new JSONObject();
			toSend.put(Results.USER_ID, parameters.getUserID());
			toSend.put(Results.PROJECT_NAME, parameters.getProjectName());
			toSend.put(Results.ENTITIES, RDFMiner.content);
			HttpPut put = new HttpPut(Endpoint.API_RESULTS);
			put.setEntity(new StringEntity(toSend.toString(), ContentType.APPLICATION_JSON));
			logger.info("PUT request: updating entities ...");
			HttpResponse response = httpClient.execute(put);
			logger.info("Status code: " + response.getStatusLine().getStatusCode());
			logger.info(new BasicResponseHandler().handleResponse(response));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}




















