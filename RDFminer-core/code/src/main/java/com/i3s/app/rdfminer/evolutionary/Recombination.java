package com.i3s.app.rdfminer.evolutionary;

import com.i3s.app.rdfminer.Parameters;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.GEIndividual;
import com.i3s.app.rdfminer.evolutionary.tools.EAOperators;
import com.i3s.app.rdfminer.evolutionary.tools.EATools;
import com.i3s.app.rdfminer.generator.Generator;
import com.i3s.app.rdfminer.launcher.GrammaticalEvolution;
import org.apache.log4j.Logger;

import java.util.ArrayList;

public class Recombination {

    private static final Logger logger = Logger.getLogger(Recombination.class.getName());

    /**
     * To compute all tasks about crossover, mutation and evaluation phasis of
     * evolutionary algorithm
     * @return a new population
     */
    public ArrayList<GEIndividual> perform(Generator generator, ArrayList<GEIndividual> elites,
                                                  ArrayList<GEIndividual> selectedIndividuals) {
        Parameters parameters = Parameters.getInstance();
        ArrayList<GEIndividual> replacement = new ArrayList<>();
//        System.out.println(newPopulation.size());
//        int phasis = 0;
        // reset nPhasis before starting a new one !
        GrammaticalEvolution.nRecombinaison = 0;
        // while the new population size is not equals to the target
        int target = parameters.getPopulationSize() - elites.size();
        EAOperators operators = new EAOperators();
        while (replacement.size() != target)  {
            // operate crossover and mutation: 2 by 2
            ArrayList<GEIndividual> couple = EATools.getCoupleInPopulation(selectedIndividuals, generator);
            // crossover
            operators.crossover(couple);
            // mutation
            operators.mutation(couple);
            // adding the new individuals
            for(GEIndividual individual : couple) {
                // update phenotype
                GEIndividual offspring = generator.getIndividualFromChromosome(individual.getChromosomes());
                // if this offspring is very new (i.e. not observed into elites and current remplacement population)
                // add it into replacement population
                if (!offspring.isInPopulation(elites) &&
                        !offspring.isInPopulation(replacement) &&
                        replacement.size() != target && !offspring.isTrivial()) {
                    replacement.add(offspring);
                }
            }
            GrammaticalEvolution.nRecombinaison++;
        }
        // return new population
        logger.info(GrammaticalEvolution.nRecombinaison + " phasis has been required to perform replacement !");
        logger.info(GrammaticalEvolution.nCrossover + " crossover(s) and " + GrammaticalEvolution.nMutation + " mutation(s) has been perform !");
        return replacement;
    }

}
