package com.i3s.app.rdfminer.output;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

public class Cache {

    private static final Logger logger = Logger.getLogger(Cache.class.getName());

    public final static String CUR_GENERATION = "curGeneration";
    public final static String CUR_CHECKPOINT = "curCheckpoint";
    public final static String INDIVIDUALS = "individuals";
    public final static String LEN_CHROMOSOMES = "lenChromosomes";

    public int curGeneration;
    public int curCheckpoint;
    public int lenChromosomes;
    public ArrayList<JSONObject> individualsJSON = new ArrayList<>();

    public Cache(File file) throws IOException {
        String content = Files.readString(file.toPath());
        // using org.json, get results (String) into a JSON Object
        JSONTokener tokener = new JSONTokener(content);
        // load it
        JSONObject loaded = new JSONObject(tokener);
        if(loaded.get(CUR_GENERATION) != null && loaded.get(CUR_CHECKPOINT) != null &&
                loaded.get(INDIVIDUALS) != null) {
            this.curGeneration = loaded.getInt(CUR_GENERATION);
            this.curCheckpoint = loaded.getInt(CUR_CHECKPOINT);
            this.lenChromosomes = loaded.getInt(LEN_CHROMOSOMES);
            for(int i=0; i<loaded.getJSONArray(INDIVIDUALS).length(); i++) {
                this.individualsJSON.add(loaded.getJSONArray(INDIVIDUALS).getJSONObject(i));
            }
        } else {
            logger.error("The loaded JSON file is not valid !");
        }
    }

    public Cache(int curGeneration, int curCheckpoint, int lenChromosomes, ArrayList<JSONObject> individualsJSON) {
        this.curGeneration = curGeneration;
        this.curCheckpoint = curCheckpoint;
        this.lenChromosomes = lenChromosomes;
        this.individualsJSON = individualsJSON;
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put(CUR_GENERATION, this.curGeneration);
        json.put(CUR_CHECKPOINT, this.curCheckpoint);
        json.put(LEN_CHROMOSOMES, this.lenChromosomes);
        json.put(INDIVIDUALS, new JSONArray(this.individualsJSON));
        return json;
    }

}
