package rdfminer;

/**
 * This class is used to centralize paths of each resources used around the project
 * @author remifelin
 *
 */
public class Global {

	/**
	 * Path to log4j.properties file
	 */
	public static final String LOG4J_PROPERTIES = System.getenv("HOME") + "/code/resources/log4j.properties";
	
	/**
	 * path to DBPEDIA TDB (not used in this version)
	 */
	public static final String DBPEDIA_TDB_PATH = "/home/remi/Bureau/dev/DBPedia/tdb";
	
	/**
	 * DBPEDIA endpoint
	 */
	public static final String DBPEDIA_SERVER = "http://134.59.130.136:8890/";
	// public static final String DBPEDIA_SERVER = "http://dbpedia.org/";
	
}
