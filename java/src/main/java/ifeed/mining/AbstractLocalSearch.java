package ifeed.mining;

import ifeed.Utils;
import ifeed.architecture.AbstractArchitecture;
import ifeed.feature.*;
import ifeed.feature.logic.Connective;
import ifeed.feature.logic.ConnectiveTester;
import ifeed.feature.logic.Literal;
import ifeed.feature.logic.LogicalConnectiveType;
import ifeed.filter.AbstractFilterFetcher;
import ifeed.local.params.BaseParams;

import java.util.*;

public abstract class AbstractLocalSearch extends AbstractDataMiningBase implements AbstractDataMiningAlgorithm {

    private ConnectiveTester root;
    private LogicalConnectiveType logic;
    private List<Feature> baseFeatures;
    private AbstractFeatureFetcher featureFetcher;
    private AbstractFilterFetcher filterFetcher;
    private FeatureExpressionHandler featureHandler;

    public AbstractLocalSearch(BaseParams params,
                               List<AbstractArchitecture> architectures,
                               List<Integer> behavioral,
                               List<Integer> non_behavioral,
                               AbstractFeatureFetcher fetcher){

        super(params, architectures, behavioral, non_behavioral);

        this.baseFeatures = super.generateBaseFeatures();

        this.featureFetcher = fetcher;
        if(this.featureFetcher.getBaseFeatures().isEmpty()){
            this.featureFetcher.setBaseFeatures(this.baseFeatures);
        }
        this.filterFetcher = this.featureFetcher.getFilterFetcher();
        this.featureHandler = new FeatureExpressionHandler(this.featureFetcher);

        this.root = null;
    }

    public AbstractLocalSearch(BaseParams params,
                                    String rootFeatureExpression,
                                    LogicalConnectiveType logic,
                                    List<AbstractArchitecture> architectures,
                                    List<Integer> behavioral,
                                    List<Integer> non_behavioral,
                                    AbstractFeatureFetcher fetcher){

        this(params, architectures, behavioral, non_behavioral, fetcher);
        ConnectiveTester root = (ConnectiveTester) this.featureHandler.generateFeatureTree(rootFeatureExpression, true);
        this.root = root;
        this.logic = logic;
    }

    public AbstractLocalSearch(BaseParams params,
                               ConnectiveTester root,
                               LogicalConnectiveType logic,
                               List<AbstractArchitecture> architectures,
                               List<Integer> behavioral,
                               List<Integer> non_behavioral,
                               AbstractFeatureFetcher fetcher){

        this(params, architectures, behavioral, non_behavioral, fetcher);
        this.root = root;
        this.logic = logic;
    }

    public void setRoot(ConnectiveTester root){
        this.root = root;
    }

    public void setLogic(LogicalConnectiveType logic){ this.logic = logic; }

    @Override
    public List<Feature> run(){

        List<Connective> sameLogicConnectives;
        List<Connective> oppositeLogicConnectives;

        LogicalConnectiveType oppositeLogic;
        if(this.logic == LogicalConnectiveType.AND){
            oppositeLogic = LogicalConnectiveType.OR;
        }else{
            oppositeLogic = LogicalConnectiveType.AND;
        }

        sameLogicConnectives = root.getDescendantConnectives(this.logic, true);
        oppositeLogicConnectives = root.getDescendantConnectives(oppositeLogic, true);

        // Initialize the extracted features
        List<ifeed.feature.Feature> extracted_features = new ArrayList<>();

        for(Connective node: sameLogicConnectives){
            ConnectiveTester testNode = (ConnectiveTester) node;
            testNode.setAddNewNode();
            List<Feature> filteredBaseFeatures = this.filterBaseFeatures(testNode, this.baseFeatures);
            List<ifeed.feature.Feature> tempFeatures = this.testLocalChanges(filteredBaseFeatures);
            extracted_features.addAll(tempFeatures);
            testNode.cancelAddNode();
        }

        for(Connective node: oppositeLogicConnectives){
            ConnectiveTester testNode = (ConnectiveTester) node;
            for(Literal literal: testNode.getLiteralChildren()){
                testNode.setAddNewNode(literal);
                List<Feature> filteredBaseFeatures = this.filterBaseFeatures(testNode, this.baseFeatures);
                List<ifeed.feature.Feature> tempFeatures = this.testLocalChanges(filteredBaseFeatures);
                extracted_features.addAll(tempFeatures);
                testNode.cancelAddNode();
            }
        }

        return extracted_features;
    }

    public boolean simplifyFeature(Connective root){
        return false;
    }

    /**
     * Method used to put constraints on base features for a given testNode
     * @param testNode
     * @param baseFeatures
     * @return
     */
    public List<Feature> filterBaseFeatures(ConnectiveTester testNode, List<Feature> baseFeatures){
        return baseFeatures;
    }

    /**
     * Runs local Search that extends a given feature
     * */
    public List<Feature> testLocalChanges(List<Feature> baseFeatures){

        if(this.root == null){
            throw new IllegalStateException("Feature tree need to be defined to run local search");
        }

        long t0 = System.currentTimeMillis();

        List<Feature> extracted_features;
        List<Feature> minedFeatures = new ArrayList<>();

        // Add a base feature to the given feature, replacing the placeholder
        for(Feature feature: baseFeatures){

            // Define which feature will be add to the current placeholder location
            this.root.setNewNode(feature.getName(), feature.getMatches());

            BitSet matches = this.root.getMatches();
            double[] metrics = Utils.computeMetricsSetNaNZero(matches, super.labels, super.population.size());

            if(Double.isNaN(metrics[0])){
                continue;
            }

            // Simplify the structure of the feature
            this.simplifyFeature(this.root);

            String name = this.root.getName();
            Feature newFeature = new Feature(name, matches, metrics[0], metrics[1], metrics[2], metrics[3]);
            minedFeatures.add(newFeature);
        }

        FeatureMetricComparator comparator1 = new FeatureMetricComparator(FeatureMetric.FCONFIDENCE);
        FeatureMetricComparator comparator2 = new FeatureMetricComparator(FeatureMetric.RCONFIDENCE);
        List<Comparator> comparators = new ArrayList<>(Arrays.asList(comparator1,comparator2));

        extracted_features = Utils.getFeatureFuzzyParetoFront(minedFeatures,comparators,0);

        long t1 = System.currentTimeMillis();
        //System.out.println("...[" + this.getClass().getSimpleName() + "] Total data mining time : " + String.valueOf(t1 - t0) + " msec");
        return extracted_features;
    }

    /**
     * Finds the base feature that maximizes a certain metric
     * @param baseFeatures
     * @param comparator
     * @return
     */
    public Feature runArgmax(List<Feature> baseFeatures, Comparator comparator){

        if(this.root == null){
            throw new IllegalStateException("Feature tree need to be defined to run local search");
        }

        // Evaluate the original feature
        double[] rootMetrics = Utils.computeMetricsSetNaNZero(this.root.getMatchesOriginalFeature(), super.labels, super.population.size());
        Feature currentBestFeature = new Feature(this.root.getName(), this.root.getMatches(), rootMetrics[0], rootMetrics[1], rootMetrics[2], rootMetrics[3]);
        Feature savedBaseFeature = null;

//        System.out.println(this.root.getName() + "| precision: " +rootMetrics[2] + ", recall: " + rootMetrics[3]);

        // Add a base feature to the given feature, replacing the placeholder
        for(Feature baseFeature: baseFeatures){

            // Define which feature will be add to the current placeholder location
            this.root.setNewNode(baseFeature.getName(), baseFeature.getMatches());

            BitSet matches = this.root.getMatches();
            double[] metrics = Utils.computeMetricsSetNaNZero(matches, super.labels, super.population.size());

            if(Double.isNaN(metrics[0])){
                continue;
            }

            this.simplifyFeature(this.root);

            String name = this.root.getName();
            Feature newFeature = new Feature(name, matches, metrics[0], metrics[1], metrics[2], metrics[3]);

//            System.out.println(this.root.getName() + "| precision: " + metrics[2] + ", recall: " + metrics[3]);

            if(comparator.compare(newFeature, currentBestFeature) > 0){
                currentBestFeature = newFeature;
                savedBaseFeature = baseFeature;
            }
        }

        return savedBaseFeature;
    }

    /**
     * Adds an extra condition or an exception to a node
     * @param root root of the tree to be modified
     * @param parent parent node where new conditions or exceptions will be added
     * @param literalToBeCombined literal that the new condition is to be combined with (can be null)
     * @param baseFeaturesToTest base features
     * @param maxNumConditions the maximum number of conditions that can be added
     * @param metric metric used to select the best feature
     */
    public void addExtraCondition(Connective root, Connective parent, Literal literalToBeCombined, List<Feature> baseFeaturesToTest, int maxNumConditions, FeatureMetric metric){

        // Create tester
        ConnectiveTester tester = new ConnectiveTester(root);
        this.setRoot(tester);

        // Find the parent node within the tester tree
        ConnectiveTester parentNodeTester = null;
        for(Connective node: tester.getDescendantConnectives(true)){
            if(this.getFeatureHandler().featureTreeEquals(parent, node)){
                parentNodeTester = (ConnectiveTester) node;
            }
        }

        boolean combineLiteral = false;
        Literal testerLiteralToBeCombined = null;
        if(literalToBeCombined != null){
            combineLiteral = true;
            for(Literal literal: parentNodeTester.getLiteralChildren()){
                if(literal.hashCode() == literalToBeCombined.hashCode()){
                    testerLiteralToBeCombined = literal;
                }
            }
        }

        // Define the comparator
        FeatureMetricComparator comparator = new FeatureMetricComparator(metric);

        for(int i = 0; i < maxNumConditions; i++){
            if(combineLiteral){
                parentNodeTester.setAddNewNode(testerLiteralToBeCombined);
            }else{
                parentNodeTester.setAddNewNode();
            }

            // Run local search
            Feature localSearchOutput = this.runArgmax(baseFeaturesToTest, comparator);

            if(localSearchOutput == null){
                break;

            }else{
                // Modify the tester
                parentNodeTester.setNewNode(localSearchOutput.getName(), localSearchOutput.getMatches());
                parentNodeTester.finalizeNewNodeAddition();

                // Directly modify the tree
                if(combineLiteral){

                    // Remove the target node from its parent
                    parent.removeNode(literalToBeCombined);

                    Connective tempBranch;
                    if(parent.getLogic() == LogicalConnectiveType.OR){
                        tempBranch = new Connective(LogicalConnectiveType.AND);
                    }else{
                        tempBranch = new Connective(LogicalConnectiveType.OR);
                    }
                    tempBranch.addNode(literalToBeCombined);
                    tempBranch.addLiteral(localSearchOutput.getName(), localSearchOutput.getMatches());
                    parent.addBranch(tempBranch);

                    // Change parent
                    parent = tempBranch;
                    boolean updatedParent = false;
                    for(Connective branch: parentNodeTester.getConnectiveChildren()){
                        if(branch.getLiteralChildren().size() == 2 && branch.getConnectiveChildren().isEmpty()){
                            for(Literal literal: branch.getLiteralChildren()){
                                if(literal.hashCode() == literalToBeCombined.hashCode()){
                                    parentNodeTester = (ConnectiveTester) branch;
                                    updatedParent = true;
                                    break;
                                }
                            }
                        }
                    }
                    if(!updatedParent){
                        throw new IllegalStateException("Error: parent not updated");
                    }
                    combineLiteral = false;

                }else{
                    parent.addLiteral(localSearchOutput.getName(), localSearchOutput.getMatches());

                }
            }
        }
    }

    public AbstractFilterFetcher getFilterFetcher() {
        return filterFetcher;
    }

    public AbstractFeatureFetcher getFeatureFetcher() {
        return featureFetcher;
    }

    public FeatureExpressionHandler getFeatureHandler() { return featureHandler; }
}
