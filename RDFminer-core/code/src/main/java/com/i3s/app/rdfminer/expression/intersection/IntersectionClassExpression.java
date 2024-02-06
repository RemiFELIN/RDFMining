/**
 * 
 */
package com.i3s.app.rdfminer.expression.intersection;

import java.util.Iterator;
import java.util.List;

import com.i3s.app.rdfminer.evolutionary.geva.Mapper.Symbol;
import com.i3s.app.rdfminer.expression.ExpressionFactory;

/**
 * A class expression of the form ObjectIntersectionOf(...).
 * 
 * @author Andrea G. B. Tettamanzi
 *
 */
public class IntersectionClassExpression extends IntersectionExpression {

	/**
	 * Creates a new intersection class expression based on the provided
	 * functional-style syntax.
	 * <p>
	 * This expression will be the intersection of its subexpressions.
	 * </p>
	 * 
	 * @param syntax an expression in OWL 2 functional-style syntax.
	 */
	public IntersectionClassExpression(List<List<Symbol>> syntax) {
		super();
		rootSymbol = "ObjectIntersectionOf";
		Iterator<List<Symbol>> i = syntax.iterator();
		while (i.hasNext())
			subExpressions.add(ExpressionFactory.createClass(i.next()));
		graphPattern = createGraphPattern("?x", "?y");
	}

}
