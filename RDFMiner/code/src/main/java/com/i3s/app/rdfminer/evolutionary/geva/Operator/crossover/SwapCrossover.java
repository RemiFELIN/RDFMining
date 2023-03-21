package com.i3s.app.rdfminer.evolutionary.geva.Operator.crossover;

import com.i3s.app.rdfminer.evolutionary.geva.Individuals.GEChromosome;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.GEIndividual;
import com.i3s.app.rdfminer.evolutionary.geva.Util.Constants;
import com.i3s.app.rdfminer.evolutionary.geva.Util.Random.RandomNumberGenerator;
import com.i3s.app.rdfminer.launcher.GrammaticalEvolution;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Properties;
import java.util.Random;

public class SwapCrossover extends CrossoverOperation {


    private static final Logger logger = Logger.getLogger(SinglePointCrossover.class.getName());

    protected boolean fixedCrossoverPoint = false;
    protected boolean codonsUsedSensitive = true;

    public SwapCrossover() {
        super();
    }

    /**
     * Creates a new instance of SwapCrossover
     *
     * @param m    random number generator
     * @param prob crossover probability
     */
    public SwapCrossover(RandomNumberGenerator m, double prob) {
        this(prob, m);
        this.fixedCrossoverPoint = true;
    }

    /**
     * Creates a new instance of SwapCrossover
     *
     * @param m    random number generator
     * @param prob crossover probability
     */
    public SwapCrossover(double prob, RandomNumberGenerator m) {
        super(prob, m);
        this.fixedCrossoverPoint = true;
    }

    /**
     * New instance
     *
     * @param m random number generator
     * @param p properties
     */
    public SwapCrossover(RandomNumberGenerator m, Properties p) {
        super(m, p);
        this.fixedCrossoverPoint = true;
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
            performCrossover(chrom1, chrom2, p1maxXOPoint, p2maxXOPoint);
        }
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
     * Creates the new chromsome with fixed crossver,
     * and returns the crossover points on the individuals.
     *
     * @param c1           Chromsome 1
     * @param c2           Chromsome 2
     * @param p1maxXOPoint Max crossover point Chromsome 1
     * @param p2maxXOPoint Max crossover point Chromsome 2
     **/
    public void performCrossover(final GEChromosome c1, final GEChromosome c2,
                                 final int p1maxXOPoint, final int p2maxXOPoint) {
        final int point1 = getXoverPoint(p1maxXOPoint, p2maxXOPoint);
        int tmp1, tmp2;
        boolean swap = new Random().nextBoolean();
        if (swap) {
            int firstPoint = 1 + new Random().nextInt(c1.size() - 1);
            int lastPoint = c1.size() - firstPoint;
            boolean topLeftToBottomRight = new Random().nextBoolean();
            // Full Swap
            if (topLeftToBottomRight) {
                int[] tmp = new int[firstPoint];
                for (int i = 0; i < firstPoint; i++) {
                    tmp[i] = c1.get(i);
                }
                for (int i = 0; i < firstPoint; i++) {
                    c1.set(i, c2.get(i + lastPoint));
                }
                int j = 0;
                for (int i = lastPoint; i < c2.size(); i++) {
                    c2.set(i, tmp[j]);
                    j++;
                }
            } else {
                //top right to bottom left
                int[] tmp = new int[c1.size() - firstPoint];
                int j = 0;
                for (int i = firstPoint; i < c1.size(); i++) {
                    tmp[j] = c1.get(i);
                    j++;
                }
                j = 0;
                for (int i = firstPoint; i < c1.size(); i++) {
                    c1.set(i, c2.get(j));
                    j++;
                }
                for (int i = 0; i < lastPoint; i++) {
                    c2.set(i, tmp[i]);
                }
            }
        } else {
            // Single Point Crossover
            for (int i = 0; i < point1; i++) {
                tmp1 = c1.get(i);
                tmp2 = c2.get(i);
                c1.set(i, tmp2);
                c2.set(i, tmp1);
            }
        }
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
