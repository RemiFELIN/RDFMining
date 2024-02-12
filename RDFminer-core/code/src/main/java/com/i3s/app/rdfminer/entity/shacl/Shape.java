package com.i3s.app.rdfminer.entity.shacl;

import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.entity.Entity;
import com.i3s.app.rdfminer.entity.axiom.Axiom;
import com.i3s.app.rdfminer.entity.shacl.vocabulary.Shacl;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.FitnessPackage.BasicFitness;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.GEIndividual;
import com.i3s.app.rdfminer.ht.HypothesisTesting;
import com.i3s.app.rdfminer.sparql.RequestBuilder;
import com.i3s.app.rdfminer.sparql.corese.CoreseEndpoint;
import org.apache.log4j.Logger;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * A class of SHACL Shape: see <em><a href="https://www.w3.org/TR/shacl/#constraints-section">
 *     SHACL Core</a></em> from W3C
 *
 * @author Rémi FELIN
 */
public class Shape extends Entity {

    private static final Logger logger = Logger.getLogger(Shape.class);

    /**
     * The IRIs (subject) of the SHACL shape
     */
    public String relativeIri;

    public String absoluteIri;

    /**
     * The content of SHACL Shape
     */
    public String content;

    /**
     * RDF4J Model
     */
    public Model model;

    /**
     * a Repository used to store the SHACL Shape
     */
    public Repository db;

    /**
     * The values of "sh:targetClass"
     */
    public List<String> targetClasses;

    /**
     * The values of "sh:targetSubjectOf"
     */
    public List<String> targetSubjectsOf;

    /**
     * The values of "sh:targetObjectsOf"
     */
    public List<String> targetObjectsOf;

    /**
     * Properties contained in "sh:property" in this shape
     */
    public ArrayList<String> properties;

    public ValidationReport validationReport;

    /**
     *
     */
    public Shape(GEIndividual individual, CoreseEndpoint endpoint) throws URISyntaxException, IOException {
        long t0 = getProcessCPUTime();
        this.individual = individual;
        this.content = this.individual.getPhenotype().getStringNoSpace();
        // get shape uri subject
        setIdentifier(this.content);
        // init model
        try {
            this.model = Rio.parse(new StringReader(Global.PREFIXES + this.relativeIri + this.content), "", RDFFormat.TURTLE);
        } catch(Exception e) {
            logger.error("Individual as RDF turtle:\n" + individual.getPhenotype().getStringNoSpace());
            logger.warn("Error during the parsing of Individual: " + e.getMessage());
            System.exit(1);
        }
        // Create a new Repository. Here, we choose a database implementation
        // that simply stores everything in main memory.
        this.db = new SailRepository(new MemoryStore());
        // get the targetted class(es) if it provides
        this.targetClasses = getValuesFromProperty(Shacl.TARGETCLASS);
        // get the targetSubjectsOf if it provides
        this.targetSubjectsOf = getValuesFromProperty(Shacl.TARGETSUBJECTSOF);
        // get the targetObjectsOf if it provides
        this.targetObjectsOf = getValuesFromProperty(Shacl.TARGETOBJECTSOF);
        // search if it provide a sh:property values
        this.properties = getProperties();
        // @TODO : sh:targetNode ; sh:targetObjectsOf ; sh:message ; sh:severity
        // update shape: assess it !
        update(endpoint);
        elapsedTime = getProcessCPUTime() - t0;
//        logger.info("elapsed time = " + elapsedTime + " ms.");
    }

    public Shape(String content, CoreseEndpoint endpoint) throws URISyntaxException, IOException {
        long t0 = getProcessCPUTime();
        this.content = content;
        try {
            this.model = Rio.parse(new StringReader(this.content), "", RDFFormat.TURTLE);
        } catch(Exception e) {
            logger.warn("Error during the parsing of proposal SHACL shape: " + e.getMessage());
        }
        // Create a new Repository. Here, we choose a database implementation
        // that simply stores everything in main memory.
        this.db = new SailRepository(new MemoryStore());
        setIdentifierWithQuery();
        this.setEntityAsString(content);
        // get the targetted class(es) if it provides
        this.targetClasses = getValuesFromProperty(Shacl.TARGETCLASS);
        // get the targetSubjectsOf if it provides
        this.targetSubjectsOf = getValuesFromProperty(Shacl.TARGETSUBJECTSOF);
        // get the targetObjectsOf if it provides
        this.targetObjectsOf = getValuesFromProperty(Shacl.TARGETOBJECTSOF);
        // search if it provide a sh:property values
        this.properties = getProperties();
        // update shape: assess it !
        update(endpoint);
        elapsedTime = getProcessCPUTime() - t0;
//        logger.info("elapsed time = " + elapsedTime + " ms.");
    }

    public ArrayList<String> getProperties() {
        ArrayList<String> results = new ArrayList<>();
        try(RepositoryConnection con = db.getConnection()) {
            con.add(this.model);
            String request = RequestBuilder.select("?bn ?p ?o", "?x a sh:NodeShape ; " + Shacl.PROPERTY + " ?bn . ?bn ?p ?o .", true);
//            System.out.println(request);
            TupleQuery query = con.prepareTupleQuery(request);
            try (TupleQueryResult result = query.evaluate()) {
                for (BindingSet solution : result) {
                    results.add("<" + solution.getValue("bn") + "> <" +
                            solution.getValue("p") + "> <" + solution.getValue("o") + ">");
                }
            }
        } finally {
            db.shutDown();
        }
        return results;
    }

    public void setIdentifier(String content) {
        String generated = String.valueOf((content.hashCode() & 0xfffffff));
        this.relativeIri = ":" + generated;
        this.absoluteIri = "http://www.example.com/myDataGraph#" + generated;
    }

    public void setIdentifierWithQuery() {
        try(RepositoryConnection con = db.getConnection()) {
            con.add(this.model);
            String request = RequestBuilder.select("?x", "?x a sh:NodeShape .", true);
            TupleQuery query = con.prepareTupleQuery(request);
            try (TupleQueryResult result = query.evaluate()) {
                for (BindingSet solution : result) {
                    this.relativeIri = "<" + solution.getValue("x") + ">";
                    this.absoluteIri = this.relativeIri;
                }
            }
        } finally {
            db.shutDown();
        }
    }

    /**
     * search if a such predicate exists and return the value of the given shape
     * @return the value(s) for 'toSearch' input
     */
    public List<String> getValuesFromProperty(String property) {
        List<String> results = new ArrayList<>();
        try(RepositoryConnection con = db.getConnection()) {
            con.add(this.model);
            String request = RequestBuilder.select("?y", "?x a sh:NodeShape ; " + property + " ?y .", true);
            TupleQuery query = con.prepareTupleQuery(request);
            try (TupleQueryResult result = query.evaluate()) {
                for (BindingSet solution : result) {
                    results.add(String.valueOf(solution.getValue("y")));
                }
            }
        } finally {
            db.shutDown();
        }
        return results;
    }

    public void fillParamFromReport(ValidationReport report) {
        String iri = this.absoluteIri.replace("<", "").replace(">", "");
//        logger.info("Extracting informations from SHACL validation for " + iri);
//        System.out.println(fullUri);
        this.referenceCardinality = report.referenceCardinalityByShape.get(iri).intValue();
        this.numConfirmations = report.numConfirmationsByShape.get(iri).intValue();
        this.numExceptions = report.numExceptionsByShape.get(iri).intValue();
        this.likelihood = (Double) report.likelihoodByShape.get(iri);
        this.generality = (double) report.generalityByShape.get(iri);
        if(report.exceptionsByShape.get(iri) != null) {
            this.exceptions = new ArrayList<>(report.exceptionsByShape.get(iri));
        }
        if(this.individual != null) {
            this.individual.setFitness(new BasicFitness(computeFitness(), this.individual));
        }
    }

    @Override
    public String toString() {
        return this.content;
    }

    /**
     * Compute the fitness of a given shape by using referenceCardinality and
     * {@link Axiom#necessity() necessity} values.
     */
    public double computeFitness() {
        // compute a hypothesis testing
        HypothesisTesting ht = new HypothesisTesting();
        ht.eval(this);
        // if the ht gives a success
        if(this.accepted) {
            return this.numConfirmations;
        } else {
            return this.numConfirmations * (this.likelihood / ht.getMaxMassFunction(this));
        }
    }

    @Override
    public void update(CoreseEndpoint endpoint) throws URISyntaxException, IOException {
        String content = this.individual == null ? Global.PREFIXES + this : Global.PREFIXES + this.relativeIri + this;
//        logger.info(content);
        // launch evaluation
        String report = endpoint.getValidationReportFromServer(content);
        // read evaluation report
        this.validationReport = new ValidationReport(report);
        if (this.validationReport.hasNoNodesToAssess()) {
            this.referenceCardinality = 0;
            this.numConfirmations = 0;
            this.numExceptions = 0;
            this.likelihood = 0.0;
            this.generality = 0;
            if(this.individual != null) this.individual.setFitness(new BasicFitness(0, this.individual));
        } else {
            // add results
            this.fillParamFromReport(validationReport);
        }
    }
}
