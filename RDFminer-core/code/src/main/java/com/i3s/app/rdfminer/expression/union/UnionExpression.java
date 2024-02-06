/**
 * 
 */
package com.i3s.app.rdfminer.expression.union;

import java.util.Iterator;

import com.i3s.app.rdfminer.expression.Expression;

/**
 * A class expression of the form ObjectUnionOf(...) or DataUnionOf(...).
 * 
 * @author Andrea G. B. Tettamanzi
 *
 */
public class UnionExpression extends Expression {

	/**
	 * Creates a new, empty union expression.
	 */
	public UnionExpression() {
		super();
	}

	/**
	 * Instantiates the graph pattern for this union expression.
	 * 
	 * @param subject the expression replacing the <tt>?x</tt> SPARQL variable
	 * @param object  ignored
	 * @return the SPARQL graph pattern for this expression
	 */
	@Override
	public String createGraphPattern(String subject, String object) {
		Iterator<Expression> i = subExpressions.iterator();
		StringBuilder pattern = new StringBuilder();
		while (i.hasNext()) {
			pattern.append("{ ").append(i.next().createGraphPattern(subject, object)).append(" } ");
			if (i.hasNext())
				pattern.append("UNION\n");
		}
		return pattern.toString();
	}

}
