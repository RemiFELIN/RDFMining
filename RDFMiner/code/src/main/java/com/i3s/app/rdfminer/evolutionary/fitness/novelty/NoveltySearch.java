package com.i3s.app.rdfminer.evolutionary.fitness.novelty;

import Individuals.FitnessPackage.BasicFitness;
import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.RDFMiner;
import com.i3s.app.rdfminer.entity.Entity;
import com.i3s.app.rdfminer.entity.axiom.Axiom;
import com.i3s.app.rdfminer.entity.axiom.AxiomFactory;
import com.i3s.app.rdfminer.sparql.corese.CoreseEndpoint;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * The novelty search is a new technic in order to reward individuals that are very different of others.
 * @author Rémi FELIN
 */
public class NoveltySearch {

    private static final Logger logger = Logger.getLogger(NoveltySearch.class.getName());

    public CoreseEndpoint endpoint;

    public NoveltySearch(CoreseEndpoint endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * Update similarities between individuals from a given population and fitness values to consider novelty approach
     * @param entities a population composed of entities
     * @return an updated population
     */
    public ArrayList<Entity> update(List<Entity> entities) throws URISyntaxException, IOException {
        logger.info("Update similarities between individuals from a given population to consider novelty approach");
        ArrayList<Entity> simEntities = new ArrayList<>();
        for(Entity phi1 : entities) {
            for(Entity phi2 : entities) {
                if(!Objects.equals(phi1.individual.getGenotype().toString(), phi2.individual.getGenotype().toString())) {
                    phi1.similarities.add(Similarity.getJaccardSimilarity(endpoint, phi1, phi2));
                }
            }
            simEntities.add(phi1);
        }
        logger.info("update the fitness for each axioms considering their similarity");
        ArrayList<Entity> updatedEntities = new ArrayList<>();
        for(Entity e : simEntities) {
            e.fitness = updateFitness(e);
            e.individual.setFitness(new BasicFitness(e.fitness, e.individual));
            updatedEntities.add(e);
        }
        return updatedEntities;
    }

    public static Axiom updateSimilarity(CoreseEndpoint endpoint, Axiom axiom, ArrayList<Axiom> axioms) throws URISyntaxException, IOException {
        logger.info("Update the similarity of axiom '" + axiom.entityAsString + "' among its population to consider novelty approach");
        for(Axiom phi : axioms) {
            if(!Objects.equals(phi.entityAsString, axiom.entityAsString)) {
                axiom.similarities.add(Similarity.getJaccardSimilarity(endpoint, axiom, phi));
            }
        }
        return axiom;
    }

    /**
     * compute the fitness value <var>f</var>(&phi) in the Novelty Search context using this formula:<br><br>
     * <center> ((&radic&#8741 &phi &#8741) &times (&Pi(&phi) + N(&phi)) &frasl; 2) &times
     *                    (1 &frasl (1 + &sum sim<sub>j</sub>(&phi)) </center>
     * @return the value of based novelty fitness <var>f</var>(&phi)
     */
    public static double updateFitness(Entity phi) {
        return phi.fitness * (1 / (1 + phi.similarities.stream().mapToDouble(x -> x).sum()));
    }

    public static void main(String[] args) throws URISyntaxException, IOException {
        // Load librdfminer_axiom_Axiom.so generated by ./compile_c_code.sh (see /scripts folder)
        System.loadLibrary(Global.SO_LIBRARY);
        // Configure the log4j loggers:
        PropertyConfigurator.configure("/home/rfelin/projects/RDFMining/RDFMiner/code/resources/log4j.properties");

        RDFMiner.parameters.loop = false;
        RDFMiner.parameters.timeOut = 10000;

        CoreseEndpoint endpoint = new CoreseEndpoint(Global.CORESE_IP, "http://172.19.0.2:9000/sparql", Global.PREFIXES);
//        Axiom a = AxiomFactory.create(null, "SubClassOf(<http://dbpedia.org/ontology/InformationAppliance> <http://www.w3.org/2004/02/skos/core#Concept>)",
//                    endpoint);
        // -0.6641085922800061
//        System.out.println(a.ari);

        List<String> axiomsAsString = Arrays.asList(
//                "SubClassOf(<http://dbpedia.org/ontology/InformationAppliance> <http://www.w3.org/2004/02/skos/core#Concept>)",
//                "SubClassOf(<http://dbpedia.org/ontology/Monarch> <http://xmlns.com/foaf/0.1/Person>)",
//                "SubClassOf(<http://schema.org/Airport> <http://dbpedia.org/ontology/Place>)",
//                "SubClassOf(<http://dbpedia.org/ontology/SoccerClub> <http://dbpedia.org/ontology/Agent>)"
                "SubClassOf(<http://dbpedia.org/ontology/SoccerClub> <http://schema.org/Organization>)",
//                "SubClassOf(<http://dbpedia.org/ontology/SoccerClub> <http://dbpedia.org/ontology/River>)",
//                "SubClassOf(<http://dbpedia.org/ontology/SoccerClub> <http://dbpedia.org/ontology/University>)",
                "SubClassOf(<http://dbpedia.org/ontology/Monarch> <http://dbpedia.org/ontology/Animal>)"
//                "SubClassOf(<http://dbpedia.org/ontology/SoccerClub> <http://schema.org/Organization>)"
        );
        ArrayList<Axiom> axioms = new ArrayList<>();

        for(String axiom : axiomsAsString) {
            axioms.add(AxiomFactory.create(null, axiom, endpoint));
        }

        for(Axiom a : axioms) {
            for(Axiom b : axioms) {
                if(a.argumentClasses.get(0).get(0) != b.argumentClasses.get(0).get(0)) {
                    a.similarities.add(Similarity.getJaccardSimilarity(endpoint, a, b));
                }
            }
            logger.info("similarities for " + a.argumentClasses + ": " + a.similarities);
//            NoveltyFitness fitnessForA = new NoveltyFitness(a);
//            ObjectivesFitness.setFitness(a);
//            NoveltyFitness.updateFitness(a);
            logger.info("fitness value = " + a.fitness);
        }

    }

}
