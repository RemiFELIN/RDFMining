package com.i3s.app.rdfminer.output;

import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.entity.Entity;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This class contains a map (as JSON) of owl axioms and their similarities
 */
public class SimilarityMap {

    private static final Logger logger = Logger.getLogger(SimilarityMap.class.getName());

    public File file;

    public JSONObject map;

    public SimilarityMap() {
        this.file = new File(Global.SIMILARITIES_FILE);
        // create this file
        try {
            Files.createFile(this.file.toPath());
            // write empty json
            new FileWriter(this.file).write("{}");
        } catch (IOException e) {
            logger.error("Error during the creation of file " + Global.SIMILARITIES_FILE);
            e.printStackTrace();
        }
        if(this.file.exists()) logger.info(this.file.getAbsolutePath() + " has been created !");
        else logger.error("This file " + Global.SIMILARITIES_FILE + " does not exists !");
        this.map = new JSONObject();
    }

    public SimilarityMap(File file) throws IOException {
        this.file = file;
        // load file as json object
        this.map = new JSONObject(new JSONTokener(Files.readString(file.toPath())));
        logger.info("The cache has been correctly loaded !");
        logger.info(this.map.length() + " similarities are already known");
    }

    public void append(Entity phi1, Entity phi2, double value) {
//        logger.debug("Append the simalirity between " + phi1 + " and " + phi2 + " into the map");
        int key = phi1.hashCode() + phi2.hashCode();
        this.map.put(String.valueOf(key), value);
    }

    public Double get(Entity phi1, Entity phi2) {
        int key = phi1.hashCode() + phi2.hashCode();
        if(this.map.has(String.valueOf(key))) {
            return this.map.getDouble(String.valueOf(key));
        } else {
            return null;
        }
    }

    public void editFile() throws IOException {
        logger.info("Write the map content ...");
        Path path = Paths.get(Global.SIMILARITIES_FILE);
        Files.writeString(path, this.map.toString(2), StandardCharsets.UTF_8);
        logger.debug("Similarity map size = " + this.map.length());
    }

    public JSONObject getMap() {
        return this.map;
    }

}
