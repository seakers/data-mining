/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.mining.moea;

/**
 *
 * @author hsbang
 */


import ifeed.architecture.AbstractArchitecture;
import ifeed.feature.FeatureExpressionHandler;
import ifeed.feature.FeatureFetcher;
import ifeed.feature.Feature;
import ifeed.mining.AbstractDataMiningBase;
import java.util.List;

public abstract class MOEABase extends AbstractDataMiningBase {
    /**
     * Generate the base features and store them
     */

    private FeatureFetcher featureFetcher;
    private FeatureExpressionHandler featureHandler;
    private List<Feature> baseFeatures;
    private RandomFeatureSelector featureSelector;

    public MOEABase(List<AbstractArchitecture> architectures,
                           List<Integer> behavioral, List<Integer> non_behavioral, FeatureFetcher fetcher){

        super(architectures, behavioral, non_behavioral);
        this.featureFetcher = fetcher;
        this.featureHandler = new FeatureExpressionHandler(this.featureFetcher);
        this.baseFeatures = super.generateBaseFeatures();
        this.featureSelector = new RandomFeatureSelector(this.baseFeatures);
    }

    public FeatureFetcher getFeatureFetcher() {
        return this.featureFetcher;
    }

    public FeatureExpressionHandler getFeatureHandler() {
        return this.featureHandler;
    }

    public RandomFeatureSelector getFeatureSelector() {
        return this.featureSelector;
    }

    public List<Feature> getBaseFeatures(){
        return this.baseFeatures;
    }
}
