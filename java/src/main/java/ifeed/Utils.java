/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed;

import com.google.common.collect.Multiset;
import ifeed.feature.Feature;
import ifeed.feature.FeatureMetricComparator;
import ifeed.feature.FeatureMetric;

import java.util.*;

/**
 *
 * @author bang
 */
public class Utils {

    public static int getMultisetHashCode(Multiset<Integer> set){
        int hash = 17;
        for(int i: set){
            hash = 37 * hash + i;
        }
        return hash;
    }

    public static int[] intCollection2Array(Collection<Integer> input){
        int[] out = new int[input.size()];
        int index = 0;
        for(int i:input){
            out[index] = i;
            index++;
        }
        return out;
    }
    
    /**
     * Gets the top n features according to the specified metric in descending
     * order. If n is greater than the number of features found by AbstractApriori, all
     * features will be returned.
     *
     * @param n the number of features desired
     * @param metric the metric used to sort the features
     * @return the top n features according to the specified metric in
     * descending order
     */
    
    public static List<Feature> getTopFeatures(List<Feature> features, int n, FeatureMetric metric) {
    
        Collections.sort(features, new FeatureMetricComparator(metric).reversed());
        
        return getTopFeatures(features,n);
    }

    public static List<Feature> getTopFeatures(List<Feature> features, int n) {

        List<Feature> subList;
        
        if (features.size() > n) {
            
            subList = features.subList(0, n);

        }else{
            subList = features;
        }        

        return subList;
    }

    public static boolean dominates(Feature f1, Feature f2, List<Comparator> comparators){
    
        boolean at_least_as_good_as = true;
        boolean better_than_in_one = false;
        
        for(int i=0;i<comparators.size();i++){

            if(comparators.get(i).compare(f1, f2) > 0){
                // First better than Second
                better_than_in_one=true;

            }else if(comparators.get(i).compare(f1, f2) < 0){
                // First is worse than Second
                at_least_as_good_as = false;
            }
        }

        return at_least_as_good_as && better_than_in_one; // First dominates Second
    }

    
    public static List<Feature> getFeatureFuzzyParetoFront(List<Feature> population, List<Comparator> comparators, int paretoRank){
        
        List<Feature> fuzzy_pareto_front = new ArrayList<>();
        
        ArrayList<Feature> current_population = new ArrayList<>();

        for (Feature f:population) {
            current_population.add(f);
        }
        
        int iteration=0;
        while(iteration <= paretoRank){
            
            ArrayList<Feature> features_next_iter = new ArrayList<>();
        
            for(int i=0;i<current_population.size();i++){

                boolean dominated = false;
                Feature f1 = current_population.get(i);

                for(int j=0;j<current_population.size();j++){
                    
                    if(i==j) continue;
                    
                    Feature f2 = current_population.get(j);
                    if(dominates(f2,f1,comparators)){ // f1 is dominated
                        dominated=true;
                        break;
                    }
                }

                if(!dominated){
                    fuzzy_pareto_front.add(f1);
                }else{
                    features_next_iter.add(f1);
                }

            }
            
            current_population = features_next_iter;
            
            iteration++;
        }

        return fuzzy_pareto_front;
    }

    public static double[] computeMetricsSetNaNZero(BitSet feature, BitSet labels, int numberOfObservations) {
        double[] out = new double[4];

        BitSet copyMatches = (BitSet) feature.clone();
        copyMatches.and(labels);
        double cnt_SF = (double) copyMatches.cardinality();
        out[0] = cnt_SF / (double) numberOfObservations; //support

        //compute the confidence and lift
        double cnt_S = (double) labels.cardinality();
        double cnt_F = (double) feature.cardinality();

        out[1] = (cnt_SF / cnt_S) / (cnt_F / (double) numberOfObservations); //lift
        out[2] = (cnt_SF) / (cnt_F);   // confidence (feature -> selection)
        out[3] = (cnt_SF) / (cnt_S);   // confidence (selection -> feature)

        if(cnt_F < 0.0001){
            cnt_F = 0;
            out[2] = 0;
            out[1] = 0;
        }

        if(cnt_S < 0.0001){
            cnt_S = 0;
            out[3] = 0;
            out[1] = 0;
        }

        return out;
    }

    public static double[] computeMetrics(BitSet feature, BitSet labels, int numberOfObservations){
        return computeMetrics(feature,labels,numberOfObservations,-1);
    }

    /**
     * Computes the metrics of a feature. The feature is represented as the
     * bitset that specifies which base features define it. If the support
     * threshold is not met, then the other metrics are not computed.
     *
     * @param feature the bit set specifying which base features define it
     * @param labels the behavioral/non-behavioral labeling
     * @return a 4-tuple containing support, lift, fcondfidence, and
     * rconfidence. If the support threshold is not met, all metrics will be NaN
     */
    public static double[] computeMetrics(BitSet feature, BitSet labels, int numberOfObservations, double supportThreshold) {
        double[] out = new double[4];

        BitSet copyMatches = (BitSet) feature.clone();
        copyMatches.and(labels);
        double cnt_SF = (double) copyMatches.cardinality();
        out[0] = cnt_SF / (double) numberOfObservations; //support

        // Check if it passes minimum support threshold
        if (out[0] > supportThreshold) {
            //compute the confidence and lift
            double cnt_S = (double) labels.cardinality();
            double cnt_F = (double) feature.cardinality();
            out[1] = (cnt_SF / cnt_S) / (cnt_F / (double) numberOfObservations); //lift
            out[2] = (cnt_SF) / (cnt_F);   // confidence (feature -> selection)
            out[3] = (cnt_SF) / (cnt_S);   // confidence (selection -> feature)

        } else {
            Arrays.fill(out, Double.NaN);
        }

        return out;
    }
}
