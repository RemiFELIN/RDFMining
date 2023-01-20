package com.i3s.app.rdfminer.entity;

import Mapper.Symbol;
import com.i3s.app.rdfminer.entity.axiom.Axiom;
import com.i3s.app.rdfminer.entity.axiom.type.DisjointClassesAxiom;
import com.i3s.app.rdfminer.fuzzy.TruthDegree;
import com.i3s.app.rdfminer.evolutionary.individual.GEIndividual;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * This class describes an abstract entity that can be designed by RDFMiner.
 * It contains all the parameters that a future object must integrate to be mine and/or assessed.
 * @author Rémi FELIN
 */
public abstract class Entity {

    public String entityAsString;

    /**
     * A service native method to query for CPU usage.
     * <p>The name and implementation of this method are adapted from
     * <a href="http://www.javaworld.com/article/2077361/learn-java/profiling-cpu-usage-from-within-a-java-application.html">this
     * 2002 blog post</a>.</p>
     * <p>The implementation in C language of this native method is contained in the two source files
     * <code>rdfminer_axiom_Axiom.h</code> and <code>rdfminer_axiom_Axiom.c</code>.</p>
     *
     * @return the number of milliseconds of CPU time used by the current process so far
     */
    public static native long getProcessCPUTime();

    /**
     * The cardinality of the universe of discourse for this entity.
     * <p>
     * The <em><a href="http://en.wikipedia.org/wiki/Domain_of_discourse">domain of
     * discourse</a></em>, also called the <em>universe of discourse</em> (or simply
     * <em>universe</em>), is the set of entities over which certain variables of
     * interest in some formal treatment may range.
     * </p>
     * <p>
     * Here, the universe of discourse is the set of all objects or properties which
     * are relevant for testing whether this entity is possible or necessary or not.
     * <p>
     */
    public List<List<Symbol>> argumentClasses;

    /**
     * The individual build with GEVA, corresponding to the current axiom
     */
    public GEIndividual individual;

    /**
     * The generality of the axiom, its value is defined when we cannot compute the
     * {@link Axiom#necessity() necessity} of the current axiom (for instance, in a
     * case of a {@link DisjointClassesAxiom})
     */
    public double generality = 0;

    /**
     * The fitness of the axiom correspond to its value evaluated by a function
     */
    public double fitness = 0.0;

    /**
     * In the GE applied with Novelty Search context, it contains the similarity values
     * in relation to the others individuals in a given population.
     */
    public ArrayList<Double> similarities = new ArrayList<>();

    /**
     * The reference cardinality will count all the instances involved by the
     * current axiom
     */
    public int referenceCardinality = 0;

    /**
     * the time it took to test the axiom, in ms.
     */
    public long elapsedTime = 0L;

    /**
     * A list of facts in the RDF store that explicitly corroborate the axiom.
     * <p>
     * For the time being, facts are represented as strings. This is the most
     * general and flexible representation if we are just interested in reporting
     * them to a human user.However, the possibility cannot be ruled out that, in
     * future, a different representation will have to be adopted, e.g., a record of
     * a SPARQL query result.
     * </p>
     */
    public List<String> confirmations = null;

    /**
     * A list of facts in the RDF store that explicitly contradict the axiom.
     * <p>
     * For the time being, facts are represented as strings. This is the most
     * general and flexible representation if we are just interested in reporting
     * them to a human user.However, the possibility cannot be ruled out that, in
     * future, a different representation will have to be adopted, e.g., a record of
     * a SPARQL query result.
     * </p>
     */
    public List<String> exceptions = null;

    /**
     * The number of facts in the RDF store that explicitly support/confirm the
     * entity.
     */
    public int numConfirmations = 0;

    /**
     * The number of facts in the RDF store that explicitly contradict the entity.
     */
    public int numExceptions = 0;

    /**
     * Computes the possibility degree of the entity, based on the known facts.
     *
     * @return the possibility degree of the entity.
     */
    public TruthDegree possibility() {
        double y = 1.0;
        if (referenceCardinality > 0) {
            double x = ((double) referenceCardinality - (double) numExceptions) / ((double) referenceCardinality);
            y = 1.0 - Math.sqrt(1.0 - x * x);
        }
        return new TruthDegree(y);
    }

    /**
     * Computes the necessity degree of the entity, based on the known facts.
     */
    public TruthDegree necessity() {
        double y = 0.0;

        if (referenceCardinality > 0 && numExceptions == 0) {
            double x = ((double) referenceCardinality - (double) numConfirmations) / ((double) referenceCardinality);
            y = Math.sqrt(1.0 - x * x);
        }
        return new TruthDegree(y);
    }

    /**
     * Specify if the exception query reached the timeout given by "-t" parameter
     */
    public boolean isTimeout = false;


    /**
     * The ARI "Acceptance/Rejection Index" of an entity is computed as follow :
     * ARI = {@link Entity#possibility() possibility} + {@link Entity#necessity() necessity} - 1
     */
    public double ari = 0.0;

    /**
     * The current ID of generation where this axiom has been found
     */
    public Integer generation = null;

    public void setEntityAsString(String entityAsString) {
        this.entityAsString = entityAsString;
    }

    public void setEntityAsString() {
        this.entityAsString = individual.getPhenotype().getStringNoSpace();
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("entity", entityAsString);
        json.put("referenceCardinality", referenceCardinality);
        json.put("numConfirmations", numConfirmations);
        json.put("numExceptions", numExceptions);
        json.put("possibility", possibility().doubleValue());
        json.put("necessity", necessity().doubleValue());
        json.put("elapsedTime", elapsedTime);
        json.put("isTimeOut", isTimeout);
        json.put("exceptions", new JSONArray(exceptions));
        json.put("confirmations", new JSONArray(confirmations));
        json.put("generation", generation);
        json.put("fitness", fitness);
        json.put("generality", generality);
        json.put("ari", ari);
//        if(individual != null)
//            json.put("isMapped", individual.isMapped());
//        else
//            json.put("isMapped", JSONObject.NULL);
        return json;
    }

}