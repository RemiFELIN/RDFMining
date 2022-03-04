/**
 * 
 */
package com.i3s.app.rdfminer.axiom;

import java.util.ArrayList;
import java.util.List;

import com.i3s.app.rdfminer.RDFMiner;
import com.i3s.app.rdfminer.axiom.type.DisjointClassesAxiom;
import com.i3s.app.rdfminer.axiom.type.SubClassOfAxiom;
import com.i3s.app.rdfminer.grammar.DLFactory;
import com.i3s.app.rdfminer.grammar.evolutionary.individual.GEIndividual;
import com.i3s.app.rdfminer.sparql.virtuoso.VirtuosoEndpoint;

import Mapper.Symbol;
import Util.Enums;

/**
 * The axiom factory singleton class is able to construct axioms of various
 * types.
 * 
 * @author Andrea G. B. Tettamanzi & Rémi FELIN
 *
 */
public class AxiomFactory extends DLFactory {
	
	/**
	 * This class should not be instantiated. Any attempt at calling this
	 * constructor will result in an exception being thrown.
	 */
	private AxiomFactory() {
		throw new RuntimeException("AxiomFactory cannot be instantiated.");
	}

	/**
	 * Creates an axiom from a list of symbols in OWL 2 functional-style syntax.
	 * <p>
	 * The types of axioms that are provided for by OWL 2 are the following 32,
	 * divided in six categories:
	 * </p>
	 * <ul>
	 * <li>class expression axioms:</li>
	 * <ul>
	 * <li><code>SubClassOf</code> axioms;</li>
	 * <li><code>EquivalentClasses</code> axioms;</li>
	 * <li><code>DisjointClasses</code> axioms;</li>
	 * <li><code>DisjointUnion</code> axioms;</li>
	 * </ul>
	 * <li>object property expression axioms:</li>
	 * <ul>
	 * <li><code>SubObjectPropertyOf</code> axioms;</li>
	 * <li><code>EquivalentObjectProperties</code> axioms;</li>
	 * <li><code>DisjointObjectProperties</code> axioms;</li>
	 * <li><code>ObjectPropertyDomain</code> axioms;</li>
	 * <li><code>ObjectPropertyRange</code> axioms;</li>
	 * <li><code>InverseObjectProperties</code> axioms;</li>
	 * <li><code>FunctionalObjectProperty</code> axioms;</li>
	 * <li><code>InverseFunctionalObjectProperty</code> axioms;</li>
	 * <li><code>ReflexiveObjectProperty</code> axioms;</li>
	 * <li><code>IrreflexiveObjectProperty</code> axioms;</li>
	 * <li><code>SymmetricObjectProperty</code> axioms;</li>
	 * <li><code>AsymmetricObjectProperty</code> axioms;</li>
	 * <li><code>TransitiveObjectProperty</code> axioms;</li>
	 * </ul>
	 * <li>data property expression axioms:</li>
	 * <ul>
	 * <li><code>SubDataPropertyOf</code> axioms;</li>
	 * <li><code>EquivalentDataProperties</code> axioms;</li>
	 * <li><code>DisjointDataProperties</code> axioms;</li>
	 * <li><code>DataPropertyDomain</code> axioms;</li>
	 * <li><code>DataPropertyRange</code> axioms;</li>
	 * <li><code>FunctionalDataProperty</code> axioms;</li>
	 * </ul>
	 * <li>datatype definition, identified by the <code>DatatypeDefinition</code>
	 * keyword;</li>
	 * <li>keys axioms, identified by the <code>HasKey</code> keyword;</li>
	 * <li>assertions:</li>
	 * <ul>
	 * <li><code>SameIndividual</code> axioms;</li>
	 * <li><code>DifferentIndividuals</code> axioms;</li>
	 * <li><code>ClassAssertion</code> axioms;</li>
	 * <li><code>ObjectPropertyAssertion</code> axioms;</li>
	 * <li><code>NegativeObjectPropertyAssertion</code> axioms;</li>
	 * <li><code>DataPropertyAssertion</code> axioms;</li>
	 * <li><code>NegativeDataPropertyAssertion</code> axioms.</li>
	 * </ul>
	 * </ul>
	 * <p>
	 * Please refer to the document
	 * <a href="http://www.w3.org/TR/2012/REC-owl2-direct-semantics-20121211/">OWL 2
	 * Web Ontology Language Direct Semantics (Second Edition)</a> for a
	 * model-theoretic semantics of all the types of axioms, compatible with the
	 * description logic <em>SROIQ</em>.
	 * </p>
	 * 
	 * @param syntax an axiom definition in OWL 2 functional-style syntax.
	 * @return the corresponding axiom.
	 */
	public static Axiom create(GEIndividual individual, List<Symbol> syntax, VirtuosoEndpoint endpoint) {

		Axiom axiom = null;
		List<List<Symbol>> arguments = parseArguments(syntax);
		
		if (syntax.get(0).equals("SubClassOf")) {
			require(arguments.size() == 2);
			RDFMiner.type = Type.SUBCLASSOF;
			axiom = new SubClassOfAxiom(arguments.get(0), arguments.get(1), endpoint);
			
		} else if (syntax.get(0).equals("EquivalentClasses")) {
			// TO DO
			RDFMiner.type = Type.EQUIVALENT_CLASSES;

		} else if (syntax.get(0).equals("DisjointClasses")) {
			require(arguments.size() > 1);
			RDFMiner.type = Type.DISJOINT_CLASSES;
			axiom = new DisjointClassesAxiom(arguments, endpoint);
			
		} else if (syntax.get(0).equals("DisjointUnion")) {
			// TO DO
		}
		// object property expression axioms:
		else if (syntax.get(0).equals("SubObjectPropertyOf")) {
			// TO DO
		} else if (syntax.get(0).equals("EquivalentObjectProperties")) {
			// TO DO
		} else if (syntax.get(0).equals("DisjointObjectProperties")) {
			// TO DO
		} else if (syntax.get(0).equals("ObjectPropertyDomain")) {
			// TO DO
		} else if (syntax.get(0).equals("ObjectPropertyRange")) {
			// TO DO
		} else if (syntax.get(0).equals("InverseObjectProperties")) {
			// TO DO
		} else if (syntax.get(0).equals("FunctionalObjectProperty")) {
			// TO DO
		} else if (syntax.get(0).equals("InverseFunctionalObjectProperty")) {
			// TO DO
		} else if (syntax.get(0).equals("ReflexiveObjectProperty")) {
			// TO DO
		} else if (syntax.get(0).equals("IrreflexiveObjectProperty")) {
			// TO DO
		} else if (syntax.get(0).equals("SymmetricObjectProperty")) {
			// TO DO
		} else if (syntax.get(0).equals("AsymmetricObjectProperty")) {
			// TO DO
		} else if (syntax.get(0).equals("TransitiveObjectProperty")) {
			// TO DO
		}
		// data property expression axioms:
		else if (syntax.get(0).equals("SubDataPropertyOf")) {
			// TO DO
		} else if (syntax.get(0).equals("EquivalentDataProperties")) {
			// TO DO
		} else if (syntax.get(0).equals("DisjointDataProperties")) {
			// TO DO
		} else if (syntax.get(0).equals("DataPropertyDomain")) {
			// TO DO
		} else if (syntax.get(0).equals("DataPropertyRange")) {
			// TO DO
		} else if (syntax.get(0).equals("FunctionalDataProperty")) {
			// TO DO
		}
		// datatype definition:
		else if (syntax.get(0).equals("DatatypeDefinition")) {
			// TO DO
		}
		// keys axioms:
		else if (syntax.get(0).equals("HasKey")) {
			// TO DO
		}
		// assertions:
		else if (syntax.get(0).equals("SameIndividual")) {
			// TO DO
		} else if (syntax.get(0).equals("DifferentIndividuals")) {
			// TO DO
		} else if (syntax.get(0).equals("ClassAssertion")) {
			// TO DO
		} else if (syntax.get(0).equals("ObjectPropertyAssertion")) {
			// TO DO
		} else if (syntax.get(0).equals("NegativeObjectPropertyAssertion")) {
			// TO DO
		} else if (syntax.get(0).equals("DataPropertyAssertion")) {
			// TO DO
		} else if (syntax.get(0).equals("NegativeDataPropertyAssertion")) {
			// TO DO
		}
		// if the given individual is not null, we can set the name of the axiom using its individual
		if(individual != null) {
			// set the individual of axiom
			axiom.individual = individual;
			// set the title of axiom
			axiom.axiomId = individual.getPhenotype().getStringNoSpace();
		}
		// set this arguments 
		axiom.argumentClasses = arguments;
		return axiom;
	}

	/**
	 * Creates an axiom from a text string in OWL 2 functional-style syntax.
	 * 
	 * @param str
	 * @return
	 */
	public static Axiom create(GEIndividual individual, String str, VirtuosoEndpoint endpoint) {
		List<Symbol> list = new ArrayList<Symbol>();
		String symbol = "";
		boolean blank = false;
		for (int i = 0; i < str.length(); i++) {
			String token = str.substring(i, i + 1);
			if (blank && token.matches("\\s"))
				continue; // treat contiguous blanks as if they were just one token
			blank = token.matches("\\s");
			if (blank)
				token = " ";
			if (token.equals(" ") || token.equals("(") || token.equals(")")) {
				if (!symbol.isEmpty())
					list.add(new Symbol(symbol, Enums.SymbolType.TSymbol));
				list.add(new Symbol(token, Enums.SymbolType.TSymbol));
				symbol = "";
			} else {
				symbol += token;
			}
		}
		if (!symbol.isEmpty())
			list.add(new Symbol(symbol, Enums.SymbolType.TSymbol));
		// for(int i = 0; i<list.size(); i++)
		// System.out.println("symbol " + i + " = " + list.get(i));
		return create(individual, list, endpoint);
	}

}
