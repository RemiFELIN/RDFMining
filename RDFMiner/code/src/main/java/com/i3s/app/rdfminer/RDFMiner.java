/**
 * 
 */
package com.i3s.app.rdfminer;

import java.io.File;
import java.io.FileWriter;
import java.util.List;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import com.i3s.app.rdfminer.grammar.evolutionary.CostGP;
import com.i3s.app.rdfminer.launcher.LaunchWithGE;
import com.i3s.app.rdfminer.launcher.LaunchWithoutGE;
import com.i3s.app.rdfminer.output.ResultsJSON;
import com.i3s.app.rdfminer.output.StatJSON;
import com.i3s.app.rdfminer.parameters.CmdLineParameters;

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

	private static Logger logger = Logger.getLogger(RDFMiner.class.getName());

	public static CmdLineParameters parameters = new CmdLineParameters();

	/**
	 * The output file in json
	 */
	public static FileWriter output;
	public static String outputFolder;
	
	// v1.0 evaluate data
	public static JSONArray axiomsList;
	// v1.2 miner data
	public static ResultsJSON results;
	public static StatJSON stats;
	public static List<JSONObject> axioms;

	/**
	 * A service native method to query for CPU usage.
	 * <p>
	 * The name and implementation of this method are adapted from <a href=
	 * "http://www.javaworld.com/article/2077361/learn-java/profiling-cpu-usage-from-within-a-java-application.html">this
	 * 2002 blog post</a>.
	 * </p>
	 * <p>
	 * The implementation in C language of this native method is contained in the
	 * two source files <code>rdfminer_RDFMiner.h</code> and
	 * <code>rdfminer_RDFMiner.c</code>.
	 * </p>
	 * 
	 * @return the number of milliseconds of CPU time used by the current process so
	 *         far
	 */
	public static native long getProcessCPUTime();

	/**
	 * A table of predicates, used in {@link CostGP}
	 */
	public static String[][] predicateTable;

	/**
	 * The entry point of the RDF Miner application.
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException {

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

		if (RDFMiner.parameters.help) {
			// print the list of available options
			System.out.println();
			parser.printUsage(System.out);
			System.out.println();
			return;
		}
		
		logger.info("Number of processors avalaibles: " + Global.NB_THREADS);
		
		if(parameters.timeOut != 0)
			logger.info("Time cap initialized at " + parameters.timeOut + " seconde(s)");
		
		// Load rdfminer_RDFMINER.so generated by ./compile_c_code.sh (see /scripts
		// folder)
		System.loadLibrary("rdfminer_RDFMiner");

		// Create cache folder if it not already exists
		if(!(new File(Global.CACHE_PATH)).exists()) {
			(new File(Global.CACHE_PATH)).mkdir();
			logger.info("Cache folder successfully created");
		}
		
		if (parameters.axiom == null) {
			if(!(new File(Global.OUTPUT_PATH + parameters.resultFolder)).exists()) {
				(new File(Global.OUTPUT_PATH + parameters.resultFolder)).mkdirs();
				logger.info(parameters.resultFolder + " folder successfully created");
			}
		}
		RDFMiner.outputFolder = Global.OUTPUT_PATH + parameters.resultFolder;
		
		// If parameters.grammaticalEvolution is used, we launch an instance of
		// Grammar-based genetic programming
		if(parameters.grammaticalEvolution) {
			try {
				LaunchWithGE miner = new LaunchWithGE();
				miner.run(parameters);
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(0);
			}
		} else {
			LaunchWithoutGE evaluate = new LaunchWithoutGE();
			evaluate.run(parameters);
		}
	}

}
