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

package com.i3s.app.rdfminer.evolutionary.geva.Operator.crossover;

import com.i3s.app.rdfminer.evolutionary.geva.Individuals.GEChromosome;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.GEIndividual;
import com.i3s.app.rdfminer.evolutionary.geva.Util.Constants;
import com.i3s.app.rdfminer.evolutionary.geva.Util.Random.RandomNumberGenerator;
import com.i3s.app.rdfminer.launcher.GrammaticalEvolution;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Properties;

/**
 * Single point crossover.
 *
 * @author Blip
 * @version 2007.0305
 */
public class SinglePointCrossover extends CrossoverOperation {

    private static final Logger logger = Logger.getLogger(SinglePointCrossover.class.getName());

    protected boolean fixedCrossoverPoint = false;
    protected boolean codonsUsedSensitive = true;

    public SinglePointCrossover() {
        super();
    }

    /**
     * Creates a new instance of SinglePointCrossover
     *
     * @param m    random number generator
     * @param prob crossover probability
     */
    @SuppressWarnings({"SameParameterValue"})
    public SinglePointCrossover(RandomNumberGenerator m, double prob) {
        this(prob, m);
    }

    /**
     * Creates a new instance of SinglePointCrossover
     *
     * @param m    random number generator
     * @param prob crossover probability
     */
    @SuppressWarnings({"SameParameterValue"})
    public SinglePointCrossover(double prob, RandomNumberGenerator m) {
        super(prob, m);
    }

    /**
     * New instance
     *
     * @param m random number generator
     * @param p properties
     */
    public SinglePointCrossover(RandomNumberGenerator m, Properties p) {
        super(m, p);
        this.setProperties(p);
    }

    /**
     * Set properties
     *
     * @param p object containing properties
     */
    @Override
    public void setProperties(Properties p) {
        super.setProperties(p);
        String value;
        boolean b = false;
        String key;
        try {
            key = Constants.FIXED_POINT_CROSSOVER;
            value = p.getProperty(key);
            if (value != null) {
                if (value.equals(Constants.TRUE)) {
                    b = true;
                }
            }
        } catch (Exception e) {
            System.out.println(this.getClass().getName() + ".setProperties " + e + " using default: " + b);
        }
        this.fixedCrossoverPoint = b;
        //CodonUsed sensitive
        b = false;
        key = Constants.CODONS_USED_SENSITIVE;
        value = p.getProperty(key);
        if (value != null) {
            if (value.equals(Constants.TRUE)) {
                b = true;
            }
        }
        this.codonsUsedSensitive = b;

    }

    public void doOperation(GEIndividual operands) {
    }

    /**
     * Performes crossover on the 2 first individuals in the incoming list.
     * Depending on the crossover probability.
     *
     * @param operands Individuals to crossover
     **/
    public void doOperation(List<GEIndividual> operands) {
        GEIndividual p1, p2;
        GEChromosome chrom1, chrom2;
        int p1maxXOPoint, p2maxXOPoint;
        if (this.rand.nextDouble() < this.probability) {
            // increase crossover counter
            GrammaticalEvolution.nCrossover++;
//            logger.info("~ perform crossover between " + operands.get(0).getGenotype() + " and " + operands.get(1).getGenotype());
            p1 = operands.get(0);
            p2 = operands.get(1);
            chrom1 = (GEChromosome) p1.getGenotype().get(0);
            chrom2 = (GEChromosome) p2.getGenotype().get(0);
            p1maxXOPoint = this.getMaxXOPoint(p1);
            p2maxXOPoint = this.getMaxXOPoint(p2);
            performCrossover(p1, p2, chrom1, chrom2, p1maxXOPoint, p2maxXOPoint);
        }
    }

    /**
     * This method actually performs the crossover and returns an int
     * array with the crossover points.
     *
     * @param indiv1            The first individual
     * @param indiv2            The second individual
     * @param chrome1           The first individual's chromosome
     * @param chrome2           The second individual's chromosome
     * @param chrome1MaxXOPoint The first Individual's maximum crossover point
     * @param chrome2MaxXOPoint The second Individual's maximum crossover point
     * @return int[] The crossover points for each individual
     */
    public int[] performCrossover(GEIndividual indiv1,
                                  GEIndividual indiv2,
                                  GEChromosome chrome1,
                                  GEChromosome chrome2,
                                  int chrome1MaxXOPoint,
                                  int chrome2MaxXOPoint) {

        // Save the crossover points
        int[] xoPoints = this.makeNewChromosome(chrome1,
                chrome2,
                chrome1MaxXOPoint,
                chrome2MaxXOPoint);
        // Clear the old parent IDs
        indiv1.getParentUIDs().clear();
        indiv2.getParentUIDs().clear();

        if (xoPoints[0] == 0)
            indiv1.getParentUIDs().add(indiv2.getUID());
        else {
            indiv1.getParentUIDs().add(indiv1.getUID());
            indiv1.getParentUIDs().add(indiv2.getUID());
        }

        if (xoPoints[1] == 0)
            indiv2.getParentUIDs().add(indiv1.getUID());
        else {
            indiv2.getParentUIDs().add(indiv2.getUID());
            indiv2.getParentUIDs().add(indiv1.getUID());
        }

        return xoPoints;
    }

    /**
     * Get xover max point based on used codons.
     * If used codons are 0 or not codon use sensitive
     * chromosone length is returned (all are legal).
     *
     * @param i GEIndividual to get find the max XO point
     * @return int Max crossover point
     */
    protected int getMaxXOPoint(final GEIndividual i) {
        final int chromsomeSize = i.getGenotype().get(0).getLength();
        // Mod for wrapping
        int maxXOpoint = i.getPreviouslyUsedCodons() % chromsomeSize;
        if (maxXOpoint <= 0 || !this.codonsUsedSensitive) {
            maxXOpoint = chromsomeSize;
        }
        //System.out.println(maxXOpoint+" mL:"+chromsomeSize+" cU:"+i.getPreviouslyUsedCodons());
        return maxXOpoint;
    }

    /**
     * Get the crossover point within the shortest of the incoming chromosomes
     *
     * @param length1 Chromsome length 1
     * @param length2 Chromsome length 2
     * @return The crossover point
     **/
    protected int getXoverPoint(final int length1, final int length2) {
        final int crossoverPoint;
        if (length1 == 2 && length2 == 2) {
            crossoverPoint = 1;
        } else if (length1 < length2) {
            crossoverPoint = rand.nextInt(length1);
        } else {
            crossoverPoint = rand.nextInt(length2);
        }
        return crossoverPoint;
    }

    /**
     * Creates the new chromsome, with fixed crossver point or not,
     * and returns the crossover points on the individuals.
     *
     * @param c1           Chromsome 1
     * @param c2           Chromsome 2
     * @param p1maxXOPoint Max crossover point Chromsome 1
     * @param p2maxXOPoint Max crossover point Chromsome 2
     * @return int[] Crossover points for both individuals
     **/
    public int[] makeNewChromosome(final GEChromosome c1, final GEChromosome c2,
                                   final int p1maxXOPoint, final int p2maxXOPoint) {
        final int point1, point2;
        int[] xoPoints = new int[2];


        if (this.fixedCrossoverPoint) {
            point1 = getXoverPoint(p1maxXOPoint, p2maxXOPoint);

            xoPoints[0] = point1;
            xoPoints[1] = point1;

            int tmp1, tmp2;
            for (int i = 0; i < point1; i++) {
                tmp1 = c1.get(i);
                tmp2 = c2.get(i);
                c1.set(i, tmp2);
                c2.set(i, tmp1);
            }
        } else {
            point1 = this.rand.nextInt(p1maxXOPoint);
            point2 = this.rand.nextInt(p2maxXOPoint);
            xoPoints[0] = point1;
            xoPoints[1] = point2;

            int[] tmp1 = c1.toArray();
            int[] tmp2 = c2.toArray();

            c1.clear();
            c2.clear();

            for (int i = 0; i < point1; i++) {
                c1.add(tmp1[i]);
            }
            for (int i = point2; i < tmp2.length; i++) {
                c1.add(tmp2[i]);
            }
            for (int i = 0; i < point2; i++) {
                c2.add(tmp2[i]);
            }
            for (int i = point1; i < tmp1.length; i++) {
                c2.add(tmp1[i]);
            }
        }

        return xoPoints;
    }

    /**
     * Chech is the crossover point is fixed
     *
     * @return true if crossover point is fixed
     */
    public boolean isFixedCrossoverPoint() {
        return fixedCrossoverPoint;
    }

    /**
     * Set crossover point to be fixed (same on both chromsomes) or not fixed
     *
     * @param fixedCrossoverPoint crossverpoint fixation
     */
    @SuppressWarnings({"SameParameterValue"})
    public void setFixedCrossoverPoint(boolean fixedCrossoverPoint) {
        this.fixedCrossoverPoint = fixedCrossoverPoint;
    }

}
