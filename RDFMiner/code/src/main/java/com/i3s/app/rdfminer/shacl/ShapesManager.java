package com.i3s.app.rdfminer.shacl;

import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.grammar.evolutionary.individual.GEIndividual;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * SHACL Shapes Manager : provides tools to manage shapes (parser ; editor ; ...)
 * @author Rémi FELIN
 */
public class ShapesManager {

    private static final Logger logger = Logger.getLogger(ShapesManager.class);

    /**
     * population from list of GEIndividuals with ID
     */
    protected List<String> population;

    /**
     * Take a list of GEIndividuals and build a list of well-formed SHACL Shapes
     * @param individuals individuals generated
     */
    public ShapesManager(ArrayList<GEIndividual> individuals) {
        population = new ArrayList<>();
        for(GEIndividual individual : individuals) {
            population.add(generateIDFromIndividual(individual) + individual.getPhenotype().toString());
        }
    }

    /**
     * Generate randomly an unique ID for a given individual
     * @param individual a GEIndividual from population
     * @return
     */
    private String generateIDFromIndividual(GEIndividual individual) {
        // the length of the substring depends of the SHACL Shapes ID size such as :
        return "<shape#" + String.format("%."+ Global.SIZE_ID_SHACL_SHAPES + "s", Math.abs(individual.getPhenotype().toString().hashCode())) + "> ";
    }

    public void printPopulation() {
        for(String population : population)
            logger.info(population);
    }

}
