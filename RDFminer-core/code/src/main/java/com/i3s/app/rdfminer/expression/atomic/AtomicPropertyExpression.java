/**
 * 
 */
package com.i3s.app.rdfminer.expression.atomic;

import java.util.List;

import com.i3s.app.rdfminer.evolutionary.geva.Mapper.Symbol;
import com.i3s.app.rdfminer.sparql.virtuoso.VirtuosoEndpoint;
import com.i3s.app.rdfminer.expression.Expression;
import com.i3s.app.rdfminer.sparql.RDFNodePair;

/**
 * An OWL 2 property expression.
 * 
 * @author Andrea G. B. Tettamanzi
 *
 */
public class AtomicPropertyExpression extends Expression {

	/**
	 * Creates a new atomic property expression based on the provided
	 * functional-style syntax.
	 * <p>
	 * The expression must be an atomic expression, consisting solely of the IRI of
	 * an OWL 2 property: in other words, it must represent a simple relation;
	 * property expressions from complex expressions must be created using the
	 * constructor of the appropriate subclass.
	 * </p>
	 * 
	 * @param syntax an expression in OWL 2 functional-style syntax.
	 */
	public AtomicPropertyExpression(List<Symbol> syntax) {
		super();
		rootSymbol = syntax.get(0).getSymbolString();
		graphPattern = createGraphPattern("?x", "?y");
		// System.out.println("AtomicPropertyExpression: graphPattern = \n" + graphPattern);
	}

	/**
	 * Instantiates the graph pattern for this atomic property expression.
	 */
	@Override
	public String createGraphPattern(String subject, String object) {
		return subject + " " + rootSymbol + " " + object + " .";
	}

	/**
	 * Check whether the given RDF node pair is an instance of the property
	 * represented by this expression.
	 * @param pair an RDF node pair
	 */
	@Override
	public boolean contains(RDFNodePair pair, VirtuosoEndpoint endpoint) {
		// This is a faster alternative to the method inherited from the superclass:
		return extension(endpoint).contains(pair);
	}

}
