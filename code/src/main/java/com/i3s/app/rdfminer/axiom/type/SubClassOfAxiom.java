/**
 * 
 */
package com.i3s.app.rdfminer.axiom.type;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.log4j.Logger;

import com.i3s.app.rdfminer.RDFMiner;
import com.i3s.app.rdfminer.axiom.Axiom;
import com.i3s.app.rdfminer.expression.Expression;
import com.i3s.app.rdfminer.expression.ExpressionFactory;
import com.i3s.app.rdfminer.expression.complement.ComplementClassExpression;
import com.i3s.app.rdfminer.sparql.RDFNodePair;
import com.i3s.app.rdfminer.sparql.SparqlEndpoint;
import com.i3s.app.rdfminer.tools.TimeMap;

//import com.hp.hpl.jena.query.QuerySolution;
//import com.hp.hpl.jena.rdf.model.RDFNode;

import Mapper.Symbol;

/**
 * A class that represents a <code>SubClassOf</code> axiom.
 * 
 * @author Andrea G. B. Tettamanzi
 *
 */
public class SubClassOfAxiom extends Axiom
{
	
	private static Logger logger = Logger.getLogger(SubClassOfAxiom.class.getName());
	
	/**
	 * The subclass expression.
	 */
	protected Expression subClass;
	
	/**
	 * The superclass expression.
	 */
	protected Expression superClass;
	
	/**
	 * The complement expression of the superclass.
	 */
	protected Expression superClassComplement;
	
	/**
	 * The time predictor for this axiom.
	 */
	protected long timePredictor;

	/**
	 * A map to hold the maximum test time observed so far for an accepted SubClassOf axiom.
	 */
	public static TimeMap maxTestTime = new TimeMap();
	
	/**
	 * Create a new <code>SubClassOf</code> object expression axiom from the two given concept expressions.
	 * 
	 * @param subClassExpression the functional-style expression of the subclass
	 * @param superClassExpression the functional-style expression of the superclass
	 */
	public SubClassOfAxiom(List<Symbol> subClassExpression,	List<Symbol> superClassExpression)
	{
		subClass = ExpressionFactory.createClass(subClassExpression);
		superClass = ExpressionFactory.createClass(superClassExpression);
		if(superClass instanceof ComplementClassExpression)
			// Handle the double negation in an optimized way:
			superClassComplement = superClass.subExpressions.get(0);
		else
			superClassComplement = new ComplementClassExpression(superClass);
		
		System.out.println("\nsub-class = " + subClass + "; graph pattern =");
		System.out.println(SparqlEndpoint.prettyPrint(subClass.graphPattern));
		
		System.out.println("\nsuper-class = " + superClass + "; graph pattern =");
		System.out.println(SparqlEndpoint.prettyPrint(superClass.graphPattern));
		
		System.out.println("\n~super-class = " + superClassComplement + "; graph pattern =");
		System.out.println(SparqlEndpoint.prettyPrint(superClassComplement.graphPattern));
		
		try
		{
			update();
		}
		catch(IllegalStateException e)
		{
			// This is the conventional unchecked exception thrown by the
			// Sparql endpoint if an HTTP 504 Gateway Time-out occurs.
			// In that case, we try a slower, but safer, naive update as the last resort:
			logger.warn("Trying a naive update: this is going to take some time...");
			naive_update();
		}
	}
	
	/**
	 * Updates the counts used to compute the possibility and necessity degrees.
	 * <p>According to the model-theoretic semantics, an axiom of the form
	 * <code>SubClassOf(CE<sub>1</sub> CE<sub>2</sub>)</code>
	 * is satisfied if
	 * <i>(CE<sub>1</sub>)<sup>C</sup></i> &sube; <i>(CE<sub>2</sub>)<sup>C</sup></i>.</p>
	 * <p>Therefore,</p>
	 * <ul>
	 * <li>the universe of discourse is the extension of <code>CE<sub>1</sub></code>;</li>
	 * <li>confirmations are RDF nodes <var>x</var> such that
	 *     <var>x</var> &in; (CE<sub>2</sub>)<sup>C</sup>;</li>
	 * <li>exceptions are RDF nodes <var>x</var> such that
	 *     <var>x</var> &in; (<code>ComplementOf</code>(CE<sub>2</sub>))<sup>C</sup>.</li>
	 * </ul>
	 * <p>This method, which is provided as a fallback in case {@link #update()} does
	 * not work, uses a much slower, but hopefully safer, way of updating the counts
	 * than the {@link #update()} method. The extension of the sub-class is retrieved,
	 * than each individual instance is checked for membership in the super-class and
	 * in the complement of the super-class by issuing two ASK queries.</p>
	 * 
	 */
	public void naive_update()
	{
		referenceCardinality = numConfirmations = numExceptions = 0;
		confirmations = new ArrayList<String>();
		exceptions = new ArrayList<String>();
		Set<RDFNodePair> extension = subClass.extension();
		
		int numIntersectingClasses = RDFMiner.endpoint.count("?D", subClass.graphPattern + " ?x a ?D . ");
		timePredictor = referenceCardinality*numIntersectingClasses;

		Iterator<RDFNodePair> i = extension.iterator();
	    while(i.hasNext())
	    {
	    	referenceCardinality++;
	    	RDFNodePair pair = i.next();
    		if(superClass.contains(pair))
    		{
    			numConfirmations++;
    			confirmations.add(Expression.sparqlEncode(pair.x));
    		}
    		// The following is correct, but not optimized (a lot of duplicated tests)
    		else if(superClassComplement.contains(pair))
    		{
    			numExceptions++;
    			exceptions.add(Expression.sparqlEncode(pair.x));
    			logger.info("Found exception: " + pair);
    		}
    		// A better idea would be to issue a SPARQL query
    		// and let the SPARQL endpoint do the work: see the method below...
	    }
	    logger.info("Reference cardinality: " + referenceCardinality + ", " +
	    		numConfirmations + " confirmation(s), " +
	    		numExceptions + " exception(s).");
	}
	
	/**
	 * Updates the counts used to compute the possibility and necessity degrees.
	 * <p>According to the model-theoretic semantics, an axiom of the form
	 * <code>SubClassOf(CE<sub>1</sub> CE<sub>2</sub>)</code>
	 * is satisfied if
	 * <i>(CE<sub>1</sub>)<sup>C</sup></i> &sube; <i>(CE<sub>2</sub>)<sup>C</sup></i>.</p>
	 * <p>Therefore,</p>
	 * <ul>
	 * <li>the universe of discourse is the extension of <code>CE<sub>1</sub></code>;</li>
	 * <li>confirmations are RDF nodes <var>x</var> such that
	 *     <var>x</var> &in; (CE<sub>2</sub>)<sup>C</sup>;</li>
	 * <li>exceptions are RDF nodes <var>x</var> such that
	 *     <var>x</var> &in; (<code>ComplementOf</code>(CE<sub>2</sub>))<sup>C</sup>.</li>
	 * </ul>
	 * <p>The updating of the counts is performed by issuing three SPARQL queries
	 * of the form <code>SELECT count(DISTINCT ?x) AS</code> <var>n</var> <code>WHERE</code>.
	 * If the number of confirmations or exceptions is not too large (currently,
	 * below 100), they are downloaded from the SPARQL endpoint and stored in a
	 * list.</p>
	 * <p>The {@link #naive_update()} method provides a slower, but hopefully safer,
	 * way of updating the counts.</p>
	 */
	@Override
	public void update()
	{
		confirmations = new ArrayList<String>();
		exceptions = new ArrayList<String>();
		referenceCardinality = RDFMiner.endpoint.count("?x", subClass.graphPattern);
		int numIntersectingClasses = RDFMiner.endpoint.count("?D", subClass.graphPattern + " ?x a ?D . ");
		logger.warn("No. of Intersecting Classes = " + numIntersectingClasses);
		timePredictor = referenceCardinality*numIntersectingClasses;
		logger.warn("Time Predictor = " + timePredictor);
		numConfirmations = RDFMiner.endpoint.count("?x",
				subClass.graphPattern + "\n" + superClass.graphPattern);
		if(numConfirmations>0 && numConfirmations<100)
		{
			// query the confirmations
			RDFMiner.endpoint.select("DISTINCT ?x WHERE { " +
				subClass.graphPattern + "\n" + superClass.graphPattern + " }");
			while(RDFMiner.endpoint.hasNext())
			{
		    	QuerySolution solution = RDFMiner.endpoint.next();
	    		RDFNode x = solution.get("x");
				confirmations.add(Expression.sparqlEncode(x));
			}
		}
		if(numConfirmations==referenceCardinality)
		{
			// No need to count the exceptions: there can't be any!
			numExceptions = 0;
			return;
		}
		// Since the query to count exception is complex and may take very long to execute,
		// we execute it with the user-supplied time out.
		try
		{
			if(RDFMiner.parameters.timeOut>0 || RDFMiner.parameters.dynTimeOut!=0.0)
			{
				// Compute the time-out (in minutes):
				long timeOut = RDFMiner.parameters.timeOut;
				timeOut += (long) Math.round(RDFMiner.parameters.dynTimeOut*timePredictor);
				logger.warn("Time Out = " + timeOut);
				
				// Prepare the call to be spawned as a new thread:
				Future<Integer> future = RDFMiner.executor.submit(new Callable<Integer>()
						{
							public Integer call()
							{
								logger.info("Starting exceptions query...");
								return new Integer(RDFMiner.endpoint.count("?x",
										subClass.graphPattern + "\n" + superClassComplement.graphPattern));
							}
						});
				
				// Here, we assume that the contract of this method w.r.t. the semantics of the time-out
				// is the same as the wait() method of class Object, i.e., a time-out of zero means no time-out.
				numExceptions = future.get(timeOut, TimeUnit.MINUTES);
				// If no exceptions are raised
				logger.info("Exceptions query finished - time: " + SparqlEndpoint.selectResponseTime + "");
			}
			else
			{
				logger.warn("Time Out = 0");
				// This is the EKAW 2014 version, without time-out:
				numExceptions = RDFMiner.endpoint.count("?x",
						subClass.graphPattern + "\n" + superClassComplement.graphPattern);
				// Log the response time
				logger.info("Exceptions query finished - time: " + SparqlEndpoint.selectResponseTime + "");
			}
			if(numExceptions>0 && numExceptions<100)
			{
				// retrieve the exceptions
				RDFMiner.endpoint.select("DISTINCT ?x WHERE { " +
					subClass.graphPattern + "\n" + superClassComplement.graphPattern + " }");
				while(RDFMiner.endpoint.hasNext())
				{
			    	QuerySolution solution = RDFMiner.endpoint.next();
		    		RDFNode x = solution.get("x");
					exceptions.add(Expression.sparqlEncode(x));
				}
			}
		}
		catch(InterruptedException | TimeoutException e)
		{
			// If the query times out, it is very likely that it would end up
			// having a large number of exceptions. Therefore, we take the reference
			// cardinality minus the number of confirmations as the conventional
			// number of exceptions in this case.
			// We take the same action also in case of interruption.
			if(e instanceof TimeoutException) {
				logger.warn("Timeout is reached");
			} else {
				logger.warn("The thread has been interrupted");
			}
			numExceptions = referenceCardinality - numConfirmations;
			// If numExceptions E ]0,100[ then we must make a simple query ("closed world")
			// to get all the exceptions with this method
			RDFMiner.endpoint.select("DISTINCT ?x WHERE { " +
					subClass.graphPattern + "\nFILTER NOT EXISTS {\n" + superClass.graphPattern + " \n}\n}");
			while(RDFMiner.endpoint.hasNext())
			{
		    	QuerySolution solution = RDFMiner.endpoint.next();
	    		RDFNode x = solution.get("x");
				exceptions.add(Expression.sparqlEncode(x));
			}
			// Specify isTimeout for this axiom
			isTimeout = true;
		}
		catch(ExecutionException e)
		{
			Throwable cause = e.getCause();
			if(cause instanceof IllegalStateException)
				throw (IllegalStateException) cause;
			throw new RuntimeException(cause);
		}
	} 

	/**
	 * Return the time predictor for this axiom.
	 * @return the value of the time predictor.
	 */
	public long timePredictor()
	{
		return timePredictor;
	}

}
