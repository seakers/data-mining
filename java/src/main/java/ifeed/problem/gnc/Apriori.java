/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.gnc;

import ifeed.architecture.AbstractArchitecture;
import ifeed.filter.AbstractFilter;
import ifeed.local.params.BaseParams;
import ifeed.mining.arm.AbstractApriori;
import ifeed.mining.arm.AbstractAssociationRuleMining;
import ifeed.feature.Feature;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

//import ifeed.featureselection.MRMR;
import java.util.BitSet;
import java.util.List;
import java.util.stream.IntStream;


/**
 *
 * @author bang
 */


public class Apriori extends AbstractApriori {

    public Apriori(BaseParams params, List<AbstractArchitecture> architectures, List<Integer> behavioral, List<Integer> non_behavioral,
                   double supp, double conf, double lift) {
        super(params, architectures, behavioral, non_behavioral, supp, conf, lift);
    }

    @Override
    public List<AbstractFilter> generateCandidates(){
        return new FeatureGenerator(super.params).generateCandidates();
    }


//    public List<Feature> runLocalSearch(Connective root){
//        List<Feature> baseFeatures = super.generateBaseFeatures(false);
//        return this.runLocalSearch(root, baseFeatures);
//    }
//
//    /**
//     * Runs local Search that extends a given feature
//     *
//     * @param root
//     *
//     * */
//    public List<Feature> runLocalSearch(Connective root, List<Feature> baseFeatures){
//
//        long t0 = System.currentTimeMillis();
//
//        System.out.println("Local Search initiated");
//
//        List<Feature> minedFeatures = new ArrayList<>();
//
//        // Add a base feature to the given feature, replacing the placeholder
//        for(Feature feature:baseFeatures){
//
//            // Define which feature will be add to the current placeholder location
//            root.setNewNode(feature.getName(), feature.getMatches());
//
//            BitSet matches = root.getMatches();
//
//            double[] metrics = Utils.computeMetrics(matches,this.labels,super.population.size());
//
//            if(Double.isNaN(metrics[0])){
//                continue;
//            }
//
//            String name = root.getName();
//
//            Feature newFeature = new Feature(name, matches, metrics[0], metrics[1], metrics[2], metrics[3]);
//
//            minedFeatures.add(newFeature);
//        }
//
//        FeatureMetricComparator comparator1 = new FeatureMetricComparator(FeatureMetric.FCONFIDENCE);
//        FeatureMetricComparator comparator2 = new FeatureMetricComparator(FeatureMetric.RCONFIDENCE);
//        List<Comparator> comparators = new ArrayList<>(Arrays.asList(comparator1,comparator2));
//
//        List<Feature> extracted_features = Utils.getFeatureFuzzyParetoFront(minedFeatures,comparators,0);
//
//        long t1 = System.currentTimeMillis();
//        System.out.println("...[Apriori] Total features found: " + minedFeatures.size() + ", Pareto front: " + extracted_features.size());
//        System.out.println("...[Apriori] Total data mining time : " + String.valueOf(t1 - t0) + " msec");
//
//        return extracted_features;
//    }
//
//
//    public List<Feature> runLocalSearch(String featureName, List<Integer> archsWithFeature){
//
//        BitSet matches = new BitSet(super.architectures.size());
//
//        for (int i = 0; i < super.architectures.size(); i++) {
//
//            DiscreteInputArchitecture a = super.architectures.get(i);
//            if (archsWithFeature.contains(a.getID())){
//                matches.set(i);
//            }
//        }
//
//        Feature feature = new Feature(featureName, matches);
//
//        return runLocalSearch(feature);
//    }
//
//
//    public List<Feature> runLocalSearch(Feature feature){
//
//        long t0 = System.currentTimeMillis();
//
//        System.out.println("Local Search initiated");
//
//        List<Feature> baseFeatures = super.generateBaseFeatures(false);
//
//        System.out.println("...[Apriori] The number of candidate features: " + baseFeatures.size());
//        System.out.println("...[Apriori] Local Search root feature name: " + feature.getName());
//
//        baseFeatures.add(feature);
//
//        AbstractApriori ap = new AbstractApriori(super.population.size(), baseFeatures, labels);
//        ap.run(baseFeatures.size()-1,super.support_threshold, super.confidence_threshold, ARMParams.maxLength);
//
//        List<Feature> mined_features = ap.exportFeatures();
//
//        FeatureMetricComparator comparator1 = new FeatureMetricComparator(FeatureMetric.FCONFIDENCE);
//        FeatureMetricComparator comparator2 = new FeatureMetricComparator(FeatureMetric.RCONFIDENCE);
//        List<Comparator> comparators = new ArrayList<>(Arrays.asList(comparator1,comparator2));
//
//        List<Feature> extracted_features = Utils.getFeatureFuzzyParetoFront(mined_features,comparators,0);
//
//        extracted_features = Utils.getTopFeatures(extracted_features, ARMParams.max_number_of_features_before_mRMR);
//
//
//        long t1 = System.currentTimeMillis();
//
//        System.out.println("...[Apriori] Total features found: " + mined_features.size() + ", Pareto front: " + extracted_features.size());
//        System.out.println("...[Apriori] Total data mining time : " + String.valueOf(t1 - t0) + " msec");
//
//        return extracted_features;
//
//    }
//

}