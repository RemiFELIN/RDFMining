package com.i3s.app.rdfminer.entity.shacl;

import Individuals.Genotype;
import Individuals.Phenotype;
import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.entity.Entity;
import com.i3s.app.rdfminer.entity.shacl.vocabulary.Shacl;
import com.i3s.app.rdfminer.evolutionary.individual.GEIndividual;
import com.i3s.app.rdfminer.sparql.RequestBuilder;
import org.apache.jena.riot.other.G;
import org.apache.log4j.Logger;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * SHACL Shapes Manager : provides tools to manage shapes (parser ; editor ; ...)
 *
 * @author Rémi FELIN
 */
public class ShapesManager {

    private static final Logger logger = Logger.getLogger(ShapesManager.class);

    public Model model;

    public Repository db;

    public ArrayList<Shape> population = new ArrayList<>();

    public String content = "";

    public ShapesManager() {}

    public ShapesManager(String content, boolean prefix) {
        // set content to submit in http request
        if(prefix)
            this.content += Global.PREFIXES;
        this.content += content;
        // init model
        try {
            this.model = Rio.parse(new StringReader(this.content), "", RDFFormat.TURTLE);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        // fill population
        fillPopulation(this.model);
//        logger.info(population.size() + " SHACL Shape(s) ready to be evaluated !");
    }

    /**
     * Take a list of GEIndividuals and build a list of well-formed SHACL Shapes
     *
     * @param individuals individuals generated
     */
    public ShapesManager(ArrayList<GEIndividual> individuals) throws IOException {
        this.content += Global.PREFIXES;
        for (GEIndividual individual : individuals) {
            Shape s = new Shape(individual);
            population.add(s);
            this.content += s + "\n";
        }
        // set the file content to evaluate this SHACL Shapes on server
//        this.path = editShapesTmpFile(this.population);
//        logger.info(population.size() + " SHACL Shapes ready to be evaluated !");
    }

    public void fillPopulation(Model model) {
        // Create a new Repository. Here, we choose a database implementation
        // that simply stores everything in main memory.
        this.db = new SailRepository(new MemoryStore());
        // We will save all SHACL Shapes in population as string
        // connect DB
        try(RepositoryConnection con = db.getConnection()) {
            // add the model
            con.add(model);
            // init query
            String request = RequestBuilder.select("?shapes", "?shapes a " + Shacl.NODESHAPE + " .", true);
            TupleQuery query = con.prepareTupleQuery(request);
            // launch and get result
            try (TupleQueryResult result = query.evaluate()) {
                // we just iterate over all solutions in the result...
                List<String> shapes = new ArrayList<>();
                for (BindingSet solution : result) {
                    // add each result on the final list
                    shapes.add("<" + solution.getValue("shapes") + ">");
                }
                for(String shapeSubject : shapes) {
                    // feel shape content
                    StringBuilder sb = new StringBuilder();
                    // we will write the content of each sh:property (if it provided by the current shape)
                    String getTriples = RequestBuilder.select("*",
                            shapeSubject + " ?p ?o . OPTIONAL { ?o ?x ?y }",
                            true);
                    try (TupleQueryResult values = con.prepareTupleQuery(getTriples).evaluate()) {
                        boolean isBN = false;
                        for (BindingSet res : values) {
                            if(res.getValue("o").isBNode()) {
                                if(!isBN) {
                                    sb.append(shapeSubject).append(" <").append(res.getValue("p")).append("> ").
                                            append(res.getValue("o")).append(" .\n");
                                    isBN = true;
                                }
                                sb.append(res.getValue("o")).append(" <").append(res.getValue("x")).
                                        append("> <").append(res.getValue("y")).append("> .\n");
                            } else {
                                sb.append(shapeSubject).append(" <").append(res.getValue("p")).append("> <").
                                        append(res.getValue("o")).append("> .\n");
                            }
                        }
                    }
                    this.population.add(new Shape(sb.toString()));
                }
            }
        } finally {
            // shutdown the DB and frees up memory space
            db.shutDown();
        }
    }

    public List<Shape> getPopulation() {
        return population;
    }

    public void setDistinctPopulationFromEntities(ArrayList<Entity> entities) {
        ArrayList<Phenotype> distinctPhenotypes = new ArrayList<>();
        for(Entity entity : entities) {
            if(!distinctPhenotypes.contains(entity.individual.getPhenotype())) {
                distinctPhenotypes.add(entity.individual.getPhenotype());
                this.population.add(new Shape(entity.individual));
            }
        }
//        logger.info(population.size() + " distinct SHACL shapes ready to be evaluated !");
        // set whole content
        setContent();
    }

    public void setContent() {
        if(this.population.size() != 0) {
            this.content += Global.PREFIXES;
            for(Shape shape : this.population) {
                this.content += shape + "\n";
            }
        } else {
            logger.warn("Population is empty !");
        }
    }

//    public File getFile() {
//        logger.info("Path: " + this.path);
//        return this.path.toFile();
//    }

//    public Path editShapesTmpFile(ArrayList<Shape> population) throws IOException {
//        Path tmpPath = Files.createTempFile(Paths.get(Global.HOME), "shapes", ".ttl");
//        // edit this file
//        FileWriter fw = new FileWriter(tmpPath.toFile());
//        // edit turtle file which will contains shapes
//        // set prefixes
//        fw.write(Global.PREFIXES);
//        for(Shape shape : population) {
//            logger.info(shape.content);
//            // write phenotype individuals
//            fw.write(shape.content);
//        }
//        fw.close();
//        return tmpPath;
//    }

//    public static void main(String[] args) {
//        ShapesManager man = new ShapesManager(Path.of("/user/rfelin/home/projects/RDFMining/IO/shapes_to_evaluate.txt"));
//    }

}
