///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package ifeed_dm.GNC;
//
//import ifeed_dm.EOSS.*;
//import ifeed_dm.binaryInput.BinaryInputArchitecture;
//import ifeed_dm.binaryInput.BinaryInputFeature;
//import ifeed_dm.DataMiningParams;
//import ifeed_dm.Feature;
//import ifeed_dm.FeatureComparator;
//import ifeed_dm.FeatureMetric;
//import ifeed_dm.Utils;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Comparator;
//import java.util.List;
//import java.util.Set;
///**
// *
// * @author bang
// */
//public class AutomatedGNCLocalSearch {
//        
//    private GNCDataMining data_mining;
//    
//    public AutomatedGNCLocalSearch(List<Integer> behavioral, List<Integer> non_behavioral, List<BinaryInputArchitecture> archs, double supp, double conf, double lift, Set<Integer> restrictedInstrumentSet){
//        this(behavioral, non_behavioral, archs, supp, conf, lift);
//        this.data_mining = new GNCDataMining(behavioral,non_behavioral,archs,supp,conf,lift,restrictedInstrumentSet);
//    }    
//    
//    
//    public AutomatedGNCLocalSearch(List<Integer> behavioral, List<Integer> non_behavioral, List<BinaryInputArchitecture> archs, double supp, double conf, double lift){
//        
//        this.data_mining = new GNCDataMining(behavioral,non_behavioral,archs,supp,conf,lift);
//    }
//    
//    
//    public List<Feature> run(int maxIter, int numInitialFeatureToAdd){
//
//        // Run data mining
//        List<Feature> extracted_features = data_mining.run();
//        
//        FeatureComparator comparator1 = new FeatureComparator(FeatureMetric.FCONFIDENCE);
//        FeatureComparator comparator2 = new FeatureComparator(FeatureMetric.RCONFIDENCE);
//        List<Comparator> comparators = new ArrayList<>(Arrays.asList(comparator1,comparator2));
//        
//        List<Feature> general_features = Utils.getTopFeatures(extracted_features, 3, FeatureMetric.RCONFIDENCE);
//        
//        List<Feature> out = new ArrayList<>();
//        
//        for(int i=0;i<numInitialFeatureToAdd;i++){
//            out.addAll(local_greedy_search(general_features.get(i), maxIter));
//        }
//        
//        return out;
//    }
//    
//    public List<Feature> local_greedy_search(Feature feature, int maxIter){
//        
//        int cnt = 0;
//        
//        List<Feature> extracted_features = new ArrayList<>();
//        List<Feature> last_iter_features;
//        extracted_features.add(feature);
//        
//        FeatureComparator comparator1 = new FeatureComparator(FeatureMetric.FCONFIDENCE);
//        FeatureComparator comparator2 = new FeatureComparator(FeatureMetric.RCONFIDENCE);
//        List<Comparator> comparators = new ArrayList<>(Arrays.asList(comparator1,comparator2));        
//        
//        while(cnt < maxIter){
//            
//            // Get non-dominated features
//            extracted_features = Utils.getFeatureFuzzyParetoFront(extracted_features,comparators,0);
//            
//            // Get the most general feature
//            List<Feature> _most_general_feature = Utils.getTopFeatures(extracted_features, 1, FeatureMetric.RCONFIDENCE);
//
//            // Get single element from the list
//            BinaryInputFeature most_general_feature = (BinaryInputFeature) _most_general_feature.get(0);
//
//            // Run local search using the most general feature
//            extracted_features = data_mining.runLocalSearch(most_general_feature);
//
//            cnt++;
//        }
//        
//        // Return only the non-dominated solutions
//        extracted_features = Utils.getFeatureFuzzyParetoFront(extracted_features,comparators,0);
//        
//        return extracted_features;
//    }
//    
//    
//}
