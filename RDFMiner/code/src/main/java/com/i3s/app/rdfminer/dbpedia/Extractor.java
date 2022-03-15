package com.i3s.app.rdfminer.dbpedia;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
//import org.apache.log4j.Logger;
import org.apache.log4j.*;

import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.sparql.virtuoso.VirtuosoEndpoint;

/**
 * @author NGUYEN Thu Huong Extraction of a sample of DBpedia
 */
public class SampleDBpediaExtraction {

	private static Logger logger = Logger.getLogger(SampleDBpediaExtraction.class.getName());

	final private static String PREFIXES = "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
			+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"
			+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
			+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
			+ "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" + "PREFIX dc: <http://purl.org/dc/elements/1.1/>\n"
			+ "PREFIX dbr: <http://dbpedia.org/resource/>\n" + "PREFIX dbp: <http://dbpedia.org/property/>\n"
			+ "PREFIX : <http://dbpedia.org/resource/>\n" + "PREFIX dbpedia2: <http://dbpedia.org/property/>\n"
			+ "PREFIX dbpedia: <http://dbpedia.org/>\n" + "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n"
			+ "PREFIX dbo: <http://dbpedia.org/ontology/>\n";

	public static ArrayList<String> arrCheckedResource = new ArrayList<String>();

	private static final String FILENAME = "./SampleDBpedia2/RDFDataset_";

	public static int countTriples = 0; // count triples recorded at recent time

	public static int countLines = 0; // count lines in each text file

	// queue containing expanded resources both objects and subjects of data
	public final static int totalNumberTriples = 669995366;

	public static LinkedList<String> queue = new LinkedList<String>();

	public static void main(String[] args) throws IOException {
		// Configure the log4j loggers:
		PropertyConfigurator.configure("log4j.properties");

		FileOutputStream fout = new FileOutputStream(FILENAME + Integer.toString(countLines) + ".nt", true);
		// create model containing stacountTriplestements
		Model m;
		// User input
		Scanner sc = new Scanner(System.in);
		// Initialize the first point of resource for extracting sample DBpedia
		System.out.print("Initial resource to extract: ");
		String data = sc.nextLine();
		// String data = "http://dbpedia.org/ontology/Place";
		// Save it in the queue
		int i = 1;
		queue.add(data);
		System.out.print("Enter threshold propotion of sample size  compared with the whole DBpedia size("
				+ totalNumberTriples + "): ");
		double propotion = sc.nextDouble();

		int size_sample = (int) (propotion * totalNumberTriples);
		logger.info("Size of sample: " + size_sample);
		System.out.println("Propotion pickup: " + propotion);
		while (countTriples < size_sample) {
			m = ModelFactory.createDefaultModel();
			if (countLines > 10000) {
				try {
					if (fout != null)
						fout.close();
					i++;
					fout = new FileOutputStream(FILENAME + "_" + Integer.toString(i) + ".nt", true);
					countLines = 0;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			RDFDatasetGenerator2(m, fout, propotion);
		}
		sc.close();
		System.out.println("Done ! Total number of triples: " + countTriples);
		fout.close();
	}

	static void RDFDatasetGenerator2(Model m, FileOutputStream fout, double propotion) throws IOException {
		List<Statement> stmts = new ArrayList<>();
		int pickedNumberTriples;
		int curNum;
		Statement st;
		String data = queue.poll();
		// Subject
		Resource s = m.createResource(data);
		VirtuosoEndpoint endpoint = new VirtuosoEndpoint(Global.VIRTUOSO_REMOTE_SPARQL_ENDPOINT, PREFIXES);
		// Set List Objects of res
		String str = " distinct ?r ?o where {{<" + data + "> ?r ?o. FILTER isIRI(?o). FILTER NOT EXISTS {<" + data
				+ "> ?r ?o. FILTER(contains(str(?o), \"^\") || contains(str(?o), \"/-\") || contains(str(?o), \",\") || contains(str(?o), \";\") || contains(str(?o), \"!\") || contains(str(?o), \"|\")|| contains(str(?o), \"dbtax\") || contains(str(?r) , \"http://purl.org\") ||contains(str(?r) , \"http://dbpedia.org/ontology/wikiPage\") || contains(str(?r) , \"http://xmlns.com/foaf/0.1/primaryTopic\") || contains(str(?r) , \"http://dbpedia.org/property/wikiPageUsesTemplate\") || contains(str(?o) , \"http://dbpedia.org/class/yago/\") || contains(str(?r) , \"http://dbpedia.org/ontology/abstract\") || contains(str(?r) , \"#comment\") || contains(str(?r) , \"label\") || contains(str(?r) , \"#sameAs\") || contains(str(?r) , \"http://xmlns.com/foaf/0.1/name\") || contains(str(?r) , \"thumbnail\") || contains(str(?r) , \"#wasDerivedFrom\") || contains(str(?r) , \"isPrimaryTopicOf\") || contains(str(?r) , \"depiction\") || contains(str(?r) , \"describedby\") || contains(str(?o) , \"wikidata\") || contains(str(?r) , \"isDefinedBy\") || contains(str(?r) , \"describes\")) } } \n"
				+ "UNION \n" + "{<" + data + "> ?r ?o.  FILTER langMatches( lang(?o), 'en').  FILTER NOT EXISTS {<"
				+ data
				+ "> ?r ?o. FILTER(contains(str(?o), \"^\") || contains(str(?o), \",\") || contains(str(?o), \";\") || contains(str(?o), \"!\") || contains(str(?o), \"/-\")  || contains(str(?o), \"|\") || contains(str(?o), \"dbtax\") || contains(str(?r) , \"http://purl.org\") || contains(str(?r) , \"http://dbpedia.org/ontology/wikiPage\") || contains(str(?r) , \"http://dbpedia.org/property/wikiPageUsesTemplate\") || contains(str(?r) , \"http://xmlns.com/foaf/0.1/primaryTopic\") || contains(str(?r) , \"http://dbpedia.org/ontology/abstract\") || contains(str(?r) , \"#comment\") || contains(str(?r) , \"label\") || contains(str(?r) , \"#sameAs\") || contains(str(?r) , \"http://xmlns.com/foaf/0.1/name\") || contains(str(?r) , \"thumbnail\") || contains(str(?r) , \"#wasDerivedFrom\") || contains(str(?r) , \"isPrimaryTopicOf\") || contains(str(?r) , \"depiction\") || contains(str(?r) , \"describedby\") || contains(str(?o) , \"wikidata\") || contains(str(?r) , \"isDefinedBy\") || contains(str(?r) , \"describes\")).   }}} order by rand()";

		String graphcount = "{<" + data + "> ?r ?o. FILTER isIRI(?o). FILTER NOT EXISTS {<" + data
				+ "> ?r ?o. FILTER(contains(str(?o), \"^\") || contains(str(?o), \"/-\") || contains(str(?o), \",\") || contains(str(?o), \";\") || contains(str(?o), \"!\") || contains(str(?o), \"|\")|| contains(str(?o), \"dbtax\") || contains(str(?r) , \"http://purl.org\") || contains(str(?r) , \"http://dbpedia.org/ontology/wikiPage\") || contains(str(?r) , \"http://xmlns.com/foaf/0.1/primaryTopic\") || contains(str(?r) , \"http://dbpedia.org/property/wikiPageUsesTemplate\") || contains(str(?o) , \"http://dbpedia.org/class/yago/\") || contains(str(?r) , \"http://dbpedia.org/ontology/abstract\") || contains(str(?r) , \"#comment\") || contains(str(?r) , \"label\") || contains(str(?r) , \"#sameAs\") || contains(str(?r) , \"http://xmlns.com/foaf/0.1/name\") || contains(str(?r) , \"thumbnail\") || contains(str(?r) , \"#wasDerivedFrom\") || contains(str(?r) , \"isPrimaryTopicOf\") || contains(str(?r) , \"depiction\") || contains(str(?r) , \"describedby\") || contains(str(?o) , \"wikidata\") || contains(str(?r) , \"isDefinedBy\") || contains(str(?r) , \"describes\")) } } \n"
				+ "UNION \n" + "{<" + data + "> ?r ?o.  FILTER langMatches( lang(?o), 'en').  FILTER NOT EXISTS {<"
				+ data
				+ "> ?r ?o. FILTER(contains(str(?o), \"^\") || contains(str(?o), \",\") || contains(str(?o), \";\") || contains(str(?o), \"!\") || contains(str(?o), \"/-\") || contains(str(?o), \"|\") || contains(str(?o), \"dbtax\") || contains(str(?r) , \"http://purl.org\") || contains(str(?r) , \"http://dbpedia.org/ontology/wikiPage\") || contains(str(?r) , \"http://dbpedia.org/property/wikiPageUsesTemplate\") || contains(str(?r) , \"http://xmlns.com/foaf/0.1/primaryTopic\") || contains(str(?r) , \"http://dbpedia.org/ontology/abstract\") || contains(str(?r) , \"#comment\") || contains(str(?r) , \"label\") || contains(str(?r) , \"#sameAs\") || contains(str(?r) , \"http://xmlns.com/foaf/0.1/name\") || contains(str(?r) , \"thumbnail\") || contains(str(?r) , \"#wasDerivedFrom\") || contains(str(?r) , \"isPrimaryTopicOf\") || contains(str(?r) , \"depiction\") || contains(str(?r) , \"describedby\") || contains(str(?o) , \"wikidata\") || contains(str(?r) , \"isDefinedBy\") || contains(str(?r) , \"describes\")).   }}";
		// String graphcount="<" + data + "> ?r ?o";
		String strCount = "?o";
		int countTriples1 = endpoint.count(strCount, graphcount, 0);
		logger.info("online triples:" + countTriples1);
		pickedNumberTriples = (int) (Math.sqrt(propotion) * (double) countTriples1);

		if ((pickedNumberTriples < 1) && (countTriples1 != 0)) {
			pickedNumberTriples = 1;
		}

		logger.info("picked number of triples:" + pickedNumberTriples);
		ResultSet result1 = endpoint.select(str, 0);
		logger.info(str);
		logger.info("Querry to pick up objects");

//		ResultSet result1 = endpoint.resultSet;
		// int numbertriples_onlinedbpedia_object=0;
		curNum = 0;

		while (result1.hasNext() && curNum < pickedNumberTriples) {

			QuerySolution querysolution1 = result1.next();
			String ob = querysolution1.get("o").toString();
			String pr = querysolution1.get("r").toString();
			Property p = m.createProperty(pr);
			// numbertriples_onlinedbpedia_object++;
			if (!arrCheckedResource.contains(ob) && !queue.contains(ob) && !ob.contains("owl#Thing")
					&& !ob.contains("/-")) {

				// arrResult_objects.add(querysolution1);
				if ((ob.contains("http://")) && (ob.indexOf("http://") == 0)) {
					Resource o = m.createResource(ob);
					st = m.createStatement(s, p, o);
					if (!ob.contains(" ") && !ob.contains("@") && !ob.contains("/-") && !queue.contains(ob)
							&& !ob.contains("owl#Thing") && ob.contains("dbpedia.org"))
						queue.add(ob);
				} else {
					Literal o = m.createLiteral(ob);
					st = m.createStatement(s, p, o);
				}
				stmts.add(st);
				countLines++;
				countTriples++;
				curNum++;
				logger.info("the triple " + countTriples + " was recored");
			}

		}
		logger.info(curNum + " triples relevant to  subject: " + data + " was recored");

		// logger.info("number triples on online dbpedia: " +
		// Integer.toString(numbertriples_onlinedbpedia_object));
		logger.info("********************************************************");

		// Set List Subjects of res
		String str2 = "distinct ?s ?r where {{?s ?r <" + data + ">. FILTER isIRI(?s). FILTER NOT EXISTS {?s ?r <" + data
				+ ">. FILTER(contains(str(?s), \"^\") || contains(str(?s), \"/-\") || contains(str(?s), \",\") || contains(str(?s), \";\") || contains(str(?s), \"!\") || contains(str(?s) , \"http://dbpedia.org/class/yago/\") || contains(str(?r) , \"http://purl.org\") ||contains(str(?r) , \"http://dbpedia.org/ontology/wikiPage\") || contains(str(?r) , \"http://xmlns.com/foaf/0.1/primaryTopic\") || contains(str(?s), \"dbtax\")|| contains(str(?r) , \"http://dbpedia.org/property/wikiPageUsesTemplate\") || contains(str(?r) , \"http://dbpedia.org/ontology/abstract\") || contains(str(?r) , \"#comment\") || contains(str(?r) , \"label\") || contains(str(?r) , \"#sameAs\") || contains(str(?r) , \"http://xmlns.com/foaf/0.1/name\") || contains(str(?r) , \"thumbnail\") || contains(str(?r) , \"#wasDerivedFrom\") || contains(str(?r) , \"isPrimaryTopicOf\") || contains(str(?r) , \"depiction\") || contains(str(?r) , \"describedby\")|| contains(str(?s) , \"wikidata\") || contains(str(?r) , \"isDefinedBy\") || contains(str(?r) , \"describes\")) } } \n"
				+ "UNION \n" + "{ ?s ?r <" + data
				+ ">.  FILTER langMatches( lang(?s), 'en').  FILTER NOT EXISTS {?s ?r <" + data
				+ ">. FILTER(contains(str(?s), \"^\") || contains(str(?s), \"/-\") || contains(str(?s), \",\") || contains(str(?s), \";\") || contains(str(?s), \"!\") || contains(str(?s), \"|\") || contains(str(?r) , \"http://purl.org\") || contains(str(?r) , \"http://dbpedia.org/ontology/wikiPage\") || contains(str(?s), \"dbtax\") || contains(str(?r) , \"http://xmlns.com/foaf/0.1/primaryTopic\") || contains(str(?r) , \"http://dbpedia.org/property/wikiPageUsesTemplate\") || contains(str(?r) , \"http://dbpedia.org/ontology/abstract\") || contains(str(?r) , \"#comment\") || contains(str(?r) , \"label\") || contains(str(?r) , \"#sameAs\") || contains(str(?r) , \"http://xmlns.com/foaf/0.1/name\") || contains(str(?r) , \"thumbnail\") || contains(str(?r) , \"#wasDerivedFrom\") || contains(str(?r) , \"isPrimaryTopicOf\") || contains(str(?r) , \"depiction\") || contains(str(?r) , \"describedby\") || contains(str(?s) , \"wikidata\") || contains(str(?r) , \"isDefinedBy\") || contains(str(?r) , \"describes\")).}}} order by rand()";

		String strcount2 = "?s";
		String graphcount2 = "{?s ?r <" + data + ">. FILTER isIRI(?s). FILTER NOT EXISTS {?s ?r <" + data
				+ ">. FILTER(contains(str(?s), \"^\") || contains(str(?s), \"/-\") || contains(str(?s), \",\") || contains(str(?s), \";\") || contains(str(?s), \"!\") || contains(str(?s) , \"http://dbpedia.org/class/yago/\") || contains(str(?r) , \"http://purl.org\") || contains(str(?r) , \"http://dbpedia.org/ontology/wikiPage\") || contains(str(?r) , \"http://xmlns.com/foaf/0.1/primaryTopic\") || contains(str(?s), \"dbtax\")|| contains(str(?r) , \"http://dbpedia.org/property/wikiPageUsesTemplate\") || contains(str(?r) , \"http://dbpedia.org/ontology/abstract\") || contains(str(?r) , \"#comment\") || contains(str(?r) , \"label\") || contains(str(?r) , \"#sameAs\") || contains(str(?r) , \"http://xmlns.com/foaf/0.1/name\") || contains(str(?r) , \"thumbnail\") || contains(str(?r) , \"#wasDerivedFrom\") || contains(str(?r) , \"isPrimaryTopicOf\") || contains(str(?r) , \"depiction\") || contains(str(?r) , \"describedby\")|| contains(str(?s) , \"wikidata\") || contains(str(?r) , \"isDefinedBy\") || contains(str(?r) , \"describes\")) } } \n"
				+ "UNION \n" + "{ ?s ?r <" + data
				+ ">.  FILTER langMatches( lang(?s), 'en').  FILTER NOT EXISTS {?s ?r <" + data
				+ ">. FILTER(contains(str(?s), \"^\") || contains(str(?s), \",\") || contains(str(?s), \";\") || contains(str(?s), \"!\") || contains(str(?s), \"/-\") || contains(str(?s), \"|\") || contains(str(?r) , \"http://purl.org\") || contains(str(?r) , \"http://dbpedia.org/ontology/wikiPage\") || contains(str(?s), \"dbtax\") || contains(str(?r) , \"http://xmlns.com/foaf/0.1/primaryTopic\") || contains(str(?r) , \"http://dbpedia.org/property/wikiPageUsesTemplate\") || contains(str(?r) , \"http://dbpedia.org/ontology/abstract\") || contains(str(?r) , \"#comment\") || contains(str(?r) , \"label\") || contains(str(?r) , \"#sameAs\") || contains(str(?r) , \"http://xmlns.com/foaf/0.1/name\") || contains(str(?r) , \"thumbnail\") || contains(str(?r) , \"#wasDerivedFrom\") || contains(str(?r) , \"isPrimaryTopicOf\") || contains(str(?r) , \"depiction\") || contains(str(?r) , \"describedby\") || contains(str(?s) , \"wikidata\") || contains(str(?r) , \"isDefinedBy\") || contains(str(?r) , \"describes\")).}}";
		// String graphcount2= "?s ?r <" + data +">." ;

		logger.info(graphcount2);
		int countTriples2 = endpoint.count(strcount2, graphcount2, 0);
		logger.info("online triples:" + countTriples2);
		logger.info("Querry to pick up subjects");
		pickedNumberTriples = (int) (Math.sqrt(propotion) * (double) countTriples2);

		if ((pickedNumberTriples < 1) && (countTriples2 != 0)) {
			pickedNumberTriples = 1;
		}

		logger.info("Number triple pickup:" + pickedNumberTriples);
		logger.info(str2);
		ResultSet result2 = endpoint.select(str2, 0);
		// int numbertriples_onlinedbpedia_subject=0;
		curNum = 0;

		while (result2.hasNext() && curNum < pickedNumberTriples) {
			QuerySolution querysolution = result2.next();
			String sb = querysolution.get("s").toString();
			String pr2 = querysolution.get("r").toString();
			// numbertriples_onlinedbpedia_subject++;
			if (!sb.contains(" ") && !sb.contains("@") && !arrCheckedResource.contains(sb) && (sb.contains("http://"))
					&& (sb.indexOf("http://") == 0) && !sb.contains("owl#Thing") && (!sb.contains("/-"))) {
				// Subject
				Resource s2 = m.createResource(sb);
				// Property
				Property p2 = m.createProperty(pr2);
				// Object
				Resource o2 = m.createResource(data);
				// Create statement RDF
				Statement st2 = m.createStatement(s2, p2, o2);
				stmts.add(st2);
				countLines++;
				countTriples++;
				curNum++;
				logger.info("the triple " + countTriples + " was recored");
				queue.add(sb);
			}

		}
		logger.info(curNum + " triples relevant to  object" + data + "was recored");

		// Write list of statements into model m

		if (stmts.size() > 0) {
			m.add(stmts);
			m.write(fout, "N-TRIPLES");
		}
		m.close();
		arrCheckedResource.add(data);
		System.out.println("First element in queue: " + queue.element());
		System.out.println("Current number of triples: " + countTriples);
	}

	/**
	 * to pickup ranmdomly triples
	 * 
	 * @param propotion
	 * @param numberTripleOnlineDBPedia
	 * @param arrResult
	 * @return
	 */
	static List<QuerySolution> RandomPickupTriples(double propotion, int numberTripleOnlineDBPedia,
			List<QuerySolution> arrResult) {

		List<QuerySolution> pickedup_array = new ArrayList<QuerySolution>();
		int pickedNumberTriples = (int) (Math.sqrt(propotion) * Double.valueOf(numberTripleOnlineDBPedia));
		if (pickedNumberTriples < 1) {
			pickedNumberTriples = 1;
		}

		logger.info("number triples on online dbpedia: " + Integer.toString(numberTripleOnlineDBPedia));
		logger.info("Number triples in arrResult: " + arrResult.size());
		// pick up triples randomly
		int k = 0;
		int size = arrResult.size();

		if (size > 0) {
			for (k = 0; k < pickedNumberTriples; k++) {
				Random rand = new Random();
				int randnumber = -1;
				while (randnumber < 0) {
					randnumber = rand.nextInt(size);
					System.out.print("random number:" + randnumber);
				}

				QuerySolution randomElement = arrResult.get(randnumber);
				System.out.println(randomElement.toString());
				pickedup_array.add(randomElement);
				arrResult.remove(randomElement);
				size--;
				countLines++;
				countTriples++;
			}
			logger.info("number of triples was recorded in the model: " + Integer.toString(k));
		}
		logger.info("Number triples in arrResult remaining: " + Integer.toString(arrResult.size()));
		logger.info("Number triples in pickedup_array: " + pickedup_array.size());
		return pickedup_array;
	}

}
