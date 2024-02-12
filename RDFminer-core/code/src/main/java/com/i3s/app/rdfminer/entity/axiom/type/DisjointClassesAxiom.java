/**
 * 
 */
package com.i3s.app.rdfminer.entity.axiom.type;

import com.i3s.app.rdfminer.entity.axiom.Axiom;
import com.i3s.app.rdfminer.evolutionary.geva.Mapper.Symbol;
import com.i3s.app.rdfminer.expression.Expression;
import com.i3s.app.rdfminer.expression.ExpressionFactory;
import com.i3s.app.rdfminer.expression.complement.ComplementClassExpression;
import com.i3s.app.rdfminer.sparql.corese.CoreseEndpoint;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

/**
 * A class that represents a <code>DisjointClasses</code> axiom.
 * 
 * @author Andrea G. B. Tettamanzi & Thu Huong Nguyen
 *
 */
public class DisjointClassesAxiom extends Axiom {
	
	private static final Logger logger = Logger.getLogger(DisjointClassesAxiom.class.getName());

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
	 * @param arguments   the functional-style expression of the subclass
	 * @param endpoint the functional-style expression of the superclass
	 */
	public DisjointClassesAxiom(List<List<Symbol>> arguments, CoreseEndpoint endpoint) throws URISyntaxException, IOException {
		super();
		long t0 = getProcessCPUTime();
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
		// set elapsedTime as a CPU usage time
		elapsedTime = getProcessCPUTime() - t0;
		logger.info("elapsed time = " + elapsedTime + " ms.");
	}

	/**
	 * Construct the <var>Q</var><sub><code>Dis</code></sub> graph pattern.
	 */
//	protected String disjunctionGraphPattern(int j, int i, String x, String y) {
//		String dc = Expression.getFreshVariableName();
//		String z1 = Expression.getFreshVariableName();
//		String z2 = Expression.getFreshVariableName();
//
//		String gp = "{ " + x + " a " + dc + " .\n";
//		gp += z1 + " a " + dc + " .\n";
//		gp += disjointClassComplement[i].createGraphPattern(z1, Expression.getFreshVariableName()) + "\n";
//		gp += "FILTER NOT EXISTS {\n";
//		gp += z2 + " a " + dc + " .\n";
//		gp += disjointClass[j].createGraphPattern(z2, Expression.getFreshVariableName()) + "\n";
//		gp += "}\n}\n";
//		return gp;
//	}
//
//
//	protected String nonConfirmationGraphPattern(int i, int j) {
//		String dc = Expression.getFreshVariableName();
//		final String graphPattern1 = "{ " + disjointClass[i].graphPattern + " \n";
//		final String graphPattern2 = disjointClass[j].createGraphPattern("?y", "y") + " \n";
//		String gp = graphPattern1;
//		gp += "?x" + " a " + dc + ".\n  ";
//		gp += "?y" + " a " + dc + ".\n";
//		gp += graphPattern2;
//		gp += "}\n";
//
//		return gp;
//	}

//	protected String nonConfirmationGraphPattern2(int i, int j) {
//		String dc = Expression.getFreshVariableName();
//		final String graphPattern3 = "{ " + disjointClass[j].graphPattern + " \n";
//		final String graphPattern4 = disjointClass[i].createGraphPattern("?y", "y") + " \n";
//		String gp2 = graphPattern3;
//		gp2 += "?x a " + dc + " .\n";
//		gp2 += "?y a " + dc + " .\n";
//		gp2 += graphPattern4;
//		gp2 += "}\n";
//		return gp2;
//	}
//
	/**
	 * Updates the counts used to compute the possibility and necessity degrees.
	 * <p>
	 * According to the model-theoretic semantics, an axiom of the form
	 * <code>DisjointClasses(CE<sub>1</sub> ... CE<sub><var>n</var></sub>)</code> is
	 * satisfied if ...
	 * </p>
	 */
	@Override
	public void update(CoreseEndpoint endpoint) throws URISyntaxException, IOException {
//		confirmations = new ArrayList<>();
//		exceptions = new ArrayList<>();
//		long timeSpent = 0;
		StringBuilder refCardGraphPattern = new StringBuilder();
		for (int i = 0; i < disjointClass.length; i++) {
			if (i > 0)
				refCardGraphPattern.append(" UNION ");
			refCardGraphPattern.append("{ ").append(disjointClass[i].graphPattern).append(" }");
		}
//		int generality1, generality2;
		int k = 0;
		// compute generality and reference cardinality
		while (k < disjointClass.length) {
			String generalityGraphPattern = "{ " + disjointClass[k].graphPattern + " }";
			String generalityGraphPattern2 = "{ " + disjointClass[k + 1].graphPattern + " }";
//			System.out.println("generalityGraphPattern: " + generalityGraphPattern);
//			System.out.println("generalityGraphPattern2: " + generalityGraphPattern2);
			// compute the cost of GP
			generality = Math.min(endpoint.count("?x", generalityGraphPattern), endpoint.count("?x", generalityGraphPattern2));
			k = k + 2;
		}
//		logger.info("generality: " + generality);
		// skipping computing the reference cardinality when generality=0
		if (generality != 0) {
			referenceCardinality = endpoint.count("?x", refCardGraphPattern.toString());
			StringBuilder exceptionGraphPattern = new StringBuilder();
			for (Expression aClass : disjointClass) exceptionGraphPattern.append(aClass.graphPattern).append("\n");
			numExceptions = endpoint.count("?x", exceptionGraphPattern.toString());
//			logger.info("Num. exceptions: " + numExceptions);
//			if (numExceptions > 0 && numExceptions < 100) {
				// query the exceptions
				// endpoint.select("TO DO");
//				while (endpoint.hasNext()) {
//					QuerySolution solution = endpoint.next();
//					RDFNode x = solution.get("x");
//					exceptions.add(Expression.sparqlEncode(x));
//				}
//			}
			numConfirmations = referenceCardinality - numExceptions;
		} else {
			referenceCardinality = 0;
		}
		logger.info("referenceCardinality = " + referenceCardinality);
	}

//	public void updateVolker(VirtuosoEndpoint endpoint) {
//		confirmations = new ArrayList<>();
//		exceptions = new ArrayList<>();
//
//		StringBuilder refCardGraphPattern = new StringBuilder();
//		for (int i = 0; i < disjointClass.length; i++) {
//			if (i > 0)
//				refCardGraphPattern.append(" UNION ");
//			refCardGraphPattern.append("{ ").append(disjointClass[i].graphPattern).append(" }");
//		}
//
//		int generality1 = 0;
//		int generality2 = 0;
//		int k = 0;
//		while (k < disjointClass.length) {
//			String generalityGraphPattern = "";
//			String generalityGraphPattern2 = "";
//			generalityGraphPattern += "{ " + disjointClass[k].graphPattern + " }";
//			generalityGraphPattern2 += "{ " + disjointClass[k + 1].graphPattern + " }";
//			generality1 = endpoint.count("?x", generalityGraphPattern, 0);
//			generality2 = endpoint.count("?x", generalityGraphPattern2, 0);
//			generality = Math.min(generality1, generality2);
//			k = k + 2;
//		}
//		// logger.info("Generality :" + generality);
//		referenceCardinality = endpoint.count("?x", refCardGraphPattern.toString(), 0);
//		// logger.info("number referenceCardinality: " + referenceCardinality);
//		StringBuilder exceptionGraphPattern = new StringBuilder();
//		for (Expression aClass : disjointClass) exceptionGraphPattern.append(aClass.graphPattern).append("\n");
//		numExceptions = endpoint.count("?x", exceptionGraphPattern.toString(), 0);
////		if (numExceptions > 0 && numExceptions < 100) {
////			// query the exceptions
////			while (endpoint.hasNext()) {
////				QuerySolution solution = endpoint.next();
////				RDFNode x = solution.get("x");
////				exceptions.add(Expression.sparqlEncode(x));
////			}
////		}
//		numConfirmations = referenceCardinality - numExceptions;
//	}

	public Expression[] getExpression() {
		return disjointClass;
	}

}
