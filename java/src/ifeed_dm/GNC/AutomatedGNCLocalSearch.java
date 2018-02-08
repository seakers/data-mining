/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed_dm.GNC;

import ifeed_dm.*;
import ifeed_dm.discreteInput.DiscreteInputArchitecture;
import ifeed_dm.featureTree.LogicNode;
import ifeed_dm.featureTree.FeatureNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 *
 * @author bang
 */

public class AutomatedGNCLocalSearch {

    private GNCDataMining data_mining;
    private List<DiscreteInputArchitecture> archs;

    public AutomatedGNCLocalSearch(List<Integer> behavioral, List<Integer> non_behavioral, List<DiscreteInputArchitecture> archs, double supp, double conf, double lift){
        this.data_mining = new GNCDataMining(behavioral,non_behavioral,archs,supp,conf,lift);
        this.archs = archs;
    }

    public List<Feature> run(int maxIter){

        List<Feature> out = new ArrayList<>();

        // Generate base features to be added to extend a given feature
        List<BaseFeature> baseFeatures = this.data_mining.generateBaseFeatures(false);

        GNCFilterExpressionHandler filterExpressionHandler = new GNCFilterExpressionHandler(this.archs.size(), baseFeatures);
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

            System.out.println("Iteration " + cnt);

            // Get the best feature based on the distance to the utopia point
            List<Feature> _best_feature = Utils.getTopFeatures(extracted_features, 5, FeatureMetric.DISTANCE2UP);

            // Save the top 3 features
            out.addAll(_best_feature);

            // Get single element from the list
            BaseFeature best_feature = (BaseFeature) _best_feature.get(0);

            double temp_specificity = best_feature.getFConfidence();
            double temp_coverage = best_feature.getRConfidence();

            double rate_of_increase_specificity = (temp_specificity-specificity) / specificity * 100;
            double rate_of_increase_coverage = (temp_coverage - coverage) / coverage * 100;

            System.out.println("s: " + temp_specificity +", c: " + temp_coverage);

            coverage = temp_coverage;
            specificity = temp_specificity;

            // Create a tree structure based on the given feature expression
            LogicNode root = filterExpressionHandler.generateFeatureTree(best_feature.getName());

            // Determine whether to increase specificity or coverage
            if(coverage > specificity){
                conjunctive_local_search = true;
            }else{
                conjunctive_local_search = false;
            }

            System.out.println(root.getName());
            if(conjunctive_local_search){
                System.out.println("conjunctive");
            }else{
                System.out.println("disjunctive");
            }

            List<LogicNode> sameLogicNodes;
            List<LogicNode> oppositeLogicNodes;

            if(conjunctive_local_search){
                sameLogicNodes = root.getDescendantNodes(LogicOperator.AND);
                oppositeLogicNodes = root.getDescendantNodes(LogicOperator.OR);
            }else{
                sameLogicNodes = root.getDescendantNodes(LogicOperator.OR);
                oppositeLogicNodes = root.getDescendantNodes(LogicOperator.AND);
            }

            // Initialize the extracted features
            extracted_features = new ArrayList<>();

            for(LogicNode node: sameLogicNodes){
                node.setAddNode();
                node.precomputeMatches();
                List<Feature> tempFeatures = data_mining.runLocalSearch(root, baseFeatures);
                extracted_features.addAll(tempFeatures);
                node.cancelAddNode();
            }

            for(LogicNode node: oppositeLogicNodes){
                for(FeatureNode feature: node.getFeatureNodeChildren()){
                    node.setAddNode(feature);
                    node.precomputeMatches();
                    List<Feature> tempFeatures = data_mining.runLocalSearch(root, baseFeatures);
                    extracted_features.addAll(tempFeatures);
                    node.cancelAddNode();
                }
            }

            cnt++;
        }

        // Get non-dominated features
        extracted_features = Utils.getFeatureFuzzyParetoFront(extracted_features, comparators,0);

        out.addAll(extracted_features);

        return out;
    }

}
