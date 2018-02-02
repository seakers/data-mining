/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed_dm;

import ifeed_dm.Feature;
import java.util.BitSet;

/**
 * An abstract class for a feature that explains the data
 *
 * @author nozomihitomi
 */
public class BaseFeature implements Feature {
    /**
     * The bitset for the observations this feature matches
     */

    private final String name;
    
    private final BitSet matches;
    private final double support;
    private final double lift;
    private final double fconfidence;
    private final double rconfidence;
    private final double distance2UP;

    public BaseFeature(String name, BitSet matches, double support, double lift, double fconfidence, double rconfidence) {
        this.name = name;
        this.matches = matches;
        this.support = support;
        this.lift = lift;
        this.fconfidence = fconfidence;
        this.rconfidence = rconfidence;
        this.distance2UP = - Math.sqrt(Math.pow(1-fconfidence,2)+Math.pow(1-rconfidence,2));
    }
    
    
    public BaseFeature(BitSet matches, double support, double lift, double fconfidence, double rconfidence) {
        this(null, matches, support, lift, fconfidence, rconfidence);
    }    
    
    
    public BaseFeature(String name, BitSet matches) {
        this(name, matches, Double.NaN, Double.NaN, Double.NaN, Double.NaN);
    }    
    
    
    public BitSet getMatches() {
        return matches;
    }
    
    @Override
    public double getSupport() {
        return support;
    }

    @Override
    public double getFConfidence() {
        return fconfidence;
    }

    @Override
    public double getRConfidence() {
        return rconfidence;
    }
    
    @Override
    public double getDistance2UP() {
        return distance2UP;
    }
    

    @Override
    public double getLift() {
        return lift;
    }
    

    public String getName() {
        return name;
    }
}
