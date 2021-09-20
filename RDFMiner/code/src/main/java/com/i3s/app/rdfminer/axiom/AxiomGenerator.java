/**
 * 
 */
package com.i3s.app.rdfminer.axiom;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.log4j.Logger;

import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.expression.Expression;
import com.i3s.app.rdfminer.grammar.DLGEGrammar;
import com.i3s.app.rdfminer.sparql.SparqlEndpoint;

import Individuals.Phenotype;
import Mapper.ContextFreeGrammar;
import Mapper.DerivationTree;
import Mapper.Production;
import Mapper.Rule;
import Mapper.Symbol;
import Util.Enums;

/**
 * An generator of OWL 2 axiom.
 * <p>
 * This is the base class for all axiom generators. The axioms are returned one
 * by one by the {@link #nextAxiom()} method.
 * </p>
 * 
 * @author Andrea G. B. Tettamanzi & Rémi FELIN
 *
 */
public abstract class AxiomGenerator {

	private static Logger logger = Logger.getLogger(AxiomGenerator.class.getName());

	/**
	 * The grammar defining the logical language of the axioms.
	 */
	protected DLGEGrammar grammar;

	/**
	 * Constructs a new axiom generator with no grammar attached.
	 */
	public AxiomGenerator() {
		grammar = null;
	}

	/**
	 * Constructs a new axiom generator for the language described by the given
	 * grammar.
	 * 
	 * @param fileName the name of the file containing the grammar.
	 * @param v2       if true, we used the second version (minimized) for the
	 *                 extraction of rules, else the first
	 * @throws InterruptedException
	 */
	public AxiomGenerator(String fileName, boolean v2) throws InterruptedException {
		// Set up the grammar to be used for generating the axioms:
		grammar = new DLGEGrammar(fileName);
		grammar.setDerivationTreeType(DerivationTree.class.getName());
		// grammar.setDerivationTreeType(ContextualDerivationTree.class.getName());
		grammar.setMaxDerivationTreeDepth(100);

		// System.out.println(grammar);
		logger.info("Grammar loaded. Adding dynamic productions...");

		if (v2) {
			logger.info("AxiomGenerator v2.0 used ...");
			// set a collection of Callable<Void>, corresponding to a collection of future
			// tasks
			Set<Callable<Void>> callables = new HashSet<Callable<Void>>();
			for (int hexDigit = 0; hexDigit < 0x10; hexDigit++) {
				String h = String.format("\"%x\"", hexDigit);
				callables.add(new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						logger.warn("Querying with FILTER(strStarts(MD5(?x), " + h + "))...");
						generateProductions("Class",
								"distinct ?class where {?class a owl:Class. FILTER(contains(str(?class), \"http://\")). FILTER( strStarts(MD5(str(?class))  , "
										+ h + ") )  }");
						return null;
					}
				});
				
				callables.add(new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						logger.warn("Querying with FILTER(strStarts(MD5(?x), " + h + "))...");
						generateProductions("ObjectPropertyOf",
								"DISTINCT ?prop WHERE { ?subj ?prop ?obj. FILTER ( isIRI(?obj) ).FILTER( strStarts(MD5(str(?prop)), "
										+ h + ") ) }");
						return null;
					}
				});
			}
			logger.info(callables.size() + " tasks are ready to be launched !");
			// We have a set of threads to compute each tasks
			ExecutorService executor = Executors.newFixedThreadPool(Global.NB_THREADS);
			// Submit tasks
			executor.invokeAll(callables);
			// Shut down the executor
			executor.shutdown();
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} else {
			extract();
		}
	}
	
	public void extract() throws InterruptedException {
		logger.info("AxiomGenerator v1.0 used ...");
		// set a collection of Callable<Void>, corresponding to a collection of future
		// tasks
		Set<Callable<Void>> callables = new HashSet<Callable<Void>>();
		// Add dynamically-generated productions for the six primitive non-terminals
		// N.B.: To circumvent the limit imposed by Virtuoso on the number of results,
		// we split each query into 16 queries, based on the MD5 hash of the results.
		// Actually, this is useful only if we use a remote SPARQL endpoint...
		for (int hexDigit = 0; hexDigit < 0x10; hexDigit++) {

			String h = String.format("\"%x\"", hexDigit);
			logger.warn("Querying with FILTER(strStarts(MD5(?x), " + h + "))...");

			callables.add(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					generateProductions("Class",
							"DISTINCT ?class WHERE { ?_ a ?class . FILTER( strStarts(MD5(str(?class)), " + h + ") ) }");
					return null;
				}
			});

			if (!(this instanceof CandidateAxiomGenerator)) {
				// If it is a CandidateAxiomGenerator that is being constructed,
				// the following dynamic productions are not needed.

				// Class-other-than-owl:Thing
				callables.add(new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						generateProductions("Class-other-than-owl:Thing",
								"DISTINCT ?class WHERE { ?_ a ?class . FILTER ( ?class != owl:Thing ) FILTER( strStarts(MD5(str(?class)), "
										+ h + ") ) }");
						return null;
					}
				});
				// ObjectPropertyOf
				callables.add(new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						generateProductions("ObjectPropertyOf",
								"DISTINCT ?prop WHERE { ?subj ?prop ?obj . FILTER ( isIRI(?obj) ) FILTER( strStarts(MD5(str(?prop)), "
										+ h + ") ) }");
						return null;
					}
				});
				// DataProperty
				callables.add(new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						generateProductions("DataProperty",
								"DISTINCT ?prop WHERE { ?subj ?prop ?obj . FILTER ( isLiteral(?obj) ) FILTER( strStarts(MD5(str(?prop)), "
										+ h + ") ) }");
						return null;
					}
				});
				// NamedIndividual
				callables.add(new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						generateProductions("NamedIndividual",
								"DISTINCT ?ind WHERE { ?ind a ?class . FILTER ( isIRI(?ind) ) FILTER( strStarts(MD5(str(?ind)), "
										+ h + ") ) }");
						return null;
					}
				});
				// Literal
				callables.add(new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						generateProductions("Literal",
								"DISTINCT ?obj WHERE { ?subj ?prop ?obj . FILTER ( isLiteral(?obj) ) FILTER( strStarts(MD5(str(?obj)), "
										+ h + ") ) }");
						return null;
					}
				});
			}
		}
		logger.info(callables.size() + " tasks are ready to be launched !");
		// We have a set of threads to compute each tasks
		ExecutorService executor = Executors.newFixedThreadPool(Global.NB_THREADS);
		// Submit tasks
		executor.invokeAll(callables);
		// Shut down the executor
		executor.shutdown();
		executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
	}

	/**
	 * Generate a cache file name from a SPARQL query, so that each file has a
	 * different name.
	 */
	public static String cacheName(String symbol, String sparql) {
		return String.format(Global.CACHE_PATH + "%s%08x.cache", symbol, sparql.hashCode());
	}

	/**
	 * Dynamically generates the productions for the rule corresponding to the given
	 * symbol using the given SPARQL query.
	 * <p>
	 * If a rule for the given symbol does not exist, it is created; if it exists,
	 * the dynamically-generated productions are simply added to the static
	 * productions defined in the grammar.
	 * </p>
	 * 
	 * @param symbol the name of a non-terminal symbol for which the productions are
	 *               to be generated
	 * @param sparql the <code>SELECT</code> clause of a SPARQL query
	 */
	protected void generateProductions(String symbol, String sparql) {
		Rule rule = grammar.findRule(symbol);
		if (rule == null) {
			rule = new Rule();
			rule.setLHS(new Symbol(symbol, Enums.SymbolType.NTSymbol));
			grammar.getRules().add(rule);
			logger.debug("Added a new (dynamical) rule for " + rule.getLHS());
		}

		try {
			// Try to read the productions from a cache file named after the query:
			BufferedReader cache = new BufferedReader(new FileReader(cacheName(symbol, sparql)));
			while (true) {
				String s = cache.readLine();
				if (s == null)
					break;

				Production prod = new Production();
				Symbol t = new Symbol(s, Enums.SymbolType.TSymbol);
				prod.add(t);
				rule.add(prod);
				// logger.debug("Added production " + prod);
			}
			cache.close();
		} catch (IOException ioe) {
			logger.debug("Cache for " + symbol + " not found. Querying SPARQL endpoint...");

			logger.info("Querying SPARQL endpoint for symbol <" + symbol + "> with query:\nSELECT "
					+ SparqlEndpoint.prettyPrint(sparql));

			SparqlEndpoint endpoint = new SparqlEndpoint(Global.LOCAL_SPARQL_ENDPOINT, Global.LOCAL_PREFIXES);
			ResultSet result = endpoint.select(sparql, 0);
			PrintStream cache = null;
			try {
				cache = new PrintStream(cacheName(symbol, sparql));
			} catch (FileNotFoundException e) {
				logger.warn("Could not create cache for symbol " + symbol + ".");
			}

			while (result.hasNext()) {
				Production prod = new Production();
				QuerySolution solution = result.next();
				Iterator<String> i = solution.varNames();
				String separator = "";
				while (i.hasNext()) {
					String varName = i.next();
					RDFNode node = solution.get(varName);
					if (node.toString().length() > 0) {
						// We SPARQL-encode the RDF nodes retrieved
						// to avoid losing information on the node's type, i.e.,
						// resource or literal...
						Symbol t = new Symbol(separator + Expression.sparqlEncode(node), Enums.SymbolType.TSymbol);
						// This was: Symbol t = new Symbol(separator + node.toString(),
						// Enums.SymbolType.TSymbol);
						prod.add(t);
						if (cache != null)
							cache.println(t);
						separator = " ";
					} else
						logger.warn("Found a node with an empty string representation");
				}
				rule.add(prod);
			}
			if (cache != null)
				cache.close();
		}
		logger.info("Done! " + rule.size() + " productions added.");
	}

	public ContextFreeGrammar getGrammar() {
		return grammar;
	}

	/**
	 * Generate the next random axiom.
	 * 
	 * @return a random axiom
	 */
	public abstract Phenotype nextAxiom();

}
