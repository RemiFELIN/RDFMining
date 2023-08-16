package com.i3s.app.rdfminer.output;

import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.RDFMiner;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to map all results on an object and generate a {@link org.json.JSONObject} of it.
 * @author Rémi FELIN
 */
public class Results {

    private static final Logger logger = Logger.getLogger(Results.class.getName());

    public List<JSONObject> content;
    public JSONObject stats;

    public Results() {
        // set the content part of the results
        // i.e. a set of assessed OWL Axioms or SHACL Shapes
        RDFMiner.content = new ArrayList<>();
        // set statistics
        // i.e. parameters used; statistics over generations; ...
        RDFMiner.stats = new Stat();
        // send it to the server
        saveResult();
    }

    /**
     * Convert a results to an instance of {@link JSONObject}
     * @return a JSONObject
     */
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("userId", RDFMiner.parameters.username);
        json.put("projectName", RDFMiner.parameters.directory);
        json.put("statistics", stats);
        json.put("entities", new JSONArray(content));
        return json;
    }

    private void saveResult() {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(Global.RDFMINER_SERVER_IP + "api/result");
            // specify the POST body to send to the server as part of the request
            post.setEntity(new StringEntity(this.toJSON().toString(), ContentType.APPLICATION_JSON));
            logger.info("POST request: sending results ...");
            HttpResponse response = httpClient.execute(post);
            logger.info("Status code: " + response.getStatusLine().getStatusCode());
            logger.info(new BasicResponseHandler().handleResponse(response));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
