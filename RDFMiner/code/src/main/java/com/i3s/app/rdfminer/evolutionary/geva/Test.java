package com.i3s.app.rdfminer.evolutionary.geva;

import com.i3s.app.rdfminer.RDFMiner;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.FitnessPackage.BasicFitness;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.GEChromosome;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.GEIndividual;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.Genotype;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.Individual;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.Populations.SimplePopulation;
import com.i3s.app.rdfminer.evolutionary.geva.Mapper.GEGrammar;
import com.i3s.app.rdfminer.evolutionary.geva.Operator.CrossoverModule;
import com.i3s.app.rdfminer.evolutionary.geva.Operator.Operations.ContextSensitiveOperations.NodalMutation;
import com.i3s.app.rdfminer.evolutionary.geva.Operator.Operations.ContextSensitiveOperations.StructuralMutation;
import com.i3s.app.rdfminer.evolutionary.geva.Operator.Operations.ContextSensitiveOperations.SubtreeCrossover;
import com.i3s.app.rdfminer.evolutionary.geva.Operator.Operations.ContextSensitiveOperations.SubtreeMutation;
import com.i3s.app.rdfminer.evolutionary.geva.Operator.Operations.*;
import com.i3s.app.rdfminer.evolutionary.geva.Util.Random.MersenneTwisterFast;
import com.i3s.app.rdfminer.evolutionary.individual.CandidatePopulation;
import com.i3s.app.rdfminer.generator.Generator;
import com.i3s.app.rdfminer.generator.axiom.RandomAxiomGenerator;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Random;

public class Test {

    /**
     * Just a test ...
     */
    public static void testSinglePointCrossoverOriginalExample(GEChromosome c1, GEChromosome c2, boolean fixed) {
        System.out.println("--------\nOriginal Example");
        SinglePointCrossover cop = new SinglePointCrossover(new MersenneTwisterFast(), 1);
        cop.setFixedCrossoverPoint(fixed);
        // test method makeNewChromosome
        System.out.println("before makeNewChromosome");
        System.out.println("c1: " + c1);
        System.out.println("c2: " + c2);
//        cop.makeNewChromosome(c1, c2, c1.size(), c2.size());
        System.out.println("after makeNewChromosome");
        System.out.println("c1: " + c1);
        System.out.println("c2: " + c2);
        // create individuals
        Genotype g1 = new Genotype();
        Genotype g2 = new Genotype();
        g1.add(c1);
        g2.add(c2);
        GEIndividual i1 = new GEIndividual();
        GEIndividual i2 = new GEIndividual();
        i1.setMapper(new GEGrammar());
        i1.setGenotype(g1);
        i2.setMapper(new GEGrammar());
        i2.setGenotype(g2);
        // create population
        ArrayList<GEIndividual> aI = new ArrayList<>(2);
        aI.add(i1);
        aI.add(i2);
        // test doOperation
        for(Individual i : aI) {
            System.out.println("before doOperation -> individual: " + i.getGenotype().get(0));
        }
        cop.doOperation(aI);
        for(Individual i : aI) {
            System.out.println("after doOperation -> individual: " + i.getGenotype().get(0));
        }
        System.out.println();
        System.out.println("Testing operation crossover");
        System.out.println();
        c1 = (GEChromosome) i1.getGenotype().get(0);
        c2 = (GEChromosome) i2.getGenotype().get(0);
        System.out.println(c1.toString());
        System.out.println(c2.toString());
        CrossoverModule cm = new CrossoverModule(new MersenneTwisterFast(), cop);
        SimplePopulation p = new SimplePopulation();
        p.add(i1);
        p.add(i2);
        cm.setPopulation(p);
        long st = System.currentTimeMillis();
        cm.perform();
        long et = System.currentTimeMillis();
        System.out.println("Done running: Total time(Ms) for 1 generations was " + (et - st));
        System.out.println();
        System.out.println("Testing module crossover");
        System.out.println();
        c1 = (GEChromosome) i1.getGenotype().get(0);
        c2 = (GEChromosome) i2.getGenotype().get(0);
        System.out.println(c1.toString());
        System.out.println(c2.toString());
    }

    /**
     * Just a test ...
     */
    public static void testSinglePointCrossover(ArrayList<GEIndividual> individuals, boolean fixed) {
        System.out.println("--------\nSINGLE CROSSOVER");
        SinglePointCrossover cop = new SinglePointCrossover(new MersenneTwisterFast(), 1);
        cop.setFixedCrossoverPoint(fixed);
        cop.doOperation(individuals);
        for(GEIndividual i : individuals) {
            System.out.println("individual after two point crossover -> " + i.getGenotype());
        }
    }

    /**
     * Just a test ...
     */
    public static void testTwoPointCrossover(ArrayList<GEIndividual> individuals, boolean fixed) {
        System.out.println("--------\nTWO POINT CROSSOVER");
        TwoPointCrossover tpc = new TwoPointCrossover(new MersenneTwisterFast(), 1);
        tpc.setFixedCrossoverPoint(fixed);
        tpc.doOperation(individuals);
        for(GEIndividual i : individuals) {
            System.out.println("individual after two point crossover -> " + i.getGenotype());
        }
    }

    /**
     * Just a test ...
     */
    public static void testSubtreeCrossover() {
        RDFMiner.parameters.initLenChromosome = 6;
        RDFMiner.parameters.populationSize = 5;
        Generator generator = null;
        try {
            generator = new RandomAxiomGenerator("/user/rfelin/home/projects/RDFMining/IO/OWL2Axiom-complex-subclassof.bnf", true);
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        };
        CandidatePopulation canPop = new CandidatePopulation(generator);
        ArrayList<GEIndividual> population = canPop.initialize(null, 1);
        System.out.println("--------\nSUBTREE CROSSOVER");
        SubtreeCrossover stc = new SubtreeCrossover(new MersenneTwisterFast(), 1);
        for(Individual i : population) {
            System.out.println("individual before subtree crossover -> " + i.getGenotype());
        }
        stc.doOperation(population);
        for(Individual i : population) {
            System.out.println("individual after subtree crossover -> " + i.getGenotype());
        }
    }





    /**
     * Just a test ...
     */
    public static void testIntFlipMutation(ArrayList<GEIndividual> individuals) {
        System.out.println("--------\nInt Flip Mutation");
        IntFlipMutation ifm = new IntFlipMutation(new MersenneTwisterFast(), .1);
        ifm.doOperation(individuals);
        for(GEIndividual i : individuals) {
            System.out.println("individual after Int Flip Mutation -> " + i.getGenotype());
        }
    }

    /**
     * Just a test ...
     */
    public static void testNodalMutation() {
        RDFMiner.parameters.initLenChromosome = 2;
        RDFMiner.parameters.populationSize = 2;
        Generator generator = null;
        try {
            generator = new RandomAxiomGenerator("/user/rfelin/home/projects/RDFMining/IO/OWL2Axiom-complex-subclassof.bnf", true);
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        };
        CandidatePopulation canPop = new CandidatePopulation(generator);
        ArrayList<GEIndividual> population = canPop.initialize(null, 1);
        System.out.println("--------\nNodal Mutation");
        NodalMutation ifm = new NodalMutation(new MersenneTwisterFast(), .8);
        for(Individual i : population) {
            System.out.println("individual before -> " + i.getGenotype());
        }
        ifm.doOperation(population);
        for(GEIndividual i : population) {
            System.out.println("individual after Nodal Mutation -> " + i.getGenotype());
        }
    }

    /**
     * Just a test ...
     */
    public static void testSubtreeMutation() {
        RDFMiner.parameters.initLenChromosome = 2;
        RDFMiner.parameters.populationSize = 2;
        Generator generator = null;
        try {
            generator = new RandomAxiomGenerator("/user/rfelin/home/projects/RDFMining/IO/OWL2Axiom-complex-subclassof.bnf", true);
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        };
        CandidatePopulation canPop = new CandidatePopulation(generator);
        ArrayList<GEIndividual> population = canPop.initialize(null, 1);
        System.out.println("--------\nNodal Mutation");
        SubtreeMutation ifm = new SubtreeMutation(new MersenneTwisterFast(), 1);
        for(Individual i : population) {
            System.out.println("individual before -> " + i.getGenotype());
        }
        ifm.doOperation(population);
        for(GEIndividual i : population) {
            System.out.println("individual after Subtree Mutation -> " + i.getGenotype());
        }
    }

    /**
     * Just a test ...
     */
    public static void testStructuralMutation() {
        RDFMiner.parameters.initLenChromosome = 2;
        RDFMiner.parameters.populationSize = 2;
        Generator generator = null;
        try {
            generator = new RandomAxiomGenerator("/user/rfelin/home/projects/RDFMining/IO/OWL2Axiom-complex-subclassof.bnf", true);
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        };
        CandidatePopulation canPop = new CandidatePopulation(generator);
        ArrayList<GEIndividual> population = canPop.initialize(null, 1);
        System.out.println("--------\nStructural Mutation");
        StructuralMutation ifm = new StructuralMutation(new MersenneTwisterFast(), 1);
        for(Individual i : population) {
            System.out.println("individual before -> " + i.getGenotype());
        }
        ifm.doOperation(population);
        for(GEIndividual i : population) {
            System.out.println("individual after Structural Mutation -> " + i.getGenotype());
        }
    }

    /**
     * Just a test ...
     */
    public static void testIntFlipByteMutation(ArrayList<GEIndividual> individuals) {
        System.out.println("--------\nInt Flip Byte Mutation");
        IntFlipByteMutation ifm = new IntFlipByteMutation(new MersenneTwisterFast(), .01);
        ifm.doOperation(individuals);
        for(GEIndividual i : individuals) {
            System.out.println("individual after Int Flip Byte Mutation -> " + i.getGenotype());
        }
    }

    public static void testCrossoverMutation() {
        // Create individuals
        GEChromosome c1 = new GEChromosome(10);
        GEChromosome c2 = new GEChromosome(10);
        for (int i = 0; i < 20; i++) {
            c1.add(1);
            c2.add(2);
        }
        Genotype g1 = new Genotype();
        Genotype g2 = new Genotype();
        g1.add(c1);
        g2.add(c2);
        GEIndividual i1 = new GEIndividual();
        GEIndividual i2 = new GEIndividual();
        GEGrammar grammar = new GEGrammar();
        i1.setMapper(grammar);
        i1.setGenotype(g1);
        i2.setMapper(grammar);
        i2.setGenotype(g2);
        // create population
        ArrayList<GEIndividual> aI = new ArrayList<>(2);
        aI.add(i1);
        aI.add(i2);
        for(Individual i : aI) {
            System.out.println("individual before -> " + i.getGenotype());
        }
        /* CROSSOVER */
        // SINGLE POINT CROSSOVER (ORIGINAL EXAMPLE)
//        Test.testSinglePointCrossoverOriginalExample(c1, c2, true);
        // SINGLE POINT CROSSOVER
//        Test.testSinglePointCrossover(aI, true);
        // TWO POINT CROSSOVER
//        Test.testTwoPointCrossover(aI, true);
        // SUBTREE CROSSOVER
//        Test.testSubtreeCrossover();

        /* MUTATION */
//        Test.testIntFlipMutation(aI);
//        Test.testNodalMutation();
//        Test.testSubtreeMutation();
//        Test.testStructuralMutation(); // didn't work !!
        Test.testIntFlipByteMutation(aI);
    }

    public static void testSelection() {
        RDFMiner.parameters.initLenChromosome = 2;
//        RDFMiner.parameters.sizeSelection = 0.3;
        RDFMiner.parameters.populationSize = 10;
        Generator generator = null;
        try {
            generator = new RandomAxiomGenerator("/user/rfelin/home/projects/RDFMining/IO/OWL2Axiom-subclassof.bnf", true);
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        };
        CandidatePopulation canPop = new CandidatePopulation(generator);
        ArrayList<GEIndividual> population = canPop.initialize(null, 1);
        ArrayList<GEIndividual> updatedPopulation = new ArrayList<>();
        // generate individuals
        for(GEIndividual individual : population) {
            // set a random fitness between 0 and 10 (exclusive)
            individual.setFitness(new BasicFitness(new Random().nextDouble() * 10, individual));
            // log usefull information
            System.out.println("i: " + individual.getGenotype() + " ~ F(i)= " + individual.getFitness().getDouble());
            // add the individual into population
            updatedPopulation.add(individual);
        }
        // operate selection
//        EliteOperationSelection selection = new EliteOperationSelection(3);
//        ScaledRouletteWheel selection = new ScaledRouletteWheel(3, new MersenneTwisterFast());
//        ProportionalRouletteWheel selection = new ProportionalRouletteWheel(3, new MersenneTwisterFast());
        TournamentSelect selection = new TournamentSelect(3, 5, new MersenneTwisterFast());
        selection.doOperation(updatedPopulation);
        System.out.println("Selection done ... ");
        for(Individual selected : selection.getSelectedPopulation().getAll()) {
            System.out.println("s(i): " + selected.getGenotype() + " ~ F(i)= " + selected.getFitness().getDouble());
        }
    }

    public static void main(String[] args) {
//        testCrossoverMutation();
        testSelection();
    }

}
