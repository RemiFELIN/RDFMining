package com.i3s.app.rdfminer.evolutionary.mining;

import Individuals.GEChromosome;
import Util.Random.MersenneTwisterFast;
import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.RDFMiner;
import com.i3s.app.rdfminer.entity.Entity;
import com.i3s.app.rdfminer.evolutionary.fitness.novelty.NoveltySearch;
import com.i3s.app.rdfminer.generator.Generator;
import com.i3s.app.rdfminer.evolutionary.crossover.SinglePointCrossoverAxiom;
import com.i3s.app.rdfminer.evolutionary.crossover.SubtreeCrossoverAxioms;
import com.i3s.app.rdfminer.evolutionary.crossover.TwoPointCrossover;
import com.i3s.app.rdfminer.evolutionary.crossover.TypeCrossover;
import com.i3s.app.rdfminer.evolutionary.individual.GEIndividual;
import com.i3s.app.rdfminer.evolutionary.mutation.IntFlipMutation;
import com.i3s.app.rdfminer.evolutionary.tools.Crowding;
import com.i3s.app.rdfminer.sparql.corese.CoreseEndpoint;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.*;

public class Generation {

    private static final Logger logger = Logger.getLogger(Generation.class.getName());

    /**
     * To compute all tasks about crossover, mutation and evaluation phasis of
     * genetical algorithm
     *
     * @param canEntities        the candidate population
     * @param curGeneration the current generation
     * @param generator     an instance of {@link Generator Generator}
     * @return a new population
     */
    public static ArrayList<Entity> compute(ArrayList<Entity> canEntities, int curGeneration, Generator generator)
            throws InterruptedException, ExecutionException {

        ArrayList<Entity> evaluatedIndividuals = new ArrayList<>();
        // We have a set of threads to compute each tasks
        ExecutorService executor = Executors.newFixedThreadPool(Global.NB_THREADS);
        Set<Callable<ArrayList<Entity>>> entitiesCallables = new HashSet<>();
        logger.info("The entities will be evaluated using the following SPARQL Endpoint : " + Global.TRAINING_SPARQL_ENDPOINT);
        logger.info("Performing crossover and mutation ...");
//		List<Crowding> shapesToEvaluate = new ArrayList<>();
        int m = 0;
        // shuffle populations before crossover & mutation
        Collections.shuffle(canEntities);
        // selected entities for crossover-mutation-crowding
        ArrayList<Entity> selectedEntities = new ArrayList<>(canEntities);
        // process crossover and mutation 2 by 2
        int even = canEntities.size() % 2;
        while (m < canEntities.size() - even) {
            // get the two individuals which are neighbours
            GEIndividual parent1 = canEntities.get(m).individual;
            GEIndividual parent2 = canEntities.get(m + 1).individual;
            GEIndividual child1, child2;
            GEChromosome[] chromosomes;
            /* CROSSOVER PHASIS */
            switch (RDFMiner.parameters.typeCrossover) {
                case TypeCrossover.SINGLE_POINT_CROSSOVER:
                    // Single-point crossover
                    SinglePointCrossoverAxiom spc = new SinglePointCrossoverAxiom(RDFMiner.parameters.proCrossover,
                            new MersenneTwisterFast(), generator, curGeneration);
                    spc.setFixedCrossoverPoint(true);
//                    child1 = parent1;
//                    child2 = parent2;
                    GEIndividual[] childs = spc.doOperation(parent1, parent2);
                    child1 = childs[0];
                    child2 = childs[1];
//                    logger.info("---");
                    break;
                case TypeCrossover.SUBTREE_CROSSOVER:
                    // subtree crossover
                    SubtreeCrossoverAxioms sca = new SubtreeCrossoverAxioms(RDFMiner.parameters.proCrossover,
                            new MersenneTwisterFast());
                    GEIndividual[] inds = sca.crossoverTree(parent1, parent2);
                    child1 = inds[0];
                    child2 = inds[1];
                    break;
                default:
                    // Two point crossover
                    TwoPointCrossover tpc = new TwoPointCrossover(RDFMiner.parameters.proCrossover,
                            new MersenneTwisterFast());
                    tpc.setFixedCrossoverPoint(true);
                    chromosomes = tpc.crossover(
                            new GEChromosome((GEChromosome) parent1.getGenotype().get(0)),
                            new GEChromosome((GEChromosome) parent2.getGenotype().get(0))
                    );
                    child1 = generator.getIndividualFromChromosome(chromosomes[0], curGeneration);
                    child2 = generator.getIndividualFromChromosome(chromosomes[1], curGeneration);
                    break;
            }

            /* MUTATION PHASIS */
//			RandomNumberGenerator rand1 = new MersenneTwisterFast();
            IntFlipMutation mutation = new IntFlipMutation(RDFMiner.parameters.proMutation, new MersenneTwisterFast());
            // make mutation and return new childs from it
            GEIndividual newChild1 = mutation.doOperation(child1, generator, curGeneration, child1.getMutationPoints());
            GEIndividual newChild2 = mutation.doOperation(child2, generator, curGeneration, child2.getMutationPoints());
            // if using crowding method in survival selection
            if (RDFMiner.parameters.diversity == 1) {
                // if crowding is chosen, we need to compute and return the individuals chosen
                // (between parents and childs) in function of their fitness
                final int idx = m;
                entitiesCallables.add(() -> new Crowding(canEntities.get(idx), canEntities.get(idx + 1), newChild1,
                        newChild2, canEntities, generator).getSurvivalSelection());
            }
            selectedEntities.remove(canEntities.get(m));
            selectedEntities.remove(canEntities.get(m + 1));
            m = m + 2;
        }
        // fill entity that was not choosen for the crossover-mutation process
        if(!selectedEntities.isEmpty()) {
            logger.debug("The last entity will be added directly on population");
            evaluatedIndividuals.add(selectedEntities.get(0));
        }
        logger.info("Crossover & Mutation done");
        logger.info(entitiesCallables.size() + " tasks ready to be launched !");
        // Submit tasks
        List<Future<ArrayList<Entity>>> futureEntities = new ArrayList<>();
//        List<Future<ArrayList<Entity>>> futures = executor.invokeAll(entitiesCallables);
        // submit callables in order to assess them
        for(Callable<ArrayList<Entity>> call : entitiesCallables) {
            futureEntities.add(executor.submit(call));
        }
        // fill the evaluated individuals
        for (Future<ArrayList<Entity>> future : futureEntities) {
            try {
                if(RDFMiner.parameters.timeCap != 0) {
                    // we multiply the timecap by 2 to consider the maximum
                    // time-cap assessment for 2 childs
                    evaluatedIndividuals.addAll(future.get(2 * RDFMiner.parameters.timeCap, TimeUnit.MINUTES));
                } else {
                    evaluatedIndividuals.addAll(future.get());
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                logger.warn("Time-cap reached !");
            }
        }
        // Log how many axioms has been evaluated
        logger.info(evaluatedIndividuals.size() + " entities has been computed after crossover-mutation !");
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
        if(RDFMiner.parameters.useNoveltySearch) {
            // Compute the similarities of each axiom between them, and update the population
            NoveltySearch noveltySearch = new NoveltySearch(new CoreseEndpoint(Global.CORESE_IP, Global.TRAINING_SPARQL_ENDPOINT, Global.PREFIXES));
            try {
                evaluatedIndividuals = noveltySearch.update(evaluatedIndividuals);
            } catch (URISyntaxException | IOException e) {
                logger.error("Error during the computation of similarities ...");
                e.printStackTrace();
            }
        }
        // return the modified individuals
        return evaluatedIndividuals;
    }


//    public static void main(String[] args) {
//        int m = 0;
//        ArrayList<Integer> test = new ArrayList<>(Arrays.asList(1,2,3,4,2,4,5,6,7));
//        ArrayList<Integer> newTest = new ArrayList<>(test);
//        int even = test.size() % 2;
//        while(m < test.size() - even) {
//            System.out.println("test.get(" + m + ") = " + test.get(m));
//            System.out.println("test.get(" + (m+1) + ") = " + test.get(m+1));
//            newTest.remove(test.get(m));
//            newTest.remove(test.get(m+1));
//            m = m + 2;
//        }
//        if(!newTest.isEmpty()) {
//            System.out.println("# elem not selected : " + newTest.size());
//            System.out.println("elem : " + newTest.get(0));
//        }
//    }

}
