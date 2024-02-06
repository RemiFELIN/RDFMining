/**
 * 
 */
package com.i3s.app.rdfminer.expression.atomic;

import java.util.List;

import com.i3s.app.rdfminer.evolutionary.geva.Mapper.Symbol;
import com.i3s.app.rdfminer.expression.Expression;

/**
 * An OWL 2 atomic datatype expression.
 * 
 * @author Andrea G. B. Tettamanzi
 *
 */
public class AtomicDatatypeExpression extends Expression {

	/**
	 * Creates a new atomic datatype expression based on the provided
	 * functional-style syntax.
	 * <p>
	 * The expression must be an atomic expression, consisting solely of the IRI of
	 * an OWL 2 datatype; datatype expressions from complex expressions must be
	 * created using the constructor of the appropriate datatype expression.
	 * </p>
	 * 
	 * @param syntax an expression in OWL 2 functional-style syntax.
	 */
	public AtomicDatatypeExpression(List<Symbol> syntax) {
		super();
		rootSymbol = syntax.get(0).getSymbolString();
		graphPattern = createGraphPattern("?x", "?y");
		// System.out.println("AtomicDatatypeExpression: graphPattern = \n" + graphPattern);
	}

	/**
	 * Instantiates the graph pattern for this atomic datatype expression.
	 */
	@Override
	public String createGraphPattern(String subject, String object) {
		return getFreshVariableName() + " " + getFreshVariableName() + " " + subject + " . " + "FILTER (datatype("
				+ subject + ") = " + rootSymbol + " )";
	}

}
