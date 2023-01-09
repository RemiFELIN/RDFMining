package com.i3s.app.rdfminer.entity.shape;

import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.RDFMiner;
import com.i3s.app.rdfminer.entity.Entity;
import com.i3s.app.rdfminer.grammar.evolutionary.individual.GEIndividual;
import com.i3s.app.rdfminer.entity.shape.vocabulary.Shacl;
import com.i3s.app.rdfminer.sparql.RequestBuilder;
import org.apache.commons.io.FileUtils;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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

    public Shape shape;

//    public ArrayList<GEIndividual> individuals;

    public File file;

    public ShapesManager(Path path) {
        // init model
        try {
            this.model = Rio.parse(new StringReader(Files.readString(path)), "", RDFFormat.TURTLE);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        // fill population
        fillPopulation(this.model);
        // set the file content to evaluate this SHACL Shapes on server
        this.file = getFile();
        logger.info(population.size() + " SHACL Shapes ready to be evaluated !");
    }

    /**
     * Take a list of GEIndividuals and build a list of well-formed SHACL Shapes
     *
     * @param individuals individuals generated
     */
    public ShapesManager(ArrayList<GEIndividual> individuals) throws IOException {
        for (GEIndividual individual : individuals) {
            population.add(new Shape(individual));
        }
        // set the file content to evaluate this SHACL Shapes on server
        this.file = getFile();
        logger.info(population.size() + " SHACL Shapes ready to be evaluated !");
    }

//    public ShapesManager(Shape shape) {
//        this.shape = shape;
//        // set the file content to evaluate this SHACL Shapes on server
//        this.file = getFile();
//    }

    private File getFile() {
        StringBuilder content = new StringBuilder(Global.PREFIXES);
        if(!population.isEmpty()) {
            for(Shape shape : population) {
                content.append(shape).append("\n");
            }
        } else if(this.shape != null){
            // set only one shape in fileContent
            content.append(this.shape).append("\n");
        }
        // Now, we can create (or edit) the file to send
        File file = new File(RDFMiner.outputFolder + "shapes.ttl");
        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // set content of file
        try {
            FileUtils.writeStringToFile(file, content.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    public List<Shape> getPopulation() {
        return population;
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

    public Path editShapesTmpFile(ArrayList<Entity> population) throws IOException {
        Path tmpPath = Files.createTempFile("shapes", ".ttl");
        // edit this file
        FileWriter fw = new FileWriter(tmpPath.toFile());
        // edit turtle file which will contains shapes
        // set prefixes
        fw.write(Global.PREFIXES);
        for(Entity entity : population) {
            // write phenotype individuals
            fw.write(entity.individual.getPhenotype().getString());
        }
        return tmpPath;
    }

    public static void main(String[] args) {
        ShapesManager man = new ShapesManager(Path.of("/user/rfelin/home/projects/RDFMining/IO/shapes_to_evaluate.txt"));
    }

}
