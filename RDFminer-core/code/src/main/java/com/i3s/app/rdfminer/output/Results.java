package com.i3s.app.rdfminer.output;

import com.i3s.app.rdfminer.entity.Entity;
import org.json.JSONObject;

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

    private ArrayList<JSONObject> entities = new ArrayList<>();

    private ArrayList<Generation> generations = new ArrayList<>();

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public void addEntity(JSONObject entity) {
        this.entities.add(entity);
    }

    public void setEntities(ArrayList<Entity> entities) {
        for(Entity entity : entities) {
            this.addEntity(entity.toJSON());
        }
    }
    
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

    public static Results getInstance() {
        if(Results.instance == null) {
            Results.instance = new Results();
        }
        return Results.getInstance();
    }

}
