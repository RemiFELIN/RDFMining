/**
 * 
 */
package com.i3s.app.rdfminer.sparql;

import java.io.File;
import java.util.Iterator;
import org.apache.jena.atlas.lib.Timer;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryException;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.shared.Lock;
import org.apache.jena.sparql.ARQException;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.apache.jena.tdb.TDBFactory;
import org.apache.log4j.Logger;

import com.i3s.app.rdfminer.Global;

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
public class SparqlEndpoint implements Iterator<QuerySolution> {
	
	private static Logger logger = Logger.getLogger(SparqlEndpoint.class.getName());

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
	 * The result of the current query.
	 */
	public ResultSet resultSet;

	/**
	 * The response time of the "SELECT" SPARQL Request on remote service (in ms)
	 */
	public static long selectResponseTime;
	
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
	public SparqlEndpoint(String url, String prefix) {
		if (new File(Global.DBPEDIA_TDB_PATH).exists()) {
			logger.warn("A local TBD directory exists. Using it as the SPARQL endpoint");
			endpoint = null;
			Dataset dataset = TDBFactory.createDataset(Global.DBPEDIA_TDB_PATH);
			tdb = dataset.getDefaultModel();
		} else {
			logger.warn("Service created, using the default SPARQL endpoint " + url);
			endpoint = url;
			tdb = ModelFactory.createDefaultModel();
		}
		prefixes = prefix;
		queryExecution = null;
		resultSet = null;
	}

	public SparqlEndpoint(Model model, String prefix) {
		endpoint = null;
		tdb = model;
		prefixes = prefix;
		queryExecution = null;
		resultSet = null;
	}

	/**
	 * Prepare the execution of the given query.
	 */
	protected void prepare(Query query) {
//		System.out.println("[PREPARE]\n" + query.toString() + "\n[/PREPARE]");
		// Close a pre-existing query, if any, to free resources:
		if (queryExecution != null) {
			queryExecution.close();
			resultSet = null;
		}
		// Prepare the execution context for the given query:
		if (endpoint != null) {
			queryExecution = (QueryEngineHTTP) QueryExecutionFactory.createServiceRequest(endpoint, query);
		} else {
			queryExecution = (QueryEngineHTTP) QueryExecutionFactory.create(query, tdb);
			tdb.enterCriticalSection(Lock.READ);
		}
	}

	/**
	 * Execute a SELECT query.
	 * 
	 * @param sparql The query string, to go after the "SELECT " keyword.
	 * @param timeout a timeout to compute result (in seconds)
	 */
	public void select(String sparql, int timeout) {
		try {
			String str = prefixes + "SELECT " + sparql;
			Query query = QueryFactory.create(str);
			prepare(query);
			// set a timeout if is not equals to 0
			if(timeout != 0) {
				queryExecution.addParam("timeout", Integer.toString(timeout * 1000));
			}
//			System.out.println(query);
			// Set a timer to compute the result time of each query
			Timer timer = new Timer();
			timer.startTimer();
			// execution of SPARQL "SELECT" request
			resultSet = queryExecution.execSelect();
//			System.out.println("time: " + timer.read());
			selectResponseTime = timer.endTimer();
		} catch (Exception e) {
			handleException(e, "making the following query:\nSELECT " + SparqlEndpoint.prettyPrint(sparql));
		}
	}

	public void selectAndCopyResults(String sparql) {
		try {
			String str = prefixes + "SELECT " + sparql;
			Query query = QueryFactory.create(str);
			prepare(query);
			// resultSet = queryExecution.execSelect();
			resultSet = ResultSetFactory.copyResults(queryExecution.execSelect());
		} catch (Exception e) {
			handleException(e, "making the following query:\nSELECT " + SparqlEndpoint.prettyPrint(sparql));
		}
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
			prepare(query);
			result = queryExecution.execAsk();
		} catch (QueryException q) {
			logger.warn("An ASK query failed: graph pattern =\n " + graphPattern
					+ "\nTrying with the corresponding SELECT...");
			select("* WHERE { " + graphPattern + " }", timeout);
			result = hasNext();
		} catch (Exception e) {
			handleException(e, "making the following query:\nASK { " + SparqlEndpoint.prettyPrint(graphPattern) + " }");
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
		select("(count(DISTINCT " + x + ") AS ?n) WHERE { " + graphPattern + " }", timeout);
		if (hasNext()) {
			QuerySolution solution = next();
			RDFNode n = solution.get("n");
			if (endpoint == null)
				tdb.leaveCriticalSection();
			return n.asLiteral().getInt();
		}
		return 0;
	}

	@Override
	public boolean hasNext() {
		boolean hasIt = false;
		try {
			if (resultSet != null) {
//				try {
				hasIt = resultSet.hasNext();
//					System.out.println("Ok !");
//				} catch(ARQException e) {
					// Fix :
					// ARQException: ResultSet no longer valid (QueryExecution has been closed)
//					logger.warn("result set no longer valid...");
//					System.out.println("TEST:\n- hasIt: " + hasIt + "\n- endpoint: " + endpoint + "\nFIN DU TEST");
					// We let hasIt to false, then return false and set the count to 0
//					return false;
//				}
			}
		} catch (Exception e) {
			// handleException(e, "checking whether another solution is available.");
			logger.warn("result set no longer valid...");
		}
		if (!hasIt && endpoint == null) {
			try {
				tdb.leaveCriticalSection();
				// Every once in a while, the method on the previous line throws an exception
				// because no lock is held...
				// This happens just after an ignored Null Pointer Exception
				// while checking whether another solution is available.
			} catch (Exception e) {
				handleException(e, "checking whether another solution is available.");
			}
		}
		return hasIt;
	}

	@Override
	public QuerySolution next() {
		try {
			if (resultSet != null)
				return resultSet.next();
		} catch (Exception e) {
			handleException(e, "retrieving the next solution.");
		}
		return null;
	}

	@Override
	public void remove() {
		try {
			if (resultSet != null)
				resultSet.remove();
		} catch (Exception e) {
			handleException(e, "removing a solution from the result set.");
		}
	}

	/**
	 * Handle an exception caught by one of the methods of this class.
	 * 
	 * @param e The exception to be handled
	 */
	private void handleException(Exception e, String context) {
		// Finally...
		if (queryExecution != null)
			queryExecution.close();
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
			prepare(query);
			tdb = queryExecution.execConstruct();
		} catch (Exception e) {
			handleException(e, "making the following query:\nCONSTRUCT " + SparqlEndpoint.prettyPrint(sparql));
		}
	}

	public ResultSet getResultSet() {
		return this.resultSet;
	}
}