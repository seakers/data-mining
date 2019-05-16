/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.assigning;

import ifeed.Utils;
import ifeed.architecture.AbstractArchitecture;
import ifeed.feature.*;
import ifeed.feature.logic.*;
import ifeed.local.params.BaseParams;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * @author bang
 */

public class SequentialLocalSearch extends LocalSearch {

    private int maxIter;

    public SequentialLocalSearch(BaseParams params,
                                 String root,
                                 LogicalConnectiveType logic,
                                 List<AbstractArchitecture> archs,
                                 List<Integer> behavioral,
                                 List<Integer> non_behavioral,
                                 int maxIter){

        super(params, root, logic, archs, behavioral,non_behavioral);
        this.maxIter = maxIter;
    }

    public List<Feature> runSequentialSearch(){

        if(super.getLogic() == LogicalConnectiveType.OR){
            return super.run();
        }

        System.out.println("Running sequential local search");

        double[] initialMetrics = Utils.computeMetrics(super.root.getMatches(), super.labels, super.getArchitectures().size());
        Feature initialFeature = new Feature(this.root.getName(), this.root.getMatches(), initialMetrics[0], initialMetrics[1], initialMetrics[2], initialMetrics[3]);

        FeatureSimplifier featureSimplifier = new FeatureSimplifier(params, (FeatureFetcher) featureFetcher);

        LogicalConnectiveType thisLogic = LogicalConnectiveType.AND;
        LogicalConnectiveType oppositeLogic = LogicalConnectiveType.OR;

        int iter = 0;
        while(iter < this.maxIter){

            List<Feature> extractedFeatures = new ArrayList<>();
            List<Feature> correspondingBaseFeature = new ArrayList<>();
            List<ConnectiveTester> parentNodes = new ArrayList<>();
            List<Literal> literals = new ArrayList<>();

            List<Connective> sameLogicConnectives = this.root.getDescendantConnectives(thisLogic);
            for(Connective node: sameLogicConnectives){
                ConnectiveTester testNode = (ConnectiveTester) node;
                testNode.setAddNewNode();
                List<Feature> filteredBaseFeatures = this.filterBaseFeatures(testNode, this.baseFeatures);
                Feature baseFeature = this.runArgmaxRecall(this.root, filteredBaseFeatures);

                if(baseFeature == null){
                    testNode.cancelAddNode();
                    continue;
                }
                this.root.setNewNode(baseFeature.getName(), baseFeature.getMatches());
                double[] metrics = Utils.computeMetricsSetNaNZero(root.getMatches(), super.labels, super.samples.size());
                Feature newFeature = new Feature(this.root.getName(), this.root.getMatches(), metrics[0], metrics[1], metrics[2], metrics[3]);
                extractedFeatures.add(newFeature);
                correspondingBaseFeature.add(baseFeature);
                parentNodes.add(testNode);
                literals.add(null);
                testNode.cancelAddNode();
            }

//            List<Connective> oppositeLogicConnectives = root.getDescendantConnectives(oppositeLogic);
//            for(Connective node: oppositeLogicConnectives){
//                ConnectiveTester testNode = (ConnectiveTester) node;
//                for(Literal literal: testNode.getLiteralChildren()){
//                    testNode.setAddNewNode(literal);
//                    List<Feature> filteredBaseFeatures = this.filterBaseFeatures(testNode, this.baseFeatures);
//                    Feature baseFeature = this.runArgmaxRecall(this.root, filteredBaseFeatures);
//                    this.root.setNewNode(baseFeature.getName(), baseFeature.getMatches());
//                    double[] metrics = Utils.computeMetricsSetNaNZero(root.getMatches(), super.labels, super.samples.size());
//                    Feature newFeature = new Feature(this.root.getName(), this.root.getMatches(), metrics[0], metrics[1], metrics[2], metrics[3]);
//                    extractedFeatures.add(newFeature);
//                    correspondingBaseFeature.add(baseFeature);
//                    parentNodes.add(testNode);
//                    literals.add(literal);
//                    testNode.cancelAddNode();
//                }
//            }

            FeatureMetricComparator comparator = new FeatureMetricComparator(FeatureMetric.RECALL);
            if(extractedFeatures.isEmpty()){
                break;
            }
            Feature bestFeature = extractedFeatures.get(0);
            int bestFeatureIndex = 0;
            for(int i = 1; i < extractedFeatures.size(); i++){
                Feature thisFeature = extractedFeatures.get(i);
                if(comparator.compare(thisFeature, bestFeature) > 0){
                    bestFeature = thisFeature;
                    bestFeatureIndex = i;
                }
            }

            if(initialFeature.getRecall() - bestFeature.getRecall() > 0.01){
                break;
            }

            Feature baseFeature = correspondingBaseFeature.get(bestFeatureIndex);
            if(literals.get(bestFeatureIndex) != null){
                this.root.setAddNewNode(literals.get(bestFeatureIndex));
            }
            parentNodes.get(bestFeatureIndex).setAddNewNode();
            this.root.setNewNode(baseFeature.getName(), baseFeature.getMatches());
            this.root.finalizeNewNodeAddition();
            featureSimplifier.simplify(this.root);

            double[] metrics = Utils.computeMetricsSetNaNZero(root.getMatches(), super.labels, super.samples.size());
            System.out.println(this.root.getName() + "| precision: " + metrics[2] + ", recall: " + metrics[3]);
            iter++;
        }

        double[] metrics = Utils.computeMetricsSetNaNZero(root.getMatches(), super.labels, super.samples.size());
        Feature newFeature = new Feature(this.root.getName(), this.root.getMatches(), metrics[0], metrics[1], metrics[2], metrics[3]);
        List<Feature> out = new ArrayList<>();
        out.add(newFeature);

        System.out.println("name: " + newFeature.getName());
        System.out.println("Done");
        return out;
    }

    public Feature runArgmaxRecall(LocalSearchTester featureToTest, List<Feature> baseFeatures){

        if(featureToTest == null){
            throw new IllegalStateException("Feature tree need to be defined to run local search");
        }

        FeatureMetricComparator precisionComparator = new FeatureMetricComparator(FeatureMetric.PRECISION);
        FeatureMetricComparator recallComparator = new FeatureMetricComparator(FeatureMetric.RECALL);

        // Evaluate the original feature
        double[] rootMetrics = Utils.computeMetricsSetNaNZero(featureToTest.getMatchesOriginalFeature(), super.labels, super.samples.size());

        BitSet originalMatches = ((ConnectiveTester) featureToTest).getMatches();
        String origianlName = ((ConnectiveTester) featureToTest).getName();

//        System.out.println("Argmax run");
//        System.out.println(this.root.getName() + "| precision: " + rootMetrics[2] + ", recall: " + rootMetrics[3]);

        Feature originalFeature = new Feature(origianlName, originalMatches, rootMetrics[0], rootMetrics[1], rootMetrics[2], rootMetrics[3]);
        Feature currentBestFeature = null;
        Feature savedBaseFeature = null;

        // Add a base feature to the given feature, replacing the placeholder
        for(Feature baseFeature: baseFeatures){

            // Define which feature will be add to the current placeholder location
            featureToTest.setNewNode(baseFeature.getName(), baseFeature.getMatches());

            BitSet matches = ((ConnectiveTester) featureToTest).getMatches();
            String name = ((ConnectiveTester) featureToTest).getName();

            double[] metrics = Utils.computeMetricsSetNaNZero(matches, super.labels, super.samples.size());
            if(Double.isNaN(metrics[0])){
                continue;
            }

            Feature newFeature = new Feature(name, matches, metrics[0], metrics[1], metrics[2], metrics[3]);

//            System.out.println(this.root.getName() + "| precision: " + metrics[2] + ", recall: " + metrics[3]);

            if(recallComparator.compare(newFeature, originalFeature) == 0){
                if(precisionComparator.compare(newFeature, originalFeature) > 0){
                    return baseFeature;
                }else{
                    continue;
                }
            }

            if(currentBestFeature == null){
                currentBestFeature = newFeature;
                savedBaseFeature = baseFeature;

            }else if(recallComparator.compare(newFeature, currentBestFeature) > 0){
                currentBestFeature = newFeature;
                savedBaseFeature = baseFeature;
            }
        }
        return savedBaseFeature;
    }
}
