package com.i3s.app.rdfminer.shacl;

import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.grammar.evolutionary.individual.GEIndividual;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * SHACL Shapes Manager : provides tools to manage shapes (parser ; editor ; ...)
 *
 * @author Rémi FELIN
 */
public class ShapesManager {

    private static final Logger logger = Logger.getLogger(ShapesManager.class);

    /**
     * population of {@link Shape} from list of GEIndividuals (Genotype) with ID
     */
    public List<Shape> population = new ArrayList<>();

    public Shape shape;

    public ArrayList<GEIndividual> individuals;

    public String fileContent;

    /**
     * Take a list of GEIndividuals and build a list of well-formed SHACL Shapes
     *
     * @param individuals individuals generated
     */
    public ShapesManager(ArrayList<GEIndividual> individuals) throws IOException {
        this.individuals = individuals;
        for (GEIndividual individual : individuals) {
            population.add(new Shape(individual));
        }
        // set the file content to evaluate this SHACL Shapes on server
        this.fileContent = getFileContent();
    }

    public ShapesManager(Shape shape) {
        this.shape = shape;
        // set the file content to evaluate this SHACL Shapes on server
        this.fileContent = getFileContent();
    }

    public void updateIndividualList(ArrayList<GEIndividual> updatedIndividuals) {
        this.individuals = new ArrayList<>(updatedIndividuals);
    }

    private String getFileContent() {
        StringBuilder content = new StringBuilder(Global.CORESE_PREFIXES);
        if(!population.isEmpty()) {
            for(Shape shape : population) {
                content.append(shape).append("\n");
            }
        } else if(this.shape != null){
            // set only one shape in fileContent
            content.append(this.shape).append("\n");
        }
        return content.toString();
    }

    public List<Shape> getPopulation() {
        return population;
    }

    public Shape getShape() {
        return shape;
    }

    public void printPopulation() {
        for (Shape ind : population)
            logger.info(ind.id);
    }

}
