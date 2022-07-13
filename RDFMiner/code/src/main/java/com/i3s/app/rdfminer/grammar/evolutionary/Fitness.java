package com.i3s.app.rdfminer.grammar.evolutionary;

import com.i3s.app.rdfminer.grammar.evolutionary.individual.GEIndividual;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * An abstract class of fitness compution in GE
 * @author Rémi FELIN
 */
public abstract class Fitness {

    /**
     * Update a given population by using evaluation of each individual and fill data
     *
     * @param population       a given population
     * @param url 			   URL of the SPARQL endpoint
     * @param prefixes		   prefixes used for each query send to the SPARQL endpoint
     * @param content          the list of elements (SHACL Shapes ; OWL 2 Axioms) in JSON format (used to return results).
     */
    public abstract ArrayList<GEIndividual> updatePopulation(ArrayList<GEIndividual> population, String url, String prefixes, List<JSONObject> content);

    /**
     * Update a given individual by using evaluation (possibility, fitness, ...)
     * @param indivi a given individual
     * @return the evaluated individual
     */
    public abstract GEIndividual updateIndividual(GEIndividual indivi) throws URISyntaxException, IOException;

    /**
     * Display a given population, fill axioms list (as a set of JSON Object)
     *
     * @param population a given population
     * @param axioms     the list of axioms in JSON format (used to return results)
     * @param generation the current generation
     */
    public abstract void display(ArrayList<GEIndividual> population, List<JSONObject> axioms, int generation);

}
