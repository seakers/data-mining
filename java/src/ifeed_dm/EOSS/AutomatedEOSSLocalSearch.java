/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed_dm.EOSS;

import ifeed_dm.*;
import ifeed_dm.BinaryInput.BinaryInputArchitecture;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
/**
 *
 * @author bang
 */
public class AutomatedEOSSLocalSearch {
        
    private EOSSDataMining data_mining;
    private List<BinaryInputArchitecture> archs;
    
    public AutomatedEOSSLocalSearch(List<Integer> behavioral, List<Integer> non_behavioral, List<BinaryInputArchitecture> archs, double supp, double conf, double lift, Set<Integer> restrictedInstrumentSet){
        this(behavioral, non_behavioral, archs, supp, conf, lift);
        this.data_mining = new EOSSDataMining(behavioral,non_behavioral,archs,supp,conf,lift,restrictedInstrumentSet);
    }

    public AutomatedEOSSLocalSearch(List<Integer> behavioral, List<Integer> non_behavioral, List<BinaryInputArchitecture> archs, double supp, double conf, double lift){
        this.data_mining = new EOSSDataMining(behavioral,non_behavioral,archs,supp,conf,lift);
        this.archs = archs;
    }

//    public List<Feature> run(int maxIter, int numInitialFeatureToAdd){
//
//        // Run data mining
//        List<Feature> extracted_features = data_mining.run();
//
//        List<Feature> general_features = Utils.getTopFeatures(extracted_features, 3, FeatureMetric.DISTANCE2UP);
//
//        List<Feature> out = new ArrayList<>();
//
//        for(int i = 0; i < numInitialFeatureToAdd; i++){
//            out.addAll(local_greedy_search(general_features.get(i), maxIter));
//        }
//        return out;
//    }


    public List<Feature> run(int maxIter){

        List<Feature> out = new ArrayList<>();

        // Generate base features to be added to extend a given feature
        List<BaseFeature> baseFeatures = this.data_mining.generateBaseFeatures(false);

        EOSSFilterExpressionHandler filterExpressionHandler = new EOSSFilterExpressionHandler(this.archs.size(), baseFeatures);
        FeatureComparator comparator1 = new FeatureComparator(FeatureMetric.FCONFIDENCE);
        FeatureComparator comparator2 = new FeatureComparator(FeatureMetric.RCONFIDENCE);
        List<Comparator> comparators = new ArrayList<>(Arrays.asList(comparator1,comparator2));

        // Run Apriori
        List<Feature> extracted_features = data_mining.run();

        // Get non-dominated features
        extracted_features = Utils.getFeatureFuzzyParetoFront(extracted_features, comparators,0);

        double coverage = 0.001;
        double specificity = 0.001;
        boolean conjunctive_local_search = true;

        int cnt = 2;
        while(cnt < maxIter){

            System.out.println("iteration " + cnt);

            // Get the best feature based on the distance to the utopia point
            List<Feature> _best_feature = Utils.getTopFeatures(extracted_features, 5, FeatureMetric.DISTANCE2UP);

            // Save the top 3 features
            out.addAll(_best_feature);

            // Get single element from the list
            BaseFeature best_feature = (BaseFeature) _best_feature.get(0);

            double temp_specificity = best_feature.getFConfidence();
            double temp_coverage = best_feature.getRConfidence();

            double rate_of_increase_specificity = (temp_specificity-specificity) / specificity;
            double rate_of_increase_coverage = (temp_coverage - coverage) / coverage;

            System.out.println("s: " + rate_of_increase_specificity +", c: " + rate_of_increase_coverage);

            coverage = temp_coverage;
            specificity = temp_specificity;

            // Create a tree structure based on the given feature expression
            FeatureTreeNode root = filterExpressionHandler.generateFeatureTree(best_feature.getName());
            System.out.println(root.getName());

            // Determine whether to increase specificity or coverage
            if(coverage > specificity){
                conjunctive_local_search = true;
            }else{
                conjunctive_local_search = false;
            }

            List<FeatureTreeNode> potential_nodes_to_add_new_feature;
            if(conjunctive_local_search){
                potential_nodes_to_add_new_feature = filterExpressionHandler.getNodes(root, LogicOperator.AND);
            }else{
                potential_nodes_to_add_new_feature = filterExpressionHandler.getNodes(root, LogicOperator.OR);
            }

            // Initialize the extracted features
            extracted_features = new ArrayList<>();

            for(FeatureTreeNode node:potential_nodes_to_add_new_feature){
                node.addPlaceholder();
                // Run local search using the most general feature
                List<Feature> tempFeatures = data_mining.runLocalSearch(root, baseFeatures);
                extracted_features.addAll(tempFeatures);
                node.removePlaceholder();
            }
            cnt++;
        }

        // Get non-dominated features
        extracted_features = Utils.getFeatureFuzzyParetoFront(extracted_features, comparators,0);

        out.addAll(extracted_features);

        return out;
    }



    
    public List<Feature> local_greedy_search(Feature feature, int maxIter){
        
        int cnt = 0;
        
        List<Feature> extracted_features = new ArrayList<>();
        List<Feature> last_iter_features;
        extracted_features.add(feature);
        
        FeatureComparator comparator1 = new FeatureComparator(FeatureMetric.FCONFIDENCE);
        FeatureComparator comparator2 = new FeatureComparator(FeatureMetric.RCONFIDENCE);
        List<Comparator> comparators = new ArrayList<>(Arrays.asList(comparator1,comparator2));        
        
        while(cnt < maxIter){

            // Get non-dominated features
            extracted_features = Utils.getFeatureFuzzyParetoFront(extracted_features,comparators,0);
            
            // Get the most general feature
            List<Feature> _best_feature = Utils.getTopFeatures(extracted_features, 1, FeatureMetric.DISTANCE2UP);

            // Get single element from the list
            BaseFeature best_feature = (BaseFeature) _best_feature.get(0);

            // Run local search using the most general feature
            extracted_features = data_mining.runLocalSearch(best_feature);

            cnt++;
        }

        // Return only the non-dominated solutions
        extracted_features = Utils.getFeatureFuzzyParetoFront(extracted_features,comparators,0);
        
        return extracted_features;
    }
}
