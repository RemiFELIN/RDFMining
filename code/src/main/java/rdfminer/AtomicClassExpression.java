/**
 * 
 */
package rdfminer;

import java.util.List;

import Mapper.Symbol;

/**
 * An OWL 2 class expression.
 * 
 * @author Andrea G. B. Tettamanzi
 *
 */
public class AtomicClassExpression extends Expression
{

	/**
	 * Creates a new atomic class expression based on the provided functional-style syntax.
	 * <p>The expression must be an atomic expression, consisting solely
	 * of the IRI of an OWL 2 class; class expressions from complex expressions
	 * must be created using the constructor of the appropriate subclass.</p>
	 * 
	 * @param syntax an expression in OWL 2 functional-style syntax. 
	 */
	public AtomicClassExpression(List<Symbol> syntax)
	{
		super();
		rootSymbol = syntax.get(0).getSymbolString();
		graphPattern = createGraphPattern("?x", "?y");
	}

	/**
	 * Instantiates the graph pattern for this atomic class expression.
	 */
	@Override
	protected String createGraphPattern(String subject, String object)
	{
		if(rootSymbol.equals("owl:Thing"))
			// Handle the special case of owl:Thing, i.e., the Top Concept:
			return subject + " a " + getFreshVariableName() + " .";
		else
			return subject + " a " + rootSymbol + " .";
	}

	/**
	 * Check whether the given RDF node is an instance of the class represented by this expression.
	 * 
	 * @see rdfminer.Expression#contains(org.apache.jena.rdf.model.RDFNode)
	 * @param node an RDF node
	 */
	@Override
	public boolean contains(RDFNodePair pair)
	{
		// This is a faster alternative to the method inherited from the superclass:
		if(rootSymbol.equals("owl:Thing"))
			// Handle the special case of owl:Thing, i.e., the Top Concept, which contains everything:
			return true;
		else
			return extension().contains(pair);
	}

}
