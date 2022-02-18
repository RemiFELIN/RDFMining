package com.i3s.app.rdfminer.shacl;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.Load;

/**
 * An abstract class of SHACL Shape
 *
 * @author Rémi FELIN
 */
public abstract class Shape {

    /**
     * The original SHACL Shape as a string
     */
    public final String shape;

    public Shape(String shape) {
        this.shape = shape;
    }

}
