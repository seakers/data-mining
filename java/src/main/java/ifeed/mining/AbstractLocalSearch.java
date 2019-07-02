package ifeed.mining;

import ifeed.Utils;
import ifeed.architecture.AbstractArchitecture;
import ifeed.feature.*;
import ifeed.feature.logic.*;
import ifeed.filter.AbstractFilterFetcher;
import ifeed.local.params.BaseParams;

import java.util.*;

public abstract class AbstractLocalSearch extends AbstractDataMiningBase implements AbstractDataMiningAlgorithm {

    protected ConnectiveTester root;
    protected LogicalConnectiveType logic;
    protected List<Feature> baseFeatures;
    protected AbstractFeatureFetcher featureFetcher;
    protected AbstractFilterFetcher filterFetcher;
    protected FeatureExpressionHandler featureHandler;

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

    public ConnectiveTester getRoot() {
        return root;
    }

    public LogicalConnectiveType getLogic() {
        return logic;
    }

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

        // Initialize the extracted features
        List<ifeed.feature.Feature> extractedFeatures = new ArrayList<>();

        sameLogicConnectives = root.getDescendantConnectives(this.logic);
        for(Connective node: sameLogicConnectives){
            ConnectiveTester testNode = (ConnectiveTester) node;
            testNode.setAddNewNode();
            List<Feature> filteredBaseFeatures = this.filterBaseFeatures(testNode, this.baseFeatures);
            List<ifeed.feature.Feature> tempFeatures = this.testLocalChanges(this.root, filteredBaseFeatures);
            extractedFeatures.addAll(tempFeatures);
            testNode.cancelAddNode();
        }
        oppositeLogicConnectives = root.getDescendantConnectives(oppositeLogic);
        for(Connective node: oppositeLogicConnectives){
            ConnectiveTester testNode = (ConnectiveTester) node;
            for(Literal literal: testNode.getLiteralChildren()){
                testNode.setAddNewNode(literal);
                List<Feature> filteredBaseFeatures = this.filterBaseFeatures(testNode, this.baseFeatures);
                List<ifeed.feature.Feature> tempFeatures = this.testLocalChanges(this.root, filteredBaseFeatures);
                extractedFeatures.addAll(tempFeatures);
                testNode.cancelAddNode();
            }
        }

        List<IfThenStatement> ifThenStatements = root.getDescendantIfThenStatements();
        if(logic == LogicalConnectiveType.AND){
            // Add node to consequent
            for(IfThenStatement node: ifThenStatements){
                IfThenStatementTester testNode = (IfThenStatementTester) node;
                testNode.setAddNewNode();
                List<Feature> filteredBaseFeatures = this.filterBaseFeatures(testNode, this.baseFeatures);
                List<ifeed.feature.Feature> tempFeatures = this.testLocalChanges(this.root, filteredBaseFeatures);
                extractedFeatures.addAll(tempFeatures);
                testNode.cancelAddNode();
            }

            // Add node to alternative
            for(IfThenStatement node: ifThenStatements){
                IfThenStatementTester testNode = (IfThenStatementTester) node;
                testNode.setAddNewNodeToAlternative();
                List<Feature> filteredBaseFeatures = this.filterBaseFeatures(testNode, this.baseFeatures);
                List<ifeed.feature.Feature> tempFeatures = this.testLocalChanges(this.root, filteredBaseFeatures);
                extractedFeatures.addAll(tempFeatures);
                testNode.cancelAddNode();
            }
        } else {
            for(IfThenStatement node: ifThenStatements){
                IfThenStatementTester testNode = (IfThenStatementTester) node;
                for(Formula literal: testNode.getConsequent()){
                    if(literal instanceof Literal){
                        testNode.setAddNewNode((Literal)literal);
                        List<Feature> filteredBaseFeatures = this.filterBaseFeatures(testNode, this.baseFeatures);
                        List<ifeed.feature.Feature> tempFeatures = this.testLocalChanges(this.root, filteredBaseFeatures);
                        extractedFeatures.addAll(tempFeatures);
                        testNode.cancelAddNode();
                    }
                }
                for(Formula literal: testNode.getAlternative()){
                    if(literal instanceof Literal){
                        testNode.setAddNewNodeToAlternative((Literal)literal);
                        List<Feature> filteredBaseFeatures = this.filterBaseFeatures(testNode, this.baseFeatures);
                        List<ifeed.feature.Feature> tempFeatures = this.testLocalChanges(this.root, filteredBaseFeatures);
                        extractedFeatures.addAll(tempFeatures);
                        testNode.cancelAddNode();
                    }
                }
            }
        }
        return extractedFeatures;
    }

    /**
     * Method used to put constraints on base features for a given testNode
     * @param testNode
     * @param baseFeatures
     * @return
     */
    public List<Feature> filterBaseFeatures(LocalSearchTester testNode, List<Feature> baseFeatures){
        return baseFeatures;
    }

    /**
     * Runs local Search that extends a given feature
     * */
    public List<Feature> testLocalChanges(LocalSearchTester featureToTest, List<Feature> baseFeatures){

        if(featureToTest == null){
            throw new IllegalStateException("Feature tree need to be defined to run local search");
        }

        List<Feature> minedFeatures = new ArrayList<>();

        // Add a base feature to the given feature, replacing the placeholder
        for(Feature feature: baseFeatures){

            // Define which feature will be add to the current placeholder location
            featureToTest.setNewNode(feature.getName(), feature.getMatches());

            BitSet matches;
            String name;
            if(featureToTest instanceof ConnectiveTester){
                matches = ((ConnectiveTester) featureToTest).getMatches();
                name = ((ConnectiveTester) featureToTest).getName();

            }else if(featureToTest instanceof IfThenStatementTester){
                matches = ((IfThenStatementTester) featureToTest).getMatches();
                name = ((IfThenStatementTester) featureToTest).getName();

            }else{
                throw new IllegalStateException("");
            }

            double[] metrics = Utils.computeMetricsSetNaNZero(matches, super.labels, super.samples.size());
            if(Double.isNaN(metrics[0])){
                continue;
            }

            Feature newFeature = new Feature(name, matches, metrics[0], metrics[1], metrics[2], metrics[3]);
            minedFeatures.add(newFeature);
        }

        FeatureMetricComparator comparator1 = new FeatureMetricComparator(FeatureMetric.PRECISION);
        FeatureMetricComparator comparator2 = new FeatureMetricComparator(FeatureMetric.RECALL);
        List<Comparator> comparators = new ArrayList<>(Arrays.asList(comparator1,comparator2));

        return Utils.getFeatureFuzzyParetoFront(minedFeatures, comparators,0);
    }

    /**
     * Finds the base feature that maximizes a certain metric
     * @param baseFeatures
     * @param comparator
     * @return
     */
    public Feature runArgmax(LocalSearchTester featureToTest, List<Feature> baseFeatures, Comparator comparator){

        if(featureToTest == null){
            throw new IllegalStateException("Feature tree need to be defined to run local search");
        }

        // Evaluate the original feature
        double[] rootMetrics = Utils.computeMetricsSetNaNZero(featureToTest.getMatchesOriginalFeature(), super.labels, super.samples.size());

        BitSet originalMatches;
        String origianlName;
        if(featureToTest instanceof ConnectiveTester){
            originalMatches = ((ConnectiveTester) featureToTest).getMatches();
            origianlName = ((ConnectiveTester) featureToTest).getName();

        }else if(featureToTest instanceof IfThenStatementTester){
            originalMatches = ((IfThenStatementTester) featureToTest).getMatches();
            origianlName = ((IfThenStatementTester) featureToTest).getName();

        }else{
            throw new IllegalStateException("");
        }

        Feature currentBestFeature = new Feature(origianlName, originalMatches, rootMetrics[0], rootMetrics[1], rootMetrics[2], rootMetrics[3]);
        Feature savedBaseFeature = null;

        System.out.println("Argmax run");
        System.out.println(origianlName + "| precision: " +rootMetrics[2] + ", recall: " + rootMetrics[3]);

        // Add a base feature to the given feature, replacing the placeholder
        for(Feature baseFeature: baseFeatures){

            // Define which feature will be add to the current placeholder location
            featureToTest.setNewNode(baseFeature.getName(), baseFeature.getMatches());

            BitSet matches;
            String name;
            if(featureToTest instanceof ConnectiveTester){
                matches = ((ConnectiveTester) featureToTest).getMatches();
                name = ((ConnectiveTester) featureToTest).getName();

            }else if(featureToTest instanceof IfThenStatementTester){
                matches = ((IfThenStatementTester) featureToTest).getMatches();
                name = ((IfThenStatementTester) featureToTest).getName();

            }else{
                throw new IllegalStateException("");
            }

            double[] metrics = Utils.computeMetricsSetNaNZero(matches, super.labels, super.samples.size());
            if(Double.isNaN(metrics[0])){
                continue;
            }

            Feature newFeature = new Feature(name, matches, metrics[0], metrics[1], metrics[2], metrics[3]);
            newFeature.setNumExceptions(baseFeature.getNumExceptions());
            newFeature.setNumGeneralizedVariable(baseFeature.getNumGeneralizedVariable());

            System.out.println(name + "| precision: " + metrics[2] + ", recall: " + metrics[3]);

            if(comparator.compare(newFeature, currentBestFeature) == 0) {
                if(newFeature.getNumExceptions() < currentBestFeature.getNumExceptions()
                        || newFeature.getNumGeneralizedVariable() > currentBestFeature.getNumGeneralizedVariable()){
                    currentBestFeature = newFeature;
                    savedBaseFeature = baseFeature;
                }

            } else if(comparator.compare(newFeature, currentBestFeature) > 0){
                currentBestFeature = newFeature;
                savedBaseFeature = baseFeature;
            }
        }
        return savedBaseFeature;
    }

    /**
    * Adds extra conditions or exceptions to a node
    * @param root root of the tree to be modified
    * @param parent node where new conditions or exceptions are to be added
    * @param literalToBeCombined literal that the new condition is to be combined with (can be null)
    * @param baseFeaturesToTest base features
    * @param maxNumConditions the maximum number of conditions that can be added
    * @param metric metric used to select the best feature
    */
    public List<Feature> addExtraConditions(Connective root, Connective parent, Literal literalToBeCombined, List<Feature> baseFeaturesToTest, int maxNumConditions, FeatureMetric metric) {
        List<Connective> parentNodes = new ArrayList<>();
        parentNodes.add(parent);
        return this.addExtraConditions(root, parentNodes, literalToBeCombined, baseFeaturesToTest, maxNumConditions, metric);
    }

    /**
    * Adds extra conditions or exceptions to a node
    * @param root root of the tree to be modified
    * @param parentNodes parent of the nodes where new conditions or exceptions are to be added
    * @param literalToBeCombined literal that the new condition is to be combined with (can be null)
    * @param baseFeaturesToTest base features
    * @param maxNumConditions the maximum number of conditions that can be added
    * @param metric metric used to select the best feature
    */
    public List<Feature> addExtraConditions(Connective root, List<Connective> parentNodes, Literal literalToBeCombined, List<Feature> baseFeaturesToTest, int maxNumConditions, FeatureMetric metric){

        // Create tester
        ConnectiveTester tester = new ConnectiveTester(root);
        this.setRoot(tester);

        List<ConnectiveTester> parentTestNodes = new ArrayList<>();

        // Find the parent node within the tester tree
        for (Connective testNode : tester.getDescendantConnectives()) {
            for (Connective parent : parentNodes) {
                if (this.getFeatureHandler().featureTreeEquals(parent, testNode)) {
                    parentTestNodes.add((ConnectiveTester) testNode);
                }
            }
        }

        boolean combineLiteral = false;
        List<Literal> testerLiteralsToBeCombined = new ArrayList<>();
        if(literalToBeCombined != null){
            combineLiteral = true;
            for(ConnectiveTester parent: parentTestNodes){
                for(Literal literal: parent.getLiteralChildren()){
                    if(literal.hashCode() == literalToBeCombined.hashCode()){
                        testerLiteralsToBeCombined.add(literal);
                    }
                }
            }
        }

        // Define the comparator
        FeatureMetricComparator comparator = new FeatureMetricComparator(metric);

        List<Feature> addedConditions = new ArrayList<>();

        for(int i = 0; i < maxNumConditions; i++){
            for(int j = 0; j < parentTestNodes.size(); j++){
                ConnectiveTester parent = parentTestNodes.get(j);
                if(combineLiteral){
                    parent.setAddNewNode(testerLiteralsToBeCombined.get(j));
                }else{
                    parent.setAddNewNode();
                }
            }

            // Run local search
            Feature localSearchOutput = this.runArgmax(tester, baseFeaturesToTest, comparator);

            if(localSearchOutput == null){
                break;

            }else{
                addedConditions.add(localSearchOutput);

                // Modify the tester object
                tester.setNewNode(localSearchOutput.getName(), localSearchOutput.getMatches());
                tester.finalizeNewNodeAddition();

                // Directly modify the tree
                if(combineLiteral){
                    List<Connective> newParentNodes = new ArrayList<>();
                    List<ConnectiveTester> newParentTestNodes = new ArrayList<>();

                    for(Connective parent: parentNodes){
                        Literal toBeRemoved = null;
                        for(Literal literal: parent.getLiteralChildren()){
                            if(literal.hashCode() == literalToBeCombined.hashCode()){
                                toBeRemoved = literal;
                            }
                        }
                        // Remove the target node from its parent
                        parent.removeNode(toBeRemoved);

                        Connective tempBranch;
                        if(parent.getLogic() == LogicalConnectiveType.OR){
                            tempBranch = new Connective(LogicalConnectiveType.AND);
                        }else{
                            tempBranch = new Connective(LogicalConnectiveType.OR);
                        }
                        tempBranch.addNode(literalToBeCombined.copy());
                        tempBranch.addLiteral(localSearchOutput.getName(), localSearchOutput.getMatches());
                        parent.addBranch(tempBranch);

                        // Change pointers to the parent nodes
                        newParentNodes.add(tempBranch);
                    }

                    for(ConnectiveTester parentTestNode: parentTestNodes){
                        boolean updatedParentTestNode = false;
                        for(Connective branch: parentTestNode.getConnectiveChildren()){
                            if(branch.getLiteralChildren().size() == 2 && branch.getConnectiveChildren().isEmpty()){
                                for(Literal literal: branch.getLiteralChildren()){
                                    if(literal.hashCode() == literalToBeCombined.hashCode()){
                                        newParentTestNodes.add((ConnectiveTester) branch);
                                        updatedParentTestNode = true;
                                        break;
                                    }
                                }
                            }
                        }
                        if(!updatedParentTestNode){
                            throw new IllegalStateException("Error: parent not updated");
                        }
                    }

                    parentNodes = newParentNodes;
                    parentTestNodes = newParentTestNodes;
                    combineLiteral = false;

                }else{
                    for(Connective parent: parentNodes){
                        parent.addLiteral(localSearchOutput.getName(), localSearchOutput.getMatches());
                    }
                }
            }
        }
        return addedConditions;
    }

    public AbstractFilterFetcher getFilterFetcher() {
        return filterFetcher;
    }

    public AbstractFeatureFetcher getFeatureFetcher() {
        return featureFetcher;
    }

    public FeatureExpressionHandler getFeatureHandler() { return featureHandler; }

    public List<Feature> getBaseFeatures(){ return this.baseFeatures; }
}
