package com.i3s.app.rdfminer;

/**
 * This class is used to centralize paths of each resources used around the
 * project
 * 
 * @author remifelin
 *
 */
public class Global {
	
	public static final String BANNER = "  _____  _____  ______   __  __ _____ _   _ ______ _____  \n"
			+ " |  __ \\|  __ \\|  ____| |  \\/  |_   _| \\ | |  ____|  __ \\ \n"
			+ " | |__) | |  | | |__    | \\  / | | | |  \\| | |__  | |__) |\n"
			+ " |  _  /| |  | |  __|   | |\\/| | | | | . ` |  __| |  _  / \n"
			+ " | | \\ \\| |__| | |      | |  | |_| |_| |\\  | |____| | \\ \\ \n"
			+ " |_|  \\_\\_____/|_|      |_|  |_|_____|_| \\_|______|_|  \\_\\\n"
			+ "                                                          \n"
			+ "\033[0;1m" + " VERSION " + System.getenv("RDFMINER_VERSION") + "\033[0m" + "\n";

	/**
	 * Path to log4j.properties file
	 */
	public static final String LOG4J_PROPERTIES = System.getenv("HOME") + "/code/resources/log4j.properties";

	/**
	 * path to DBPEDIA TDB (not used in this version)
	 */
	public static final String DBPEDIA_TDB_PATH = "/home/remi/Bureau/dev/DBPedia/tdb";

	/**
	 * SPARQL Endpoint : our Virtuoso server endpoint
	 */
	public static final String REMOTE_SPARQL_ENDPOINT = "http://134.59.130.136:8890/sparql";
	
	public final static String REMOTE_PREFIXES = "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
			+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"
			+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
			+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
			+ "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" + "PREFIX dc: <http://purl.org/dc/elements/1.1/>\n"
			+ "PREFIX : <http://dbpedia.org/resource/>\n" + "PREFIX dbpedia2: <http://dbpedia.org/property/>\n"
			+ "PREFIX dbpedia: <http://dbpedia.org/>\n" + "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n"
			+ "PREFIX dbo: <http://dbpedia.org/ontology/>\n";
	
	/**
	 * Local SPARQL Endpoint
	 */
	public static final String LOCAL_SPARQL_ENDPOINT = "http://172.19.0.2:9000/sparql";

	final public static String LOCAL_PREFIXES = "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
			+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"
			+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
			+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
			+ "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" + "PREFIX dc: <http://purl.org/dc/elements/1.1/>\n"
			+ "PREFIX dbr: <http://dbpedia.org/resource/>\n" + "PREFIX dbp: <http://dbpedia.org/property/>\n"
			+ "PREFIX : <http://dbpedia.org/resource/>\n" + "PREFIX dbpedia2: <http://dbpedia.org/property/>\n"
			+ "PREFIX dbpedia: <http://dbpedia.org/>\n" + "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n"
			+ "PREFIX dbo: <http://dbpedia.org/ontology/>\n" + "PREFIX ex:    <http://example.org/demo#> \n"
			+ "PREFIX rdfs:  <http://www.w3.org/2000/01/rdf-schema#> \n" + "PREFIX sp:    <http://spinrdf.org/sp#> \n";
	
	/**
	 * Output path : corresponding to '/rdfminer/io/' in container
	 */
	public static final String OUTPUT_PATH = System.getenv("HOME") + "/io/";
	
	/**
	 * .cache files folder path
	 */
	public static final String CACHE_PATH = System.getenv("HOME") + "/caches/";
	
	public static final int NB_THREADS = Runtime.getRuntime().availableProcessors();
	
}
