package com.i3s.app.rdfminer.sparql.corese;

import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.RDFMiner;
import com.i3s.app.rdfminer.sparql.RequestBuilder;
import io.searchbox.client.http.apache.HttpGetWithEntity;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * Sparql endpoint to manage and request the Corese server
 * @author Rémi FELIN
 */
public class CoreseEndpoint {

    private static final Logger logger = Logger.getLogger(CoreseEndpoint.class.getName());

    /**
     * The URL of the SPARQL endpoint.
     */
    public String url;

    /**
     * The service to query using Corese federated queries
     */
    public String service;

    /**
     * The prefixes that will be used to query the SPARQL endpoint.
     */
    public String prefixes;

    /**
     * The timeout used for each queries (in ms)
     */
    public long timeout = Integer.MAX_VALUE;

//    /**
//     * HTTP client
//     */
//    private final CloseableHttpClient httpClient = HttpClientBuilder.create().build();

    /**
     * Constructor of SparqlEndpoint
     * @param service the SPARQL endpoint to ask into federated query
     * @param prefixes the prefixes used for the queries
     */
    public CoreseEndpoint(String service, String prefixes) {
        this.url = Global.CORESE_IP;
        this.service = service;
        this.prefixes = prefixes;
    }

    public String addFederatedQuery(String sparql) {
        return "SERVICE <" + this.service + "> { " + sparql + " }";
    }

    public String addFederatedQueryWithLoop(String sparql, int limit) {
        return "SERVICE <" + this.service + "?loop=true&limit=" + limit + "> { " + sparql + " }";
    }

//    public String timeoutParam = "@timeout " + this.timeout + " ";

    public String buildSelectAllQuery(String sparql) {
//        System.out.println("sparql: " + RequestBuilder.select("*", sparql, this.timeout, true));
        return RequestBuilder.select("*", sparql, this.timeout, true);// "\nSELECT * WHERE { " + sparql + " }";
    }

    public String buildQuery(String basis, String sparql) {
//        System.out.println("sparql: " + RequestBuilder.select("*", sparql, this.timeout, true));
        return RequestBuilder.select(basis, sparql, this.timeout, true);// "\nSELECT * WHERE { " + sparql + " }";
    }


    public boolean askFederatedQuery(String sparql) throws URISyntaxException, IOException {
        String request = RequestBuilder.ask(addFederatedQuery(sparql), true);// "\nASK WHERE { " + addFederatedQuery(sparql) + " }";
//        logger.debug("ASK Request: " + request);
        String resultAsJSON = query(Format.JSON, request);
        return ResultParser.getResultFromAskQuery(resultAsJSON);
    }

    public List<String> selectFederatedQuery(String var, String sparql) throws URISyntaxException, IOException {
        String request = buildSelectAllQuery(addFederatedQuery(sparql));
//        System.out.println(request);
        String resultAsJSON = query(Format.JSON, request);
        return ResultParser.getResultsFromVariable(var, resultAsJSON);
    }

    public List<String> select(String var, String sparql) throws URISyntaxException, IOException {
        String resultAsJSON = query(Format.JSON, sparql);
        return ResultParser.getResultsFromVariable(var, resultAsJSON);
    }

    /**
     * <i>SELECT (count(distinct ?x) as ?n) WHERE { ... }</i> in SERVICE clause
     */
    public int count(String sparql) throws URISyntaxException, IOException {
        // "SELECT (count(distinct ?x) as ?n) WHERE { " + sparql + " }"));
        String request = buildSelectAllQuery(addFederatedQuery(RequestBuilder.select("(count(distinct ?x) as ?n)", sparql, this.timeout, false)));
        String resultAsJSON = query(Format.JSON, request);
        if(resultAsJSON.contains("Read timed out") || resultAsJSON.contains("connect timed out")) {
            // time out
            // i.e. SocketTimeoutException from Corese server
            return -1;
        }
        try {
            return Integer.parseInt(Objects.requireNonNull(ResultParser.getResultsFromVariable("n", resultAsJSON)).get(0));
        } catch (NullPointerException e) {
            logger.error("Error during the counting ...");
            logger.error("Result as JSON: " + resultAsJSON);
        }
        // return an error
        return -1;
    }

    /**
     * <i>SELECT (count(var) as ?n) WHERE { ... }</i> in SERVICE clause
     */
    public int count(String var, String sparql) throws URISyntaxException, IOException {
        // "SELECT (count(distinct ?x) as ?n) WHERE { " + sparql + " }"));
        String request = buildSelectAllQuery(addFederatedQuery(RequestBuilder.select("(count(" + var + ") as ?n)", sparql, this.timeout, false)));
        String resultAsJSON = query(Format.JSON, request);
        if(resultAsJSON.contains("Read timed out") || resultAsJSON.contains("connect timed out")) {
            // time out
            // i.e. SocketTimeoutException from Corese server
            return -1;
        }
        try {
            return Integer.parseInt(Objects.requireNonNull(ResultParser.getResultsFromVariable("n", resultAsJSON)).get(0));
        } catch (NullPointerException e) {
            logger.error("Error during the counting ...");
            logger.error("Result as JSON: " + resultAsJSON);
        }
        // return an error
        return -1;
    }

    /**
     * <i>SELECT (count(*) as ?n) WHERE { ?s ?p ?o }</i> in SERVICE clause
     */
    public int countAllFromCoreseTripleStore() throws URISyntaxException, IOException {
        // "SELECT (count(distinct ?x) as ?n) WHERE { " + sparql + " }"));
        String request = buildQuery("(count(*) as ?n)", "?s ?p ?o");
        String resultAsJSON = query(Format.JSON, request);
        if(resultAsJSON.contains("Read timed out") || resultAsJSON.contains("connect timed out")) {
            // time out
            // i.e. SocketTimeoutException from Corese server
            return -1;
        }
        try {
            return Integer.parseInt(Objects.requireNonNull(ResultParser.getResultsFromVariable("n", resultAsJSON)).get(0));
        } catch (NullPointerException e) {
            logger.error("Error during the counting ...");
            logger.error("Result as JSON: " + resultAsJSON);
        }
        // return an error
        return -1;
    }

    /**
     * Build a HTTP Request on Corese server : SELECT query
     * @param format the expected format
     * @param sparql the SPARQL Request
     * @return the result in the desired format
     * @throws URISyntaxException Error concerning the syntax of the given URL
     * @throws IOException Error concerning the execution of the POST request
     */
    public String query(String format, String sparql) throws URISyntaxException, IOException {
        // build the final URL
        final String service = this.url + CoreseService.CORESE_SPARQL_ENDPOINT;
        // specify all the query params needed to launch a request on Corese server
        HashMap<String, String> params = new HashMap<>();
        // specify SPARQL and Format in parameters
        params.put("query", sparql);
//        System.out.println("Request :\n" + sparql);
        params.put("format", format);
        // call the get method and return it result
        return get(service, params);
    }

    /**
     * Allow to send string content into shapes.ttl stored in the Corese server, it will replace the
     * previous content by the new one
     * @param file the file to upload, must contains SHACL Shapes
     * @throws URISyntaxException Error concerning the syntax of the given URL
     * @throws IOException Error concerning the execution of the POST request
     */
    public void sendFileToServer(File file, String nameAndExtension) throws URISyntaxException, IOException {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        // build the final URL
        final String service = this.url + CoreseService.CORESE_SEND_SHACL_SHAPES_ENDPOINT;
        URIBuilder builder = new URIBuilder(service);
        // set params
        builder.setParameter("name", nameAndExtension);
        // POST request
        HttpPost post = new HttpPost(builder.build());
        // set entity
        HttpEntity entity = MultipartEntityBuilder.create().addPart("file", new FileBody(file)).build();
        post.setEntity(entity);
        // launch service
        logger.info("send " + nameAndExtension + " to the server ...");
        HttpResponse response = httpClient.execute(post);
        if(response.getStatusLine().getStatusCode() != HttpURLConnection.HTTP_OK)
            logger.error("Error " + response.getStatusLine().getStatusCode() + " while sending SHACL Shapes on server ...");
    }

    public String getValidationReportFromServer(String content) throws URISyntaxException, IOException {
//        logger.info("Perform SHACL validation:\n" + content);
        // fill params
        HashMap<String, String> params = new HashMap<>();
        if(RDFMiner.parameters.useProbabilisticShaclMode) {
            params.put("mode", CoreseService.PROBABILISTIC_SHACL_EVALUATION);
            params.put("p", RDFMiner.parameters.probShaclP);
            params.put("n-triples", String.valueOf(Global.nTriples));
        } else {
            params.put("mode", CoreseService.SHACL_EVALUATION);
        }
//        params.put("uri", this.url + CoreseService.CORESE_GET_SHACL_SHAPES_ENDPOINT + "?name=" + Global.SHACL_SHAPES_FILENAME);
        params.put("query", "construct where {?s ?p ?o}");
        params.put("format", Format.TURTLE);
        // send GET request
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        URIBuilder builder = new URIBuilder(this.url + CoreseService.CORESE_SPARQL_ENDPOINT);
        // take the first elem of params parameter to get all keys and values
        for(String param : params.keySet()) {
            builder.setParameter(param, params.get(param));
        }
        // GET request
        HttpGetWithEntity get = new HttpGetWithEntity(builder.build());
        // set entity
        HttpEntity entity = MultipartEntityBuilder.create().addTextBody("content", content).build();//addPart("file", new FileBody(file)).build();
        get.setEntity(entity);
        // Accept header
        get.setHeader("Accept", "*/*");
        // exec
//        System.out.println(get.getRequestLine());
        HttpResponse response = httpClient.execute(get);
        // catch status code of the request
        if(response.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_OK) {
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
        } else {
            // this section handle the error from the request
            logger.error("request: " + get.getRequestLine());
            logger.error("Request fail (code " + response.getStatusLine().getStatusCode() + ")");
            return null;
        }
    }

//    public String getFileFromServer(String nameAndExtension) throws URISyntaxException, IOException {
//        final String service = this.url + CoreseService.CORESE_GET_SHACL_SHAPES_ENDPOINT;
//        // fill params
//        HashMap<String, String> params = new HashMap<>();
//        params.put("name", nameAndExtension);
//        return get(service, params);
//    }

    public String getFilePathFromServer(String nameAndExtension) {
        return this.url + CoreseService.CORESE_GET_SHACL_SHAPES_ENDPOINT + "?name=" + nameAndExtension;
    }

    /**
     * Send a specific file which contains RDF data to our server (which contains a datastore).
     * @param datapath the RDF data file path to send
     */
    public int sendRDFDataToDB(String datapath) throws URISyntaxException, IOException {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        // build the final URL
        final String service = Global.CORESE_IP + CoreseService.CORESE_LOAD_RDF;
        URIBuilder builder = new URIBuilder(service);
        // POST request
        HttpPost post = new HttpPost(builder.build());
        post.setHeader("Content-Type", "application/x-www-form-urlencoded");
        post.setHeader("Accept", "*/*");
        // set x-www-form-urlencoded body
        StringEntity entity = new StringEntity("remote_path=" + datapath,
                ContentType.APPLICATION_FORM_URLENCODED);
        post.setEntity(entity);
        // launch service
//        logger.info("save " + datapath + " into Corese graph ...");
        HttpResponse response = httpClient.execute(post);
        if(response.getStatusLine().getStatusCode() != HttpURLConnection.HTTP_OK)
            logger.error("Error " + response.getStatusLine().getStatusCode() + " while loading " + datapath + " on server ...");
        else
            logger.info("A RDF data file has been loaded in Corese server !");
        return response.getStatusLine().getStatusCode();
    }

    public String getHTMLResultFromSTTLTransformation(String sttl) throws URISyntaxException, IOException {
        // build the final URL
        final String service = this.url + CoreseService.CORESE_SPARQL_ENDPOINT;
        // specify all the query params needed to launch a request on Corese server
        HashMap<String, String> params = new HashMap<>();
        // specify sttl template
        params.put("query", sttl);
        // call the get method and return it result
        return get(service, params);
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
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        URIBuilder builder = new URIBuilder(service);
        // params
        if(params.length > 0) {
            // take the first elem of params parameter to get all keys and values
            for(String param : params[0].keySet()) {
                builder.setParameter(param, params[0].get(param));
            }
        }
        // GET request
        HttpGetWithEntity get = new HttpGetWithEntity(builder.build());
        // Accept header
        get.setHeader("Accept", "*/*");
        get.setEntity(MultipartEntityBuilder.create().addTextBody("content", "").build());
//        logger.info("HTTP Request: " + get);
        // exec
        HttpResponse response = httpClient.execute(get);
        // catch status code of the request
        if(response.getStatusLine().getStatusCode() != HttpURLConnection.HTTP_OK) {
            // this section handle the error from the request
            logger.error("request: " + get.getRequestLine());
            logger.error("Request fail (code " + response.getStatusLine().getStatusCode() + ")");
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

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

}
