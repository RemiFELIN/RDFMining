/**
 * 
 */
package com.i3s.app.rdfminer.grammar;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;

/**
 * A specialized tokenizer for parsing W3C BNF grammars.
 * 
 * @author Andrea G. B. Tettamanzi
 *
 */
public class BNFGrammarTokenizer extends StreamTokenizer {
	static final int TT_TERMINAL = '\'';

	/**
	 * Creates a tokenizer that parses the given character stream formatted
	 * according to the extended BNF notation used by the W3C technical
	 * recommendations relevant to OWL 2.
	 *
	 * @param r a character stream, represented by a reader.
	 */
	public BNFGrammarTokenizer(Reader r) {
		super(r);
		resetSyntax();
		commentChar('#');
		quoteChar('\'');
		wordChars('A', 'Z');
		wordChars('a', 'z');
		wordChars(':', ':');
		wordChars('=', '=');
		wordChars('-', '-');
		whitespaceChars(0, ' ');
		eolIsSignificant(true);
	}

	/**
	 * Throws an I/O exception to inform the user that a syntax error has been
	 * encountered. The message specifies the line on which the problem was
	 * encountered.
	 *
	 * @param msg a specific message explaining the nature of the error.
	 */
	private void error(String msg) throws IOException {
		throw new IOException("Syntax error on line " + lineno() + " of model file: " + msg + ". Token type = " + ttype
				+ " sval = " + sval + " nval = " + nval);
	}

	/**
	 * Checks if the current token is a word identical to the one provided as
	 * argument. If that is not the case, a syntax error is thrown.
	 *
	 * @param s the required word.
	 */
	public void require(String s) throws IOException {
		if (ttype == TT_WORD)
			if (has(s))
				return;
		error(s + " expected");
	}

	/**
	 * Checks if the current token is a word. If that is not the case, a syntax
	 * error is thrown.
	 *
	 * @param what an explanation of what the required word should represent.
	 */
	public String requireWord(String what) throws IOException {
		if (ttype != TT_WORD)
			error(what + " expected");
		return sval;
	}

	/**
	 * Checks if the current token is a word. If that is not the case, a syntax
	 * error is thrown.
	 */
	public String requireWord() throws IOException {
		return requireWord("word");
	}

	/**
	 * Checks if the current token is the specified type. If that is not the case, a
	 * syntax error is thrown.
	 */
	public void require(char c) throws IOException {
		if (ttype != c)
			error("'" + c + "' expected");
	}

	/**
	 * Checks if the current token is a number. If that is not the case, a syntax
	 * error is thrown.
	 *
	 * @param what an explanation of what the required number should represent.
	 */
	public double requireNumber(String what) throws IOException {
		if (ttype != TT_NUMBER)
			error(what + " expected");
		return nval;
	}

	/**
	 * Checks if the current token is a number. If that is not the case, a syntax
	 * error is thrown.
	 */
	public double requireNumber() throws IOException {
		return requireNumber("number");
	}

	/**
	 * Checks if the current token is identical to the given string. Useful to check
	 * for the presence of a keyword.
	 *
	 * @param s a word to be compared with the current token.
	 */
	public boolean has(String s) {
		if (ttype != TT_WORD)
			return false;
		return sval.compareTo(s) == 0;
	}
}
