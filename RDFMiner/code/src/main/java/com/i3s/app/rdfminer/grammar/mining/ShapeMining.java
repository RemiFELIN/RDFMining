package com.i3s.app.rdfminer.grammar.mining;

import com.i3s.app.rdfminer.RDFMiner;
import com.i3s.app.rdfminer.entity.axiom.Axiom;
import com.i3s.app.rdfminer.entity.shacl.Shape;
import com.i3s.app.rdfminer.generator.Generator;
import com.i3s.app.rdfminer.grammar.evolutionary.EATools;
import com.i3s.app.rdfminer.grammar.evolutionary.individual.GEIndividual;
import com.i3s.app.rdfminer.grammar.evolutionary.selection.EliteSelection;
import com.i3s.app.rdfminer.output.axiom.GenerationJSON;
import com.i3s.app.rdfminer.parameters.CmdLineParameters;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class ShapeMining {

    private static final Logger logger = Logger.getLogger(ShapeMining.class.getName());

    public static ArrayList<Shape> run(CmdLineParameters parameters, Generator generator, ArrayList<Shape> shapes, int curGeneration, int curCheckpoint) throws IOException, URISyntaxException, InterruptedException {
        // set size selection
        int sizeSelection = (int) (parameters.sizeSelection * parameters.populationSize);
        int sizeElite = parameters.sizeElite * parameters.populationSize < 1 ? 1 : (int) (parameters.sizeElite * parameters.populationSize);

        // Checkpoint reached, this is a code to evaluate and save axioms in output file
        if (curGeneration == parameters.maxGeneration / parameters.checkpoint) {
            logger.info("Checkpoint n°" + curCheckpoint + " reached !");
            for(Shape shape : shapes) {
                RDFMiner.content.add(shape.toJSON());
            }
        }
        ArrayList<Shape> distinctShapes = EATools.getDistinctGenotypePopulationFromShapes(shapes);
        GenerationJSON generation = new GenerationJSON();
        generation.setGenerationJSONFromShapes(shapes, distinctShapes, curGeneration);
        // Log usefull stats concerning the algorithm evolution
        logger.info("Average fitness: " + generation.averageFitness);
        logger.info("Diversity coefficient: " + generation.diversityCoefficient);
        logger.info("Genotype diversity coefficient: " + generation.genotypeDiversityCoefficient);
        logger.info("Number of individual(s) with a non-null fitness: " + generation.numIndividualsWithNonNullFitness);
        RDFMiner.stats.generations.add(generation.toJSON());

        // STEP 3 - SELECTION OPERATION - Reproduce Selection - Parent Selection
        ArrayList<GEIndividual> shapesIndividual = new ArrayList<>();
        ArrayList<GEIndividual> distinctShapesIndividual = new ArrayList<>();
        ArrayList<GEIndividual> crossoverIndividuals, selectedIndividuals, elitismIndividuals = new ArrayList<>();

        // Use list of individuals instead of list of Axioms
        // i.e. apply GE process directly on individuals
        for(Shape shape : shapes) {
            shapesIndividual.add(shape.individual);
        }
        for(Shape shape : distinctShapes) {
            distinctShapesIndividual.add(shape.individual);
        }

        if (parameters.elitism == 1) {
            // Elitism method, which copies the best chromosome( or a few best
            // chromosome) to new population. The rest done classical way. it
            // prevents losing the best found solution
            logger.info("Selecting elite individuals...");
            logger.info("Selecting + " + (int) (parameters.sizeElite * 100)
                    + "% elite individuals for the new population");
            EliteSelection elite = new EliteSelection(sizeElite);
            elite.setParentsSelectionElitism(distinctShapesIndividual);
            selectedIndividuals = elite.setupSelectedPopulation(distinctShapesIndividual);
            logger.info("Size of the selected population: " + selectedIndividuals.size());
            elitismIndividuals = elite.getElitedPopulation();
            logger.info("Size of the elitism population: " + elitismIndividuals.size());
        } else {
            selectedIndividuals = distinctShapesIndividual;
            sizeElite = 0;
        }
        // set the type selection
        crossoverIndividuals = EATools.getTypeSelection(parameters.typeSelect, selectedIndividuals, sizeElite, sizeSelection);
        if(crossoverIndividuals == null) {
            crossoverIndividuals = shapesIndividual;
        }

        /* STEP 4 - CROSSOVER & MUTATION OPERATION */
        // Crossover single point between 2 individuals of the selected population
//        ArrayList<GEIndividual> crossoverList = new ArrayList<>(crossoverIndividuals);
        ArrayList<Shape> crossoverShapes = EATools.bindIndividualsWithShapes(crossoverIndividuals, distinctShapes);
        ArrayList<Shape> elitismShapes = EATools.bindIndividualsWithShapes(elitismIndividuals, distinctShapes);
        // shuffle populations before crossover & mutation
        java.util.Collections.shuffle(crossoverShapes);

        // Add new population on a new list of individuals
        ArrayList<Shape> newPopulation = EATools.computeShapesGeneration(crossoverShapes,
                parameters.proCrossover, parameters.proMutation, curGeneration, generator,
                parameters.diversity);

        return EATools.renewShapes(curGeneration, newPopulation, elitismShapes);
    }

}
