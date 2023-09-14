package com.i3s.app.rdfminer.evolutionary;

import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.RDFMiner;
import com.i3s.app.rdfminer.entity.Entity;
import com.i3s.app.rdfminer.evolutionary.fitness.Fitness;
import com.i3s.app.rdfminer.evolutionary.generation.Generation;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.GEIndividual;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.Individual;
import com.i3s.app.rdfminer.evolutionary.geva.Operator.selection.EliteOperationSelection;
import com.i3s.app.rdfminer.evolutionary.geva.Operator.selection.ProportionalRouletteWheel;
import com.i3s.app.rdfminer.evolutionary.geva.Operator.selection.ScaledRouletteWheel;
import com.i3s.app.rdfminer.evolutionary.geva.Operator.selection.TournamentSelect;
import com.i3s.app.rdfminer.evolutionary.tools.EATools;
import com.i3s.app.rdfminer.evolutionary.types.TypeSelection;
import com.i3s.app.rdfminer.generator.Generator;
import com.i3s.app.rdfminer.output.GenerationJSON;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class EntityMining {

    private static final Logger logger = Logger.getLogger(EntityMining.class.getName());

    private static long start;

    public static ArrayList<Entity> run(Generator generator, ArrayList<Entity> entities,
                                        int curGeneration, int curCheckpoint)
            throws ExecutionException, InterruptedException {
        // compute execution time (in ns)
        start = System.nanoTime();
        // set a save of original population
        // to see how GE will modify it

        // set size selection
//        int sizeSelection = (int) (RDFMiner.parameters.sizeSelection * RDFMiner.parameters.populationSize);
//        int sizeElite = RDFMiner.parameters.sizeElite * RDFMiner.parameters.populationSize < 1 ?
//                1 : (int) (RDFMiner.parameters.sizeElite * RDFMiner.parameters.populationSize);
        // Checkpoint reached, this is a code to evaluate and save axioms in output file
        if((long) RDFMiner.parameters.populationSize * curGeneration >=
                Math.round((double) (RDFMiner.parameters.kBase * (curCheckpoint + 1)) / RDFMiner.parameters.checkpoint)) {
            ArrayList<Entity> originalPopulation = new ArrayList<>(entities);
            if(RDFMiner.parameters.checkpoint != 1 && curCheckpoint != RDFMiner.parameters.checkpoint - 1) {
                // INTERMEDIATE step (i.e. checkpoint)
                logger.info("Checkpoint n°" + (curCheckpoint + 1) + " reached !");
                // evaluate distinct genotype and avoid additional useless computation
                ArrayList<Entity> newPopulation = Fitness.computePopulation(entities, generator);
                // stats
                setStats(originalPopulation, newPopulation, curGeneration);
                // return final pop
                return newPopulation;
            } else {
                // FINAL step
                logger.info("Final assessment !");
                // evaluate distinct genotype and avoid additional useless computation
                ArrayList<Entity> newPopulation = Fitness.computePopulation(entities, generator);
                // stats
                setStats(originalPopulation, newPopulation, curGeneration);
                // fill content in json output file
                for(Entity entity : newPopulation) {
                    // add this entity is its fitness is not equal to 0
//                    if(entity.individual.getFitness().getDouble() != 0) {
                    RDFMiner.content.add(entity.toJSON());
//                    }
                }
                logger.info(RDFMiner.content.size() + " entities has been added in " + Global.RESULTS_FILENAME);
                // return final pop
                return newPopulation;
            }
        }
        // STEP 3 - SELECTION OPERATION
//        logger.debug("entities.size= " + entities.size());
        ArrayList<GEIndividual> entitiesAsIndividuals = new ArrayList<>();
        ArrayList<GEIndividual> selectedIndividuals = new ArrayList<>();
        // Use list of individuals instead of list of entities
        // i.e. apply GE process directly on individuals
        for(Entity entity : entities) {
            entitiesAsIndividuals.add(entity.individual);
        }
        /* SELECTION */
        switch(RDFMiner.parameters.typeSelection) {
            default:
            case TypeSelection.ELITE_OPERATION_SELECTION:
                EliteOperationSelection eos = new EliteOperationSelection();
                eos.doOperation(entitiesAsIndividuals);
//                logger.debug("getSelectedPop.size= " + eos.getSelectedPopulation().size());
                for(Individual selected : eos.getSelectedPopulation().getAll()) {
                    selectedIndividuals.add((GEIndividual) selected);
//                    entitiesAsIndividuals.remove((GEIndividual) selected);
                }
                break;
            case TypeSelection.PROPORTIONAL_ROULETTE_WHEEL:
                ProportionalRouletteWheel prw = new ProportionalRouletteWheel();
                prw.doOperation(entitiesAsIndividuals);
                for(Individual selected : prw.getSelectedPopulation().getAll()) {
                    selectedIndividuals.add((GEIndividual) selected);
//                    entitiesAsIndividuals.remove((GEIndividual) selected);
                }
                break;
            case TypeSelection.SCALED_ROULETTE_WHEEL:
                ScaledRouletteWheel srw = new ScaledRouletteWheel();
                srw.doOperation(entitiesAsIndividuals);
                for(Individual selected : srw.getSelectedPopulation().getAll()) {
                    selectedIndividuals.add((GEIndividual) selected);
//                    entitiesAsIndividuals.remove((GEIndividual) selected);
                }
                break;
            case TypeSelection.TOURNAMENT_SELECT:
                TournamentSelect ts = new TournamentSelect();
                ts.doOperation(entitiesAsIndividuals);
                for(Individual selected : ts.getSelectedPopulation().getAll()) {
                    selectedIndividuals.add((GEIndividual) selected);
//                    entitiesAsIndividuals.remove((GEIndividual) selected);
                }
                break;
        }
        /* STEP 4 - CROSSOVER & MUTATION OPERATION */
//        logger.debug("selectedIndividuals.size= " + selectedIndividuals.size());
//        logger.debug("entitiesAsIndividuals.size= " + entitiesAsIndividuals.size());
        ArrayList<Entity> selectedEntities = EATools.bindIndividualsWithEntities(selectedIndividuals, entities);
        logger.debug("Selected individuals:");
        for(Entity selected : selectedEntities) {
            logger.debug(selected.individual.getGenotype() + ": " + selected.individual.getPhenotype().getStringNoSpace());
        }
        // individuals to compute
//        ArrayList<Entity> toCompute = EATools.bindIndividualsWithEntities(entitiesAsIndividuals, entities);
//        logger.debug("selectedEntities.size= " + selectedEntities.size());
//        logger.debug("toCompute.size= " + toCompute.size());
        // Compute GE and add new population on a new list of individuals

        ArrayList<Entity> newPopulation = Generation.compute(entities, curGeneration, generator);
//        logger.debug("size computed pop = " + computedPopulation.size());
        // set new population
        newPopulation.addAll(selectedEntities);// EATools.renew(curGeneration, computedPopulation, selectedEntities);
//        logger.debug("size new pop = " + newPopulation.size());
        // stats
        setStats(entities, newPopulation, curGeneration);
//        logger.debug("size new pop= " + newPopulation.size());
        // renew population
        return newPopulation;
    }

    public static void setStats(ArrayList<Entity> originalPopulation, ArrayList<Entity> newPopulation, int curGeneration) {
        for(Entity ent : newPopulation) {
            logger.debug("newPop(i): " + ent.individual.getPhenotype().getStringNoSpace());
        }
        // set stats
        GenerationJSON generation = new GenerationJSON();
        // get computation time in ms
        long duration = (System.nanoTime() - start) / 1000000;
        generation.setGenerationJSON(originalPopulation, newPopulation, curGeneration, duration);
        // Log usefull stats concerning the algorithm evolution
        logger.info("Computation time: " + generation.averageFitness + "s");
        logger.info("Average fitness: " + generation.averageFitness);
        logger.info("Diversity coefficient: " + (generation.diversityCoefficient * 100) + "%");
        logger.info("Population development rate: " + (generation.populationDevelopmentRate * 100) + "%");
        logger.info("Number of individual(s) with a non-null fitness: " + generation.numIndividualsWithNonNullFitness);
        RDFMiner.stats.generations.put(generation.toJSON());
        // send generations to the server
        sendGenerations();
    }

    public static void sendGenerations() {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            JSONObject toSend = new JSONObject();
            toSend.put(Results.USER_ID, RDFMiner.parameters.username);
            toSend.put(Results.PROJECT_NAME, RDFMiner.parameters.directory);
            toSend.put(Results.STATISTICS, RDFMiner.stats.toJSON());
            //
            HttpPut put = new HttpPut(Global.RDFMINER_SERVER_IP + "api/results");
//            System.out.println("update generations:");
//            System.out.println(toSend.toString(2));
            put.setEntity(new StringEntity(toSend.toString(), ContentType.APPLICATION_JSON));
            logger.info("PUT request: updating generations ...");
            HttpResponse response = httpClient.execute(put);
            logger.info("Status code: " + response.getStatusLine().getStatusCode());
            logger.info(new BasicResponseHandler().handleResponse(response));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
