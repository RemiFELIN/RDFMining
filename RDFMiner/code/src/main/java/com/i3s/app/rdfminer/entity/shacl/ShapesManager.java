package com.i3s.app.rdfminer.entity.shacl;

import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.RDFMiner;
import com.i3s.app.rdfminer.grammar.evolutionary.individual.GEIndividual;
import com.i3s.app.rdfminer.entity.shacl.vocabulary.ShaclKW;
import com.i3s.app.rdfminer.sparql.RequestBuilder;
import com.i3s.app.rdfminer.sparql.corese.CoreseEndpoint;
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
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
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

    /**
     * population of {@link Shape} from list of GEIndividuals (Genotype) with ID
     */
    public List<Shape> population = new ArrayList<>();

    public Shape shape;

    public ArrayList<GEIndividual> individuals;

    public File file;

    public ShapesManager(String filePath) throws URISyntaxException, IOException {
        Path path = Path.of(filePath);
        // init model
        try {
            this.model = Rio.parse(new StringReader(Files.readString(path)), "", RDFFormat.TURTLE);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        // Create a new Repository. Here, we choose a database implementation
        // that simply stores everything in main memory.
        this.db = new SailRepository(new MemoryStore());
        // We will save all SHACL Shapes in population as string
        // connect DB
        try(RepositoryConnection con = db.getConnection()) {
            // add the model
            con.add(this.model);
            // init query
            // "SELECT ?shapes WHERE { \n" + "?shapes a " + ShaclKW.NODESHAPE + " . }";
            String request = RequestBuilder.select("?shapes", "?shapes a " + ShaclKW.NODESHAPE + " .", true);
            TupleQuery query = con.prepareTupleQuery(request);
            // launch and get result
            try (TupleQueryResult result = query.evaluate()) {
                // we just iterate over all solutions in the result...
                List<String> shapes = new ArrayList<>();
                for (BindingSet solution : result) {
                    // add each result on the final list
                    shapes.add(String.valueOf(solution.getValue("shapes")));
                }
                for(String shape : shapes) {
                    StringBuilder shapeAsNTriple = new StringBuilder();
                    // we will write the content of each sh:property (if it provided by the current shape)
                    String getTriples = RequestBuilder.select("DISTINCT ?s ?p ?o", "{ <" + shape + "> ?p ?o . " +
                            "BIND(<" + shape + "> AS ?s) } UNION { ?s ?p ?o . <" + shape + "> (!<>)* ?o . FILTER(?o != sh:NodeShape) }", true);
                    try (TupleQueryResult values = con.prepareTupleQuery(getTriples).evaluate()) {
                        for (BindingSet res : values) {
                            String s = String.valueOf(res.getValue("s"));
                            String o = String.valueOf(res.getValue("o"));
                            if(s.contains("_:")) shapeAsNTriple.append(s + " <"); else shapeAsNTriple.append("<" + s + "> <");
                            shapeAsNTriple.append(res.getValue("p")).append("> ");
                            if(o.contains("_:")) shapeAsNTriple.append(o + " .\n"); else shapeAsNTriple.append("<" + o + "> .\n");
                        }
                    }
                    population.add(new Shape(shapeAsNTriple.toString(), "<" + shape + ">"));
                }
            }
        } finally {
            // shutdown the DB and frees up memory space
            db.shutDown();
        }
        // set the file content to evaluate this SHACL Shapes on server
        this.file = getFile();
        logger.info(population.size() + " SHACL Shapes ready to be evaluated !");
    }

    /**
     * Take a list of GEIndividuals and build a list of well-formed SHACL Shapes
     *
     * @param individuals individuals generated
     */
    public ShapesManager(ArrayList<GEIndividual> individuals) throws IOException, URISyntaxException {
        this.individuals = individuals;
        for (GEIndividual individual : individuals) {
            population.add(new Shape(individual));
        }
        // set the file content to evaluate this SHACL Shapes on server
        this.file = getFile();
    }

    public ShapesManager(Shape shape) throws URISyntaxException, IOException {
        this.shape = shape;
        // set the file content to evaluate this SHACL Shapes on server
        this.file = getFile();
    }

    public void updateIndividualList(ArrayList<GEIndividual> updatedIndividuals) {
        this.individuals = new ArrayList<>(updatedIndividuals);
    }

    private File getFile() throws URISyntaxException, IOException {
        StringBuilder content = new StringBuilder(Global.PREFIXES);
        CoreseEndpoint endpoint = new CoreseEndpoint(Global.CORESE_SPARQL_ENDPOINT, Global.SPARQL_ENDPOINT, Global.PREFIXES);
        content.append(endpoint.getFileFromServer());
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

}
