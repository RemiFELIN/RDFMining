package com.i3s.app.rdfminer.entity.axiom.type;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * List of 32 OWL Axioms provided by W3C
 * @author Rémi FELIN
 */
public abstract class OWLAxiom {

    public final static String SUBCLASSOF = "SubClassOf";
    public final static String EQUIVALENTCLASSES = "EquivalentClasses";
    public final static String DISJOINTCLASSES = "DisjointClasses";
    public final static String DISJOINTUNION = "DisjointUnion";
    public final static String SUBOBJECTPROPERTYOF = "SubObjectPropertyOf";
    public final static String EQUIVALENTOBJECTPROPERTIES = "EquivalentObjectProperties";
    public final static String DISJOINTOBJECTPROPERTIES = "DisjointObjectProperties";
    public final static String OBJECTPROPERTYDOMAIN = "ObjectPropertyDomain";
    public final static String OBJECTPROPERTYRANGE = "ObjectPropertyRange";
    public final static String INVERSEOBJECTPROPERTIES = "InverseObjectProperties";
    public final static String FUNCTIONALOBJECTPROPERTY = "FunctionalObjectProperty";
    public final static String INVERSEFUNCTIONALOBJECTPROPERTY = "InverseFunctionalObjectProperty";
    public final static String REFLEXIVEOBJECTPROPERTY = "ReflexiveObjectProperty";
    public final static String IRREFLEXIVEOBJECTPROPERTY = "IrreflexiveObjectProperty";
    public final static String SYMMETRICOBJECTPROPERTY = "SymmetricObjectProperty";
    public final static String ASYMMETRICOBJECTPROPERTY = "AsymmetricObjectProperty";
    public final static String TRANSITIVEOBJECTPROPERTY = "TransitiveObjectProperty";
    public final static String SUBDATAPROPERTYOF = "SubDataPropertyOf";
    public final static String EQUIVALENTDATAPROPERTIES = "EquivalentDataProperties";
    public final static String DISJOINTDATAPROPERTIES = "DisjointDataProperties";
    public final static String DATAPROPERTYDOMAIN = "DataPropertyDomain";
    public final static String DATAPROPERTYRANGE = "DataPropertyRange";
    public final static String FUNCTIONALDATAPROPERTY = "FunctionalDataProperty";
    public final static String DATATYPEDEFINITION = "DatatypeDefinition";
    public final static String HASKEY = "HasKey";
    public final static String SAMEINDIVIDUAL = "SameIndividual";
    public final static String DIFFERENTINDIVIDUALS = "DifferentIndividuals";
    public final static String CLASSASSERTION = "ClassAssertion";
    public final static String OBJECTPROPERTYASSERTION = "ObjectPropertyAssertion";
    public final static String NEGATIVEOBJECTPROPERTYASSERTION = "NegativeObjectPropertyAssertion";
    public final static String DATAPROPERTYASSERTION = "DataPropertyAssertion";
    public final static String NEGATIVEDATAPROPERTYASSERTION = "NegativeDataPropertyAssertion";

    public static ArrayList<String> getList() {
        return new ArrayList<>(
                Arrays.asList(OWLAxiom.SUBCLASSOF, OWLAxiom.EQUIVALENTCLASSES, OWLAxiom.DISJOINTCLASSES,
                        OWLAxiom.DISJOINTUNION, OWLAxiom.SUBOBJECTPROPERTYOF, OWLAxiom.EQUIVALENTOBJECTPROPERTIES,
                        OWLAxiom.DISJOINTOBJECTPROPERTIES, OWLAxiom.OBJECTPROPERTYDOMAIN, OWLAxiom.OBJECTPROPERTYRANGE,
                        OWLAxiom.INVERSEOBJECTPROPERTIES, OWLAxiom.FUNCTIONALOBJECTPROPERTY,
                        OWLAxiom.INVERSEFUNCTIONALOBJECTPROPERTY, OWLAxiom.REFLEXIVEOBJECTPROPERTY,
                        OWLAxiom.IRREFLEXIVEOBJECTPROPERTY, OWLAxiom.SYMMETRICOBJECTPROPERTY,
                        OWLAxiom.ASYMMETRICOBJECTPROPERTY, OWLAxiom.TRANSITIVEOBJECTPROPERTY,
                        OWLAxiom.SUBDATAPROPERTYOF, OWLAxiom.EQUIVALENTDATAPROPERTIES, OWLAxiom.DISJOINTDATAPROPERTIES,
                        OWLAxiom.DATAPROPERTYDOMAIN, OWLAxiom.DATAPROPERTYRANGE, OWLAxiom.FUNCTIONALDATAPROPERTY,
                        OWLAxiom.DATATYPEDEFINITION, OWLAxiom.HASKEY, OWLAxiom.SAMEINDIVIDUAL,
                        OWLAxiom.DIFFERENTINDIVIDUALS, OWLAxiom.CLASSASSERTION, OWLAxiom.OBJECTPROPERTYASSERTION,
                        OWLAxiom.NEGATIVEOBJECTPROPERTYASSERTION, OWLAxiom.DATAPROPERTYASSERTION,
                        OWLAxiom.NEGATIVEDATAPROPERTYASSERTION)
        );
    }

}
