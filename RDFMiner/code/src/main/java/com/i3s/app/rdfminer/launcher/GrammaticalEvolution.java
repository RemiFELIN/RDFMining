package com.i3s.app.rdfminer.launcher;

import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.RDFMiner;
import com.i3s.app.rdfminer.entity.Entity;
import com.i3s.app.rdfminer.evolutionary.fitness.Fitness;
import com.i3s.app.rdfminer.evolutionary.individual.CandidatePopulation;
import com.i3s.app.rdfminer.evolutionary.individual.GEIndividual;
import com.i3s.app.rdfminer.evolutionary.mining.EntityMining;
import com.i3s.app.rdfminer.generator.Generator;
import com.i3s.app.rdfminer.generator.axiom.RandomAxiomGenerator;
import com.i3s.app.rdfminer.generator.shacl.RandomShapeGenerator;
import com.i3s.app.rdfminer.output.Cache;
import com.i3s.app.rdfminer.output.Results;
import com.i3s.app.rdfminer.parameters.CmdLineParameters;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class GrammaticalEvolution {

    private static final Logger logger = Logger.getLogger(GrammaticalEvolution.class.getName());

    /**
     * The second version of RDFMiner launcher, with Grammar Evolutionary
     * @param parameters all parameters given in the execution of JAR
     */
    public static void run(CmdLineParameters parameters) throws Exception {

        // ShutDownHook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Save results in output file
            writeAndFinish();
            logger.warn("Shutting down RDFMiner ...");
        }));

        // set results content as JSON object
        RDFMiner.results = new Results();

        Generator generator = null;
        if (parameters.axiomFile == null) {
            if (parameters.useRandomAxiomGenerator) {
                // if a randomly generated Axiom already exists then continue
                // to generate a new Axioms based on BNF
                logger.info("Initializing the random axiom generator with grammar " + parameters.grammarFile + "...");
                generator = new RandomAxiomGenerator(parameters.grammarFile, true);
            } else if (parameters.useRandomShaclShapeGenerator) {
                // launch random SHACL Shapes generator
                logger.info("Initializing the random SHACL Shapes generator with grammar " + parameters.grammarFile + "...");
                generator = new RandomShapeGenerator(parameters.grammarFile);
            } else {
                logger.error("Generator is not defined ! Cannot generate any individuals ...");
                logger.warn("You can use: (-ra) to generate OWL Axioms; (-rs) to generate SHACL Shapes");
                System.exit(1);
            }
        }
        assert generator != null;

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
        setParameters(parameters);
        /* Parameters as the inputs of GE */
        logger.info("========================================================");
        logger.info("PARAMETERS AS THE INPUTS OF GE");
        logger.info("========================================================");
        logger.info("POPULATION SIZE: " + parameters.populationSize);
        logger.info("SIZE SELECTION: " + parameters.sizeSelection);
        int sizeElite = parameters.sizeElite * parameters.populationSize < 1 ?
                1 : (int) (parameters.sizeElite * parameters.populationSize);
        logger.info("SIZE ELITE: " + sizeElite);
        logger.info("TOTAL EFFORT : " + parameters.kBase);
        if(parameters.checkpoint != 1)
            logger.info("# CHECKPOINT: " + parameters.checkpoint);
        logger.info("INITIALIZED LENGTH CHROMOSOME: " + parameters.initLenChromosome);
        logger.info("MAXIMUM WRAPPING: " + parameters.maxWrapp);
        logger.info("CROSSOVER PROBABILITY: " + parameters.proCrossover);
        logger.info("MUTATION PROBABILITY: " + parameters.proMutation);
        logger.info("SPARQL TIMEOUT: " + (parameters.sparqlTimeOut == 0 ? "Not used" : parameters.sparqlTimeOut + " ms."));
        logger.info("TIME-CAP: " + (parameters.timeCap == 0 ? "Not used" : parameters.timeCap + " min."));
        logger.info("========================================================");
        logger.info("NUMBER OF THREAD(S) USED: " + Global.NB_THREADS);
        logger.info("========================================================");

//        GEChromosome[] chromosomes = new GEChromosome[parameters.populationSize];
        ArrayList<GEIndividual> candidatePopulation;

        int curCheckpoint = 0;
        int curGeneration = 1;

        Cache cache = null;
        final String CACHE_PATH = RDFMiner.outputFolder + "buffer_size" + RDFMiner.parameters.populationSize + ".json";
        File bufferFile = new File(CACHE_PATH);
        // check if the buffer file exists and if it is not empty
        if (bufferFile.exists() && (new BufferedReader(new FileReader(CACHE_PATH))).readLine() != null) {
            // instanciate cache
            cache = new Cache(bufferFile);
            curGeneration = cache.curGeneration;
            curCheckpoint = cache.curCheckpoint;
            logger.info("Buffer file founded ! starting from gen." + cache.curGeneration + " ...");
        }
        logger.info("Initializing candidate population ...");
        // Generate candidate population
        CandidatePopulation canPop = new CandidatePopulation(generator);
        candidatePopulation = canPop.initialize(cache, curGeneration);
        // Initialize population as Axioms or SHACL Shapes
        ArrayList<Entity> entities = Fitness.initializePopulation(candidatePopulation, generator);
        // start GE
        while (curCheckpoint < parameters.checkpoint) {
            System.out.println("\n--------------------------------------------------------\n");
            logger.info("Generation: " + curGeneration);
            // Grammatical evolution of OWL Axioms
            // i.e. run a generation
            entities = EntityMining.run(generator, entities, curGeneration, curCheckpoint);
            editCache(CACHE_PATH, entities, curGeneration, curCheckpoint);
            // update checkpoint
            if (parameters.populationSize * curGeneration == parameters.kBase * (curCheckpoint + 1)) {
                curCheckpoint++;
            }
            // Turn to the next generation
            curGeneration++;
        }
        logger.info("Evolutionary process is done...");
        System.exit(0);
    }

    public static void editCache(String cachePath, ArrayList<Entity> entities, int curGeneration, int curCheckpoint) throws IOException {
        PrintWriter writer = new PrintWriter(cachePath, StandardCharsets.UTF_8);
        ArrayList<String> genotypes = new ArrayList<>();
        for(Entity entity : entities) {
            genotypes.add(entity.individual.getGenotype().toString().substring(22,
                    entity.individual.getGenotype().toString().length() - 1));
        }
        Cache cache = new Cache(curGeneration, curCheckpoint, RDFMiner.parameters.initLenChromosome, genotypes);
        writer.println(cache.toJSON().toString(2));
        writer.close();
    }

    public static void writeAndFinish() {
        try {
            logger.info("Edit JSON file results ...");
            RDFMiner.results.stats = RDFMiner.stats.toJSON();
            RDFMiner.results.content = RDFMiner.content;
            RDFMiner.output.write(RDFMiner.results.toJSON().toString(2));
            RDFMiner.output.close();
            // if novelty seach is used
            if(RDFMiner.similarityMap != null) {
                RDFMiner.similarityMap.editFile();
            }
        } catch (IOException e) {
            logger.error("I/O error while closing JSON writer: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void setParameters(CmdLineParameters parameters) {
        RDFMiner.stats.populationSize = parameters.populationSize;
        RDFMiner.stats.maxLengthChromosome = parameters.initLenChromosome;
        RDFMiner.stats.maxWrapping = parameters.maxWrapp;
        RDFMiner.stats.crossoverProbability = parameters.proCrossover;
        RDFMiner.stats.mutationProbability = parameters.proMutation;
        RDFMiner.stats.timeOut = (int) parameters.sparqlTimeOut;
        // Elitism
        if (parameters.elitism == 1) {
            RDFMiner.stats.elitismSelection = true;
            RDFMiner.stats.eliteSize = parameters.sizeElite;
        } else {
            RDFMiner.stats.elitismSelection = false;
        }
        // Type select
        switch (parameters.typeSelect) {
            case 1:
                RDFMiner.stats.selectionMethod = "Roulette Wheel selection method";
                break;
            default:
            case 2:
                RDFMiner.stats.selectionMethod = "Truncation selection method";
                RDFMiner.stats.selectionSize = parameters.sizeSelection;
                break;
            case 3:
                RDFMiner.stats.selectionMethod = "Tournament selection method";
                break;
            case 4:
                RDFMiner.stats.selectionMethod = "Normal selection method";
                break;
        }
    }

}
