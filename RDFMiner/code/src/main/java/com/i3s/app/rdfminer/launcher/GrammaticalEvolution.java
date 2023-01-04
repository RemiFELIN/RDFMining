package com.i3s.app.rdfminer.launcher;

import Individuals.GEChromosome;
import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.RDFMiner;
import com.i3s.app.rdfminer.entity.shape.Shape;
import com.i3s.app.rdfminer.generator.Generator;
import com.i3s.app.rdfminer.generator.axiom.RandomAxiomGenerator;
import com.i3s.app.rdfminer.generator.shacl.RandomShapeGenerator;
import com.i3s.app.rdfminer.grammar.evolutionary.EATools;
import com.i3s.app.rdfminer.grammar.evolutionary.fitness.AxiomFitnessEvaluation;
import com.i3s.app.rdfminer.grammar.evolutionary.fitness.Fitness;
import com.i3s.app.rdfminer.grammar.evolutionary.fitness.ShapeFitnessEvaluation;
import com.i3s.app.rdfminer.grammar.evolutionary.individual.CandidatePopulation;
import com.i3s.app.rdfminer.grammar.evolutionary.individual.GEIndividual;
import com.i3s.app.rdfminer.grammar.evolutionary.selection.EliteSelection;
import com.i3s.app.rdfminer.output.axiom.AxiomsResultsJSON;
import com.i3s.app.rdfminer.output.axiom.GenerationJSON;
import com.i3s.app.rdfminer.output.axiom.StatJSON;
import com.i3s.app.rdfminer.statistics.Statistics;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class GrammaticalEvolution {

    private static final Logger logger = Logger.getLogger(GrammaticalEvolution.class.getName());

    /**
     * The second version of RDFMiner launcher, with Grammar Evolutionary
     */
    public void run() throws Exception {

        // ShutDownHook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.warn("Shutting down RDFMiner ...");
            // Save results in output file
            writeAndFinish();
        }));

        RDFMiner.results = new AxiomsResultsJSON();
        RDFMiner.content = new ArrayList<>();
        RDFMiner.stats = new StatJSON();

        Generator generator = null;

        if (RDFMiner.parameters.axiomFile == null) {
            if (RDFMiner.parameters.useRandomAxiomGenerator) {
                // if a randomly generated Axiom already exists then continue
                // to generate a new Axioms based on BNF
                logger.info("Initializing the random axiom generator with grammar " + RDFMiner.parameters.grammarFile + "...");
                generator = new RandomAxiomGenerator(RDFMiner.parameters.grammarFile, true);
            }
            if (RDFMiner.parameters.useRandomShaclShapeGenerator) {
                // launch random SHACL Shapes generator
                logger.info("Initializing the random SHACL Shapes generator with grammar " + RDFMiner.parameters.grammarFile + "...");
                generator = new RandomShapeGenerator(RDFMiner.parameters.grammarFile);
            }
        }

        // Create the results file
        try {
            RDFMiner.output = new FileWriter(RDFMiner.outputFolder + Global.RESULTS_FILENAME);
        } catch (IOException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        /* GRAMMATICAL EVOLUTIONARY */
        /* Parameters as the inputs of GE */
        logger.info("========================================================");
        logger.info("PARAMETERS AS THE INPUTS OF GE");
        logger.info("========================================================");
        logger.info("POPULATION SIZE: " + RDFMiner.parameters.populationSize);
        logger.info("SIZE SELECTION: " + RDFMiner.parameters.sizeSelection);
        int sizeElite = RDFMiner.parameters.sizeElite * RDFMiner.parameters.populationSize < 1 ?
                1 : (int) (RDFMiner.parameters.sizeElite * RDFMiner.parameters.populationSize);
        logger.info("SIZE ELITE: " + sizeElite);
        logger.info("TOTAL EFFORT : " + RDFMiner.parameters.kBase);
        logger.info("MAX GENERATION NUMBER: " + (RDFMiner.parameters.kBase / RDFMiner.parameters.populationSize));
        logger.info("INITIALIZED LENGTH CHROMOSOME: " + RDFMiner.parameters.initLenChromosome);
        logger.info("MAXIMUM WRAPPING: " + RDFMiner.parameters.maxWrapp);
        logger.info("CROSSOVER PROBABILITY: " + RDFMiner.parameters.proCrossover);
        logger.info("MUTATION PROBABILITY: " + RDFMiner.parameters.proMutation);
        logger.info("TIME-CAP: " + (RDFMiner.parameters.timeOut == 0 ?
                "Not used" : RDFMiner.parameters.timeOut + " secondes"));
        logger.info("========================================================");
        logger.info("NUMBER OF THREAD(S) USED: " + Global.NB_THREADS);
        logger.info("========================================================");

        GEChromosome[] chromosomes = new GEChromosome[RDFMiner.parameters.populationSize];

        ArrayList<GEIndividual> candidatePopulation;
        ArrayList<GEIndividual> elitismPopulation = null;
        Statistics stat = new Statistics();

        int sizeSelection = (int) (RDFMiner.parameters.sizeSelection * RDFMiner.parameters.populationSize);

        int curCheckpoint;
        int curGeneration;
        boolean flag = false;
        Reader buffer;
        File bufferFile = new File(RDFMiner.outputFolder + "buffer_size" + RDFMiner.parameters.populationSize + ".txt");
        if (!bufferFile.exists()) {
            buffer = null;
            curCheckpoint = curGeneration = 1;
        } else {
            FileInputStream reader = new FileInputStream(
                    RDFMiner.outputFolder + "buffer_size" + RDFMiner.parameters.populationSize + ".txt");
            buffer = new InputStreamReader(reader, StandardCharsets.UTF_8);
            int intch;
            StringBuilder st = new StringBuilder();
            while ((intch = reader.read()) != '\n') {
                st.append((char) intch);
            }
            curGeneration = Integer.parseInt(st.toString());
            st = new StringBuilder();
            while ((intch = reader.read()) != '\n') {
                st.append((char) intch);
            }
            curCheckpoint = Integer.parseInt(st.toString());
            logger.info("Buffer file founded ! starting from gen." + curGeneration + " ...");
        }
        logger.info("Initializing candidate population in generation " + curGeneration + "...");
        CandidatePopulation canPop = new CandidatePopulation(RDFMiner.parameters.populationSize, generator,
                RDFMiner.parameters.typeInitialization, chromosomes, RDFMiner.parameters.initLenChromosome,
                RDFMiner.parameters.maxValCodon, RDFMiner.parameters.maxWrapp);
        candidatePopulation = canPop.initialize(buffer, curGeneration);
        // Fill the 'stats' part of the JSON output
        RDFMiner.stats.populationSize = RDFMiner.parameters.populationSize;
        RDFMiner.stats.maxLengthChromosome = RDFMiner.parameters.initLenChromosome;
        RDFMiner.stats.maxWrapping = RDFMiner.parameters.maxWrapp;
        RDFMiner.stats.crossoverProbability = RDFMiner.parameters.proCrossover;
        RDFMiner.stats.mutationProbability = RDFMiner.parameters.proMutation;
        RDFMiner.stats.timeOut = (int) RDFMiner.parameters.timeOut;

        if (RDFMiner.parameters.elitism == 1) {
            RDFMiner.stats.elitismSelection = true;
            RDFMiner.stats.eliteSize = RDFMiner.parameters.sizeElite;
        } else {
            RDFMiner.stats.elitismSelection = false;
        }

        switch (RDFMiner.parameters.typeSelect) {
            case 1:
                RDFMiner.stats.selectionMethod = "Roulette Wheel selection method";
                break;
            default:
            case 2:
                RDFMiner.stats.selectionMethod = "Truncation selection method";
                RDFMiner.stats.selectionSize = RDFMiner.parameters.sizeSelection;
                break;
            case 3:
                RDFMiner.stats.selectionMethod = "Tournament selection method";
                break;
            case 4:
                RDFMiner.stats.selectionMethod = "Normal selection method";
                break;
        }

        // set the fitness method
        Fitness fit = null;
        if (RDFMiner.mode.isAxiomMode()) {
            // set a Fitness method provided to update individuals as OWL 2 Axiom
            fit = new AxiomFitnessEvaluation();
        } else if (RDFMiner.mode.isShaclMode()) {
            // set a Fitness method provided to update individuals as SHACL Shape
            fit = new ShapeFitnessEvaluation();
        }

        assert fit != null;

        while (curCheckpoint <= RDFMiner.parameters.checkpoint) {
            System.out.println("\n--------------------------------------------------------\n");
            logger.info("Generation: " + curGeneration);
            // First step of the grammatical evolution
            if ((curGeneration == 1) || ((buffer != null) && (!flag))) {
                // if1
                if(RDFMiner.mode.isAxiomMode()) {
                    // special case for axiom mode
                    candidatePopulation = fit.updatePopulation(candidatePopulation, Global.TRAINING_SPARQL_ENDPOINT, Global.PREFIXES, null);
                } else {
                    candidatePopulation = fit.updatePopulation(candidatePopulation, Global.TARGET_SPARQL_ENDPOINT, Global.PREFIXES, null);
                }
            }
            // Checkpoint reached, this is a code to evaluate and save axioms in output file
            if (RDFMiner.parameters.populationSize * curGeneration == RDFMiner.parameters.kBase * curCheckpoint) {

                if(RDFMiner.mode.isAxiomMode()) {
                    List<JSONObject> content = new ArrayList<>();
                    ArrayList<GEIndividual> candidatePopulation2 = new ArrayList<>();
                    for (GEIndividual geIndividual : candidatePopulation) {
                        GEIndividual indivi = new GEIndividual();
                        indivi.setMapper(geIndividual.getMapper());
                        indivi.setGenotype(geIndividual.getGenotype());
                        indivi.setPhenotype(geIndividual.getPhenotype());
                        indivi.setMapped(geIndividual.isMapped());
                        candidatePopulation2.add(indivi);
                    }
                    logger.info("Evaluating axioms against to the RDF Data of the whole DBPedia.");
                    fit.updatePopulation(candidatePopulation2, Global.TARGET_SPARQL_ENDPOINT, Global.PREFIXES, content);
                    RDFMiner.content.addAll(content);
                } else {
                    assert fit instanceof ShapeFitnessEvaluation;
                    for(Shape shape : ((ShapeFitnessEvaluation) fit).getShapes()) {
//                        RDFMiner.content.add(shape.toJSON());
                    }
                }

                curCheckpoint++;
            }

            ArrayList<GEIndividual> distinctCandidatePopulation = EATools.getDistinctPopulation(candidatePopulation);
            ArrayList<GEIndividual> distinctGenotypeCandidatePopulation = EATools
                    .getDistinctGenotypePopulation(candidatePopulation);

            GenerationJSON generation = new GenerationJSON(curGeneration, stat.getCountSuccessMapping(distinctCandidatePopulation),
                    (double) distinctCandidatePopulation.size() / candidatePopulation.size(),
                    (double) distinctGenotypeCandidatePopulation.size()/candidatePopulation.size(),
                    stat.computeAverageFitness(distinctCandidatePopulation),
                    stat.getCountComplexAxiom(distinctCandidatePopulation),
                    stat.getIndividualsWithNonNullFitness(distinctCandidatePopulation));
            // Log usefull stats concerning the algorithm evolution
            logger.info("Average fitness: " + generation.averageFitness);
            logger.info("Diversity coefficient: " + generation.diversityCoefficient);
            logger.info("Genotype diversity coefficient: " + generation.genotypeDiversityCoefficient);
            logger.info("Number of individual(s) with a non-null fitness: " + generation.numIndividualsWithNonNullFitness);
            // set generation as JSON
            RDFMiner.stats.generations.add(generation.toJSON());

            if (curGeneration * RDFMiner.parameters.populationSize <= RDFMiner.parameters.kBase * RDFMiner.parameters.checkpoint) {
                // STEP 3 - SELECTION OPERATION - Reproduce Selection - Parent Selection
                ArrayList<GEIndividual> crossoverPopulation, selectedPopulation;
                if (RDFMiner.parameters.elitism == 1) {
                    // Elitism method, which copies the best chromosome( or a few best
                    // chromosome) to new population. The rest done classical way. it
                    // prevents losing the best found solution
                    logger.info("Selecting elite individuals...");
                    logger.info("Selecting + " + (int) (RDFMiner.parameters.sizeElite * 100)
                            + "% elite individuals for the new population");
//                    logger.info("The size of elite population: " + sizeElite);
                    EliteSelection elite = new EliteSelection(sizeElite);
                    elite.setParentsSelectionElitism(distinctCandidatePopulation);
                    selectedPopulation = elite.setupSelectedPopulation(distinctCandidatePopulation);
                    logger.info("Size of the selected population: " + selectedPopulation.size());
                    elitismPopulation = elite.getElitedPopulation();
                    logger.info("Size of the elitism population: " + elitismPopulation.size());
                } else {
                    selectedPopulation = distinctCandidatePopulation;
                    sizeElite = 0;
                }
                // set the type selection
                crossoverPopulation = EATools.getTypeSelection(RDFMiner.parameters.typeSelect, selectedPopulation, sizeElite, sizeSelection);
                if(crossoverPopulation == null) {
                    crossoverPopulation = candidatePopulation;
                }
                /* STEP 4 - CROSSOVER & MUTATION OPERATION */
                // Crossover single point between 2 individuals of the selected population
                ArrayList<GEIndividual> crossoverList = new ArrayList<>(crossoverPopulation);
                // shuffle populations before crossover & mutation
                java.util.Collections.shuffle(crossoverList);
                // Add new population on a new list of individuals
                ArrayList<GEIndividual> newPopulation = EATools.computeGeneration(crossoverList,
                        RDFMiner.parameters.proCrossover, RDFMiner.parameters.proMutation, curGeneration, generator,
                        RDFMiner.parameters.diversity, RDFMiner.mode);
                // renew population
                candidatePopulation = canPop.renew(newPopulation, curGeneration, elitismPopulation);
                // Turn to the next generation
                curGeneration++;
                // Write to buffer file
                PrintWriter writer = new PrintWriter(
                        RDFMiner.outputFolder + "buffer_size" + RDFMiner.parameters.populationSize + ".txt", StandardCharsets.UTF_8);
                writer.println(curGeneration);
                writer.println(curCheckpoint);
                flag = true;
                for (GEIndividual geIndividual : candidatePopulation) {
                    writer.println(geIndividual.getGenotype().toString().substring(22,
                            geIndividual.getGenotype().toString().length() - 1));
                }
                writer.close();
            } else {
                logger.info("Evolutionary process is done...");
                break;
            }
        }
        System.exit(0);
    }

    public static void writeAndFinish() {
        try {
            RDFMiner.results.stats = RDFMiner.stats.toJSON();
            // sort axioms (by ARI or Generality) using type of axioms
//            if(mode.isAxiomMode()) {
//                if (RDFMiner.type == Type.DISJOINT_CLASSES)
//                    logger.info("sort axioms by generality ...");
//                else
//                    logger.info("sort axioms by ARI ...");
//                RDFMiner.content.sort(Comparator.comparingDouble(j -> {
//                    // if we have disjoint classes axioms, we need to sort using generality
//                    if (RDFMiner.type == Type.DISJOINT_CLASSES) {
//                        return j.getInt("generality");
//                    }
//                    return j.getDouble("ari");
//                }));
//            }
            RDFMiner.results.content = RDFMiner.content;
            RDFMiner.output.write(RDFMiner.results.toJSON().toString());
            RDFMiner.output.close();
        } catch (IOException e) {
            logger.error("I/O error while closing JSON writer: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

}
