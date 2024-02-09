//package com.i3s.app.rdfminer.evolutionary.geva;
//
//import com.i3s.app.rdfminer.Global;
//import com.i3s.app.rdfminer.RDFMiner;
//import com.i3s.app.rdfminer.entity.Entity;
//import com.i3s.app.rdfminer.entity.shacl.Shape;
//import com.i3s.app.rdfminer.evolutionary.fitness.Fitness;
//import com.i3s.app.rdfminer.evolutionary.geva.Individuals.GEChromosome;
//import com.i3s.app.rdfminer.evolutionary.geva.Individuals.GEIndividual;
//import com.i3s.app.rdfminer.evolutionary.geva.Individuals.Genotype;
//import com.i3s.app.rdfminer.evolutionary.geva.Individuals.Individual;
//import com.i3s.app.rdfminer.evolutionary.geva.Individuals.Populations.SimplePopulation;
//import com.i3s.app.rdfminer.evolutionary.geva.Mapper.GEGrammar;
//import com.i3s.app.rdfminer.evolutionary.geva.Operator.crossover.*;
//import com.i3s.app.rdfminer.evolutionary.geva.Operator.mutation.*;
//import com.i3s.app.rdfminer.evolutionary.geva.Operator.selection.ProportionalRouletteWheel;
//import com.i3s.app.rdfminer.evolutionary.geva.Util.Random.MersenneTwisterFast;
//import com.i3s.app.rdfminer.evolutionary.individual.CandidatePopulation;
//import com.i3s.app.rdfminer.generator.Generator;
//import com.i3s.app.rdfminer.generator.axiom.RandomAxiomGenerator;
//import com.i3s.app.rdfminer.generator.shacl.RandomShapeGenerator;
//import com.i3s.app.rdfminer.sparql.corese.CoreseEndpoint;
//
//import java.io.IOException;
//import java.net.URISyntaxException;
//import java.util.ArrayList;
//
//public class Test {
//
//    /**
//     * Just a test ...
//     */
//    public static void testSinglePointCrossoverOriginalExample(GEChromosome c1, GEChromosome c2, boolean fixed) {
//        System.out.println("--------\nOriginal Example");
//        SinglePointCrossover cop = new SinglePointCrossover(new MersenneTwisterFast(), 1);
//        cop.setFixedCrossoverPoint(fixed);
//        // test method makeNewChromosome
//        System.out.println("before makeNewChromosome");
//        System.out.println("c1: " + c1);
//        System.out.println("c2: " + c2);
////        cop.makeNewChromosome(c1, c2, c1.size(), c2.size());
//        System.out.println("after makeNewChromosome");
//        System.out.println("c1: " + c1);
//        System.out.println("c2: " + c2);
//        // create individuals
//        Genotype g1 = new Genotype();
//        Genotype g2 = new Genotype();
//        g1.add(c1);
//        g2.add(c2);
//        GEIndividual i1 = new GEIndividual();
//        GEIndividual i2 = new GEIndividual();
//        i1.setMapper(new GEGrammar());
//        i1.setGenotype(g1);
//        i2.setMapper(new GEGrammar());
//        i2.setGenotype(g2);
//        // create population
//        ArrayList<GEIndividual> aI = new ArrayList<>(2);
//        aI.add(i1);
//        aI.add(i2);
//        // test doOperation
//        for(Individual i : aI) {
//            System.out.println("before doOperation -> individual: " + i.getGenotype().get(0));
//        }
//        cop.doOperation(aI);
//        for(Individual i : aI) {
//            System.out.println("after doOperation -> individual: " + i.getGenotype().get(0));
//        }
//        System.out.println();
//        System.out.println("Testing operation crossover");
//        System.out.println();
//        c1 = (GEChromosome) i1.getGenotype().get(0);
//        c2 = (GEChromosome) i2.getGenotype().get(0);
//        System.out.println(c1.toString());
//        System.out.println(c2.toString());
//        CrossoverModule cm = new CrossoverModule(new MersenneTwisterFast(), cop);
//        SimplePopulation p = new SimplePopulation();
//        p.add(i1);
//        p.add(i2);
//        cm.setPopulation(p);
//        long st = System.currentTimeMillis();
//        cm.perform();
//        long et = System.currentTimeMillis();
//        System.out.println("Done running: Total time(Ms) for 1 generations was " + (et - st));
//        System.out.println();
//        System.out.println("Testing module crossover");
//        System.out.println();
//        c1 = (GEChromosome) i1.getGenotype().get(0);
//        c2 = (GEChromosome) i2.getGenotype().get(0);
//        System.out.println(c1.toString());
//        System.out.println(c2.toString());
//    }
//
//    /**
//     * Just a test ...
//     */
//    public static void testSinglePointCrossover(ArrayList<GEIndividual> individuals, boolean fixed) {
//        System.out.println("--------\nSINGLE CROSSOVER");
//        SinglePointCrossover cop = new SinglePointCrossover(new MersenneTwisterFast(), 1);
//        cop.setFixedCrossoverPoint(fixed);
//        cop.doOperation(individuals);
//        for(GEIndividual i : individuals) {
//            System.out.println("individual after two point crossover -> " + i.getGenotype());
//        }
//    }
//
//    /**
//     * Just a test ...
//     */
//    public static void testTwoPointCrossover(ArrayList<GEIndividual> individuals, boolean fixed) {
//        System.out.println("--------\nTWO POINT CROSSOVER");
//        TwoPointCrossover tpc = new TwoPointCrossover(new MersenneTwisterFast(), 1);
//        tpc.setFixedCrossoverPoint(fixed);
//        tpc.doOperation(individuals);
//        for(GEIndividual i : individuals) {
//            System.out.println("individual after two point crossover -> " + i.getGenotype());
//        }
//    }
//
//    /**
//     * Just a test ...
//     */
//    public static void testSubtreeCrossover() {
//        RDFMiner.parameters.sizeChromosome = 2;
//        RDFMiner.parameters.populationSize = 2;
//        Generator generator = null;
//        try {
//            generator = new RandomAxiomGenerator("/user/rfelin/home/projects/RDFMining/IO/OWL2Axiom-complex-subclassof.bnf", true);
//        } catch (URISyntaxException | IOException e) {
//            e.printStackTrace();
//        };
//        CandidatePopulation canPop = new CandidatePopulation(generator);
//        ArrayList<GEIndividual> population = canPop.initialize(null);
//        System.out.println("--------\nSUBTREE CROSSOVER");
//        SubtreeCrossover stc = new SubtreeCrossover(new MersenneTwisterFast(), 1);
//        for(Individual i : population) {
//            System.out.println("individual before subtree crossover -> " + i.getGenotype());
//        }
//        stc.doOperation(population);
//        for(Individual i : population) {
//            System.out.println("individual after subtree crossover -> " + i.getGenotype());
//        }
//    }
//
//    /**
//     * Just a test ...
//     */
//    public static void testSinglePointCrossoverOnRealPopulation() {
//        RDFMiner.parameters.sizeChromosome = 2;
//        RDFMiner.parameters.populationSize = 2;
//        RDFMiner.parameters.proCrossover = 1;
//        Generator generator = null;
//        try {
//            generator = new RandomAxiomGenerator("/user/rfelin/home/projects/RDFMining/IO/OWL2Axiom-subclassof.bnf", true);
//        } catch (URISyntaxException | IOException e) {
//            e.printStackTrace();
//        };
//        CandidatePopulation canPop = new CandidatePopulation(generator);
//        ArrayList<GEIndividual> population = canPop.initialize(null);
//        System.out.println("--------\nSUBTREE CROSSOVER");
//        SinglePointCrossover stc = new SinglePointCrossover();
//        stc.setFixedCrossoverPoint(true);
//        for(GEIndividual i : population) {
//            System.out.println("individual before subtree crossover -> " + i.getGenotype() + " is mapped ? " + i.isMapped());
//        }
//        stc.doOperation(population);
//        for(GEIndividual i : population) {
//            System.out.println("individual after subtree crossover -> " + i.getGenotype() + " is mapped ? " + i.isMapped());
//        }
//    }
//
//    /**
//     * Just a test ...
//     */
//    public static void testIntFlipMutation(ArrayList<GEIndividual> individuals) {
//        System.out.println("--------\nInt Flip Mutation");
//        IntFlipMutation ifm = new IntFlipMutation(new MersenneTwisterFast(), .1);
//        ifm.doOperation(individuals);
//        for(GEIndividual i : individuals) {
//            System.out.println("individual after Int Flip Mutation -> " + i.getGenotype());
//        }
//    }
//
//    /**
//     * Just a test ...
//     */
//    public static void testNodalMutation() {
//        RDFMiner.parameters.sizeChromosome = 2;
//        RDFMiner.parameters.populationSize = 2;
//        Generator generator = null;
//        try {
//            generator = new RandomAxiomGenerator("/user/rfelin/home/projects/RDFMining/IO/OWL2Axiom-complex-subclassof.bnf", true);
//        } catch (URISyntaxException | IOException e) {
//            e.printStackTrace();
//        };
//        CandidatePopulation canPop = new CandidatePopulation(generator);
//        ArrayList<GEIndividual> population = canPop.initialize(null);
//        System.out.println("--------\nNodal Mutation");
//        NodalMutation ifm = new NodalMutation(new MersenneTwisterFast(), .8);
//        for(Individual i : population) {
//            System.out.println("individual before -> " + i.getGenotype());
//        }
//        ifm.doOperation(population);
//        for(GEIndividual i : population) {
//            System.out.println("individual after Nodal Mutation -> " + i.getGenotype());
//        }
//    }
//
//    /**
//     * Just a test ...
//     */
//    public static void testSubtreeMutation() {
//        RDFMiner.parameters.sizeChromosome = 2;
//        RDFMiner.parameters.populationSize = 2;
//        Generator generator = null;
//        try {
//            generator = new RandomAxiomGenerator("/user/rfelin/home/projects/RDFMining/IO/OWL2Axiom-complex-subclassof.bnf", true);
//        } catch (URISyntaxException | IOException e) {
//            e.printStackTrace();
//        };
//        CandidatePopulation canPop = new CandidatePopulation(generator);
//        ArrayList<GEIndividual> population = canPop.initialize(null);
//        System.out.println("--------\nNodal Mutation");
//        SubtreeMutation ifm = new SubtreeMutation(new MersenneTwisterFast(), 1);
//        for(Individual i : population) {
//            System.out.println("individual before -> " + i.getGenotype());
//        }
//        ifm.doOperation(population);
//        for(GEIndividual i : population) {
//            System.out.println("individual after Subtree Mutation -> " + i.getGenotype());
//        }
//    }
//
//    /**
//     * Just a test ...
//     */
//    public static void testStructuralMutation() {
//        RDFMiner.parameters.sizeChromosome = 2;
//        RDFMiner.parameters.populationSize = 2;
//        Generator generator = null;
//        try {
//            generator = new RandomAxiomGenerator("/user/rfelin/home/projects/RDFMining/IO/OWL2Axiom-complex-subclassof.bnf", true);
//        } catch (URISyntaxException | IOException e) {
//            e.printStackTrace();
//        };
//        CandidatePopulation canPop = new CandidatePopulation(generator);
//        ArrayList<GEIndividual> population = canPop.initialize(null);
//        System.out.println("--------\nStructural Mutation");
//        StructuralMutation ifm = new StructuralMutation(new MersenneTwisterFast(), 1);
//        for(Individual i : population) {
//            System.out.println("individual before -> " + i.getGenotype());
//        }
//        ifm.doOperation(population);
//        for(GEIndividual i : population) {
//            System.out.println("individual after Structural Mutation -> " + i.getGenotype());
//        }
//    }
//
//    /**
//     * Just a test ...
//     */
//    public static void testIntFlipByteMutation(ArrayList<GEIndividual> individuals) {
//        System.out.println("--------\nInt Flip Byte Mutation");
//        IntFlipByteMutation ifm = new IntFlipByteMutation(new MersenneTwisterFast(), .01);
//        ifm.doOperation(individuals);
//        for(GEIndividual i : individuals) {
//            System.out.println("individual after Int Flip Byte Mutation -> " + i.getGenotype());
//        }
//    }
//
//    /**
//     * Just a test ...
//     */
//    public static void testCollectionCopy() {
//        ArrayList<Integer> src = new ArrayList<>();
//        src.add(1);
//        src.add(2);
//        ArrayList<Integer> dest = (ArrayList<Integer>) src.clone();
//        dest.remove(0);
//        for(Integer i : src) {
//            System.out.println("src: " + i);
//        }
//        for(Integer i : dest) {
//            System.out.println("dest: " + i);
//        }
//    }
//
//    public static void testCrossoverMutation() {
//        // Create individuals
//        GEChromosome c1 = new GEChromosome(10);
//        GEChromosome c2 = new GEChromosome(10);
//        for (int i = 0; i < 2; i++) {
//            c1.add(1);
//            c2.add(2);
//        }
//        Genotype g1 = new Genotype();
//        Genotype g2 = new Genotype();
//        g1.add(c1);
//        g2.add(c2);
//        GEIndividual i1 = new GEIndividual();
//        GEIndividual i2 = new GEIndividual();
//        GEGrammar grammar = new GEGrammar();
//        i1.setMapper(grammar);
//        i1.setGenotype(g1);
//        i2.setMapper(grammar);
//        i2.setGenotype(g2);
//        // create population
//        ArrayList<GEIndividual> aI = new ArrayList<>(2);
//        aI.add(i1);
//        aI.add(i2);
//        for(Individual i : aI) {
//            System.out.println("individual before -> " + i.getGenotype());
//        }
//        /* CROSSOVER */
//        // SINGLE POINT CROSSOVER (ORIGINAL EXAMPLE)
////        Test.testSinglePointCrossoverOriginalExample(c1, c2, true);
//        // SINGLE POINT CROSSOVER
//        Test.testSinglePointCrossover(aI, true);
//        // TWO POINT CROSSOVER
////        Test.testTwoPointCrossover(aI, true);
//        // SUBTREE CROSSOVER
////        Test.testSubtreeCrossover();
//
//        /* MUTATION */
////        Test.testIntFlipMutation(aI);
////        Test.testNodalMutation();
////        Test.testSubtreeMutation();
////        Test.testStructuralMutation(); // didn't work !!
////        Test.testIntFlipByteMutation(aI);
//    }
//
//    public static void testSelection() {
//        System.loadLibrary(Global.SO_LIBRARY);
//        Global.TARGET_SPARQL_ENDPOINT = Global.TRAINING_SPARQL_ENDPOINT;
//        RDFMiner.parameters.sizeChromosome = 2;
////        RDFMiner.parameters.sizeSelection = 0.3;
//        RDFMiner.parameters.populationSize = 2;
//        Generator generator = null;
//        try {
//            generator = new RandomAxiomGenerator("/user/rfelin/home/projects/RDFMining/IO/OWL2Axiom-subclassof.bnf", true);
//        } catch (URISyntaxException | IOException e) {
//            e.printStackTrace();
//        };
//        CandidatePopulation canPop = new CandidatePopulation(generator);
//        ArrayList<GEIndividual> population = canPop.initialize(null);
//        for(GEIndividual e : population) {
////            System.out.println(e.getGenotype());
//            System.out.println("GEIndividual: " + e.getGenotype());
//        }
//        ArrayList<GEIndividual> updatedPopulation = new ArrayList<>();
//        // generate individuals
////        for(GEIndividual individual : population) {
////            // set a random fitness between 0 and 10 (exclusive)
////            individual.setFitness(new BasicFitness(new Random().nextDouble() * 10, individual));
////            // log usefull information
////            System.out.println("i: " + individual.getGenotype() + " ~ F(i)= " + individual.getFitness().getDouble());
////            // add the individual into population
////            updatedPopulation.add(individual);
////        }
//        ArrayList<Entity> entities = Fitness.initializePopulation(population, generator);
//        for(Entity e : entities) {
//            updatedPopulation.add(e.individual);
//            System.out.println("i: " + e.individual.getGenotype() + " ~ F(i)= " + e.individual.getFitness().getDouble());
//        }
//        // operate selection
////        EliteOperationSelection selection = new EliteOperationSelection();
////        ScaledRouletteWheel selection = new ScaledRouletteWheel(3, new MersenneTwisterFast());
//        ProportionalRouletteWheel selection = new ProportionalRouletteWheel();
////        TournamentSelect selection = new TournamentSelect(3, 5, new MersenneTwisterFast());
//        selection.doOperation(updatedPopulation);
//        System.out.println("Selection done ... ");
//        for(Individual selected : selection.getSelectedPopulation().getAll()) {
//            System.out.println("s(i): " + selected.getGenotype() + " ~ F(i)= " + selected.getFitness().getDouble());
//        }
//    }
//
//    public static void testSwapCrossover() {
//        // Create individuals
//        GEChromosome c1 = new GEChromosome(10);
//        GEChromosome c2 = new GEChromosome(10);
//        c1.add(1);
//        c1.add(2);
//        c1.add(3);
//        c1.add(4);
//        c2.add(5);
//        c2.add(6);
//        c2.add(7);
//        c2.add(8);
//        Genotype g1 = new Genotype();
//        Genotype g2 = new Genotype();
//        g1.add(c1);
//        g2.add(c2);
//        GEIndividual i1 = new GEIndividual();
//        GEIndividual i2 = new GEIndividual();
//        GEGrammar grammar = new GEGrammar();
//        i1.setMapper(grammar);
//        i1.setGenotype(g1);
//        i2.setMapper(grammar);
//        i2.setGenotype(g2);
//        // create population
//        ArrayList<GEIndividual> aI = new ArrayList<>(2);
//        aI.add(i1);
//        aI.add(i2);
//        for(Individual i : aI) {
//            System.out.println("individual before -> " + i.getGenotype());
//        }
//        System.out.println("--------\nSINGLE CROSSOVER");
//        SwapCrossover cop = new SwapCrossover(new MersenneTwisterFast(), 1);
//        cop.doOperation(aI);
//        for(GEIndividual i : aI) {
//            System.out.println("individual after two point crossover -> " + i.getGenotype());
//        }
//    }
//
//    public static void testSwapCrossoverOnRealData() {
//        RDFMiner.parameters.sizeChromosome = 2;
//        RDFMiner.parameters.populationSize = 2;
//        RDFMiner.parameters.proCrossover = 1.0;
//        Generator generator = null;
//        try {
//            generator = new RandomAxiomGenerator("/user/rfelin/home/projects/RDFMining/IO/OWL2Axiom-subclassof.bnf", true);
//        } catch (URISyntaxException | IOException e) {
//            e.printStackTrace();
//        };
//        CandidatePopulation canPop = new CandidatePopulation(generator);
//        ArrayList<GEIndividual> population = canPop.initialize(null);
//        ArrayList<GEIndividual> saveOriginalPop = new ArrayList<>();
//        for(GEIndividual ind : population) {
//            System.out.println("original pop: " + ind.getGenotype() + ": " + ind.getPhenotype().getStringNoSpace());
//            assert generator != null;
//            saveOriginalPop.add(generator.getIndividualFromChromosome(ind.getChromosomes()));
//        }
//        SwapCrossover swp = new SwapCrossover();
//        swp.doOperation(population);
//        for(GEIndividual ind : saveOriginalPop) {
//            System.out.println("saved pop: " + ind.getGenotype() + ": " + ind.getPhenotype().getStringNoSpace());
//        }
//        for(GEIndividual ind : population) {
//            GEIndividual i = generator.getIndividualFromChromosome(ind.getChromosomes());
//            System.out.println("new pop: " + i.getGenotype() + ": " + i.getPhenotype().getStringNoSpace());
//        }
//    }
//
//    public static void generateRandomPhenotypes() {
//        RDFMiner.parameters.sizeChromosome = 100;
//        RDFMiner.parameters.populationSize = 3000;
////        Global.TRAINING_SPARQL_ENDPOINT = Global.;
//        Generator generator = null;
//        try {
//            generator = new RandomAxiomGenerator("/user/rfelin/home/projects/RDFMining/IO/tmp.bnf", true);
//        } catch (URISyntaxException | IOException e) {
//            e.printStackTrace();
//        };
//        CandidatePopulation canPop = new CandidatePopulation(generator);
//        ArrayList<GEIndividual> population = canPop.initialize(null);
//        System.out.println();
//        for(GEIndividual ind : population) {
//            System.out.println(ind.getPhenotype());
//        }
//    }
//
//    public static void generateComplexRandomPhenotypes() {
//        RDFMiner.parameters.sizeChromosome = 100;
//        RDFMiner.parameters.populationSize = 10;
////        Global.TRAINING_SPARQL_ENDPOINT = Global.;
//        Generator generator = null;
//        try {
//            generator = new RandomAxiomGenerator("/user/rfelin/home/projects/RDFMining/IO/complex-shapes.bnf", true);
//        } catch (URISyntaxException | IOException e) {
//            e.printStackTrace();
//        };
//        CandidatePopulation canPop = new CandidatePopulation(generator);
//        ArrayList<GEIndividual> population = canPop.initialize(null);
//        System.out.println();
//        for(GEIndividual ind : population) {
//            System.out.println(ind.getPhenotype());
//        }
//    }
//
//    public static void test() {
//        RDFMiner.parameters.sizeChromosome = 8;
//        RDFMiner.parameters.populationSize = 10;
//        Generator generator = null;
//        try {
//            generator = new RandomShapeGenerator("/user/rfelin/home/projects/RDFMining/IO/shacl-shapes-grammar.bnf");
//        } catch (URISyntaxException | IOException e) {
//            e.printStackTrace();
//        };
//        CandidatePopulation canPop = new CandidatePopulation(generator);
//        ArrayList<GEIndividual> population = canPop.initialize(null);
//        for(GEIndividual individual: population) {
//            System.out.println(individual.getDistinctPhenotypes());
//            System.out.println("individuals: " + individual.getChromosomes());
//            System.out.println("phenotype: " + individual.getPhenotype().getStringNoSpace());
//        }
//
//    }
//
//    public static void testGetIndividualFromChromosome() throws URISyntaxException, IOException {
//        RDFMiner.parameters.sizeChromosome = 3;
//        RDFMiner.parameters.populationSize = 1;
//        RDFMiner.parameters.useProbabilisticShaclMode = true;
//        Generator generator = null;
//        try {
//            // /user/rfelin/home/projects/RDFMining/IO/OWL2Axiom-subclassof.bnf
//            generator = new RandomShapeGenerator("/user/rfelin/home/projects/eurogp_2024/results/V1_100_5000_1/grammar.bnf");
//        } catch (URISyntaxException | IOException e) {
//            e.printStackTrace();
//        }
//        GEChromosome chrom = new GEChromosome(RDFMiner.parameters.sizeChromosome);
//        chrom.setMaxChromosomeLength(1000);
//        // 1000427925,1741643077,
//        // 1000427925,1620996495,
//        chrom.add(746431190);
//        chrom.add(746431190);
//        chrom.add(2061549156);
//        // a sh:NodeShape ; sh:targetClass <http://www.wikidata.org/entity/Q837783>  ;
//        // sh:property [  sh:path rdf:type ; sh:hasValue <http://www.wikidata.org/entity/Q5380903>  ;  ]
//        GEIndividual ind = generator.getIndividualFromChromosome(chrom);
//        System.out.println(ind.isTrivial());
//        System.out.println(ind.getUsedCodons());
//        System.out.println(ind.getPhenotype().getStringNoSpace());
//        System.out.println(ind.getChromosomes());
//        // initi shape
//        Shape shape = new Shape(ind, new CoreseEndpoint("http://localhost:9100/sparql", Global.PREFIXES));
//        System.out.println(shape.referenceCardinality+ " " + shape.numExceptions);
//    }
//
//    public static void main(String[] args) throws URISyntaxException, IOException {
//        System.loadLibrary(Global.SO_LIBRARY);
////        testGetIndividualFromChromosome();
//        generateComplexRandomPhenotypes();
////        testSinglePointCrossoverOnRealPopulation();
////        testSwapCrossover();
////        testSwapCrossoverOnRealData();
////        testSubtreeCrossover();
////        testSelection();
////        testSinglePointCrossoverOnRealPopulation();
////        testCollectionCopy();
////        testSinglePointCrossoverRapide();
//    }
//
//}
