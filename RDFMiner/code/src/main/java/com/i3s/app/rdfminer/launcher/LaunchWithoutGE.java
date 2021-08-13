package com.i3s.app.rdfminer.launcher;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import org.apache.jena.shared.JenaException;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.RDFMiner;
import com.i3s.app.rdfminer.axiom.Axiom;
import com.i3s.app.rdfminer.axiom.AxiomFactory;
import com.i3s.app.rdfminer.axiom.AxiomGenerator;
import com.i3s.app.rdfminer.axiom.CandidateAxiomGenerator;
import com.i3s.app.rdfminer.axiom.IncreasingTimePredictorAxiomGenerator;
import com.i3s.app.rdfminer.axiom.RandomAxiomGenerator;
import com.i3s.app.rdfminer.axiom.type.SubClassOfAxiom;
import com.i3s.app.rdfminer.output.AxiomJSON;
import com.i3s.app.rdfminer.parameters.CmdLineParameters;
import com.i3s.app.rdfminer.sparql.SparqlEndpoint;

import Individuals.Phenotype;

public class LaunchWithoutGE {

	private static Logger logger = Logger.getLogger(LaunchWithoutGE.class.getName());

	public final static String PREFIXES = "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
			+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"
			+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
			+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
			+ "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" + "PREFIX dc: <http://purl.org/dc/elements/1.1/>\n"
			+ "PREFIX : <http://dbpedia.org/resource/>\n" + "PREFIX dbpedia2: <http://dbpedia.org/property/>\n"
			+ "PREFIX dbpedia: <http://dbpedia.org/>\n" + "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n"
			+ "PREFIX dbo: <http://dbpedia.org/ontology/>\n";
	
	private List<JSONObject> axioms;
	
	/**
	 * The first version of RDFMiner launcher
	 */
	public void run(CmdLineParameters parameters) {
		
		AxiomGenerator generator = null;
		BufferedReader axiomFile = null;

		// Create an empty JSON object which will be fill with our results
		RDFMiner.axiomsList = new JSONArray();
		axioms = new ArrayList<>();

		// Set SPARQL Endpoit
		RDFMiner.endpoint = new SparqlEndpoint(Global.SPARQL_ENDPOINT, PREFIXES);
		
		if (parameters.axiomFile == null) {
			if (parameters.axiom == null) {
				if (parameters.useRandomAxiomGenerator) {
					logger.info(
							"Initializing the random axiom generator with grammar " + parameters.grammarFile + "...");
					generator = new RandomAxiomGenerator(parameters.grammarFile, false);
				} else if (parameters.subclassList != null) {
					logger.info("Initializing the increasing TP axiom generator...");
					generator = new IncreasingTimePredictorAxiomGenerator(parameters.subclassList);
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
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				// Save results in output file
				if (parameters.axiom == null)
					writeAndFinish();
			}
		});
				
		RDFMiner.executor = Executors.newSingleThreadExecutor();

		if (parameters.axiom == null) {
			// as the test of a single axiom is return on standard output, we don't need to
			// write file of the results
			try {
				RDFMiner.output = new FileWriter(parameters.resultFile);
			} catch (IOException e) {
				logger.error(e.getMessage());
				e.printStackTrace();
				System.exit(1);
			}
		}

		while (true) {

			Axiom a = null;
			String axiomName = null;
			long t0 = RDFMiner.getProcessCPUTime();

			if (generator != null) {
				Phenotype axiom = generator.nextAxiom();
				if (axiom == null)
					break;
				axiomName = axiom.getStringNoSpace();
				logger.info("Testing axiom: " + axiomName);
				try {
					a = AxiomFactory.create(axiom, RDFMiner.endpoint);
				} catch (QueryExceptionHTTP httpError) {
					logger.error("HTTP Error " + httpError.getMessage() + " making a SPARQL query.");
					httpError.printStackTrace();
					writeAndFinish();
					System.exit(1);
				} catch (JenaException jenaException) {
					logger.error("Jena Exception " + jenaException.getMessage() + " making a SPARQL query.");
					jenaException.printStackTrace();
					writeAndFinish();
					System.exit(1);
				}
			} else {
				try {
					if (axiomFile == null && parameters.axiom != null) {
						axiomName = parameters.axiom;
					} else if (axiomFile != null && parameters.axiom == null) {
						axiomName = axiomFile.readLine();
					} else {
						logger.error("'-a' and '-sa' are used at the same time");
						System.exit(1);
					}
					if (axiomName == null)
						break;
					if (axiomName.isEmpty())
						break;
					logger.info("Testing axiom: " + axiomName);
					a = AxiomFactory.create(axiomName, RDFMiner.endpoint);
				} catch (IOException e) {
					writeAndFinish();
					logger.error("Could not read the next axiom.");
					e.printStackTrace();
					System.exit(1);
				}
			}

			// long t = System.currentTimeMillis();
			long t = RDFMiner.getProcessCPUTime();

			if (a != null) {
				// Save a JSON report of the test
				AxiomJSON reportJSON = new AxiomJSON();
				reportJSON.axiom = axiomName;
				reportJSON.elapsedTime = t - t0;
				reportJSON.referenceCardinality = a.referenceCardinality;
				reportJSON.numConfirmations = a.numConfirmations;
				reportJSON.numExceptions = a.numExceptions;
				reportJSON.possibility = a.possibility().doubleValue();
				reportJSON.necessity = a.necessity().doubleValue();
				reportJSON.isTimeout = a.isTimeout;
				reportJSON.generality = a.generality;
				reportJSON.fitness = a.fitness;
				if (a.numConfirmations > 0 && a.numConfirmations < 100)
					reportJSON.confirmations = a.confirmations;
				if (a.numExceptions > 0 && a.numExceptions < 100)
					reportJSON.exceptions = a.exceptions;

				// fill json results
				RDFMiner.axiomsList.put(reportJSON.toJSON());

				// print useful results
				logger.info("Num. confirmations: " + a.numConfirmations);
				logger.info("Num. exceptions: " + a.numExceptions);
//				logger.info("Possibility = " + a.possibility().doubleValue());
//				logger.info("Necessity = " + a.necessity().doubleValue());

				if (a instanceof SubClassOfAxiom && a.necessity().doubleValue() > 1.0 / 3.0) {
					SubClassOfAxiom sa = (SubClassOfAxiom) a;
					SubClassOfAxiom.maxTestTime.maxput(sa.timePredictor(), t - t0);
				}

				if (parameters.axiom != null) {
					System.out.println("[RES]" + RDFMiner.results.toString());
					break;
				}

			} else
				logger.warn("Axiom type not supported yet!");
			logger.info("Test completed in " + (t - t0) + " ms.");
		}
		logger.info("Done testing axioms. Exiting.");
//		if (parameters.axiom == null) {
//			try {
//				output.write(RDFMiner.results.toString());
//				output.close();
//			} catch (IOException e) {
//				logger.error("I/O error while closing JSON writer: " + e.getMessage());
//				e.printStackTrace();
//				System.exit(1);
//			}
//			writeAndFinish();
//		}
		System.exit(0);
	}
	
	public void writeAndFinish() {
		try {
			logger.warn("Shutting down RDFMiner ...");
			RDFMiner.output.write(RDFMiner.axiomsList.toString());
			RDFMiner.output.close();
		} catch (IOException e) {
			logger.error("I/O error while closing JSON writer: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}

}
