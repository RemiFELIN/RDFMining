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

import com.i3s.app.rdfminer.sparql.SparqlEndpoint;

/**
 * @author NGUYEN Thu Huong Extraction of a sample of DBpedia
 */
public class SampleDBpediaExtraction {
//	final private static String dbpedia ="http://dbpedia.org/sparql";
	private static Logger logger = Logger.getLogger(SampleDBpediaExtraction.class.getName());
//	final private static String dbpedia ="http://134.59.130.136:8890/sparql";
	final private static String dbpedia = "http://localhost:8890/sparql";
	final private static String PREFIXES = "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
			+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"
			+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
			+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
			+ "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" + "PREFIX dc: <http://purl.org/dc/elements/1.1/>\n"
			+ "PREFIX dbr: <http://dbpedia.org/resource/>\n" + "PREFIX dbp: <http://dbpedia.org/property/>\n"
			+ "PREFIX : <http://dbpedia.org/resource/>\n" + "PREFIX dbpedia2: <http://dbpedia.org/property/>\n"
			+ "PREFIX dbpedia: <http://dbpedia.org/>\n" + "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n"
			+ "PREFIX dbo: <http://dbpedia.org/ontology/>\n";
	public static SparqlEndpoint endpoint;
//	public static SparqlEndpoint endpoint2;
	public static ArrayList<String> arrCheckedResource = new ArrayList<String>();
	private static final String FILENAME = "./SampleDBpedia2/RDFDataset_";
	// private static final String FILENAME2 =
	// "./SampleDBpedia2/RDFDatasetNTRIPLES_";
	public static int count_triples = 0; // count triples recorded at recent time
	public static int count_lines = 0; // count lines in each text file
	// queue containing expanded resources both objects and subjects of data
	public final static int totalnumbertriples = 669995366;
	public static LinkedList<String> queue = new LinkedList<String>();

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		// Configure the log4j loggers:
		PropertyConfigurator.configure("log4j.properties");

		FileOutputStream fout = new FileOutputStream(FILENAME + Integer.toString(count_lines) + ".nt", true);
		// create model containing statements
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
		System.out.print("Enter threshold propotion of sample size  compared with the whole DBpedia size(669995366): ");
		double propotion = sc.nextDouble();

		int size_sample = (int) (propotion * totalnumbertriples);
		logger.info("Size of Sample: " + size_sample);
		System.out.println("Propotion pickup: " + propotion);
		while (count_triples < size_sample) {
			m = ModelFactory.createDefaultModel();
			if (count_lines > 10000) {

				try

				{
					if (fout != null)
						fout.close();
					/*
					 * if(m != null)int m.close();
					 */
					// bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(FILENAME
					// + Integer.toString(count_lines) + ".nt"), StandardCharsets.UTF_8));
					i++;
					fout = new FileOutputStream(FILENAME + "_" + Integer.toString(i) + ".nt", true);
					count_lines = 0;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			RDFDatasetGenerator2(m, fout, propotion);

		}
		sc.close();
		System.out.println("DONE! Total number of triples: " + count_triples);
		fout.close();
	}

	static void RDFDatasetGenerator2(Model m, FileOutputStream fout, double propotion) throws IOException {
		List<Statement> stmts = new ArrayList<Statement>();
		int pickednumber_triples;
		int cur_num;
		Statement st;

		String data = queue.poll();

		// Subject

		Resource s = m.createResource(data);
		endpoint = new SparqlEndpoint(dbpedia, PREFIXES);

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

		String strcount = "?o";
		int countriples = endpoint.count(strcount, graphcount, 0);
		logger.info("online triples:" + countriples);
		pickednumber_triples = (int) (Math.sqrt(propotion) * Double.valueOf(countriples));
		if ((pickednumber_triples < 1) && (countriples != 0)) {
			pickednumber_triples = 1;
		}
		logger.info("picked number of triples:" + pickednumber_triples);

		endpoint.selectAndCopyResults(str);
		logger.info(str);
		// }
		logger.info("Querry to pick up objects");

		ResultSet result1 = endpoint.resultSet;
		// int numbertriples_onlinedbpedia_object=0;
		cur_num = 0;
		while (result1.hasNext() && cur_num < pickednumber_triples) {

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
				count_lines++;
				count_triples++;
				cur_num++;
				logger.info("the triple " + count_triples + " was recored");

			}

		}
		logger.info(cur_num + " triples relevant to  subject: " + data + " was recored");

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
		int countriples2 = endpoint.count(strcount2, graphcount2, 0);
		logger.info("online triples:" + countriples2);
		logger.info("Querry to pick up subjects");
		pickednumber_triples = (int) (Math.sqrt(propotion) * Double.valueOf(countriples2));
		if ((pickednumber_triples < 1) && (countriples2 != 0)) {
			pickednumber_triples = 1;
		}
		logger.info("Number triple pickup:" + pickednumber_triples);
		endpoint.selectAndCopyResults(str2);
		logger.info(str2);

		ResultSet result2 = endpoint.resultSet;
		// int numbertriples_onlinedbpedia_subject=0;
		cur_num = 0;
		while (result2.hasNext() && cur_num < pickednumber_triples) {

			QuerySolution querysolution = result2.next();
			String sb = querysolution.get("s").toString();
			String pr2 = querysolution.get("r").toString();
			// numbertriples_onlinedbpedia_subject++;
			if (!sb.contains(" ") && !sb.contains("@") && !arrCheckedResource.contains(sb) && (sb.contains("http://"))
					&& (sb.indexOf("http://") == 0) && !sb.contains("owl#Thing") && (!sb.contains("/-"))) {

				// arrResult_subjects.add(querysolution);

				// Subject
				Resource s2 = m.createResource(sb);
				// Property

				Property p2 = m.createProperty(pr2);
				// Object
				Resource o2 = m.createResource(data);
				// Create statement RDF
				Statement st2 = m.createStatement(s2, p2, o2);
				stmts.add(st2);
				count_lines++;
				count_triples++;
				cur_num++;

				logger.info("the triple " + count_triples + " was recored");
				queue.add(sb);
			}

		}
		logger.info(cur_num + " triples relevant to  object" + data + "was recored");

		// Write list of statements into model m

		if (stmts.size() > 0) {
			m.add(stmts);
			m.write(fout, "N-TRIPLES");
		}
		m.close();
		arrCheckedResource.add(data);
		System.out.println("First element in queue: " + queue.element());
		System.out.println("Current number of triples: " + count_triples);
	}

	static List<QuerySolution> RandomPickupTriples(double propotion, int numbertriples_onlinedbpedia,
			List<QuerySolution> arrResult)// to pickup ranmdomly triples
	{

		List<QuerySolution> pickedup_array = new ArrayList<QuerySolution>();
		int pickednumber_triples = (int) (Math.sqrt(propotion) * Double.valueOf(numbertriples_onlinedbpedia));
		if (pickednumber_triples < 1) {
			pickednumber_triples = 1;
		}

		logger.info("number triples on online dbpedia: " + Integer.toString(numbertriples_onlinedbpedia));
		logger.info("Number triples in arrResult: " + arrResult.size());
		// pick up triples randomly
		int k = 0;
		int size = arrResult.size();

		if (size > 0) {
			for (k = 0; k < pickednumber_triples; k++) {
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
				count_lines++;
				count_triples++;
			}
			logger.info("number of triples was recorded in the model: " + Integer.toString(k));
		}
		logger.info("Number triples in arrResult remaining: " + Integer.toString(arrResult.size()));
		logger.info("Number triples in pickedup_array: " + pickedup_array.size());
		return pickedup_array;
	}

}
