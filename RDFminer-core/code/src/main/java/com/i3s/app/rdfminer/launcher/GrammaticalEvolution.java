package com.i3s.app.rdfminer.launcher;

import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.Parameters;
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
import com.i3s.app.rdfminer.output.Results;
import org.apache.log4j.Logger;

import java.util.ArrayList;

public class GrammaticalEvolution {

    private static final Logger logger = Logger.getLogger(GrammaticalEvolution.class.getName());

    public static int nRecombinaison;

    public static int nCrossover;

    public static int nMutation;

    private Parameters parameters;

    private Results results;

    public GrammaticalEvolution() {
        this.results = Results.getInstance();
        this.parameters = Parameters.getInstance();
    }

    /**
     * Implementation of GE
     */
    public void run(Generator generator) {
        // log settings
        logUsedParameters(this.parameters);
        // Generate candidate population
        logger.info("Initializing candidate population ...");
        // init population as GEIndividuals
        ArrayList<GEIndividual> candidatePopulation = new CandidatePopulation(generator).initialize();
        // Mapping GEIndividuals as entities
        ArrayList<Entity> entities = Fitness.initializePopulation(candidatePopulation, generator);
        // Stop Criterion
        StopCriterion stopCriterion;
        // select the way to stop GE
        switch (this.parameters.getStopCriterion()) {
            default:
            case 1:
                // We stop the mining process based on the maximum time provided
                stopCriterion = new ClockWorldStop();
                break;
            case 2:
                // Effort determines directly the number of iterations of GE
                stopCriterion = new EffortStop();
                break;
        }
        // starting stop criterion (useful for clock-world stop option)
        stopCriterion.start();
        // start GE
        EntityMining mining = new EntityMining();
        while (!stopCriterion.isFinish()) {
            logger.info("===============");
            logger.info("Generation: " + stopCriterion.getCurGeneration());
            // running an iteration ...
            entities = mining.iterate(generator, entities, stopCriterion.getCurGeneration());
            // it means that the mining has been interrupted (exception or by calling /stop web service)
            // finishing GE and return current results
            if (entities == null) {
                break;
            }
//            editCache(CACHE_PATH, entities, stopCriterion.getCurGeneration(), curCheckpoint);
            // reset crossover and mutation counter
            nCrossover = 0;
            nMutation = 0;
            // update
            stopCriterion.update();
        }
        logger.info("===============");
        // end of the process ...
        // fill content in json output file
//        for(Entity entity : entities) {
//            RDFminer.content.add(entity.toJSON());
//        }
//        if (entities != null) {
//            this.results.setEntities(entities);
//        }
//        logger.info(RDFminer.content.size() + " entities has been added in " + Global.RESULTS_FILENAME);
        logger.info("Evolutionary process is done...");
        // System.exit(0);
    }

//    public void editCache(String cachePath, ArrayList<Entity> entities, int curGeneration, int curCheckpoint) throws IOException {
//        PrintWriter writer = new PrintWriter(cachePath, StandardCharsets.UTF_8);
//        ArrayList<JSONObject> individualsJSON = new ArrayList<>();
//        for(Entity entity : entities) {
//            // fix chromosome content
//            String genotype = entity.individual.getGenotype().get(0).toString().replace("Chromosome Contents: ", "");
//            double fitness = entity.individual.getFitness().getDouble();
//            individualsJSON.add(new IndividualJSON(genotype, fitness).toJSON());
//        }
//        Cache cache = new Cache(curGeneration, curCheckpoint, parameters.getSizeChromosome(), individualsJSON);
//        writer.println(cache.toJSON().toString(2));
//        writer.close();
//    }

//    public void writeAndFinish() {
//        try {
//            logger.info("Edit JSON file results ...");
//            RDFminer.results.statistics = RDFminer.stats.toJSON();
////            RDFMiner.results.content = RDFMiner.content;
//            // save entities
//            sendEntities();
//            RDFminer.output.write(RDFminer.results.toJSON().toString(2));
//            RDFminer.output.close();
//            // if novelty seach is used
//            if(RDFminer.similarityMap != null) {
//                RDFminer.similarityMap.editFile();
//            }
//        } catch (IOException e) {
//            logger.error("I/O error while closing JSON writer: " + e.getMessage());
//            e.printStackTrace();
//            System.exit(1);
//        }
//    }

//    public void sendEntities() {
//        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
//            JSONObject toSend = new JSONObject();
//            toSend.put(Results.USER_ID, parameters.getUserID());
//            toSend.put(Results.PROJECT_NAME, parameters.getProjectName());
//            toSend.put(Results.ENTITIES, RDFminer.content);
//            HttpPut put = new HttpPut(Endpoint.API_RESULTS);
//            put.setEntity(new StringEntity(toSend.toString(), ContentType.APPLICATION_JSON));
//            logger.info("PUT request: updating entities ...");
//            HttpResponse response = httpClient.execute(put);
//            logger.info("Status code: " + response.getStatusLine().getStatusCode());
//            logger.info(new BasicResponseHandler().handleResponse(response));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    private void logUsedParameters(Parameters parameters) {
        logger.info("=== RESOURCES SETTINGS ==============================");
        logger.info("SPARQL endpoint: " + Global.SPARQL_ENDPOINT);
        logger.info("named data graph: " + parameters.getNamedDataGraph());
        switch (parameters.getStopCriterion()) {
            case 1:
                logger.info("max. mining time: " + parameters.getMaxMiningTime() + " min.");
                break;
            case 2:
                logger.info("effort: " + parameters.getEffort());
                break;
        }
        logger.info("n. of thread(s) used: " + Global.NB_THREADS);
        //
        logger.info("=== POPULATION SETTINGS ==============================");
        logger.info("population size: " + parameters.getPopulationSize());
        logger.info("chromosomes length: " + parameters.getSizeChromosome());
        logger.info("max. wrapping: " + parameters.getMaxWrap());
        //
        logger.info("=== SELECTION SETTINGS ==============================");
        logger.info("elite rate: " + parameters.getEliteSelectionRate());
        logger.info("type selection (recombination): " + TypeSelection.getLabel(parameters.getSelectionType()));
        logger.info("selection rate: " + parameters.getSelectionRate());
        if (parameters.getSelectionType() == TypeSelection.TOURNAMENT_SELECT) {
            logger.info("tournament selection rate " + parameters.getTournamentSelectionRate());
        }
        //
        logger.info("=== OPERATORS SETTINGS ==============================");
        logger.info("type crossover: " + TypeCrossover.getLabel(parameters.getCrossoverType()));
        logger.info("prob. crossover: " + parameters.getProCrossover());
        logger.info("type mutation: " + TypeMutation.getLabel(parameters.getMutationType()));
        logger.info("prob. mutation: " + parameters.getProMutation());
        logger.info("=====================================================");
    }

}
