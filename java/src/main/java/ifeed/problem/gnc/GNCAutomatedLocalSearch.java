/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.gnc;

import ifeed.*;
import ifeed.filter.Filter;
import ifeed.mining.AbstractDataMiningAlgorithm;
import ifeed.mining.AbstractDataMiningBase;
import ifeed.architecture.AbstractArchitecture;
import ifeed.feature.Feature;
import ifeed.feature.FeatureComparator;
import ifeed.feature.FeatureExpressionHandler;
import ifeed.feature.FeatureMetric;
import ifeed.feature.logic.Connective;
import ifeed.feature.logic.ConnectiveTester;
import ifeed.feature.logic.Literal;
import ifeed.feature.logic.LogicalConnectiveType;
import ifeed.problem.eoss.EOSSAssociationRuleMining;
import ifeed.problem.eoss.EOSSLocalSearch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author bang
 */

public class GNCAutomatedLocalSearch extends AbstractDataMiningBase implements AbstractDataMiningAlgorithm{

    private int maxIter;
    private double supp;
    private double conf;
    private double lift;

    public GNCAutomatedLocalSearch(List<AbstractArchitecture> archs, List<Integer> behavioral, List<Integer> non_behavioral,
                                    int maxIter, double supp, double conf, double lift){

        super(archs, behavioral,non_behavioral);
        this.maxIter = maxIter;
        this.supp = supp;
        this.conf = conf;
        this.lift = lift;
    }

    @Override
    public List<Filter> generateCandidates(){
        return new GNCFeatureGenerator().generateCandidates();
    }

    @Override
    public List<Feature> run(){

        EOSSAssociationRuleMining arm = new EOSSAssociationRuleMining(super.architectures, super.behavioral, super.non_behavioral,
                this.supp, this.conf, this.lift);

        EOSSLocalSearch localSearch = new EOSSLocalSearch(null, super.architectures, super.behavioral, super.non_behavioral);

        List<Feature> out = new ArrayList<>();

        // Generate base features to be added to extend a given feature
        List<Feature> baseFeatures = this.generateBaseFeatures();

        GNCFeatureFetcher featureFetcher = new GNCFeatureFetcher(baseFeatures, this.architectures);
        FeatureExpressionHandler filterExpressionHandler = new FeatureExpressionHandler(featureFetcher);

        FeatureComparator comparator1 = new FeatureComparator(FeatureMetric.FCONFIDENCE);
        FeatureComparator comparator2 = new FeatureComparator(FeatureMetric.RCONFIDENCE);
        List<Comparator> comparators = new ArrayList<>(Arrays.asList(comparator1,comparator2));

        // Run Apriori
        List<Feature> extracted_features = arm.run();

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
            Feature best_feature = (Feature) _best_feature.get(0);

            double temp_specificity = best_feature.getFConfidence();
            double temp_coverage = best_feature.getRConfidence();

            double rate_of_increase_specificity = (temp_specificity-specificity) / specificity * 100;
            double rate_of_increase_coverage = (temp_coverage - coverage) / coverage * 100;

            System.out.println("s: " + temp_specificity +", c: " + temp_coverage);

            coverage = temp_coverage;
            specificity = temp_specificity;

            // Create a tree structure based on the given feature expression
            ConnectiveTester root = (ConnectiveTester) filterExpressionHandler.generateFeatureTree(best_feature.getName(), true);
            localSearch.setRoot(root);

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

            List<Connective> sameConnectives;
            List<Connective> oppositeConnectives;

            if(conjunctive_local_search){
                sameConnectives = root.getDescendantConnectives(LogicalConnectiveType.AND, true);
                oppositeConnectives = root.getDescendantConnectives(LogicalConnectiveType.OR, true);
            }else{
                sameConnectives = root.getDescendantConnectives(LogicalConnectiveType.OR, true);
                oppositeConnectives = root.getDescendantConnectives(LogicalConnectiveType.AND, true);
            }

            // Initialize the extracted features
            extracted_features = new ArrayList<>();

            for(Connective node: sameConnectives){
                ConnectiveTester tester = (ConnectiveTester) node;
                tester.setAddNewLiteral();
                tester.computeMatchesLiteral();
                List<Feature> tempFeatures = localSearch.run(baseFeatures);
                extracted_features.addAll(tempFeatures);
                tester.cancelAddNode();
            }

            for(Connective node: oppositeConnectives){
                ConnectiveTester tester = (ConnectiveTester) node;
                for(Literal feature: node.getLiteralChildren()){
                    tester.setAddNewLiteral(feature);
                    tester.computeMatchesLiteral();
                    List<Feature> tempFeatures = localSearch.run(baseFeatures);
                    extracted_features.addAll(tempFeatures);
                    tester.cancelAddNode();
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
