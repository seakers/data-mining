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
import ifeed.feature.AbstractFeatureFetcher;
import ifeed.feature.Feature;
import ifeed.mining.AbstractDataMiningBase;

import java.util.ArrayList;
import java.util.List;
import java.util.BitSet;

public abstract class MOEABase extends AbstractDataMiningBase {
    /**
     * Generate the base features and store them
     */

    private AbstractFeatureFetcher featureFetcher;
    private FeatureExpressionHandler featureHandler;
    private List<Feature> baseFeatures;
    private RandomFeatureSelector featureSelector;
    private List<FeatureRecord> recordedFeatures;

    public MOEABase(List<AbstractArchitecture> architectures,
                           List<Integer> behavioral, List<Integer> non_behavioral, AbstractFeatureFetcher fetcher){

        super(architectures, behavioral, non_behavioral);

        this.baseFeatures = super.generateBaseFeatures();

        this.featureFetcher = fetcher;
        if(this.featureFetcher.getBaseFeatures().isEmpty()){
            this.featureFetcher.setBaseFeatures(this.baseFeatures);
        }
        this.featureHandler = new FeatureExpressionHandler(this.featureFetcher);
        this.featureSelector = new RandomFeatureSelector(this.baseFeatures);
    }

    public AbstractFeatureFetcher getFeatureFetcher() {
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

    public void recordFeature(String name, BitSet matches, double[] objectives){

        int index;

        if(this.recordedFeatures == null){
            this.recordedFeatures = new ArrayList<>();
            index = 0;

        }else{
            index = this.recordedFeatures.size();

        }

        this.recordedFeatures.add(new FeatureRecord(index, name, matches, objectives));
    }

    public List<FeatureRecord> getRecordedFeatures(){
        return this.recordedFeatures;
    }

    public class FeatureRecord{

        private int index;
        private String name;
        private BitSet matches;
        private double[] objectives;

        public FeatureRecord(int index, String name, BitSet matches, double[] objectives){
            this.index = index;
            this.name = name;
            this.matches = matches;
            this.objectives = objectives;
        }

        public int getIndex() {
            return index;
        }

        public String getName() {
            return name;
        }

        public BitSet getMatches() {
            return matches;
        }

        public double[] getObjectives() {
            return objectives;
        }

    }
}