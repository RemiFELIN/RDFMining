package com.i3s.app.rdfminer.launcher;

import Individuals.GEChromosome;
import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.RDFMiner;
import com.i3s.app.rdfminer.axiom.Type;
import com.i3s.app.rdfminer.generator.Generator;
import com.i3s.app.rdfminer.generator.axiom.RandomAxiomGenerator;
import com.i3s.app.rdfminer.generator.shacl.RandomShapeGenerator;
import com.i3s.app.rdfminer.grammar.evolutionary.EATools;
import com.i3s.app.rdfminer.grammar.evolutionary.Fitness;
import com.i3s.app.rdfminer.grammar.evolutionary.fitness.AxiomFitnessEvaluation;
import com.i3s.app.rdfminer.grammar.evolutionary.fitness.ShapeFitnessEvaluation;
import com.i3s.app.rdfminer.grammar.evolutionary.individual.CandidatePopulation;
import com.i3s.app.rdfminer.grammar.evolutionary.individual.GEIndividual;
import com.i3s.app.rdfminer.grammar.evolutionary.selection.EliteSelection;
import com.i3s.app.rdfminer.grammar.evolutionary.selection.TruncationSelection;
import com.i3s.app.rdfminer.grammar.evolutionary.selection.TypeSelection;
import com.i3s.app.rdfminer.mode.Mode;
import com.i3s.app.rdfminer.output.axiom.GenerationJSON;
import com.i3s.app.rdfminer.output.axiom.AxiomsResultsJSON;
import com.i3s.app.rdfminer.output.axiom.StatJSON;
import com.i3s.app.rdfminer.parameters.CmdLineParameters;
import com.i3s.app.rdfminer.shacl.Shape;
import com.i3s.app.rdfminer.statistics.Statistics;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class LaunchWithGE {

    private static final Logger logger = Logger.getLogger(LaunchWithGE.class.getName());

    /**
     * The second version of RDFMiner launcher, with Grammar Evolutionary
     *
     * @param parameters all parameters given in the execution of JAR
     */
    public void run(CmdLineParameters parameters) throws Exception {

        // ShutDownHook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.warn("Shutting down RDFMiner ...");
            // Save results in output file
            writeAndFinish(RDFMiner.mode);
        }));

        RDFMiner.results = new AxiomsResultsJSON();
        RDFMiner.content = new ArrayList<>();
        RDFMiner.stats = new StatJSON();

        Generator generator = null;
//        Mode mode = null ;

        if (parameters.axiomFile == null) {
            if (parameters.useRandomAxiomGenerator) {
                // if a randomly generated Axiom already exists then continue
                // to generate a new Axioms based on BNF
                logger.info("Initializing the random axiom generator with grammar " + parameters.grammarFile + "...");
                generator = new RandomAxiomGenerator(parameters.grammarFile, true);
                // set the mode to AXIOMS
//                mode = new Mode(TypeMode.AXIOMS);
            } else if (parameters.useRandomShaclShapesGenerator) {
                // launch random SHACL Shapes generator
                logger.info("Initializing the random SHACL Shapes generator with grammar " + parameters.grammarFile + "...");
                generator = new RandomShapeGenerator(parameters.grammarFile);
                // set the mode to SHACL_SHAPE
//                mode = new Mode(TypeMode.SHACL_SHAPE);
            }
        }
//        assert mode != null;

        // Create the results file
        try {
            RDFMiner.output = new FileWriter(RDFMiner.outputFolder + Global.RESULTS_FILENAME);
        } catch (IOException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        /* GRAMMATICAL EVOLUTIONARY */
        /* Parameters as the inputs of GE */
        logger.info("========================================================");
        logger.info("PARAMETERS AS THE INPUTS OF GE");
        logger.info("========================================================");
        logger.info("POPULATION SIZE : " + parameters.populationSize);
        logger.info("TOTAL EFFORT : " + parameters.kBase);
        logger.info("MAX GENERATION NUMBER: " + (parameters.kBase / parameters.populationSize));
        logger.info("INITIALIZED LENGTH CHROMOSOME: " + parameters.initLenChromosome);
        logger.info("MAXIMUM WRAPPING: " + parameters.maxWrapp);
        logger.info("CROSSOVER PROBABILITY: " + parameters.proCrossover);
        logger.info("MUTATION PROBABILITY: " + parameters.proMutation);
        logger.info("TIME-CAP: " + (parameters.timeOut == 0 ? "Not used" : parameters.timeOut + " secondes"));
        logger.info("========================================================");
        logger.info("NUMBER OF THREAD(S) USED: " + Global.NB_THREADS);
        logger.info("========================================================");

        GEChromosome[] chromosomes = new GEChromosome[parameters.populationSize];

        ArrayList<GEIndividual> candidatePopulation;
        ArrayList<GEIndividual> etilismPopulation = null;
        Statistics stat = new Statistics();

        int sizeElite = (int) (parameters.sizeElite * parameters.populationSize);
        int sizeSelection = (int) (parameters.sizeSelection * parameters.populationSize);

        int curCheckpoint;
        int curGeneration;
        boolean flag = false;
        Reader buffer;
        File bufferFile = new File(RDFMiner.outputFolder + "buffer_size" + parameters.populationSize + ".txt");
        if (!bufferFile.exists()) {
            buffer = null;
            curCheckpoint = curGeneration = 1;
        } else {
            FileInputStream reader = new FileInputStream(
                    RDFMiner.outputFolder + "buffer_size" + parameters.populationSize + ".txt");
            buffer = new InputStreamReader(reader, StandardCharsets.UTF_8);
            int intch;
            StringBuilder st = new StringBuilder();
            while ((intch = reader.read()) != '\n') {
                st.append((char) intch);
            }
            curGeneration = Integer.parseInt(st.toString());
            st = new StringBuilder();
            while ((intch = reader.read()) != '\n') {
                st.append((char) intch);
            }
            curCheckpoint = Integer.parseInt(st.toString());
            logger.info("Buffer file founded ! starting from gen." + curGeneration + " ...");
        }
        logger.info("Initializing candidate population in generation " + curGeneration + "...");
        CandidatePopulation canPop = new CandidatePopulation(parameters.populationSize, generator,
                parameters.typeInitialization, chromosomes, parameters.initLenChromosome, parameters.maxValCodon,
                parameters.maxWrapp);
        candidatePopulation = canPop.initialize(buffer, curGeneration);

//        for(GEIndividual ind : candidatePopulation) {
//            System.out.println("ind: " + ind.getPhenotype());
//        }

        // Fill the 'stats' part of the JSON output
        RDFMiner.stats.populationSize = parameters.populationSize;
        RDFMiner.stats.maxLengthChromosome = parameters.initLenChromosome;
        RDFMiner.stats.maxWrapping = parameters.maxWrapp;
        RDFMiner.stats.crossoverProbability = parameters.proCrossover;
        RDFMiner.stats.mutationProbability = parameters.proMutation;
        RDFMiner.stats.timeOut = (int) parameters.timeOut;

        if (parameters.elitism == 1) {
            RDFMiner.stats.elitismSelection = true;
            RDFMiner.stats.eliteSize = parameters.sizeElite;
        } else {
            RDFMiner.stats.elitismSelection = false;
        }

        switch (parameters.typeSelect) {
            case 1:
                RDFMiner.stats.selectionMethod = "Roulette Wheel selection method";
                break;
            default:
            case 2:
                RDFMiner.stats.selectionMethod = "Truncation selection method";
                RDFMiner.stats.selectionSize = parameters.sizeSelection;
                break;
            case 3:
                RDFMiner.stats.selectionMethod = "Tournament selection method";
                break;
            case 4:
                RDFMiner.stats.selectionMethod = "Normal selection method";
                break;
        }

        // set the fitness method
        Fitness fit = null;
        if (RDFMiner.mode.isAxiomMode()) {
            // set a Fitness method provided to update individuals as OWL 2 Axiom
            fit = new AxiomFitnessEvaluation();
        } else if (RDFMiner.mode.isShaclMode()) {
            // set a Fitness method provided to update individuals as SHACL Shape
            fit = new ShapeFitnessEvaluation();
        }



        assert fit != null;

        while (curCheckpoint <= parameters.checkpoint) {
            System.out.println("\n--------------------------------------------------------\n");
            logger.info("Generation: " + curGeneration);
            // First step of the grammatical evolution
            if ((curGeneration == 1) || ((buffer != null) && (!flag))) {
                // if1
                if(RDFMiner.mode.isAxiomMode()) {
                    // special case for axiom mode
                    candidatePopulation = fit.updatePopulation(candidatePopulation, Global.VIRTUOSO_SMALL_DBPEDIA_2015_04_SPARQL_ENDPOINT, Global.PREFIXES, null);
                } else {
                    candidatePopulation = fit.updatePopulation(candidatePopulation, Global.SPARQL_ENDPOINT, Global.PREFIXES, null);
                }
            }
            // Checkpoint reached, this is a code to evaluate and save axioms in output file
            if (parameters.populationSize * curGeneration == parameters.kBase * curCheckpoint) {

                if(RDFMiner.mode.isAxiomMode()) {
                    List<JSONObject> content = new ArrayList<>();
//                    logger.info("\n\nDEBUG: candidatePopulation_size=" + candidatePopulation.size() + "\n");
//                    fit.display(candidatePopulation, content, curGeneration);
                    ArrayList<GEIndividual> candidatePopulation2 = new ArrayList<>();
                    for (GEIndividual geIndividual : candidatePopulation) {
                        GEIndividual indivi = new GEIndividual();
                        indivi.setMapper(geIndividual.getMapper());
                        indivi.setGenotype(geIndividual.getGenotype());
                        indivi.setPhenotype(geIndividual.getPhenotype());
                        indivi.setMapped(geIndividual.isMapped());
                        candidatePopulation2.add(indivi);
                    }
                    logger.info("Evaluating axioms against to the RDF Data of the whole DBPedia.");
                    fit.updatePopulation(candidatePopulation2, Global.SPARQL_ENDPOINT, Global.PREFIXES, content);
                    RDFMiner.content.addAll(content);
                } else {
                    assert fit instanceof ShapeFitnessEvaluation;
                    for(Shape shape : ((ShapeFitnessEvaluation) fit).getShapes()) {
                        RDFMiner.content.add(shape.toJSON());
                    }
                }

                curCheckpoint++;
            }

            ArrayList<GEIndividual> distinctCandidatePopulation = EATools.getDistinctPopulation(candidatePopulation);
            ArrayList<GEIndividual> distinctGenotypeCandidatePopulation = EATools
                    .getDistinctGenotypePopulation(candidatePopulation);

            GenerationJSON generation = new GenerationJSON();
            generation.idGeneration = curGeneration;
            generation.numSuccessMapping = stat.getCountSuccessMapping(distinctCandidatePopulation);
            generation.diversityCoefficient = (double) distinctCandidatePopulation.size() / candidatePopulation.size();
            generation.genotypeDiversityCoefficient = (double) distinctGenotypeCandidatePopulation.size()
                    / candidatePopulation.size();
            generation.averageFitness = stat.computeAverageFitness(distinctCandidatePopulation);
            generation.numComplexAxiom = stat.getCountComplexAxiom(distinctCandidatePopulation);
            generation.numComplexAxiomSpecial = stat.getCountComplexAxiomSpecial(distinctCandidatePopulation);
            RDFMiner.stats.generations.add(generation.toJSON());
            // Log usefull stats concerning the algorithm evolution
            logger.info("Average fitness: " + generation.averageFitness);
            logger.info("Diversity coefficient: " + generation.diversityCoefficient);
            logger.info("Genotype diversity coefficient: " + generation.genotypeDiversityCoefficient);

            if (curGeneration * parameters.populationSize < parameters.kBase * parameters.checkpoint) {
                // if4
                // STEP 3 - SELECTION OPERATION - Reproduce Selection - Parent Selection
                ArrayList<GEIndividual> crossoverPopulation, selectedPopulation;
                if (parameters.elitism == 1) {
                    // Elitism method, which copies the best chromosome( or a few best
                    // chromosome) to new population. The rest done classical way. it
                    // prevents losing the best found solution
                    logger.info("Selecting elite individuals...");
                    logger.info("Selecting + " + (int) (parameters.sizeElite * 100)
                            + "% elite individuals for the new population");
                    logger.info("The size of elite population: " + sizeElite);
                    EliteSelection elite = new EliteSelection(sizeElite);
                    elite.setParentsSelectionElitism(distinctCandidatePopulation);
                    selectedPopulation = elite.setupSelectedPopulation(distinctCandidatePopulation);
                    etilismPopulation = elite.getElitedPopulation();

                } else {
                    selectedPopulation = distinctCandidatePopulation;
                    sizeElite = 0;
                }
                // set the type selection
                crossoverPopulation = EATools.getTypeSelection(parameters.typeSelect, selectedPopulation, sizeElite, sizeSelection);
                if(crossoverPopulation == null) {
                    crossoverPopulation = candidatePopulation;
                }

                /* STEP 4 - CROSSOVER OPERATION */
                // Crossover single point between 2 individuals of the selected population
                ArrayList<GEIndividual> crossoverList = new ArrayList<>(crossoverPopulation);
                // shuffle populations before crossover & mutation
                java.util.Collections.shuffle(crossoverList);

                // Add new population on a new list of individuals
                ArrayList<GEIndividual> newPopulation = EATools.computeGeneration(crossoverList,
                        parameters.proCrossover, parameters.proMutation, curGeneration, generator,
                        parameters.diversity, RDFMiner.mode);

                candidatePopulation = canPop.renew(newPopulation, curGeneration, etilismPopulation);
                // Turn to the next generation
                curGeneration++;

                // Write to buffer file
                PrintWriter writer = new PrintWriter(
                        RDFMiner.outputFolder + "buffer_size" + parameters.populationSize + ".txt", StandardCharsets.UTF_8);
                writer.println(curGeneration);
                writer.println(curCheckpoint);
                flag = true;
                for (GEIndividual geIndividual : candidatePopulation) {
                    writer.println(geIndividual.getGenotype().toString().substring(22,
                            geIndividual.getGenotype().toString().length() - 1));
                }
                writer.close();
            } else {
                logger.info("Evolutionary process is done...");
                break;
            }
        }
        System.exit(0);
    }

    public static void writeAndFinish(Mode mode) {
        try {
            RDFMiner.results.stats = RDFMiner.stats.toJSON();
            // sort axioms (by ARI or Generality) using type of axioms
            if(mode.isAxiomMode()) {
                if (RDFMiner.type == Type.DISJOINT_CLASSES)
                    logger.info("sort axioms by generality ...");
                else
                    logger.info("sort axioms by ARI ...");
//                logger.info("DEBUG: size_content=" + RDFMiner.content.size());
                RDFMiner.content.sort(Comparator.comparingDouble(j -> {
                    // if we have disjoint classes axioms, we need to sort using generality
                    if (RDFMiner.type == Type.DISJOINT_CLASSES) {
                        return j.getInt("generality");
                    }
                    return j.getDouble("ari");
                }));
            }
            RDFMiner.results.content = RDFMiner.content;
            RDFMiner.output.write(RDFMiner.results.toJSON().toString());
            RDFMiner.output.close();
        } catch (IOException e) {
            logger.error("I/O error while closing JSON writer: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

}
