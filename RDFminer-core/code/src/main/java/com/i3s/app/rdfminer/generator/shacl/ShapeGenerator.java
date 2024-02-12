package com.i3s.app.rdfminer.generator.shacl;

import com.i3s.app.rdfminer.Parameters;
import com.i3s.app.rdfminer.evolutionary.geva.Mapper.Production;
import com.i3s.app.rdfminer.evolutionary.geva.Mapper.Rule;
import com.i3s.app.rdfminer.evolutionary.geva.Mapper.Symbol;
import com.i3s.app.rdfminer.evolutionary.geva.Util.Enums;
import com.i3s.app.rdfminer.generator.Generator;
import com.i3s.app.rdfminer.sparql.corese.CoreseEndpoint;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
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
            for (Rule rule : grammar.getRules()) {
                if (rule.get(0).toString().contains(sparql)) {
                    String body = rule.get(0).toString().replace(sparql, "");
//                    System.out.println("SELECT distinct ?" + rule.getLHS().getSymbolString() + " WHERE { " + body + " FILTER( strStarts(MD5(str(?" + rule.getLHS().getSymbolString() + ")), " + h + ") ) }");
                    generateProductions(rule.getLHS().getSymbolString(), getSparqlQuery(rule.getLHS().getSymbolString(), body, h));
                }
            }
        }
        ArrayList<Rule> copyRules = new ArrayList<>();
        for (Rule rule : grammar.getRules()) {
            copyRules.add((Rule) rule.clone());
        }
        for (Rule rule : copyRules) {
            for(Production prod: rule) {
                if (prod.toString().contains(sparql)) {
                    int idRule = grammar.getRules().indexOf(rule);
                    grammar.getRules().get(idRule).remove(prod);
                }
            }
        }
//        System.out.println(grammar.getRules());
    }

    @Override
    protected void generateProductions(String symbol, String sparql) throws URISyntaxException, IOException {
        Parameters parameters = Parameters.getInstance();
        Rule rule = grammar.findRule(symbol);
        if (rule == null) {
            rule = new Rule();
            rule.setLHS(new Symbol(symbol, Enums.SymbolType.NTSymbol));
            grammar.getRules().add(rule);
            logger.debug("Added a new (dynamical) rule for " + rule.getLHS());
        }

        this.setCachesPath(symbol, sparql);
        try {
            logger.info(this.getCachesPath());
            // Try to read the productions from a cache file named after the query:
            BufferedReader cache = new BufferedReader(new FileReader(this.getCachesPath()));
            while (true) {
                String s = cache.readLine();
                if (s == null)
                    break;

                Production prod = new Production();
                Symbol t = new Symbol(s, Enums.SymbolType.TSymbol);
                prod.add(t);
                rule.add(prod);
            }
            logger.info("File readed: " + this.getCachesPath() + ", " + rule.size() + " production(s) added !");
            cache.close();
        } catch (IOException ioe) {
            logger.info("Cache for " + symbol + " not found. Querying SPARQL endpoint");
            logger.info("Querying SPARQL endpoint for symbol <" + symbol + "> ...");
            CoreseEndpoint endpoint = new CoreseEndpoint(parameters.getNamedDataGraph(), parameters.getPrefixes());
            List<String> results = endpoint.select(symbol, sparql, false);
            if(results.size() > 0) {
                PrintStream cache = null;
                try {
                    cache = new PrintStream(this.getCachesPath());
                } catch (FileNotFoundException e) {
                    logger.warn("Could not create cache for symbol " + symbol + ".");
                }
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
                logger.info("Done! " + rule.size() + " productions added.");
                if (cache != null) cache.close();
            }
        }
    }

}
