/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed_dm;

import java.util.BitSet;

/**
 *
 * @author nozomihitomi
 */
public class DrivingFeature2 extends AbstractFeature{
    
    /**
     * Name associated to the feature;
     */
    private final String name;
    
    public DrivingFeature2(String name, BitSet matches) {
        this(name, matches, Double.NaN, Double.NaN, Double.NaN, Double.NaN);
    }

    public DrivingFeature2(String name, BitSet matches, double support, double lift, double fconfidence, double rconfidence) {
        super(matches, support, lift, fconfidence, rconfidence);
        this.name = name;
    }    
    
    public DrivingFeature2(String name) {
        this(name, new BitSet(), Double.NaN, Double.NaN, Double.NaN, Double.NaN);
    }

    public String getName() {
        return name;
    }
    
    
}
