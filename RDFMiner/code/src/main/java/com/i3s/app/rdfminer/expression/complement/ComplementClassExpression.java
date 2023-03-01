/**
 * 
 */
package com.i3s.app.rdfminer.expression.complement;

import java.util.List;

import com.i3s.app.rdfminer.evolutionary.geva.Mapper.Symbol;
import com.i3s.app.rdfminer.expression.Expression;
import com.i3s.app.rdfminer.expression.ExpressionFactory;

/**
 * A class expression of the form ObjectComplementOf(...).
 * 
 * @author Andrea G. B. Tettamanzi
 *
 */
public class ComplementClassExpression extends ComplementExpression {

	/**
	 * Creates a new complement class expression based on the provided
	 * functional-style syntax.
	 * <p>
	 * This expression will be the complement of the only subexpression.
	 * </p>
	 * 
	 * @param syntax an expression in OWL 2 functional-style syntax.
	 */
	public ComplementClassExpression(List<Symbol> syntax) {
		super();
		rootSymbol = "ObjectComplementOf";
		subExpressions.add(ExpressionFactory.createClass(syntax));
		graphPattern = createGraphPattern("?x", "?y");
	}

	/**
	 * Creates a new complement class expression from the class expression of its
	 * complement.
	 * 
	 * @param expr an existing class expression
	 */
	public ComplementClassExpression(Expression expr) {
		super();
		rootSymbol = "ObjectComplementOf";
		subExpressions.add(expr);
		graphPattern = createGraphPattern("?x", "?y");
	}

}
