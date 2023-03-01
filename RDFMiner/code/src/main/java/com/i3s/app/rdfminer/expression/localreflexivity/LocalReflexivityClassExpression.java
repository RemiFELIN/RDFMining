/**
 * 
 */
package com.i3s.app.rdfminer.expression.localreflexivity;

import java.util.List;

import com.i3s.app.rdfminer.evolutionary.geva.Mapper.Symbol;
import com.i3s.app.rdfminer.expression.Expression;
import com.i3s.app.rdfminer.expression.ExpressionFactory;

/**
 * A local reflexivity class expression of the form ObjectHasSelf(...).
 * 
 * @author Andrea G. B. Tettamanzi
 *
 */
public class LocalReflexivityClassExpression extends Expression {

	/**
	 * Creates a new local reflexivity class expression based on the provided
	 * functional-style syntax.
	 *
	 */
	public LocalReflexivityClassExpression(List<Symbol> role) {
		super();
		subExpressions.add(ExpressionFactory.createProperty(role));
		rootSymbol = "ObjectHasSelf";
		graphPattern = createGraphPattern("?x", "?y");
	}

	/**
	 * Instantiates the graph pattern for this local reflexivity class expression.
	 * 
	 * @param subject the expression replacing the <tt>?x</tt> SPARQL variable
	 * @param object  ignored
	 * @return the SPARQL graph pattern for this expression
	 */
	@Override
	public String createGraphPattern(String subject, String object) {
		return subExpressions.get(0).createGraphPattern(subject, subject);
	}

}
