package com.i3s.app.rdfminer.evolutionary.fitness.entity;

import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.RDFMiner;
import com.i3s.app.rdfminer.entity.Entity;
import com.i3s.app.rdfminer.entity.shacl.Shape;
import com.i3s.app.rdfminer.entity.shacl.ShapesManager;
import com.i3s.app.rdfminer.entity.shacl.ValidationReport;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.GEChromosome;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.GEIndividual;
import com.i3s.app.rdfminer.generator.Generator;
import com.i3s.app.rdfminer.generator.shacl.RandomShapeGenerator;
import com.i3s.app.rdfminer.launcher.evaluator.ExtendedShacl;
import com.i3s.app.rdfminer.sparql.corese.CoreseEndpoint;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * It is the class to setup the fitness value for SHACL Shapes in the
 * specified population
 *
 * @author Rémi FELIN
 */
public class ShapeFitnessEvaluation implements FitnessEvaluation {

    private static final Logger logger = Logger.getLogger(ShapeFitnessEvaluation.class.getName());

    @Override
    public ArrayList<Entity> initializePopulation(ArrayList<GEIndividual> individuals) {
        // evaluation of SHACL Shapes
        try {
            ShapesManager shapesManager = new ShapesManager(individuals);
            logger.info(shapesManager.getPopulation().size() + " SHACL Shapes ready to be evaluated !");
            // launch evaluation
            CoreseEndpoint endpoint = new CoreseEndpoint(Global.TARGET_SPARQL_ENDPOINT, Global.PREFIXES);
            String report = endpoint.getValidationReportFromServer(shapesManager.content);
            // read evaluation report
            ValidationReport validationReport = new ValidationReport(report);
            // For each SHACL Shapes individuals, we set all results of them
            ArrayList<Entity> newPopulation = new ArrayList<>();
            for(Shape shape : shapesManager.getPopulation()) {
                shape.fillParamFromReport(validationReport);
//                BasicFitness fit = new BasicFitness(shape.fitness, shape);
//                fit.setIndividual(shape);
//                fit.getIndividual().setValid(true);
//                shape.setFitness(fit);
                newPopulation.add(shape);
//                if(shape.individual.getFitness().getDouble() != 0)
//                    logger.debug("i: " + shape.individual.getGenotype() + " ~ F(i)= " + shape.individual.getFitness().getDouble());
            }
            logger.info("Done");
            // return new population
            return newPopulation;
        } catch (IOException e) {
            logger.error("I/O exceptions while evaluating SHACL Shapes ...");
            logger.error(e.getMessage());
            System.exit(1);
        } catch (URISyntaxException e) {
            logger.error("URI Syntax error while evaluating SHACL Shapes ...");
            logger.error(e.getMessage());
            System.exit(1);
        }
        return null;
    }

    public ArrayList<Entity> updatePopulation(ArrayList<Entity> population) {
        // evaluation of SHACL Shapes
        try {
            // set content in shape manager
            ShapesManager shapesManager = new ShapesManager();
            // last population evaluation of shapes mining
            // we will assess distinct shapes from population (avoid duplication)
            shapesManager.setDistinctPopulationFromEntities(population);
            logger.info(shapesManager.getPopulation().size() + " distinct SHACL Shapes ready to be evaluated !");
            // launch evaluation
            CoreseEndpoint endpoint = new CoreseEndpoint(Global.TARGET_SPARQL_ENDPOINT, Global.PREFIXES);
//            System.out.println(shapesManager.content);
            String report = endpoint.getValidationReportFromServer(shapesManager.content);
            // read evaluation report
            ValidationReport validationReport = new ValidationReport(report);
//            System.out.println(validationReport.prettifyPrint());
            // For each SHACL Shapes individuals, we set all results of them
            ArrayList<Entity> newPop = new ArrayList<>();
            for(Shape shape : shapesManager.getPopulation()) {
                shape.fillParamFromReport(validationReport);
                // set the fitness of each individuals provided by the population
//                BasicFitness fit = new BasicFitness(shape.fitness, shape);
//                fit.setIndividual(shape);
//                fit.getIndividual().setValid(true);
//                shape.setFitness(fit);
                newPop.add(shape);
            }
            // write validation report in file
            FileWriter fw = new FileWriter(RDFMiner.outputFolder + Global.SHACL_VALIDATION_REPORT_FILENAME);
            fw.write(validationReport.prettifyPrint());
            fw.close();
            // run extended shacl
            ExtendedShacl.runWithoutEval(validationReport, shapesManager);
            // return new population
            return newPop;
        } catch (IOException e) {
            logger.error("I/O exceptions while evaluating SHACL Shapes ...");
            logger.error(e.getMessage());
            System.exit(1);
        } catch (URISyntaxException e) {
            logger.error("URI Syntax error while evaluating SHACL Shapes ...");
            logger.error(e.getMessage());
            System.exit(1);
        }
        return null;
    }

    /**
     * Not used in this context
     */
    @Override
    public Entity updateIndividual(GEIndividual individual) {
        // evaluation of SHACL Shapes
        try {
            // fill our population in a tmp file
            // and init shapes manager
            ShapesManager shapesManager = new ShapesManager(new ArrayList<>(List.of(individual)));
            // launch evaluation
            CoreseEndpoint endpoint = new CoreseEndpoint(Global.TARGET_SPARQL_ENDPOINT, Global.PREFIXES);
            String report = endpoint.getValidationReportFromServer(shapesManager.content);
            // read evaluation report
            ValidationReport validationReport = new ValidationReport(report);
            Shape shape = shapesManager.getPopulation().get(0);
            // set all results
            shape.setIndividual(individual);
            shape.fillParamFromReport(validationReport);
//            logger.debug("-----------------------------------------");
//            logger.debug("phenotype: " + shape.individual.getPhenotype().getStringNoSpace());
//            logger.debug("genotype: " + shape.individual.getGenotype().toString());
//            logger.debug("refCard: " + shape.referenceCardinality);
//            logger.debug("numConf: " + shape.numConfirmations);
//            logger.debug("fitness: " + shape.individual.getFitness().getDouble());
            return shape;
        } catch (IOException e) {
            logger.error("I/O exceptions while evaluating SHACL Shapes ...");
            logger.error(e.getMessage());
            System.exit(1);
        } catch (URISyntaxException e) {
            logger.error("URI Syntax error while evaluating SHACL Shapes ...");
            logger.error(e.getMessage());
            System.exit(1);
        }
        return null;
    }

    public static void main(String[] args) throws URISyntaxException, IOException {
        // Load librdfminer_axiom_Axiom.so generated by ./compile_c_code.sh (see /scripts folder)
        System.loadLibrary(Global.SO_LIBRARY);
        RDFMiner.parameters.useProbabilisticShaclMode = true;
        RDFMiner.parameters.probShaclP = String.valueOf(0.5);
        // Configure the log4j loggers:
        PropertyConfigurator.configure("/home/rfelin/projects/RDFMining/RDFMiner/code/resources/log4j.properties");
        // corese
        Generator generator = null;
        try {
            generator = new RandomShapeGenerator("/user/rfelin/home/projects/RDFMining/IO/shacl-shapes-test.bnf");
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
        // test
//        Fitness.computeEntity(shapesManager.getPopulation().get(0).individual, generator);
        // launch evaluation
        GEChromosome chrom = new GEChromosome(2);
        chrom.setMaxCodonValue(RDFMiner.parameters.maxValCodon);
        chrom.setMaxChromosomeLength(1000);
        chrom.add(537439393);
        chrom.add(537439393);
        GEIndividual individual = generator.getIndividualFromChromosome(chrom, 1);
        ShapesManager shapesManager1 = new ShapesManager(new ArrayList<>(List.of(individual)));
        CoreseEndpoint endpoint1 = new CoreseEndpoint(Global.CORESE_IP, Global.PREFIXES);
//        System.out.println(shapesManager1.content);
        String report1 = endpoint1.getValidationReportFromServer(shapesManager1.content);
        // read evaluation report
        ValidationReport validationReport = new ValidationReport(report1);
        Shape shape = shapesManager1.getPopulation().get(0);
        // set all results
        shape.fillParamFromReport(validationReport);
        logger.debug("-----------------------------------------");
        logger.debug("phenotype: " + individual.getPhenotype().getStringNoSpace());
        logger.debug("shape.phenotype: " +  shape.individual.getPhenotype().getStringNoSpace());
        logger.debug("genotype: " + individual.getGenotype().toString());
        logger.debug("shape.genotype: " +  shape.individual.getGenotype().toString());
        logger.debug("fitness: " + individual.getFitness().getDouble());
        logger.debug("shape.fitness: " + shape.individual.getFitness().getDouble());
        logger.debug("is trivial ? " + shape.isTrivial());
    }

}
