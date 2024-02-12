package com.i3s.app.rdfminer.evolutionary.fitness.novelty;

import com.i3s.app.rdfminer.Parameters;
import com.i3s.app.rdfminer.RDFminer;
import com.i3s.app.rdfminer.entity.Entity;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.FitnessPackage.BasicFitness;
import com.i3s.app.rdfminer.sparql.corese.CoreseEndpoint;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * The novelty search is a new technic in order to reward individuals that are very different of others.
 *
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
     *
     * @param entities a population composed of entities
     * @return an updated population
     */
    public ArrayList<Entity> update(List<Entity> entities) throws URISyntaxException, IOException {
        logger.info("Updating similarities between individuals ...");
        ArrayList<Entity> simEntities = new ArrayList<>();
        int countNewSim = 0;
        for (int i = 0; i < entities.size(); i++) {
            for (int j = 0; j < entities.size(); j++) {
                if (i != j) {
                    // use similarity cache
                    if (RDFminer.similarityMap.get(entities.get(i), entities.get(j)) != null) {
//                        logger.debug("get similarity value from similarity map ...");
                        entities.get(i).similarities.add(RDFminer.similarityMap.get(entities.get(i), entities.get(j)));
                    } else {
                        Similarity sim = new Similarity(entities.get(i), entities.get(j));
                        double simValue = sim.getModifiedSimilarity(endpoint);
                        entities.get(i).similarities.add(simValue);
                        RDFminer.similarityMap.append(entities.get(i), entities.get(j), simValue);
                        countNewSim++;
                    }
                }
            }
            simEntities.add(entities.get(i));
        }
        logger.info(countNewSim + " new similarities has been added into map");
        logger.info("updating the fitness ...");
        ArrayList<Entity> updatedEntities = new ArrayList<>();
        for (Entity e : simEntities) {
            e.individual.setFitness(new BasicFitness(updateFitness(e), e.individual));
            updatedEntities.add(e);
        }
        logger.info("Done !");
        return updatedEntities;
    }

    public double getDistanceOfEntityFromPopulation(Entity entity, ArrayList<Entity> population) throws URISyntaxException, IOException {
        double distance = 0;
        for(Entity individual : population) {
            distance += new Similarity(entity, individual).getModifiedSimilarity(endpoint);
        }
        return distance;
    }

//    public static Entity updateSimilarity(CoreseEndpoint endpoint, Entity entity, ArrayList<Entity> entities) throws URISyntaxException, IOException {
//        logger.debug("Update the similarity of axiom '" + entity.entityAsString + "' among its population to consider novelty approach");
//        for(Entity other : entities) {
//            if(!Objects.equals(entity.individual.getGenotype().toString(), other.individual.getGenotype().toString())) {
//                entity.similarities.add(Similarity.getNormalizedSimilarity(endpoint, entity, other));
//            }
//        }
//        return entity;
//    }

    /**
     * compute the fitness value <var>f</var>(&phi) in the Novelty Search context using this formula:<br><br>
     * <center> ((&radic&#8741 &phi &#8741) &times (&Pi(&phi) + N(&phi)) &frasl; 2) &times
     * (1 &frasl (1 + &sum sim<sub>j</sub>(&phi)) </center>
     *
     * @return the value of based novelty fitness <var>f</var>(&phi)
     */
    public double updateFitness(Entity phi) {
        Parameters parameters = Parameters.getInstance();
        if (!parameters.isUseNoveltySearch()) {
            return phi.individual.getFitness().getDouble();
        }
//        logger.debug(phi.individual.getGenotype() + ": sum(sim)= " + );
        double sumSim = 0;
        for(double sim : phi.similarities) {
            sumSim += sim;
        }
        // reset phi similarities and avoid side behaviors
        phi.similarities.clear();
        // return updated fitness
        return phi.individual.getFitness().getDouble() * (1 / (1 + sumSim));
    }

    public double getScore(Entity phi, double distance) {
        return phi.individual.getFitness().getDouble() * (1 / (1 + distance));
    }

//    public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException, ExecutionException, TimeoutException {
//        // Load librdfminer_axiom_Axiom.so generated by ./compile_c_code.sh (see /scripts folder)
//        System.loadLibrary(Global.SO_LIBRARY);
//        // Configure the log4j loggers:
//        PropertyConfigurator.configure("/home/rfelin/projects/RDFMining/RDFMiner/code/resources/log4j.properties");
//
//        parameters.loop = true;
//        parameters.sparqlTimeOut = 1000000;
//
//        RDFMiner.similarityMap = new SimilarityMap(new File("/user/rfelin/home/projects/RDFMining/RDFMiner/caches/axioms_similarity.json"));
//
//        CoreseEndpoint endpoint = new CoreseEndpoint("http://virtuoso:9000/sparql", Global.PREFIXES);
////        Axiom a = AxiomFactory.create(null, "SubClassOf(<http://dbpedia.org/ontology/InformationAppliance> <http://www.w3.org/2004/02/skos/core#Concept>)",
////                    endpoint);
//        // -0.6641085922800061
////        System.out.println(a.ari);
//
//        List<String> axiomsAsString = Arrays.asList(
////                "SubClassOf(<http://dbpedia.org/ontology/InformationAppliance> <http://www.w3.org/2004/02/skos/core#Concept>)",
////                "SubClassOf(<http://dbpedia.org/ontology/Monarch> <http://xmlns.com/foaf/0.1/Person>)",
////                "SubClassOf(<http://schema.org/Airport> <http://dbpedia.org/ontology/Place>)",
////                "SubClassOf(<http://dbpedia.org/ontology/SoccerClub> <http://dbpedia.org/ontology/Agent>)"
////                "SubClassOf(<http://dbpedia.org/ontology/Bone> <http://dbpedia.org/ontology/AnatomicalStructure>)",
//                "SubClassOf(<http://dbpedia.org/ontology/SoccerClub> <http://schema.org/Organization>)",
////                "SubClassOf(<http://dbpedia.org/ontology/SoccerClub> <http://schema.org/Organization>)"
//                "SubClassOf(<http://dbpedia.org/ontology/SoccerClub> <http://dbpedia.org/ontology/River>)"
////                "SubClassOf(<http://dbpedia.org/ontology/SoccerClub> <http://dbpedia.org/ontology/University>)"
////                "SubClassOf(<http://dbpedia.org/ontology/Bone> <http://dbpedia.org/ontology/AnatomicalStructure>)"
////                "SubClassOf(<http://dbpedia.org/ontology/SoccerClub> <http://schema.org/Organization>)"
//        );
//        ArrayList<Entity> axioms = new ArrayList<>();
//
////        // We have a set of threads to compute each axioms
////        ExecutorService executor = Executors.newFixedThreadPool(Global.NB_THREADS);
////        // test future get
////        List<Callable<Axiom>> callables = new ArrayList<>();
////        callables.add(() -> AxiomFactory.create(null, axiomsAsString.get(0), endpoint));
////        Future<Axiom> futures = executor.submit(callables.get(0));
////        Axiom test = futures.get(2000, TimeUnit.MILLISECONDS);
////        System.out.println(test);
//
////        String test1 = "SubClassOf(<http://dbpedia.org/ontology/Bone> <http://dbpedia.org/ontology/AnatomicalStructure>)";
////        String test2 = "SubClassOf(<http://dbpedia.org/ontology/SoccerClub> <http://schema.org/Organization>)";
////        logger.info("hascode of SubClassOf(<http://dbpedia.org/ontology/Bone> <http://dbpedia.org/ontology/AnatomicalStructure>) ~> " + test1.hashCode());
////        logger.info("hashcode of SubClassOf(<http://dbpedia.org/ontology/SoccerClub> <http://schema.org/Organization>) ~> " + test2.hashCode());
//
//        for (String axiom : axiomsAsString) {
//            Axiom a = AxiomFactory.create(null, axiom, endpoint);
//            a.individual = new GEIndividual();
//            axioms.add(a);
//        }
//
//        for (int i = 0; i < axioms.size(); i++) {
//            for (int j = 0; j < axioms.size(); j++) {
//                if (i != j) {
//                    Similarity sim = new Similarity(axioms.get(i), axioms.get(j));
//                    logger.info("v0.1 similarity = " + sim.getJaccardSimilarity(endpoint));
//                    logger.info("New similarity = " + sim.getModifiedSimilarity(endpoint));
//                }
//            }
//        }
////            logger.info("similarities for " + a.argumentClasses + ": " + a.similarities);
////            logger.info("sum(simJ) = " + a.similarities.stream().mapToDouble(x -> x).sum());
////            logger.info("sqrt(refCard): " + Math.sqrt(a.referenceCardinality));
////            NoveltyFitness fitnessForA = new NoveltyFitness(a);
////            ObjectivesFitness.setFitness(a);
////            NoveltyFitness.updateFitness(a);
////            logger.info("fitness value before update: " + a.fitness);
////            a.fitness = updateFitness(a);
////            logger.info("fitness value after update: " + a.fitness);
//    }

}
