package com.i3s.app.rdfminer.entity.shacl;

import Individuals.Phenotype;
import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.entity.Entity;
import com.i3s.app.rdfminer.entity.axiom.Axiom;
import com.i3s.app.rdfminer.entity.shacl.vocabulary.Shacl;
import com.i3s.app.rdfminer.evolutionary.individual.GEIndividual;
import com.i3s.app.rdfminer.sparql.RequestBuilder;
import org.apache.commons.lang3.RandomStringUtils;
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

import java.io.StringReader;
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
     * The URI of the SHACL Shape
     */
    public String uri;

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

//    public Number referenceCardinality;
//    public Number numConfirmation;
//    public Number numException;
//    public Number generality;
//    public Number fitness;
//    public List<String> exceptions = new ArrayList<>();

    /**
     *
     */
    public Shape(GEIndividual individual) {
        this.individual = individual;
        this.content = individual.getPhenotype().getStringNoSpace();
        // get shape uri subject
        this.uri = getShapeUri(this.content);
        // init model
        try {
            this.model = Rio.parse(new StringReader(Global.PREFIXES + this.uri + this.content), "", RDFFormat.TURTLE);
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
//        System.out.println(this.properties);
        // @TODO : sh:targetNode ; sh:targetObjectsOf ; sh:message ; sh:severity
    }

    public Shape(String content) {
        this.content = content;
        try {
            this.model = Rio.parse(new StringReader(this.content), "", RDFFormat.TURTLE);
        } catch(Exception e) {
            logger.warn("Error during the parsing of proposal SHACL shape: " + e.getMessage());
        }
        // Create a new Repository. Here, we choose a database implementation
        // that simply stores everything in main memory.
        this.db = new SailRepository(new MemoryStore());
//        System.out.println("uri: " + this.uri);
        // get the targetted class(es) if it provides
        this.targetClasses = getValuesFromProperty(Shacl.TARGETCLASS);
//        System.out.println("targetClasses: " + this.targetClasses);
        // get the targetSubjectsOf if it provides
        this.targetSubjectsOf = getValuesFromProperty(Shacl.TARGETSUBJECTSOF);
        // get the targetObjectsOf if it provides
        this.targetObjectsOf = getValuesFromProperty(Shacl.TARGETOBJECTSOF);
        // search if it provide a sh:property values
        this.properties = getProperties();
//        System.out.println("properties: " + this.properties);
//        System.out.println(this);
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

    public String getShapeUri(String content) {
        // return a random shape uri as subject
        return ":" + (content.hashCode() & 0xfffffff);
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

//    /**
//     * Generate randomly an unique ID for a given individual.
//     * @param individual a GEIndividual from population
//     * @return a unique ID for a given individual. Example: <code>< shape#[random integer] ></code>
//     */
//    private String generateIDFromIndividual(GEIndividual individual) {
//        // the length of the substring depends of the SHACL Shapes ID size such as :
//        return "<" + String.format("%." + Global.SIZE_ID_SHACL_SHAPES + "s",
//                Math.abs(individual.getPhenotype().toString().hashCode())) +
//                RandomStringUtils.randomAlphabetic(4) +  "> ";
//    }

    public void fillParamFromReport(ValidationReport report) {
        String fullUri = this.uri.replace(":", "http://www.example.com/myDataGraph#");
//        System.out.println(fullUri);
        this.referenceCardinality = report.referenceCardinalityByShape.get(fullUri).intValue();
        this.numConfirmations = report.numConfirmationsByShape.get(fullUri).intValue();
        this.numExceptions = report.numExceptionsByShape.get(fullUri).intValue();
        this.likelihood = report.likelihoodByShape.get(fullUri);
//        this.generality = report.generalityByShape.get(parsedUri);
        this.fitness = computeFitness();
        if(report.exceptionsByShape.get(fullUri) != null) {
            this.exceptions = new ArrayList<>(report.exceptionsByShape.get(fullUri));
        }
    }

    @Override
    public String toString() {
        return this.uri + this.content;
    }

    /**
     * Compute the fitness of a given shape by using referenceCardinality and
     * {@link Axiom#necessity() necessity} values.
     */
    public double computeFitness() {
        return this.numConfirmations * this.likelihood.doubleValue();
    }

//    public static void main(String[] args) {
//        String s = " a sh:NodeShape ; sh:targetClass <http://www.wikidata.org/entity/Q14875321>, <http://www.wikidata.org/entity/Q348> ; sh:property [" +
//                " sh:path rdf:type ; sh:hasValue <http://www.wikidata.org/entity/Q14863991>; ] .";
//        Shape shape = new Shape(s);
//        System.out.println(shape);
//    }

}
