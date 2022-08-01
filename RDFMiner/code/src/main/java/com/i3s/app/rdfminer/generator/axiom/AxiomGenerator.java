/**
 * 
 */
package com.i3s.app.rdfminer.generator.axiom;

import Individuals.Phenotype;
import Mapper.Production;
import Mapper.Rule;
import Mapper.Symbol;
import Util.Enums;
import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.expression.Expression;
import com.i3s.app.rdfminer.generator.Generator;
import com.i3s.app.rdfminer.sparql.corese.CoreseEndpoint;
import com.i3s.app.rdfminer.sparql.corese.Format;
import com.i3s.app.rdfminer.sparql.corese.ResultParser;
import com.i3s.app.rdfminer.sparql.virtuoso.VirtuosoEndpoint;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;

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
public abstract class AxiomGenerator extends Generator {

	private static final Logger logger = Logger.getLogger(AxiomGenerator.class.getName());

	/**
	 * Constructs a new axiom generator with no grammar attached.
	 */
	public AxiomGenerator() {
		super(null);
	}

	/**
	 * Constructs a new axiom generator for the language described by the given
	 * grammar.
	 * 
	 * @param fileName the name of the file containing the grammar.
	 * @param v2       if true, we used the second version (minimized) for the
	 *                 extraction of rules, else the first
	 */
	public AxiomGenerator(String fileName, boolean v2) throws URISyntaxException, IOException {
		super(fileName);
		logger.info("Grammar loaded. Adding dynamic productions...");
		if(v2) {
			logger.info("AxiomGenerator v2.0 used ...");
			for(int hexDigit = 0; hexDigit<0x10; hexDigit++)
			{
				String h = String.format("\"%x\"", hexDigit);
				logger.warn("Querying with FILTER(strStarts(MD5(?x), " + h + "))...");
				generateProductions("Class", "SELECT distinct ?Class WHERE {?Class a owl:Class . FILTER(contains(str(?Class), \"http://\")). FILTER( strStarts(MD5(str(?Class)), " + h + ") ) }");
				generateProductions("ObjectPropertyOf","SELECT distinct ?ObjectPropertyOf WHERE { ?subj ?ObjectPropertyOf ?obj . FILTER ( isIRI(?obj) ).FILTER( strStarts(MD5(str(?ObjectPropertyOf)), " + h + ") ) }");
			}
		} else {
			extract();
		}
	}

	public void extract() throws URISyntaxException, IOException {
		logger.info("AxiomGenerator v1.0 used ...");
		// Add dynamically-generated productions for the six primitive non-terminals
		// N.B.: To circumvent the limit imposed by Virtuoso on the number of results,
		// we split each query into 16 queries, based on the MD5 hash of the results.
		// Actually, this is useful only if we use a remote SPARQL endpoint...
		for (int hexDigit = 0; hexDigit < 0x10; hexDigit++) {
			String h = String.format("\"%x\"", hexDigit);
			logger.warn("Querying with FILTER(strStarts(MD5(?x), " + h + "))...");
			generateProductions("Class",
					"SELECT distinct ?Class WHERE { ?_ a ?class . FILTER( strStarts(MD5(str(?class)), " + h + ") ) }");
			if (!(this instanceof CandidateAxiomGenerator)) {
				// If it is a CandidateAxiomGenerator that is being constructed,
				// the following dynamic productions are not needed.
				generateProductions("ClassOtherThanOwlThing",
						"SELECT distinct ?ClassOtherThanOwlThing WHERE { ?x a ?ClassOtherThanOwlThing . FILTER ( ?ClassOtherThanOwlThing != owl:Thing ) FILTER( strStarts(MD5(str(?ClassOtherThanOwlThing)), "
								+ h + ") ) }");
				generateProductions("ObjectPropertyOf",
						"SELECT distinct ?ObjectPropertyOf WHERE { ?subj ?ObjectPropertyOf ?obj . FILTER ( isIRI(?obj) ) FILTER( strStarts(MD5(str(?ObjectPropertyOf)), "
								+ h + ") ) }");
				generateProductions("DataProperty",
						"SELECT distinct ?DataProperty WHERE { ?subj ?DataProperty ?obj . FILTER ( isLiteral(?obj) ) FILTER( strStarts(MD5(str(?DataProperty)), "
								+ h + ") ) }");
				generateProductions("NamedIndividual",
						"SELECT distinct ?NamedIndividual WHERE { ?NamedIndividual a ?class . FILTER ( isIRI(?NamedIndividual) ) FILTER( strStarts(MD5(str(?NamedIndividual)), "
								+ h + ") ) }");
				generateProductions("Literal",
						"SELECT distinct ?Literal WHERE { ?subj ?prop ?Literal . FILTER ( isLiteral(?Literal) ) FILTER( strStarts(MD5(str(?Literal)), "
								+ h + ") ) }");
			}
		}
	}

	@Override
	protected void generateProductions(String symbol, String sparql) throws URISyntaxException, IOException {
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
			}
			logger.info("File readed: " + cacheName(symbol, sparql) + ", " + rule.size() + " production(s) added !");
			cache.close();
		} catch (IOException ioe) {
			logger.info("Cache for " + symbol + " not found. Querying SPARQL endpoint");
			logger.info("Querying SPARQL endpoint for symbol <" + symbol + "> ...");
			CoreseEndpoint endpoint = new CoreseEndpoint(Global.CORESE_SPARQL_ENDPOINT, Global.VIRTUOSO_SMALL_DBPEDIA_2015_04_SPARQL_ENDPOINT, Global.PREFIXES);
			PrintStream cache = null;
			try {
				cache = new PrintStream(cacheName(symbol, sparql));
			} catch (FileNotFoundException e) {
				logger.warn("Could not create cache for symbol " + symbol + ".");
			}
			List<String> results = endpoint.selectFederatedQuery(symbol, sparql);
			if(results.size() > 0) {
				for(String result : results) {
					// declare a new production
					Production prod = new Production();
					// Create a symbol and add the result
					Symbol t = new Symbol(result, Enums.SymbolType.TSymbol);
					// add the symbol to production
					prod.add(t);
					// Write the cache with the symbol found
					assert cache != null;
					cache.println(t + " ");
					// Adding production founded by SPARQL Request
					rule.add(prod);
				}
			}

			if (cache != null)
				cache.close();
			logger.info("Done ! " + rule.size() + " productions added.");
		}
	}

	/**
	 * Generate the next random axiom.
	 * @return a random axiom
	 */
	public abstract Phenotype nextAxiom() throws URISyntaxException, IOException;

}
