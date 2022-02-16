package fr.inria.corese.server.webservice;

import fr.inria.corese.core.print.ResultFormat;
import fr.inria.corese.sparql.api.ResultFormatDef;
import fr.inria.corese.sparql.triple.parser.URLParam;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

/**
 * Web service to manage the files transfer between Corese server and RDFMiner project
 * @author Rémi FELIN
 */
@Path("rdfminer")
public class SHACLShapesRestAPI implements ResultFormatDef, URLParam {

    private static final Logger logger = LogManager.getLogger(SHACLShapesRestAPI.class);

    /**
     * header accept
     */
    private static final String headerAccept = "Access-Control-Allow-Origin";

    /**
     * Path of the file used to store SHACL Shapes on each generations
     */
    public final String RDFMINER_SHAPES_FILEPATH = "rdfminer/shacl-shapes/shapes.ttl";

    /**
     * UTF-8 encoding
     */
    public final String UTF8 = "UTF-8";

    /**
     * Post the content on /rdfminer/shacl-shape and create a file in this path
     * Use case : http://172.19.0.4:9100/rdfminer/send/shapes?fileContent=[CONTENT]
     * @param content Body of the shapes.ttl file
    */
    @POST
    @Path("/send/shapes")
    // @Produces({ResultFormat.TEXT})
    public Response postSHACLShapesFile(@javax.ws.rs.core.Context HttpServletRequest request,
            @QueryParam("content") String content) {
        
        if(content == null) {
            String msg = "No content specified !";
            logger.error(msg);
            return Response.status(Response.Status.NOT_FOUND).header(headerAccept, "*").entity(msg).build();
        }

        logger.info("send shapes started ...");
        logger.info("request: " + request);
        Writer writer = null;
        File file = null;

        try {
            if(!new File(RDFMINER_SHAPES_FILEPATH).exists()) {
                file = new File(RDFMINER_SHAPES_FILEPATH);
                // Will create parent directories if not exists
                file.getParentFile().mkdirs();
                file.createNewFile();
            } else {
                file = new File(RDFMINER_SHAPES_FILEPATH);
            }
            logger.info(file.getAbsolutePath() + " is correctly created !");
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false), StandardCharsets.UTF_8));
            writer.write(content);
        } catch (IOException ex) {
            String msg = "Error during file edition: " + ex.getMessage();
            logger.error(msg);
            return Response.status(Response.Status.NOT_FOUND).header(headerAccept, "*").entity(msg).build();
        } finally {
            try {
                assert writer != null;
                writer.close();
            } catch (Exception ex) {
                String msg = "Error during file close: " + ex.getMessage();
                logger.error(msg);
                return Response.status(Response.Status.NOT_FOUND).header(headerAccept, "*").entity(msg).build();
            }
        }

        return Response.status(Response.Status.OK).
                        header(headerAccept, "*").
                        entity(file.getAbsolutePath() + " is correctly edited !").
                        build();

    }

    @GET
    @Path("/shacl/shapes")
    @Produces({ResultFormat.TURTLE})
    public Response getSHACLShapesFile(@javax.ws.rs.core.Context HttpServletRequest request) {

        if(!new File(RDFMINER_SHAPES_FILEPATH).exists()) {
            String msg = "The file does not exist !";
            logger.error(msg);
            return Response.status(Response.Status.NOT_FOUND).header(headerAccept, "*").entity(msg).build();
        }

        String content = null;
        try {
            File file = new File(RDFMINER_SHAPES_FILEPATH);
            content = Files.readString(file.toPath());
        } catch (IOException e) {
            logger.error("Error during file extraction: " + e.getMessage());
        }
        assert content != null;
        return Response.status(Response.Status.OK).
                        header(headerAccept, "*").
                        entity(content).
                        build();
    }

    
    
}
