/**
 * 
 */
package rdfminer;

import java.util.Iterator;
import java.util.List;

import Mapper.Symbol;

/**
 * A class expression of the form ObjectUnionOf(...).
 * 
 * @author Andrea G. B. Tettamanzi
 *
 */
public class UnionClassExpression extends UnionExpression
{

	/**
	 * Creates a new union class expression based on the provided functional-style syntax.
	 * <p>This expression will be the union of its subexpressions.</p>
	 * 
	 * @param syntax an expression in OWL 2 functional-style syntax. 
	 */
	public UnionClassExpression(List<List<Symbol>> syntax)
	{
		super();
		rootSymbol = "ObjectUnionOf";
		Iterator<List<Symbol>> i = syntax.iterator();
		while(i.hasNext())
			subExpressions.add(ExpressionFactory.createClass(i.next()));
		graphPattern = createGraphPattern("?x", "?y");
	}

}
