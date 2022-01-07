package com.i3s.app.rdfminer.axiom;

/**
 * This class is used to classify every type of experiments by ID
 * Each ID correspond to a type of axioms (subClassOf, DisjointClasses, ...)
 *
 * @author Rémi FELIN
 */
public class Type {

    public final static int SUBCLASSOF = 1;

    public final static int DISJOINT_CLASSES = 2;

    public final static int EQUIVALENT_CLASSES = 3;

}
