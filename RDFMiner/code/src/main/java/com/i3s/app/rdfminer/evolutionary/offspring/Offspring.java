package com.i3s.app.rdfminer.evolutionary.offspring;

import com.i3s.app.rdfminer.entity.Entity;
import com.i3s.app.rdfminer.evolutionary.fitness.Fitness;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.GEIndividual;
import com.i3s.app.rdfminer.generator.Generator;
import com.i3s.app.rdfminer.launcher.GrammaticalEvolution;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Offspring {

    private static final Logger logger = Logger.getLogger(Offspring.class.getName());

    protected Entity parent1;
    protected Entity parent2;
    protected Entity child1;
    protected Entity child2;
    protected Generator generator;

    public Offspring(Entity parent1, Entity parent2, GEIndividual child1, GEIndividual child2, Generator generator)
            throws URISyntaxException, IOException {
//        logger.debug("-----------");
//        logger.debug("parent1: " + parent1.individual.getGenotype() + "\nis mapped: " + parent1.individual.isMapped());
//        logger.debug("parent2: " + parent2.individual.getGenotype() + "\nis mapped: " + parent2.individual.isMapped());
//        logger.debug("child1: " + child1.getGenotype() + "\nis mapped: " + child1.isMapped());
//        logger.debug("child2: " + child2.getGenotype() + "\nis mapped: " + child2.isMapped());
//        logger.debug("-----------");
        this.generator = generator;
        this.parent1 = parent1;
        this.parent2 = parent2;
        if(Objects.equals(child1.getGenotype().toString(), parent1.individual.getGenotype().toString())) {
            this.child1 = this.parent1;
        } else {
            this.child1 = Fitness.computeEntity(child1, this.generator);
            if(this.child1.individual.getFitness().getDouble() > this.parent1.individual.getFitness().getDouble()) {
                GrammaticalEvolution.nBetterIndividual++;
                logger.debug("new(i): " + this.child1.individual.getGenotype() + " ~ F(i)= " + this.child1.individual.getFitness().getDouble());
            }
            // test if the offspring fitness is not worst than its parent
            if(this.child1.individual.getFitness().getDouble() < this.parent1.individual.getFitness().getDouble()) {
//                logger.debug("keep the parent i: " + this.parent1.individual.getGenotype() + " alive: F(i)= " + this.parent1.individual.getFitness().getDouble());
                this.child1 = this.parent1;
            }
        }
        if(Objects.equals(child2.getGenotype().toString(), parent2.individual.getGenotype().toString())) {
            this.child2 = this.parent2;
        } else {
            this.child2 = Fitness.computeEntity(child2, this.generator);
            if(this.child2.individual.getFitness().getDouble() > this.parent2.individual.getFitness().getDouble()) {
                GrammaticalEvolution.nBetterIndividual++;
                logger.debug("new(i): " + this.child2.individual.getGenotype() + " ~ F(i)= " + this.child2.individual.getFitness().getDouble());
            }
            // test if the offspring fitness is not worst than its parent
            if(this.child2.individual.getFitness().getDouble() < this.parent2.individual.getFitness().getDouble()) {
//                logger.debug("keep the parent i: " + this.parent2.individual.getGenotype() + " alive: F(i)= " + this.parent2.individual.getFitness().getDouble());
                this.child2 = this.parent2;
            }
        }
    }

    public ArrayList<Entity> get() {
        // return offsprings
        return new ArrayList<>(List.of(this.child1, this.child2));
    }

}
