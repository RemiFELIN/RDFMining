package com.i3s.app.rdfminer.evolutionary.mining;

import com.i3s.app.rdfminer.RDFMiner;
import com.i3s.app.rdfminer.entity.Entity;
import com.i3s.app.rdfminer.generator.Generator;
import com.i3s.app.rdfminer.evolutionary.tools.EATools;
import com.i3s.app.rdfminer.evolutionary.fitness.Fitness;
import com.i3s.app.rdfminer.evolutionary.individual.GEIndividual;
import com.i3s.app.rdfminer.evolutionary.selection.EliteSelection;
import com.i3s.app.rdfminer.output.GenerationJSON;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class EntityMining {

    private static final Logger logger = Logger.getLogger(EntityMining.class.getName());

    public static ArrayList<Entity> run(Generator generator, ArrayList<Entity> entities,
                                        int curGeneration, int curCheckpoint)
            throws ExecutionException, InterruptedException {
        // set size selection
        int sizeSelection = (int) (RDFMiner.parameters.sizeSelection * RDFMiner.parameters.populationSize);
        int sizeElite = RDFMiner.parameters.sizeElite * RDFMiner.parameters.populationSize < 1 ?
                1 : (int) (RDFMiner.parameters.sizeElite * RDFMiner.parameters.populationSize);

        // Checkpoint reached, this is a code to evaluate and save axioms in output file
        if ((RDFMiner.parameters.checkpoint != 0 || RDFMiner.parameters.checkpoint != curCheckpoint) && RDFMiner.parameters.populationSize * curGeneration == RDFMiner.parameters.kBase * curCheckpoint) {
            logger.info("Checkpoint n°" + (curCheckpoint + 1) + " reached !");
            // evaluate distinct genotype and avoid additional useless computation
            ArrayList<Entity> newPopulation = Fitness.computePopulation(EATools.getDistinctGenotypePopulation(entities), generator);
            // stats
            setStats(newPopulation, curGeneration);
            // fill content in json output file
//            for(Entity entity : newPopulation) {
//                entity.setEntityAsString();
//                RDFMiner.content.add(entity.toJSON());
//            }
            // return final pop
            return newPopulation;
        } else if (RDFMiner.parameters.populationSize * curGeneration == RDFMiner.parameters.kBase) {
            // evaluate distinct genotype and avoid additional useless computation
            ArrayList<Entity> newPopulation = Fitness.computePopulation(EATools.getDistinctGenotypePopulation(entities), generator);
            // stats
            setStats(newPopulation, curGeneration);
            // fill content in json output file
            for(Entity entity : newPopulation) {
                entity.setEntityAsString();
                RDFMiner.content.add(entity.toJSON());
            }
            // return final pop
            return newPopulation;
        }

        ArrayList<Entity> distinctEntities = EATools.getDistinctGenotypePopulation(entities);
        // STEP 3 - SELECTION OPERATION - Reproduce Selection - Parent Selection
//        ArrayList<GEIndividual> entitiesAsIndividuals = new ArrayList<>();
        ArrayList<GEIndividual> distinctEntitiesAsIndividuals = new ArrayList<>();
        ArrayList<GEIndividual> selectedIndividuals;
        ArrayList<GEIndividual> elitismIndividuals = new ArrayList<>();

        // Use list of individuals instead of list of entities
        // i.e. apply GE process directly on individuals
//        for(Entity entity : entities) {
//            entitiesAsIndividuals.add(entity.individual);
//        }
        for(Entity entity : distinctEntities) {
            distinctEntitiesAsIndividuals.add(entity.individual);
        }

        if (RDFMiner.parameters.elitism == 1) {
            // Elitism method, which copies the best chromosome( or a few best
            // chromosome) to new population. The rest done classical way. it
            // prevents losing the best found solution
            logger.info("Selecting elite individuals...");
            logger.info("Selecting " + (int) (RDFMiner.parameters.sizeElite * 100)
                    + "% elite individuals for the new population");
            EliteSelection elite = new EliteSelection(sizeElite);
            elite.setParentsSelectionElitism(distinctEntitiesAsIndividuals);
            selectedIndividuals = elite.setupSelectedPopulation(distinctEntitiesAsIndividuals);
            logger.info("Size of the selected population: " + selectedIndividuals.size());
            elitismIndividuals = elite.getElitedPopulation();
            logger.info("Size of the elitism population: " + elitismIndividuals.size());
        } else {
            selectedIndividuals = distinctEntitiesAsIndividuals;
            sizeElite = 0;
        }
        // set the type selection
        ArrayList<GEIndividual> crossoverIndividuals = EATools.getTypeSelection(RDFMiner.parameters.typeSelect,
                selectedIndividuals, sizeElite, sizeSelection);
        if(crossoverIndividuals == null) {
            // set distinct entities instead of all entities
            // i.e. remove duplicates
            crossoverIndividuals = distinctEntitiesAsIndividuals;
        }
        /* STEP 4 - CROSSOVER & MUTATION OPERATION */
        // Crossover single point between 2 individuals of the selected population
        ArrayList<Entity> crossoverEntities = EATools.bindIndividualsWithEntities(crossoverIndividuals, distinctEntities);
        ArrayList<Entity> elitismEntities = EATools.bindIndividualsWithEntities(elitismIndividuals, distinctEntities);
        // Compute GE and add new population on a new list of individuals
        ArrayList<Entity> computedPopulation = Generation.compute(crossoverEntities, curGeneration, generator);
        // set new population
        ArrayList<Entity> newPopulation = EATools.renew(curGeneration, computedPopulation, elitismEntities);
        // stats
        setStats(newPopulation, curGeneration);
        // renew population
        return newPopulation;
    }

    public static void setStats(ArrayList<Entity> entities, int curGeneration) {
        // set stats
        GenerationJSON generation = new GenerationJSON();
        generation.setGenerationJSON(entities, EATools.getDistinctGenotypePopulation(entities), curGeneration);
        // Log usefull stats concerning the algorithm evolution
        logger.info("Average fitness: " + generation.averageFitness);
        logger.info("Diversity coefficient: " + generation.diversityCoefficient);
        logger.info("Number of individual(s) with a non-null fitness: " + generation.numIndividualsWithNonNullFitness);
        RDFMiner.stats.generations.add(generation.toJSON());
    }

}
