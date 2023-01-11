package com.i3s.app.rdfminer.grammar.evolutionary;

import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.RDFMiner;
import com.i3s.app.rdfminer.entity.axiom.Axiom;
import com.i3s.app.rdfminer.entity.axiom.AxiomFactory;
import com.i3s.app.rdfminer.grammar.evolutionary.fitness.novelty.Similarity;
import com.i3s.app.rdfminer.sparql.corese.CoreseEndpoint;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
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
     * Update similarities between individuals from a given population to consider novelty approach
     * @param axioms a population composed of OWL Axioms
     * @return an updated population
     * @throws URISyntaxException
     * @throws IOException
     */
    public ArrayList<Axiom> updateSimilarities(ArrayList<Axiom> axioms) throws URISyntaxException, IOException {
        logger.info("Update similarities between individuals from a given population to consider novelty approach");
        ArrayList<Axiom> updatedAxioms = new ArrayList<>();
        for(Axiom phi1 : axioms) {
            for(Axiom phi2 : axioms) {
                if(!Objects.equals(phi1.entityAsString, phi2.entityAsString)) {
                    phi1.similarities.add(Similarity.getJaccardSimilarity(endpoint, phi1, phi2));
                }
            }
//            logger.info("similarities for " + phi1.argumentClasses + ": " + phi1.similarities);
//            double fitness = new NoveltyFitness(phi1).getFitness();
//            logger.info("fitness value = " + fitnessForA.getFitness());
//            BasicFitness fit = new BasicFitness(fitness, phi1.individual);
//            fit.getIndividual().setValid(true);
//            phi1.individual.setFitness(fit);
//            phi1.fitness = fitness;
            updatedAxioms.add(phi1);
        }
        return updatedAxioms;
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

    public static void main(String[] args) throws URISyntaxException, IOException {
        // Load librdfminer_axiom_Axiom.so generated by ./compile_c_code.sh (see /scripts folder)
        System.loadLibrary("rdfminer_entity_axiom_Axiom");
        // Configure the log4j loggers:
        PropertyConfigurator.configure("/home/rfelin/projects/RDFMining/RDFMiner/code/resources/log4j.properties");

        RDFMiner.parameters.loop = false;
        RDFMiner.parameters.timeOut = 10000;

        CoreseEndpoint endpoint = new CoreseEndpoint(Global.CORESE_SPARQL_ENDPOINT, "http://134.59.130.136:8890/sparql", Global.PREFIXES);
        Axiom a = AxiomFactory.create(null, "SubClassOf(<http://dbpedia.org/ontology/InformationAppliance> <http://www.w3.org/2004/02/skos/core#Concept>)",
                    endpoint);
        // -0.6641085922800061
//        System.out.println(a.ari);

//        List<String> axiomsAsString = Arrays.asList(
////                "SubClassOf(<http://dbpedia.org/ontology/InformationAppliance> <http://www.w3.org/2004/02/skos/core#Concept>)",
////                "SubClassOf(<http://dbpedia.org/ontology/Monarch> <http://xmlns.com/foaf/0.1/Person>)",
////                "SubClassOf(<http://schema.org/Airport> <http://dbpedia.org/ontology/Place>)",
////                "SubClassOf(<http://dbpedia.org/ontology/SoccerClub> <http://dbpedia.org/ontology/Agent>)"
//                "SubClassOf(<http://dbpedia.org/ontology/SoccerClub> <http://schema.org/Organization>)",
//                "SubClassOf(<http://dbpedia.org/ontology/SoccerClub> <http://dbpedia.org/ontology/River>)",
//                "SubClassOf(<http://dbpedia.org/ontology/SoccerClub> <http://dbpedia.org/ontology/University>)",
//                "SubClassOf(<http://dbpedia.org/ontology/Monarch> <http://dbpedia.org/ontology/Person>)"
//
//        );
//        ArrayList<Axiom> axioms = new ArrayList<>();
//
//        for(String axiom : axiomsAsString) {
//            Axiom a = AxiomFactory.create(null, axiom,
//                    new CoreseEndpoint(Global.CORESE_SPARQL_ENDPOINT, "http://134.59.130.136:8890/sparql", Global.PREFIXES)
//            );
//            axioms.add(a);
//        }
//
//        CoreseEndpoint endpoint = new CoreseEndpoint(Global.CORESE_SPARQL_ENDPOINT, "http://134.59.130.136:8890/sparql", Global.PREFIXES);
//
//        for(Axiom a : axioms) {
//            for(Axiom b : axioms) {
//                if(a.argumentClasses.get(0).get(0) != b.argumentClasses.get(0).get(0)) {
//                    a.similarities.add(Similarity.getJaccardSimilarity(endpoint, a, b));
//                }
//            }
//            logger.info("similarities for " + a.argumentClasses + ": " + a.similarities);
////            NoveltyFitness fitnessForA = new NoveltyFitness(a);
//            ObjectivesFitness.setFitness(a);
//            NoveltyFitness.updateFitness(a);
//            logger.info("fitness value = " + a.fitness);
//        }

    }

}
