package com.i3s.app.rdfminer.entity.shacl;

import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.RDFMiner;
import com.i3s.app.rdfminer.grammar.evolutionary.individual.GEIndividual;
import com.i3s.app.rdfminer.entity.shacl.vocabulary.ShaclKW;
import com.i3s.app.rdfminer.sparql.RequestBuilder;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.jena.vocabulary.RDF;
import org.apache.log4j.Logger;
import org.eclipse.rdf4j.model.Model;
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
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class of SHACL Shape: see <em><a href="https://www.w3.org/TR/shacl/#constraints-section">
 *     SHACL Core</a></em> from W3C
 *
 * @author Rémi FELIN
 */
public class Shape {

    public GEIndividual individual;

    private static final Logger logger = Logger.getLogger(Shape.class);

    /**
     * The ID of the SHACL Shape
     */
    public String id;

    /**
     * The original SHACL Shape as a string
     */
    public String shape;

    /**
     * The URI of the SHACL Shape
     */
    public String uri;

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
    public List<String> targetClass;

    /**
     * The values of "sh:targetSubjectOf"
     */
    public List<String> targetSubjectOf;

    /**
     * The values of "sh:targetObjectsOf"
     */
    public List<String> targetObjectsOf;

    /**
     * Is it a nodeShape ?
     */
    public boolean isNodeShape;

    /**
     * Properties contained in "sh:property" in this shape
     */
    public HashMap<String, String> properties;

    public Number referenceCardinality;
    public Number numConfirmation;
    public Number numException;
    public Number likelihood;
    public Number generality;
    public Number fitness;
    public List<String> exceptions = new ArrayList<>();

    /**
     *
     * @param individual
     */
    public Shape(GEIndividual individual) {
        this.individual = individual;
        this.id = generateIDFromIndividual(individual);
        this.shape = this.id + individual.getPhenotype().toString();
        this.uri = getUriFromID();
        // init model
        try {
            this.model = Rio.parse(new StringReader(Global.PREFIXES + this.shape), "", RDFFormat.TURTLE);
        } catch(Exception e) {
            logger.warn("Error during the parsing of Individual: " + e.getMessage());
            System.exit(1);
        }
        // Create a new Repository. Here, we choose a database implementation
        // that simply stores everything in main memory.
        this.db = new SailRepository(new MemoryStore());
        // search if it is a sh:NodeShape
        this.isNodeShape = ask("a", ShaclKW.NODESHAPE);
        // get the targetted class(es) if it provides
        this.targetClass = getValuesFromProperty(ShaclKW.TARGETCLASS);
        // get the targetSubjectsOf if it provides
        this.targetSubjectOf = getValuesFromProperty(ShaclKW.TARGETSUBJECTSOF);
        // get the targetObjectsOf if it provides
        this.targetObjectsOf = getValuesFromProperty(ShaclKW.TARGETOBJECTSOF);
        // search if it provide a sh:property values
        this.properties = getProperties();
        // @TODO : sh:targetNode ; sh:targetObjectsOf ; sh:message ; sh:severity
    }

    public Shape(String content) {
        this.shape = content;
        this.id = getID(this.shape);
        this.uri = getUriFromID();
        // init model
        try {
            this.model = Rio.parse(new StringReader(Global.PREFIXES + this.shape), "", RDFFormat.TURTLE);
        } catch(Exception e) {
            logger.warn("Error during the parsing of Individual: " + e.getMessage());
            logger.warn("[DEBUG] Individual: " + this.shape);
        }
        // Create a new Repository. Here, we choose a database implementation
        // that simply stores everything in main memory.
        this.db = new SailRepository(new MemoryStore());
        // search if it is a sh:NodeShape
        this.isNodeShape = ask("a", ShaclKW.NODESHAPE);
        // get the targetted class(es) if it provides
        this.targetClass = getValuesFromProperty(ShaclKW.TARGETCLASS);
        // get the targetSubjectsOf if it provides
        this.targetSubjectOf = getValuesFromProperty(ShaclKW.TARGETSUBJECTSOF);
        // get the targetObjectsOf if it provides
        this.targetObjectsOf = getValuesFromProperty(ShaclKW.TARGETOBJECTSOF);
        // search if it provide a sh:property values
        this.properties = getProperties();
        // @TODO : sh:targetNode ; sh:message ; sh:severity
    }

    public Shape(String content, String id) {
//        System.out.println(content);
        this.shape = content;
        // TODO: faire mieux ! uniformiser pour les autres constructeurs
        this.id = this.uri = id;
        // init model
        try {
            this.model = Rio.parse(new StringReader(Global.PREFIXES + this.shape), "", RDFFormat.TURTLE);
        } catch(Exception e) {
            logger.warn("Error during the parsing of Individual: " + e.getMessage());
            System.exit(1);
        }
        // Create a new Repository. Here, we choose a database implementation
        // that simply stores everything in main memory.
        this.db = new SailRepository(new MemoryStore());
        // search if it is a sh:NodeShape
        this.isNodeShape = ask("a", ShaclKW.NODESHAPE);
        // get the targetted class(es) if it provides
        this.targetClass = getValuesFromProperty(ShaclKW.TARGETCLASS);
        // get the targetSubjectsOf if it provides
        this.targetSubjectOf = getValuesFromProperty(ShaclKW.TARGETSUBJECTSOF);
        // get the targetObjectsOf if it provides
        this.targetObjectsOf = getValuesFromProperty(ShaclKW.TARGETOBJECTSOF);
        // search if it provide a sh:property values
        this.properties = getProperties();
        // @TODO : sh:targetNode ; sh:message ; sh:severity
    }

    public String getUriFromID() {
        // We need to transform it before
        return "http://rdfminer.com/shapes/" + this.id.replace("<", "").replace(">", "").strip();
    }

    public String getID(String shape) {
        Pattern p = Pattern.compile("(<.*>)  a");
        Matcher m = p.matcher(shape);
        if(m.find())
            return m.group(1);
        return null;
    }

    /**
     * ASK Query to search if the given triple : ?id ?predicate ?object is true or not
     * @param predicate ?p
     * @param object ?o
     * @return true if the ASK query return a non-empty map, else false.
     */
    public boolean ask(String predicate, String object) {
        try(RepositoryConnection con = db.getConnection()) {
            // add the model
            con.add(this.model);
            // init query
            String request = RequestBuilder.ask(this.id + " " + predicate + " " + object + " .", true);
            BooleanQuery query = con.prepareBooleanQuery(request);
            // launch and get result
            if(!query.evaluate()) logger.debug(predicate + " is not provided by the shape " + this.id);
            return query.evaluate();
        } finally {
            // shutdown the DB and frees up memory space
            this.db.shutDown();
        }
    }

    /**
     * Search and return a map < K , V > where K is the property and V its value contained in sh:property
     * @return the value(s) for 'toSearch' input
     */
    public HashMap<String, String> getProperties() {
        HashMap<String, String> results = new HashMap<>();
        // <id> ?predicate ?prop exists ?
        if(!ask(ShaclKW.PROPERTY, "?x")) return null;
        // get the value(s) of predicate
        try(RepositoryConnection con = db.getConnection()) {
            // add the model
            con.add(this.model);
            // init query
            // With this request, we obtain a blank node. In it we can find all excepted results
            String request = RequestBuilder.select("?p ?o", this.id + " " + ShaclKW.PROPERTY + " ?bn . " + "?bn ?p ?o .", true);
            TupleQuery query = con.prepareTupleQuery(request);
            // launch and get result
            try (TupleQueryResult result = query.evaluate()) {
                // we just iterate over all solutions in the result...
                for (BindingSet solution : result) {
                    // add each result on the final list
                    results.put(String.valueOf(solution.getValue("p")), parse(String.valueOf(solution.getValue("o"))));
                }
            }
        } finally {
            // shutdown the DB and frees up memory space
            db.shutDown();
        }
        if(results.isEmpty())
            logger.info("No result found for: " + this.id + " " + ShaclKW.PROPERTY + " ?prop");
        return results;
    }

    /**
     * search if a such predicate exists and return the value of the given shape
     * @return the value(s) for 'toSearch' input
     */
    public List<String> getValuesFromProperty(String property) {
        List<String> results = new ArrayList<>();
        // <id> ?predicate ?prop exists ?
        if(!ask(property, "?y")) return null;
        // get the value(s) of predicate
        try(RepositoryConnection con = db.getConnection()) {
            // add the model
            con.add(this.model);
            // init query
            String request = RequestBuilder.select("?y", this.id + " " + property + " ?y .", true);// Global.PREFIXES + "SELECT ?y WHERE { " + this.id + " " + property + " ?y . }";
            // init query
            TupleQuery query = con.prepareTupleQuery(request);
            // launch and get result
            try (TupleQueryResult result = query.evaluate()) {
                // we just iterate over all solutions in the result...
                for (BindingSet solution : result) {
                    // add each result on the final list
                    results.add(String.valueOf(solution.getValue("y")));
                }
            }
        } finally {
            // shutdown the DB and frees up memory space
            db.shutDown();
        }
        if(results.isEmpty()) logger.info("No result found for: " + this.id + " " + property + " ?y");
        return results;
    }

    /**
     * Generate randomly an unique ID for a given individual
     *
     * @param individual a GEIndividual from population
     * @return a unique ID for a given individual. Example: <code>< shape#[random integer] ></code>
     */
    private String generateIDFromIndividual(GEIndividual individual) {
        // the length of the substring depends of the SHACL Shapes ID size such as :
        return "<" + String.format("%." + Global.SIZE_ID_SHACL_SHAPES + "s",
                Math.abs(individual.getPhenotype().toString().hashCode())) +
                RandomStringUtils.randomAlphabetic(4) +  "> ";
    }

    public void fillParamFromReport(ValidationReport report) {
        String parsedUri = this.uri.replace("<", "").replace(">", "");
        this.referenceCardinality = report.referenceCardinalityByShape.get(parsedUri);
        // if referenceCardinality is null, it means that no supports are found for this shape
        if(this.referenceCardinality == null) {
            this.referenceCardinality = this.numException = this.likelihood = this.generality = this.fitness = 0;
        } else {
            this.numConfirmation = report.numConfirmationsByShape.get(parsedUri);
            this.numException = report.numExceptionsByShape.get(parsedUri);
            this.likelihood = report.likelihoodByShape.get(parsedUri);
            this.generality = report.generalityByShape.get(parsedUri);
            this.fitness = computeFitness();
            if(report.exceptionsByShape.get(parsedUri) != null) {
                this.exceptions = new ArrayList<>(report.exceptionsByShape.get(parsedUri));
            }
        }
    }

    public double computeFitness() {
        return this.likelihood.doubleValue() * this.generality.doubleValue();
    }

    @Override
    public String toString() {
        return this.shape;
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        // set params
        json.put("shape", this.id); // @todo fix it
        json.put("referenceCardinality", this.referenceCardinality);
        json.put("numConfirmation", this.numConfirmation);
        json.put("numException", this.numException);
        json.put("likelihood", this.likelihood);
        json.put("generality", this.generality);
        json.put("fitness", this.fitness);
        if (this.individual != null) json.put("generation", this.individual.getAge());
        else json.put("generation", JSONObject.NULL);
        JSONArray exceptions = new JSONArray();
        if(this.exceptions.size() > 0) {
            for(String exception : this.exceptions) {
                exceptions.put("<" + exception + ">");
            }
        }
        json.put("exceptions", exceptions);
        // Probabilistic params
        json.put("p", Double.valueOf(RDFMiner.parameters.probShaclP));
        return json;
    }

    public String parse(String subject) {
        String obj = subject.replace("+", "\\+");
        // Parse literal if any (as REGEX)
        if(obj.contains("\"") && obj.contains("\"")) {
            // remove doube quote, remove all spaces and add double quote
            obj = "\"" + obj.replace("\"", "").replace("\"", "").trim() + "\"";
        }
        return obj;
    }

}
