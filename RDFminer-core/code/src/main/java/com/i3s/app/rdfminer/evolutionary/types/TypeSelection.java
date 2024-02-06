package com.i3s.app.rdfminer.evolutionary.types;

public class TypeSelection {

    /**
     * a fixed percentage of the best individuals from the current population are selected based on their
     * fitness scores.
     */
//    public final static int ELITE_OPERATION_SELECTION = 1;

    /**
     *  Proportional roulette wheel selection constructs a roulette wheel, where the size of the pie
     *  slice assigned to each individual is proportional to its fitness score. Individuals with higher
     *  fitness scores receive larger slices of the wheel, while those with lower fitness scores get
     *  smaller slices. This creates a representation of the population's diversity in terms of fitness.
     */
    public final static int PROPORTIONAL_ROULETTE_WHEEL = 1;

    /**
     * By applying fitness scaling, it allows for a more dynamic adjustment of selection probabilities,
     * potentially emphasizing the differences in fitness among individuals. This can be useful when dealing
     * with problems where fitness values vary widely or when it's necessary to control the exploration-exploitation
     * trade-off in the search process.
     */
    public final static int SCALED_ROULETTE_WHEEL = 2;

    /**
     *  It involves organizing small "tournaments" among individuals in the population to select the best candidates
     *  for the next generation. In this context, it is deterministic.
     */
    public final static int TOURNAMENT_SELECT = 3;

    public static String getLabel(int typeSelection) {
        switch (typeSelection) {
//            default:
//            case ELITE_OPERATION_SELECTION:
//                return "Elite Operation";
            case PROPORTIONAL_ROULETTE_WHEEL:
                return "Proportional Roulette Wheel";
            case SCALED_ROULETTE_WHEEL:
                return "Scaled Roulette Wheel";
            default:
            case TOURNAMENT_SELECT:
                return "Tournament";
        }
    }

}