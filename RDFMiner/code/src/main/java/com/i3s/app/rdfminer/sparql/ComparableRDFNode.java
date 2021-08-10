/**
 * 
 */
package com.i3s.app.rdfminer.sparql;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.apache.jena.graph.Node;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.RDFVisitor;
import org.apache.jena.rdf.model.Resource;
import org.apache.log4j.Logger;

import com.i3s.app.rdfminer.RDFMiner;

/**
 * An adapter of RDFNode to make it comparable.
 * 
 * @see org.apache.jena.graph.RDFNode
 * @author Andrea G. B. Tettamanzi
 *
 */
public class ComparableRDFNode implements RDFNode, Comparable<Object> {
	
	private static Logger logger = Logger.getLogger(ComparableRDFNode.class.getName());

	/**
	 * A reference to the actual instance of an RDF node encapsulated by this
	 * adapter.
	 */
	protected RDFNode node;
	
	public SparqlEndpoint endpoint;
	
	/**
	 * Creates a comparable RDF node from an existing RDF node.
	 */
	public ComparableRDFNode(RDFNode node, SparqlEndpoint endpoint) {
		this.node = node;
		this.endpoint = endpoint;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jena.graph.FrontsNode#asNode()
	 */
	@Override
	public Node asNode() {
		return node.asNode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jena.rdf.model.RDFNode#isAnon()
	 */
	@Override
	public boolean isAnon() {
		return node.isAnon();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jena.rdf.model.RDFNode#isLiteral()
	 */
	@Override
	public boolean isLiteral() {
		return node.isLiteral();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jena.rdf.model.RDFNode#isURIResource()
	 */
	@Override
	public boolean isURIResource() {
		return node.isURIResource();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jena.rdf.model.RDFNode#isResource()
	 */
	@Override
	public boolean isResource() {
		return node.isResource();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jena.rdf.model.RDFNode#as(java.lang.Class)
	 */
	@Override
	public <T extends RDFNode> T as(Class<T> view) {
		return node.as(view);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jena.rdf.model.RDFNode#canAs(java.lang.Class)
	 */
	@Override
	public <T extends RDFNode> boolean canAs(Class<T> view) {
		return node.canAs(view);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jena.rdf.model.RDFNode#getModel()
	 */
	@Override
	public Model getModel() {
		return node.getModel();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.jena.rdf.model.RDFNode#inModel(org.apache.jena.rdf.model.Model)
	 */
	@Override
	public RDFNode inModel(Model m) {
		return node.inModel(m);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jena.rdf.model.RDFNode#visitWith(org.apache.jena.rdf.model.
	 * RDFVisitor)
	 */
	@Override
	public Object visitWith(RDFVisitor rv) {
		return node.visitWith(rv);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jena.rdf.model.RDFNode#asResource()
	 */
	@Override
	public Resource asResource() {
		return node.asResource();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jena.rdf.model.RDFNode#asLiteral()
	 */
	@Override
	public Literal asLiteral() {
		return node.asLiteral();
	}

	@Override
	public String toString() {
		return node.toString();
	}

	/**
	 * Compares this RDF node to another object.
	 * <p>
	 * The comparison is made by transforming both this RDF node and the compared
	 * object into their natural string representations by means of their
	 * <code>toString</code> method and then by comparing those strings.
	 * 
	 * @param that an object to be compared to
	 * @return the result of the comparison
	 */
	@Override
	public int compareTo(Object that) {
		return node.toString().compareTo(that.toString());
	}

	/**
	 * Returns the set of all concepts this RDF node is an explicit instance of.
	 * <p>
	 * The result of this method consists of all the RDF nodes <var>C</var> such
	 * that a triple <var>this node</var> <code>rdf:type</code> <var>C</var> exists.
	 * 
	 * @return the set of all concepts this RDF node is an explicit instance of
	 */
	public Set<ComparableRDFNode> getContainingClasses() {
		Set<ComparableRDFNode> result = new TreeSet<ComparableRDFNode>();

		String str = "DISTINCT ?class WHERE {\n" + "\t<" + node + "> a ?class .\n"
				+ "\tFILTER ( ?class != owl:Thing )\n" + "}";
		logger.info("Querying SPARQL endpoint for the containing classes of " + node + " with query:\n" + str);
		endpoint.select(str, 0);
		while (endpoint.hasNext()) {
			QuerySolution solution = endpoint.next();
			Iterator<String> i = solution.varNames();
			if (i.hasNext()) {
				String varName = i.next();
				RDFNode node = solution.get(varName);
				result.add(new ComparableRDFNode(node, endpoint));
			}
		}
		logger.info("Found " + result.size() + " classes.");
		return result;
	}

	@Override
	public boolean isStmtResource() {
		// TODO Auto-generated method stub
		return false;
	}

}
