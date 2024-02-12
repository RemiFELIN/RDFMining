/*
Grammatical Evolution in Java
Release: GEVA-v2.0.zip
Copyright (C) 2008 Michael O'Neill, Erik Hemberg, Anthony Brabazon, Conor Gilligan 
Contributors Patrick Middleburgh, Eliott Bartley, Jonathan Hugosson, Jeff Wrigh

Separate licences for asm, bsf, antlr, groovy, jscheme, commons-logging, jsci is included in the lib folder. 
Separate licence for rieps is included in src/com folder.

This licence refers to GEVA-v2.0.

This software is distributed under the terms of the GNU General Public License.


This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
/>.
*/

package com.i3s.app.rdfminer.evolutionary.geva.Operator.selection;

import com.i3s.app.rdfminer.Parameters;
import com.i3s.app.rdfminer.evolutionary.geva.Exceptions.BadParameterException;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.FitnessPackage.Fitness;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.GEIndividual;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.Individual;
import com.i3s.app.rdfminer.evolutionary.geva.Util.Constants;
import com.i3s.app.rdfminer.evolutionary.geva.Util.Random.MersenneTwisterFast;
import com.i3s.app.rdfminer.evolutionary.geva.Util.Random.RandomNumberGenerator;
import com.i3s.app.rdfminer.evolutionary.geva.Util.Random.Stochastic;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * The operation of this class is tournament selection.
 * The individual with the best fitness from a randomly
 * selected tournament of size tournamentSize is cloned to the selected population.
 **/
public class TournamentSelect extends SelectionOperation implements Stochastic {

    private static final Logger logger = Logger.getLogger(TournamentSelect.class.getName());
    
    protected RandomNumberGenerator rng;
    protected int tournamentSize;
    protected double pressureModifier; // p=1 for deterministic selection
    protected ArrayList<Fitness> tour;
    
    /** Creates a new instance of TournamentSelect
     * @param size size of selected population
     * @param tourSize size of tournament
     * @param rand random number generator
     */
    public TournamentSelect(int size, int tourSize, RandomNumberGenerator rand) {
        super(size);
        this.rng = rand;
        this.tournamentSize = tourSize;
        tour = new ArrayList<>(tourSize);
    }
    
    /** Creates a new instance of TournamentSelect
     * @param rand random number generator
     * @param p properties
     */
    public TournamentSelect(RandomNumberGenerator rand, Properties p) {
        super(p);
        setProperties(p);
        this.rng = rand;
        tour = new ArrayList<>(this.tournamentSize);
    }

    /**
     * New instantion
     */
    public TournamentSelect() {
        super();
        Parameters parameters = Parameters.getInstance();
        this.size = (int) (parameters.getSelectionRate() * parameters.getPopulationSize());
        this.rng = new MersenneTwisterFast();
        this.tournamentSize = (int) (parameters.getTournamentSelectionRate() * parameters.getPopulationSize());
        tour = new ArrayList<>(this.tournamentSize);
    }
    
    public void setProperties(Properties p) {
        super.setProperties(p);
        int value;
                
        String key;
        try {
            key = Constants.TOURNAMENT_SIZE;
            value = Integer.parseInt(p.getProperty(key));
            if(value < 1) {
                throw new BadParameterException(key);
            }
        } catch(Exception e) {
            value = 3;
            System.out.println(e+" using default: "+value);
        }
        this.tournamentSize = value;
    }
    
    public void doOperation(GEIndividual operand) {}
    
    /**
     * Individuals from operands will be added to the selected population
     * if the win their tournament.
     * @param operands Individuals to be selected from
     **/
    public void doOperation(List<GEIndividual> operands) {
        this.selectedPopulation.clear();
        int nTournament = 0;
        while(this.selectedPopulation.size()<this.size){
            fillTour(operands);
            selectFromTour();
            nTournament++;
        }
        logger.info(nTournament + " tournament(s) has been performed !");
    }

    /**
     * Adds individual to the tournament by randomly selecting from
     * the operands untill the tounramentSize is reached.
     * @param operands Individuals that can be selected to the tournament
     **/
    public void fillTour(List<GEIndividual> operands) {
        tour.clear();
        int contestant;
        int i=0;
        while(i < this.tournamentSize) {
            contestant = this.rng.nextInt(operands.size());
            Fitness participant = operands.get(contestant).getFitness();
            // if participant is not already into the tournament
            if (!tour.contains(participant)) {
                tour.add(operands.get(contestant).getFitness());
                i++;
            }
        }
    }
    
    /**
     * Select a winner from the tournament and add to the selected population.
     **/
    public void selectFromTour() {
//        System.out.println("Tournament: ");
        tour.sort(Collections.reverseOrder());
//        for(Fitness f : tour) {
//            System.out.println(f.getIndividual().getGenotype() + " ~ f(i)= " + f.getDouble());
//        }
        boolean added = false;
        int winnerIdx = 0;
        // avoid duplicates: if the winner is already into the selection, we'll take the 2nd and so on...
        while (!added) {
            Individual winner = tour.get(winnerIdx).getIndividual();
            if (!this.selectedPopulation.contains(winner)) {
//                System.out.println("Winner is the number " + winnerIdx);
                this.selectedPopulation.add(winner);
                added = true;
            } else {
                winnerIdx++;
            }
            // It is possible that all participants are already in the population !
            if (winnerIdx == tour.size()) {
                break;
            }
        }


    }

    public void setRNG(RandomNumberGenerator m) {
            this.rng = m;
    }

    public RandomNumberGenerator getRNG() {
        return this.rng;
    }

}

