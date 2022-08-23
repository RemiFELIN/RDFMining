package com.i3s.app.rdfminer.shacl;

import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.shacl.vocabulary.RDFMinerKW;
import com.i3s.app.rdfminer.shacl.vocabulary.ShaclKW;
import org.apache.log4j.Logger;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.Literals;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.eclipse.rdf4j.sail.memory.model.DecimalMemLiteral;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ValidationReport {

    private static final Logger logger = Logger.getLogger(ValidationReport.class.getName());

    public final String content;

    public Model model;

    public Repository db;

    public final int numSummary;

    public final List<String> reportedShapes;

    public final HashMap<String, List<String>> exceptionsByShape;

    public final HashMap<String, Number> numExceptionsByShape;

    public final HashMap<String, Number> numConfirmationsByShape;

    public final HashMap<String, Number> fitnessByShape;

    public final HashMap<String, Number> generalityByShape;

    public final HashMap<String, Number> probabilityByShape;

    public final HashMap<String, Number> referenceCardinalityByShape;

    public ValidationReport(String content) throws IOException {
        this.content = content;
        // init model
        this.model = Rio.parse(new StringReader(this.content), "", RDFFormat.TURTLE);
        // Create a new Repository. Here, we choose a database implementation
        // that simply stores everything in main memory.
        this.db = new SailRepository(new MemoryStore());
        // compute the number of summary
        this.numSummary = getNumSummary();
        // get all the reported shapes in a list
        this.reportedShapes = getShapes();
        // set a Map< str , List<str> > which contains a list of exceptions by Shape
        this.exceptionsByShape = getExceptionsByShape();
        // set a Map< str , number > which contains the numerical values by Shape
        this.numExceptionsByShape = getNumericalInValSummaryByShape(RDFMinerKW.NUM_EXCEPTION);
        this.numConfirmationsByShape = getNumericalInValSummaryByShape(RDFMinerKW.NUM_CONFIRMATION);
        this.referenceCardinalityByShape = getNumericalInValSummaryByShape(RDFMinerKW.REFERENCE_CARDINALITY);
        this.probabilityByShape = getNumericalInValSummaryByShape(RDFMinerKW.PROBABILITY);
        this.fitnessByShape = getNumericalInValSummaryByShape(RDFMinerKW.FITNESS);
        this.generalityByShape = getNumericalInValSummaryByShape(RDFMinerKW.GENERALITY);
    }

    public HashMap<String, Number> getNumericalInValSummaryByShape(String parameter) {
        HashMap<String, Number> results = new HashMap<>();
        // connect DB
        try(RepositoryConnection con = db.getConnection()) {
            // add the model
            con.add(this.model);
            // init query
            StringBuilder request = new StringBuilder(Global.PREFIXES + "SELECT ?shape ?value WHERE { \n" +
                    "?x " + ShaclKW.SOURCE_SHAPE + " ?shape .\n" +
                    "?x " + parameter + " ?value .\n" +
                    " VALUES ?shape { ");
            // Use the results of getShapes() in order to set values of ?shape
            for(String uri : this.reportedShapes) {
                request.append("<").append(uri).append("> ");
            }
            request.append(" } }");
//            System.out.println("SPARQL Request: " + request);
            TupleQuery query = con.prepareTupleQuery(request.toString());
            // launch and get result
            try (TupleQueryResult result = query.evaluate()) {
                // we just iterate over all solutions in the result...
                for (BindingSet solution : result) {
                    // add each result on the final list
                    if(solution.getValue("value").getClass() == DecimalMemLiteral.class) {
                        results.put(String.valueOf(solution.getValue("shape")), Literals.getDoubleValue(solution.getValue("value"), 0));
                    } else {
                        results.put(String.valueOf(solution.getValue("shape")), Literals.getIntValue(solution.getValue("value"), 0));
                    }
                }
            }
        } finally {
            // shutdown the DB and frees up memory space
            db.shutDown();
        }
        return results;
    }

    public HashMap<String, List<String>> getExceptionsByShape() {
        HashMap<String, List<String>> results = new HashMap<>();
        // connect DB
        try(RepositoryConnection con = db.getConnection()) {
            // add the model
            con.add(this.model);
            // init query
            StringBuilder request = new StringBuilder(Global.PREFIXES + "SELECT ?shape ?node WHERE { \n" +
                    "?x " + ShaclKW.SOURCE_SHAPE + " ?shape .\n" +
                    "?x " + RDFMinerKW.EXCEPTION + " ?ex .\n" +
                    "?ex " + ShaclKW.FOCUS_NODE + " ?node .\n" +
                    " VALUES ?shape { ");
            // Use the results of getShapes() in order to set values of ?shape
            for(String uri : this.reportedShapes) {
                request.append("<").append(uri).append("> ");
            }
            request.append(" } }");
            TupleQuery query = con.prepareTupleQuery(request.toString());
            // launch and get result
            try (TupleQueryResult result = query.evaluate()) {
                List<String> exceptions = new ArrayList<>();
                // we just iterate over all solutions in the result...
                for (BindingSet solution : result) {
                    String shape = String.valueOf(solution.getValue("shape"));
                    String node = String.valueOf(solution.getValue("node"));
                    // add each result on the final list
                    if(!results.containsKey(shape)) {
                        exceptions = new ArrayList<>();
                        exceptions.add(node);
                        results.put(shape, exceptions);
                    } else {
                        if(!exceptions.contains(node))
                            exceptions.add(node);
                        results.put(shape, exceptions);
                    }
                }
            }
        } finally {
            // shutdown the DB and frees up memory space
            db.shutDown();
        }
        return results;
    }

    private List<String> getShapes() {
        List<String> results = new ArrayList<>();
        // connect DB
        try(RepositoryConnection con = db.getConnection()) {
            // add the model
            con.add(this.model);
            // init query
            String request = Global.PREFIXES + "SELECT ?shapes WHERE { \n" +
                    "?y a " + ShaclKW.VALIDATION_REPORT + " .\n" +
                    "?y " + RDFMinerKW.SUMMARY + " ?x . \n" +
                    "?x " + ShaclKW.SOURCE_SHAPE + " ?shapes . }";
            TupleQuery query = con.prepareTupleQuery(request);
            // launch and get result
            try (TupleQueryResult result = query.evaluate()) {
                // we just iterate over all solutions in the result...
                for (BindingSet solution : result) {
                    // add each result on the final list
                    results.add(String.valueOf(solution.getValue("shapes")));
                }
            }
        } finally {
            // shutdown the DB and frees up memory space
            db.shutDown();
        }
        return results;
    }

    public int getNumSummary() {
        try(RepositoryConnection con = db.getConnection()) {
            // add the model
            con.add(this.model);
            // init request
            String request = Global.PREFIXES + "SELECT (count(?x) as ?n) WHERE { \n" +
                    "?y a " + ShaclKW.VALIDATION_REPORT + " .\n" +
                    "?y " + RDFMinerKW.SUMMARY + " ?x . }";
            // init query
            TupleQuery query = con.prepareTupleQuery(request);
            // launch and get result
            try (TupleQueryResult result = query.evaluate()) {
                // we just iterate over all solutions in the result...
                for (BindingSet solution : result) {
                    return Literals.getIntValue(solution.getValue("n"), 0);
                }
            }
        } finally {
            // shutdown the DB and frees up memory space
            db.shutDown();
        }
        return 0;
    }

    @Override
    public String toString() {
        return this.numSummary + " |\n" +
                this.reportedShapes + " |\n" +
                this.exceptionsByShape + " |\n" +
                this.numExceptionsByShape + " |\n" +
                this.numConfirmationsByShape + " |\n" +
                this.referenceCardinalityByShape + " |\n" +
                this.probabilityByShape + " |\n" +
                this.fitnessByShape + " |\n" +
                this.generalityByShape;
    }

    public String prettifyPrint() {
        return this.content.replace(".@", ".\n@")
                .replace(".<", ".\n\n<")
                .replace(";sh", ";\nsh")
                .replace(";r", ";\nr")
                .replace("._", ".\n\n_");
    }

//    public static void main(String[] args) throws IOException {
//        Shape shape = new Shape("<http://rdfminer.com/shapes/2>", "<http://rdfminer.com/shapes/2> a sh:NodeShape ; sh:targetClass <http://dbpedia.org/ontology/Species> ;" +
//                " sh:property [ sh:path rdf:type ; sh:hasValue <http://dbpedia.org/ontology/Eukaryote> ; ] .");
//        ValidationReport report = new ValidationReport("@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n" +
//                "@prefix xsh: <http://www.w3.org/ns/shacl#> .\n" +
//                "@prefix sh: <http://www.w3.org/ns/shacl#> .\n" +
//                "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" +
//                "@prefix rdfminer: <http://ns.inria.fr/rdfminer/shacl#> .\n" +
//                "\n" +
//                "\n" +
//                "<urn:uuid:eaa748ac-7c4e-4df7-9a49-5b263042128d> rdf:type sh:ValidationResult ;\n" +
//                "sh:focusNode <http://rdfminer.com/data/UnvalidData> ;\n" +
//                "sh:resultMessage \"Fail at: [sh:hasValue <http://dbpedia.org/ontology/Species> ;\\n  sh:path rdf:type]\" ;\n" +
//                "sh:resultPath rdf:type ;\n" +
//                "sh:resultSeverity sh:Violation ;\n" +
//                "sh:sourceConstraintComponent sh:HasValueConstraintComponent ;\n" +
//                "sh:sourceShape _:b518 ;\n" +
//                "sh:value <http://dbpedia.org/ontology/Species> .\n" +
//                "\n" +
//                "<urn:uuid:53b1c6c6-c507-4591-a505-12ff89546715> rdf:type sh:ValidationResult ;\n" +
//                "sh:focusNode <http://rdfminer.com/data/UnvalidDataShape21> ;\n" +
//                "sh:resultMessage \"Fail at: [sh:hasValue <http://dbpedia.org/ontology/Eukaryote> ;\\n  sh:path rdf:type]\" ;\n" +
//                "sh:resultPath rdf:type ;\n" +
//                "sh:resultSeverity sh:Violation ;\n" +
//                "sh:sourceConstraintComponent sh:HasValueConstraintComponent ;\n" +
//                "sh:sourceShape _:b519 ;\n" +
//                "sh:value <http://dbpedia.org/ontology/Eukaryote> .\n" +
//                "\n" +
//                "<urn:uuid:d1c149fc-de62-454a-b85c-533513a074d6> rdf:type sh:ValidationResult ;\n" +
//                "sh:focusNode <http://rdfminer.com/data/UnvalidDataShape2> ;\n" +
//                "sh:resultMessage \"Fail at: [sh:hasValue <http://dbpedia.org/ontology/Eukaryote> ;\\n  sh:path rdf:type]\" ;\n" +
//                "sh:resultPath rdf:type ;\n" +
//                "sh:resultSeverity sh:Violation ;\n" +
//                "sh:sourceConstraintComponent sh:HasValueConstraintComponent ;\n" +
//                "sh:sourceShape _:b519 ;\n" +
//                "sh:value <http://dbpedia.org/ontology/Eukaryote> .\n" +
//                "\n" +
//                "<urn:uuid:b4cf670c-2b04-4a6f-afad-d40febf7300a> rdf:type sh:ValidationResult ;\n" +
//                "sh:focusNode <http://rdfminer.com/data/Tata> ;\n" +
//                "sh:resultMessage \"Fail at: [sh:hasValue <http://dbpedia.org/ontology/Species> ;\\n  sh:path rdf:type]\" ;\n" +
//                "sh:resultPath rdf:type ;\n" +
//                "sh:resultSeverity sh:Violation ;\n" +
//                "sh:sourceConstraintComponent sh:HasValueConstraintComponent ;\n" +
//                "sh:sourceShape _:b518 ;\n" +
//                "sh:value <http://dbpedia.org/ontology/Species> .\n" +
//                "\n" +
//                "<urn:uuid:a9f0de22-2982-4819-ad61-2726a5b7f999> rdf:type sh:ValidationResult ;\n" +
//                "sh:focusNode <http://rdfminer.com/data/AnotherValidData> ;\n" +
//                "sh:resultMessage \"Fail at: [sh:hasValue <http://dbpedia.org/ontology/Eukaryote> ;\\n  sh:path rdf:type]\" ;\n" +
//                "sh:resultPath rdf:type ;\n" +
//                "sh:resultSeverity sh:Violation ;\n" +
//                "sh:sourceConstraintComponent sh:HasValueConstraintComponent ;\n" +
//                "sh:sourceShape _:b519 ;\n" +
//                "sh:value <http://dbpedia.org/ontology/Eukaryote> .\n" +
//                "\n" +
//                "_:bb0 rdfminer:summary _:bb9 ;\n" +
//                "rdfminer:summary _:bb16 ;\n" +
//                "rdf:type sh:ValidationReport ;\n" +
//                "sh:conforms false ;\n" +
//                "sh:result <urn:uuid:a9f0de22-2982-4819-ad61-2726a5b7f999> ;\n" +
//                "sh:result <urn:uuid:53b1c6c6-c507-4591-a505-12ff89546715> ;\n" +
//                "sh:result <urn:uuid:d1c149fc-de62-454a-b85c-533513a074d6> ;\n" +
//                "sh:result <urn:uuid:b4cf670c-2b04-4a6f-afad-d40febf7300a> ;\n" +
//                "sh:result <urn:uuid:eaa748ac-7c4e-4df7-9a49-5b263042128d> .\n" +
//                "\n" +
//                "_:bb16 rdfminer:exception <urn:uuid:b4cf670c-2b04-4a6f-afad-d40febf7300a> ;\n" +
//                "rdfminer:exception <urn:uuid:eaa748ac-7c4e-4df7-9a49-5b263042128d> ;\n" +
//                "rdfminer:fitness \"0.25\"^^xsd:decimal ;\n" +
//                "rdfminer:generality \"0.5\"^^xsd:decimal ;\n" +
//                "rdfminer:numConfirmation 2 ;\n" +
//                "rdfminer:numException 2 ;\n" +
//                "rdfminer:probability \"0.5\"^^xsd:decimal ;\n" +
//                "rdfminer:referenceCardinality 4 ;\n" +
//                "rdf:type rdfminer:ValidationSummary ;\n" +
//                "sh:sourceShape <http://rdfminer.com/shapes/EukaryoteScoSpeciesShape> .\n" +
//                "\n" +
//                "_:bb9 rdfminer:exception <urn:uuid:a9f0de22-2982-4819-ad61-2726a5b7f999> ;\n" +
//                "rdfminer:exception <urn:uuid:53b1c6c6-c507-4591-a505-12ff89546715> ;\n" +
//                "rdfminer:exception <urn:uuid:d1c149fc-de62-454a-b85c-533513a074d6> ;\n" +
//                "rdfminer:fitness \"0.2500\"^^xsd:decimal ;\n" +
//                "rdfminer:generality \"0.625\"^^xsd:decimal ;\n" +
//                "rdfminer:numConfirmation 2 ;\n" +
//                "rdfminer:numException 3 ;\n" +
//                "rdfminer:probability \"0.4\"^^xsd:decimal ;\n" +
//                "rdfminer:referenceCardinality 5 ;\n" +
//                "rdf:type rdfminer:ValidationSummary ;\n" +
//                "sh:sourceShape <http://rdfminer.com/shapes/2> .");
//        System.out.println(shape.toJSON(report));
//    }
}
