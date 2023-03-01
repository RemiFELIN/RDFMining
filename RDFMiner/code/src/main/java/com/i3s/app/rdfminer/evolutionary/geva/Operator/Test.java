package com.i3s.app.rdfminer.evolutionary.geva.Operator;

import com.i3s.app.rdfminer.RDFMiner;
import com.i3s.app.rdfminer.evolutionary.crossover.TwoPointCrossover;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.GEChromosome;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.GEIndividual;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.Genotype;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.Individual;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.Populations.SimplePopulation;
import com.i3s.app.rdfminer.evolutionary.geva.Mapper.GEGrammar;
import com.i3s.app.rdfminer.evolutionary.geva.Operator.Operations.ContextSensitiveOperations.SubtreeCrossover;
import com.i3s.app.rdfminer.evolutionary.geva.Operator.Operations.SinglePointCrossover;
import com.i3s.app.rdfminer.evolutionary.geva.Util.Random.MersenneTwisterFast;
import com.i3s.app.rdfminer.evolutionary.individual.CandidatePopulation;
import com.i3s.app.rdfminer.generator.Generator;
import com.i3s.app.rdfminer.generator.axiom.RandomAxiomGenerator;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

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
        ArrayList<Individual> aI = new ArrayList<>(2);
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
        CrossoverModule cm = new CrossoverModule(new MersenneTwisterFast(), cop);
        SimplePopulation p = new SimplePopulation();
        for(GEIndividual i : individuals) {
            p.add(i);
        }
        cm.setPopulation(p);
        long st = System.currentTimeMillis();
        cm.perform();
        long et = System.currentTimeMillis();
        System.out.println("Done running: Total time(Ms) for 1 generations was " + (et - st));
        System.out.println();
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
        CrossoverModule cm = new CrossoverModule(new MersenneTwisterFast(), tpc);
        SimplePopulation p = new SimplePopulation();
        for(GEIndividual i : individuals) {
            p.add(i);
        }
        cm.setPopulation(p);
        long st = System.currentTimeMillis();
        cm.perform();
        long et = System.currentTimeMillis();
        System.out.println("Done running: Total time(Ms) for 1 generations was " + (et - st));
        System.out.println();
//        tpc.setFixedCrossoverPoint(fixed);
//        tpc.doOperation(individuals);
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

    public static void main(String[] args) {
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
        i1.setMapper(new GEGrammar());
        i1.setGenotype(g1);
        i2.setMapper(new GEGrammar());
        i2.setGenotype(g2);
        // create population
        ArrayList<GEIndividual> aI = new ArrayList<>(2);
        aI.add(i1);
        aI.add(i2);
        for(Individual i : aI) {
            System.out.println("individual before crossover -> " + i.getGenotype());
        }
        // SINGLE POINT CROSSOVER (ORIGINAL EXAMPLE)
//        Test.testSinglePointCrossoverOriginalExample(c1, c2, true);
        // SINGLE POINT CROSSOVER
//        Test.testSinglePointCrossover(aI, true);
        // TWO POINT CROSSOVER
        Test.testTwoPointCrossover(aI, true);
        // SUBTREE CROSSOVER
//        Test.testSubtreeCrossover();
    }

}
