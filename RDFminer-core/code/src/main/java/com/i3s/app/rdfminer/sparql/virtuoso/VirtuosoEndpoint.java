/**
 * 
 */
package com.i3s.app.rdfminer.sparql.virtuoso;

import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.apache.jena.tdb.TDBFactory;
import org.apache.log4j.Logger;

import java.io.File;

/**
 * A class to encapsulate a SPARQL endpoint and centralise all accesses to the
 * RDF store.
 * <p>
 * If a local directory named "<code>tdb</code>" exists, Jena TDB will be used
 * as the SPARQL executor. Otherwise, the SPARQL endpoint will default to the
 * url provided to the constructor.
 * </p>
 * 
 * <p>
 * When using Jena TDB, the query is locked to make this class thread-safe,
 * following the guidelines given in <a href=
 * "https://jena.apache.org/documentation/notes/concurrency-howto.html">this
 * note about concurrent access to models</a> in the Jena documentation.
 * </p>
 * 
 * @author Andrea G. B. Tettamanzi
 *
 */
public class VirtuosoEndpoint {
	
	private static Logger logger = Logger.getLogger(VirtuosoEndpoint.class.getName());

	/**
	 * The URL of the SPARQL endpoint.
	 * <p>
	 * If this attribute is <code>null</code>, this means the TDB on the local
	 * machine is to be used
	 * </p>
	 */
	public String endpoint;

	/**
	 * The RDF model corresponding to the TDB on the local machine.
	 * <p>
	 * If a TDB is not found and a remote SPARQL endpoint is used instead, this RDF
	 * model is initialized as a default model, once and for all, to be used for all
	 * operations that require a model.
	 * </p>
	 */
	public Model tdb;

	/**
	 * The prefixes that will be used to query the SPARQL endpoint.
	 */
	public String prefixes;

	/**
	 * The current query execution.
	 */
	public QueryEngineHTTP queryExecution;
	
	/**
	 * Create a new SPARQL endpoint.
	 * <p>
	 * If a local directory named "<code>tdb</code>" exists, Jena TDB will be used
	 * as the SPARQL executor. Otherwise, the SPARQL endpoint will default to the
	 * URL provided to the constructor.
	 * </p>
	 *
	 * @param url    The URL of the endpoint.
	 * @param prefix The SPARQL prefixes, that will be prepended to all queries.
	 */
	public VirtuosoEndpoint(String url, String prefix) {
		if (new File("tmp").exists()) {
			endpoint = null;
			Dataset dataset = TDBFactory.createDataset("tmp");
			tdb = dataset.getDefaultModel();
		} else {
			endpoint = url;
		}
		prefixes = prefix;
	}

	public VirtuosoEndpoint(Model model, String prefix) {
		endpoint = null;
		tdb = model;
		prefixes = prefix;
	}

	/**
	 * Execute a SELECT query.
	 * 
	 * @param sparql The query string, to go after the "SELECT " keyword.
	 * @param timeout a timeout to compute result (in seconds)
	 */
	public ResultSet select(String sparql, int timeout) {
		try {
			String str = prefixes + "\nSELECT " + sparql;
			Query query = QueryFactory.create(str);
			queryExecution = new QueryEngineHTTP(endpoint, query);
			if(timeout != 0) {
				queryExecution.addParam("timeout", Integer.toString(timeout * 1000));
			}
			// execution of SPARQL "SELECT" request
			ResultSet result = ResultSetFactory.copyResults(queryExecution.execSelect());
			queryExecution.close();
			return result;
		} catch (Exception e) {
			handleException(e, "making the following query:\nSELECT " + VirtuosoEndpoint.prettyPrint(sparql));
		}
		return null;
	}
	
	/**
	 * Execute an ASK query
	 * 
	 * @param graphPattern The graph pattern
	 * @param timeout a timeout to compute result (in seconds)
	 * @return
	 */
	public boolean ask(String graphPattern, int timeout) {
		boolean result = false;
		try {
			String str = prefixes + "ASK { " + graphPattern + " }";
			Query query = QueryFactory.create(str);
			queryExecution = new QueryEngineHTTP(endpoint, query);
			result = queryExecution.execAsk();
			queryExecution.close();
		} catch (QueryException q) {
			logger.warn("An ASK query failed: graph pattern =\n " + graphPattern
					+ "\nTrying with the corresponding SELECT...");
			ResultSet slct = select("* WHERE { " + graphPattern + " }", timeout);
			result = slct.hasNext();
			queryExecution.close();
		} catch (Exception e) {
			handleException(e, "making the following query:\nASK { " + VirtuosoEndpoint.prettyPrint(graphPattern) + " }");
		} finally {
			if (endpoint == null)
				tdb.leaveCriticalSection();
		}
		return result;
	}

	/**
	 * Perform a simple count query of the form
	 * <code>SELECT count(DISTINCT ?x) AS ?n WHERE</code>.
	 * <p>
	 * The <code>DISTINCT</code> modifier is needed to ensure that the same binding
	 * is not counted multiple times in complex graph patterns.
	 * </p>
	 * 
	 * @param x            the name of a SPARQL variable that must be bound in the
	 *                     graph pattern
	 * @param graphPattern a graph pattern
	 * @param timeout a timeout to compute result (in seconds)
	 * @return the count
	 */
	public int count(String x, String graphPattern, int timeout) {
		ResultSet result = select("(count(DISTINCT " + x + ") AS ?n) WHERE { " + graphPattern + " }", timeout);
		if (result.hasNext()) {
			QuerySolution solution = result.next();
			RDFNode n = solution.get("n");
			if (endpoint == null)
				tdb.leaveCriticalSection();
			return n.asLiteral().getInt();
		}
		return 0;
	}

	/**
	 * Handle an exception caught by one of the methods of this class.
	 * 
	 * @param e The exception to be handled
	 */
	private void handleException(Exception e, String context) {
		
		if (endpoint == null)
			tdb.leaveCriticalSection();
		// Now, depending on the kind of exception, take the most suitable action:
		if (e instanceof NullPointerException) {
			// It looks like when a thread is interrupted, this happens inside the Jena
			// SPARQL engine...
			logger.warn("Null Pointer Exception while " + context + "\n" + e.getMessage());
			logger.info("Ignoring...");
			// I have empirically determined that if we ignore this exception, everything
			// else goes as planned.
			return;
		}
		if (e instanceof QueryParseException) {
			logger.error("Query parse exception while " + context + "\n" + e.getMessage());
		} else if (e instanceof QueryException) {
			// logger.error("Query exception while " + context + "\n" + e.getMessage());
			// logger.error("Cause: " + e.getCause());
			e.printStackTrace();
			// This is what happens if there is a HTTP 504 Gateway Time-out:
			if (e.getCause() == null)
				// We signal this to the caller, so that it can take appropriate actions
				throw new IllegalStateException(e.getMessage());
		} else if (e instanceof HttpException) {
			HttpException httpe = (HttpException) e;
			logger.error("Caught an HTTP exception while " + context);
			logger.error(httpe.getMessage());
			logger.error("Cause: " + httpe.getCause().getMessage());
		}
		/*
		 * else if(e instanceof InterruptedException || e instanceof TimeoutException) {
		 * logger.warn("Interrupted|Timeout exception:\n" + e.getStackTrace()); // To
		 * compute the number of exceptions, we set the "isTimeout" of SubclassOfAxiom
		 * on true SubClassOfAxiom.isTimeout = true; }
		 */
		else
			logger.error(e.getMessage() + " while " + context);
		e.printStackTrace();
		System.exit(1);
	}

	/**
	 * An auxiliary method to add an indent of the given level to a line.
	 * 
	 * @param l level of indent
	 * @return a string of spaces of a length proportional to l
	 */
	private static String indent(int l) {
		String s = "";
		for (int i = 0; i < l; i++)
			s += "  ";
		return s;
	}

	/**
	 * Pretty-print a SPARQL query or a part of it, like a graph pattern.
	 * 
	 * @param query a string in SPARQL syntax
	 * @return a pretty-printed version of the input string
	 */
	public static String prettyPrint(String query) {

		int level = 0;
		String pretty = "";
		boolean bol = true; // beginning of line
		// TO DO: pretty-print...
		// 1) replace sequences of blanks by a single blank
		// 2) use '{' and '}' to control indentation
		String[] tokens = query.split("\\s");
		for (int i = 0; i < tokens.length; i++) {
			if (tokens[i].equals(".")) {
				pretty += " .\n";
				bol = true;
			} else if (tokens[i].equals("{")) {
				if (!bol)
					pretty += "\n";
				pretty += indent(level++) + "{\n";
				bol = true;
			} else if (tokens[i].equals("}")) {
				if (!bol)
					pretty += "\n";
				pretty += indent(--level) + "}\n";
				bol = true;
			} else if (!tokens[i].isEmpty()) {
				if (bol)
					pretty += indent(level);
				else
					pretty += " ";
				pretty += tokens[i];
				bol = false;
			}
		}
		return pretty;
	}

	/**
	 * Execute a CONSTRUCT query.
	 * 
	 * @param sparql The query string, to go after the "CONSTRUCT " keyword.
	 */
	public void construct(String sparql) {
		try {
			String str = prefixes + "CONSTRUCT " + sparql;
			Query query = QueryFactory.create(str);
			QueryEngineHTTP queryExecution = QueryExecutionFactory.createServiceRequest(endpoint, query);
			tdb = queryExecution.execConstruct();
			queryExecution.close();
		} catch (Exception e) {
			handleException(e, "making the following query:\nCONSTRUCT " + VirtuosoEndpoint.prettyPrint(sparql));
		}
	}

}
