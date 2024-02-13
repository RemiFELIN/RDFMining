package com.i3s.app.rdfminer.evolutionary.fitness;

import com.i3s.app.rdfminer.entity.Entity;
import com.i3s.app.rdfminer.evolutionary.fitness.entity.AxiomFitnessEvaluation;
import com.i3s.app.rdfminer.evolutionary.fitness.entity.ShapeFitnessEvaluation;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.GEIndividual;
import com.i3s.app.rdfminer.generator.Generator;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class Fitness {

    private static final Logger logger = Logger.getLogger(Fitness.class.getName());

    public static ArrayList<Entity> initializePopulation(ArrayList<GEIndividual> individuals, Generator generator) {
        if(generator.generateAxioms) {
            AxiomFitnessEvaluation fit = new AxiomFitnessEvaluation();
            return fit.initializePopulation(individuals);
        } else if(generator.generateShapes) {
            ShapeFitnessEvaluation fit = new ShapeFitnessEvaluation();
            return fit.initializePopulation(individuals);
        } else {
            logger.warn("Cannot initialize population !");
            System.exit(1);
        }
        return null;
    }

    public static ArrayList<Entity> computePopulation(ArrayList<Entity> population, Generator generator) {
        if(generator.generateAxioms) {
            AxiomFitnessEvaluation fit = new AxiomFitnessEvaluation();
            return fit.updatePopulation(population);
        } else if(generator.generateShapes) {
            ShapeFitnessEvaluation fit = new ShapeFitnessEvaluation();
            return fit.updatePopulation(population);
        } else {
            logger.warn("Cannot compute fitness !");
            System.exit(1);
        }
        return null;
    }

    public static Entity computeEntity(GEIndividual individual, Generator generator) throws URISyntaxException, IOException {
        logger.debug("eval. fitness of individual: " + individual.getPhenotype().getStringNoSpace());
        if(generator.generateAxioms) {
            AxiomFitnessEvaluation fit = new AxiomFitnessEvaluation();
            return fit.updateIndividual(individual);
        } else if(generator.generateShapes) {
            ShapeFitnessEvaluation fit = new ShapeFitnessEvaluation();
            return fit.updateIndividual(individual);
        } else {
            logger.warn("Cannot compute fitness !");
            System.exit(1);
        }
        return null;
    }

}
