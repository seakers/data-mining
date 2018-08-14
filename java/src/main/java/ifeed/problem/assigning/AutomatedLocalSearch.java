/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.assigning;

import ifeed.architecture.AbstractArchitecture;
import ifeed.feature.*;
import ifeed.feature.FeatureMetricComparator;
import ifeed.feature.logic.Connective;
import ifeed.feature.logic.Literal;
import ifeed.feature.logic.LogicalConnectiveType;
import ifeed.feature.logic.ConnectiveTester;
import ifeed.filter.AbstractFilter;
import ifeed.local.params.BaseParams;
import ifeed.mining.AbstractDataMiningAlgorithm;
import ifeed.mining.AbstractDataMiningBase;

import ifeed.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author bang
 */

public class AutomatedLocalSearch extends AbstractDataMiningBase implements AbstractDataMiningAlgorithm{

    private int maxIter;
    private double supp;
    private double conf;
    private double lift;

    public AutomatedLocalSearch(BaseParams params, List<AbstractArchitecture> archs, List<Integer> behavioral, List<Integer> non_behavioral,
                                int maxIter, double supp, double conf, double lift){
        super(params, archs, behavioral,non_behavioral);
        this.maxIter = maxIter;
        this.supp = supp;
        this.conf = conf;
        this.lift = lift;
    }

    @Override
    public List<AbstractFilter> generateCandidates(){
        return new FeatureGenerator(params).generateCandidates();
    }

    @Override
    public List<Feature> run(){

        AssociationRuleMining arm = new AssociationRuleMining(params, super.architectures, super.behavioral, super.non_behavioral,
                this.supp, this.conf, this.lift);

        LocalSearch localSearch = new LocalSearch(params, null, super.architectures, super.behavioral, super.non_behavioral);

        List<Feature> out = new ArrayList<>();

        // Generate base features to be added to extend a given feature
        List<Feature> baseFeatures = this.generateBaseFeatures();

        AbstractFeatureFetcher featureFetcher = new FeatureFetcher(params, baseFeatures, this.architectures);
        FeatureExpressionHandler filterExpressionHandler = new FeatureExpressionHandler(featureFetcher);

        FeatureMetricComparator comparator1 = new FeatureMetricComparator(FeatureMetric.FCONFIDENCE);
        FeatureMetricComparator comparator2 = new FeatureMetricComparator(FeatureMetric.RCONFIDENCE);
        List<Comparator> comparators = new ArrayList<>(Arrays.asList(comparator1,comparator2));

        // Run Apriori
        List<Feature> extracted_features = arm.run();

        // Get non-dominated features
        extracted_features = Utils.getFeatureFuzzyParetoFront(extracted_features, comparators,0);

        double coverage = 0.001;
        double specificity = 0.001;
        boolean conjunctive_local_search = true;

        int cnt = 2;
        while(cnt < this.maxIter){

            System.out.println("Iteration " + cnt);

            // Get the best feature based on the distance to the utopia point
            List<Feature> _best_feature = Utils.getTopFeatures(extracted_features, 5, FeatureMetric.DISTANCE2UP);

            // Save the top 3 features
            out.addAll(_best_feature);

            // Get single element from the list
            Feature best_feature = (Feature) _best_feature.get(0);

            double temp_specificity = best_feature.getPrecision();
            double temp_coverage = best_feature.getRecall();

            double rate_of_increase_specificity = (temp_specificity-specificity) / specificity * 100;
            double rate_of_increase_coverage = (temp_coverage - coverage) / coverage * 100;

            System.out.println("s: " + temp_specificity +", c: " + temp_coverage);

            coverage = temp_coverage;
            specificity = temp_specificity;

            // Create a tree structure based on the given feature expression (creates an instance of ConnectiveTester class)
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
                tester.setAddNewNode();
                tester.preComputeMatchesLiteral();
                List<Feature> tempFeatures = localSearch.run(baseFeatures);
                extracted_features.addAll(tempFeatures);
                tester.cancelAddNode();
            }

            for(Connective node: oppositeConnectives){
                ConnectiveTester tester = (ConnectiveTester) node;
                for(Literal feature: node.getLiteralChildren()){
                    tester.setAddNewNode(feature);
                    tester.preComputeMatchesLiteral();
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

//    public List<Feature> local_greedy_search(Feature feature, int maxIter){
//
//        int cnt = 0;
//        List<Feature> extracted_features = new ArrayList<>();
//        List<Feature> last_iter_features;
//        extracted_features.add(feature);
//
//        FeatureMetricComparator comparator1 = new FeatureMetricComparator(FeatureMetric.FCONFIDENCE);
//        FeatureMetricComparator comparator2 = new FeatureMetricComparator(FeatureMetric.RCONFIDENCE);
//        List<Comparator> comparators = new ArrayList<>(Arrays.asList(comparator1,comparator2));
//
//        while(cnt < maxIter){
//
//            // Get non-dominated features
//            extracted_features = Utils.getFeatureFuzzyParetoFront(extracted_features,comparators,0);
//
//            // Get the most general feature
//            List<Feature> _best_feature = Utils.getTopFeatures(extracted_features, 1, FeatureMetric.DISTANCE2UP);
//
//            // Get single element from the list
//            Feature best_feature = (Feature) _best_feature.get(0);
//
//            // Run local Search using the most general feature
//            extracted_features = data_mining.runLocalSearch(best_feature);
//
//            cnt++;
//        }
//
//        // Return only the non-dominated solutions
//        extracted_features = Utils.getFeatureFuzzyParetoFront(extracted_features,comparators,0);
//
//        return extracted_features;
//    }
}
