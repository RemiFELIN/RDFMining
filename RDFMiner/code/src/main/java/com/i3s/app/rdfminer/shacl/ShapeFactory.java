package com.i3s.app.rdfminer.shacl;

import com.i3s.app.rdfminer.shacl.type.HasValueShape;

/**
 * Analyze the SHACL Shapes and map it in the appropriate object
 *
 * @author Rémi FELIN
 */
public class ShapeFactory {

    public static Shape create(String shape) {
        return new HasValueShape("tutu", "toto", "tata");
    }

}
