package com.i3s.app.rdfminer.grammar.evolutionary.fitness;

import Individuals.FitnessPackage.BasicFitness;
import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.grammar.evolutionary.individual.GEIndividual;
import com.i3s.app.rdfminer.entity.shacl.Shape;
import com.i3s.app.rdfminer.entity.shacl.ShapesManager;
import com.i3s.app.rdfminer.entity.shacl.ValidationReport;
import com.i3s.app.rdfminer.sparql.corese.CoreseEndpoint;
import com.i3s.app.rdfminer.sparql.corese.CoreseService;
import org.apache.log4j.Logger;
import org.json.JSONObject;

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
public class ShapeFitnessEvaluation {

    private static final Logger logger = Logger.getLogger(ShapeFitnessEvaluation.class.getName());

//    public ArrayList<Shape> shapes;
//
////    public CoreseEndpoint endpoint;
//
//    public ShapeFitnessEvaluation() {
//        shapes = new ArrayList<>();
//    }

    /**
     * Update a given population by using evaluation of each individual and fill data
     * @param population       a given population
     */
    public static ArrayList<GEIndividual> updatePopulation(ArrayList<GEIndividual> population) {
        // evaluation of SHACL Shapes
        try {
            ShapesManager shapesManager = new ShapesManager(population);
            // launch evaluation
            CoreseEndpoint endpoint = new CoreseEndpoint(Global.SPARQL_ENDPOINT, Global.PREFIXES);
            String report = endpoint.getValidationReportFromServer(shapesManager.file, CoreseService.PROBABILISTIC_SHACL_EVALUATION);
            // read evaluation report
            ValidationReport validationReport = new ValidationReport(report);
//            System.out.println(validationReport.prettifyPrint());
            // For each SHACL Shapes individuals, we set all results of them
            ArrayList<GEIndividual> newPop = new ArrayList<>();
            for(Shape shape : shapesManager.getPopulation()) {
                shape.fillParamFromReport(validationReport);
                // Add JSON Result in the content
//                this.shapes.add(shape);
                // set the fitness of each individuals provided by the population
                BasicFitness fit = new BasicFitness((Double) shape.fitness, shape.individual);
                fit.setIndividual(shape.individual);
                fit.getIndividual().setValid(true);
                shape.individual.setFitness(fit);
//                shape.toJSON();
                newPop.add(shape.individual);
            }
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

    public static ArrayList<Shape> initializePopulation(ArrayList<GEIndividual> candidatePopulation) {
        // evaluation of SHACL Shapes
        try {
            ShapesManager shapesManager = new ShapesManager(candidatePopulation);
            // launch evaluation
            CoreseEndpoint endpoint = new CoreseEndpoint(Global.SPARQL_ENDPOINT, Global.PREFIXES);
            String report = endpoint.getValidationReportFromServer(shapesManager.file, CoreseService.PROBABILISTIC_SHACL_EVALUATION);
            // read evaluation report
            ValidationReport validationReport = new ValidationReport(report);
            // For each SHACL Shapes individuals, we set all results of them
            ArrayList<Shape> newPop = new ArrayList<>();
            for(Shape shape : shapesManager.getPopulation()) {
                shape.fillParamFromReport(validationReport);
                BasicFitness fit = new BasicFitness((Double) shape.fitness, shape.individual);
                fit.setIndividual(shape.individual);
                fit.getIndividual().setValid(true);
                shape.individual.setFitness(fit);
                newPop.add(shape);
            }
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

//    public ArrayList<Shape> getShapes() {
//        return this.shapes;
//    }

}
