package com.i3s.app.rdfminer.evolutionary.offspring;

import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.RDFMiner;
import com.i3s.app.rdfminer.entity.Entity;
import com.i3s.app.rdfminer.evolutionary.fitness.Fitness;
import com.i3s.app.rdfminer.evolutionary.fitness.novelty.NoveltySearch;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.GEIndividual;
import com.i3s.app.rdfminer.generator.Generator;
import com.i3s.app.rdfminer.launcher.GrammaticalEvolution;
import com.i3s.app.rdfminer.sparql.corese.CoreseEndpoint;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Objects;

public class Offspring {

    private static final Logger logger = Logger.getLogger(Offspring.class.getName());

    protected ArrayList<Entity> population;
    protected ArrayList<Entity> survivors;
    protected Generator generator;

    public Offspring(Entity parent1, Entity parent2, GEIndividual child1, GEIndividual child2, ArrayList<Entity> population, Generator generator)
            throws URISyntaxException, IOException {
        this.generator = generator;
        this.population = population;
        this.survivors = new ArrayList<>();
        // compare child and parent n°1
        compare(parent1, child1);
        // compare child and parent n°2
        compare(parent2, child2);
    }

    private void compare(Entity parent, GEIndividual offspring) throws URISyntaxException, IOException {
//        logger.debug("\nParent: " + parent.individual.getPhenotype().getStringNoSpace() + "\nChild: " + offspring.getPhenotype().getStringNoSpace());
        // equivalence testing
        if(Objects.equals(offspring.getGenotype().toString(), parent.individual.getGenotype().toString())) {
            this.survivors.add(parent);
        } else {
            Entity child = Fitness.computeEntity(offspring, this.generator);
            // if the novelty search is enabled, we would like to know if the tested offspring is far (or not)
            // from the current population, in order to reward 'very novel' assumption
            if (RDFMiner.parameters.useNoveltySearch) {
                CoreseEndpoint endpoint = new CoreseEndpoint(Global.CORESE_IP, Global.TRAINING_SPARQL_ENDPOINT, Global.PREFIXES);
                NoveltySearch ns = new NoveltySearch(endpoint);
                double scoreChild = ns.getScore(child, ns.getDistanceOfEntityFromPopulation(child, this.population));
                double scoreParent = ns.getScore(parent, ns.getDistanceOfEntityFromPopulation(parent, this.population));
                logger.debug("child.score (" + scoreChild + ") vs parent.score (" + scoreParent + ")");
                // we keep the better individual alive
                if (scoreChild > scoreParent) {
                    // report if a better individual is found
                    GrammaticalEvolution.nBetterIndividual++;
                }
                // In a case that child1 is better or as good as the parent, we'll keep the child in order to ensure the
                // diversity of the future population
                if(scoreChild >= scoreParent) {
                    this.survivors.add(child);
                } else {
                    // the child is not better ! we'll keep the parent alive
                    this.survivors.add(parent);
                }
            } else {
                // report if a better individual is found
                if(child.individual.getFitness().getDouble() > parent.individual.getFitness().getDouble()) {
                    GrammaticalEvolution.nBetterIndividual++;
                }
                // In a case that child1 is better or as good as the parent, we'll keep the child in order to ensure the
                // diversity of the future population
                if(child.individual.getFitness().getDouble() >= parent.individual.getFitness().getDouble()) {
                    this.survivors.add(child);
                } else {
                    // the child is not better ! we'll keep the parent alive
                    this.survivors.add(parent);
                }
            }
        }
    }

    public ArrayList<Entity> get() {
        // return survivors
        return this.survivors;
    }

}
