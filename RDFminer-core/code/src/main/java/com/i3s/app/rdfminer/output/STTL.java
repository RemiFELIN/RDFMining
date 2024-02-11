//package com.i3s.app.rdfminer.output;
//
//import com.i3s.app.rdfminer.sparql.corese.CoreseEndpoint;
//import org.apache.log4j.Logger;
//
//import java.io.IOException;
//import java.net.URISyntaxException;
//import java.nio.charset.StandardCharsets;
//import java.nio.file.Files;
//import java.nio.file.Path;
//
//public class STTL {
//
//    private static final Logger logger = Logger.getLogger(STTL.class.getName());
//
//    public static void perform(CoreseEndpoint endpoint, String template, String outputFilename) throws IOException, URISyntaxException {
//        // STTL Transformation
//        // load template
//        logger.info("Perform STTL Transformation ...");
//        String sttl = Files.readString(Path.of(template), StandardCharsets.UTF_8);
//        // perform sttl query
//        String sttl_result = endpoint.getHTMLResultFromSTTLTransformation(sttl);
//        // write results in output file
//        logger.info("Writting results in " + outputFilename);
////        FileWriter fw = new FileWriter(RDFminer.outputFolder + outputFilename);
////        fw.write(sttl_result);
////        fw.close();
//    }
//
//}
