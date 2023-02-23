package com.i3s.app.rdfminer.evolutionary.fitness.entity;

import Individuals.FitnessPackage.BasicFitness;
import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.RDFMiner;
import com.i3s.app.rdfminer.entity.Entity;
import com.i3s.app.rdfminer.entity.shacl.Shape;
import com.i3s.app.rdfminer.entity.shacl.ShapesManager;
import com.i3s.app.rdfminer.entity.shacl.ValidationReport;
import com.i3s.app.rdfminer.evolutionary.individual.GEIndividual;
import com.i3s.app.rdfminer.launcher.evaluator.ExtendedShacl;
import com.i3s.app.rdfminer.sparql.corese.CoreseEndpoint;
import com.i3s.app.rdfminer.sparql.corese.CoreseService;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
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
                BasicFitness fit = new BasicFitness(shape.fitness, shape.individual);
                fit.setIndividual(shape.individual);
                fit.getIndividual().setValid(true);
                shape.individual.setFitness(fit);
                newPopulation.add(shape);
            }
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
            logger.info(shapesManager.getPopulation().size() + " SHACL Shapes ready to be evaluated !");
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
                BasicFitness fit = new BasicFitness(shape.fitness, shape.individual);
                fit.setIndividual(shape.individual);
                fit.getIndividual().setValid(true);
                shape.individual.setFitness(fit);
                newPop.add(shape);
            }
            // run extended shacl
            ExtendedShacl.runWithoutEval(report, shapesManager);
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
            shape.fillParamFromReport(validationReport);
            // set fitness
            BasicFitness fit = new BasicFitness(shape.fitness, shape.individual);
            fit.setIndividual(shape.individual);
            fit.getIndividual().setValid(true);
            shape.individual.setFitness(fit);
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
        // Configure the log4j loggers:
        PropertyConfigurator.configure("/home/rfelin/projects/RDFMining/RDFMiner/code/resources/log4j.properties");
        // corese
        String content = "\n:1 a sh:NodeShape ; sh:targetClass <http://www.wikidata.org/entity/Q11435>  ;  sh:property [  sh:path rdf:type ; sh:hasValue <http://www.wikidata.org/entity/Q39833>  ;  ] .\n"
                + "\n:1 a sh:NodeShape ; sh:targetClass <http://www.wikidata.org/entity/Q11435>  ;  sh:property [  sh:path rdf:type ; sh:hasValue <http://www.wikidata.org/entity/Q39833>  ;  ] ."
                ;
        ShapesManager shapesManager = new ShapesManager(content, true);
        // launch evaluation
        CoreseEndpoint endpoint = new CoreseEndpoint(Global.CORESE_IP, Global.PREFIXES);
        String report = endpoint.getValidationReportFromServer(shapesManager.content);
        System.out.println(new ValidationReport(report).prettifyPrint());
    }

}
