/**
 * Created on November 13, 2013.
 */
package com.i3s.app.rdfminer.grammar;

import com.i3s.app.rdfminer.evolutionary.geva.Exceptions.MalformedGrammarException;
import com.i3s.app.rdfminer.evolutionary.geva.Mapper.GEGrammar;
import com.i3s.app.rdfminer.evolutionary.geva.Mapper.Production;
import com.i3s.app.rdfminer.evolutionary.geva.Mapper.Rule;
import com.i3s.app.rdfminer.evolutionary.geva.Mapper.Symbol;
import com.i3s.app.rdfminer.evolutionary.geva.Util.Enums;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.Iterator;
import java.util.Properties;

/**
 * A Genetic Evolution grammar of a Description Logic language, adapted to the
 * specificities of RDF mining.
 * <p>
 * The first and foremost adaptation is the capability of parsing an extended
 * BNF grammar specification of OWL 2 or
 * <a href="http://www.w3.org/TR/owl2-profiles">one of its fragments</a>,
 * written in the notation used by the <a href="http://www.w3.org">W3C</a>
 * which, albeit being quite easy to understand, does not correspond neither to
 * pure BNF, nor to ABNF, nor to EBNF, the standard W3C grammar notation, nor to
 * any standardized grammar notation that I am aware of.
 * </p>
 * <p>
 * Some conventions stipulated by the GEVA Mapper package are the following:
 * </p>
 * <ol>
 * <li>a <code>Rule</code> is a group of <code>Production</code>s sharing the
 * same non-terminal symbol as their left-hand side;</li>
 * <li>a <code>Production</code> is a production rule, consisting of a list of
 * symbols.</li>
 * </ol>
 *
 * @author Andrea G. B. Tettamanzi
 */
public class DLGEGrammar extends GEGrammar {

    static Logger logger = Logger.getLogger(DLGEGrammar.class.getName());

    /**
     * Creates a new, empty instance of DLGEGrammar.
     */
    public DLGEGrammar() {
        super();
    }

    /**
     * Create a new description-logic grammar from a text file.
     * <p>
     * The format of the text file must comply with the notation used by the
     * <a href="http://w3c.org">W3C</a> in the technical recommendations related to
     * the <a href="http://www.w3.org/TR/owl2-overview/">OWL 2 Web Ontology
     * Language</a>.
     * </p>
     *
     * @param file the name of a text file to read the grammar from.
     */
    public DLGEGrammar(String file) {
        super();
        try {
            this.readW3CGrammarFile(file);
        } catch (MalformedGrammarException e) {
            logger.error("Syntax error while parsing a grammar file");
        }
    }

    /**
     * New instance
     *
     * @param p properties
     */
    public DLGEGrammar(Properties p) {
        super(p);
    }

    /**
     * Copy constructor. Does not copy the genotype and phenotype
     *
     * @param copy grammar to copy
     */
    public DLGEGrammar(GEGrammar copy) {
        super(copy);
    }

    /**
     * Read and parse a W3C description-logic grammar file to construct an internal
     * representation of the grammar.
     * <p>
     * The format of the text file must comply with the notation used by the
     * <a href="http://w3c.org">W3C</a> in the technical recommendations related to
     * the <a href="http://www.w3.org/TR/owl2-overview/">OWL 2 Web Ontology
     * Language</a>.
     * </p>
     *
     * @param bnfGrammar the BNF grammar provided.
     */
    public void readW3CGrammarFile(String bnfGrammar) throws MalformedGrammarException {
        try {
            logger.info("Reading grammar ...");
//            File file = new File(fileName);
//            InputStream is = new FileInputStream(file);
//            logger.assertLog(is != null, "Cannot open file: " + fileName);
//            InputStreamReader isr = new InputStreamReader(is);
//            Reader r = new BufferedReader(isr, 1024);
            Reader r = new StringReader(bnfGrammar);
            // Initialize the BNF grammar tokenizer:
            BNFGrammarTokenizer source = new BNFGrammarTokenizer(r);

            Symbol lhs = null; // left-hand symbol of a rule
            Symbol t, nt = null; // variables to hold a terminal and a non-terminal symbol
            Rule rule = null; // current rule;
            Production prod = null; // current production;
            Production list = null; // a list of symbols surrounded by { } or [ ]

            // The empty string: not sure this is necessary, but it looks like a good idea
            final Symbol EMPTY = new Symbol("", Enums.SymbolType.TSymbol);

            // States of parser
            final int START = 0; // looking for the start symbol
            final int LHS = 1; // parsed the NT on the left-hand side of a rule
            final int RHS = 2; // parsing the right-hand side of a rule
            final int NEWLINE = 3; // on a new line while parsing a rule
            final int NT_ON_NEWLINE = 4; // found a NT on a new line: possibly a new rule?
            final int KLEENE = 5; // found the {-symbol of a zero-or-more expression
            final int OPTION = 6; // found the [-symbol of a zero-or-one expression

            int state = START; // Initialize the current state of the parser

            while (source.nextToken() != BNFGrammarTokenizer.TT_EOF) {
                switch (source.ttype) {
                    case BNFGrammarTokenizer.TT_EOL: // a line terminator
                        switch (state) {
                            case LHS:
                                throw new MalformedGrammarException("Unexpected end of line at line " + source.lineno());
                            case NT_ON_NEWLINE:
                                // add the pending nt to the current production...
                                prod.add(nt);
                            case RHS:
                                state = NEWLINE;
                                break;
                            /*
                             * case START: case NEWLINE: case KLEENE: case OPTION: break; // do nothing
                             */
                        }
                        break;
                    case BNFGrammarTokenizer.TT_TERMINAL: // a terminal symbol
                        switch (state) {
                            case START:
                            case LHS:
                                throw new MalformedGrammarException("Unexpected terminal symbol: " + source);
                            case KLEENE:
                            case OPTION:
                                t = new Symbol(source.sval, Enums.SymbolType.TSymbol);
                                list.add(t);
                                break;
                            case RHS:
                            case NEWLINE:
                            case NT_ON_NEWLINE:
                                t = new Symbol(source.sval, Enums.SymbolType.TSymbol);
                                prod.add(t);
                                state = RHS;
                                break;
                        }
                        break;
                    case BNFGrammarTokenizer.TT_WORD:
                        if (source.has(":=")) {
                            // production symbol
                            switch (state) {
                                case START:
                                case RHS:
                                case NEWLINE:
                                case KLEENE:
                                case OPTION:
                                    throw new MalformedGrammarException("Unexpected production symbol: " + source);
                                case NT_ON_NEWLINE:
                                    // commit the current production (if any):
                                    rule.add(prod);
                                    lhs = nt;
                                case LHS:
                                    // Start a new rule with lhs as its left-hand side:
                                    rule = findRule(lhs);
                                    if (rule == null) {
                                        rule = new Rule();
                                        rule.setLHS(lhs);
                                        getRules().add(rule);
                                        logger.debug("Added a new rule for " + rule.getLHS());
                                    }
                                    prod = new Production();
                                    state = RHS;
                                    break;
                            }
                        } else {
                            // a non-terminal symbol
                            switch (state) {
                                case START:
                                    lhs = new Symbol(source.sval, Enums.SymbolType.NTSymbol);
                                    setStartSymbol(lhs);
                                    logger.debug("Start symbol = " + lhs);
                                    state = LHS;
                                    break;
                                case LHS:
                                    throw new MalformedGrammarException("Unexpected non-terminal symbol: " + source);
                                case NEWLINE:
                                    nt = new Symbol(source.sval, Enums.SymbolType.NTSymbol);
                                    state = NT_ON_NEWLINE;
                                    break;
                                case NT_ON_NEWLINE:
                                    // add the pending nt to the current production...
                                    prod.add(nt);
                                    state = RHS;
                                case RHS:
                                    // add this non-terminal symbol to the current rule.
                                    nt = new Symbol(source.sval, Enums.SymbolType.NTSymbol);
                                    prod.add(nt);
                                    break;
                                case KLEENE:
                                case OPTION:
                                    nt = new Symbol(source.sval, Enums.SymbolType.NTSymbol);
                                    list.add(nt);
                                    break;
                            }
                        }
                        break;
                    case '{':
                        switch (state) {
                            case START:
                            case LHS:
                            case KLEENE:
                            case OPTION:
                                throw new MalformedGrammarException(
                                        "Unexpected beginning of zero-or-more construct: " + source);
                            case RHS:
                            case NEWLINE:
                            case NT_ON_NEWLINE:
                                list = new Production();
                                state = KLEENE;
                                break;
                        }
                        break;
                    case '}':
                        if (state != KLEENE)
                            throw new MalformedGrammarException("Unmatched " + source);
                        // Add to the current production as a Kleene closure:
                        // 1. add a new non-terminal "X*" to the current production
                        Symbol kleene = new Symbol(list.toString() + "*", Enums.SymbolType.NTSymbol);
                        prod.add(kleene);
                        // 2. if it does not already exist, create a rule of the form:
                        // X* := | X X*
                        Rule kr = findRule(kleene);
                        if (kr == null) {
                            logger.debug("Creating a rule for " + kleene);
                            kr = new Rule();
                            kr.setLHS(kleene);
                            getRules().add(kr);
                            Production kp = new Production();
                            kp.add(EMPTY);
                            kr.add(kp); // X* := ''
                            kp = new Production();
                            kp.addAll(list);
                            kp.add(kleene);
                            kr.add(kp); // X* := X X*
                        }
                        state = RHS;
                        break;
                    case '[':
                        switch (state) {
                            case START:
                            case LHS:
                            case KLEENE:
                            case OPTION:
                                throw new MalformedGrammarException("Unexpected beginning of zero-or-one construct: " + source);
                            case RHS:
                            case NEWLINE:
                            case NT_ON_NEWLINE:
                                list = new Production();
                                state = OPTION;
                                break;
                        }
                        break;
                    case ']':
                        if (state != OPTION)
                            throw new MalformedGrammarException("Unmatched " + source);
                        // Add to the current production as an optional symbol:
                        // 1. add the non-terminal "X?" to the current production
                        Symbol opt = new Symbol(list.toString() + "?", Enums.SymbolType.NTSymbol);
                        prod.add(opt);
                        // 2. if it does not already exist, create a rule of the form:
                        // X? := | X
                        Rule or = findRule(opt);
                        if (or == null) {
                            logger.debug("Creating a rule for " + opt);
                            or = new Rule();
                            or.setLHS(opt);
                            getRules().add(or);
                            Production op = new Production();
                            op.add(EMPTY);
                            or.add(op); // X? := ''
                            op = new Production();
                            op.addAll(list);
                            or.add(op); // X? := X
                        }
                        state = RHS;
                        break;
                    case '|':
                        switch (state) {
                            case START:
                            case LHS:
                            case KLEENE:
                            case OPTION:
                                throw new MalformedGrammarException("Unexpected alternative: " + source);
                            case NT_ON_NEWLINE:
                                prod.add(nt);
                            case RHS:
                            case NEWLINE:
                                // Record the alternative:
                                rule.add(prod);
                                prod = new Production();
                                state = RHS;
                                break;
                        }
                        break;
                    default: // any other character...
                        throw new MalformedGrammarException("Syntax error: " + source);
                }
            }
            // Add the last production, which is still pending, to the current rule:
            rule.add(prod);
            r.close();
        } catch (IOException e) {
            logger.error("IOException when looking for BNF grammar: " + e.getMessage());
        }

    }

    public String toString() {
        StringBuilder s = new StringBuilder();
        for (Rule rule : getRules()) {
            s.append("<").append(rule.getLHS()).append("> ::= \n");
            Iterator<Production> j = rule.iterator();
            while (j.hasNext()) {
                Production prod = j.next();

                s.append("\t");
                for (Symbol x : prod) {
                    if (x.getType() == Enums.SymbolType.NTSymbol)
                        s.append("<").append(x).append(">");
                    else
                        s.append(x.getSymbolString());
                }
                s.append(j.hasNext() ? " |\n" : "\n");
            }
        }
        return s.toString();
    }
}
