package com.i3s.app.rdfminer.generator.shacl;

import Mapper.Production;
import Mapper.Rule;
import Mapper.Symbol;
import Util.Enums;
import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.generator.Generator;
import com.i3s.app.rdfminer.sparql.corese.CoreseEndpoint;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.URISyntaxException;
import java.util.List;

/**
 * An generator of SHACL Shape.
 * <p>
 * This is the base class for all SHACL Shapes generators
 * </p>
 *
 * @author Rémi FELIN
 *
 */
public abstract class ShapeGenerator extends Generator {

    private static final Logger logger = Logger.getLogger(ShapeGenerator.class.getName());

    /**
     * Constructs a new SHACL Shape generator for the language described by the given grammar.
     * @param fileName the name of the file containing the grammar.
     * @throws URISyntaxException Error concerning the syntax of the given URL
     * @throws IOException Error concerning the execution of the GET request
     */
    ShapeGenerator(String fileName) throws URISyntaxException, IOException {
        super(fileName);
        // set shapes generator status to true
        generateShapes = true;
        logger.info("Grammar loaded. Adding dynamic productions...");
        // get '?class' productions
        for(int hexDigit = 0; hexDigit<0x10; hexDigit++) {
            String h = String.format("\"%x\"", hexDigit);
            logger.warn("Querying with FILTER(strStarts(MD5(?x), " + h + "))...");
            // SPARQL Request
            generateProductions("Class", "SELECT distinct ?Class WHERE { ?x a ?Class . FILTER( strStarts(MD5(str(?Class)), " + h + ") ) }");
            generateProductions("Property","SELECT distinct ?Property WHERE { ?subj ?Property ?obj . FILTER ( isIRI(?Property) ) . FILTER( strStarts(MD5(str(?Property)), " + h + ") ) }");
            generateProductions("Node", "SELECT distinct ?Node WHERE { ?Node ?p ?o . FILTER( strStarts(MD5(str(?Node)), " + h + ") ) } LIMIT 1000");
            generateProductions("DataType", "SELECT distinct ?DataType WHERE { { SELECT distinct ?o WHERE { ?s ?p ?o . FILTER ( isLiteral(?o) ) } } BIND( datatype(?o) as ?DataType ) . FILTER( strStarts(MD5(str(?DataType)), " + h + ") ) }");
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
            CoreseEndpoint endpoint = new CoreseEndpoint(Global.CORESE_SPARQL_ENDPOINT, Global.PREFIXES);
            PrintStream cache = null;
            try {
                cache = new PrintStream(cacheName(symbol, sparql));
            } catch (FileNotFoundException e) {
                logger.warn("Could not create cache for symbol " + symbol + ".");
            }

            List<String> results = endpoint.select(symbol, sparql);
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
            logger.info("Done! " + rule.size() + " productions added.");
            if (cache != null) cache.close();
        }
    }

}
