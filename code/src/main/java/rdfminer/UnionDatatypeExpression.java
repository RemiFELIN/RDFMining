/**
 * 
 */
package rdfminer;

import java.util.Iterator;
import java.util.List;

import Mapper.Symbol;

/**
 * A class expression of the form DataUnionOf(...).
 * 
 * @author Andrea G. B. Tettamanzi
 *
 */
public class UnionDatatypeExpression extends UnionExpression
{

	/**
	 * Creates a new union datatype expression based on the provided functional-style syntax.
	 * <p>This expression will be the union of its subexpressions.</p>
	 * 
	 * @param syntax an expression in OWL 2 functional-style syntax. 
	 */
	public UnionDatatypeExpression(List<List<Symbol>> syntax)
	{
		super();
		rootSymbol = "DataUnionOf";
		Iterator<List<Symbol>> i = syntax.iterator();
		while(i.hasNext())
			subExpressions.add(ExpressionFactory.createDatatype(i.next()));
		graphPattern = createGraphPattern("?x", "?y");
	}

}
