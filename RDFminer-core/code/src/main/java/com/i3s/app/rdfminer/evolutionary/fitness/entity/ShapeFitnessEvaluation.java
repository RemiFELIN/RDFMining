package com.i3s.app.rdfminer.evolutionary.fitness.entity;

import com.i3s.app.rdfminer.Parameters;
import com.i3s.app.rdfminer.entity.Entity;
import com.i3s.app.rdfminer.entity.shacl.Shape;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.GEIndividual;
import com.i3s.app.rdfminer.sparql.corese.CoreseEndpoint;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

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
        Parameters parameters = Parameters.getInstance();
        // evaluation of SHACL Shapes
        try {
//            ShapesManager shapesManager = new ShapesManager(individuals);
            logger.info(individuals.size() + " SHACL Shapes ready to be evaluated !");
            // launch evaluation
            CoreseEndpoint endpoint = new CoreseEndpoint(parameters.getNamedDataGraph(), parameters.getPrefixes());
            // For each SHACL Shapes individuals, we instanciate SHACL shape
            ArrayList<Entity> newPopulation = new ArrayList<>();
            for(GEIndividual individual : individuals) {
                newPopulation.add(new Shape(individual, endpoint));
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
        Parameters parameters = Parameters.getInstance();
        // evaluation of SHACL Shapes
        try {
            logger.info(population.size() + " distinct SHACL Shapes ready to be evaluated !");
            // set endpoint
            CoreseEndpoint endpoint = new CoreseEndpoint(parameters.getNamedDataGraph(), parameters.getPrefixes());
            // For each SHACL Shapes individuals, we set all results of them
            ArrayList<Entity> newPop = new ArrayList<>();
            // iterate on entities
            for(Entity entity : population) {
                // add into newPop
                newPop.add(new Shape(entity.individual, endpoint));
            }
            // write validation report in file
//            FileWriter fw = new FileWriter(RDFminer.outputFolder + Global.SHACL_VALIDATION_REPORT_FILENAME);
//            fw.close();
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
        Parameters parameters = Parameters.getInstance();
        // evaluation of SHACL Shapes
        try {
            CoreseEndpoint endpoint = new CoreseEndpoint(parameters.getNamedDataGraph(), parameters.getPrefixes());
            return new Shape(individual, endpoint);
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

}
