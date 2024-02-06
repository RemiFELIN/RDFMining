package com.i3s.app.rdfminer.evolutionary.types;

public class TypeCrossover {
	
	public final static int SINGLE_POINT = 1;
	public final static int TWO_POINT = 2;
	public final static int SUBTREE = 3;
	public final static int SWAP = 4;

	public static String getLabel(int typeCrossover) {
		switch (typeCrossover) {
			case SINGLE_POINT:
				return "Single Point";
			default:
			case TWO_POINT:
				return "Two Point";
			case SUBTREE:
				return "Subtree";
			case SWAP:
				return "Swap";
		}
	}
}
