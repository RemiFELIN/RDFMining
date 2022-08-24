package com.i3s.app.rdfminer.sparql;

import com.i3s.app.rdfminer.Global;

/**
 * This class is used to build SELECT ; ASK ; CONSTRUCT query with a predefined template
 * @author Rémi FELIN
 */
public class RequestBuilder {

    /**
     * Build SELECT query
     * @param vars element(s) to select such as : <code>( ?s; distinct ?s ; ?s ?p ; ... )</code>
     * @param body the body of the request such as : <code>( ?s ?p ?o ; ?s a ?o ; ... )</code>
     * @return the SELECT request
     */
    public static String select(String vars, String body) {
        return Global.PREFIXES + "\nSELECT " + vars + " WHERE { " + body + " }";
    }

    public static String ask(String body) {
        return Global.PREFIXES + "\nASK { " + body + " }";
    }

}
