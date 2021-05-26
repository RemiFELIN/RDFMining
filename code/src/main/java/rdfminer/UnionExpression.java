/**
 * 
 */
package rdfminer;

import java.util.Iterator;

/**
 * A class expression of the form ObjectUnionOf(...) or DataUnionOf(...).
 * 
 * @author Andrea G. B. Tettamanzi
 *
 */
public class UnionExpression extends Expression
{

	/**
	 * Creates a new, empty union expression.
	 * 
	 * @param syntax an expression in OWL 2 functional-style syntax. 
	 */
	public UnionExpression()
	{
		super();
	}

	/**
	 * Instantiates the graph pattern for this union expression.
	 * 
	 * @param subject the expression replacing the <tt>?x</tt> SPARQL variable
	 * @param object ignored
	 * @return the SPARQL graph pattern for this expression
	 */
	@Override
	protected String createGraphPattern(String subject, String object)
	{
		Iterator<Expression> i = subExpressions.iterator();
		String pattern = "";
		while(i.hasNext())
		{
			pattern += "{ " + i.next().createGraphPattern(subject, object) + " } ";
			if(i.hasNext())
				pattern += "UNION\n";
		}
		return pattern;
	}

}
