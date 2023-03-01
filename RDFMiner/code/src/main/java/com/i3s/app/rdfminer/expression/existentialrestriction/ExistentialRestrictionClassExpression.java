/**
 * 
 */
package com.i3s.app.rdfminer.expression.existentialrestriction;

import java.util.List;

import com.i3s.app.rdfminer.evolutionary.geva.Mapper.Symbol;
import com.i3s.app.rdfminer.expression.Expression;
import com.i3s.app.rdfminer.expression.ExpressionFactory;
import com.i3s.app.rdfminer.expression.extensional.ExtensionalClassExpression;

/**
 * An existential restriction class expression of the form
 * ObjectSomeValuesFrom(...) or ObjectHasValue(...).
 * 
 * @author Andrea G. B. Tettamanzi
 *
 */
public class ExistentialRestrictionClassExpression extends Expression {

	/**
	 * Creates a new existential restriction class expression based on the provided
	 * functional-style syntax.
	 *
	 */
	public ExistentialRestrictionClassExpression(List<Symbol> role, List<Symbol> concept) {
		super();
		subExpressions.add(ExpressionFactory.createProperty(role));
		Expression restriction = ExpressionFactory.createClass(concept);
		subExpressions.add(restriction);
		if (restriction instanceof ExtensionalClassExpression && restriction.subExpressions.size() == 1)
			rootSymbol = "ObjectHasValue";
		else
			rootSymbol = "ObjectSomeValuesFrom";
		graphPattern = createGraphPattern("?x", "?y");
	}

	/**
	 * Instantiates the graph pattern for this existential restriction class
	 * expression.
	 * 
	 * @param subject the expression replacing the <tt>?x</tt> SPARQL variable
	 * @param object  the expression replacing the <tt>?y</tt> SPARQL variable
	 * @return the SPARQL graph pattern for this expression
	 */
	@Override
	public String createGraphPattern(String subject, String object) {
		String z1 = getFreshVariableName();
		String z2 = getFreshVariableName();

		// handle the two special cases:
		if (rootSymbol.equals("ObjectHasValue")) {
			// 1) existential restriction with extensional concept
			String a = subExpressions.get(1).extension.iterator().next().x.toString();
			return subExpressions.get(0).createGraphPattern(subject, a) + "\n";
		} else if (subExpressions.get(1) instanceof ExtensionalClassExpression)
			// 2) existential restriction with a nominal (= ObjectHasValue)
			return "{ " + subExpressions.get(0).createGraphPattern(subject, z1) + "\n"
					+ ((ExtensionalClassExpression) subExpressions.get(1)).getFilter(z1, false) + " }\n";
		else
			// Otherwise, for the general case, apply the general formula:
			return subExpressions.get(0).createGraphPattern(subject, z1) + "\n"
					+ subExpressions.get(1).createGraphPattern(z1, z2);
	}

}
