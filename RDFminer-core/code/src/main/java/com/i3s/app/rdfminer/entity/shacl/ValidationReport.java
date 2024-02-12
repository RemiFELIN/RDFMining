package com.i3s.app.rdfminer.entity.shacl;

import com.i3s.app.rdfminer.entity.shacl.vocabulary.ProbabilisticShacl;
import com.i3s.app.rdfminer.entity.shacl.vocabulary.Shacl;
import com.i3s.app.rdfminer.sparql.RequestBuilder;
import org.apache.log4j.Logger;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.Literals;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.eclipse.rdf4j.sail.memory.model.DecimalMemLiteral;
import org.eclipse.rdf4j.sail.memory.model.NumericMemLiteral;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidationReport {

    private static final Logger logger = Logger.getLogger(ValidationReport.class.getName());

    public final String content;

    public Model model;

    public Repository db;

    public final int numSummary;

    public final List<String> reportedShapes;

    public final HashMap<String, List<String>> exceptionsByShape;

    public HashMap<String, Number> numExceptionsByShape = new HashMap<>();

    public HashMap<String, Number> numConfirmationsByShape = new HashMap<>();

//    public final HashMap<String, Number> fitnessByShape;

    public HashMap<String, Number> generalityByShape = new HashMap<>();

    public HashMap<String, Number> likelihoodByShape = new HashMap<>();

    public HashMap<String, Number> referenceCardinalityByShape = new HashMap<>();

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
//        if(parameters.getMod() == Mod.SHAPE_MINING) {
            // set a Map< str , number > which contains the numerical values by Shape
        this.numExceptionsByShape = getNumericalInValSummaryByShape(ProbabilisticShacl.NUM_VIOLATION);
        this.numConfirmationsByShape = getNumericalInValSummaryByShape(ProbabilisticShacl.NUM_CONFIRMATION);
        this.referenceCardinalityByShape = getNumericalInValSummaryByShape(ProbabilisticShacl.REFERENCE_CARDINALITY);
        this.likelihoodByShape = getNumericalInValSummaryByShape(ProbabilisticShacl.LIKELIHOOD);
        this.generalityByShape = getNumericalInValSummaryByShape(ProbabilisticShacl.GENERALITY);
//        }
//        logger.info("Validation structure:\n" + exceptionsByShape.keySet());
    }

    public HashMap<String, Number> getNumericalInValSummaryByShape(String parameter) {
        HashMap<String, Number> results = new HashMap<>();
        // connect DB
        try(RepositoryConnection con = db.getConnection()) {
            // add the model
            con.add(this.model);
            // init query
            String request = RequestBuilder.select("?shape ?value",
                    "?v " + ProbabilisticShacl.SUMMARY + " ?bn . ?bn " + ProbabilisticShacl.FOCUS_SHAPE + " ?shape ; " + parameter + " ?value", true);

            TupleQuery query = con.prepareTupleQuery(request);
            // launch and get result
            try (TupleQueryResult result = query.evaluate()) {
                // we just iterate over all solutions in the result...
                for (BindingSet solution : result) {
                    // add each result on the final list
                    if(solution.getValue("value").getClass() == DecimalMemLiteral.class || solution.getValue("value").getClass() == NumericMemLiteral.class) {
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
            String request = RequestBuilder.select("?shape ?node",
                    "?a " + ProbabilisticShacl.SUMMARY + " ?bn . ?bn " + ProbabilisticShacl.FOCUS_SHAPE + " ?shape . " +
                            "?x a " + Shacl.VALIDATION_RESULT + "; " + Shacl.SOURCE_SHAPE + " ?shape; " + Shacl.FOCUS_NODE + " ?node", true);
            TupleQuery query = con.prepareTupleQuery(request);
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
            String request = RequestBuilder.select("?shapes", "?y a " + Shacl.VALIDATION_REPORT + " . " +
                    "?y " + ProbabilisticShacl.SUMMARY + " ?x . ?x " + ProbabilisticShacl.FOCUS_SHAPE + " ?shapes .", true);
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

    public boolean hasNoNodesToAssess() {
        try(RepositoryConnection con = db.getConnection()) {
            // add the model
            con.add(this.model);
            // init query
            String request = RequestBuilder.ask("?x psh:summary ?y", true);
            // prepare query
            BooleanQuery query = con.prepareBooleanQuery(request);
            // launch and get result
            if (Objects.equals(query.evaluate(), false)) {
                return true;
            }
        } finally {
            // shutdown the DB and frees up memory space
            db.shutDown();
        }
        return false;
    }

    public int getNumSummary() {
        try(RepositoryConnection con = db.getConnection()) {
            // add the model
            con.add(this.model);
            // init request
            String request = RequestBuilder.select("(count(distinct ?x) as ?n)", "?y a " + Shacl.VALIDATION_REPORT + " ; " +
                    ProbabilisticShacl.SUMMARY + " ?x .", true);
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
        return this.prettifyPrint(this.content);
    }

    public String prettifyPrint(String validationReport) {
        return validationReport.replace(".@", ".\n@")
                .replace(".<", ".\n\n<")
                .replace(";sh", ";\nsh")
                .replace(";psh", ";\npsh")
                .replace(";r", ";\nr")
                .replace("._", ".\n\n_");
    }

    public String getContent(boolean prefix) {
        String filteredContent = this.content;
        if (!prefix) {
            // filtering content: remove all the prefixes definition
            Pattern pattern = Pattern.compile("(@prefix [a-zA-Z]*[:] <http:\\/\\/[a-zA-Z0-9\\/\\.\\#\\-]*> \\.)");
            Matcher matcher = pattern.matcher(this.content);
            while(matcher.find()) {
//                logger.info("removing prefix: " + matcher.group());
                filteredContent = filteredContent.replace(matcher.group(0), "");
            }
        }
//        logger.info("filteredContent: " + filteredContent);
        return this.prettifyPrint(filteredContent) + "\n";
    }

//    public static void main(String[] args) {
//        String report = "@prefix psh: <http://ns.inria.fr/probabilistic-shacl/> .@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .@prefix xsh: <http://www.w3.org/ns/shacl#> .@prefix sh: <http://www.w3.org/ns/shacl#> .@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .<urn:uuid:b1d44ca5-1b71-4315-b88c-42e41ce67b1f> rdf:type sh:ValidationResult ;sh:focusNode <http://ns.inria.fr/covid19/51dd0bdd5680a978bf834d5b4cbfaa8dd84dea90> ;sh:resultMessage \"Fail at: [sh:hasValue <http://www.wikidata.org/entity/Q16023751> ;\\n  sh:path rdf:type]\" ;sh:resultPath rdf:type ;sh:resultSeverity sh:Violation ;sh:sourceConstraintComponent sh:HasValueConstraintComponent ;sh:sourceShape <http://www.example.com/myDataGraph#3> ;sh:value <http://www.wikidata.org/entity/Q16023751> .<urn:uuid:1adb2307-6848-4d7f-83ae-b3e37fafea85> rdf:type sh:ValidationResult ;sh:focusNode <http://ns.inria.fr/covid19/380469cdbf2999dc4d165cbed03b008549cc93a7> ;sh:resultMessage \"Fail at: [sh:hasValue <http://www.wikidata.org/entity/Q16023751> ;\\n  sh:path rdf:type]\" ;sh:resultPath rdf:type ;sh:resultSeverity sh:Violation ;sh:sourceConstraintComponent sh:HasValueConstraintComponent ;sh:sourceShape <http://www.example.com/myDataGraph#3> ;sh:value <http://www.wikidata.org/entity/Q16023751> .<urn:uuid:ab5852d5-a9f7-47d6-9234-5de8730bb3ab> rdf:type sh:ValidationResult ;sh:focusNode <http://ns.inria.fr/covid19/237f3ce9618785cef42c210a3e679cf8d1d5f519> ;sh:resultMessage \"Fail at: [sh:hasValue <http://www.wikidata.org/entity/Q16023751> ;\\n  sh:path rdf:type]\" ;sh:resultPath rdf:type ;sh:resultSeverity sh:Violation ;sh:sourceConstraintComponent sh:HasValueConstraintComponent ;sh:sourceShape <http://www.example.com/myDataGraph#3> ;sh:value <http://www.wikidata.org/entity/Q16023751> .<urn:uuid:c765f9e7-d767-4577-aaa4-7008956fce89> rdf:type sh:ValidationResult ;sh:focusNode <http://ns.inria.fr/covid19/56e960feeb64c50c3153251668f26b3b33d4ded2> ;sh:resultMessage \"Fail at: [sh:hasValue <http://www.wikidata.org/entity/Q16023751> ;\\n  sh:path rdf:type]\" ;sh:resultPath rdf:type ;sh:resultSeverity sh:Violation ;sh:sourceConstraintComponent sh:HasValueConstraintComponent ;sh:sourceShape <http://www.example.com/myDataGraph#3> ;sh:value <http://www.wikidata.org/entity/Q16023751> .<urn:uuid:5f66d79f-fa07-43dc-a2e5-a467d0564dc7> rdf:type sh:ValidationResult ;sh:focusNode <http://ns.inria.fr/covid19/273ed1bf60b19e3db4ab3894a20598336b7e0ef4> ;sh:resultMessage \"Fail at: [sh:hasValue <http://www.wikidata.org/entity/Q16023751> ;\\n  sh:path rdf:type]\" ;sh:resultPath rdf:type ;sh:resultSeverity sh:Violation ;sh:sourceConstraintComponent sh:HasValueConstraintComponent ;sh:sourceShape <http://www.example.com/myDataGraph#3> ;sh:value <http://www.wikidata.org/entity/Q16023751> .<urn:uuid:33aef801-e35f-4399-b029-1338fd6e0948> rdf:type sh:ValidationResult ;sh:focusNode <http://ns.inria.fr/covid19/52ee7bd3f076b9f68b8fcea6687db4327d99cf71> ;sh:resultMessage \"Fail at: [sh:hasValue <http://www.wikidata.org/entity/Q16023751> ;\\n  sh:path rdf:type]\" ;sh:resultPath rdf:type ;sh:resultSeverity sh:Violation ;sh:sourceConstraintComponent sh:HasValueConstraintComponent ;sh:sourceShape <http://www.example.com/myDataGraph#3> ;sh:value <http://www.wikidata.org/entity/Q16023751> .<urn:uuid:7cb4ca56-d14d-47f6-a82d-9f05fd6f9fac> rdf:type sh:ValidationResult ;sh:focusNode <http://ns.inria.fr/covid19/70b51615329fe89b718e0df871d649b989586b05> ;sh:resultMessage \"Fail at: [sh:hasValue <http://www.wikidata.org/entity/Q16023751> ;\\n  sh:path rdf:type]\" ;sh:resultPath rdf:type ;sh:resultSeverity sh:Violation ;sh:sourceConstraintComponent sh:HasValueConstraintComponent ;sh:sourceShape <http://www.example.com/myDataGraph#3> ;sh:value <http://www.wikidata.org/entity/Q16023751> .<urn:uuid:0444e80a-1a73-435b-89b3-d32b0e3dfb4a> rdf:type sh:ValidationResult ;sh:focusNode <http://ns.inria.fr/covid19/435b2c1a312dad2b0d70591efe688612cc47fe19> ;sh:resultMessage \"Fail at: [sh:hasValue <http://www.wikidata.org/entity/Q16023751> ;\\n  sh:path rdf:type]\" ;sh:resultPath rdf:type ;sh:resultSeverity sh:Violation ;sh:sourceConstraintComponent sh:HasValueConstraintComponent ;sh:sourceShape <http://www.example.com/myDataGraph#3> ;sh:value <http://www.wikidata.org/entity/Q16023751> .<urn:uuid:12a40d2f-9662-46b6-8f5c-58f908e57268> rdf:type sh:ValidationResult ;sh:focusNode <http://ns.inria.fr/covid19/54e82ae1126dc9eed820d7a255cb4edca68a9f82> ;sh:resultMessage \"Fail at: [sh:hasValue <http://www.wikidata.org/entity/Q16023751> ;\\n  sh:path rdf:type]\" ;sh:resultPath rdf:type ;sh:resultSeverity sh:Violation ;sh:sourceConstraintComponent sh:HasValueConstraintComponent ;sh:sourceShape <http://www.example.com/myDataGraph#3> ;sh:value <http://www.wikidata.org/entity/Q16023751> .<urn:uuid:84e50a02-941f-4c37-84c9-7d053106d46a> rdf:type sh:ValidationResult ;sh:focusNode <http://ns.inria.fr/covid19/49861eebbc323ac1fda5c90a3f25b755300b7413> ;sh:resultMessage \"Fail at: [sh:hasValue <http://www.wikidata.org/entity/Q16023751> ;\\n  sh:path rdf:type]\" ;sh:resultPath rdf:type ;sh:resultSeverity sh:Violation ;sh:sourceConstraintComponent sh:HasValueConstraintComponent ;sh:sourceShape <http://www.example.com/myDataGraph#3> ;sh:value <http://www.wikidata.org/entity/Q16023751> .<urn:uuid:eb6c1fa8-3b7d-4a00-850f-86347a206589> rdf:type sh:ValidationResult ;sh:focusNode <http://ns.inria.fr/covid19/1376526470f18216200ecd6c375d9913942fa362> ;sh:resultMessage \"Fail at: [sh:hasValue <http://www.wikidata.org/entity/Q16023751> ;\\n  sh:path rdf:type]\" ;sh:resultPath rdf:type ;sh:resultSeverity sh:Violation ;sh:sourceConstraintComponent sh:HasValueConstraintComponent ;sh:sourceShape <http://www.example.com/myDataGraph#3> ;sh:value <http://www.wikidata.org/entity/Q16023751> .<urn:uuid:b06805ad-2be0-44e2-aac7-b21a198b0c8b> rdf:type sh:ValidationResult ;sh:focusNode <http://ns.inria.fr/covid19/5e6b537332182bd3c8f642accc5eae577055a4c4> ;sh:resultMessage \"Fail at: [sh:hasValue <http://www.wikidata.org/entity/Q16023751> ;\\n  sh:path rdf:type]\" ;sh:resultPath rdf:type ;sh:resultSeverity sh:Violation ;sh:sourceConstraintComponent sh:HasValueConstraintComponent ;sh:sourceShape <http://www.example.com/myDataGraph#3> ;sh:value <http://www.wikidata.org/entity/Q16023751> .<urn:uuid:5cc29ddd-4dec-4ec3-b1c8-82debc0b423f> rdf:type sh:ValidationResult ;sh:focusNode <http://ns.inria.fr/covid19/3bac8eb8788f6b0387777fd123f57f9264e59df9> ;sh:resultMessage \"Fail at: [sh:hasValue <http://www.wikidata.org/entity/Q16023751> ;\\n  sh:path rdf:type]\" ;sh:resultPath rdf:type ;sh:resultSeverity sh:Violation ;sh:sourceConstraintComponent sh:HasValueConstraintComponent ;sh:sourceShape <http://www.example.com/myDataGraph#3> ;sh:value <http://www.wikidata.org/entity/Q16023751> .<urn:uuid:5a8db7e5-b24d-40c9-b1a3-f8e32961330a> rdf:type sh:ValidationResult ;sh:focusNode <http://ns.inria.fr/covid19/0537823ee7ac420f3f6e1a1261afa425dd4cd1a6> ;sh:resultMessage \"Fail at: [sh:hasValue <http://www.wikidata.org/entity/Q16023751> ;\\n  sh:path rdf:type]\" ;sh:resultPath rdf:type ;sh:resultSeverity sh:Violation ;sh:sourceConstraintComponent sh:HasValueConstraintComponent ;sh:sourceShape <http://www.example.com/myDataGraph#3> ;sh:value <http://www.wikidata.org/entity/Q16023751> ._:bb4483 psh:summary _:bb4529 ;rdf:type sh:ValidationReport ;sh:conforms false ;sh:result <urn:uuid:5a8db7e5-b24d-40c9-b1a3-f8e32961330a> ;sh:result <urn:uuid:eb6c1fa8-3b7d-4a00-850f-86347a206589> ;sh:result <urn:uuid:ab5852d5-a9f7-47d6-9234-5de8730bb3ab> ;sh:result <urn:uuid:5f66d79f-fa07-43dc-a2e5-a467d0564dc7> ;sh:result <urn:uuid:1adb2307-6848-4d7f-83ae-b3e37fafea85> ;sh:result <urn:uuid:5cc29ddd-4dec-4ec3-b1c8-82debc0b423f> ;sh:result <urn:uuid:0444e80a-1a73-435b-89b3-d32b0e3dfb4a> ;sh:result <urn:uuid:84e50a02-941f-4c37-84c9-7d053106d46a> ;sh:result <urn:uuid:b1d44ca5-1b71-4315-b88c-42e41ce67b1f> ;sh:result <urn:uuid:33aef801-e35f-4399-b029-1338fd6e0948> ;sh:result <urn:uuid:12a40d2f-9662-46b6-8f5c-58f908e57268> ;sh:result <urn:uuid:c765f9e7-d767-4577-aaa4-7008956fce89> ;sh:result <urn:uuid:b06805ad-2be0-44e2-aac7-b21a198b0c8b> ;sh:result <urn:uuid:7cb4ca56-d14d-47f6-a82d-9f05fd6f9fac> ._:bb4529 psh:focusShape <http://www.example.com/myDataGraph#3> ;psh:generality \"0.000291745485003341\"^^xsd:decimal ;psh:likelihood \"6.767468643021722E-11\"^^xsd:double ;psh:numConfirmation 17 ;psh:numViolation 14 ;psh:referenceCardinality 31 ;rdf:type psh:ValidationSummary .";
//        // filtering content: remove all the prefixes definition
//        Pattern pattern = Pattern.compile("(@prefix [a-zA-Z]*[:] <http:\\/\\/[a-zA-Z0-9\\/\\.\\#\\-]*> \\.)");
//        Matcher matcher = pattern.matcher(report);
//        while(matcher.find()) {
//            logger.info("removing prefix: " + matcher.group());
//            report = report.replace(matcher.group(), "");
//        }
//        report = report.replace(".@", ".\n@")
//                .replace(".<", ".\n\n<")
//                .replace(";sh", ";\nsh")
//                .replace(";psh", ";\npsh")
//                .replace(";r", ";\nr")
//                .replace("._", ".\n\n_");
//        logger.info(report);
//    }

}
