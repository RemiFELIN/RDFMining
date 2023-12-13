package com.i3s.app.rdfminer.evolutionary;

import com.i3s.app.rdfminer.Endpoint;
import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.RDFMiner;
import com.i3s.app.rdfminer.entity.Entity;
import com.i3s.app.rdfminer.evolutionary.fitness.Fitness;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.GEIndividual;
import com.i3s.app.rdfminer.evolutionary.tools.EAOperators;
import com.i3s.app.rdfminer.evolutionary.tools.EATools;
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
import java.util.*;
import java.util.concurrent.*;

public class EntityMining {

    private static final Logger logger = Logger.getLogger(EntityMining.class.getName());

//    private static long start;

    public static ArrayList<Entity> run(Generator generator, ArrayList<Entity> entities,
                                        int curGeneration, int curCheckpoint) {
        // compute execution time (in ns)
//        start = System.nanoTime();
        // checkpoint ?
//        boolean checkpointReached = (long) RDFMiner.parameters.populationSize * curGeneration >=
//                Math.round((double) (RDFMiner.parameters.kBase * (curCheckpoint + 1)) / RDFMiner.parameters.checkpoint);
        // Checkpoint reached, this is a code to evaluate and save axioms in output file
//        if(checkpointReached) {
//            ArrayList<Entity> originalPopulation = new ArrayList<>(entities);
//            if(RDFMiner.parameters.checkpoint != 1 && curCheckpoint != RDFMiner.parameters.checkpoint - 1) {
//                // INTERMEDIATE step (i.e. checkpoint)
//                logger.info("Checkpoint n°" + (curCheckpoint + 1) + " reached !");
//                // evaluate distinct genotype and avoid additional useless computation
//                ArrayList<Entity> newPopulation = Fitness.computePopulation(entities, generator);
//                // stats
//                setStats(originalPopulation, newPopulation, curGeneration);
//                // return final pop
//                return newPopulation;
//            } else {
//                // FINAL step
//                logger.info("Final assessment !");
//                // evaluate distinct genotype and avoid additional useless computation
//                ArrayList<Entity> newPopulation = Fitness.computePopulation(entities, generator);
//                // stats
//                setStats(originalPopulation, newPopulation, curGeneration);
//                // fill content in json output file
//                for(Entity entity : newPopulation) {
//                    // add this entity is its fitness is not equal to 0
////                    if(entity.individual.getFitness().getDouble() != 0) {
//                    RDFMiner.content.add(entity.toJSON());
////                    }
//                }
//                logger.info(RDFMiner.content.size() + " entities has been added in " + Global.RESULTS_FILENAME);
//                // return final pop
//                return newPopulation;
//            }
//        }
        // A list of individuals (from entities list)
        ArrayList<GEIndividual> entitiesI = new ArrayList<>();
        // Use list of individuals instead of list of entities
        // i.e. apply GE process directly on individuals
        for(Entity entity : entities) {
            entitiesI.add(entity.individual);
        }
        // Elites population
        logger.info("Searching the best individuals...");
        ArrayList<GEIndividual> elites = EAOperators.getElitesFromPopulation(entitiesI);
        for(GEIndividual elite : elites) {
            logger.debug("#elite: " + elite.getGenotype().get(0) + " ~ " + elite.getPhenotype().getStringNoSpace() + " ~ Fit= " + elite.getFitness().getDouble());
        }
        // Selected population (for replacement)
        logger.info("Selection of individuals...");
        ArrayList<GEIndividual> selected = EAOperators.getSelectionFromPopulation(entitiesI);
        // replacement phasis
        ArrayList<GEIndividual> replacement = Recombination.perform(generator, elites, selected);
        // Assessment phasis:
        // bind individuals and define fitness value for new individuals
        ArrayList<Entity> newPopulation = new ArrayList<>(EATools.bindIndividualsWithEntities(elites, entities));
        // iterate on replacement and assess it if the fitness value is null
        // i.e. instanciate an entity. We'll use the multy threads system
        // We have a set of threads to compute each tasks
        ExecutorService executor = Executors.newFixedThreadPool(Global.NB_THREADS);
        Set<Callable<Entity>> callables = new HashSet<>();
        for(GEIndividual individual : replacement) {
            if (individual.getFitness() == null) {
                callables.add(() -> Fitness.computeEntity(individual, generator));
            } else {
                logger.debug(individual.getGenotype().get(0) + ": its fitness is not null without assessment ???");
            }
        }
        logger.info(callables.size() + " tasks ready to be launched !");
        // Submit tasks
        List<Future<Entity>> futureEntities = new ArrayList<>();
//        List<Future<ArrayList<Entity>>> futures = executor.invokeAll(entitiesCallables);
        // submit callables in order to assess them
        for(Callable<Entity> callable : callables) {
            futureEntities.add(executor.submit(callable));
        }
        // fill the evaluated individuals
        for (Future<Entity> future : futureEntities) {
            try {
                if(RDFMiner.parameters.timeCap != 0) {
                    // we multiply the timecap by 2 to consider the maximum
                    // time-cap assessment for 2 childs
                    newPopulation.add(future.get(RDFMiner.parameters.timeCap, TimeUnit.MINUTES));
                } else {
                    newPopulation.add(future.get());
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                logger.warn("Time-cap reached !");
            }
        }
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                logger.debug("force the shutdown of executor ...");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }

        // Check if Novelty Search is enabled
//        if(RDFMiner.parameters.useNoveltySearch) {
//            // Compute the similarities of each axiom between them, and update the population
//            NoveltySearch noveltySearch = new NoveltySearch(new CoreseEndpoint(Global.TRAINING_SPARQL_ENDPOINT, Global.PREFIXES));
//            try {
//                evaluatedIndividuals = noveltySearch.update(evaluatedIndividuals);
//            } catch (URISyntaxException | IOException e) {
//                logger.error("Error during the computation of similarities ...");
//                e.printStackTrace();
//            }
//        }
        // stats
        setStats(entities, newPopulation, curGeneration);
//        logger.debug("size new pop= " + newPopulation.size());
        // renew population
        return newPopulation;
    }

    public static void setStats(ArrayList<Entity> originalPopulation, ArrayList<Entity> newPopulation, int curGeneration) {
//        for(Entity ent : newPopulation) {
//            logger.debug("newPop(i): " +
//                    Arrays.toString(ent.individual.getGenotype().get(0).toString().replace("Chromosome Contents: ", "").split(","))
//            + " ~ " + ent.individual.getPhenotype().getStringNoSpace());
//
//        }
        // set stats
        // get computation time in ms
        ArrayList<Long> durations = new ArrayList<>();
        for(Entity entity : newPopulation) {
            durations.add(entity.elapsedTime);
        }
        long duration = durations.stream().mapToLong(a -> a).sum();
        GenerationJSON generation = new GenerationJSON(originalPopulation, newPopulation, curGeneration, durations);
        // Log usefull stats concerning the algorithm evolution
        logger.info("Computation time: " + duration + " ms.");
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
            HttpPut put = new HttpPut(Endpoint.API_RESULTS);
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
