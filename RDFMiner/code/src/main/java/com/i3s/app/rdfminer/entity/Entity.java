package com.i3s.app.rdfminer.entity;

import com.i3s.app.rdfminer.entity.axiom.Axiom;
import com.i3s.app.rdfminer.entity.axiom.type.DisjointClassesAxiom;
import com.i3s.app.rdfminer.grammar.evolutionary.individual.GEIndividual;

import java.util.ArrayList;
import java.util.List;

/**
 * This class describes an abstract entity that can be designed by RDFMiner.
 * It contains all the parameters that a future object must integrate to be mine and/or assessed.
 * @author Rémi FELIN
 */
public abstract class Entity {

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
     * The current ID of generation where this axiom has been found
     */
    public int generation;

}
