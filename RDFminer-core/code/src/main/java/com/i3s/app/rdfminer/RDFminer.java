/**
 * 
 */
package com.i3s.app.rdfminer;

import com.i3s.app.rdfminer.generator.Generator;
import com.i3s.app.rdfminer.generator.axiom.RandomAxiomGenerator;
import com.i3s.app.rdfminer.generator.shacl.RandomShapeGenerator;
import com.i3s.app.rdfminer.launcher.Evaluator;
import com.i3s.app.rdfminer.launcher.GrammaticalEvolution;
import com.i3s.app.rdfminer.output.SimilarityMap;
import org.apache.log4j.*;

import java.io.IOException;
import java.net.URISyntaxException;
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
public class RDFminer {

	private static final Logger logger = Logger.getLogger(RDFminer.class.getName());

	// Novelty search
	public static SimilarityMap similarityMap = null;

	/**
	 * The entry point of the RDFMiner application.
	 */
	public static void exec(Parameters parameters) throws InterruptedException, ExecutionException, URISyntaxException, IOException {
		// load additional librairies
		System.loadLibrary(Global.SO_LIBRARY);
		// Configure the log4j loggers:
		PropertyConfigurator.configure(Global.LOG4J_PROPERTIES);
		// Configuring logger on a specific file
		configureFileLogger(parameters.getProjectName());
		// Print the banner of RDFminer
		logRDFminerBanner();
		// define a set of prefixes provided by user (with -prefix option), else use the default prefixes
//		Global.PREFIXES_FILE = RDFMiner.outputFolder + parameters.getNamedDataGraph();
//		File prefixesFile = new File(Global.PREFIXES_FILE);
//		if(prefixesFile.exists()) {
//			logger.info("RDFMiner will use the specified prefixes file");
//			logger.info(prefixesFile.getAbsolutePath());
//			try {
//				Global.PREFIXES = Files.readString(Path.of(prefixesFile.getAbsolutePath()));
//			} catch (IOException e) {
//				logger.error("Error when reading the prefix file ...");
//				logger.error(e.getMessage());
//				System.exit(1);
//			}
//		} else {
//			logger.info("RDFMiner will use the default prefixes to perform SPARQL queries ...");
//		}

		// compute the number of triples published on the considered SPARQL endpoint
		// usefull to compute 'one time' the number of triples (because it is redundant and can be very long ...)
		// use it to provide it to Corese and compute the generality for each shape
//		CoreseEndpoint endpoint = new CoreseEndpoint(Global.SPARQL_ENDPOINT, Global.PREFIXES);
//		logger.info("COUNT the total number of triples available through the SPARQL endpoint: " + Global.SPARQL_ENDPOINT);
//		Global.nTriples = endpoint.count("*", "?s ?p ?o");
//		logger.info("COUNT #RDF triples: " + Global.nTriples);

		// Novelty search
//		if(parameters.isUseNoveltySearch()) {
//			Global.SIMILARITIES_FILE = Global.CACHE_FOLDER + "axioms_similarity.json";
//			if(!new File(Global.SIMILARITIES_FILE).exists()) {
//				logger.info("Create the similarity map and save it into " + Global.SIMILARITIES_FILE);
//				similarityMap = new SimilarityMap();
//			} else {
//				similarityMap = new SimilarityMap(new File(Global.SIMILARITIES_FILE));
//			}
//		}
		//
		Generator generator;
		GrammaticalEvolution evolution;
		Evaluator evaluator;
		// Launching the appropriate feature according to the parameters
		switch (parameters.getMod()) {
			// Shape Mining
			// require SHACL shapes generator to build well-formed candidates
			// grammatical evolution will be used
			case Mod.SHAPE_MINING:
				// init generator with BNF grammar provided by user
				generator = new RandomShapeGenerator(parameters.getGrammar());
				evolution = new GrammaticalEvolution();
				try {
					evolution.run(generator);
				} catch (Exception e) {
					logger.error("error during the GE process ...");
					logger.error(e.getMessage());
					// System.exit(0);
				}
				break;
			// Axiom Mining
			// require OWL axiom generator to build well-formed candidates
			// grammatical evolution will be used
			case Mod.AXIOM_MINING:
				// init generator with BNF grammar provided by user
				generator = new RandomAxiomGenerator(parameters.getGrammar(), true);
				evolution = new GrammaticalEvolution();
				try {
					evolution.run(generator);
				} catch (Exception e) {
					logger.error("error during the GE process ...");
					logger.error(e.getMessage());
					// System.exit(0);
				}
				break;
			// OWL axioms or SHACL shapes assessment
			// The system can assess (1) SubClassOf and DisjointClass oxioms and (2) SHACL shapes
			case Mod.AXIOM_ASSESSMENT:
			case Mod.SHAPE_ASSESSMENT:
				// launch evaluator
				evaluator = new Evaluator();
				evaluator.run(parameters.getMod());
				break;

		}
//		if(parameters.grammaticalEvolution) {
//			try {
//				GrammaticalEvolution evolution = new GrammaticalEvolution();
//				evolution.run();
//			} catch (Exception e) {
//				e.printStackTrace();
//				System.exit(0);
//			}
//		} else {
//			// launch evaluator !
//			new Evaluator();
//		}
	}

	private static void configureFileLogger(String projectName) {
		try {
			String filePath = "/user/rfelin/home/projects/RDFMining/IO/logs/" + projectName + ".log";
			//
			RollingFileAppender fileAppender = new RollingFileAppender(
					new PatternLayout("%d{yyyy-MM-dd HH:mm:ss} [%t] %-5p %c - %m%n"), filePath);
			fileAppender.setMaxFileSize("1GB");
			fileAppender.setMaxBackupIndex(5);
			//
			logger.addAppender(fileAppender);
			logger.setLevel(Level.DEBUG);
			//
			Logger.getRootLogger().removeAppender("server");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void logRDFminerBanner() {
		for(String line : Global.BANNER) {
			logger.info(line);
		}
		logger.info("This is RDFminer v." + System.getenv("RDFMINER_VERSION") + " !");
	}

}
