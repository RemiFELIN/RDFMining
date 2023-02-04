package com.i3s.app.rdfminer.entity.shacl;

import com.i3s.app.rdfminer.entity.shacl.vocabulary.ProbabilisticShacl;
import com.i3s.app.rdfminer.entity.shacl.vocabulary.Shacl;
import com.i3s.app.rdfminer.sparql.RequestBuilder;
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
import org.eclipse.rdf4j.sail.memory.model.NumericMemLiteral;

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

    public final HashMap<String, Number> numViolationsByShape;

    public final HashMap<String, Number> numConfirmationsByShape;

//    public final HashMap<String, Number> fitnessByShape;

    public final HashMap<String, Number> generalityByShape;

    public final HashMap<String, Number> likelihoodByShape;

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
        this.numViolationsByShape = getNumericalInValSummaryByShape(ProbabilisticShacl.NUM_EXCEPTION);
        this.numConfirmationsByShape = getNumericalInValSummaryByShape(ProbabilisticShacl.NUM_CONFIRMATION);
        this.referenceCardinalityByShape = getNumericalInValSummaryByShape(ProbabilisticShacl.REFERENCE_CARDINALITY);
        this.likelihoodByShape = getNumericalInValSummaryByShape(ProbabilisticShacl.LIKELIHOOD);
        this.generalityByShape = getNumericalInValSummaryByShape(ProbabilisticShacl.GENERALITY);
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
        return this.numSummary + " |\n" +
                this.reportedShapes + " |\n" +
                this.exceptionsByShape + " |\n" +
                this.numViolationsByShape + " |\n" +
                this.numConfirmationsByShape + " |\n" +
                this.referenceCardinalityByShape + " |\n" +
                this.likelihoodByShape + " |\n" +
                this.generalityByShape;
    }

    public String prettifyPrint() {
        return this.content.replace(".@", ".\n@")
                .replace(".<", ".\n\n<")
                .replace(";sh", ";\nsh")
                .replace(";r", ";\nr")
                .replace("._", ".\n\n_");
    }

}
