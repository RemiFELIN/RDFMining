package com.i3s.app.rdfminer.grammar.evolutionary.fitness;

import Individuals.FitnessPackage.BasicFitness;
import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.entity.Entity;
import com.i3s.app.rdfminer.entity.shape.Shape;
import com.i3s.app.rdfminer.entity.shape.ShapesManager;
import com.i3s.app.rdfminer.entity.shape.ValidationReport;
import com.i3s.app.rdfminer.sparql.corese.CoreseEndpoint;
import com.i3s.app.rdfminer.sparql.corese.CoreseService;
import org.apache.log4j.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

/**
 * It is the class to setup the fitness value for SHACL Shapes in the
 * specified population
 *
 * @author Rémi FELIN
 */
public class ShapeFitnessEvaluation implements FitnessEvaluation {

    private static final Logger logger = Logger.getLogger(ShapeFitnessEvaluation.class.getName());

    public ArrayList<Entity> updatePopulation(ArrayList<Entity> population) {
        // evaluation of SHACL Shapes
        try {
            // fill our population in a tmp file
            // and init shapes manager
            ShapesManager shapesManager = new ShapesManager(editShapesTmpFile(population));
            // launch evaluation
            CoreseEndpoint endpoint = new CoreseEndpoint(Global.TARGET_SPARQL_ENDPOINT, Global.PREFIXES);
            String report = endpoint.getValidationReportFromServer(shapesManager.file, CoreseService.PROBABILISTIC_SHACL_EVALUATION);
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
    public Entity updateIndividual(Entity entity) {
        return null;
    }

    /**
     * Store a given population in a temporary file
     * @param population a given population of entities
     * @return the path of the temporary file
     */
    public Path editShapesTmpFile(ArrayList<Entity> population) throws IOException {
        // create temp file
        Path tmpPath = Files.createTempFile("shapes", ".ttl");
        // edit this file
        FileWriter fw = new FileWriter(tmpPath.toFile());
        // edit turtle file which will contains shapes
        // set prefixes
        fw.write(Global.PREFIXES);
        for(Entity entity : population) {
            // write phenotype individuals
            fw.write(entity.individual.getPhenotype().getString());
        }
        fw.close();
        return tmpPath;
    }

}
