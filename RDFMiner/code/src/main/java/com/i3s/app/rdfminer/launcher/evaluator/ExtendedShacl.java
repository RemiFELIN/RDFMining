package com.i3s.app.rdfminer.launcher.evaluator;

import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.RDFMiner;
import com.i3s.app.rdfminer.entity.shacl.Shape;
import com.i3s.app.rdfminer.entity.shacl.ShapesManager;
import com.i3s.app.rdfminer.entity.shacl.ValidationReport;
import com.i3s.app.rdfminer.ht.HypothesisTesting;
import com.i3s.app.rdfminer.output.STTL;
import com.i3s.app.rdfminer.sparql.corese.CoreseEndpoint;
import com.i3s.app.rdfminer.sparql.corese.Format;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;

public class ExtendedShacl {

    private static final Logger logger = Logger.getLogger(ExtendedShacl.class.getName());

    public static void runWithEval(ShapesManager shapesManager) throws URISyntaxException, IOException {
        // launch evaluation
        CoreseEndpoint endpoint = new CoreseEndpoint(Global.CORESE_IP, Global.PREFIXES);
        // Launch SHACL evaluation from the Corese server and get the result in turtle
        String report = endpoint.getValidationReportFromServer(shapesManager.content);
        endpoint.sendFileToServer(new File(RDFMiner.parameters.shapeFile), Global.SHACL_SHAPES_FILENAME);
        // write SHACL report in output file
        ValidationReport validationReport = new ValidationReport(report);
        logger.info("Writting validation report in " + RDFMiner.outputFolder + Global.SHACL_VALIDATION_REPORT_FILENAME + " ...");
        RDFMiner.output.write(validationReport.prettifyPrint());
        RDFMiner.output.close();
        // perform hypothesis testing
        performHypothesisTesting(validationReport, shapesManager);
        // send hypothesis result to Corese graph
        endpoint.sendFileToServer(new File(RDFMiner.outputFolder + Global.SHACL_HYPOTHESIS_TEST_FILENAME), Global.SHACL_HYPOTHESIS_TEST_FILENAME);
        endpoint.sendRDFDataToDB(endpoint.getFilePathFromServer(Global.SHACL_HYPOTHESIS_TEST_FILENAME));
        // Send the SHACL Validation Report and shapes graph into Corese graph in order to
        // perform a HTML report with STTL transformation
        endpoint.sendFileToServer(new File(RDFMiner.outputFolder + Global.SHACL_VALIDATION_REPORT_FILENAME), Global.SHACL_VALIDATION_REPORT_FILENAME);
        endpoint.sendRDFDataToDB(endpoint.getFilePathFromServer(Global.SHACL_VALIDATION_REPORT_FILENAME));
        // load shapes graph in corese DB
        endpoint.sendRDFDataToDB(endpoint.getFilePathFromServer(Global.SHACL_SHAPES_FILENAME));
        // perform STTL transformation
        STTL.perform(endpoint, Global.PROBABILISTIC_STTL_TEMPLATE, Global.PROBABILISTIC_STTL_RESULT_AS_HTML);
        // remove imported data
        removeImportedData(endpoint);
    }

    public static void runWithoutEval(String report, ShapesManager shapesManager) throws URISyntaxException, IOException {
        CoreseEndpoint endpoint = new CoreseEndpoint(Global.CORESE_IP, Global.PREFIXES);
        // write shapes from population
        createShapesFile(shapesManager.content);
        endpoint.sendFileToServer(new File(Global.SHACL_SHAPES_FILENAME), Global.SHACL_SHAPES_FILENAME);
        logger.info("Writting validation report in " + RDFMiner.outputFolder + Global.SHACL_VALIDATION_REPORT_FILENAME + " ...");
        ValidationReport validationReport = new ValidationReport(report);
        // write SHACL report in output file
        FileWriter validationReportFw = new FileWriter(Global.SHACL_VALIDATION_REPORT_FILENAME);
        validationReportFw.write(validationReport.prettifyPrint());
        validationReportFw.close();
        // perform hypothesis testing
        performHypothesisTesting(new ValidationReport(report), shapesManager);
        // send hypothesis result to Corese graph
        endpoint.sendFileToServer(new File(RDFMiner.outputFolder + Global.SHACL_HYPOTHESIS_TEST_FILENAME), Global.SHACL_HYPOTHESIS_TEST_FILENAME);
        endpoint.sendRDFDataToDB(endpoint.getFilePathFromServer(Global.SHACL_HYPOTHESIS_TEST_FILENAME));
        // Send the SHACL Validation Report and shapes graph into Corese graph in order to
        // perform a HTML report with STTL transformation
        endpoint.sendFileToServer(new File(RDFMiner.outputFolder + Global.SHACL_VALIDATION_REPORT_FILENAME), Global.SHACL_VALIDATION_REPORT_FILENAME);
        endpoint.sendRDFDataToDB(endpoint.getFilePathFromServer(Global.SHACL_VALIDATION_REPORT_FILENAME));
        // load shapes graph in corese DB
        endpoint.sendRDFDataToDB(endpoint.getFilePathFromServer(Global.SHACL_SHAPES_FILENAME));
        // perform STTL transformation
        STTL.perform(endpoint, Global.PROBABILISTIC_STTL_TEMPLATE, Global.PROBABILISTIC_STTL_RESULT_AS_HTML);
        // remove imported data
        removeImportedData(endpoint);
    }

    public static void performHypothesisTesting(ValidationReport validationReport, ShapesManager shapesManager) throws IOException {
        // save the result of statistic test into RDF triples
        // in order to put it in Corese graph and get its value into STTL Transformation and HTML result
        // In the same way, we note the acceptance (or not) of a given shape using proportion or hypothesis testing
        logger.info("Writting hypothesis test results in " + Global.SHACL_HYPOTHESIS_TEST_FILENAME);
        FileWriter hypothesisTestFw = new FileWriter(RDFMiner.outputFolder + Global.SHACL_HYPOTHESIS_TEST_FILENAME);
        hypothesisTestFw.write(Global.PREFIXES + "\n");
        logger.info(validationReport.reportedShapes.size() + " shapes has been evaluated !");
        for(Shape shape : shapesManager.getPopulation()) {
            if(validationReport.reportedShapes.contains(shape.fullUri.replace("<", "").replace(">", ""))) {
                // get shapes with metrics
                shape.fillParamFromReport(validationReport);
                // instanciate hypothesis testing with current shape
                HypothesisTesting ht = new HypothesisTesting(shape);
                hypothesisTestFw.write(ht.getAcceptanceTriple());
                if(ht.getX2() != null)
                    hypothesisTestFw.write(ht.getX2ValueTriple());
            }
        }
        hypothesisTestFw.close();
    }

    public static void removeImportedData(CoreseEndpoint endpoint) throws URISyntaxException, IOException {
        logger.info("remove imported data ...");
        // reset Corese endpoint: remove shapes; SHACL val. report and hypothesis testing results
        String constraints = "?x ?p ?o . FILTER( contains(str(?p), str(sh:)) || " +
                "contains(str(?p), str(psh:)) || contains(str(?o), str(sh:)) || contains(str(?o), str(psh:)) || " +
                "contains(str(?p), str(ex:)) ) .";
        String query = "DELETE { ?x ?p ?o . } WHERE { " + constraints + "}";
        // remove data in GET service
        endpoint.query(Format.JSON, query);
        logger.info("Done !");
    }

    public static void createShapesFile(String shapes) throws IOException {
        FileWriter fw = new FileWriter(Global.SHACL_SHAPES_FILENAME);
        fw.write(shapes);
        fw.close();
    }

}
