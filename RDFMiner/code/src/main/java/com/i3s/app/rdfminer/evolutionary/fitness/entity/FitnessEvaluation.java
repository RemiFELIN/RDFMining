package com.i3s.app.rdfminer.evolutionary.fitness.entity;

import com.i3s.app.rdfminer.entity.Entity;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.GEIndividual;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 *  An interface of fitness compution in GE
 *  @author Rémi FELIN
 */
public interface FitnessEvaluation {

    /**
     * initialize a given population by using evaluation of each individual
     * @param individuals	a given list of individuals
     * @return 				a list of assessed entities
     */
    public ArrayList<Entity> initializePopulation(ArrayList<GEIndividual> individuals);

    /**
     * Update a given population by using evaluation of each entity
     * @param population       a given population
     */
    public ArrayList<Entity> updatePopulation(ArrayList<Entity> population);

    /**
     * Update a given entity by using evaluation (possibility, fitness, ...)
     * @param individual a given individual
     * @return the evaluated entity
     */
    public Entity updateIndividual(GEIndividual individual) throws URISyntaxException, IOException;

}
