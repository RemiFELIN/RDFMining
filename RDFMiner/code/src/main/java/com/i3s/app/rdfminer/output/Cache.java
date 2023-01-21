package com.i3s.app.rdfminer.output;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Cache {

    private static final Logger logger = Logger.getLogger(Cache.class.getName());

    public int curGeneration;

    public int curCheckpoint;

    public int lenChromosomes;

    public ArrayList<String> genotypes = new ArrayList<>();

    public Cache(File file) throws IOException {
        String content = Files.readString(file.toPath());
        // using org.json, get results (String) into a JSON Object
        JSONTokener tokener = new JSONTokener(content);
        // load it
        JSONObject loaded = new JSONObject(tokener);
        if(loaded.get("curGeneration") != null && loaded.get("curCheckpoint") != null &&
                loaded.get("genotypes") != null) {
            this.curGeneration = loaded.getInt("curGeneration");
            this.curCheckpoint = loaded.getInt("curCheckpoint");
            this.lenChromosomes = loaded.getInt("lenChromosomes");
            for(int i=0; i<loaded.getJSONArray("genotypes").length(); i++) {
                this.genotypes.add(loaded.getJSONArray("genotypes").getString(i));
            }
        } else {
            logger.error("The loaded JSON file is not valid !");
        }
    }

    public Cache(int curGeneration, int curCheckpoint, int lenChromosomes, ArrayList<String> genotypes) {
        this.curGeneration = curGeneration;
        this.curCheckpoint = curCheckpoint;
        this.lenChromosomes = lenChromosomes;
        this.genotypes = genotypes;
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("curGeneration", this.curGeneration);
        json.put("curCheckpoint", this.curCheckpoint);
        json.put("lenChromosomes", this.lenChromosomes);
        json.put("genotypes", new JSONArray(this.genotypes));
        return json;
    }

}
