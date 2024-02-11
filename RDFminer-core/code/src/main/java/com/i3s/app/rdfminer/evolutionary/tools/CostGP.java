//package com.i3s.app.rdfminer.evolutionary.tools;
//
//import org.topbraid.spin.vocabulary.SP;
//
//import com.i3s.app.rdfminer.RDFminer;
//import com.i3s.app.rdfminer.sparql.virtuoso.VirtuosoEndpoint;
//
//import java.util.ArrayList;
//
//import org.apache.jena.query.*;
//import org.apache.jena.rdf.model.Model;
//import org.apache.jena.rdf.model.ModelFactory;
//import org.apache.jena.shared.Lock;
//import org.apache.jena.vocabulary.RDF;
//import org.apache.jena.vocabulary.RDFS;
//
///**
// * Compute the cost of graph pattern. (it's only a plan but is not used in WCCI
// * and ICCS )
// *
// * @author NGUYEN Thu Huong
// *
// */
//
//public class CostGP {
//
//	final private static String PREFIXES = "PREFIX ex:    <http://example.org/demo#> \n"
//			+ "PREFIX rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
//			+ "PREFIX rdfs:  <http://www.w3.org/2000/01/rdf-schema#> \n" + "PREFIX sp:    <http://spinrdf.org/sp#> \n";
//
//	int countUnionOperators;
//	int countFilterOperators;
//	int countTriples;
//	int countVariables;
//	int countInstancesPredicates;
//	static VirtuosoEndpoint endpointSpin;
//	String gp;
//	Model model;
//
//	CostGP(String gp) {
//		this.gp = gp;
//		String query = "Select ?x where { \n" + gp + "}";
//		model = TranslateToSpin(query);
//		endpointSpin = new VirtuosoEndpoint(model, PREFIXES);
//		this.countUnionOperators = countUnionOperator() * 2; // UNION is more expensive so multiple with 2
//		this.countFilterOperators = countFilterOperator();
//		this.countTriples = countTriples();
//		this.countVariables = countVariables();
//	}
//
//	static int countFilterOperator() {
//		int count = 0;
//		String GP = "(count(DISTINCT ?t) AS ?n)  where {\n " + "?t a sp:Filter. \n" + " }";
//		ResultSet rs = endpointSpin.select(GP, 0);
//		if (rs.hasNext()) {
//			count = rs.next().get("n").asLiteral().getInt();
//		}
//		return count;
//	}
//
//	static int countTriples() {
//		int count = 0;
//		String GP = "(count(DISTINCT ?t) AS ?n)  where {\n " + "?t sp:subject ?s. \n" + " }";
//		ResultSet rs = endpointSpin.select(GP, 0);
//		if (rs.hasNext()) {
//			count = rs.next().get("n").asLiteral().getInt();
//		}
//		return count;
//	}
//
//	static int countUnionOperator() {
//		int count = 0;
//		String GP = "(count(DISTINCT ?t) AS ?n)  where {\n " + "?t a sp:Union. \n" + " }";
//		ResultSet rs = endpointSpin.select(GP, 0);
//		if (rs.hasNext()) {
//			count = rs.next().get("n").asLiteral().getInt();
//		}
//		return count;
//	}
//
//	static int countTriplesFilterOperator() {
//		int count = 0;
//		String GP = "?x (count(?t) as ?c) where {\n" + "?x a sp:notExists ;\n"
//				+ "sp:elements/rdf:rest*/rdf:first ?t. \n" + "?t sp:subject ?s. \n" + "}\n" + "group by ?x";
//		ResultSet rs = endpointSpin.select(GP, 0);
//		while (rs.hasNext()) {
//			QuerySolution r = rs.next();
//			System.out.println(r.toString());
//			count = count + r.get("c").asLiteral().getInt();
//		}
//		return count;
//	}
//
//	static int countTriplesUnionOperator() {
//		int count = 0;
//		String GP = "?x (count(?t) as ?c) where {\n" + "?x a sp:Union ;\n" + "sp:elements/rdf:rest*/rdf:first ?exp.\n"
//				+ "?exp rdf:rest*/rdf:first ?t.\n" + "?t sp:subject ?s.\n" + "}\n" + "group by ?x";
//		ResultSet rs = endpointSpin.select(GP, 0);
//		while (rs.hasNext()) {
//			QuerySolution r = rs.next();
//			count = count + r.get("c").asLiteral().getInt();
//		}
//		return count;
//	}
//
//	static int countVariables() {
//		int count = 0;
//		String GP = "(count(distinct ?v) as ?c)  where {\n" + "?t sp:object ?o.\n" + "?o sp:varName ?v.\n" + "}\n";
//		ResultSet rs = endpointSpin.select(GP, 0);
//		while (rs.hasNext()) {
//			QuerySolution r = rs.next();
//			count = count + r.get("c").asLiteral().getInt();
//		}
//		return count;
//	}
//
//	static ArrayList<String> getPredicates(VirtuosoEndpoint endpoint_spin, Model model) {
//
//		String GP = "distinct ?p where {\n" + "?t sp:predicate ?p.\n" + "}\n";
//		ArrayList<String> list = new ArrayList<String>();
//		ResultSet rs = endpoint_spin.select(GP, 0);
//		while (rs.hasNext()) {
//			QuerySolution r = rs.next();
//			list.add(r.get("p").toString());
//
//		}
//
//		return list;
//	}
//
//	int countInstancesPredicates(VirtuosoEndpoint endpoint, Model model) {
//		ArrayList<String> predicates = getPredicates(endpoint, model);
//		int cost = 0;
//		for (int index = 0; index < predicates.size(); index++) {
//			int j = 0;
//			while (j < RDFminer.predicateTable.length) {
//				if (predicates.get(index).equals(RDFminer.predicateTable[j][0])) {
//					cost = cost + Integer.parseInt(RDFminer.predicateTable[j][1]);
//				}
//				j++;
//			}
//		}
//		return cost;
//	}
//
//	static Model TranslateToSpin(String GraphPatern) {
//		Model model = ModelFactory.createDefaultModel();
//		model.setNsPrefix("rdf", RDF.getURI());
//		model.setNsPrefix("ex", "http://example.org/demo#");
//		model.setNsPrefix("sp", SP.getURI());
//		model.setNsPrefix("rdfs", RDFS.getURI());
//		model.enterCriticalSection(Lock.READ);
//		return model;
//	}
//
//	int getCountTriples() {
//		return this.countTriples;
//	}
//
//	int getCountUnionOperators() {
//		return this.countUnionOperators;
//	}
//
//	int getCountFilterOperators() {
//		return this.countFilterOperators;
//	}
//
//	int getCountVariables() {
//		return this.countVariables;
//	}
//
//	int getCountInstancesPredicates() {
//		return this.countInstancesPredicates;
//	}
//}
