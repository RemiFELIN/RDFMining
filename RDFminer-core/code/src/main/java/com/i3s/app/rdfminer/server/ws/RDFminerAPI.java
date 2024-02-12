package com.i3s.app.rdfminer.server.ws;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.i3s.app.rdfminer.Parameters;
import com.i3s.app.rdfminer.RDFminer;
import com.i3s.app.rdfminer.output.ServerError;
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
        MyLogger.info("exec RDFminer ...");
        // map parameters provided by user
        Parameters parameters = new ObjectMapper().readValue(params, Parameters.class);
//        new ObjectMapper().updateValue(parameters, params);
        MyLogger.info("project: " + parameters.getProjectName() + " is launched by " + parameters.getUserID());
        // instanciate results
        Results results = Results.getInstance();
        results.setUserID(parameters.getUserID());
        results.setProjectName(parameters.getProjectName());
        results.resetLists();
        // starting RDFminer
        RDFminer rdfMiner = new RDFminer();
        try {
            rdfMiner.exec();
        } catch (InterruptedException | ExecutionException | URISyntaxException | IOException e) {
            ServerError error = new ServerError();
            error.setCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            error.setMessage(e.getMessage());
            MyLogger.error("error during the RDFminer execution ...");
            MyLogger.error(e.getMessage());
            MyLogger.error("project " + parameters.getProjectName() + " (user: " + parameters.getUserID() + ") failed !");
//            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ObjectMapper().writeValueAsString(error)).build();
        }
        MyLogger.info("project: " + parameters.getProjectName() + " (user: " + parameters.getUserID() + ") finished !");
        return Response.ok(results.toJSON().toString(2)).build();
    }

}
