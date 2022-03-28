package fr.inria.corese.server.webservice;

import fr.inria.corese.core.print.ResultFormat;
import fr.inria.corese.sparql.api.ResultFormatDef;
import fr.inria.corese.sparql.triple.parser.URLParam;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import jakarta.ws.rs.core.MediaType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

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
    public final String RDFMINER_SHAPES_FILEPATH = "rdfminer/shacl/shapes.ttl";

    /**
     * UTF-8 encoding
     */
    public final String UTF8 = "UTF-8";

    /**
     * Post the file in the body of the request and create a file rdfminer/shacl/shapes.ttl
    */
    @POST
    @Path("/upload")
    public Response postSHACLShapesFile(@jakarta.ws.rs.core.Context HttpServletRequest request,
                                        @FormDataParam("file") InputStream uploadedInputStream,
                                        @FormDataParam("file") FormDataContentDisposition fileDetail) {
        File file;
        try {
            if(!new File(RDFMINER_SHAPES_FILEPATH).exists()) {
                file = new File(RDFMINER_SHAPES_FILEPATH);
                // Will create parent directories if not exists
                file.getParentFile().mkdirs();
                file.createNewFile();
            } else {
                file = new File(RDFMINER_SHAPES_FILEPATH);
            }
            FileOutputStream out = new FileOutputStream(file);
            int read;
            byte[] bytes = new byte[1024];
            while ((read = uploadedInputStream.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
            return Response.status(Response.Status.NOT_FOUND).
                    header(headerAccept, "*").
                    entity("Error during uploading: " + e.getMessage()).
                    build();
        }
        return Response.status(Response.Status.OK).
                        header(headerAccept, "*").
                        entity(RDFMINER_SHAPES_FILEPATH + " is correctly uploaded !").
                        build();
    }

    @GET
    @Path("/shacl/shapes")
    public Response getSHACLShapesFile(@jakarta.ws.rs.core.Context HttpServletRequest request,
                                       @jakarta.ws.rs.core.Context HttpServletResponse response) {

        // response.setContentType("text/plain");
        response.setHeader("Content-disposition", "attachment; filename=shapes.ttl");
        response.setHeader("Content-Type", "text/turtle");
        if(!new File(RDFMINER_SHAPES_FILEPATH).exists()) {
            String msg = "The file does not exist !";
            logger.error(msg);
            return Response.status(Response.Status.NOT_FOUND).header(headerAccept, "*").entity(msg).build();
        }

        String content;
        try {
            File file = new File(RDFMINER_SHAPES_FILEPATH);
            OutputStream out = response.getOutputStream();
            out.write(Files.readString(file.toPath()).getBytes(StandardCharsets.UTF_8));
//            content = Files.readString(file.toPath());
        } catch (IOException e) {
            String msg = "Error during file extraction: " + e.getMessage();
            logger.error(msg);
            return Response.status(Response.Status.NOT_FOUND).header(headerAccept, "*").entity(msg).build();
        }
        return Response.status(Response.Status.OK).
                        header(headerAccept, "*").
                        build();
    }

}
