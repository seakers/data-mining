package ifeed.mining;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import ifeed.Utils;
import ifeed.architecture.AbstractArchitecture;
import ifeed.feature.*;
import ifeed.feature.logic.*;
import ifeed.filter.AbstractFilter;
import ifeed.filter.AbstractFilterFetcher;
import ifeed.local.params.BaseParams;
import ifeed.problem.assigning.filters.*;

import java.util.*;

public abstract class AbstractLocalSearch extends AbstractDataMiningBase implements AbstractDataMiningAlgorithm, InteractiveSearch {

    protected ConnectiveTester root;
    protected LogicalConnectiveType logic;
    protected List<Feature> baseFeatures;
    protected AbstractFeatureFetcher featureFetcher;
    protected AbstractFilterFetcher filterFetcher;
    protected FeatureExpressionHandler featureHandler;
    protected volatile boolean exit;

    public AbstractLocalSearch(BaseParams params,
                               List<AbstractArchitecture> architectures,
                               List<Integer> behavioral,
                               List<Integer> non_behavioral,
                               AbstractFeatureFetcher fetcher){

        super(params, architectures, behavioral, non_behavioral);
        this.featureFetcher = fetcher;
        this.filterFetcher = this.featureFetcher.getFilterFetcher();
        this.featureHandler = new FeatureExpressionHandler(this.featureFetcher);
        this.root = null;
        this.exit = false;
    }

    public AbstractLocalSearch init(List<Feature> baseFeatures){
        this.baseFeatures = baseFeatures;
        if(this.featureFetcher.getBaseFeatures().isEmpty()){
            this.featureFetcher.setBaseFeatures(this.baseFeatures);
        }
        return this;
    }

    public AbstractLocalSearch init(){
        if(this.baseFeatures == null){
            this.baseFeatures = super.generateBaseFeatures();
            if(this.featureFetcher.getBaseFeatures().isEmpty()){
                this.featureFetcher.setBaseFeatures(this.baseFeatures);
            }
        }
        return this;
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

    public List<Feature> runBothLogic(){
        List<Feature> out = new ArrayList<>();
        this.setLogic(LogicalConnectiveType.AND);
        out.addAll(this.run());
        this.setLogic(LogicalConnectiveType.OR);
        out.addAll(this.run());
        return out;
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
            if(this.getExitFlag()){
                return new ArrayList<>();
            }
            ConnectiveTester testNode = (ConnectiveTester) node;
            testNode.setAddNewNode();
            List<Feature> filteredBaseFeatures = this.filterBaseFeatures(testNode, this.baseFeatures);
            List<ifeed.feature.Feature> tempFeatures = this.testLocalChanges(this.root, filteredBaseFeatures);
            extractedFeatures.addAll(tempFeatures);
            testNode.cancelAddNode();
        }

        oppositeLogicConnectives = root.getDescendantConnectives(oppositeLogic);
        for(Connective node: oppositeLogicConnectives){
            if(this.getExitFlag()){
                return new ArrayList<>();
            }
            ConnectiveTester testNode = (ConnectiveTester) node;
            for(Literal literal: testNode.getLiteralChildren()){
                if(this.getExitFlag()){
                    return new ArrayList<>();
                }
                testNode.setAddNewNode(literal);
                List<Feature> filteredBaseFeatures = this.filterBaseFeatures(testNode, this.baseFeatures);
                List<ifeed.feature.Feature> tempFeatures = this.testLocalChanges(this.root, filteredBaseFeatures);
                extractedFeatures.addAll(tempFeatures);
                testNode.cancelAddNode();
            }
        }

        return extractedFeatures;
    }


    public List<Feature> runLocalSearchAND(ConnectiveTester root){
        // Initialize the extracted features
        List<ifeed.feature.Feature> extractedFeatures = new ArrayList<>();

        List<Connective> conjunctiveNodes = root.getDescendantConnectives(LogicalConnectiveType.AND);
        for(Connective node: conjunctiveNodes){
            if(this.getExitFlag()){
                return new ArrayList<>();
            }
            ConnectiveTester testNode = (ConnectiveTester) node;
            testNode.setAddNewNode();
            List<ifeed.feature.Feature> tempFeatures = this.testLocalChanges(root, this.baseFeatures);
            extractedFeatures.addAll(tempFeatures);
            testNode.cancelAddNode();
        }

        List<Connective> disjunctiveNodes = root.getDescendantConnectives(LogicalConnectiveType.OR);
        for(Connective node: disjunctiveNodes){
            if(this.getExitFlag()){
                return new ArrayList<>();
            }
            ConnectiveTester testNode = (ConnectiveTester) node;
            for(Literal literal: testNode.getLiteralChildren()){
                if(this.getExitFlag()){
                    return new ArrayList<>();
                }
                testNode.setAddNewNode(literal);
                List<Feature> filteredBaseFeatures = this.filterBaseFeatures(testNode, this.baseFeatures);
                List<ifeed.feature.Feature> tempFeatures = this.testLocalChanges(root, filteredBaseFeatures);
                extractedFeatures.addAll(tempFeatures);
                testNode.cancelAddNode();
            }
        }
        return extractedFeatures;
    }

    public List<Feature> runLocalSearchOR(ConnectiveTester root){

        List<String> allowedClasses = new ArrayList<>();
        allowedClasses.add("inOrbit");
        allowedClasses.add("notInOrbit");
        allowedClasses.add("present");
        allowedClasses.add("absent");
        allowedClasses.add("separate");
        allowedClasses.add("together");
        allowedClasses.add("emptyOrbit");
//        allowedClasses.add("numOrbits");
//        allowedClasses.add("numInstruments");
//        allowedClasses.add("numInstrumentsInOrbit");

        // Save the coverage of the original feature
        double[] initialMetrics = Utils.computeMetricsSetNaNZero(root.getMatches(), super.labels, super.samples.size());
        double initialCoverage = initialMetrics[3];

        // Select the literal that decreases coverage the most
        double largestCoverageDiff = 0.0;
        ConnectiveTester savedParent = null;
        Literal savedLiteral = null;

        List<Connective> parentNodes = root.getDescendantConnectives(LogicalConnectiveType.AND);
        for(Connective parent: parentNodes){
            if(parent.getLiteralChildren().size() == 1){
                continue;
            }

            for(Literal literal: parent.getLiteralChildren()){

                boolean allowed = false;
                String name = literal.getName();
                for(String featureName: allowedClasses){
                    if(name.contains(featureName)) {
                        allowed = true;
                    }
                }

                if(name.contains("separate") || name.contains("together")){
                    AbstractFilter filter = filterFetcher.fetch(name);
                    if(filter instanceof Separate){
                        if(((Separate) filter).getInstruments().size() > 2){
                            allowed = false;
                        }
                    } else if(filter instanceof Together){
                        if(((Together) filter).getInstruments().size() > 2){
                            allowed = false;
                        }
                    }
                }

                if(!allowed){
                    continue;
                }

                // Copy the feature tree
                ConnectiveTester rootCopy = root.copy();
                ConnectiveTester parentCopy = (ConnectiveTester) featureHandler.findMatchingNodes(rootCopy, parent).get(0);
                Literal literalCopy = (Literal) featureHandler.findMatchingNodes(parentCopy, literal).get(0);

                // Try removing the literal
                parentCopy.removeNode(literalCopy);

                double[] metrics = Utils.computeMetricsSetNaNZero(rootCopy.getMatches(), super.labels, super.samples.size());
                if(Double.isNaN(metrics[0])){
                    continue;
                }

                // must be larger than the initial coverage, since a literal is removed from the feature
                double coverageDiff = metrics[3] - initialCoverage;
                if(coverageDiff > largestCoverageDiff){
                    largestCoverageDiff = coverageDiff;
                    savedParent = (ConnectiveTester) parent;
                    savedLiteral = literal;
                }
            }
        }

        if(savedLiteral == null){
            throw new IllegalStateException();
        }

        ConnectiveTester parent = (ConnectiveTester) featureHandler.findMatchingNodes(root, savedParent).get(0);
        Literal literal = (Literal) featureHandler.findMatchingNodes(root, savedLiteral).get(0);
        AbstractFilter filter = this.filterFetcher.fetch(literal.getName());
        AbstractFilter selectedFilter = null;
        AbstractFilter oppositeFilter = null;

        // Remove the selected literal from the parent
        parent.removeNode(literal);

        // Find the opposite filter
        if(filter instanceof InOrbit || filter instanceof NotInOrbit){
            // If the num of instruments is more than one, find the instrument that decreases coverage the most

            int orbit = -1;
            Multiset<Integer> instruments = HashMultiset.create();;
            if(filter instanceof InOrbit){
                orbit = ((InOrbit) filter).getOrbit();
                instruments = ((InOrbit) filter).getInstruments();
            } else if(filter instanceof NotInOrbit){
                orbit = ((NotInOrbit) filter).getOrbit();
                instruments = ((NotInOrbit) filter).getInstruments();
            }

            if(instruments.size() == 1){
                if(filter instanceof InOrbit){
                    selectedFilter = filter;
                    oppositeFilter = new NotInOrbit(params, orbit, instruments);
                } else if(filter instanceof NotInOrbit){
                    selectedFilter = filter;
                    oppositeFilter = new InOrbit(params, orbit, instruments);
                }

            } else {
                int savedVariable = -1;
                largestCoverageDiff = 0.0;

                Set<Integer> tempInstrumentSet = new HashSet<>(instruments);
                for(int i: tempInstrumentSet){
                    instruments.remove(i);

                    Feature tempFeature = featureFetcher.fetch(filter.toString());
                    Literal tempLiteral = new Literal(tempFeature.getName(), tempFeature.getMatches());
                    parent.addNode(tempLiteral);

                    double[] metrics = Utils.computeMetricsSetNaNZero(root.getMatches(), super.labels, super.samples.size());
                    if(Double.isNaN(metrics[0])){
                        continue;
                    }

                    // must be larger than the initial coverage, since a literal is removed from the feature
                    double coverageDiff = metrics[3] - initialCoverage;
                    if(coverageDiff > largestCoverageDiff){
                        largestCoverageDiff = coverageDiff;
                        savedVariable = i;
                    }

                    parent.removeLiteral(tempLiteral);
                    instruments.add(i);
                }

                instruments.remove(savedVariable);
                Feature tempFeature = featureFetcher.fetch(filter.toString());
                Literal tempLiteral = new Literal(tempFeature.getName(), tempFeature.getMatches());
                parent.addNode(tempLiteral);

                if(filter instanceof InOrbit){
                    selectedFilter = new InOrbit(params, orbit, savedVariable);
                    oppositeFilter = new NotInOrbit(params, orbit, savedVariable);
                } else if(filter instanceof NotInOrbit){
                    selectedFilter = new NotInOrbit(params, orbit, savedVariable);
                    oppositeFilter = new InOrbit(params, orbit, savedVariable);
                }
            }

        }else if(filter instanceof Present){
            selectedFilter = filter;
            oppositeFilter = new Absent(params, ((Present) filter).getInstrument());

        }else if(filter instanceof Absent){
            selectedFilter = filter;
            oppositeFilter = new Present(params, ((Absent) filter).getInstrument());

        }else if(filter instanceof Separate){
            selectedFilter = filter;
            oppositeFilter = new Together(params, ((Separate) filter).getInstruments());

        }else if(filter instanceof Together) {
            selectedFilter = filter;
            oppositeFilter = new Separate(params, ((Together) filter).getInstruments());

        }else if(filter instanceof EmptyOrbit) {
            selectedFilter = filter;
            int[] bounds = new int[2];
            bounds[0] = 1;
            bounds[1] = 12;
            oppositeFilter = new NumInstrumentsInOrbit(params, ((EmptyOrbit)filter).getOrbit(), -1, bounds);

        }else{
            throw new IllegalStateException();
        }

        ConnectiveTester newBranch = new ConnectiveTester(LogicalConnectiveType.OR);
        Feature tempFeature1 = featureFetcher.fetch(selectedFilter.toString());
        Feature tempFeature2 = featureFetcher.fetch(oppositeFilter.toString());
        Literal tempLiteral1 = new Literal(tempFeature1.getName(), tempFeature1.getMatches());
        Literal tempLiteral2 = new Literal(tempFeature2.getName(), tempFeature2.getMatches());
        newBranch.addLiteral(tempLiteral1);
        newBranch.addLiteral(tempLiteral2);
        parent.addNode(newBranch);

        // Initialize the extracted features
        List<ifeed.feature.Feature> extractedFeatures = new ArrayList<>();

        newBranch.setAddNewNode(tempLiteral1);
        extractedFeatures.addAll(this.runLocalSearchAND(root));
        newBranch.setAddNewNode(tempLiteral2);
        extractedFeatures.addAll(this.runLocalSearchAND(root));
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
            if(this.getExitFlag()){
                return new ArrayList<>();
            }

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

    public Feature runArgmax(LocalSearchTester featureToTest, List<Feature> baseFeatures, Comparator comparator){
        return runArgmax(featureToTest, baseFeatures, comparator, true);
    }

    /**
     * Finds the base feature that maximizes a certain metric
     * @param baseFeatures
     * @param comparator
     * @return
     */
    public Feature runArgmax(LocalSearchTester featureToTest, List<Feature> baseFeatures, Comparator comparator, boolean includeSelf){

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

        System.out.println("Argmax run");
        GeneralizableFeature currentBestFeature;
        if(includeSelf){
            currentBestFeature = new GeneralizableFeature(origianlName, originalMatches, rootMetrics[0], rootMetrics[1], rootMetrics[2], rootMetrics[3]);
            System.out.println(origianlName + "| precision: " +rootMetrics[2] + ", recall: " + rootMetrics[3]);
        }else{
            currentBestFeature = null;
        }
        Feature savedBaseFeature = null;

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

            GeneralizableFeature newFeature = new GeneralizableFeature(name, matches, metrics[0], metrics[1], metrics[2], metrics[3]);
            if(baseFeature instanceof GeneralizableFeature){
                // Inherit the number of generaliations and the number of exception variables from the base feature
                newFeature.setNumExceptionVariables(((GeneralizableFeature) baseFeature).getNumExceptionVariables());
                newFeature.setNumGeneralizations(((GeneralizableFeature) baseFeature).getNumGeneralizations());
            }

            System.out.println(name + "| precision: " + metrics[2] + ", recall: " + metrics[3]);

            if(currentBestFeature == null){
                currentBestFeature = newFeature;
                savedBaseFeature = baseFeature;

            }else{
                if(comparator.compare(newFeature, currentBestFeature) == 0) {
                    if(newFeature.getNumGeneralizations() > currentBestFeature.getNumGeneralizations()){
                        currentBestFeature = newFeature;
                        savedBaseFeature = baseFeature;
                    }else if(newFeature.getNumExceptionVariables() < currentBestFeature.getNumExceptionVariables()){
                        currentBestFeature = newFeature;
                        savedBaseFeature = baseFeature;
                    }

                } else if(comparator.compare(newFeature, currentBestFeature) > 0){
                    currentBestFeature = newFeature;
                    savedBaseFeature = baseFeature;
                }
            }
        }

//        if(savedBaseFeature != null){
//            System.out.println("Selected base feature: " + savedBaseFeature.getName());
//        }else{
//            System.out.println("No base feature selected ");
//        }

        return savedBaseFeature;
    }

    public List<Feature> addExtraConditions(Connective root, Connective parent, Literal literalToBeCombined, List<Feature> baseFeaturesToTest, int maxNumConditions, FeatureMetric metric) {
        return this.addExtraConditions(root, parent, literalToBeCombined, baseFeaturesToTest, maxNumConditions, metric, true);
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
    public List<Feature> addExtraConditions(Connective root, Connective parent, Literal literalToBeCombined, List<Feature> baseFeaturesToTest, int maxNumConditions, FeatureMetric metric, boolean includeSelf) {
        List<Connective> parentNodes = new ArrayList<>();
        parentNodes.add(parent);
        return this.addExtraConditions(root, parentNodes, literalToBeCombined, baseFeaturesToTest, maxNumConditions, metric, includeSelf);
    }

    public List<Feature> addExtraConditions(Connective root, List<Connective> parentNodes, Literal literalToBeCombined, List<Feature> baseFeaturesToTest, int maxNumConditions, FeatureMetric metric){
        return this.addExtraConditions(root, parentNodes, literalToBeCombined, baseFeaturesToTest, maxNumConditions, metric, true);
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
    public List<Feature> addExtraConditions(Connective root, List<Connective> parentNodes, Literal literalToBeCombined, List<Feature> baseFeaturesToTest, int maxNumConditions, FeatureMetric metric, boolean includeSelf){

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
        FeatureMetricEpsilonComparator comparator = new FeatureMetricEpsilonComparator(metric,0.01);

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

            if(this.getExitFlag()){
                return new ArrayList<>();
            }

            // Run local search
            Feature localSearchOutput = this.runArgmax(tester, baseFeaturesToTest, comparator, includeSelf);

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

    @Override
    public void stop(){
        this.exit = true;
    }

    @Override
    public boolean getExitFlag(){
        return this.exit;
    }
}
