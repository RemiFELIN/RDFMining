package com.i3s.app.rdfminer.evolutionary.mining;

import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.RDFMiner;
import com.i3s.app.rdfminer.entity.Entity;
import com.i3s.app.rdfminer.evolutionary.TypeCrossover;
import com.i3s.app.rdfminer.evolutionary.TypeMutation;
import com.i3s.app.rdfminer.evolutionary.fitness.novelty.NoveltySearch;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.GEIndividual;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.Populations.SimplePopulation;
import com.i3s.app.rdfminer.evolutionary.geva.Operator.CrossoverModule;
import com.i3s.app.rdfminer.evolutionary.geva.Operator.Operations.ContextSensitiveOperations.NodalMutation;
import com.i3s.app.rdfminer.evolutionary.geva.Operator.Operations.ContextSensitiveOperations.StructuralMutation;
import com.i3s.app.rdfminer.evolutionary.geva.Operator.Operations.ContextSensitiveOperations.SubtreeCrossover;
import com.i3s.app.rdfminer.evolutionary.geva.Operator.Operations.ContextSensitiveOperations.SubtreeMutation;
import com.i3s.app.rdfminer.evolutionary.geva.Operator.Operations.IntFlipByteMutation;
import com.i3s.app.rdfminer.evolutionary.geva.Operator.Operations.IntFlipMutation;
import com.i3s.app.rdfminer.evolutionary.geva.Operator.Operations.SinglePointCrossover;
import com.i3s.app.rdfminer.evolutionary.geva.Operator.Operations.TwoPointCrossover;
import com.i3s.app.rdfminer.evolutionary.geva.Util.Random.MersenneTwisterFast;
import com.i3s.app.rdfminer.evolutionary.tools.Crowding;
import com.i3s.app.rdfminer.generator.Generator;
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
            ArrayList<GEIndividual> parents = new ArrayList<>(List.of(canEntities.get(m).individual, canEntities.get(m + 1).individual));
            // prepare crossover module
            CrossoverModule crossoverModule = null;
//            GEChromosome[] chromosomes;
            /* CROSSOVER PHASIS */
            switch (RDFMiner.parameters.typeCrossover) {
                default:
                case TypeCrossover.SINGLE_POINT:
                    // Single-point crossover
                    SinglePointCrossover spc = new SinglePointCrossover(new MersenneTwisterFast(), RDFMiner.parameters.proCrossover);
                    spc.setFixedCrossoverPoint(true);
                    crossoverModule = new CrossoverModule(new MersenneTwisterFast(), spc);
                    break;
                case TypeCrossover.TWO_POINT:
                    // Two point crossover
                    TwoPointCrossover tpc = new TwoPointCrossover(new MersenneTwisterFast(), RDFMiner.parameters.proCrossover);
                    tpc.setFixedCrossoverPoint(true);
                    crossoverModule = new CrossoverModule(new MersenneTwisterFast(), tpc);
                    break;
                case TypeCrossover.SUBTREE:
                    // subtree crossover
                    // special implementation due to the original implementation by GEVA developers
                    SubtreeCrossover stc = new SubtreeCrossover(new MersenneTwisterFast(), RDFMiner.parameters.proCrossover);
                    stc.doOperation(parents);
                    break;
            }
            // perform crossover
            if(RDFMiner.parameters.typeCrossover != TypeCrossover.SUBTREE && crossoverModule != null) {
                Generation.performCrossover(parents, crossoverModule);
            }
            /* MUTATION PHASIS */
            switch (RDFMiner.parameters.typeMutation) {
                default:
                case TypeMutation.INT_FLIP:
                    IntFlipMutation ifm = new IntFlipMutation(new MersenneTwisterFast(), RDFMiner.parameters.proMutation);
                    ifm.doOperation(parents);
                    break;
                case TypeMutation.NODAL:
                    NodalMutation nm = new NodalMutation(new MersenneTwisterFast(), RDFMiner.parameters.proMutation);
                    nm.doOperation(parents);
                    break;
                case TypeMutation.SUBTREE:
                    SubtreeMutation sm = new SubtreeMutation(new MersenneTwisterFast(), RDFMiner.parameters.proMutation);
                    sm.doOperation(parents);
                    break;
                case TypeMutation.STRUCTURAL:
                    StructuralMutation stm = new StructuralMutation(new MersenneTwisterFast(), RDFMiner.parameters.proMutation);
                    stm.doOperation(parents);
                    break;
                case TypeMutation.INT_FLIP_BYTE:
                    IntFlipByteMutation ifbm = new IntFlipByteMutation(new MersenneTwisterFast(), RDFMiner.parameters.proMutation);
                    ifbm.doOperation(parents);
                    break;
            }
            // After crossover and mutation phasis; each parent is directly modified and gives an offspring
            // if using crowding method in survival selection
            if (RDFMiner.parameters.diversity == 1) {
                // if crowding is chosen, we need to compute and return the individuals chosen
                // (between parents and childs) in function of their fitness
                final int idx = m;
                entitiesCallables.add(() -> new Crowding(canEntities.get(idx), canEntities.get(idx + 1), parents.get(0),
                        parents.get(1), canEntities, generator).getSurvivalSelection());
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

    private static void performCrossover(ArrayList<GEIndividual> individuals, CrossoverModule cm) {
        SimplePopulation p = new SimplePopulation();
        for(GEIndividual i : individuals) {
            p.add(i);
        }
        cm.setPopulation(p);
        cm.perform();
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
