/**
 * 
 */
package com.i3s.app.rdfminer.expression.intersection;

import java.util.Iterator;

import com.i3s.app.rdfminer.expression.Expression;

/**
 * A class expression of the form ObjectIntersectionOf(...) or
 * DataIntersectionOf(...).
 * 
 * @author Andrea G. B. Tettamanzi
 *
 */
public class IntersectionExpression extends Expression {

	/**
	 * Creates a new, empty intersection expression.
	 */
	public IntersectionExpression() {
		super();
	}

	/**
	 * Instantiates the graph pattern for this intersection expression.
	 * 
	 * @param subject the expression replacing the <tt>?x</tt> SPARQL variable
	 * @param object  ignored
	 * @return the SPARQL graph pattern for this expression
	 */
	@Override
	public String createGraphPattern(String subject, String object) {
		Iterator<Expression> i = subExpressions.iterator();
		String pattern = "";
		while (i.hasNext())
			pattern += i.next().createGraphPattern(subject, object) + "\n";
		return pattern;
	}

}
