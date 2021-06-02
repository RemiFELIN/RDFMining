/**
 * 
 */
package com.i3s.app.rdfminer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.jena.shared.JenaException;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import com.i3s.app.rdfminer.axiom.Axiom;
import com.i3s.app.rdfminer.axiom.AxiomFactory;
import com.i3s.app.rdfminer.axiom.AxiomGenerator;
import com.i3s.app.rdfminer.axiom.CandidateAxiomGenerator;
import com.i3s.app.rdfminer.axiom.IncreasingTimePredictorAxiomGenerator;
import com.i3s.app.rdfminer.axiom.RandomAxiomGenerator;
import com.i3s.app.rdfminer.axiom.type.SubClassOfAxiom;
import com.i3s.app.rdfminer.output.AxiomTestCSV;
import com.i3s.app.rdfminer.output.AxiomTestXML;
import com.i3s.app.rdfminer.parameters.CmdLineParameters;
import com.i3s.app.rdfminer.sparql.SparqlEndpoint;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import Individuals.Phenotype;

//import com.hp.hpl.jena.shared.JenaException;
//import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP;

/**
 * The main class of the RDFMiner experimental tool.
 * <p>More information about OWL 2 may be found in the
 * <a href="http://www.w3.org/TR/2012/REC-owl2-quick-reference-20121211/">OWL2 Quick Reference, 2nd Edition</a>.</p>
 * 
 * @author Andrea G. B. Tettamanzi
 *
 */
public class RDFMiner {
	
	private static Logger logger = Logger.getLogger(RDFMiner.class.getName());
	
	public static CmdLineParameters parameters = new CmdLineParameters();
	
	final private static String PREFIXES =
			"PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
			"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
			"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
			"PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
			"PREFIX dc: <http://purl.org/dc/elements/1.1/>\n" +
			"PREFIX : <http://dbpedia.org/resource/>\n" +
			"PREFIX dbpedia2: <http://dbpedia.org/property/>\n" +
			"PREFIX dbpedia: <http://dbpedia.org/>\n" +
			"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n" +
			"PREFIX dbo: <http://dbpedia.org/ontology/>\n";
	
	/**
	 * A SPARQL endpoint which can be used to query the RDF repository.
	 */
	public static SparqlEndpoint endpoint;
	
	/**
	 * An executor to be used to submit asynchronous tasks which might be subjected to a time-out. 
	 */
	public static ExecutorService executor;
	
	/**
	 * A service native method to query for CPU usage.
	 * <p>The name and implementation of this method are adapted from
	 * <a href="http://www.javaworld.com/article/2077361/learn-java/profiling-cpu-usage-from-within-a-java-application.html">this
	 * 2002 blog post</a>.</p>
	 * <p>The implementation in C language of this native method is contained in the two source files
	 * <code>rdfminer_RDFMiner.h</code> and <code>rdfminer_RDFMiner.c</code>.</p>
	 * 
	 * @return the number of milliseconds of CPU time used by the current process so far
	 */
	public static native long getProcessCPUTime();

	/**
	 * The entry point of the RDF Miner application.
	 */
	public static void main(String[] args) {
		
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
        }
        catch(CmdLineException e) {
            // if there's a problem in the command line, you'll get this 
        	// exception. this will report an error message.
            System.err.println(e.getMessage());
            // print the list of available options
            System.err.println();
            parser.printUsage(System.err);
            System.err.println();
            return;
        }
        
        if(RDFMiner.parameters.help) {
        	// print the list of available options
        	System.out.println();
        	parser.printUsage(System.out);
        	System.out.println();
        	return;
        }
        
        // Get environment variable from container (defined in Dockerfile)
		logger.info("This is RDF Miner, version " + System.getenv("RDFMINER_VERSION"));
		// Load rdfminer_RDFMINER.so generated by ./compile_c_code.sh (see /scripts folder)
		System.loadLibrary("rdfminer_RDFMiner");
		// Set SPARQL Endpoit
		endpoint = new SparqlEndpoint(Global.SPARQL_ENDPOINT, PREFIXES);
		
		Marshaller marshaller = null;
		FileOutputStream xmlStream = null;
		File csvStream = new File(parameters.resultFile + ".csv");
		Writer writer = null;
		StatefulBeanToCsv<AxiomTestCSV> beanToCsv = null;
		
		try {
		    // prepare XML File output
			xmlStream = new FileOutputStream(parameters.resultFile + ".xml", true); // here, 'true' means 'append'...
		    // prepare CSV File output
			writer = new PrintWriter(csvStream);
			// Set header of CSV File
			writer.append(AxiomTestCSV.COLUMNS_NAME);
		    beanToCsv = new StatefulBeanToCsvBuilder<AxiomTestCSV>(writer).withApplyQuotesToAll(false).build();
		    // prepare JAXBContext to edit the XML File
		    JAXBContext context = JAXBContext.newInstance(AxiomTestXML.class);
		    marshaller = context.createMarshaller();
		    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		}
		catch(JAXBException | IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
		
		AxiomGenerator generator = null;
		BufferedReader axiomFile = null;
		
		if(parameters.axiomFile==null) {
			if(parameters.useRandomAxiomGenerator) {
				logger.info("Initializing the random axiom generator with grammar " + parameters.grammarFile + "...");
				generator = new RandomAxiomGenerator(parameters.grammarFile);
			}
			else if(parameters.subclassList!=null) {
				logger.info("Initializing the increasing TP axiom generator...");
				generator = new IncreasingTimePredictorAxiomGenerator(parameters.subclassList);
			}
			else {
				logger.info("Initializing the candidate axiom generator...");
				generator = new CandidateAxiomGenerator(parameters.grammarFile);
			}
		}
		else {
			logger.info("Reading axioms from file " + parameters.axiomFile + "...");
			try {
				// Try to read the status file:
				axiomFile = new BufferedReader(new FileReader(parameters.axiomFile));
			}
			catch(IOException e) {
				logger.error("Could not open file " + parameters.axiomFile);
				return;
			}
		}
		
		executor = Executors.newSingleThreadExecutor();
		
		while(true) {
			
			Axiom a = null;
			String axiomName = null;
			long t0 = getProcessCPUTime();
			
			if(generator!=null) {
				Phenotype axiom = generator.nextAxiom();
				if(axiom==null)
					break;
				axiomName = axiom.getStringNoSpace();
				logger.info("Testing axiom: " + axiomName);
				try {
					a = AxiomFactory.create(axiom);
				}
				catch (QueryExceptionHTTP httpError) {
					logger.error("HTTP Error " + httpError.getMessage() + " making a SPARQL query.");
					httpError.printStackTrace();
					System.exit(1);
				}
				catch (JenaException jenaException) {
					logger.error("Jena Exception " + jenaException.getMessage() + " making a SPARQL query.");
					jenaException.printStackTrace();
					System.exit(1);
				}
			}
			else {
				try {
					axiomName = axiomFile.readLine();
					if(axiomName==null)
						break;
					if(axiomName.isEmpty())
						break;
					logger.info("Testing axiom: " + axiomName);
					a = AxiomFactory.create(axiomName);
				} catch (IOException e) {
					logger.error("Could not read the next axiom.");
					e.printStackTrace();
					System.exit(1);
				}
			}
		
			// long t = System.currentTimeMillis();
			long t = getProcessCPUTime();
			
			if(a != null) {
				// Save an XML report of the confirmations and the exceptions:
				AxiomTestXML reportXML = new AxiomTestXML();
				reportXML.axiom = axiomName;
				if(a.numConfirmations<100)
					reportXML.confirmations = a.confirmations;
				if(a.numExceptions<100)
					reportXML.exceptions = a.exceptions;
				// Save an CSV report of the test:
				AxiomTestCSV reportCSV = new AxiomTestCSV();
				reportCSV.axiom = axiomName;
				reportCSV.elapsedTime = t - t0;
				reportCSV.referenceCardinality = a.referenceCardinality;
				reportCSV.numConfirmations = a.numConfirmations;
				reportCSV.numExceptions = a.numExceptions;
				reportCSV.possibility = a.possibility().doubleValue();
				reportCSV.necessity = a.necessity().doubleValue();
				// print useful results
				logger.info("Num. confirmations: " + a.numConfirmations);
				logger.info("Num. exceptions: " + a.numExceptions);
				logger.info("Possibility = " + reportCSV.possibility);
				logger.info("Necessity = " + reportCSV.necessity);
				
				if(a instanceof SubClassOfAxiom && reportCSV.necessity > 1.0/3.0) {
					SubClassOfAxiom sa = (SubClassOfAxiom) a;
					SubClassOfAxiom.maxTestTime.maxput(sa.timePredictor(), reportCSV.elapsedTime);
				}
				// Edit CSV file
				if(writer!=null && beanToCsv!=null) {
					try {
						beanToCsv.write(reportCSV);
					} catch (CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
						logger.error("OpenCSV error : " + e.getMessage());
						e.printStackTrace();
						System.exit(1);
					}
				}
				// Edit XML file
				if(marshaller!=null) {
					// We write confirmations and exceptions if we found at least one confirmation or exception
					if(reportCSV.numConfirmations > 0 && reportCSV.numConfirmations < 100 || reportCSV.numExceptions > 0 && reportCSV.numExceptions < 100) {
						try {
							marshaller.marshal(reportXML, xmlStream);
							xmlStream.flush();
						}
						catch(JAXBException e) {
							logger.error("Marshaling error while writing test report to the XML file:" + e.getMessage());
							e.printStackTrace();
						}
						catch(IOException e) {
							logger.error("I/O error while writing test report to the XML file:" + e.getMessage());
							e.printStackTrace();
						}
					}
				}
			}
			else
				logger.warn("Axiom type not supported yet!");
			logger.info("Test completed in " + (t - t0) + " ms.");
		}
		logger.info("Done testing axioms. Exiting.");
		try {
			writer.close();
		} catch (IOException e) {
			logger.error("I/O error while closing CSV writer: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
		System.exit(0);
	}
	
}
