package com.i3s.app.rdfminer.launcher;

import Individuals.GEChromosome;
import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.RDFMiner;
import com.i3s.app.rdfminer.entity.axiom.Axiom;
import com.i3s.app.rdfminer.entity.axiom.Type;
import com.i3s.app.rdfminer.generator.Generator;
import com.i3s.app.rdfminer.generator.axiom.RandomAxiomGenerator;
import com.i3s.app.rdfminer.generator.shacl.RandomShapeGenerator;
import com.i3s.app.rdfminer.grammar.evolutionary.EATools;
import com.i3s.app.rdfminer.grammar.evolutionary.fitness.AxiomFitnessEvaluation;
import com.i3s.app.rdfminer.grammar.evolutionary.fitness.ShapeFitnessEvaluation;
import com.i3s.app.rdfminer.grammar.evolutionary.individual.CandidatePopulation;
import com.i3s.app.rdfminer.grammar.evolutionary.individual.GEIndividual;
import com.i3s.app.rdfminer.grammar.mining.AxiomMining;
import com.i3s.app.rdfminer.grammar.mining.ShapeMining;
import com.i3s.app.rdfminer.mode.Mode;
import com.i3s.app.rdfminer.mode.TypeMode;
import com.i3s.app.rdfminer.output.axiom.AxiomsResultsJSON;
import com.i3s.app.rdfminer.parameters.CmdLineParameters;
import com.i3s.app.rdfminer.entity.shacl.Shape;
import com.i3s.app.rdfminer.statistics.Statistics;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;

public class GrammaticalEvolution {

    private static final Logger logger = Logger.getLogger(GrammaticalEvolution.class.getName());

    /**
     * The second version of RDFMiner launcher, with Grammar Evolutionary
     * @param parameters all parameters given in the execution of JAR
     */
    public static void run(CmdLineParameters parameters) throws Exception {

        // ShutDownHook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.warn("Shutting down RDFMiner ...");
            // Save results in output file
            writeAndFinish(RDFMiner.mode);
        }));

        RDFMiner.results = new AxiomsResultsJSON();

        Generator generator = null;
        if (parameters.axiomFile == null) {
            if (parameters.useRandomAxiomGenerator) {
                // if a randomly generated Axiom already exists then continue
                // to generate a new Axioms based on BNF
                logger.info("Initializing the random axiom generator with grammar " + parameters.grammarFile + "...");
                generator = new RandomAxiomGenerator(parameters.grammarFile, true);
            } else if (parameters.useShaclMode) {
                // launch random SHACL Shapes generator
                logger.info("Initializing the random SHACL Shapes generator with grammar " + parameters.grammarFile + "...");
                generator = new RandomShapeGenerator(parameters.grammarFile);
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
        // Fill the 'stats' part of the JSON output
        Statistics.setParameters(parameters);
        /* Parameters as the inputs of GE */
        logger.info("========================================================");
        logger.info("PARAMETERS AS THE INPUTS OF GE");
        logger.info("========================================================");
        logger.info("POPULATION SIZE: " + parameters.populationSize);
        logger.info("SIZE SELECTION: " + parameters.sizeSelection);
        int sizeElite = parameters.sizeElite * parameters.populationSize < 1 ? 1 : (int) (parameters.sizeElite * parameters.populationSize);
        logger.info("SIZE ELITE: " + sizeElite);
//        logger.info("TOTAL EFFORT : " + parameters.kBase);
        logger.info("MAX GENERATION: " + parameters.maxGeneration);
        logger.info("INITIALIZED LENGTH CHROMOSOME: " + parameters.initLenChromosome);
        logger.info("MAXIMUM WRAPPING: " + parameters.maxWrapp);
        logger.info("CROSSOVER PROBABILITY: " + parameters.proCrossover);
        logger.info("MUTATION PROBABILITY: " + parameters.proMutation);
        logger.info("TIME-CAP: " + (parameters.timeOut == 0 ? "Not used" : parameters.timeOut + " secondes"));
        logger.info("========================================================");
        logger.info("NUMBER OF THREAD(S) USED: " + Global.NB_THREADS);
        logger.info("========================================================");

        GEChromosome[] chromosomes = new GEChromosome[parameters.populationSize];
        ArrayList<GEIndividual> candidatePopulation;
//        ArrayList<GEIndividual> elitismPopulation = null;

        int curCheckpoint;
        int curGeneration;
//        boolean flag = false;
        Reader buffer = null;

        final String CACHE_PATH = RDFMiner.outputFolder + "buffer_size" + parameters.populationSize + ".txt";
        File bufferFile = new File(CACHE_PATH);
        // check if the buffer file exists and if it is not empty
        if (bufferFile.exists() && (new BufferedReader(new FileReader(CACHE_PATH))).readLine() != null) {
            FileInputStream reader = new FileInputStream(CACHE_PATH);
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
        } else {
            // the file does not exists or it's empty file,
            curCheckpoint = curGeneration = 1;
        }
        logger.info("Initializing candidate population in generation " + curGeneration + "...");
        // Generate candidate population
        CandidatePopulation canPop = new CandidatePopulation(parameters.populationSize, generator,
                parameters.typeInitialization, chromosomes, parameters.initLenChromosome, parameters.maxValCodon,
                parameters.maxWrapp);
        candidatePopulation = canPop.initialize(buffer, curGeneration);

        PrintWriter writer = new PrintWriter(
                RDFMiner.outputFolder + "buffer_size" + parameters.populationSize + ".txt", StandardCharsets.UTF_8);

        ArrayList<Axiom> axioms = new ArrayList<>();
        ArrayList<Shape> shapes = new ArrayList<>();
        // Initialize population as Axioms or SHACL Shapes
        switch (RDFMiner.mode.type) {
            case TypeMode.AXIOMS:
                axioms = AxiomFitnessEvaluation.initializePopulation(candidatePopulation);
                break;
            case TypeMode.SHACL_SHAPE:
                shapes = ShapeFitnessEvaluation.initializePopulation(candidatePopulation);
                break;
        }

        while (curGeneration <= parameters.maxGeneration) {

            System.out.println("\n--------------------------------------------------------\n");
            logger.info("Generation: " + curGeneration);

            switch (RDFMiner.mode.type) {
                // Grammatical evolution of OWL Axioms
                // i.e. run a generation
                case TypeMode.AXIOMS:
                    axioms = AxiomMining.run(parameters, generator, axioms, curGeneration, curCheckpoint);
                    editCache(CACHE_PATH, EATools.getIndividualsFromAxioms(axioms), curGeneration, curCheckpoint);
                    break;
                case TypeMode.SHACL_SHAPE:
                    shapes = ShapeMining.run(parameters, generator, shapes, curGeneration, curCheckpoint);
                    editCache(CACHE_PATH, EATools.getIndividualsFromShapes(shapes), curGeneration, curCheckpoint);
                    break;
            }

            // update checkpoint
            if (curGeneration == parameters.maxGeneration / parameters.checkpoint) {
                curCheckpoint++;
            }
            // Turn to the next generation
            curGeneration++;
            // First step of the grammatical evolution

//            if ((curGeneration == 1) || ((buffer != null) && (!flag))) {
//                // if1
//                if(RDFMiner.mode.isAxiomMode()) {
//                    // special case for axiom mode
//                    axioms = AxiomFitnessEvaluation.initializePopulation(candidatePopulation, Global.TRAINING_SPARQL_ENDPOINT);
//                } else {
//                    ShapeFitnessEvaluation shapeFitnessEvaluation = new ShapeFitnessEvaluation();
//                    shapes = shapeFitnessEvaluation.updatePopulation(candidatePopulation, Global.SPARQL_ENDPOINT);
//                }
//            }
//            AxiomMining.run(parameters, generator, axioms, curGeneration, curCheckpoint);
//            if (curGeneration == parameters.maxGeneration / parameters.checkpoint) {
//                curCheckpoint++;
//            }
//            // Checkpoint reached, this is a code to evaluate and save axioms in output file
//            else if (curGeneration == parameters.maxGeneration / parameters.checkpoint) {
//                logger.info("Checkpoint n°" + curCheckpoint + " reached !");
//                if(RDFMiner.mode.isAxiomMode()) {
//                    List<JSONObject> content = new ArrayList<>();
//                    ArrayList<GEIndividual> candidatePopulation2 = new ArrayList<>();
//                    for (GEIndividual individual : candidatePopulation) {
//                        GEIndividual i = new GEIndividual();
//                        i.setMapper(individual.getMapper());
//                        i.setGenotype(individual.getGenotype());
//                        i.setPhenotype(individual.getPhenotype());
//                        i.setMapped(individual.isMapped());
//                        candidatePopulation2.add(i);
//                    }
//                    logger.info("Evaluating axioms against to the RDF Data of the whole DBPedia.");
//                    AxiomFitnessEvaluation.updatePopulation(candidatePopulation2, Global.SPARQL_ENDPOINT, content);
//                    RDFMiner.content.addAll(content);
//                } else {
////                    assert fit instanceof ShapeFitnessEvaluation;
//                    for(Shape shape : shapes) {
//                        RDFMiner.content.add(shape.toJSON());
//                    }
//                }
//
//                curCheckpoint++;
//            }
//
//            ArrayList<GEIndividual> distinctCandidatePopulation = EATools.getDistinctPopulation(candidatePopulation);
//            GenerationJSON generation = new GenerationJSON(candidatePopulation, distinctCandidatePopulation, curGeneration);
//            // Log usefull stats concerning the algorithm evolution
//            logger.info("Average fitness: " + generation.averageFitness);
//            logger.info("Diversity coefficient: " + generation.diversityCoefficient);
//            logger.info("Genotype diversity coefficient: " + generation.genotypeDiversityCoefficient);
//            logger.info("Number of individual(s) with a non-null fitness: " + generation.numIndividualsWithNonNullFitness);
//
////            if (curGeneration * parameters.populationSize <= parameters.kBase * parameters.checkpoint) {
//            // STEP 3 - SELECTION OPERATION - Reproduce Selection - Parent Selection
//            ArrayList<GEIndividual> crossoverPopulation, selectedPopulation;
//            if (parameters.elitism == 1) {
//                // Elitism method, which copies the best chromosome( or a few best
//                // chromosome) to new population. The rest done classical way. it
//                // prevents losing the best found solution
//                logger.info("Selecting elite individuals...");
//                logger.info("Selecting + " + (int) (parameters.sizeElite * 100)
//                        + "% elite individuals for the new population");
////                    logger.info("The size of elite population: " + sizeElite);
//                EliteSelection elite = new EliteSelection(sizeElite);
//                elite.setParentsSelectionElitism(distinctCandidatePopulation);
//                selectedPopulation = elite.setupSelectedPopulation(distinctCandidatePopulation);
//                logger.info("Size of the selected population: " + selectedPopulation.size());
//                elitismPopulation = elite.getElitedPopulation();
//                logger.info("Size of the elitism population: " + elitismPopulation.size());
//            } else {
//                selectedPopulation = distinctCandidatePopulation;
//                sizeElite = 0;
//            }
//            // set the type selection
//            crossoverPopulation = EATools.getTypeSelection(parameters.typeSelect, selectedPopulation, sizeElite, sizeSelection);
//            if(crossoverPopulation == null) {
//                crossoverPopulation = candidatePopulation;
//            }
//
//            /* STEP 4 - CROSSOVER & MUTATION OPERATION */
//            // Crossover single point between 2 individuals of the selected population
//            ArrayList<GEIndividual> crossoverList = new ArrayList<>(crossoverPopulation);
//            // shuffle populations before crossover & mutation
//            java.util.Collections.shuffle(crossoverList);
//
//            // Add new population on a new list of individuals
//            ArrayList<GEIndividual> newPopulation = EATools.computeGeneration(crossoverList,
//                    parameters.proCrossover, parameters.proMutation, curGeneration, generator,
//                    parameters.diversity, RDFMiner.mode);
//
//            candidatePopulation = canPop.renew(newPopulation, curGeneration, elitismPopulation);
//            // Turn to the next generation
//            curGeneration++;
//
//            writer.println(curGeneration);
//            writer.println(curCheckpoint);
//            flag = true;
//            for (GEIndividual geIndividual : candidatePopulation) {
//                writer.println(geIndividual.getGenotype().toString().substring(22,
//                        geIndividual.getGenotype().toString().length() - 1));
//            }
//            writer.close();
//            } else {
//            logger.info("Evolutionary process is done...");
//                break;
        }
        logger.info("Evolutionary process is done...");
        System.exit(0);
    }

    public static void writeAndFinish(Mode mode) {
        try {
            RDFMiner.results.stats = RDFMiner.stats.toJSON();
            // sort axioms (by ARI or Generality) using type of axioms
            if(mode.isAxiomMode()) {
                if (RDFMiner.type == Type.DISJOINT_CLASSES)
                    logger.info("sort axioms by generality ...");
                else
                    logger.info("sort axioms by ARI ...");
                RDFMiner.content.sort(Comparator.comparingDouble(j -> {
                    // if we have disjoint classes axioms, we need to sort using generality
                    if (RDFMiner.type == Type.DISJOINT_CLASSES) {
                        return j.getInt("generality");
                    }
                    return j.getDouble("ari");
                }));
            }
            RDFMiner.results.content = RDFMiner.content;
            RDFMiner.output.write(RDFMiner.results.toJSON().toString());
            RDFMiner.output.close();
        } catch (IOException e) {
            logger.error("I/O error while closing JSON writer: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void editCache(String cachePath, ArrayList<GEIndividual> individuals, int curGeneration, int curCheckpoint) throws IOException {
        PrintWriter writer = new PrintWriter(cachePath, StandardCharsets.UTF_8);
        writer.println(curGeneration);
        writer.println(curCheckpoint);
        for (GEIndividual individual : individuals) {
            logger.info("individual as genotype: " + individual.getGenotype());
            writer.println(individual.getGenotype().toString().substring(22,
                    individual.getGenotype().toString().length() - 1));
        }
        writer.close();
    }

}
