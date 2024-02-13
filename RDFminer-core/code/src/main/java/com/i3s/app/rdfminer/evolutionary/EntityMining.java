package com.i3s.app.rdfminer.evolutionary;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.i3s.app.rdfminer.Endpoint;
import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.Parameters;
import com.i3s.app.rdfminer.entity.Entity;
import com.i3s.app.rdfminer.evolutionary.fitness.Fitness;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.GEIndividual;
import com.i3s.app.rdfminer.evolutionary.tools.EAOperators;
import com.i3s.app.rdfminer.evolutionary.tools.EATools;
import com.i3s.app.rdfminer.generator.Generator;
import com.i3s.app.rdfminer.output.Generation;
import com.i3s.app.rdfminer.output.Results;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

public class EntityMining {

    private static final Logger logger = Logger.getLogger(EntityMining.class.getName());

    public ArrayList<Entity> iterate(Generator generator, ArrayList<Entity> entities, int curGeneration) {
        Parameters parameters = Parameters.getInstance();
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
        EAOperators operators = new EAOperators();
        ArrayList<GEIndividual> selected = operators.getSelectionFromPopulation(entitiesI);
        // replacement phasis
        Recombination recombination = new Recombination();
        ArrayList<GEIndividual> replacement = recombination.perform(generator, elites, selected);
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
                if(parameters.timeCap != 0) {
                    // we multiply the timecap by 2 to consider the maximum
                    // time-cap assessment for 2 childs
                    newPopulation.add(future.get(parameters.timeCap, TimeUnit.MINUTES));
                } else {
                    newPopulation.add(future.get());
                }
            } catch (InterruptedException | ExecutionException e) {
                logger.warn("The mining has been interrupted !");
                executor.shutdown();
                return null;
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
        // update results and send it to the RDFminer-server
        updateResults(entities, newPopulation, curGeneration);
        return newPopulation;
    }

    private void updateResults(ArrayList<Entity> originalPopulation, ArrayList<Entity> newPopulation, int curGeneration) {
        Results results = Results.getInstance();
        Generation generation = new Generation(originalPopulation, newPopulation, curGeneration);
        results.addGeneration(generation);
        results.setEntities(newPopulation);
        // Log usefull stats concerning the algorithm evolution
        logGenerationInfo(generation);
        // send generations to the server
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // PUT request
            HttpPut put = new HttpPut(Endpoint.API_RESULTS);
            // Mapping Results instance in JSON string using Jackson
            String updated = new ObjectMapper().writeValueAsString(results);
            put.setEntity(new StringEntity(updated, ContentType.APPLICATION_JSON));
            logger.info("/PUT - update results ...");
            HttpResponse response = httpClient.execute(put);
            logger.info("Status code: " + response.getStatusLine().getStatusCode());
            logger.info(new BasicResponseHandler().handleResponse(response));
        } catch (IOException e) {
            logger.warn("RDFminer-server is offline !");
        }
    }

    private void logGenerationInfo(Generation generation) {
        logger.info("sum. computation time: " + generation.getComputationTime() + " ms.");
        logger.info("avg. fitness: " + generation.getAverageFitness());
        logger.info("diversity coefficient: " + (generation.getDiversityCoefficient() * 100) + "%");
        logger.info("population development rate: " + (generation.getPopulationDevelopmentRate() * 100) + "%");
        logger.info("Number of individual(s) with a non-null fitness: " + generation.getNumIndividualsWithNonNullFitness());
    }

}
