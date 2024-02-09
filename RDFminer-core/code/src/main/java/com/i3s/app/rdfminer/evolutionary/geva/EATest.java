//package com.i3s.app.rdfminer.evolutionary.geva;
//
//import com.i3s.app.rdfminer.RDFMiner;
//import com.i3s.app.rdfminer.evolutionary.geva.Individuals.FitnessPackage.BasicFitness;
//import com.i3s.app.rdfminer.evolutionary.geva.Individuals.GEChromosome;
//import com.i3s.app.rdfminer.evolutionary.geva.Individuals.GEIndividual;
//import com.i3s.app.rdfminer.evolutionary.geva.Individuals.Individual;
//import com.i3s.app.rdfminer.evolutionary.geva.Operator.crossover.SinglePointCrossover;
//import com.i3s.app.rdfminer.evolutionary.geva.Operator.crossover.SubtreeCrossover;
//import com.i3s.app.rdfminer.evolutionary.geva.Operator.crossover.SwapCrossover;
//import com.i3s.app.rdfminer.evolutionary.geva.Operator.crossover.TwoPointCrossover;
//import com.i3s.app.rdfminer.evolutionary.geva.Operator.mutation.IntFlipByteMutation;
//import com.i3s.app.rdfminer.evolutionary.geva.Operator.mutation.IntFlipMutation;
//import com.i3s.app.rdfminer.evolutionary.geva.Operator.mutation.NodalMutation;
//import com.i3s.app.rdfminer.evolutionary.geva.Operator.mutation.SubtreeMutation;
//import com.i3s.app.rdfminer.evolutionary.geva.Operator.selection.EliteOperationSelection;
//import com.i3s.app.rdfminer.evolutionary.geva.Operator.selection.ProportionalRouletteWheel;
//import com.i3s.app.rdfminer.evolutionary.geva.Operator.selection.ScaledRouletteWheel;
//import com.i3s.app.rdfminer.evolutionary.geva.Operator.selection.TournamentSelect;
//import com.i3s.app.rdfminer.evolutionary.individual.CandidatePopulation;
//import com.i3s.app.rdfminer.evolutionary.types.TypeCrossover;
//import com.i3s.app.rdfminer.evolutionary.types.TypeMutation;
//import com.i3s.app.rdfminer.evolutionary.types.TypeSelection;
//import com.i3s.app.rdfminer.generator.Generator;
//import com.i3s.app.rdfminer.generator.shacl.RandomShapeGenerator;
//import com.i3s.app.rdfminer.launcher.GrammaticalEvolution;
//
//import java.io.IOException;
//import java.net.URISyntaxException;
//import java.util.*;
//
//public class EATest {
//
//    public static ArrayList<GEIndividual> createInitialPopulation(Generator generator) {
//        ArrayList<GEIndividual> population = new CandidatePopulation(generator).initialize(null);
//        // simulation: fitness
//        ArrayList<Integer> fitness = new ArrayList<>(List.of(1,5,7,2,11,3,15,9,2,20));
//        for (int i=0; i<population.size(); i++) {
//            population.get(i).setFitness(new BasicFitness(fitness.get(i), population.get(i)));
//        }
//        return population;
//    }
//
//    public static ArrayList<GEIndividual> getElitePopulation(ArrayList<GEIndividual> population) {
//        ArrayList<GEIndividual> elites = new ArrayList<>();
//        // elite selection
//        EliteOperationSelection eos = new EliteOperationSelection();
//        eos.doOperation(population);
//        for(Individual selected : eos.getSelectedPopulation().getAll()) {
//            elites.add((GEIndividual) selected);
//        }
//        return elites;
//    }
//
//    public static ArrayList<GEIndividual> getSelectedIndividualsForReplacement(ArrayList<GEIndividual> population) {
////        int size = (int) (RDFMiner.parameters.sizeSelectedPop * RDFMiner.parameters.populationSize);
////        RandomNumberGenerator rng = new MersenneTwisterFast();
//        //
//        ArrayList<GEIndividual> selectedPopulation = new ArrayList<>();
//        switch(RDFMiner.parameters.selectionType) {
//            default:
//            case TypeSelection.PROPORTIONAL_ROULETTE_WHEEL:
//                ProportionalRouletteWheel prw = new ProportionalRouletteWheel();
//                prw.doOperation(population);
//                for(Individual selected : prw.getSelectedPopulation().getAll()) {
//                    selectedPopulation.add((GEIndividual) selected);
////                    entitiesAsIndividuals.remove((GEIndividual) selected);
//                }
//                break;
//            case TypeSelection.SCALED_ROULETTE_WHEEL:
//                ScaledRouletteWheel srw = new ScaledRouletteWheel();
//                srw.doOperation(population);
//                for(Individual selected : srw.getSelectedPopulation().getAll()) {
//                    selectedPopulation.add((GEIndividual) selected);
////                    entitiesAsIndividuals.remove((GEIndividual) selected);
//                }
//                break;
//            case TypeSelection.TOURNAMENT_SELECT:
//                TournamentSelect ts = new TournamentSelect();
//                ts.doOperation(population);
//                for(Individual selected : ts.getSelectedPopulation().getAll()) {
//                    selectedPopulation.add((GEIndividual) selected);
////                    entitiesAsIndividuals.remove((GEIndividual) selected);
//                }
//                break;
//        }
//        return selectedPopulation;
//    }
//
//    public static void crossover(ArrayList<GEIndividual> couple) {
//        switch (RDFMiner.parameters.crossoverType) {
//            default:
//            case TypeCrossover.SINGLE_POINT:
//                // Single-point crossover
//                SinglePointCrossover spc = new SinglePointCrossover();
//                spc.setFixedCrossoverPoint(true);
//                spc.doOperation(couple);
//                break;
//            case TypeCrossover.TWO_POINT:
//                // Two point crossover
//                TwoPointCrossover tpc = new TwoPointCrossover();
//                tpc.setFixedCrossoverPoint(true);
//                tpc.doOperation(couple);
//                break;
//            case TypeCrossover.SUBTREE:
//                // subtree crossover
//                // special implementation due to the original implementation by GEVA developers
//                SubtreeCrossover stc = new SubtreeCrossover();
//                stc.doOperation(couple);
//                break;
//            case TypeCrossover.SWAP:
//                // Swap crossover
//                // contribution testing for ShaMPA
//                SwapCrossover swp = new SwapCrossover();
//                swp.doOperation(couple);
//                break;
//        }
//    }
//
//    public static void mutation(ArrayList<GEIndividual> couple) {
//        switch (RDFMiner.parameters.mutationType) {
//            default:
//            case TypeMutation.INT_FLIP:
//                IntFlipMutation ifm = new IntFlipMutation();
//                ifm.doOperation(couple);
//                break;
//            case TypeMutation.NODAL:
//                NodalMutation nm = new NodalMutation();
//                nm.doOperation(couple);
//                break;
//            case TypeMutation.SUBTREE:
//                SubtreeMutation sm = new SubtreeMutation();
//                sm.doOperation(couple);
//                break;
//            case TypeMutation.INT_FLIP_BYTE:
//                IntFlipByteMutation ifbm = new IntFlipByteMutation();
//                ifbm.doOperation(couple);
//                break;
//        }
//    }
//
//    public static boolean isInPopulation(GEIndividual individual, ArrayList<GEIndividual> population) {
//        for (GEIndividual i : population) {
//            if (Objects.equals(individual.getGenotype().toString(), i.getGenotype().toString())) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public static ArrayList<GEIndividual> performOperators(Generator generator, ArrayList<GEIndividual> elites, ArrayList<GEIndividual> selectedIndividuals) {
//        ArrayList<GEIndividual> newPopulation = new ArrayList<>(elites);
////        System.out.println(newPopulation.size());
//        // while the new population size is not equals to the initial one
//        int phasis = 0;
//        while (newPopulation.size() != RDFMiner.parameters.populationSize)  {
//            // shuffle selected individuals
////            Collections.shuffle(selectedIndividuals);
//            Random rand = new Random();
//            // crossover and mutation
//            // 2 by 2
//            ArrayList<GEIndividual> couple = new ArrayList<>();
//            int firstIdx = rand.nextInt(selectedIndividuals.size());
//            int secondIdx = firstIdx;
//            while (secondIdx == firstIdx) {
//                secondIdx = rand.nextInt(selectedIndividuals.size());
//            }
//            GEChromosome chromParent1 = selectedIndividuals.get(firstIdx).getChromosomes();
//            GEChromosome chromParent2 = selectedIndividuals.get(secondIdx).getChromosomes();
//            couple.add(generator.getIndividualFromChromosome(chromParent1));
//            couple.add(generator.getIndividualFromChromosome(chromParent2));
////            System.out.println("(0): " + firstIdx + " (1): " + secondIdx);
//            // crossover
//            crossover(couple);
//            // mutation
//            mutation(couple);
//            // adding the new individuals
//            for(GEIndividual offspring : couple) {
//                // !newPopulation.contains(offspring) && !elites.contains(offspring)
//                if (!isInPopulation(offspring, newPopulation) && !isInPopulation(offspring, elites) &&
//                        newPopulation.size() != RDFMiner.parameters.populationSize) {
////                    System.out.println("new!");
//                    newPopulation.add(offspring);
//                }
//            }
//            phasis++;
//        }
//        // return new population
//        System.out.println(">>> " + phasis + " phasis has been required to perform replacement !");
//        System.out.println(">>> " + GrammaticalEvolution.nCrossover + " crossover(s) and " + GrammaticalEvolution.nMutation + " mutation(s)");
//        return newPopulation;
//    }
//
//    public static double getDifferenceRatio(ArrayList<GEIndividual> init, ArrayList<GEIndividual> fin) {
//        if (init.size() != fin.size()) {
//            return 0;
//        }
//        Set<String> distinctsGenotype = new HashSet<>();
//        for (GEIndividual initI : init) {
//            distinctsGenotype.add(initI.getGenotype().toString());
//        }
//        for (GEIndividual finI : fin) {
//            distinctsGenotype.add(finI.getGenotype().toString());
//        }
//        System.out.println("[size distincts] ~ " + distinctsGenotype.size());
//        return (double) (distinctsGenotype.size() - init.size()) * 100 / init.size();
//    }
//
//    public static void execRefonte() {
//        // init
//        RDFMiner.parameters.selectionRate = 0.5;
//        RDFMiner.parameters.eliteSelectionRate = 0.2;
//        RDFMiner.parameters.sizeChromosome = 2;
//        RDFMiner.parameters.populationSize = 10;
//        RDFMiner.parameters.selectionType = 4;
//        RDFMiner.parameters.tournamentSelectionRate = 0.3;
//        RDFMiner.parameters.proCrossover = 0.01;
//        RDFMiner.parameters.proMutation = 0.01;
//        Generator generator = null;
//        try {
//            generator = new RandomShapeGenerator("/user/rfelin/home/projects/RDFMining/IO/users/64e7594dfbb9f24cf6d7c272/test/grammar.bnf");
//        } catch (URISyntaxException | IOException e) {
//            e.printStackTrace();
//        }
//        // create a population
//        ArrayList<GEIndividual> initialPopulation = EATest.createInitialPopulation(generator);
//        System.out.println("~~~");
//        for(GEIndividual individual: initialPopulation) {
//            System.out.println(individual.getChromosomes() + " -> " + individual.getFitness().getDouble());
//        }
//        // extract elite pop
//        System.out.println("~~~\nElites:\n");
//        ArrayList<GEIndividual> elites = EATest.getElitePopulation(initialPopulation);
//        for(GEIndividual elite : elites) {
//            System.out.println(elite.getChromosomes() + " -> " + elite.getFitness().getDouble());
//        }
//        // extract other individuals selected for replacement
//        System.out.println("~~~\nSelected for replacement: (Tournament)\n");
//        // 2: Proportional Roulette Wheel; 3: Scaled Roulette Wheel; 4: Tournament
//        ArrayList<GEIndividual> selectedPopulation = EATest.getSelectedIndividualsForReplacement(initialPopulation);
//        for(GEIndividual selected : selectedPopulation) {
//            System.out.println(selected.getChromosomes() + " -> " + selected.getFitness().getDouble());
//        }
//        // perform crossover / mutation on selected Pop
//        System.out.println("\n\nFINAL RESULT\n\n");
//        System.out.println("~~~\nInitial population:");
//        for(GEIndividual individual: initialPopulation) {
//            System.out.println(individual.getChromosomes() + " -> " + individual.getFitness().getDouble());
//        }
//        System.out.println("~~~\nFinal population:");
//        ArrayList<GEIndividual> finalPopulation = EATest.performOperators(generator, elites, selectedPopulation);
//        for(GEIndividual individual : finalPopulation) {
//            System.out.println(individual.getChromosomes());
//        }
//        System.out.println(">>> Differences ratio: " + EATest.getDifferenceRatio(initialPopulation, finalPopulation) + "%");
//    }
//
//    public static void testStopCriterionOnTime() throws InterruptedException {
//        // start time measure
//        long start = System.currentTimeMillis();
//        long max = start + 3 * 60000; // 3 minutes !
//        long ckp = 0;
//        int i = 1;
//        while (ckp <= max) {
//            System.out.println("* Generation " + i + " ...");
//            // sleep 30 seconds ...
//            Thread.sleep(30000);
//            ckp = System.currentTimeMillis();
//            System.out.println(ckp);
//            i++;
//        }
//    }
//
//    public static void main(String[] args) throws InterruptedException {
////        execRefonte();
//        testStopCriterionOnTime();
//    }
//
//}
