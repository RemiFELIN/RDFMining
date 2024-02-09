package com.i3s.app.rdfminer.evolutionary.fitness.entity;

import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.RDFMiner;
import com.i3s.app.rdfminer.entity.Entity;
import com.i3s.app.rdfminer.entity.shacl.Shape;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.GEIndividual;
import com.i3s.app.rdfminer.sparql.corese.CoreseEndpoint;
import org.apache.log4j.Logger;

import java.io.FileWriter;
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
        // evaluation of SHACL Shapes
        try {
//            ShapesManager shapesManager = new ShapesManager(individuals);
            logger.info(individuals.size() + " SHACL Shapes ready to be evaluated !");
            // launch evaluation
            CoreseEndpoint endpoint = new CoreseEndpoint(Global.TARGET_SPARQL_ENDPOINT, Global.PREFIXES);
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
        // evaluation of SHACL Shapes
        try {
            logger.info(population.size() + " distinct SHACL Shapes ready to be evaluated !");
            // set endpoint
            CoreseEndpoint endpoint = new CoreseEndpoint(Global.TARGET_SPARQL_ENDPOINT, Global.PREFIXES);
            // For each SHACL Shapes individuals, we set all results of them
            ArrayList<Entity> newPop = new ArrayList<>();
            // iterate on entities
            for(Entity entity : population) {
                // add into newPop
                newPop.add(new Shape(entity.individual, endpoint));
            }
            // write validation report in file
            FileWriter fw = new FileWriter(RDFMiner.outputFolder + Global.SHACL_VALIDATION_REPORT_FILENAME);
            fw.close();
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
            CoreseEndpoint endpoint = new CoreseEndpoint(Global.TARGET_SPARQL_ENDPOINT, Global.PREFIXES);
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

//    public static void main(String[] args) throws URISyntaxException, IOException {
//        // Load librdfminer_axiom_Axiom.so generated by ./compile_c_code.sh (see /scripts folder)
//        System.loadLibrary(Global.SO_LIBRARY);
//        RDFMiner.parameters.useProbabilisticShaclMode = true;
//        RDFMiner.parameters.getProbShaclP() = String.valueOf(0.5);
//        // Configure the log4j loggers:
//        PropertyConfigurator.configure("/home/rfelin/projects/RDFMining/RDFMiner/code/resources/log4j.properties");
//        // corese
//        Generator generator = null;
//        try {
//            generator = new RandomShapeGenerator("/user/rfelin/home/projects/RDFMining/IO/shacl-shapes-test.bnf");
//        } catch (URISyntaxException | IOException e) {
//            e.printStackTrace();
//        }
//        // test
////        Fitness.computeEntity(shapesManager.getPopulation().get(0).individual, generator);
//        // launch evaluation
//        GEChromosome chrom = new GEChromosome(2);
//        chrom.setMaxCodonValue(RDFMiner.parameters.maxValCodon);
//        chrom.setMaxChromosomeLength(1000);
//        chrom.add(537439393);
//        chrom.add(537439393);
//        GEIndividual individual = generator.getIndividualFromChromosome(chrom);
////        ShapesManager shapesManager1 = new ShapesManager(individual);
//        CoreseEndpoint endpoint1 = new CoreseEndpoint(Global.CORESE_IP, Global.PREFIXES);
//        Shape shape = new Shape(individual, endpoint1);
////        System.out.println(shapesManager1.content);
////        String report1 = endpoint1.getValidationReportFromServer(shapesManager1.content);
//        // read evaluation report
////        ValidationReport validationReport = new ValidationReport(report1);
////        Shape shape = shapesManager1.getPopulation().get(0);
//        // set all results
////        shape.fillParamFromReport(validationReport);
//        logger.debug("-----------------------------------------");
//        logger.debug("phenotype: " + individual.getPhenotype().getStringNoSpace());
//        logger.debug("shape.phenotype: " +  shape.individual.getPhenotype().getStringNoSpace());
//        logger.debug("genotype: " + individual.getGenotype().toString());
//        logger.debug("shape.genotype: " +  shape.individual.getGenotype().toString());
//        logger.debug("fitness: " + individual.getFitness().getDouble());
//        logger.debug("shape.fitness: " + shape.individual.getFitness().getDouble());
//    }

}
