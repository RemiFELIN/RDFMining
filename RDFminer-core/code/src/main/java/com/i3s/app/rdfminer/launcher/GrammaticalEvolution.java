package com.i3s.app.rdfminer.launcher;

import com.i3s.app.rdfminer.Endpoint;
import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.Parameters;
import com.i3s.app.rdfminer.RDFMiner;
import com.i3s.app.rdfminer.entity.Entity;
import com.i3s.app.rdfminer.evolutionary.EntityMining;
import com.i3s.app.rdfminer.evolutionary.fitness.Fitness;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.GEIndividual;
import com.i3s.app.rdfminer.evolutionary.individual.CandidatePopulation;
import com.i3s.app.rdfminer.evolutionary.stopCriterion.ClockWorldStop;
import com.i3s.app.rdfminer.evolutionary.stopCriterion.EffortStop;
import com.i3s.app.rdfminer.evolutionary.stopCriterion.StopCriterion;
import com.i3s.app.rdfminer.evolutionary.types.TypeCrossover;
import com.i3s.app.rdfminer.evolutionary.types.TypeMutation;
import com.i3s.app.rdfminer.evolutionary.types.TypeSelection;
import com.i3s.app.rdfminer.generator.Generator;
import com.i3s.app.rdfminer.generator.axiom.RandomAxiomGenerator;
import com.i3s.app.rdfminer.generator.shacl.RandomShapeGenerator;
import com.i3s.app.rdfminer.output.Cache;
import com.i3s.app.rdfminer.output.IndividualJSON;
import com.i3s.app.rdfminer.output.Results;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class GrammaticalEvolution {

    private static final Logger logger = Logger.getLogger(GrammaticalEvolution.class.getName());

    Parameters parameters = Parameters.getInstance();

//    public static int nBetterIndividual;
    public static int nRecombinaison;
    public static int nCrossover;
    public static int nMutation;

    /**
     * Implementation of GE
     */
    public void run() throws Exception {

        // ShutDownHook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Save results in output file
            writeAndFinish();
            logger.warn("Shutting down RDFMiner ...");
        }));

        // set results content as JSON object
        RDFMiner.results = new Results(false);
        RDFMiner.results.saveResult();

        Generator generator = null;
        if (parameters.getAxioms() == null) {
            Global.GRAMMAR_FILE = Global.OUTPUT_PATH + parameters.getGrammar();
            if (parameters.useRandomAxiomGenerator) {
                // if a randomly generated Axiom already exists then continue
                // to generate a new Axioms based on BNF
                logger.info("Initializing the random axiom generator with grammar: " + Global.GRAMMAR_FILE);
                generator = new RandomAxiomGenerator(Global.GRAMMAR_FILE, true);
            } else if (parameters.useRandomShaclShapeGenerator) {
                // launch random SHACL Shapes generator
                logger.info("Initializing the random SHACL Shapes generator with grammar: " + Global.GRAMMAR_FILE + "...");
                generator = new RandomShapeGenerator(Global.GRAMMAR_FILE);
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

        // Max time to spent for GE
        // convert time min to ms
        long limitTime = parameters.getMaxMiningTime() * 60000L;

        /* GRAMMATICAL EVOLUTIONARY */
        /* Parameters as the inputs of GE */
        logger.info("========================================================");
        logger.info("PARAMETERS AS THE INPUTS OF GE");
        logger.info("========================================================");
        logger.info("POPULATION SIZE: " + parameters.getPopulationSize());
        logger.info("LIMIT TIME: " + limitTime + " min.");
//        logger.info("TOTAL EFFORT : " + parameters.kBase);
        logger.info("# GENERATIONS: " + Math.ceil((double) parameters.getEffort() / parameters.getPopulationSize()));
        logger.info("LENGTH CHROMOSOME: " + parameters.getSizeChromosome());
        logger.info("MAXIMUM WRAPPING: " + parameters.getMaxWrap());
        logger.info("========================================================");
        logger.info("ELITE SELECTION RATE: " + parameters.getEliteSelectionRate());
        logger.info("TYPE SELECTION: " + TypeSelection.getLabel(parameters.getSelectionType()));
        logger.info("SELECTION RATE: " + parameters.getSelectionRate());
        if (parameters.getSelectionType() == TypeSelection.TOURNAMENT_SELECT) {
            logger.info("TOURNAMENT SELECTION RATE: " + parameters.getTournamentSelectionRate());
        }
//        int sizeElite = parameters.sizeElite * parameters.populationSize < 1 ?
//                1 : (int) (parameters.sizeElite * parameters.populationSize);
//        logger.info("SIZE ELITE: " + sizeElite);
        logger.info("========================================================");
        logger.info("TYPE CROSSOVER: " + TypeCrossover.getLabel(parameters.getCrossoverType()));
        logger.info("CROSSOVER PROBABILITY: " + parameters.getProCrossover());
        logger.info("========================================================");
        logger.info("TYPE MUTATION: " + TypeMutation.getLabel(parameters.getMutationType()));
        logger.info("MUTATION PROBABILITY: " + parameters.getProMutation());
        logger.info("========================================================");
        logger.info("TARGET SPARQL ENDPOINT: " + Global.TARGET_SPARQL_ENDPOINT);
//        logger.info("SPARQL TIMEOUT: " + (RDFMiner.parameters.sparqlTimeOut == 0 ? "Not used" : RDFMiner.parameters.sparqlTimeOut + " ms."));
//        logger.info("TIME-CAP: " + (RDFMiner.parameters.timeCap == 0 ? "Not used" : RDFMiner.parameters.timeCap + " min."));
        logger.info("NUMBER OF THREAD(S) USED: " + Global.NB_THREADS);
        logger.info("========================================================");
//        if(RDFMiner.parameters.checkpoint != 1) {
//            logger.info("# CHECKPOINT: " + RDFMiner.parameters.checkpoint);
//            logger.info("========================================================");
//        }

//        GEChromosome[] chromosomes = new GEChromosome[parameters.populationSize];
        ArrayList<GEIndividual> candidatePopulation;

        int curCheckpoint = 0;
        int curGeneration = 1;

        Cache cache = null;
        final String CACHE_PATH = RDFMiner.outputFolder + "/buffer_size" + parameters.getPopulationSize() + ".json";
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
        candidatePopulation = canPop.initialize(cache);
        // Initialize population
        ArrayList<Entity> entities = Fitness.initializePopulation(candidatePopulation, generator);

        // Stop Criterion
        StopCriterion stopCriterion;
        // select the way to stop GE
        switch (parameters.getStopCriterion()) {
            default:
            case 1:
                stopCriterion = new ClockWorldStop();
                break;
            case 2:
                stopCriterion = new EffortStop();
                break;
        }
        // start
        stopCriterion.start();
        // start GE
        EntityMining mining = new EntityMining();
        while (!stopCriterion.isFinish()) {
            System.out.println("\n--------------------------------------------------------\n");
            logger.info("Generation: " + stopCriterion.getCurGeneration());
            // running a generation ...
            entities = mining.run(generator, entities, stopCriterion.getCurGeneration());
            editCache(CACHE_PATH, entities, stopCriterion.getCurGeneration(), curCheckpoint);
            // reset crossover and mutation counter
            nCrossover = 0;
            nMutation = 0;
            // update
            stopCriterion.update();
        }
        // end of the process ...
        // fill content in json output file
        for(Entity entity : entities) {
            RDFMiner.content.add(entity.toJSON());
        }
        logger.info(RDFMiner.content.size() + " entities has been added in " + Global.RESULTS_FILENAME);
        logger.info("Evolutionary process is done...");
        System.exit(0);
    }

    public void editCache(String cachePath, ArrayList<Entity> entities, int curGeneration, int curCheckpoint) throws IOException {
        PrintWriter writer = new PrintWriter(cachePath, StandardCharsets.UTF_8);
        ArrayList<JSONObject> individualsJSON = new ArrayList<>();
        for(Entity entity : entities) {
            // fix chromosome content
            String genotype = entity.individual.getGenotype().get(0).toString().replace("Chromosome Contents: ", "");
            double fitness = entity.individual.getFitness().getDouble();
            individualsJSON.add(new IndividualJSON(genotype, fitness).toJSON());
        }
        Cache cache = new Cache(curGeneration, curCheckpoint, parameters.getSizeChromosome(), individualsJSON);
        writer.println(cache.toJSON().toString(2));
        writer.close();
    }

    public void writeAndFinish() {
        try {
            logger.info("Edit JSON file results ...");
            RDFMiner.results.statistics = RDFMiner.stats.toJSON();
//            RDFMiner.results.content = RDFMiner.content;
            // save entities
            sendEntities();
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

    public void sendEntities() {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            JSONObject toSend = new JSONObject();
            toSend.put(Results.USER_ID, parameters.getUserID());
            toSend.put(Results.PROJECT_NAME, parameters.getProjectName());
            toSend.put(Results.ENTITIES, RDFMiner.content);
            HttpPut put = new HttpPut(Endpoint.API_RESULTS);
            put.setEntity(new StringEntity(toSend.toString(), ContentType.APPLICATION_JSON));
            logger.info("PUT request: updating entities ...");
            HttpResponse response = httpClient.execute(put);
            logger.info("Status code: " + response.getStatusLine().getStatusCode());
            logger.info(new BasicResponseHandler().handleResponse(response));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
