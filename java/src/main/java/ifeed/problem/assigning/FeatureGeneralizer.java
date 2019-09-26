package ifeed.problem.assigning;

import ifeed.Utils;
import ifeed.architecture.AbstractArchitecture;
import ifeed.feature.*;
import ifeed.feature.logic.Connective;
import ifeed.feature.logic.Formula;
import ifeed.feature.logic.Literal;
import ifeed.filter.AbstractFilter;
import ifeed.filter.AbstractFilterFetcher;
import ifeed.local.params.BaseParams;
import ifeed.mining.AbstractLocalSearch;
import ifeed.mining.moea.GPMOEABase;
import ifeed.mining.moea.operators.AbstractExhaustiveSearchOperator;
import ifeed.mining.moea.operators.AbstractLogicOperator;
import ifeed.ontology.OntologyManager;
import ifeed.problem.assigning.logicOperators.generalization.combined.localSearch.*;
import ifeed.problem.assigning.logicOperators.generalization.single.InstrumentGeneralizer;
import ifeed.problem.assigning.logicOperators.generalization.single.InstrumentGeneralizerExhaustive;
import ifeed.problem.assigning.logicOperators.generalization.single.NotInOrbit2Absent;
import ifeed.problem.assigning.logicOperators.generalization.single.OrbitGeneralizer;
import ifeed.problem.assigning.logicOperators.generalization.single.localSearch.NotInOrbit2EmptyOrbitWithException;
import ifeed.problem.assigning.logicOperators.generalization.single.localSearch.NotInOrbitInstrGeneralizationWithException;

import java.util.*;

public class FeatureGeneralizer extends AbstractFeatureGeneralizer{

    private AbstractFeatureFetcher featureFetcher;
    private AbstractFilterFetcher filterFetcher;
    private FeatureExpressionHandler expressionHandler;
    private BitSet labels;
    private GPMOEABase base;
    private AbstractLocalSearch localSearch;
    private FeatureSimplifier simplifier;

    public FeatureGeneralizer(BaseParams params,
                              List<AbstractArchitecture> architectures,
                              List<Integer> behavioral,
                              List<Integer> non_behavioral,
                              OntologyManager ontologyManager){

        super(params, architectures, behavioral, non_behavioral, ontologyManager);

        // Set label
        this.labels = new BitSet(architectures.size());
        for (int i = 0; i < architectures.size(); i++) {
            AbstractArchitecture a = architectures.get(i);
            if (behavioral.contains(a.getID())) {
                this.labels.set(i);
            }
        }
    }

    public void initialize(){
        if(this.featureFetcher == null){
            this.featureFetcher = new FeatureFetcher(params, architectures);
        }

        if(this.filterFetcher == null){
            this.filterFetcher = featureFetcher.getFilterFetcher();
        }

        if(this.expressionHandler == null){
            this.expressionHandler = new FeatureExpressionHandler(featureFetcher);
        }

        if(this.base == null){
            this.base = new GPMOEA(params, architectures, behavioral, non_behavioral);
            this.base.init();
            this.simplifier = new FeatureSimplifier(params, (FeatureFetcher) base.getFeatureFetcher());
        }

        if(this.localSearch == null){
            this.localSearch = new LocalSearch(params, architectures, behavioral, non_behavioral);
            this.localSearch.init(this.base.getBaseFeatures());
        }
    }

    @Override
    public Set<FeatureWithDescription> generalize(String rootFeatureExpression, String nodeFeatureExpression){
        this.initialize();

        if(super.getExitFlag()){
            return new HashSet<>();
        }

        // Create a tree structure based on the given feature expression
        Connective root = expressionHandler.generateFeatureTree(rootFeatureExpression);

        Formula node;
        if(rootFeatureExpression.equalsIgnoreCase(nodeFeatureExpression) || nodeFeatureExpression == null || nodeFeatureExpression.trim().length() == 0){
            // The whole feature tree is used
            node = null;

        }else{
            // Only a part of the feature tree is used
            Connective tempRoot = expressionHandler.generateFeatureTree(nodeFeatureExpression);

            Formula nodeToBeSearched;
            if(tempRoot.getConnectiveChildren().isEmpty() && tempRoot.getLiteralChildren().size() == 1){
                // node is a literal
                nodeToBeSearched = tempRoot.getLiteralChildren().get(0);

            }else{
                // node is a logical connective node
                nodeToBeSearched = tempRoot;
            }

            // Get node
            List<Formula> nodes = expressionHandler.findMatchingNodes(root, nodeToBeSearched);
            if(nodes.isEmpty()){
                throw new IllegalStateException("Node " + nodeFeatureExpression + " not found from " + root.getName());
            }else if(nodes.size() > 1){
                throw new IllegalStateException("Node " + nodeFeatureExpression + " found more than once from " + root.getName());
            }
            node = nodes.get(0);
        }

        // Create an empty set where the features will be stored
        Set<FeatureWithDescription> generalizedFeaturesWithDescription = new HashSet<>();

        List<AbstractLogicOperator> combinedGeneralizationOperators = new ArrayList<>();
        combinedGeneralizationOperators.add(new NotInOrbits2AbsentWithException(params, base, localSearch));
        combinedGeneralizationOperators.add(new NotInOrbitsOrbGeneralizationWithException(params, base, localSearch));
        combinedGeneralizationOperators.add(new NotInOrbitInstrGeneralizationWithException(params, base, localSearch));
        combinedGeneralizationOperators.add(new NotInOrbit2EmptyOrbitWithException(params, base, localSearch));
        combinedGeneralizationOperators.add(new Separates2AbsentWithException(params, base, localSearch));
        combinedGeneralizationOperators.add(new SeparatesGeneralizationWithException(params, base, localSearch));
        combinedGeneralizationOperators.add(new InOrbitsOrbGeneralizationWithLocalSearch(params, base, localSearch));
        combinedGeneralizationOperators.add(new InOrbitsInstrGeneralizationWithLocalSearch(params, base, localSearch));
        generalizedFeaturesWithDescription.addAll(this.runExhaustiveGeneralizationSearch(combinedGeneralizationOperators, root, node));

        List<AbstractLogicOperator> singleGeneralizationOperators = new ArrayList<>();
        singleGeneralizationOperators.add(new InstrumentGeneralizerExhaustive(params, base));
        generalizedFeaturesWithDescription.addAll(this.runExhaustiveGeneralizationSearch(singleGeneralizationOperators, root, node));

        System.out.println("Total generalized features found: " + generalizedFeaturesWithDescription.size());
        return generalizedFeaturesWithDescription;
    }

    public List<FeatureWithDescription> runExhaustiveGeneralizationSearch(List<AbstractLogicOperator> operators, Connective root, Formula node){
        if(super.getExitFlag()){
            return new ArrayList<>();
        }

        Set<Integer> uniqueFeatureHashCode = new HashSet<>();

        // Save the initial feature
        uniqueFeatureHashCode.add(root.hashCode());

        List<Feature> nonDominatedFeatures = new ArrayList<>();
        List<Feature> dominatingFeatures = new ArrayList<>();
        List<List<String>> dominatingFeaturesDesc = new ArrayList<>();
        List<List<String>> nonDominatedFeaturesDesc = new ArrayList<>();

        FeatureMetricComparator comparator1 = new FeatureMetricComparator(FeatureMetric.PRECISION);
        FeatureMetricComparator comparator2 = new FeatureMetricComparator(FeatureMetric.RECALL);
        List<Comparator> comparators = new ArrayList<>(Arrays.asList(comparator1,comparator2));

        // Set input feature
        double[] metrics = Utils.computeMetricsSetNaNZero(root.getMatches(), this.labels, this.architectures.size());
        Feature inputFeature = new Feature(root.getName(), root.getMatches(), metrics[0], metrics[1], metrics[2], metrics[3]);

        System.out.println("original feature: " + root.getName() + ", metrics: " + metrics[2] + ", " + metrics[3]);

        boolean targetNodeGiven = true;
        AbstractFilter targetNodeFilter = null;
        if(node == null){
            node = root;
            targetNodeGiven = false;
        }else{
            if(node instanceof Literal){
                targetNodeFilter = this.filterFetcher.fetch(node.getName());
            }
        }

        // For each exhaustive search operator
        for(AbstractLogicOperator operator: operators){
            // Find potential parent nodes
            List<Connective> parentNodesOfApplicableNodes = operator.getParentNodesOfApplicableNodes(root, operator.getLogic());

            // Current operator not applicable
            if(parentNodesOfApplicableNodes.isEmpty()){
                continue;
            }

            // For each parent node
            for(Connective parentNodeOfApplicableNodes: parentNodesOfApplicableNodes){
                if(targetNodeGiven && node instanceof Connective){
                    if(expressionHandler.featureTreeEquals(parentNodeOfApplicableNodes, (Connective)node)){
                        // If the target node is a branch, then only the parent node that matches the target is considered
                    }else{
                        continue;
                    }
                }

                // Find the applicable nodes under the given parent node
                Map<AbstractFilter, Set<AbstractFilter>> applicableFiltersMap = new HashMap<>();
                Map<AbstractFilter, Literal> applicableLiteralsMap = new HashMap<>();
                operator.findApplicableNodesUnderGivenParentNode(parentNodeOfApplicableNodes, applicableFiltersMap, applicableLiteralsMap);

                // Reset the search for each parent node tested
                ((AbstractExhaustiveSearchOperator) operator).resetSearch();
                for(AbstractFilter constraintSetter: applicableFiltersMap.keySet()){
                    if(targetNodeGiven && node instanceof Literal){
                        if(constraintSetter.hashCode() == targetNodeFilter.hashCode()){
                            // If the target node is a literal, then only the literal that matches the target is considered
                        }else{
                            continue;
                        }
                    }

                    ((AbstractExhaustiveSearchOperator) operator).resetSearchForGivenConstraintSetter();
                    while(!((AbstractExhaustiveSearchOperator)operator).isSearchFinished()){
                        if(super.getExitFlag()){ // Search stopped by user
                            return new ArrayList<>();
                        }

                        // Copy features
                        Connective rootCopy = root.copy();
                        Connective parentNode = (Connective) expressionHandler.findMatchingNodes(rootCopy, parentNodeOfApplicableNodes).get(0);

                        Map<AbstractFilter, Literal> applicableLiteralsMapCopy = new HashMap<>();
                        for(AbstractFilter filter: applicableLiteralsMap.keySet()){
                            for(Literal childNode: parentNode.getLiteralChildren()){
                                if(filter.hashCode() == this.filterFetcher.fetch(childNode.getName()).hashCode()){
                                    applicableLiteralsMapCopy.put(filter, childNode);
                                    break;
                                }
                            }
                        }

                        Set<AbstractFilter> matchingFilters = applicableFiltersMap.get(constraintSetter);
                        List<String> opDescription = new ArrayList<>();

                        System.out.println("====== " + operator.getClass().getSimpleName() + " =======");
                        System.out.println("Constraint setter feature: " + constraintSetter.toString());

                        // Modify the nodes using the given argument
                        boolean modified = operator.apply(rootCopy, parentNode, constraintSetter, matchingFilters, applicableLiteralsMap, opDescription);

                        if(!modified){
                            continue;
                        }

                        // Simplify the feature
                        simplifier.simplify(rootCopy);

                        // Retain only the unique set of features
                        if(uniqueFeatureHashCode.contains(rootCopy.hashCode())){
                            continue;
                        }else{
                            uniqueFeatureHashCode.add(rootCopy.hashCode());
                        }

                        // Compute metrics
                        metrics = Utils.computeMetricsSetNaNZero(rootCopy.getMatches(), this.labels, this.architectures.size());
                        Feature generalizedFeature = new Feature(rootCopy.getName(), rootCopy.getMatches(), metrics[0], metrics[1], metrics[2], metrics[3]);

                        if(Utils.dominates(inputFeature, generalizedFeature, comparators)){
                            // The new feature is dominated by the input feature
                            continue;

                        }else{
                            if(Utils.dominates(generalizedFeature, inputFeature, comparators)){
                                // The new feature dominates the input feature
                                dominatingFeatures.add(generalizedFeature);
                                dominatingFeaturesDesc.add(opDescription);
                            }
                            nonDominatedFeatures.add(generalizedFeature);
                            nonDominatedFeaturesDesc.add(opDescription);
                        }

                        System.out.println("Extracted generalized feature: " + rootCopy.getName() + " | " + metrics[2] + ", " + metrics[3]);
                        // Print description if it is available
                        if(!opDescription.isEmpty()){
                            System.out.println("Description: ");
                            for(int i = 0; i < opDescription.size(); i++){
                                System.out.println(opDescription.get(i));
                            }
                        }
                    }
                }
            }
        }

        // If there are features that dominate the input feature, return those.
        List<FeatureWithDescription> outputFeaturesWithDescription = new ArrayList<>();
        List<Feature> outputFeatures;
        List<List<String>> outputDescription;
        if(!dominatingFeatures.isEmpty()){
            outputFeatures = dominatingFeatures;
            outputDescription = dominatingFeaturesDesc;

        }else{
            // Otherwise, return all non-dominated features
            outputFeatures = nonDominatedFeatures;
            outputDescription = nonDominatedFeaturesDesc;
        }

        for(int i = 0; i < outputFeatures.size(); i++){
            StringJoiner sj = new StringJoiner("\n");
            for(String desc: outputDescription.get(i)){
                sj.add(desc);
            }

            FeatureWithDescription feature = new FeatureWithDescription(outputFeatures.get(i), sj.toString());
            outputFeaturesWithDescription.add(feature);
        }
        return outputFeaturesWithDescription;
    }

    public List<FeatureWithDescription> runRandomGeneralizationSearch(AbstractLogicOperator operator, Connective root, Formula node, int numTrials){
        List<AbstractLogicOperator> operators = new ArrayList<>();
        operators.add(operator);
        return this.runRandomGeneralizationSearch(operators, root, node, numTrials);
    }

    public List<FeatureWithDescription> runRandomGeneralizationSearch(List<AbstractLogicOperator> operators, Connective root, Formula node, int numTrials){
        if(super.getExitFlag()){
            return new ArrayList<>();
        }

        // Number of trials for each operator
        int cnt = numTrials;

        // Get random number generator
        Random random = new Random();

        Set<Integer> uniqueFeatureHashCode = new HashSet<>();
        List<Feature> nonDominatedFeatures = new ArrayList<>();
        List<Feature> dominatingFeatures = new ArrayList<>();
        List<List<String>> dominatingFeaturesDesc = new ArrayList<>();
        List<List<String>> nonDominatedFeaturesDesc = new ArrayList<>();

        FeatureMetricEpsilonComparator comparator1 = new FeatureMetricEpsilonComparator(FeatureMetric.PRECISION, 0.05);
        FeatureMetricEpsilonComparator comparator2 = new FeatureMetricEpsilonComparator(FeatureMetric.RECALL, 0.05);
        List<Comparator> comparators = new ArrayList<>(Arrays.asList(comparator1,comparator2));

        // Set input feature
        double[] metrics = Utils.computeMetricsSetNaNZero(root.getMatches(), this.labels, this.architectures.size());
        Feature inputFeature = new Feature(root.getName(), root.getMatches(), metrics[0], metrics[1], metrics[2], metrics[3]);


        System.out.println("original feature: " + root.getName() + ", metrics: " + metrics[2] + ", " + metrics[3]);

        boolean targetNodeGiven = true;
        AbstractFilter targetNodeFilter = null;
        if(node == null){
            node = root;
            targetNodeGiven = false;
        }else{
            if(node instanceof Literal){
                targetNodeFilter = this.filterFetcher.fetch(node.getName());
            }
        }

        // For each generalization search operator
        for (AbstractLogicOperator operator : operators) {

            // Find potential parent nodes
            List<Connective> parentNodesOfApplicableNodes = operator.getParentNodesOfApplicableNodes(root, operator.getLogic());

            // Current operator not applicable
            if (parentNodesOfApplicableNodes.isEmpty()) {
                continue;
            }

            for(int i = 0; i < cnt; i++) {
                // Stop the search whenever the exit flag is activated
                if (super.getExitFlag()) {
                    return new ArrayList<>();
                }

                // Select the parent node
                Connective parentNodeOfApplicableNodes;

                if (targetNodeGiven && node instanceof Connective) {
                    boolean targetNodeFound = false;
                    parentNodeOfApplicableNodes = parentNodesOfApplicableNodes.get(0); // placeholder

                    for(Connective tempNode: parentNodesOfApplicableNodes){
                        // If the target node is a branch, then only the parent node that matches the target is considered
                        if (expressionHandler.featureTreeEquals(tempNode, (Connective) node)) {
                            parentNodeOfApplicableNodes = tempNode;
                            targetNodeFound = true;
                            break;
                        }
                    }
                    if(!targetNodeFound){
                        throw new IllegalStateException();
                    }

                } else {
                    parentNodeOfApplicableNodes = parentNodesOfApplicableNodes.get(random.nextInt(parentNodesOfApplicableNodes.size()));
                }

                // Find the applicable nodes under the given parent node
                Map<AbstractFilter, Set<AbstractFilter>> applicableFiltersMap = new HashMap<>();
                Map<AbstractFilter, Literal> applicableLiteralsMap = new HashMap<>();
                operator.findApplicableNodesUnderGivenParentNode(parentNodeOfApplicableNodes, applicableFiltersMap, applicableLiteralsMap);

                // Select a constraint setter node
                AbstractFilter constraintSetter;
                if (targetNodeGiven && node instanceof Literal) {

                    // If the target node is a literal, then only the literal that matches the target is considered
                    boolean targetNodeFound = false;
                    constraintSetter = applicableFiltersMap.keySet().iterator().next();

                    for (AbstractFilter tempFilter : applicableFiltersMap.keySet()) {
                        if (tempFilter.hashCode() == targetNodeFilter.hashCode()) {
                            constraintSetter = tempFilter;
                            targetNodeFound = true;
                        }
                    }

                    if(!targetNodeFound){
                        throw new IllegalStateException();
                    }

                } else {
                    List<AbstractFilter> filtersList = new ArrayList<>(applicableFiltersMap.keySet());
                    constraintSetter = filtersList.get(random.nextInt(filtersList.size()));
                }


                // Copy features
                Connective rootCopy = root.copy();
                Connective parentNode = (Connective) expressionHandler.findMatchingNodes(rootCopy, parentNodeOfApplicableNodes).get(0);

                Map<AbstractFilter, Literal> applicableLiteralsMapCopy = new HashMap<>();
                for (AbstractFilter filter : applicableLiteralsMap.keySet()) {
                    for (Literal childNode : parentNode.getLiteralChildren()) {
                        if (filter.hashCode() == this.filterFetcher.fetch(childNode.getName()).hashCode()) {
                            applicableLiteralsMapCopy.put(filter, childNode);
                            break;
                        }
                    }
                }

                Set<AbstractFilter> matchingFilters = applicableFiltersMap.get(constraintSetter);
                List<String> opDescription = new ArrayList<>();

                System.out.println("====== " + operator.getClass().getSimpleName() + " =======");
                System.out.println("Constraint setter feature: " + constraintSetter.toString());

                // Modify the nodes using the given argument
                boolean modified = operator.apply(rootCopy, parentNode, constraintSetter, matchingFilters, applicableLiteralsMap, opDescription);

                if (!modified) {
                    continue;
                }

                // Simplify the feature
                simplifier.simplify(rootCopy);

                // Retain only the unique set of features
                if (uniqueFeatureHashCode.contains(rootCopy.hashCode())) {
                    continue;
                } else {
                    uniqueFeatureHashCode.add(rootCopy.hashCode());
                }

                // Compute metrics
                metrics = Utils.computeMetricsSetNaNZero(rootCopy.getMatches(), this.labels, this.architectures.size());
                Feature generalizedFeature = new Feature(rootCopy.getName(), rootCopy.getMatches(), metrics[0], metrics[1], metrics[2], metrics[3]);

                if (Utils.dominates(inputFeature, generalizedFeature, comparators)) {
                    // The new feature is dominated by the input feature
                    continue;

                } else {
                    if (Utils.dominates(generalizedFeature, inputFeature, comparators)) {
                        // The new feature dominates the input feature
                        dominatingFeatures.add(generalizedFeature);
                        dominatingFeaturesDesc.add(opDescription);
                    }
                    nonDominatedFeatures.add(generalizedFeature);
                    nonDominatedFeaturesDesc.add(opDescription);
                }

                System.out.println("Extracted generalized feature: " + rootCopy.getName() + " | " + metrics[2] + ", " + metrics[3]);
                // Print description if it is available
                if (!opDescription.isEmpty()) {
                    System.out.println("Description: ");
                    for (int j = 0; j < opDescription.size(); j++) {
                        System.out.println(opDescription.get(j));
                    }
                }
            }
        }

        // If there are features that dominate the input feature, return those.
        List<FeatureWithDescription> outputFeaturesWithDescription = new ArrayList<>();
        List<Feature> outputFeatures;
        List<List<String>> outputDescription;
        if (!dominatingFeatures.isEmpty()) {
            outputFeatures = dominatingFeatures;
            outputDescription = dominatingFeaturesDesc;

        } else {
            // Otherwise, return all non-dominated features
            outputFeatures = nonDominatedFeatures;
            outputDescription = nonDominatedFeaturesDesc;
        }

        for (int i = 0; i < outputFeatures.size(); i++) {
            StringJoiner sj = new StringJoiner("\n");
            for (String desc : outputDescription.get(i)) {
                sj.add(desc);
            }
            FeatureWithDescription feature = new FeatureWithDescription(outputFeatures.get(i), sj.toString());
            outputFeaturesWithDescription.add(feature);
        }
        return outputFeaturesWithDescription;
    }

    @Override
    public void stop(){
        this.exit = true;
        if(this.localSearch != null){
            this.localSearch.stop();
        }
        if(this.base != null){
            this.base.stop();
        }
    }
}
