/**
 * 
 */
package com.i3s.app.rdfminer.axiom.type;

import java.util.ArrayList;
import java.util.List;

import com.i3s.app.rdfminer.axiom.Axiom;
import com.i3s.app.rdfminer.expression.Expression;
import com.i3s.app.rdfminer.expression.ExpressionFactory;
import com.i3s.app.rdfminer.expression.complement.ComplementClassExpression;
import com.i3s.app.rdfminer.sparql.virtuoso.VirtuosoEndpoint;

import Mapper.Symbol;
import org.apache.log4j.Logger;

/**
 * A class that represents a <code>EquivalentClasses</code> axiom.
 * 
 * @author Nguyen Thu Huong
 *
 */
public class EquivalentClassesAxiom extends Axiom {

	private static Logger logger = Logger.getLogger(EquivalentClassesAxiom.class.getName());

	/**
	 * An array of class expressions which are declared to be mutually equivalent.
	 */
	protected Expression[] equivalentClass;

	/**
	 * An array of complements of the class expressions which are declared to be
	 * mutually equivalent.
	 */
	protected Expression[] equivalentClassComplement;

	/**
	 * Create a new <code>EquivalentClasses</code> axiom from the given list of
	 * concept expressions.
	 * 
	 *
	 */
	public EquivalentClassesAxiom(List<List<Symbol>> arguments, VirtuosoEndpoint endpoint) {
		long t0 = getProcessCPUTime();
		equivalentClass = new Expression[arguments.size()];
		equivalentClassComplement = new Expression[equivalentClass.length];
		for (int i = 0; i < equivalentClass.length; i++) {
			equivalentClass[i] = ExpressionFactory.createClass(arguments.get(i));
			if (equivalentClass[i] instanceof ComplementClassExpression) {
				// Handle the double negation in an optimized way:
				equivalentClassComplement[i] = equivalentClass[i].subExpressions.get(0);
			} else {
				equivalentClassComplement[i] = new ComplementClassExpression(equivalentClass[i]);
			}
			logger.info("\nclass_" + i + " = " + equivalentClass[i] + "; graph pattern =");
			logger.info(VirtuosoEndpoint.prettyPrint(equivalentClass[i].graphPattern));
			logger.info("\n~class_" + i + " = " + equivalentClassComplement[i] + "; graph pattern =");
			logger.info(VirtuosoEndpoint.prettyPrint(equivalentClassComplement[i].graphPattern));
		}
		update(endpoint);
		// set elapsedTime as a CPU usage time
		elapsedTime = getProcessCPUTime() - t0;
		logger.info("elapsed time = " + elapsedTime + " ms.");
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
	protected String equivalentGraphPattern(int j, int i, String x, String y) {
		String dc = Expression.getFreshVariableName(); // tra lai ki hieu cua Expression "?z_" + (freshVarId++)
		String z1 = Expression.getFreshVariableName();
		String z2 = Expression.getFreshVariableName();

		String gp = "{ " + x + " a " + dc + " .\n"; // gp -graphpattern
		gp += z1 + " a " + dc + " .\n";
		gp += equivalentClassComplement[i].createGraphPattern(z1, Expression.getFreshVariableName()) + "\n";
		gp += "FILTER NOT EXISTS {\n";
		gp += z2 + " a " + dc + " .\n";
		gp += equivalentClass[j].createGraphPattern(z2, Expression.getFreshVariableName()) + "\n";
		gp += "}\n}\n";
		return gp;
	}

	/**
	 * Updates the counts used to compute the possibility and necessity degrees.
	 * <p>
	 * According to the model-theoretic semantics, an axiom of the form
	 * <code>DisjointClasses(CE<sub>1</sub> ... CE<sub><var>n</var></sub>)</code> is
	 * satisfied if ...
	 * </p>
	 */
	@Override
	public void update(VirtuosoEndpoint endpoint) {
		confirmations = new ArrayList<String>();
		exceptions = new ArrayList<String>();
		String refCardGraphPattern = "";
		for (int i = 0; i < equivalentClass.length; i++) {
			if (i > 0)
				refCardGraphPattern += " UNION ";
			refCardGraphPattern += "{ " + equivalentClass[i].graphPattern + " }";
		}
		referenceCardinality = endpoint.count("?x", refCardGraphPattern, 0);

		String confirmationGraphPattern = "";
		for (int i = 0; i < equivalentClass.length; i++) {
			if (i > 0)
				confirmationGraphPattern += " UNION ";
			confirmationGraphPattern += "{ ";
			for (int j = 0; j < equivalentClass.length; j++) {
				if (j == i)
					confirmationGraphPattern += equivalentClass[j].graphPattern + "\n";
				else
					confirmationGraphPattern += equivalentGraphPattern(j, i, "?x", "?y");
			}
			confirmationGraphPattern += " }";
		}
		numConfirmations = endpoint.count("?x", confirmationGraphPattern, 0);
//		if (numConfirmations > 0 && numConfirmations < 100) {
//			// query the confirmations
//			RDFMiner.endpoint.select("TO DO", 0);
//			while (RDFMiner.endpoint.hasNext()) {
//				QuerySolution solution = RDFMiner.endpoint.next();
//				RDFNode x = solution.get("x");
//				confirmations.add(Expression.sparqlEncode(x));
//			}
//		}

		String exceptionGraphPattern = "";
		for (int i = 0; i < equivalentClass.length; i++)
			exceptionGraphPattern += equivalentClass[i].graphPattern + "\n";
		numExceptions = endpoint.count("?x", exceptionGraphPattern, 0);
//		if (numExceptions > 0 && numExceptions < 100) {
//			// query the exceptions
//			RDFMiner.endpoint.select("TO DO", 0);
//			while (RDFMiner.endpoint.hasNext()) {
//				QuerySolution solution = RDFMiner.endpoint.next();
//				RDFNode x = solution.get("x");
//				exceptions.add(Expression.sparqlEncode(x));
//			}
//		}
	}

}
