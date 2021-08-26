package com.i3s.app.rdfminer.grammar.evolutionary;

import org.topbraid.spin.vocabulary.SP;

import com.i3s.app.rdfminer.RDFMiner;
import com.i3s.app.rdfminer.sparql.SparqlEndpoint;

import java.util.ArrayList;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.shared.Lock;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

/**
 * Compute the cost of graph pattern. (it's only a plan but is not used in WCCI
 * and ICCS )
 * 
 * @author NGUYEN Thu Huong
 *
 */

public class CostGP {
	final private static String PREFIXES = "PREFIX ex:    <http://example.org/demo#> \n"
			+ "PREFIX rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
			+ "PREFIX rdfs:  <http://www.w3.org/2000/01/rdf-schema#> \n" + "PREFIX sp:    <http://spinrdf.org/sp#> \n";

	int count_UNION_Operators;
	int count_FILTER_Operators;
	int count_Triples;
	int count_Variables;
	int count_Instances_predicates;
	static SparqlEndpoint endpoint_spin;
	String gp;
	Model model;

	CostGP(String gp) {
		this.gp = gp;
		String query = "Select ?x where { \n" + gp + "}";
		model = TranslateToSpin(query);
		endpoint_spin = new SparqlEndpoint(model, PREFIXES);
		this.count_UNION_Operators = count_UNION_Operator() * 2; // UNION is more expensive so multiple with 2
		this.count_FILTER_Operators = count_FILTER_Operator();
		this.count_Triples = count_Triples();
		this.count_Variables = count_Variables();
		// this.count_Instances_predicates=
		// count_Instances_Predicates(endpoint_spin,model);

	}

	static int count_FILTER_Operator() {
		int count = 0;
		String GP = "(count(DISTINCT ?t) AS ?n)  where {\n " + "?t a sp:Filter. \n" + " }";
		ResultSet rs = endpoint_spin.select(GP, 0);
//		ResultSet rs = endpoint_spin.getResultSet();
		if (rs.hasNext()) {
			count = rs.next().get("n").asLiteral().getInt();
		}

		return count;
	}

	static int count_Triples() {
		int count = 0;
		String GP = "(count(DISTINCT ?t) AS ?n)  where {\n " + "?t sp:subject ?s. \n" + " }";
		ResultSet rs = endpoint_spin.select(GP, 0);
//		ResultSet rs = endpoint_spin.getResultSet();
		if (rs.hasNext()) {
			count = rs.next().get("n").asLiteral().getInt();
		}

		return count;
	}

	static int count_UNION_Operator() {
		int count = 0;
		String GP = "(count(DISTINCT ?t) AS ?n)  where {\n " + "?t a sp:Union. \n" + " }";
		ResultSet rs = endpoint_spin.select(GP, 0);
//		ResultSet rs = endpoint_spin.getResultSet();
		if (rs.hasNext()) {
			count = rs.next().get("n").asLiteral().getInt();
		}

		return count;
	}

	static int count_Triples_FILTER_Operator() {
		int count = 0;
		String GP = "?x (count(?t) as ?c) where {\n" + "?x a sp:notExists ;\n"
				+ "sp:elements/rdf:rest*/rdf:first ?t. \n" + "?t sp:subject ?s. \n" + "}\n" + "group by ?x";
		ResultSet rs = endpoint_spin.select(GP, 0);
//		ResultSet rs = endpoint_spin.getResultSet();
		while (rs.hasNext()) {
			QuerySolution r = rs.next();
			System.out.println(r.toString());
			count = count + r.get("c").asLiteral().getInt();
		}

		return count;
	}

	static int count_Triples_UNION_Operator() {
		int count = 0;
		String GP = "?x (count(?t) as ?c) where {\n" + "?x a sp:Union ;\n" + "sp:elements/rdf:rest*/rdf:first ?exp.\n"
				+ "?exp rdf:rest*/rdf:first ?t.\n" + "?t sp:subject ?s.\n" + "}\n" + "group by ?x";
		ResultSet rs = endpoint_spin.select(GP, 0);
//		ResultSet rs = endpoint_spin.getResultSet();
		while (rs.hasNext()) {
			QuerySolution r = rs.next();
			// System.out.println(r.toString());
			count = count + r.get("c").asLiteral().getInt();
		}

		return count;
	}

	static int count_Variables() {
		int count = 0;
		String GP = "(count(distinct ?v) as ?c)  where {\n" + "?t sp:object ?o.\n" + "?o sp:varName ?v.\n" + "}\n";
		ResultSet rs = endpoint_spin.select(GP, 0);
//		ResultSet rs = endpoint_spin.getResultSet();
		while (rs.hasNext()) {
			QuerySolution r = rs.next();
			// System.out.println(r.toString());
			count = count + r.get("c").asLiteral().getInt();
		}

		return count;
	}

	static ArrayList<String> list_predicates(SparqlEndpoint endpoint_spin, Model model) {

		String GP = "distinct ?p where {\n" + "?t sp:predicate ?p.\n" + "}\n";
		ArrayList<String> list = new ArrayList<String>();
		ResultSet rs = endpoint_spin.select(GP, 0);
//		ResultSet rs = endpoint_spin.getResultSet();
		while (rs.hasNext()) {
			QuerySolution r = rs.next();
			// System.out.println("list predicates " + r.get("p").toString());
			list.add(r.get("p").toString());

		}

		return list;
	}

	int count_Instances_Predicates(SparqlEndpoint endpoint, Model model) {
		ArrayList<String> list_predicate = list_predicates(endpoint, model);
		int cost = 0;
		for (int index = 0; index < list_predicate.size(); index++) {
			int j = 0;
			// System.out.println(list_predicate.get(index));
			while (j < RDFMiner.Predicate_Table.length) {
				// System.out.println(RDFMiner.Predicate_Table[j][0]);

				if (list_predicate.get(index).equals(RDFMiner.Predicate_Table[j][0])) {
					cost = cost + Integer.parseInt(RDFMiner.Predicate_Table[j][1]);
					// System.out.println("found!! " +
					// Integer.parseInt(RDFMiner.Predicate_Table[j][1]));
				}
				j++;
			}
			// System.out.println("cost= " + cost);

		}
		return cost;
	}

	static Model TranslateToSpin(String GraphPatern) {
		Model model = ModelFactory.createDefaultModel();
		model.setNsPrefix("rdf", RDF.getURI());
		model.setNsPrefix("ex", "http://example.org/demo#");
		model.setNsPrefix("sp", SP.getURI());
		model.setNsPrefix("rdfs", RDFS.getURI());
		// Query arqQuery = (Query) ARQFactory.get().createQuery(model, GraphPatern);
		model.enterCriticalSection(Lock.READ);
		// ARQ2SPIN arq2SPIN = new ARQ2SPIN(model);
		// Select select1 = (Select) arq2SPIN.createQuery(arqQuery, null);
		// select1.addProperty(RDFS.comment, "Comment1"); // <-- as part of rdf

		/*
		 * Resource anon = model.createResource(); anon.addProperty(RDF.type,
		 * SP.Select); anon.addProperty(SP.text, model.createTypedLiteral(
		 * "# Comment2\n" + // <-- as part of string "SELECT ?person\n" + "WHERE {\n" +
		 * "    ?person a ex:Person .\n" + "    ?person ex:age ?age .\n" +
		 * "    FILTER (?age < 22) .\n" + "}")); Select selectAndCopyResults = anon.as(Select.class);
		 * System.out.println("========================");
		 */

		// System.out.println("Select1:\n" + select1);
		// System.out.println("========================");
		// model.write(System.out, "ttl");
		return model;
	}

	int getCount_Triples() {
		return this.count_Triples;
	}

	int getCount_UNION_Operators() {
		return this.count_UNION_Operators;
	}

	int getCount_FILTER_Operators() {
		return this.count_FILTER_Operators;
	}

	int getCount_Variables() {
		return this.count_Variables;
	}

	int getCount_Instances_Predicates() {
		return this.count_Instances_predicates;
	}
}
