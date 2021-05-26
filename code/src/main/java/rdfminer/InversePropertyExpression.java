/**
 * 
 */
package rdfminer;

import java.util.List;

import Mapper.Symbol;

/**
 * An OWL 2 property expression of the form ObjectInverseOf(...).
 * 
 * @author Andrea G. B. Tettamanzi
 *
 */
public class InversePropertyExpression extends Expression
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
	public InversePropertyExpression(List<Symbol> syntax)
	{
		super();
		rootSymbol = "ObjectInverseOf";
				
		subExpressions.add(ExpressionFactory.createProperty(syntax));
		graphPattern = createGraphPattern("?x", "?y");
	}

	/**
	 * Instantiates the graph pattern for this inverse property expression.
	 */
	@Override
	protected String createGraphPattern(String subject, String object)
	{
		return subExpressions.get(0).createGraphPattern(object, subject);
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
		return subExpressions.get(0).contains(new RDFNodePair(pair.y, pair.x));
	}

}
