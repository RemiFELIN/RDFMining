package com.i3s.app.rdfminer.shacl.type;

import com.i3s.app.rdfminer.shacl.Shape;

/**
 * A specific SHACL Shape where a <code>sh:targetClass</code> must have a <code>sh:hasValue</code>
 * object for a given <code>sh:path</code> property.<br><br>
 *
 * Meaning : for a class <code>A</code>, I must observe the linked object <code>B</code>
 * for a <code>P</code> property.
 *
 * @author Rémi FELIN
 */
public class HasValueShape extends Shape {

    /**
     * The value of <code>sh:targetClass</code>
     */
    public String targetClass;

    /**
     * The value of <code>sh:path</code>
     */
    public String path;

    /**
     * The value of <code>sh:hasValue</code>
     */
    public String hasValue;

    public HasValueShape(String targetClass, String path, String hasValue) {
        this.targetClass = targetClass;
        this.path = path;
        this.hasValue = hasValue;
    }

}
