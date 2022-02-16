package com.i3s.app.rdfminer.sparql;

/**
 * This class is used to build SELECT ; ASK ; CONSTRUCT query with a predefined template
 * @author Rémi FELIN
 */
public class RequestBuilder {

    /**
     * Build SELECT query
     * @param toSelect elements to select such as : <code>( ?s; distinct ?s ; ?s ?p ; ... )</code>
     * @param body the body of the request such as : <code>( ?s ?p ?o ; ?s a ?o ; ... )</code>
     * @return the SELECT request
     */
    public static String buildSelectRequest(String toSelect, String body) {
        return "SELECT " + toSelect + " WHERE { " + body + " }";
    }

}
