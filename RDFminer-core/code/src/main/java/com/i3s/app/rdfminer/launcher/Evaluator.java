package com.i3s.app.rdfminer.launcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.i3s.app.rdfminer.Endpoint;
import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.Mod;
import com.i3s.app.rdfminer.Parameters;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

public class Evaluator {

	private static final Logger logger = Logger.getLogger(Evaluator.class.getName());

	public void run(int mod) throws ExecutionException, InterruptedException {
		Parameters parameters = Parameters.getInstance();
		switch (mod) {
			case Mod.AXIOM_ASSESSMENT:
				// launch axiom evaluation
				runAxiomEvaluation(parameters);
				break;
			case Mod.SHAPE_ASSESSMENT:
				// launch shape evaluation
				runShapeEvaluation(parameters);
				break;
		}
	}
	
	/**
	 * The first version of RDFMiner launcher
	 * @param parameters
	 */
	private void runAxiomEvaluation(Parameters parameters) throws InterruptedException, ExecutionException {
		Results results = Results.getInstance();
		// Prepare multi-threading
		// We have a set of threads to compute each axioms
		ExecutorService executor = Executors.newFixedThreadPool(Global.NB_THREADS);
		Set<Callable<Axiom>> callables = new HashSet<>();
		List<Axiom> axioms = new ArrayList<>();
		// assessment of axioms
		if (parameters.getAxioms() != null) {
			// iterate on content, 1 line = 1 axiom to assess
			for (String phenotype : parameters.getAxioms().split("\n")) {
				// add axiom assessment task into callables list
				callables.add(() -> {
					logger.info("Testing axiom: " + phenotype);
					Axiom a = AxiomFactory.create(null, phenotype, new CoreseEndpoint(Global.SPARQL_ENDPOINT, Global.PREFIXES));
					a.setEntityAsString(phenotype);
					return a;
				});
			}
		} else {
			logger.error("A list of axioms must be provided to perform RDFminer !");
			// System.exit(1);
		}
		// get all callables and execute it
		logger.info(callables.size() + " tasks are ready ...");
		List<Future<Axiom>> futureAxioms = executor.invokeAll(callables);
		// We recover our axioms
		for (Future<Axiom> axiom : futureAxioms) {
			axioms.add(axiom.get());
		}
		// Shut down the executor
		executor.shutdown();
		executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		// save results
		for(Axiom axiom : axioms) {
			// add axiom results as JSON object
			results.addEntity(axiom);
		}
		// end !
		logger.info("Done testing axioms. Exiting.");
	}

	/**
	 * The first version of RDFMiner launcher
	 * @param parameters
	 */
	private void runShapeEvaluation(Parameters parameters) {
		Results results = Results.getInstance();
		// init sparql endpoint
		CoreseEndpoint endpoint = new CoreseEndpoint(parameters.getNamedDataGraph(), parameters.getPrefixes());
		// init SHACL shapes manager
		ShapesManager shapesManager = new ShapesManager(parameters.getShapes(), endpoint);
		// iterate on shapes
		HypothesisTesting ht = new HypothesisTesting();
		for (int i=0; i<shapesManager.getPopulation().size(); i++) {
			Shape shape = shapesManager.getPopulation().get(i);
			ht.eval(shape);
			// save results
			results.addEntity(shape);
//			sendEntities();
		}
		logger.info("Done testing SHACL shape(s). Exiting.");
		// System.exit(0);
	}
	
	public static void writeAndFinish() {
		logger.warn("Shutting down RDFMiner ...");
	}

	public void sendEntities() {
		Results results = Results.getInstance();
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			HttpPut put = new HttpPut(Endpoint.API_RESULTS);
			put.setEntity(new StringEntity(new ObjectMapper().writeValueAsString(results), ContentType.APPLICATION_JSON));
			logger.info("PUT request: updating entities ...");
			HttpResponse response = httpClient.execute(put);
			logger.info("Status code: " + response.getStatusLine().getStatusCode());
			logger.info(new BasicResponseHandler().handleResponse(response));
		} catch (IOException e) {
			logger.warn("RDFminer-server is offline !");
		}
	}
}




















