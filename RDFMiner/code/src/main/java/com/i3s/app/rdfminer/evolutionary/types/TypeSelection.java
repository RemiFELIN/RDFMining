package com.i3s.app.rdfminer.evolutionary.types;

public class TypeSelection {

    public final static int ELITE_OPERATION_SELECTION = 1;
    public final static int PROPORTIONAL_ROULETTE_WHEEL = 2;
    public final static int SCALED_ROULETTE_WHEEL = 3;
    public final static int TOURNAMENT_SELECT = 4;

    public static String getLabel(int typeSelection) {
        switch (typeSelection) {
            default:
            case ELITE_OPERATION_SELECTION:
                return "Elite Operation";
            case PROPORTIONAL_ROULETTE_WHEEL:
                return "Proportional Roulette Wheel";
            case SCALED_ROULETTE_WHEEL:
                return "Scaled Roulette Wheel";
            case TOURNAMENT_SELECT:
                return "Tournament";
        }
    }

}