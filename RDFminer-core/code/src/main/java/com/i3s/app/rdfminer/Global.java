package com.i3s.app.rdfminer;

/**
 * This class is used to centralize paths of each resources used around the
 * project
 *
 * @author Rémi FELIN
 */
public class Global {

    /**
     * This is a banner to present RDF Miner on a terminal
     */
    public static final String BANNER = "  _____  _____  ______   __  __ _____ _   _ ______ _____  \n"
            + " |  __ \\|  __ \\|  ____| |  \\/  |_   _| \\ | |  ____|  __ \\ \n"
            + " | |__) | |  | | |__    | \\  / | | | |  \\| | |__  | |__) |\n"
            + " |  _  /| |  | |  __|   | |\\/| | | | | . ` |  __| |  _  / \n"
            + " | | \\ \\| |__| | |      | |  | |_| |_| |\\  | |____| | \\ \\ \n"
            + " |_|  \\_\\_____/|_|      |_|  |_|_____|_| \\_|______|_|  \\_\\\n"
            + "                                                          \n" + "\033[0;1m" + " VERSION "
            + System.getenv("RDFMINER_VERSION") + "\033[0m" + "\n";

    public static final String HOME = System.getenv("HOME");

    /**
     * Path to log4j.properties file
     */
    public static final String LOG4J_PROPERTIES = HOME + "/code/resources/log4j.properties";

    /**
     * Load librdfminer_entity_Entity.so generated by ./compile_c_code.sh (see /scripts folder)
     */
    public static final String SO_LIBRARY = "rdfminer_entity_Entity";

    /**
     * path to DBPEDIA TDB (not used in this version)
     */
    public static final String DBPEDIA_TDB_PATH = HOME + "/tdb/";

    /**
     * The SPARQL Endpoint targetted (to query it)
     */
    public static String TARGET_SPARQL_ENDPOINT = null;

    /**
     * SPARQL endpoint which contains full instance of DBPedia
     */
    public final static String VIRTUOSO_DBPEDIA_2015_04_SPARQL_ENDPOINT = "store:DBpedia";

    /**
     * SPARQL endpoint from local server used as training dataset (a part of full dataset used)
     * By default, it contains 1% of full instance of DBPedia 2015.04
     */
    public static String TRAINING_SPARQL_ENDPOINT = "store:SmallDBpedia";

    /**
     * Corese IP Address
     */
    public static final String CORESE_IP = "http://172.19.0.1:" + System.getenv("CORESE_PORT") + "/";

    /**
     * RDFMiner Server IP Address
     */
    public static final String RDFMINER_SERVER_IP = "http://server:" + System.getenv("RDFMINER_SERVER_PORT") + "/";

    /**
     * Number of RDF triples available on the tested SPARQL endpoint
     */
    public static int nTriples;

    /**
     * default prefixes used to perform queries with the remote {@link Global#TARGET_SPARQL_ENDPOINT
     * SPARQL endpoint}
     */
    public static String PREFIXES =
              "PREFIX :         <http://www.example.com/myDataGraph#>\n"
            + "PREFIX owl:      <http://www.w3.org/2002/07/owl#>\n"
            + "PREFIX xsd:      <http://www.w3.org/2001/XMLSchema#>\n"
            + "PREFIX rdfs:     <http://www.w3.org/2000/01/rdf-schema#>\n"
            + "PREFIX rdf:      <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
            + "PREFIX foaf:     <http://xmlns.com/foaf/0.1/>\n"
            + "PREFIX dc:       <http://purl.org/dc/elements/1.1/>\n"
            + "PREFIX dbr:      <http://dbpedia.org/resource/>\n"
            + "PREFIX dbp:      <http://dbpedia.org/property/>\n"
            + "PREFIX dbpedia:  <http://dbpedia.org/>\n"
            + "PREFIX skos:     <http://www.w3.org/2004/02/skos/core#>\n"
            + "PREFIX dbo:      <http://dbpedia.org/ontology/>\n"
            + "PREFIX ex:       <http://example.org/demo#> \n"
            + "PREFIX sp:       <http://spinrdf.org/sp#> \n"
            + "PREFIX sh:       <http://www.w3.org/ns/shacl#> \n"
            + "PREFIX dct:      <http://purl.org/dc/terms/> \n"
            + "PREFIX psh:      <http://ns.inria.fr/probabilistic-shacl/> \n";

    /**
     * Size of the SHACL Shapes ID randomly generated using BNF Grammar
     */
//    public static final int SIZE_ID_SHACL_SHAPES = 6;

    /**
     * Output path of results, it corresponding to '/rdfminer/io/' in container
     */
    public static String OUTPUT_PATH = HOME + "io/";

    /**
     * Name of output results file in JSON
     */
    public static final String RESULTS_FILENAME = "/results.json";

    /**
     * Name of SHACL Validation report (Classic; probabilistic) file using turtle (.ttl) format
     */
    public static final String SHACL_VALIDATION_REPORT_FILENAME = "/shacl_report.ttl";

    public static final String SHACL_SHAPES_FILENAME = "shapes.ttl";

    public static final String SHACL_HYPOTHESIS_TEST_FILENAME = "hypothesis_test.ttl";

    /**
     * Path to the probabilisitic STTL Template file (.rq format)
     */
    public static final String PROBABILISTIC_STTL_TEMPLATE = OUTPUT_PATH + "psh_template.rq";

    /**
     * Path to the probabilisitic STTL Template result (html)
     */
    public static final String PROBABILISTIC_STTL_RESULT_AS_HTML = "results.html";

    public static final String USERS = "users/";

    /**
     * .cache files folder path
     */
    public static String CACHE_FOLDER = "";

    public static String GRAMMAR_FILE = "";

    public static String PREFIXES_FILE = "";

    public static String SIMILARITIES_FILE = "";

    public static String AXIOMS_FILE = "";

    public static String SHAPES_FILE = "";

    /**
     * Number of threads used to perform evaluation of axioms, its value depends on
     * the processor(s) of the server which are used to deploy and use the RDF
     * Miner.
     */
    public static final int NB_THREADS = Runtime.getRuntime().availableProcessors();

}