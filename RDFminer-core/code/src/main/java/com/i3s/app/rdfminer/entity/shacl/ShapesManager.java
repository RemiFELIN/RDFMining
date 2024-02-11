package com.i3s.app.rdfminer.entity.shacl;

import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.entity.Entity;
import com.i3s.app.rdfminer.entity.shacl.vocabulary.Shacl;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.GEIndividual;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.Genotype;
import com.i3s.app.rdfminer.sparql.RequestBuilder;
import com.i3s.app.rdfminer.sparql.corese.CoreseEndpoint;
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

import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
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

    public ShapesManager(String content, CoreseEndpoint endpoint) {
        // set content to submit in http request
        this.content = endpoint.getPrefixes() + "\n\n" + content;
        // init model
        try {
            this.model = Rio.parse(new StringReader(this.content), "", RDFFormat.TURTLE);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        // fill population
        fillPopulation(this.model, endpoint);
//        logger.info(population.size() + " SHACL Shape(s) ready to be evaluated !");
    }

    /**
     * Take a list of GEIndividuals and build a list of well-formed SHACL Shapes
     *
     * @param individuals individuals generated
     */
    public ShapesManager(ArrayList<GEIndividual> individuals, CoreseEndpoint endpoint) throws IOException, URISyntaxException {
        this.content += Global.PREFIXES;
        for (GEIndividual individual : individuals) {
            Shape s = new Shape(individual, endpoint);
            population.add(s);
            this.content += s + "\n";
        }
        // set the file content to evaluate this SHACL Shapes on server
//        this.path = editShapesTmpFile(this.population);
//        logger.info(population.size() + " SHACL Shapes ready to be evaluated !");
    }

    public ShapesManager(GEIndividual individual, CoreseEndpoint endpoint) throws URISyntaxException, IOException {
        this.content += Global.PREFIXES;
        Shape s = new Shape(individual, endpoint);
        population.add(s);
        this.content += s + "\n";
    }

    public void fillPopulation(Model model, CoreseEndpoint endpoint) {
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
                logger.info(shapes.size() + " shape(s) are ready to be evaluated !");
//                RDFminer.results.setNumberEntities(shapes.size());
//                RDFminer.results.saveResult();
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
                    this.population.add(new Shape(sb.toString(), endpoint));
                }
            } catch (URISyntaxException | IOException e) {
                e.printStackTrace();
            }
        } finally {
            // shutdown the DB and frees up memory space
            db.shutDown();
        }
    }

    public List<Shape> getPopulation() {
        return population;
    }

    public void setDistinctPopulationFromEntities(ArrayList<Entity> entities, CoreseEndpoint endpoint) throws URISyntaxException, IOException {
        ArrayList<Genotype> distinctGenotypes = new ArrayList<>();
        for(Entity entity : entities) {
            if(!distinctGenotypes.contains(entity.individual.getGenotype())) {
                distinctGenotypes.add(entity.individual.getGenotype());
                this.population.add(new Shape(entity.individual, endpoint));
            }
        }
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

}
