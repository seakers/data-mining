/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.feature;


import java.util.BitSet;

/**
 * A binary feature
 *
 * @author bang
 */

public class Feature {

    protected final String name;
    protected final BitSet matches;
    protected final double support;
    protected final double lift;
    protected final double precision;
    protected final double recall;
    protected final double distance2UP;
    protected double complexity;

    public Feature(String name, BitSet matches, double support, double lift, double precision, double recall, double complexity) {
        this.name = name;
        this.matches = matches;
        this.support = support;
        this.lift = lift;
        this.precision = precision;
        this.recall = recall;
        this.distance2UP = - Math.sqrt(Math.pow(1-precision,2)+Math.pow(1-recall,2));
        this.complexity = complexity;
    }

    public Feature(String name, BitSet matches, double support, double lift, double precision, double recall) {
        this(name, matches, support, lift, precision, recall, -1);
    }

    public Feature(String name, BitSet matches) {
        this(name, matches, Double.NaN, Double.NaN, Double.NaN, Double.NaN);
    }

    public Feature(BitSet matches, double support, double lift, double precision, double recall) {
        this(null, matches, support, lift, precision, recall);
    }

    public Feature copy(){
        Feature copied = new Feature(name, matches, support, lift, precision, recall, complexity);
        return copied;
    }

    public BitSet getMatches() {
        return matches;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString(){ return this.getName(); }

    /**
     * Gets the support of the feature
     *
     * @return
     */
    public double getSupport() {
        return support;
    }

    /**
     * Gets the precision confidence of the feature. Given that a solution has a
     * feature, what is the likelihood of it also being in target region?
     *
     * @return
     */
    public double getPrecision() {
        return precision;
    }

    /**
     * Gets the recall of the feature. Given that a solution is in
     * the target region, what is the likelihood of it also containing feature?
     *
     * @return
     */
    public double getRecall() {
        return recall;
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

    public void setComplexity(double complexity){
        this.complexity = complexity;
    }

    public double getComplexity(){
        return this.complexity;
    }
}
