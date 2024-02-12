package com.i3s.app.rdfminer.output;

import com.i3s.app.rdfminer.entity.Entity;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

/**
 * This class is used to map all results on an object and generate a {@link org.json.JSONObject} of it.
 * This is a singleton
 * @author Rémi FELIN
 */
public class Results {

    private static Results instance = null;

    private String userID;

    private String projectName;

    /**
     * This is a key to retrieve logs file into RDFminer logs folder if needed
     */
    private String logs;

    private ArrayList<JSONObject> entities = new ArrayList<>();

    private ArrayList<Generation> generations = new ArrayList<>();

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
        // define logs filename
        this.setLogs();
    }

    public void addEntity(Entity entity) {
        this.entities.add(entity.toJSON());
    }

    public void setEntities(ArrayList<Entity> entities) {
        // reset list before it
        this.entities = new ArrayList<>();
        for(Entity entity : entities) {
            this.addEntity(entity);
        }
    }

    public void addGeneration(Generation generation) { this.generations.add(generation); }

    public void setGenerations(ArrayList<Generation> generations) {
        this.generations = generations;
    }

    //    public void saveResult() {
//        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
//            HttpPost post = new HttpPost(Endpoint.API_RESULTS);
//            // specify the POST body to send to the server as part of the request
//            post.setEntity(new StringEntity(this.toJSON().toString(), ContentType.APPLICATION_JSON));
//            logger.info("POST request: sending results ...");
//            HttpResponse response = httpClient.execute(post);
//            logger.info("Status code: " + response.getStatusLine().getStatusCode());
//            logger.debug(new BasicResponseHandler().handleResponse(response));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    public void setLogs() {
        this.logs = Instant.now().truncatedTo(ChronoUnit.MILLIS) + "_" + this.projectName.hashCode() + ".log";
    }

    public String getLogs() {
        return this.logs;
    }

    public static Results getInstance() {
        if(instance == null) {
            instance = new Results();
        }
        return instance;
    }

    public void resetLists() {
        this.entities = new ArrayList<>();
        this.generations = new ArrayList<>();
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("userID", this.userID);
        json.put("projectName", this.projectName);
        json.put("logs", this.logs);
        json.put("entities", new JSONArray(entities));
        json.put("generations", new JSONArray(generations));
        return json;
    }

}
