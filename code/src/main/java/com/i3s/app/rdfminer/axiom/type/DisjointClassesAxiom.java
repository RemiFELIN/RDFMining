/**
 * 
 */
package com.i3s.app.rdfminer.axiom.type;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.RDFNode;

import com.i3s.app.rdfminer.RDFMiner;
import com.i3s.app.rdfminer.axiom.Axiom;
import com.i3s.app.rdfminer.expression.Expression;
import com.i3s.app.rdfminer.expression.ExpressionFactory;
import com.i3s.app.rdfminer.expression.complement.ComplementClassExpression;
import com.i3s.app.rdfminer.sparql.SparqlEndpoint;

// import org.apache.log4j.Logger;

//import com.hp.hpl.jena.query.QuerySolution;
//import com.hp.hpl.jena.rdf.model.RDFNode;

import Mapper.Symbol;

/**
 * A class that represents a <code>DisjointClasses</code> axiom.
 * 
 * @author Andrea G. B. Tettamanzi
 *
 */
public class DisjointClassesAxiom extends Axiom
{
	// private static Logger logger = Logger.getLogger(DisjointClassesAxiom.class.getName());
	
	/**
	 * An array of class expressions which are declared to be mutually disjoint.
	 */
	protected Expression[] disjointClass;
	
	/**
	 * An array of complements of the class expressions which are declared to be mutually disjoint.
	 */
	protected Expression[] disjointClassComplement;

	/**
	 * Create a new <code>DisjointClasses</code> axiom from the given list of concept expressions.
	 * 
	 * @param subClassExpression the functional-style expression of the subclass
	 * @param superClassExpression the functional-style expression of the superclass
	 */
	public DisjointClassesAxiom(List<List<Symbol>> arguments)
	{
		disjointClass = new Expression[arguments.size()];
		disjointClassComplement = new Expression[disjointClass.length];
		for(int i = 0; i<disjointClass.length; i++)
		{
			disjointClass[i] = ExpressionFactory.createClass(arguments.get(i));
			if(disjointClass[i] instanceof ComplementClassExpression)
				// Handle the double negation in an optimized way:
				disjointClassComplement[i] = disjointClass[i].subExpressions.get(0);
			else
				disjointClassComplement[i] = new ComplementClassExpression(disjointClass[i]);
			
			System.out.println("\nclass_" + i + " = " + disjointClass[i] + "; graph pattern =");
			System.out.println(SparqlEndpoint.prettyPrint(disjointClass[i].graphPattern));
			
			System.out.println("\n~class_" + i + " = " + disjointClassComplement[i] + "; graph pattern =");
			System.out.println(SparqlEndpoint.prettyPrint(disjointClassComplement[i].graphPattern));			
		}

		update();
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
	protected String disjunctionGraphPattern(int j, int i, String x, String y)
	{
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
		
	/**
	 * Updates the counts used to compute the possibility and necessity degrees.
	 * <p>According to the model-theoretic semantics, an axiom of the form
	 * <code>DisjointClasses(CE<sub>1</sub> ... CE<sub><var>n</var></sub>)</code>
	 * is satisfied if ...</p>
	 * <p>The {@link #naive_update()} method provides a slower, but hopefully safer,
	 * way of updating the counts.</p>
	 */
	@Override
	public void update()
	{
		confirmations = new ArrayList<String>();
		exceptions = new ArrayList<String>();
		
		String refCardGraphPattern = "";
		for(int i = 0; i<disjointClass.length; i++)
		{
			if(i>0)
				refCardGraphPattern += " UNION ";
			refCardGraphPattern += "{ " + disjointClass[i].graphPattern + " }";
		}
		referenceCardinality = RDFMiner.endpoint.count("?x", refCardGraphPattern);
		
		String confirmationGraphPattern = "";
		for(int i = 0; i<disjointClass.length; i++)
		{
			if(i>0)
				confirmationGraphPattern += " UNION ";
			confirmationGraphPattern += "{ ";
			for(int j = 0; j<disjointClass.length; j++)
			{
				if(j==i)
					confirmationGraphPattern += disjointClass[j].graphPattern + "\n";
				else
					confirmationGraphPattern += disjunctionGraphPattern(j, i, "?x", "?y");					
			}
			confirmationGraphPattern += " }";
		}
		numConfirmations = RDFMiner.endpoint.count("?x", confirmationGraphPattern);
		if(numConfirmations>0 && numConfirmations<100)
		{
			// query the confirmations
			RDFMiner.endpoint.select("TO DO");
			while(RDFMiner.endpoint.hasNext())
			{
		    	QuerySolution solution = RDFMiner.endpoint.next();
	    		RDFNode x = solution.get("x");
				confirmations.add(Expression.sparqlEncode(x));
			}
		}
		
		String exceptionGraphPattern = "";
		for(int i = 0; i<disjointClass.length; i++)
			exceptionGraphPattern += disjointClass[i].graphPattern + "\n";
		numExceptions = RDFMiner.endpoint.count("?x", exceptionGraphPattern);
		if(numExceptions>0 && numExceptions<100)
		{
			// query the exceptions
			RDFMiner.endpoint.select("TO DO");
			while(RDFMiner.endpoint.hasNext())
			{
		    	QuerySolution solution = RDFMiner.endpoint.next();
	    		RDFNode x = solution.get("x");
				exceptions.add(Expression.sparqlEncode(x));
			}
		}
	}

}
