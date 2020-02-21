/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.mining.moea;

/**
 *
 * @author hsbang
 */

import ifeed.feature.Feature;
import ifeed.feature.logic.Connective;

import java.util.List;

public abstract class AbstractRandomFeatureGenerator {

    protected List<Feature> baseFeatures;

    public AbstractRandomFeatureGenerator(List<Feature> baseFeatures){
        this.baseFeatures = baseFeatures;
    }

    public void setBaseFeatures(List<Feature> baseFeatures){
        this.baseFeatures = baseFeatures;
    }

    /**
     * Generates a random feature tree
     * @return
     */
    public abstract Connective generateRandomFeature();
}
