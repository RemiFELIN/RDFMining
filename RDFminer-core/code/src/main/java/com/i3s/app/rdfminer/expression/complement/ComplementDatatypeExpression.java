/**
 * 
 */
package com.i3s.app.rdfminer.expression.complement;

import java.util.List;

import com.i3s.app.rdfminer.evolutionary.geva.Mapper.Symbol;
import com.i3s.app.rdfminer.expression.Expression;
import com.i3s.app.rdfminer.expression.ExpressionFactory;

/**
 * A datatype expression of the form DataComplementOf(...).
 * 
 * @author Andrea G. B. Tettamanzi
 *
 */
public class ComplementDatatypeExpression extends ComplementExpression {

	/**
	 * Creates a new complement datatype expression based on the provided
	 * functional-style syntax.
	 * <p>
	 * This expression will be the complement of the only subexpression.
	 * </p>
	 * 
	 * @param syntax an expression in OWL 2 functional-style syntax.
	 */
	public ComplementDatatypeExpression(List<Symbol> syntax) {
		super();
		rootSymbol = "DataComplementOf";
		subExpressions.add(ExpressionFactory.createDatatype(syntax));
		graphPattern = createGraphPattern("?x", "?y");
	}

	/**
	 * Creates a new complement datatype expression from the datatype expression of
	 * its complement.
	 * 
	 * @param expr an existing datatype expression
	 */
	public ComplementDatatypeExpression(Expression expr) {
		super();
		rootSymbol = "DataComplementOf";
		subExpressions.add(expr);
		graphPattern = createGraphPattern("?x", "?y");
	}

}
