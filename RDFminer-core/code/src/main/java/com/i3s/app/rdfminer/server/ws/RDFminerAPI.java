package com.i3s.app.rdfminer.server.ws;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.i3s.app.rdfminer.Parameters;
import com.i3s.app.rdfminer.RDFminer;
import com.i3s.app.rdfminer.output.Results;
import com.i3s.app.rdfminer.server.MyLogger;
import com.i3s.app.rdfminer.server.RDFminerProcess;

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
@Path("/")
public class RDFminerAPI {

    @POST
    @Path("start")
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
        // init RDFminer processes manager
        RDFminerProcess processes = RDFminerProcess.getInstance();
        Thread task = new Thread(() -> {
            // starting RDFminer
            RDFminer rdfMiner = new RDFminer();
            try {
                rdfMiner.exec();
            } catch (InterruptedException e) {
                // thread interruption due to /stop ws call
                MyLogger.warn("RDFminer task has been interrupted !");
            } catch (ExecutionException | URISyntaxException | IOException e) {
                // others cases
                MyLogger.error("error during the RDFminer execution ...");
                MyLogger.error(e.getMessage());
                MyLogger.error("project " + parameters.getProjectName() + " (user: " + parameters.getUserID() + ") failed !");
            }
        });
        // submit task
        if (processes.setProcess(parameters.getUserID(), task)) {
            // launch
            processes.startThread(parameters.getUserID());
            MyLogger.info("project: " + parameters.getProjectName() + " (user: " + parameters.getUserID() + ") finished !");
            return Response.ok(results.toJSON().toString(2)).build();
        }
        return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("RDFminer-core is unavalaible (already in use, maintenance, ...), try it latter !").build();
    }

    @GET
    @Path("stop")
    public Response stop(@QueryParam("userID") String userID) {
        RDFminerProcess processes = RDFminerProcess.getInstance();
        // return the instance of current results
        Results results = Results.getInstance();
        // kill process
        if (processes.killProcess(userID)) {
            MyLogger.info("user: " + userID + " has stopped its experiment ...");
            return Response.ok(results.toJSON().toString(2)).build();
        }
        return Response.status(Response.Status.NOT_FOUND).entity("No RDFminer execution to stop ...").build();
    }

}
