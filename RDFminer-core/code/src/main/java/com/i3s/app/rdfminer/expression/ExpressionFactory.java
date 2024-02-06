/**
 * 
 */
package com.i3s.app.rdfminer.expression;

import java.util.ArrayList;
import java.util.List;

import com.i3s.app.rdfminer.evolutionary.geva.Mapper.Symbol;
import com.i3s.app.rdfminer.evolutionary.geva.Util.Enums;
import com.i3s.app.rdfminer.expression.atomic.AtomicClassExpression;
import com.i3s.app.rdfminer.expression.atomic.AtomicDatatypeExpression;
import com.i3s.app.rdfminer.expression.atomic.AtomicPropertyExpression;
import com.i3s.app.rdfminer.expression.complement.ComplementClassExpression;
import com.i3s.app.rdfminer.expression.complement.ComplementDatatypeExpression;
import com.i3s.app.rdfminer.expression.existentialrestriction.ExistentialRestrictionClassExpression;
import com.i3s.app.rdfminer.expression.extensional.ExtensionalClassExpression;
import com.i3s.app.rdfminer.expression.extensional.ExtensionalDatatypeExpression;
import com.i3s.app.rdfminer.expression.intersection.IntersectionClassExpression;
import com.i3s.app.rdfminer.expression.intersection.IntersectionDatatypeExpression;
import com.i3s.app.rdfminer.expression.inverseproperty.InversePropertyExpression;
import com.i3s.app.rdfminer.expression.localreflexivity.LocalReflexivityClassExpression;
import com.i3s.app.rdfminer.expression.numberrestriction.NumberRestrictionClassExpression;
import com.i3s.app.rdfminer.expression.union.UnionClassExpression;
import com.i3s.app.rdfminer.expression.union.UnionDatatypeExpression;
import com.i3s.app.rdfminer.expression.valuerestriction.ValueRestrictionClassExpression;
import com.i3s.app.rdfminer.grammar.DLFactory;

/**
 * The expression factory singleton class is able to construct expressions of
 * various types.
 * <p>
 * There are seven types of atomic expressions, namely:
 * </p>
 * <ul>
 * <li>classes;</li>
 * <li>object properties;</li>
 * <li>data properties;</li>
 * <li>individuals;</li>
 * <li>datatypes;</li>
 * <li>literals;</li>
 * <li>facets.</li>
 * </ul>
 * <p>
 * The types of complex expressions that are provided for by OWL 2 are the
 * following 23, divided in three categories:
 * </p>
 * <ul>
 * <li>object property expressions:</li>
 * <ul>
 * <li><code>ObjectInverseOf</code> expressions;</li>
 * </ul>
 * <li>data type expressions:</li>
 * <ul>
 * <li><code>DataIntersectionOf</code> expressions;</li>
 * <li><code>DataUnionOf</code> expressions;</li>
 * <li><code>DataComplementOf</code> expressions;</li>
 * <li><code>DataOneOf</code> expressions;</li>
 * <li><code>DatatypeRestriction</code> expressions;</li>
 * </ul>
 * <li>class expressions:</li>
 * <ul>
 * <li><code>ObjectIntersectionOf</code> expressions;</li>
 * <li><code>ObjectUnionOf</code> expressions;</li>
 * <li><code>ObjectComplementOf</code> expressions;</li>
 * <li><code>ObjectOneOf</code> expressions;</li>
 * <li><code>ObjectSomeValuesFrom</code> expressions;</li>
 * <li><code>ObjectAllValuesFrom</code> expressions;</li>
 * <li><code>ObjectHasValue</code> expressions;</li>
 * <li><code>ObjectHasSelf</code> expressions;</li>
 * <li><code>ObjectMinCardinality</code> expressions;</li>
 * <li><code>ObjectMaxCardinality</code> expressions;</li>
 * <li><code>ObjectExactCardinality</code> expressions;</li>
 * <li><code>DataSomeValuesFrom</code> expressions;</li>
 * <li><code>DataAllValuesFrom</code> expressions;</li>
 * <li><code>DataHasValue</code> expressions;</li>
 * <li><code>DataMinCardinality</code> expressions;</li>
 * <li><code>DataMaxCardinality</code> expressions;</li>
 * <li><code>DataExactCardinality</code> expressions.</li>
 * </ul>
 * </ul>
 * <p>
 * Please refer to the document
 * <a href="http://www.w3.org/TR/2012/REC-owl2-direct-semantics-20121211/">OWL 2
 * Web Ontology Language Direct Semantics (Second Edition)</a> for a
 * model-theoretic semantics of all the types of expressions, compatible with
 * the description logic <em>SROIQ</em>.
 * </p>
 * 
 * @author Andrea G. B. Tettamanzi
 *
 */
public class ExpressionFactory extends DLFactory {
	/**
	 * This class should not be instantiated. Any attempt at calling this
	 * constructor will result in an exception being thrown.
	 */
	private ExpressionFactory() {
		throw new RuntimeException("ExpressionFactory cannot be instantiated.");
	}

	/**
	 * Creates a property expression from a list of symbols in OWL 2
	 * functional-style syntax.
	 * 
	 * @param syntax an expression in OWL 2 functional-style syntax.
	 * @return the corresponding expression.
	 */
	public static Expression createProperty(List<Symbol> syntax) {
		Expression expr = null;
		if (syntax.get(0).equals("ObjectInverseOf")) {
			List<List<Symbol>> subexpression = parseArguments(syntax);
			require(subexpression.size() == 1);
			expr = new InversePropertyExpression(subexpression.get(0));
		} else // Atomic expression
			expr = new AtomicPropertyExpression(syntax);
		return expr;
	}

	/**
	 * Creates a datatype expression from a list of symbols in OWL 2
	 * functional-style syntax.
	 * 
	 * @param syntax an expression in OWL 2 functional-style syntax.
	 * @return the corresponding expression.
	 */
	public static Expression createDatatype(List<Symbol> syntax) {
		Expression expr = null;
		if (syntax.get(0).equals("DataIntersectionOf")) {
			List<List<Symbol>> subexpression = parseArguments(syntax);
			require(subexpression.size() > 1);
			expr = new IntersectionDatatypeExpression(subexpression);
		} else if (syntax.get(0).equals("DataUnionOf")) {
			List<List<Symbol>> subexpression = parseArguments(syntax);
			require(subexpression.size() > 1);
			expr = new UnionDatatypeExpression(subexpression);
		} else if (syntax.get(0).equals("DataComplementOf")) {
			List<List<Symbol>> subexpression = parseArguments(syntax);
			require(subexpression.size() == 1);
			expr = new ComplementDatatypeExpression(subexpression.get(0));
		} else if (syntax.get(0).equals("DataOneOf")) {
			List<List<Symbol>> subexpression = parseArguments(syntax);
			require(subexpression.size() > 0);
			expr = new ExtensionalDatatypeExpression(subexpression);
		} else if (syntax.get(0).equals("DatatypeRestriction")) {
			// TODO
		} else {
			// Atomic expression
			expr = new AtomicDatatypeExpression(syntax);
		}
		return expr;
	}

	/**
	 * Creates a class expression from a list of symbols in OWL 2 functional-style
	 * syntax.
	 * 
	 * @param syntax an expression in OWL 2 functional-style syntax.
	 * @return the corresponding expression.
	 */
	public static Expression createClass(List<Symbol> syntax) {
		Expression expr = null;

		if (syntax.get(0).equals("ObjectIntersectionOf")) {
			List<List<Symbol>> subexpression = parseArguments(syntax);
			require(subexpression.size() > 1);
			expr = new IntersectionClassExpression(subexpression);
		} else if (syntax.get(0).equals("ObjectUnionOf")) {
			List<List<Symbol>> subexpression = parseArguments(syntax);
			require(subexpression.size() > 1);
			expr = new UnionClassExpression(subexpression);
		} else if (syntax.get(0).equals("ObjectComplementOf")) {
			List<List<Symbol>> subexpression = parseArguments(syntax);
			require(subexpression.size() == 1);
			expr = new ComplementClassExpression(subexpression.get(0));
		} else if (syntax.get(0).equals("ObjectOneOf")) {
			List<List<Symbol>> subexpression = parseArguments(syntax);
			require(subexpression.size() > 0);
			expr = new ExtensionalClassExpression(subexpression);
		} else if (syntax.get(0).equals("ObjectSomeValuesFrom")) {
			List<List<Symbol>> subexpression = parseArguments(syntax);
			require(subexpression.size() == 2);
			expr = new ExistentialRestrictionClassExpression(subexpression.get(0), subexpression.get(1));
		} else if (syntax.get(0).equals("ObjectAllValuesFrom")) {
			List<List<Symbol>> subexpression = parseArguments(syntax);
			require(subexpression.size() == 2);
			expr = new ValueRestrictionClassExpression(subexpression.get(0), subexpression.get(1));
		} else if (syntax.get(0).equals("ObjectHasValue")) {
			// This is a special case of ObjectSomeValuesFrom where the restriction
			// is a singleton extensional concept. Therefore, we construct an
			// ObjectOneOf expression containing the second argument.
			List<List<Symbol>> subexpression = parseArguments(syntax);
			require(subexpression.size() == 2);
			require(subexpression.get(1).size() == 1);
			List<Symbol> nominal = new ArrayList<Symbol>();
			nominal.add(new Symbol("ObjectOneOf", Enums.SymbolType.TSymbol));
			nominal.add(new Symbol("(", Enums.SymbolType.TSymbol));
			nominal.addAll(subexpression.get(1));
			nominal.add(new Symbol(")", Enums.SymbolType.TSymbol));
			expr = new ExistentialRestrictionClassExpression(subexpression.get(0), nominal);
		} else if (syntax.get(0).equals("ObjectHasSelf")) {
			List<List<Symbol>> subexpression = parseArguments(syntax);
			require(subexpression.size() == 1);
			expr = new LocalReflexivityClassExpression(subexpression.get(0));
		} else if (syntax.get(0).equals("ObjectMinCardinality")) {
			expr = createNumberRestriction(syntax);
		} else if (syntax.get(0).equals("ObjectMaxCardinality")) {
			expr = createNumberRestriction(syntax);
		} else if (syntax.get(0).equals("ObjectExactCardinality")) {
			expr = createNumberRestriction(syntax);
		} else if (syntax.get(0).equals("DataSomeValuesFrom")) {
			// TODO
		} else if (syntax.get(0).equals("DataAllValuesFrom")) {
			// TODO
		} else if (syntax.get(0).equals("DataHasValue")) {
			// TODO
		} else if (syntax.get(0).equals("DataMinCardinality")) {
			// TODO
		} else if (syntax.get(0).equals("DataMaxCardinality")) {
			// TODO
		} else if (syntax.get(0).equals("DataExactCardinality")) {
			// TODO
		} else {
			// Atomic expression
			expr = new AtomicClassExpression(syntax);
		}
		return expr;
	}

	protected static Expression createNumberRestriction(List<Symbol> syntax) {
		List<List<Symbol>> subexpression = parseArguments(syntax);
		require(subexpression.size() == 2 || subexpression.size() == 3);
		List<Symbol> cardinality = subexpression.get(0);
		require(cardinality.size() == 1);
		int n = Integer.parseInt(cardinality.get(0).getSymbolString());
		if (subexpression.size() == 2)
			return new NumberRestrictionClassExpression(syntax.get(0).getSymbolString(), n, subexpression.get(1));
		else
			return new NumberRestrictionClassExpression(syntax.get(0).getSymbolString(), 0, subexpression.get(1),
					subexpression.get(2));
	}

}
