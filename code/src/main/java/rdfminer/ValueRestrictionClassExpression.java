/**
 * 
 */
package rdfminer;

import java.util.List;

import Mapper.Symbol;

/**
 * A class expression of the form ObjectAllValuesFrom(...).
 * 
 * @author Andrea G. B. Tettamanzi
 *
 */
public class ValueRestrictionClassExpression extends Expression
{

	/**
	 * Creates a new value restriction class expression based on the
	 * provided functional-style syntax.
	 * 
	 * @param syntax an expression in OWL 2 functional-style syntax. 
	 */
	public ValueRestrictionClassExpression(List<Symbol> role, List<Symbol> concept)
	{
		super();
		rootSymbol = "ObjectAllValuesFrom";
		subExpressions.add(ExpressionFactory.createProperty(role));
		subExpressions.add(ExpressionFactory.createClass(concept));
		graphPattern = createGraphPattern("?x", "?y");
	}

	/**
	 * Instantiates the graph pattern for this value restriction class expression.
	 * 
	 * @param subject the expression replacing the <tt>?x</tt> SPARQL variable
	 * @param object the expression replacing the <tt>?y</tt> SPARQL variable
	 * @return the SPARQL graph pattern for this expression
	 */
	@Override
	protected String createGraphPattern(String subject, String object)
	{
		String z0 = getFreshVariableName();
		String z1 = getFreshVariableName();
		String z2 = getFreshVariableName();
		
		if(subExpressions.get(1) instanceof ExtensionalClassExpression)
			// Handle an extensional concept for the filler in an optimized way:
			return "{ " + subExpressions.get(0).createGraphPattern(subject, z0) + "\n" +
				"FILTER NOT EXISTS {\n" +
				subExpressions.get(0).createGraphPattern(subject, z1) + "\n" +
				((ExtensionalClassExpression) (subExpressions.get(1))).getFilter(z1, true) +
				" } } .\n";
		else
			return "{ " + subExpressions.get(0).createGraphPattern(subject, z0) + "\n" +
				"FILTER NOT EXISTS {\n" +
				subExpressions.get(0).createGraphPattern(subject, z1) + "\n" +
				"  FILTER NOT EXISTS {\n" +
				subExpressions.get(1).createGraphPattern(z1, z2) + "\n " +
				" } } } .\n";
	}

}
