package com.i3s.app.rdfminer.sparql.corese;

import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.sparql.RequestBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.*;
import java.util.HashMap;

/**
 * Sparql endpoint to manage and request the Corese server
 * @author Rémi FELIN
 */
public class SparqlEndpoint {

    private static final Logger logger = Logger.getLogger(SparqlEndpoint.class);

    /**
     * The URL of the SPARQL endpoint.
     */
    public String url;

    /**
     * The prefixes that will be used to query the SPARQL endpoint.
     */
    public String prefixes;

    /**
     * HTTP client
     */
    private final CloseableHttpClient httpClient = HttpClientBuilder.create().build();

    /**
     * Corese service : SPARQL endpoint
     */
    public static final String CORESE_SPARQL_ENDPOINT = "sparql";

    /**
     * Corese service : send SHACL Shapes endpoint
     */
    public final String CORESE_SEND_SHACL_SHAPES_ENDPOINT = "rdfminer/send/shapes";

    /**
     * Corese service : get SHACL Shapes endpoint
     */
    public final String CORESE_GET_SHACL_SHAPES_ENDPOINT = "rdfminer/shacl/shapes";

    /**
     * Constructor of SparqlEndpoint
     * @param url the IP Address of Corese server
     * @param prefixes the prefixes used for the queries
     */
    public SparqlEndpoint(String url, String prefixes) {
        this.url = url;
        this.prefixes = prefixes;
    }

    /**
     * Build a HTTP Request on Corese server : SELECT query
     * @param format the expected format
     * @param sparql the SPARQL Request
     * @return the result in the desired format
     * @throws URISyntaxException Error concerning the syntax of the given URL
     * @throws IOException Error concerning the execution of the POST request
     */
    public String select(String format, String sparql) throws URISyntaxException, IOException {
        // build the final URL
        final String service = url + CORESE_SPARQL_ENDPOINT;
        // specify all the query params needed to launch a request on Corese server
        HashMap<String, String> params = new HashMap<>();
        params.put("query", sparql);
        params.put("format", format);
        // call the get method and return it result
        return get(service, params);
    }

    /**
     * Allow to send string content into shapes.ttl stored in the Corese server, it will replace the
     * previous content by the new one
     * @param fileContent content of the future file, must contains SHACL Shapes
     * @return the HTTP Status code of the request
     * @throws URISyntaxException Error concerning the syntax of the given URL
     * @throws IOException Error concerning the execution of the POST request
     */
    public int sendSHACLShapesToServer(String fileContent) throws URISyntaxException, IOException {
        // build the final URL
        final String service = url + CORESE_SEND_SHACL_SHAPES_ENDPOINT;
        URIBuilder builder = new URIBuilder(service);
        // params
        builder.setParameter("content", fileContent);
        // POST request
        HttpPost post = new HttpPost(builder.build());
        HttpResponse response = httpClient.execute(post);
        return response.getStatusLine().getStatusCode();
    }

    /**
     * Allow to read the SHACL Shapes file (shapes.ttl) stored in the Corese server
     * @return the content of the file: shapes.ttl
     * @throws URISyntaxException Error concerning the syntax of the given URL
     * @throws IOException Error concerning the execution of the POST request
     */
    public String getSHACLShapesFromServer() throws URISyntaxException, IOException {
        // build the final URL
        final String service = url + CORESE_GET_SHACL_SHAPES_ENDPOINT;
        // call the get method and return it result
        return get(service);
    }

    /**
     * GET Request send to the server using a given service
     * @param service URL of the service endpoint
     * @param params [OPTIONAL] the query params of the request
     * @return the result of the given request
     * @throws URISyntaxException Error concerning the syntax of the given URL
     * @throws IOException Error concerning the execution of the POST request
     */
    @SafeVarargs
    public final String get(String service, HashMap<String, String>... params) throws URISyntaxException, IOException {
        URIBuilder builder = new URIBuilder(service);
        // params
        if(params.length > 0) {
            // take the first elem of params parameter to get all keys and values
            for(String param : params[0].keySet()) {
                builder.setParameter(param, params[0].get(param));
            }
        }
        // GET request
        HttpGet get = new HttpGet(builder.build());
        // Accept header
        get.setHeader("Accept", "*/*");
        // exec
        HttpResponse response = httpClient.execute(get);
        // catch status code of the request
        if(response.getStatusLine().getStatusCode() != HttpURLConnection.HTTP_OK) {
            // this section handle the error from the request
            logger.error("code:" + response.getStatusLine().getStatusCode() + " ; Request fail !");
            return null;
        }
        // the request has a 200 OK response from the server
        // read the content of the response
        BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String inputLine;
        StringBuilder sb = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            sb.append(inputLine);
        }
        in.close();
        return sb.toString();
    }

    public static void main(String[] args) throws URISyntaxException, IOException {
        SparqlEndpoint endpoint = new SparqlEndpoint(Global.CORESE_IP_ADDRESS, Global.CORESE_PREFIXES);
        for(int hexDigit = 0; hexDigit<0x10; hexDigit++) {
            String h = String.format("\"%x\"", hexDigit);
            System.out.println("### h=" + h);
            String sparql = RequestBuilder.buildSelectRequest("distinct ?class", "?class a ?z. FILTER(contains(str(?class), \"http://\")). FILTER( strStarts(MD5(str(?class))  , " + h + ") )");
            String res = endpoint.select(Format.FORMAT_JSON, sparql);
            ResultParser.getResultsfromVariable("class", res);
        }

    }

}
