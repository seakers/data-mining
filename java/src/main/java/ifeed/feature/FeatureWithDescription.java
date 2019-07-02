/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.feature;


import java.util.BitSet;

/**
 * A feature that explains data
 *
 * @author bang
 */

public class FeatureWithDescription extends Feature {

    protected String description;

    public FeatureWithDescription(String name, BitSet matches, double support, double lift, double precision, double recall, double complexity, String description) {
        super(name, matches, support, lift, precision, recall, complexity);
        this.description = description;
    }

    public FeatureWithDescription(String name, BitSet matches, double support, double lift, double precision, double recall, String description) {
        super(name, matches, support, lift, precision, recall, -1);
        this.description = description;
    }

    public FeatureWithDescription(String name, BitSet matches, String description) {
        super(name, matches, Double.NaN, Double.NaN, Double.NaN, Double.NaN);
        this.description = description;
    }

    public FeatureWithDescription(BitSet matches, double support, double lift, double precision, double recall, String description) {
        super(null, matches, support, lift, precision, recall);
        this.description = description;
    }

    public FeatureWithDescription(Feature feature, String description){
        super(feature.getName(), feature.getMatches(), feature.getSupport(), feature.getLift(), feature.getPrecision(), feature.getRecall(), feature.getComplexity());
        this.description = description;
    }

    public String getDescription(){ return this.description; }

    public void setDescription(String description){ this.description = description; }
}
