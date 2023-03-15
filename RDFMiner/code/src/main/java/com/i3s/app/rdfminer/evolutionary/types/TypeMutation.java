package com.i3s.app.rdfminer.evolutionary.types;

public class TypeMutation {

    public final static int INT_FLIP = 1;
    public final static int INT_FLIP_BYTE = 2;
    public final static int NODAL = 3;
    public final static int SUBTREE = 4;
//    public final static int STRUCTURAL = 5;

    public static String getLabel(int typeMutation) {
        switch (typeMutation) {
            default:
            case INT_FLIP:
                return "Int Flip";
            case INT_FLIP_BYTE:
                return "Int Flip Byte";
            case NODAL:
                return "Nodal";
            case SUBTREE:
                return "Subtree";
        }
    }


}
