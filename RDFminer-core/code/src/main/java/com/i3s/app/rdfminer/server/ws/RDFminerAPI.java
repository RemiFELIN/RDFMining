package com.i3s.app.rdfminer.server.ws;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.i3s.app.rdfminer.Parameters;
import com.i3s.app.rdfminer.RDFminer;
import com.i3s.app.rdfminer.output.Results;
import com.i3s.app.rdfminer.server.MyLogger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

/**
 * RDFminer Web services
 * endpoints to exploit RDFminer tools (SHACL or OWL axioms mining; SHACL Validation; ...)
 */
@Path("rdfminer")
public class RDFminerAPI {

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response exec(@FormParam("params") String params) throws JsonProcessingException {
        MyLogger.info("POST", "exec RDFminer ...");
        // map parameters provided by user
        Parameters parameters = new ObjectMapper().readValue(params, Parameters.class);
        MyLogger.info("POST", "project: " + parameters.getProjectName() + " (" + parameters.getUserID() + ")");
        // get instance of results
        Results results = Results.getInstance();
        results.setUserID(parameters.getUserID());
        results.setProjectName(parameters.getProjectName());
        try {
            RDFminer.exec(parameters);
        } catch (InterruptedException | ExecutionException | URISyntaxException | IOException e) {
            MyLogger.info("ERROR", "error during the RDFminer execution ...");
            MyLogger.info("ERROR", e.getMessage());
            MyLogger.info("ERROR", "project " + parameters.getProjectName() + " (" + parameters.getUserID() + ") failed !");
//            e.printStackTrace();
        }
        return Response.ok(new ObjectMapper().writeValueAsString(results)).build();
    }

}
