/**
 * 
 */
package com.i3s.app.rdfminer.expression.numberrestriction;

import java.util.List;

import com.i3s.app.rdfminer.evolutionary.geva.Mapper.Symbol;
import com.i3s.app.rdfminer.expression.Expression;
import com.i3s.app.rdfminer.expression.ExpressionFactory;

/**
 * A number restriction class expression of the form ObjectMinCardinality(...),
 * ObjectMaxCardinality(...), or ObjectExactCardinality(...).
 * <p>
 * Number restrictions may come in two form:
 * </p>
 * <ol>
 * <li>qualified number restrictions, with three arguments: number, role, and
 * concept;</li>
 * <li>unqualified number restrictions, with just two arguments: number and
 * role; alternatively, the OWL 2 syntax allows one to express an unqualified
 * number restriction with three arguments: number, role, and
 * <code>owl:Thing</code> as the concept.</li>
 * </ol>
 * <p>
 * These two forms are reflected by two distinct constructors.
 * </p>
 * 
 * @author Andrea G. B. Tettamanzi
 *
 */
public class NumberRestrictionClassExpression extends Expression {
	/**
	 * The target cardinality of this number restriction.
	 */
	protected int number;

	/**
	 * Creates a new unqualified number restriction class expression based on the
	 * provided functional-style syntax.
	 * 
	 * @param op   the name of the operator, one of "ObjectMinCardinality",
	 *             "ObjectMaxCardinality", or "ObjectExactCardinality";
	 * @param n    the cardinality of the number restriction;
	 * @param role a property expression in OWL 2 functional-style syntax.
	 */
	public NumberRestrictionClassExpression(String op, int n, List<Symbol> role) {
		this(op, n, role, null);
	}

	/**
	 * Creates a new qualified number restriction class expression based on the
	 * provided functional-style syntax.
	 * <p>
	 * If the last parameter is <code>null</code> or consists of the expression
	 * <code>owl:Thing</code>, then an unqualified number restriction is
	 * constructed.
	 * </p>
	 * 
	 * @param op      the name of the operator, one of "ObjectMinCardinality",
	 *                "ObjectMaxCardinality", or "ObjectExactCardinality";
	 * @param n       the cardinality of the number restriction;
	 * @param role    a property expression in OWL 2 functional-style syntax.
	 * @param concept a class expression in OWL 2 functional-style syntax.
	 */
	public NumberRestrictionClassExpression(String op, int n, List<Symbol> role, List<Symbol> concept) {
		super();
		rootSymbol = op;
		number = n;
		subExpressions.add(ExpressionFactory.createProperty(role));
		if (concept != null)
			if (!concept.get(0).equals("owl:Thing"))
				subExpressions.add(ExpressionFactory.createClass(concept));
		graphPattern = createGraphPattern("?x", "?y");
	}

	/**
	 * Instantiates the graph pattern for this number restriction class expression.
	 * 
	 * @param subject the expression replacing the <tt>?x</tt> SPARQL variable
	 * @param object  the expression replacing the <tt>?y</tt> SPARQL variable
	 * @return the SPARQL graph pattern for this expression
	 */
	@Override
	public String createGraphPattern(String subject, String object) {
		String z0 = getFreshVariableName();
		String z1 = getFreshVariableName();
		String z2 = getFreshVariableName();

		String op = "="; // "ObjectExactCardinality" by default...
		if (rootSymbol.equals("ObjectMinCardinality"))
			op = ">=";
		else if (rootSymbol.equals("ObjectMaxCardinality"))
			op = "<=";

		String pattern = "{ ";
		if (subject.startsWith("?") || subject.startsWith("$")) // i.e., a SPARQL variable:
		{
			pattern += subExpressions.get(0).createGraphPattern(subject, z0) + "\n" + "{ SELECT " + subject
					+ " WHERE {\n" + subExpressions.get(0).createGraphPattern(subject, z1) + "\n";
			if (subExpressions.size() > 1)
				pattern += subExpressions.get(1).createGraphPattern(z1, z2) + "\n ";
			pattern += "} GROUP BY " + subject + " HAVING ( count(DISTINCT " + z1 + ") " + op + " " + number
					+ " ) } } .\n";
		} else // special pattern in case the "subject" is an RDF resource:
		{
			pattern += "SELECT " + z0 + " WHERE {\n" + "BIND ( " + subject + " AS " + z0 + " )\n"
					+ subExpressions.get(0).createGraphPattern(z0, z1) + "\n";
			if (subExpressions.size() > 1)
				pattern += subExpressions.get(1).createGraphPattern(z1, z2) + "\n ";
			pattern += "} GROUP BY " + z0 + " HAVING ( count(DISTINCT " + z1 + ") " + op + " " + number + " ) } .\n";
		}
		return pattern;
	}

}
