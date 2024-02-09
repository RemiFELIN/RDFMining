package com.i3s.app.rdfminer.server.ws;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.i3s.app.rdfminer.Parameters;
import com.i3s.app.rdfminer.server.MyLogger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
        Parameters parameters = new ObjectMapper().readValue(params, Parameters.class);
        MyLogger.info("POST", "Project: " + parameters.getProjectName() + " (" + parameters.getUserID() + ")");
        // launch RDFminer-core
//        System.out.println(parameters.getUserID());
//        System.out.println(parameters.getPopulationSize());
//        System.out.println(parameters.getCrossoverType());
        return Response.ok().build();
    }

    // /user/rfelin/home/projects/RDFMining/IO/logs/test.log

}
