/**
 * 
 */
package rdfminer;

import java.util.List;

import Mapper.Symbol;

/**
 * An OWL 2 property expression.
 * 
 * @author Andrea G. B. Tettamanzi
 *
 */
public class AtomicPropertyExpression extends Expression
{

	/**
	 * Creates a new atomic property expression based on the provided functional-style syntax.
	 * <p>The expression must be an atomic expression, consisting solely
	 * of the IRI of an OWL 2 property: in other words, it must represent
	 * a simple relation; property expressions from complex expressions
	 * must be created using the constructor of the appropriate subclass.</p>
	 * 
	 * @param syntax an expression in OWL 2 functional-style syntax. 
	 */
	public AtomicPropertyExpression(List<Symbol> syntax)
	{
		super();
		rootSymbol = syntax.get(0).getSymbolString();
		graphPattern = createGraphPattern("?x", "?y");
	}

	/**
	 * Instantiates the graph pattern for this atomic property expression.
	 */
	@Override
	protected String createGraphPattern(String subject, String object)
	{
		return subject + " " + rootSymbol + " " + object + " .";
	}

	/**
	 * Check whether the given RDF node pair is an instance of the property represented by this expression.
	 * 
	 * @see rdfminer.Expression#contains(org.apache.jena.rdf.model.RDFNode)
	 * @param pair an RDF node pair
	 */
	@Override
	public boolean contains(RDFNodePair pair)
	{
		// This is a faster alternative to the method inherited from the superclass:
		return extension().contains(pair);
	}

}
