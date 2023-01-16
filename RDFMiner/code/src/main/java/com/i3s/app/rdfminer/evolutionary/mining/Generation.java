package com.i3s.app.rdfminer.evolutionary.mining;

import Individuals.GEChromosome;
import Util.Random.MersenneTwisterFast;
import Util.Random.RandomNumberGenerator;
import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.RDFMiner;
import com.i3s.app.rdfminer.entity.Entity;
import com.i3s.app.rdfminer.generator.Generator;
import com.i3s.app.rdfminer.evolutionary.crossover.SinglePointCrossoverAxiom;
import com.i3s.app.rdfminer.evolutionary.crossover.SubtreeCrossoverAxioms;
import com.i3s.app.rdfminer.evolutionary.crossover.TwoPointCrossover;
import com.i3s.app.rdfminer.evolutionary.crossover.TypeCrossover;
import com.i3s.app.rdfminer.evolutionary.individual.GEIndividual;
import com.i3s.app.rdfminer.evolutionary.mutation.IntFlipMutation;
import com.i3s.app.rdfminer.evolutionary.tools.Crowding;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

        while (m <= canEntities.size() - 2) {
            RandomNumberGenerator rand = new MersenneTwisterFast();
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
                            rand, generator, curGeneration);
                    spc.setFixedCrossoverPoint(true);
                    child1 = parent1;
                    child2 = parent2;
                    GEIndividual[] childs = spc.doOperation(child1, child2);
                    child1 = childs[0];
                    child2 = childs[1];
                    logger.info("---");
                    break;
                case TypeCrossover.SUBTREE_CROSSOVER:
                    // subtree crossover
                    SubtreeCrossoverAxioms sca = new SubtreeCrossoverAxioms(RDFMiner.parameters.proCrossover, rand);
                    GEIndividual[] inds = sca.crossoverTree(parent1, parent2);
                    child1 = inds[0];
                    child2 = inds[1];
                    break;
                default:
                    // Two point crossover
                    TwoPointCrossover tpc = new TwoPointCrossover(RDFMiner.parameters.proCrossover, rand);
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
//				logger.info("CROWDING diversity method used ...");
                // fill callables of crowding to compute
                final int idx = m;
                entitiesCallables.add(() -> new Crowding(canEntities.get(idx), canEntities.get(idx + 1), newChild1, newChild2, generator)
                        .getSurvivalSelection());
            }
            m = m + 2;
        }
        logger.info("Crossover & Mutation done");
        logger.info(entitiesCallables.size() + " tasks ready to be launched !");
        // Submit tasks
        List<Future<ArrayList<Entity>>> futures = executor.invokeAll(entitiesCallables);
        // fill the evaluated individuals
        for (Future<ArrayList<Entity>> future : futures) {
            evaluatedIndividuals.addAll(future.get());
        }
        // Shutdown the service
        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        // return the modified individuals
        return evaluatedIndividuals;
    }


}
