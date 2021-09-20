/**
 * 
 */
package com.i3s.app.rdfminer.expression.complement;

import com.i3s.app.rdfminer.expression.Expression;

/**
 * An expression of the form ObjectComplementOf(...) or DataComplementOf(...).
 * 
 * @author Andrea G. B. Tettamanzi
 *
 */
public class ComplementExpression extends Expression {

	/**
	 * Creates a new, empty complement expression.
	 */
	public ComplementExpression() {
		super();
	}

	/**
	 * Instantiates the graph pattern for this complement expression.
	 * 
	 * @param subject the expression replacing the <tt>?x</tt> SPARQL variable
	 * @param object  ignored
	 * @return the SPARQL graph pattern for this expression
	 */
	@Override
	public String createGraphPattern(String subject, String object) {
		String dc = getFreshVariableName();
		String z = getFreshVariableName();
		return "{ " + subject + " a " + dc + " .\n" + "FILTER NOT EXISTS { " + z + " a " + dc + " .\n"
				+ subExpressions.get(0).createGraphPattern(z, getFreshVariableName()) + "\n}\n}";
	}

	/**
	 * Asks the SPARQL endpoint whether this expression contains the given RDF node.
	 * <p>
	 * Class <code>ObjectComplementOf</code>(<var>C</var>) contains node
	 * <var>x</var> if there exists a class <var>D</var> such that
	 * <code>DisjointClasses<code>(<var>C</var>, <var>D</var>) and <var>D</var>
	 * contains <var>x</var>.
	 * </p>
	 * 
	 * @param node an RDF node.
	 * @return true if the given node is a member of the extension of this
	 *         expression.
	 */
//	@Override
//	public boolean contains(ComparableRDFNode node)
//	{
//		boolean foundDisjointClass = false;
//		// Consider the class C this class is the complement of
//		Expression c = subExpressions.get(0);
//		
//		// Consider all classes D the given node is an instance of and
//		// try to find one which is disjoint from C...
//		Set<ComparableRDFNode> concepts = node.getContainingClasses();
//		Iterator<ComparableRDFNode> i = concepts.iterator();
//		while(i.hasNext() && !foundDisjointClass)
//		{
//			ComparableRDFNode d = i.next();
//			// Skip class C, which we know already is not disjoint...
//			if(d.toString().equals(c.toString()))
//				continue;
//			
//			// check whether C and D are disjoint
//			// C and D are disjoint if !exists x in C : x in D, i.e., for all x in C, !(x in D)
//			Iterator<ComparableRDFNode> j = c.extension().iterator();
//			boolean dMayBeDisjoint = true;
//			while(j.hasNext() && dMayBeDisjoint)
//			{
//				ComparableRDFNode instanceOfC = j.next();
//				String str = // RDFMiner.PREFIXES +
//						"ASK { <" + instanceOfC + "> a <" + d + "> }";
//				Query query = QueryFactory.create(str);
//				QueryExecution qe = QueryExecutionFactory.sparqlService(RDFMiner.dbpedia, query);
//	
//				try
//				{
//				    dMayBeDisjoint = !qe.execAsk();
//				}
//				catch(Exception e)
//				{ 
//				    logger.error(e.getMessage());
//					logger.info("Querying DBpedia about the membership of " + instanceOfC + " in " + d + " with query\n" + str);
//				    throw e;
//				}
//				finally {
//				    qe.close();
//				}
//			}
//			// If D may still be disjoint from C after having looked at
//			// all instances of C and found none that belongs in D,
//			// then we have found a class D the node is an instance of
//			// that is disjoint from C.
//			foundDisjointClass = dMayBeDisjoint;
//			if(foundDisjointClass)
//				logger.info("Found a disjoint class: " + d);
//		}
//		return foundDisjointClass;
//		// return !subExpressions.get(0).contains(node);
//	}

}
