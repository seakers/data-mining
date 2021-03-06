/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.mining.arm;

import ifeed.architecture.AbstractArchitecture;
import ifeed.local.params.BaseParams;
import ifeed.mining.AbstractDataMiningAlgorithm;
import ifeed.mining.AbstractDataMiningBase;
import ifeed.local.params.ARMParams;
import ifeed.feature.Feature;

import java.util.*;

/**
 *
 * @author bang
 */

public abstract class AbstractAssociationRuleMining extends AbstractDataMiningBase implements AbstractDataMiningAlgorithm {

    /**
     * Thresholds used for association rule mining
     */
    protected double support_threshold;
    protected double confidence_threshold;
    protected double lift_threshold;
    protected double [] thresholds;

    public AbstractAssociationRuleMining(BaseParams params,
                                         List<AbstractArchitecture> architectures,
                                         List<Integer> behavioral,
                                         List<Integer> non_behavioral,
                                         double supp, double conf, double lift){

        super(params, architectures, behavioral, non_behavioral);

        // Initialize threshold values
        this.support_threshold = supp;
        this.confidence_threshold = conf;
        this.lift_threshold = lift;
        this.thresholds = new double[3];
        thresholds[0] = support_threshold;
        thresholds[1] = lift_threshold;
        thresholds[2] = confidence_threshold;
    }

    public double getSupportThreshold(){return this.support_threshold;}
    public double getConfidenceThreshold(){return this.confidence_threshold;}
    public double getLiftThreshold(){return this.lift_threshold;}

    public List<Feature> adjustBaseFeatureSize(List<Feature> baseFeatures){

        ArrayList<Feature> reducedFeatureList = new ArrayList<>();

        ArrayList<Integer> addedFeatureIndices = new ArrayList<>();
        double[] bounds = new double[2];
        bounds[0] = 0;
        bounds[1] = (double) super.behavioral.size() / this.samples.size();

        int minRuleNum = ARMParams.minRuleNum;
        int maxRuleNum = ARMParams.maxRuleNum;
        int maxIter = ARMParams.adjustRuleSizeMaxIter;
        double adaptSupp = (double) this.behavioral.size() / this.samples.size() * 0.5; // 1/2 of the maximum possible support

        int iter = 0;
        while (addedFeatureIndices.size() < minRuleNum || addedFeatureIndices.size() > maxRuleNum) {

            if (iter > maxIter) {
                break;

            } else if (iter > 0) {
                // max supp threshold is support_S
                // min supp threshold is 0

                double a;
                if (addedFeatureIndices.size() > maxRuleNum) { // Too many rules -> increase threshold
                    bounds[0] = adaptSupp; // Set the minimum bound to the current level
                    a = bounds[1];
                } else { // too few rules -> decrease threshold
                    bounds[1] = adaptSupp;
                    a = bounds[0];
                }
                // Bisection
                adaptSupp = (double) (adaptSupp + a) * 0.5;
            }

            addedFeatureIndices = new ArrayList<>();
            for (int i = 0; i < baseFeatures.size(); i++) {

                // For each feature
                Feature feature = baseFeatures.get(i);

                // Check if each feature has the minimum support and count the number
                if (feature.getSupport() > adaptSupp) {
                    addedFeatureIndices.add(i);
                    if (addedFeatureIndices.size() > maxRuleNum) {
                        break;
                    } else if ((baseFeatures.size() - (i + 1)) + addedFeatureIndices.size() < minRuleNum) {
                        break;
                    }
                }
            }

            iter++;
        }

        this.support_threshold = adaptSupp;
        System.out.println("...["+ this.getClass().getSimpleName() +"] Adjusted the support threshold in " + iter + " steps with rule size: " + addedFeatureIndices.size());

        for (int ind : addedFeatureIndices) {
            reducedFeatureList.add(baseFeatures.get(ind));
        }

        return reducedFeatureList;
    }
}
