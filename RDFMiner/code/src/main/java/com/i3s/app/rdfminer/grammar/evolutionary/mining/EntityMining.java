package com.i3s.app.rdfminer.grammar.evolutionary.mining;

import com.i3s.app.rdfminer.RDFMiner;
import com.i3s.app.rdfminer.entity.Entity;
import com.i3s.app.rdfminer.entity.axiom.Axiom;
import com.i3s.app.rdfminer.generator.Generator;
import com.i3s.app.rdfminer.grammar.evolutionary.EATools;
import com.i3s.app.rdfminer.grammar.evolutionary.fitness.AxiomFitnessEvaluation;
import com.i3s.app.rdfminer.grammar.evolutionary.fitness.ShapeFitnessEvaluation;
import com.i3s.app.rdfminer.grammar.evolutionary.selection.EliteSelection;
import com.i3s.app.rdfminer.output.axiom.GenerationJSON;
import com.i3s.app.rdfminer.parameters.CmdLineParameters;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class EntityMining {

    private static final Logger logger = Logger.getLogger(EntityMining.class.getName());

    public static ArrayList<Entity> run(CmdLineParameters parameters, Generator generator, ArrayList<Entity> entities,
                                        int curGeneration, int curCheckpoint)
            throws URISyntaxException, IOException, ExecutionException, InterruptedException {
        // set size selection
        int sizeSelection = (int) (parameters.sizeSelection * parameters.populationSize);
        int sizeElite = parameters.sizeElite * parameters.populationSize < 1 ?
                1 : (int) (parameters.sizeElite * parameters.populationSize);
        // Checkpoint reached, this is a code to evaluate and save axioms in output file
        if (RDFMiner.parameters.populationSize * curGeneration == RDFMiner.parameters.kBase * curCheckpoint) {
            logger.info("Checkpoint n°" + curCheckpoint + " reached !");
            if(generator.generateAxioms) {
                AxiomFitnessEvaluation fit = new AxiomFitnessEvaluation();
                entities = fit.updatePopulation(entities);
                logger.info("Done ! fill " + fit.getEvaluatedAxioms().size() + " axioms in the results file ...");
                RDFMiner.content.addAll(fit.getEvaluatedAxioms());
            } else if(generator.generateShapes) {
                ShapeFitnessEvaluation fit = new ShapeFitnessEvaluation();
                entities = fit.updatePopulation(entities);
            }
            // curCheckpoint++;
            return entities;
        }

        ArrayList<Entity> distinctEntities = EATools.getDistinctGenotypePopulation(entities);
        GenerationJSON generation = new GenerationJSON();
        generation.setGenerationJSON(entities, distinctEntities, curGeneration);
        // Log usefull stats concerning the algorithm evolution
        logger.info("Average fitness: " + generation.averageFitness);
        logger.info("Diversity coefficient: " + generation.diversityCoefficient);
        logger.info("Genotype diversity coefficient: " + generation.genotypeDiversityCoefficient);
        logger.info("Number of individual(s) with a non-null fitness: " + generation.numIndividualsWithNonNullFitness);
        RDFMiner.stats.generations.add(generation.toJSON());

        // STEP 3 - SELECTION OPERATION - Reproduce Selection - Parent Selection
        ArrayList<Entity> entitiesIndividual = new ArrayList<>();
        ArrayList<GEIndividual> distinctAxiomsIndividual = new ArrayList<>();
        ArrayList<GEIndividual> crossoverIndividuals, selectedIndividuals, elitismIndividuals = new ArrayList<>();

        // Use list of individuals instead of list of Axioms
        // i.e. apply GE process directly on individuals
        for(Axiom axiom : axioms) {
            axiomsIndividual.add(axiom.individual);
        }
        for(Axiom axiom : distinctAxioms) {
            distinctAxiomsIndividual.add(axiom.individual);
        }

        if (parameters.elitism == 1) {
            // Elitism method, which copies the best chromosome( or a few best
            // chromosome) to new population. The rest done classical way. it
            // prevents losing the best found solution
            logger.info("Selecting elite individuals...");
            logger.info("Selecting + " + (int) (parameters.sizeElite * 100)
                    + "% elite individuals for the new population");
            EliteSelection elite = new EliteSelection(sizeElite);
            elite.setParentsSelectionElitism(distinctAxiomsIndividual);
            selectedIndividuals = elite.setupSelectedPopulation(distinctAxiomsIndividual);
            logger.info("Size of the selected population: " + selectedIndividuals.size());
            elitismIndividuals = elite.getElitedPopulation();
            logger.info("Size of the elitism population: " + elitismIndividuals.size());
        } else {
            selectedIndividuals = distinctAxiomsIndividual;
            sizeElite = 0;
        }
        // set the type selection
        crossoverIndividuals = EATools.getTypeSelection(parameters.typeSelect, selectedIndividuals, sizeElite, sizeSelection);
        if(crossoverIndividuals == null) {
            crossoverIndividuals = axiomsIndividual;
        }

        /* STEP 4 - CROSSOVER & MUTATION OPERATION */
        // Crossover single point between 2 individuals of the selected population
//        ArrayList<GEIndividual> crossoverList = new ArrayList<>(crossoverIndividuals);
        ArrayList<Axiom> crossoverAxioms = EATools.bindIndividualsWithAxioms(crossoverIndividuals, distinctAxioms);
        ArrayList<Axiom> elitismAxioms = EATools.bindIndividualsWithAxioms(elitismIndividuals, distinctAxioms);
        // shuffle populations before crossover & mutation
        java.util.Collections.shuffle(crossoverAxioms);

        // Add new population on a new list of individuals
        ArrayList<Axiom> newPopulation = EATools.computeAxiomsGeneration(crossoverAxioms,
                parameters.proCrossover, parameters.proMutation, curGeneration, generator,
                parameters.diversity);

        return EATools.renewAxioms(curGeneration, newPopulation, elitismAxioms);
    }

}
