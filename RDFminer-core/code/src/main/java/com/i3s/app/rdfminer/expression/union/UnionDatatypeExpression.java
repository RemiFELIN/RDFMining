/**
 * 
 */
package com.i3s.app.rdfminer.expression.union;

import java.util.Iterator;
import java.util.List;

import com.i3s.app.rdfminer.evolutionary.geva.Mapper.Symbol;
import com.i3s.app.rdfminer.expression.ExpressionFactory;

/**
 * A class expression of the form DataUnionOf(...).
 * 
 * @author Andrea G. B. Tettamanzi
 *
 */
public class UnionDatatypeExpression extends UnionExpression {

	/**
	 * Creates a new union datatype expression based on the provided
	 * functional-style syntax.
	 * <p>
	 * This expression will be the union of its subexpressions.
	 * </p>
	 * 
	 * @param syntax an expression in OWL 2 functional-style syntax.
	 */
	public UnionDatatypeExpression(List<List<Symbol>> syntax) {
		super();
		rootSymbol = "DataUnionOf";
		for (List<Symbol> symbols : syntax)
			subExpressions.add(ExpressionFactory.createDatatype(symbols));
		graphPattern = createGraphPattern("?x", "?y");
	}

}
