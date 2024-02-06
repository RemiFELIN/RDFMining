/**
 * 
 */
package com.i3s.app.rdfminer.expression.extensional;

import java.util.Iterator;

import com.i3s.app.rdfminer.sparql.virtuoso.VirtuosoEndpoint;
import com.i3s.app.rdfminer.expression.Expression;
import com.i3s.app.rdfminer.sparql.RDFNodePair;

/**
 * An extensional expression of the form ObjectOneOf(...) or DataOneOf(...).
 * 
 * @author Andrea G. B. Tettamanzi
 *
 */
public abstract class ExtensionalExpression extends Expression {

	/**
	 * Creates a new, empty extensional expression.
	 */
	public ExtensionalExpression() {
		super();
	}

	/**
	 * Returns the <code>FILTER</code> expression of a graph pattern corresponding
	 * to this extensional expression.
	 * 
	 * @param x          the variable name that will replace the <tt>?x</tt>
	 *                   variable of the filter.
	 * @param complement true if the negated filter is to be returned.
	 * @return a <code>FILTER</code> expression.
	 */
	public String getFilter(String x, boolean complement) {
		String filter = "FILTER ( ";

		if (extension.size() > 1)
			filter += x + (complement ? " NOT IN ( " : " IN ( ");
		else
			filter += x + (complement ? " != " : " = ");

		Iterator<RDFNodePair> i = extension.iterator();
		while (i.hasNext()) {
			filter += sparqlEncode(i.next().x);
			if (i.hasNext())
				filter += ", ";
		}
		if (extension.size() > 1)
			filter += " )";

		return filter + " )";
	}

	/**
	 * Check whether the given pair of RDF nodes is part of the concept represented
	 * by this extensional expression.
	 * <p>
	 * This method overrides the one inherited from
	 * {@link com.i3s.app.rdfminer.expression.Expression} to provide a more
	 * efficient implementation: since the extension is given explicitly and is part
	 * of the expression, it is trivial to check the membership of a node pair
	 * without having to query a SPARQL endpoint.
	 * </p>
	 * 
	 * @see com.i3s.app.rdfminer.expression.Expression#contains(com.i3s.app.rdfminer.sparql.RDFNodePair)
	 */
	@Override
	public boolean contains(RDFNodePair pair, VirtuosoEndpoint endpoint) {
		return extension.contains(pair);
	}

}
