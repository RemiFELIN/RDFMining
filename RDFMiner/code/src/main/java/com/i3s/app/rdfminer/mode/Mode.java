package com.i3s.app.rdfminer.mode;

/**
 * Describe the mode used to manage the processing.
 *
 * @author Rémi FELIN
 */
public class Mode {

    public TypeMode type;

    /**
     * Set a mode instance using TypeMode enum.
     *
     * @param type a value of TypeMode enum
     */
    public Mode(TypeMode type) {
        this.type = type;
    }

}
