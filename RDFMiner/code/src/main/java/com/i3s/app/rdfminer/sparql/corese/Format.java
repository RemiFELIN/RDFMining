package com.i3s.app.rdfminer.sparql.corese;

/**
 * Different format used to read and push data through Corese and Virtuoso server
 */
public class Format {

    /**
     * Format : TURTLE
     */
    public static final String TURTLE = "turtle";

    /**
     * Format : JSON
     */
    public static final String JSON = "json";

    /**
     * Type result : URI
     */
    public static final String TYPE_URI = "uri";

    /**
     * Type result : typed-literal
     */
    public static final String TYPE_LITERAL = "typed-literal";

    /**
     * Datatype : integer
     */
    public static final String DATATYPE_INTEGER = "http://www.w3.org/2001/XMLSchema#integer";

}
