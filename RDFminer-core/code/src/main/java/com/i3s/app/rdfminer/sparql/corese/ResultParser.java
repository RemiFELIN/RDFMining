package com.i3s.app.rdfminer.sparql.corese;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Analyse and parse results from requests on Corese server
 * @author Rémi FELIN
 */
public class ResultParser {

    private static final Logger logger = Logger.getLogger(ResultParser.class);

    public static boolean getResultFromAskQuery(String json) {
        // using org.json, get results (String) into a JSON Object
        JSONTokener tokener = new JSONTokener(json);
        try {
            JSONObject resultAsJson = new JSONObject(tokener);
            return resultAsJson.getBoolean("boolean");
        } catch (JSONException e) {
            logger.error("Error during the ASK SPARQL Query");
            logger.warn("Received JSON: " + json);
        }
        return false;
    }

    public static List<String> getResultsFromVariable(String var, String json) {
        List<String> results = new ArrayList<>();
//        System.out.println(json);
        // if json does not contains any result, we must return an empty list
        if(json == null) {
            logger.warn("The given json param is null ...");
            return results;
        }
        // using org.json, get results (String) into a JSON Object
        JSONTokener tokener = new JSONTokener(json);
        JSONObject resultsJSON;
        try {
            resultsJSON = new JSONObject(tokener);
        } catch (JSONException e) {
            // Read time out
            // i.e. SocketTimeoutException from Corese server
//            if(json.contains("Read timed out") || json.contains("connect timed out")) {
//                logger.warn("Timeout reached !");
//            }
            return null;
        }


        // if var is not in json results, return an error
        JSONArray vars = resultsJSON.getJSONObject("head").getJSONArray("vars");
        boolean inVars = false;
        for(int i=0; i<vars.length(); i++) {
            if(Objects.equals(vars.getString(i), var.replace("?", ""))) {
                inVars = true;
                break;
            }
        }
        if(!inVars) {
            logger.error("variable " + var + " is not found in vars list from results !");
            return results;
        }

        // Now, we iterate on each bindings result to get the URI, Literal, ...
        // find for the given variable
        JSONArray bindings = resultsJSON.getJSONObject("results").getJSONArray("bindings");
        if(bindings.length() == 0) {
//            if(json.contains("Read timed out")) {
//                logger.info("Timeout reached !");
//                return null;
//            } else {
//            logger.warn("No results are found !");
            return results;
//            }
        }

        for(int i=0; i<bindings.length(); i++) {
            // get object from given var
            JSONObject choosenVar = bindings.getJSONObject(i).getJSONObject(var.replace("?", ""));
            String type = choosenVar.getString("type");
            if(Objects.equals(type, Format.TYPE_URI)) {
                // URI case
                results.add("<" + choosenVar.getString("value") + ">");
            } else {
                // Literal ; Integer ; ...
                results.add(choosenVar.getString("value"));
            }
        }
        return results;
    }

    public static int getSizeBindings(String json) {
        // using org.json, get results (String) into a JSON Object
        JSONTokener tokener = new JSONTokener(json);
        JSONObject resultAsJson = new JSONObject(tokener);
        return resultAsJson.getJSONObject("results").getJSONArray("bindings").length();
    }

}
