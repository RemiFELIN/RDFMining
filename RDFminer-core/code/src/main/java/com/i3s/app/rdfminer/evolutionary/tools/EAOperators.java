package com.i3s.app.rdfminer.evolutionary.tools;

import com.i3s.app.rdfminer.Parameters;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.GEIndividual;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.Individual;
import com.i3s.app.rdfminer.evolutionary.geva.Operator.crossover.SinglePointCrossover;
import com.i3s.app.rdfminer.evolutionary.geva.Operator.crossover.SubtreeCrossover;
import com.i3s.app.rdfminer.evolutionary.geva.Operator.crossover.SwapCrossover;
import com.i3s.app.rdfminer.evolutionary.geva.Operator.crossover.TwoPointCrossover;
import com.i3s.app.rdfminer.evolutionary.geva.Operator.mutation.IntFlipByteMutation;
import com.i3s.app.rdfminer.evolutionary.geva.Operator.mutation.IntFlipMutation;
import com.i3s.app.rdfminer.evolutionary.geva.Operator.mutation.NodalMutation;
import com.i3s.app.rdfminer.evolutionary.geva.Operator.mutation.SubtreeMutation;
import com.i3s.app.rdfminer.evolutionary.geva.Operator.selection.EliteOperationSelection;
import com.i3s.app.rdfminer.evolutionary.geva.Operator.selection.ProportionalRouletteWheel;
import com.i3s.app.rdfminer.evolutionary.geva.Operator.selection.ScaledRouletteWheel;
import com.i3s.app.rdfminer.evolutionary.geva.Operator.selection.TournamentSelect;
import com.i3s.app.rdfminer.evolutionary.types.TypeCrossover;
import com.i3s.app.rdfminer.evolutionary.types.TypeMutation;
import com.i3s.app.rdfminer.evolutionary.types.TypeSelection;

import java.util.ArrayList;

public class EAOperators {

    public static ArrayList<GEIndividual> getElitesFromPopulation(ArrayList<GEIndividual> population) {
        ArrayList<GEIndividual> elites = new ArrayList<>();
        // find the best individuals
        EliteOperationSelection eos = new EliteOperationSelection();
        eos.doOperation(population);
        for(Individual elite : eos.getSelectedPopulation().getAll()) {
            elites.add((GEIndividual) elite);
        }
        return elites;
    }

    /**
     * Find the individuals to select, depending on the selection mod
     * @param population the entire population
     * @return selected population
     */
    public ArrayList<GEIndividual> getSelectionFromPopulation(ArrayList<GEIndividual> population) {
        Parameters parameters = Parameters.getInstance();
        ArrayList<GEIndividual> selectedPopulation = new ArrayList<>();
        switch(parameters.getSelectionType()) {
            default:
            case TypeSelection.PROPORTIONAL_ROULETTE_WHEEL:
                ProportionalRouletteWheel prw = new ProportionalRouletteWheel();
                prw.doOperation(population);
                for(Individual selected : prw.getSelectedPopulation().getAll()) {
                    selectedPopulation.add((GEIndividual) selected);
                }
                break;
            case TypeSelection.SCALED_ROULETTE_WHEEL:
                ScaledRouletteWheel srw = new ScaledRouletteWheel();
                srw.doOperation(population);
                for(Individual selected : srw.getSelectedPopulation().getAll()) {
                    selectedPopulation.add((GEIndividual) selected);
                }
                break;
            case TypeSelection.TOURNAMENT_SELECT:
                TournamentSelect ts = new TournamentSelect();
                ts.doOperation(population);
                for(Individual selected : ts.getSelectedPopulation().getAll()) {
                    selectedPopulation.add((GEIndividual) selected);
                }
                break;
        }
        return selectedPopulation;
    }

    public void crossover(ArrayList<GEIndividual> couple) {
        Parameters parameters = Parameters.getInstance();
        switch (parameters.getCrossoverType()) {
            default:
            case TypeCrossover.SINGLE_POINT:
                // Single-point crossover
                SinglePointCrossover spc = new SinglePointCrossover();
                spc.setFixedCrossoverPoint(true);
                spc.doOperation(couple);
                break;
            case TypeCrossover.TWO_POINT:
                // Two point crossover
                TwoPointCrossover tpc = new TwoPointCrossover();
                tpc.setFixedCrossoverPoint(true);
                tpc.doOperation(couple);
                break;
            case TypeCrossover.SUBTREE:
                // subtree crossover
                // special implementation due to the original implementation by GEVA developers
                SubtreeCrossover stc = new SubtreeCrossover();
                stc.doOperation(couple);
                break;
            case TypeCrossover.SWAP:
                // Swap crossover
                // contribution testing for ShaMPA
                SwapCrossover swp = new SwapCrossover();
                swp.doOperation(couple);
                break;
        }
    }

    public void mutation(ArrayList<GEIndividual> couple) {
        Parameters parameters = Parameters.getInstance();
        switch (parameters.getMutationType()) {
            default:
            case TypeMutation.INT_FLIP:
                IntFlipMutation ifm = new IntFlipMutation();
                ifm.doOperation(couple);
                break;
            case TypeMutation.NODAL:
                NodalMutation nm = new NodalMutation();
                nm.doOperation(couple);
                break;
            case TypeMutation.SUBTREE:
                SubtreeMutation sm = new SubtreeMutation();
                sm.doOperation(couple);
                break;
            case TypeMutation.INT_FLIP_BYTE:
                IntFlipByteMutation ifbm = new IntFlipByteMutation();
                ifbm.doOperation(couple);
                break;
        }
    }

}
