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

    /**
     * Append the two keys: (phi1.toString.hashCode,phi2.toString.hashCode) and (phi2.toString.hashCode,phi1.toString.hashCode)
     * into similarities map
     * @param phi1 the first axiom
     * @param phi2 the second axiom
     * @param value similarity values between them
     */
    public void append(Entity phi1, Entity phi2, double value) {
//        logger.debug("Append the simalirity between " + phi1 + " and " + phi2 + " into the map");
        String k1 = String.valueOf(phi1.hashCode()).concat(String.valueOf(phi2.hashCode()));
        String k2 = String.valueOf(phi2.hashCode()).concat(String.valueOf(phi1.hashCode()));
        this.map.put(k1, value);
        this.map.put(k2, value);
    }

    public Double get(Entity phi1, Entity phi2) {
        String k1 = String.valueOf(phi1.hashCode()).concat(String.valueOf(phi2.hashCode()));
        String k2 = String.valueOf(phi2.hashCode()).concat(String.valueOf(phi1.hashCode()));
        if(this.map.has(k1)) {
            return this.map.getDouble(k1);
        } else if (this.map.has(k2)) {
            return this.map.getDouble(k2);
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

//    public static void main(String[] args) {
//        System.out.println("Toto -> " + "Toto".hashCode());
//        System.out.println("TATA -> " + "TATA".hashCode());
//        System.out.println("Toto".hashCode() + "TATA".hashCode());
//        System.out.println(String.valueOf("Toto".hashCode()).concat(String.valueOf("TATA".hashCode())));
//    }

}
