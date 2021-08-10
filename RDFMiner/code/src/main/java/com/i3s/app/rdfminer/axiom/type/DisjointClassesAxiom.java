/**
 * 
 */
package com.i3s.app.rdfminer.axiom.type;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.RDFNode;
//import org.apache.log4j.Logger;

import com.i3s.app.rdfminer.axiom.Axiom;
import com.i3s.app.rdfminer.expression.Expression;
import com.i3s.app.rdfminer.expression.ExpressionFactory;
import com.i3s.app.rdfminer.expression.complement.ComplementClassExpression;
import com.i3s.app.rdfminer.sparql.SparqlEndpoint;

import Mapper.Symbol;

/**
 * A class that represents a <code>DisjointClasses</code> axiom.
 * 
 * @author Andrea G. B. Tettamanzi & Thu Huong Nguyen
 *
 */
public class DisjointClassesAxiom extends Axiom {
	
//	private static Logger logger = Logger.getLogger(DisjointClassesAxiom.class.getName());

	/**
	 * An array of class expressions which are declared to be mutually disjoint.
	 */
	protected Expression[] disjointClass;

	/**
	 * An array of complements of the class expressions which are declared to be
	 * mutually disjoint.
	 */
	protected Expression[] disjointClassComplement;

	/**
	 * Create a new <code>DisjointClasses</code> axiom from the given list of
	 * concept expressions.
	 * 
	 * @param subClassExpression   the functional-style expression of the subclass
	 * @param superClassExpression the functional-style expression of the superclass
	 */
	public DisjointClassesAxiom(List<List<Symbol>> arguments, SparqlEndpoint endpoint) {
		disjointClass = new Expression[arguments.size()];
		disjointClassComplement = new Expression[disjointClass.length];
		for (int i = 0; i < disjointClass.length; i++) {
			disjointClass[i] = ExpressionFactory.createClass(arguments.get(i));
			if (disjointClass[i] instanceof ComplementClassExpression)
				// Handle the double negation in an optimized way:
				disjointClassComplement[i] = disjointClass[i].subExpressions.get(0);
			else
				disjointClassComplement[i] = new ComplementClassExpression(disjointClass[i]);
		}

		update(endpoint);
	}

	/**
	 * Construct the <var>Q</var><sub><code>Dis</code></sub> graph pattern.
	 * 
	 * @param j
	 * @param i
	 * @param x
	 * @param y
	 * @return
	 */
	protected String disjunctionGraphPattern(int j, int i, String x, String y) {
		String dc = Expression.getFreshVariableName();
		String z1 = Expression.getFreshVariableName();
		String z2 = Expression.getFreshVariableName();

		String gp = "{ " + x + " a " + dc + " .\n";
		gp += z1 + " a " + dc + " .\n";
		gp += disjointClassComplement[i].createGraphPattern(z1, Expression.getFreshVariableName()) + "\n";
		gp += "FILTER NOT EXISTS {\n";
		gp += z2 + " a " + dc + " .\n";
		gp += disjointClass[j].createGraphPattern(z2, Expression.getFreshVariableName()) + "\n";
		gp += "}\n}\n";
		return gp;
	}


	protected String nonConfirmationGraphPattern(int i, int j) {
		String dc = Expression.getFreshVariableName();
		final String graphPattern1 = "{ " + disjointClass[i].graphPattern + " \n";
		final String graphPattern2 = disjointClass[j].createGraphPattern("?y", "y") + " \n";
		String gp = graphPattern1;
		gp += "?x" + " a " + dc + ".\n  ";
		gp += "?y" + " a " + dc + ".\n";
		gp += graphPattern2;
		gp += "}\n";

		return gp;
	}

	protected String nonConfirmationGraphPattern2(int i, int j) {
		String dc = Expression.getFreshVariableName();
		final String graphPattern3 = "{ " + disjointClass[j].graphPattern + " \n";
		final String graphPattern4 = disjointClass[i].createGraphPattern("?y", "y") + " \n";
		String gp2 = graphPattern3;
		gp2 += "?x" + " a " + dc + ".\n";
		gp2 += "?y" + " a " + dc + ".\n";
		gp2 += graphPattern4;
		gp2 += "}\n";
		return gp2;
	}
	
	/**
	 * Updates the counts used to compute the possibility and necessity degrees.
	 * <p>
	 * According to the model-theoretic semantics, an axiom of the form
	 * <code>DisjointClasses(CE<sub>1</sub> ... CE<sub><var>n</var></sub>)</code> is
	 * satisfied if ...
	 * </p>
	 * <p>
	 * The {@link #naive_update()} method provides a slower, but hopefully safer,
	 * way of updating the counts.
	 * </p>
	 */
	@Override
	public void update(SparqlEndpoint endpoint) {
		confirmations = new ArrayList<String>();
		exceptions = new ArrayList<String>();

		String refCardGraphPattern = "";
		for (int i = 0; i < disjointClass.length; i++) {
			if (i > 0)
				refCardGraphPattern += " UNION ";
			refCardGraphPattern += "{ " + disjointClass[i].graphPattern + " }";
		}

		int generality1 = 0;
		int generality2 = 0;
		int k = 0;
		while (k < disjointClass.length) {
			String generalityGraphPattern = "";
			String generalityGraphPattern2 = "";
			generalityGraphPattern += "{ " + disjointClass[k].graphPattern + " }";
			generalityGraphPattern2 += "{ " + disjointClass[k + 1].graphPattern + " }";
			// ----compute the cost of GP
			generality1 = endpoint.count("?x", generalityGraphPattern, 0);
			generality2 = endpoint.count("?x", generalityGraphPattern2, 0);
			if (generality1 > generality2)
				generality = generality2;
			else
				generality = generality1;
			k = k + 2;
		}
		// logger.info("Generality: " + generality);
		if (generality != 0) {
			referenceCardinality = endpoint.count("?x", refCardGraphPattern, 0);
			// skipping computing the reference cardinality when generality=0
			// logger.info("Number referenceCardinality: " + referenceCardinality);
			String exceptionGraphPattern = "";
			for (int i = 0; i < disjointClass.length; i++)
				exceptionGraphPattern += disjointClass[i].graphPattern + "\n";
			numExceptions = endpoint.count("?x", exceptionGraphPattern, 0);
			// logger.info("Number of exception: " + numExceptions);
			// logger.info(" ");
			if (numExceptions > 0 && numExceptions < 100) {
				// query the exceptions
				// endpoint.select("TO DO");
				while (endpoint.hasNext()) {
					QuerySolution solution = endpoint.next();
					RDFNode x = solution.get("x");
					exceptions.add(Expression.sparqlEncode(x));
				}
			}
			numConfirmations = referenceCardinality - numExceptions;
		} else
			referenceCardinality = 0;
	}

	public void updateVolker(SparqlEndpoint endpoint) {
		confirmations = new ArrayList<String>();
		exceptions = new ArrayList<String>();

		String refCardGraphPattern = "";
		for (int i = 0; i < disjointClass.length; i++) {
			if (i > 0)
				refCardGraphPattern += " UNION ";
			refCardGraphPattern += "{ " + disjointClass[i].graphPattern + " }";
		}

		int generality1 = 0;
		int generality2 = 0;
		int k = 0;
		while (k < disjointClass.length) {
			String generalityGraphPattern = "";
			String generalityGraphPattern2 = "";
			generalityGraphPattern += "{ " + disjointClass[k].graphPattern + " }";
			generalityGraphPattern2 += "{ " + disjointClass[k + 1].graphPattern + " }";
			generality1 = endpoint.count("?x", generalityGraphPattern, 0);
			generality2 = endpoint.count("?x", generalityGraphPattern2, 0);
			if (generality1 > generality2)
				generality = generality2;
			else
				generality = generality1;
			k = k + 2;
		}
		// logger.info("Generality :" + generality);
		referenceCardinality = endpoint.count("?x", refCardGraphPattern, 0);
		// logger.info("number referenceCardinality: " + referenceCardinality);
		String exceptionGraphPattern = "";
		for (int i = 0; i < disjointClass.length; i++)
			exceptionGraphPattern += disjointClass[i].graphPattern + "\n";
		numExceptions = endpoint.count("?x", exceptionGraphPattern, 0);
		// logger.info("number of exception: " + numExceptions);
		// logger.info(".............................................................");
		if (numExceptions > 0 && numExceptions < 100) {
			// query the exceptions
			while (endpoint.hasNext()) {
				QuerySolution solution = endpoint.next();
				RDFNode x = solution.get("x");
				exceptions.add(Expression.sparqlEncode(x));
			}
		}
		numConfirmations = referenceCardinality - numExceptions;
	}

	public Expression[] getExpression() {
		return disjointClass;
	}

	@Override
	public void update() {}

}
