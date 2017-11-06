/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed_dm.EOSS;

import ifeed_dm.BinaryInputArchitecture;
import ifeed_dm.BinaryInputFeature;
import ifeed_dm.DataMiningParams;
import ifeed_dm.Feature;
import ifeed_dm.FeatureComparator;
import ifeed_dm.FeatureMetric;
import ifeed_dm.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author bang
 */
public class AutomatedEOSSLocalSearch {
        
    private EOSSDataMining data_mining;
    
    public AutomatedEOSSLocalSearch(List<Integer> behavioral, List<Integer> non_behavioral, List<BinaryInputArchitecture> archs, double supp, double conf, double lift){
        
// Initialize DrivingFeaturesGenerator
        data_mining = new EOSSDataMining(behavioral,non_behavioral,archs,supp,conf,lift);
        
    }
    
    
    public List<Feature> run(){


        // Run data mining
        List<Feature> extracted_features = data_mining.run();
        
        FeatureComparator comparator1 = new FeatureComparator(FeatureMetric.FCONFIDENCE);
        FeatureComparator comparator2 = new FeatureComparator(FeatureMetric.RCONFIDENCE);
        List<Comparator> comparators = new ArrayList<>(Arrays.asList(comparator1,comparator2));
        
        List<Feature> general_features = Utils.getTopFeatures(extracted_features, 5, FeatureMetric.RCONFIDENCE);
        
        List<Feature> out = new ArrayList<>();
        
        for(int i=0;i<5;i++){
            out.addAll(local_greedy_search(general_features.get(i)));
        }
        
        return out;
    }
    
    public List<Feature> local_greedy_search(Feature feature){
        
        int cnt = 0;
        
        List<Feature> extracted_features = new ArrayList<>();
        extracted_features.add(feature);
        
        while(cnt<10){

            //extracted_features = UTILS.getFeatureFuzzyParetoFront(extracted_features,comparators,0);

            List<Feature> _most_general_feature = Utils.getTopFeatures(extracted_features, 1, FeatureMetric.RCONFIDENCE);

            BinaryInputFeature most_general_feature = (BinaryInputFeature) _most_general_feature.get(0);

            extracted_features = data_mining.runLocalSearch(most_general_feature);

            cnt++;
        }
        
        return extracted_features;
    }
    
    
}
