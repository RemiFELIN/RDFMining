/**
 * 
 */
package com.i3s.app.rdfminer.entity.axiom;

import com.i3s.app.rdfminer.entity.axiom.type.DisjointClassesAxiom;
import com.i3s.app.rdfminer.entity.axiom.type.OWLAxiom;
import com.i3s.app.rdfminer.entity.axiom.type.SubClassOfAxiom;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.GEIndividual;
import com.i3s.app.rdfminer.evolutionary.geva.Mapper.Symbol;
import com.i3s.app.rdfminer.evolutionary.geva.Util.Enums;
import com.i3s.app.rdfminer.grammar.DLFactory;
import com.i3s.app.rdfminer.sparql.corese.CoreseEndpoint;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * The axiom factory singleton class is able to construct axioms of various
 * types.
 * 
 * @author Andrea G. B. Tettamanzi & Rémi FELIN
 *
 */
public class AxiomFactory extends DLFactory {

	private static final Logger logger = Logger.getLogger(AxiomFactory.class.getName());

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
	public static Axiom create(GEIndividual individual, List<Symbol> syntax, CoreseEndpoint endpoint) throws URISyntaxException, IOException {

		Axiom axiom = null;
		List<List<Symbol>> arguments = parseArguments(syntax);
//		if(parameters.sparqlTimeOut != 0) {
//			// set timeout
//			endpoint.setTimeout(parameters.sparqlTimeOut);
//		}
		// check syntax
		if (syntax.get(0).equals(OWLAxiom.SUBCLASSOF)) {
			require(arguments.size() == 2);
//			RDFMiner.type = Type.SUBCLASSOF;
			axiom = new SubClassOfAxiom(arguments.get(0), arguments.get(1), endpoint);
			
		} else if (syntax.get(0).equals(OWLAxiom.EQUIVALENTCLASSES)) {
			// TO DO
//			RDFMiner.type = Type.EQUIVALENT_CLASSES;

		} else if (syntax.get(0).equals(OWLAxiom.DISJOINTCLASSES)) {
			require(arguments.size() > 1);
//			RDFMiner.type = Type.DISJOINT_CLASSES;
			axiom = new DisjointClassesAxiom(arguments, endpoint);
			
		} else if (syntax.get(0).equals(OWLAxiom.DISJOINTUNION)) {
			// TO DO
		}
		// object property expression axioms:
		else if (syntax.get(0).equals(OWLAxiom.SUBOBJECTPROPERTYOF)) {
			// TO DO
		} else if (syntax.get(0).equals(OWLAxiom.EQUIVALENTOBJECTPROPERTIES)) {
			// TO DO
		} else if (syntax.get(0).equals(OWLAxiom.DISJOINTOBJECTPROPERTIES)) {
			// TO DO
		} else if (syntax.get(0).equals(OWLAxiom.OBJECTPROPERTYDOMAIN)) {
			// TO DO
		} else if (syntax.get(0).equals(OWLAxiom.OBJECTPROPERTYRANGE)) {
			// TO DO
		} else if (syntax.get(0).equals(OWLAxiom.INVERSEOBJECTPROPERTIES)) {
			// TO DO
		} else if (syntax.get(0).equals(OWLAxiom.FUNCTIONALOBJECTPROPERTY)) {
			// TO DO
		} else if (syntax.get(0).equals(OWLAxiom.INVERSEFUNCTIONALOBJECTPROPERTY)) {
			// TO DO
		} else if (syntax.get(0).equals(OWLAxiom.REFLEXIVEOBJECTPROPERTY)) {
			// TO DO
		} else if (syntax.get(0).equals(OWLAxiom.IRREFLEXIVEOBJECTPROPERTY)) {
			// TO DO
		} else if (syntax.get(0).equals(OWLAxiom.SYMMETRICOBJECTPROPERTY)) {
			// TO DO
		} else if (syntax.get(0).equals(OWLAxiom.ASYMMETRICOBJECTPROPERTY)) {
			// TO DO
		} else if (syntax.get(0).equals(OWLAxiom.TRANSITIVEOBJECTPROPERTY)) {
			// TO DO
		}
		// data property expression axioms:
		else if (syntax.get(0).equals(OWLAxiom.SUBDATAPROPERTYOF)) {
			// TO DO
		} else if (syntax.get(0).equals(OWLAxiom.EQUIVALENTDATAPROPERTIES)) {
			// TO DO
		} else if (syntax.get(0).equals(OWLAxiom.DISJOINTDATAPROPERTIES)) {
			// TO DO
		} else if (syntax.get(0).equals(OWLAxiom.DATAPROPERTYDOMAIN)) {
			// TO DO
		} else if (syntax.get(0).equals(OWLAxiom.DATAPROPERTYRANGE)) {
			// TO DO
		} else if (syntax.get(0).equals(OWLAxiom.FUNCTIONALDATAPROPERTY)) {
			// TO DO
		}
		// datatype definition:
		else if (syntax.get(0).equals(OWLAxiom.DATATYPEDEFINITION)) {
			// TO DO
		}
		// keys axioms:
		else if (syntax.get(0).equals(OWLAxiom.HASKEY)) {
			// TO DO
		}
		// assertions:
		else if (syntax.get(0).equals(OWLAxiom.SAMEINDIVIDUAL)) {
			// TO DO
		} else if (syntax.get(0).equals(OWLAxiom.DIFFERENTINDIVIDUALS)) {
			// TO DO
		} else if (syntax.get(0).equals(OWLAxiom.CLASSASSERTION)) {
			// TO DO
		} else if (syntax.get(0).equals(OWLAxiom.OBJECTPROPERTYASSERTION)) {
			// TO DO
		} else if (syntax.get(0).equals(OWLAxiom.NEGATIVEOBJECTPROPERTYASSERTION)) {
			// TO DO
		} else if (syntax.get(0).equals(OWLAxiom.DATAPROPERTYASSERTION)) {
			// TO DO
		} else if (syntax.get(0).equals(OWLAxiom.NEGATIVEDATAPROPERTYASSERTION)) {
			// TO DO
		} else {
			// log error syntax
			logger.warn(syntax.get(0));
			logger.warn("This entity is not conform, it didn't match any type of OWL Axioms !");
			return null;
		}
		// if the given individual is not null, we can set the name of the axiom using its individual
		// set the individual of axiom
		axiom.individual = individual;
		// set the title of axiom
//			axiom.setEntityAsString(individual.getPhenotype().getString());
		// set this arguments
		axiom.argumentClasses = arguments;
		return axiom;
	}

	/**
	 * Creates an axiom from a text string in OWL 2 functional-style syntax.
	 */
	public static Axiom create(GEIndividual individual, String str, CoreseEndpoint endpoint) throws URISyntaxException, IOException {
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
