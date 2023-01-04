package com.i3s.app.rdfminer.grammar.evolutionary.fitness;

import com.i3s.app.rdfminer.entity.Entity;
import com.i3s.app.rdfminer.grammar.evolutionary.individual.GEIndividual;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 *  An interface of fitness compution in GE
 *  @author Rémi FELIN
 */
public interface FitnessEvaluation {

    /**
     * Update a given population by using evaluation of each entity
     * @param population       a given population
     */
    public ArrayList<Entity> updatePopulation(ArrayList<Entity> population);

    /**
     * Update a given entity by using evaluation (possibility, fitness, ...)
     * @param entity a given entity
     * @return the evaluated entity
     */
    public Entity updateIndividual(Entity entity) throws URISyntaxException, IOException;

}
