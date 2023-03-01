/**
 * 
 */
package com.i3s.app.rdfminer.expression.union;

import java.util.Iterator;
import java.util.List;

import com.i3s.app.rdfminer.evolutionary.geva.Mapper.Symbol;
import com.i3s.app.rdfminer.expression.ExpressionFactory;

/**
 * A class expression of the form ObjectUnionOf(...).
 * 
 * @author Andrea G. B. Tettamanzi
 *
 */
public class UnionClassExpression extends UnionExpression {

	/**
	 * Creates a new union class expression based on the provided functional-style
	 * syntax.
	 * <p>
	 * This expression will be the union of its subexpressions.
	 * </p>
	 * 
	 * @param syntax an expression in OWL 2 functional-style syntax.
	 */
	public UnionClassExpression(List<List<Symbol>> syntax) {
		super();
		rootSymbol = "ObjectUnionOf";
		for (List<Symbol> symbols : syntax)
			subExpressions.add(ExpressionFactory.createClass(symbols));
		graphPattern = createGraphPattern("?x", "?y");
	}

}
