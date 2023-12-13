/**
 * 
 */
package com.i3s.app.rdfminer;

import com.i3s.app.rdfminer.evolutionary.tools.CostGP;
import com.i3s.app.rdfminer.launcher.GrammaticalEvolution;
import com.i3s.app.rdfminer.launcher.evaluator.Evaluator;
import com.i3s.app.rdfminer.output.Results;
import com.i3s.app.rdfminer.output.SimilarityMap;
import com.i3s.app.rdfminer.output.Stat;
import com.i3s.app.rdfminer.parameters.CmdLineParameters;
import com.i3s.app.rdfminer.sparql.corese.CoreseEndpoint;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * The main class of the RDFMiner experimental tool.
 * <p>
 * More information about OWL 2 may be found in the
 * <a href="http://www.w3.org/TR/2012/REC-owl2-quick-reference-20121211/">OWL2
 * Quick Reference, 2nd Edition</a>.
 * </p>
 * 
 * @author Andrea G. B. Tettamanzi & Rémi FELIN
 *
 */
public class RDFMiner {

	private static final Logger logger = Logger.getLogger(RDFMiner.class.getName());

	public static CmdLineParameters parameters = new CmdLineParameters();

	/**
	 * The output file in json
	 */
	public static FileWriter output;
	public static String outputFolder;
	
	// v1.0 evaluate data
	public static JSONArray evaluatedEntities;
	// v1.2 miner data
	public static Results results;
	public static Stat stats;
	public static List<JSONObject> content;
	public static int type;
	// v1.5 Novelty search
	public static SimilarityMap similarityMap = null;

	/**
	 * A table of predicates, used in {@link CostGP}
	 */
	public static String[][] predicateTable;

	/**
	 * The entry point of the RDFMiner application.
	 */
	public static void main(String[] args) throws InterruptedException, ExecutionException, URISyntaxException, IOException {

		// Print the banner of RDF Miner
		System.out.println(Global.BANNER);
		
		// Configure the log4j loggers:
		PropertyConfigurator.configure(Global.LOG4J_PROPERTIES);
		
		// Parse the command-line parameters and options:
		CmdLineParser parser = new CmdLineParser(parameters);

		// if you have a wider console, you could increase the value;
		// here 80 is also the default
		parser.getProperties().withUsageWidth(80);

		try {
			// parse the arguments.
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			// if there's a problem in the command line, you'll get this
			// exception. this will report an error message.
			System.err.println(e.getMessage());
			// print the list of available options
			System.err.println();
			parser.printUsage(System.out);
			System.err.println();
			return;
		}

		if (parameters.help) {
			// print the list of available options
			System.out.println();
			parser.printUsage(System.out);
			System.out.println();
			return;
		}
		
		logger.info("Number of processors avalaibles: " + Global.NB_THREADS);
		
		if(parameters.sparqlTimeOut != 0)
			logger.info("Time cap initialized at " + parameters.sparqlTimeOut + " ms.");

		System.loadLibrary(Global.SO_LIBRARY);

		// Update output and caches path
		Global.OUTPUT_PATH += Global.USERS + parameters.username + "/";

		if(parameters.singleAxiom == null) {
			logger.info("Output folder: " + Global.OUTPUT_PATH + parameters.directory);
			if(!(new File(Global.OUTPUT_PATH + parameters.directory)).exists()) {
				boolean created = (new File(Global.OUTPUT_PATH + parameters.directory)).mkdirs();
				if(created)
					logger.info("Successfully created !");
			}
			RDFMiner.outputFolder = Global.OUTPUT_PATH + parameters.directory;
		}
		// Create cache folder if it not already exists
		Global.CACHE_FOLDER = RDFMiner.outputFolder + "/caches/";
		if(!(new File(Global.CACHE_FOLDER)).exists()) {
			boolean created = (new File(Global.CACHE_FOLDER)).mkdir();
			if(created)
				logger.info("Cache folder successfully created");
		}

		// define a SPARQL Endpoint to use if provided
		// todo: switch case ???
		if ((parameters.useClassicShaclMode || parameters.useProbabilisticShaclMode) && parameters.targetSparqlEndpoint == null) {
			// SHACL Shapes mining !
			Global.TARGET_SPARQL_ENDPOINT = Global.CORESE_IP;
			logger.warn("RDFMiner will query the Corese semantic factory: " + Global.TARGET_SPARQL_ENDPOINT);
			logger.warn("This version of Corese contains an implementation of SHACL (standard and probabilistic) ...");
		} else if(parameters.targetSparqlEndpoint != null) {
			logger.info("(--target-endpoint) a target SPARQL Endpoint is specified !");
			try {
				// Test if the given url is a valid URL or not
				new URL(parameters.targetSparqlEndpoint);
			} catch (MalformedURLException e) {
				logger.error("The given SPARQL Endpoint is not a valid URL ...");
				System.exit(1);
			}
			Global.TARGET_SPARQL_ENDPOINT = parameters.targetSparqlEndpoint;
			logger.info("RDFMiner will query the following link in SERVICE clause: " + Global.TARGET_SPARQL_ENDPOINT);
		} else {
			Global.TARGET_SPARQL_ENDPOINT = Global.VIRTUOSO_DBPEDIA_2015_04_SPARQL_ENDPOINT;
			logger.warn("No target database specified !");
			logger.warn("RDFMiner will query the following link in SERVICE clause: " + Global.TARGET_SPARQL_ENDPOINT);
			logger.warn("This database contains a dump of DBPedia 2015-04 ...");
		}
		// define a training dataset if it's provided
		if(parameters.trainSparqlEndpoint != null) {
			logger.info("(--train-endpoint) a training SPARQL Endpoint is specified !");
			try {
				// Test if the given url is a valid URL or not
				new URL(parameters.trainSparqlEndpoint);
			} catch (MalformedURLException e) {
				logger.error("The given SPARQL Endpoint is not a valid URL ...");
				System.exit(1);
			}
			Global.TRAINING_SPARQL_ENDPOINT = parameters.trainSparqlEndpoint;
			logger.info("RDFMiner will query the following link in SERVICE clause: " + Global.TRAINING_SPARQL_ENDPOINT);
		} else if(parameters.grammaticalEvolution && (!parameters.useProbabilisticShaclMode && !parameters.useClassicShaclMode)) {
			logger.warn("Grammatical evolution activated without training dataset specified !");
			logger.warn("RDFMiner will query the target database in SERVICE clause: " + Global.TARGET_SPARQL_ENDPOINT);
			logger.warn("The processes may take longer if the target database contains a large set of RDF triples ...");
			Global.TRAINING_SPARQL_ENDPOINT = Global.TARGET_SPARQL_ENDPOINT;
		}

		// define a set of prefixes provided by user (with -prefix option), else use the default prefixes
		Global.PREFIXES_FILE = RDFMiner.outputFolder + parameters.prefixesFile;
		File prefixesFile = new File(Global.PREFIXES_FILE);
		if(prefixesFile.exists()) {
			logger.info("RDFMiner will use the specified prefixes file");
			logger.info(prefixesFile.getAbsolutePath());
			try {
				Global.PREFIXES = Files.readString(Path.of(prefixesFile.getAbsolutePath()));
			} catch (IOException e) {
				logger.error("Error when reading the prefix file ...");
				logger.error(e.getMessage());
				System.exit(1);
			}
		} else {
			logger.info("RDFMiner will use the default prefixes to perform SPARQL queries ...");
		}

		// compute the number of triples published on the considered SPARQL endpoint
		// usefull to compute 'one time' the number of triples (because it is redundant and can be very long ...)
		// use it to provide it to Corese and compute the generality for each shape
		CoreseEndpoint endpoint = new CoreseEndpoint(Global.TARGET_SPARQL_ENDPOINT, Global.PREFIXES);
		logger.info("COUNT the total number of triples available through the SPARQL endpoint: " + Global.TARGET_SPARQL_ENDPOINT);
		Global.nTriples = endpoint.count("*", "?s ?p ?o");
		logger.info("COUNT #RDF triples: " + Global.nTriples);

		// Novelty search
		if(parameters.useNoveltySearch) {
			Global.SIMILARITIES_FILE = Global.CACHE_FOLDER + "axioms_similarity.json";
			if(!new File(Global.SIMILARITIES_FILE).exists()) {
				logger.info("Create the similarity map and save it into " + Global.SIMILARITIES_FILE);
				similarityMap = new SimilarityMap();
			} else {
				similarityMap = new SimilarityMap(new File(Global.SIMILARITIES_FILE));
			}
		}

		// If parameters.grammaticalEvolution is used, we launch an instance of
		// Grammar-based genetic programming
		if(parameters.grammaticalEvolution) {
			try {
				GrammaticalEvolution.run();
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(0);
			}
		} else {
			// launch evaluator !
			new Evaluator();
		}
	}

	public static void sendEntities() {
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			JSONObject toSend = new JSONObject();
			toSend.put(Results.USER_ID, RDFMiner.parameters.username);
			toSend.put(Results.PROJECT_NAME, RDFMiner.parameters.directory);
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
