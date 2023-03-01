/**
 * 
 */
package com.i3s.app.rdfminer.grammar;

import com.i3s.app.rdfminer.evolutionary.geva.Mapper.Symbol;
import com.i3s.app.rdfminer.evolutionary.geva.Util.Enums;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrea G. B. Tettamanzi
 *
 *         The ancestor of all factory classes.
 *         <p>
 *         This class just contains some utility methods that are used to parse
 *         lists of symbols.
 *         </p>
 */
public class DLFactory {
	/**
	 * Parse a term in a list of symbols starting at the given position and put it
	 * in the given list.
	 * <p>
	 * Consecutive symbols different from "(", " ", and ")" are concatenated into a
	 * single symbol.
	 * </p>
	 * 
	 * @param syntax     the list of symbols from which the term has to be extracted
	 * @param i          the position in the <code>syntax</code> list where to start
	 * @param expression a list of symbols to receive the extracted expression
	 * @return the position of the next symbol to read.
	 */
	protected static int parseTerm(List<Symbol> syntax, int i, List<Symbol> expression) {
		int level = 0;
		String token = ""; // used for concatenating consecutive symbols

		while (i < syntax.size() && !(level == 0 && (syntax.get(i).equals(" ") || syntax.get(i).equals(")")))) {
			Symbol next = syntax.get(i);
			if (next.equals("(")) {
				if (!token.isEmpty()) {
					expression.add(new Symbol(token, Enums.SymbolType.TSymbol));
					token = "";
				}
				expression.add(next);
				level++;
			} else if (next.equals(")")) {
				if (!token.isEmpty()) {
					expression.add(new Symbol(token, Enums.SymbolType.TSymbol));
					token = "";
				}
				expression.add(next);
				--level;
			} else if (next.equals(" ")) {
				if (!token.isEmpty()) {
					expression.add(new Symbol(token, Enums.SymbolType.TSymbol));
					token = "";
				}
				expression.add(next);
			} else {
				token += next.getSymbolString();
			}
			i++;
		}
		if (!token.isEmpty())
			expression.add(new Symbol(token, Enums.SymbolType.TSymbol));
		return i;
	}

	protected static void require(boolean condition) {
		if (!condition)
			throw new RuntimeException("Syntax Error in an axiom!");
	}

	public static List<List<Symbol>> parseArguments(List<Symbol> syntax) {
		int i = 1;
		require(syntax.get(i++).equals("("));
		List<List<Symbol>> argumentList = new ArrayList<List<Symbol>>();
		while (!syntax.get(i).equals(")")) {
			while (syntax.get(i).equals(" "))
				i++;
			List<Symbol> argument = new ArrayList<Symbol>();
			i = parseTerm(syntax, i, argument);
			argumentList.add(argument);
			while (syntax.get(i).equals(" "))
				i++;
		}
		return argumentList;
	}

}
