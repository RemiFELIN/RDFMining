/**
 * 
 */
package com.i3s.app.rdfminer.sparql;

import org.apache.jena.rdf.model.RDFNode;

/**
 * @author Andrea G. B. Tettamanzi
 *
 */
public class RDFNodePair implements Comparable<RDFNodePair> {
	public RDFNode x, y;

	public RDFNodePair(RDFNode subj, RDFNode obj) {
		x = subj;
		y = obj;
	}

	/**
	 * Implements a lexicographic ordering over the pairs of RDF nodes.
	 */
	@Override
	public int compareTo(RDFNodePair that) {
		String thisX = x != null ? x.toString() : "";
		String thisY = y != null ? y.toString() : "";
		String thatX = that.x != null ? that.x.toString() : "";
		String thatY = that.y != null ? that.y.toString() : "";

		if (thisX.equals(thatX))
			return thisY.compareTo(thatY);
		return thisX.compareTo(thatX);
	}

	public String toString() {
		return "(" + x + ", " + y + ")";
	}
}
