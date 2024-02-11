/**
 * 
 */
package com.i3s.app.rdfminer.expression.extensional;

import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import com.i3s.app.rdfminer.evolutionary.geva.Mapper.Symbol;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;

import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.sparql.RDFNodePair;
import com.i3s.app.rdfminer.sparql.virtuoso.VirtuosoEndpoint;

/**
 * A class expression of the form DataOneOf(...).
 * 
 * @author Andrea G. B. Tettamanzi
 *
 */
public class ExtensionalDatatypeExpression extends ExtensionalExpression {

	/**
	 * Creates a new extensional datatype expression based on the provided
	 * functional-style syntax.
	 * <p>
	 * This expression will be the set of the literals given as its subexpressions.
	 * </p>
	 * <p>
	 * The extension of this expression is pre-compiled based on the arguments of
	 * the <code>DataOneOf</code> constructor.
	 * </p>
	 * 
	 * @param syntax an expression in OWL 2 functional-style syntax.
	 */
	public ExtensionalDatatypeExpression(List<List<Symbol>> syntax) {
		super();
		rootSymbol = "DataOneOf (";
		extension = new TreeSet<RDFNodePair>();
		VirtuosoEndpoint endpoint = new VirtuosoEndpoint(Global.SPARQL_ENDPOINT, Global.PREFIXES);
		Model m = endpoint.tdb;
		Iterator<List<Symbol>> i = syntax.iterator();
		
		while (i.hasNext()) {
			RDFNode r;
			Symbol sym = i.next().get(0);
			rootSymbol += " " + sym;
			// We create an RDF node from the symbol
			String s = sym.getSymbolString();
			if (s.startsWith("<")) {
				// this should never happen !
				throw new RuntimeException("Resource node in an enumerative datatype!");
			}
			r = m.createLiteral(s);
			extension.add(new RDFNodePair(r, null));
		}
		
		rootSymbol += " )";
		graphPattern = createGraphPattern("?x", "?y");
	}

	/**
	 * Instantiates the graph pattern for this extensional datatype expression.
	 * 
	 * @param subject the expression replacing the <tt>?x</tt> SPARQL variable
	 * @param object  this expression is ignored
	 * @return the SPARQL graph pattern for this expression
	 */
	@Override
	public String createGraphPattern(String subject, String object) {
		String s = getFreshVariableName();
		String p = getFreshVariableName();
		return s + " " + p + " " + subject + " .\n" + getFilter(subject, false) + " .\n";
	}

}
