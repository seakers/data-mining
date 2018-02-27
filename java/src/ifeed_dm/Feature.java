/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed_dm;


import java.util.BitSet;

/**
 * A feature that explains data
 *
 * @author bang
 */

public class Feature {

    protected final String name;
    protected final BitSet matches;

    protected final double support;
    protected final double lift;
    protected final double fconfidence;
    protected final double rconfidence;
    protected final double distance2UP;
    private double algebraicComplexity;

    public Feature(String name, BitSet matches, double support, double lift, double fconfidence, double rconfidence, double complexity) {
        this.name = name;
        this.matches = matches;
        this.support = support;
        this.lift = lift;
        this.fconfidence = fconfidence;
        this.rconfidence = rconfidence;
        this.distance2UP = - Math.sqrt(Math.pow(1-fconfidence,2)+Math.pow(1-rconfidence,2));
        this.algebraicComplexity = complexity;
    }

    public Feature(String name, BitSet matches, double support, double lift, double fconfidence, double rconfidence) {
        this(name, matches, support, lift, fconfidence, rconfidence, -1);
    }

    public Feature(String name, BitSet matches) {
        this(name, matches, Double.NaN, Double.NaN, Double.NaN, Double.NaN);
    }

    public Feature(BitSet matches, double support, double lift, double fconfidence, double rconfidence) {
        this(null, matches, support, lift, fconfidence, rconfidence);
    }

    public BitSet getMatches() {
        return matches;
    }

    public String getName() {
        return name;
    }

    /**
     * Gets the support of the feature
     *
     * @return
     */
    public double getSupport() {
        return support;
    }

    /**
     * Gets the forward confidence of the feature. Given that a solution has a
     * feature, what is the likelihood of it also being in target region?
     *
     * @return
     */
    public double getFConfidence() {
        return fconfidence;
    }

    /**
     * Gets the reverse confidence of the feature. Given that a solution is in
     * the target region, what is the likelihood of it also containing feature?
     *
     * @return
     */
    public double getRConfidence() {
        return rconfidence;
    }

    /**
     * Gets the lift of the feature
     *
     * @return
     */
    public double getLift() {
        return lift;
    }

    /**
     * Gets the distance to the utopia point (confidence of 1 in both directions)
     *
     * @return
     */
    public double getDistance2UP() {
        return distance2UP;
    }

    public void setAlgebraicComplexity(double complexity){
        this.algebraicComplexity = complexity;
    }

    public double getAlgebraicComplexity(){
        return this.algebraicComplexity;
    }
}
