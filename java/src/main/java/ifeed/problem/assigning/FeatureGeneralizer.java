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
import ifeed.mining.moea.operators.AbstractGeneralizationOperator;
import ifeed.mining.moea.operators.AbstractLogicOperator;
import ifeed.ontology.OntologyManager;
import ifeed.problem.assigning.logicOperators.generalization.combined.*;
import ifeed.problem.assigning.logicOperators.generalization.combined.localSearch.*;
import ifeed.problem.assigning.logicOperators.generalization.single.NotInOrbit2EmptyOrbit;
import ifeed.problem.assigning.logicOperators.generalization.single.NotInOrbitInstrGeneralizer;
import ifeed.problem.assigning.logicOperators.generalization.single.localSearch.*;

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
                //Collections.shuffle(nodes);
                throw new IllegalStateException("Node " + nodeFeatureExpression + " found more than once from " + root.getName());

            }
            node = nodes.get(0);
        }

        // Create an empty set where the features will be stored
        Set<FeatureWithDescription> generalizedFeaturesWithDescription = new HashSet<>();

        List<AbstractLogicOperator> combinedGeneralizationOperators = new ArrayList<>();
//        generalizationOperators.add(new InOrbits2PresentWithLocalSearch(params, base, localSearch));
//        generalizationOperators.add(new InOrbitsOrbGeneralizationWithLocalSearch(params, base, localSearch));
//        generalizationOperators.add(new InOrbitsInstrGeneralizationWithLocalSearch(params, base, localSearch));

        combinedGeneralizationOperators.add(new NotInOrbits2AbsentWithException(params, base, localSearch));
        combinedGeneralizationOperators.add(new NotInOrbitsOrbGeneralizationWithException(params, base, localSearch));
        combinedGeneralizationOperators.add(new NotInOrbitInstrGeneralizationWithException(params, base, localSearch));
//        combinedGeneralizationOperators.add(new Separates2AbsentWithException(params, base, localSearch));
//
        generalizedFeaturesWithDescription.addAll(this.runExhaustiveGeneralizationSearch(combinedGeneralizationOperators, root, node));
        generalizedFeaturesWithDescription.addAll(this.apply(new NotInOrbit2EmptyOrbitWithException(params, base, localSearch), root, node, 1));

        System.out.println("Total generalized features found: " + generalizedFeaturesWithDescription.size());
        return generalizedFeaturesWithDescription;
    }

    public List<FeatureWithDescription> runExhaustiveGeneralizationSearch(List<AbstractLogicOperator> operators, Connective root, Formula node){
        if(super.getExitFlag()){
            return new ArrayList<>();
        }

        Set<Integer> uniqueFeatureHashCode = new HashSet<>();
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

        boolean nodeIsRoot = false;
        if(node == null){
            node = root;
            nodeIsRoot = true;
        }

        for(AbstractLogicOperator operator: operators){
            // Find potential parent nodes
            List<Connective> parentNodesOfApplicableNodes = operator.getParentNodesOfApplicableNodes((Connective)node, operator.getLogic());

            // Current operator not applicable
            if(parentNodesOfApplicableNodes.isEmpty()){
                continue;
            }

            for(Connective parentNodOfApplicableNodes: parentNodesOfApplicableNodes){

                // Find the applicable nodes under the parent node found
                Map<AbstractFilter, Set<AbstractFilter>> applicableFiltersMap = new HashMap<>();
                Map<AbstractFilter, Literal> applicableLiteralsMap = new HashMap<>();
                operator.findApplicableNodesUnderGivenParentNode(parentNodOfApplicableNodes, applicableFiltersMap, applicableLiteralsMap);

                for(AbstractFilter constraintSetter: applicableFiltersMap.keySet()){
                    while(!((AbstractGeneralizationOperator)operator).isExhaustiveSearchFinished()){

                        if(super.getExitFlag()){
                            return new ArrayList<>();
                        }

                        // Copy features
                        Connective rootCopy = root.copy();
                        Connective nodeCopy;

                        if(nodeIsRoot){
                            // rootCopy can be modified directly
                            nodeCopy = rootCopy;

                        }else{
                            // Find parent nodes
                            nodeCopy = (Connective) expressionHandler.findMatchingNodes(rootCopy, node).get(0);
                        }

//                        if(expressionHandler.findMatchingNodes(nodeCopy, parentNodOfApplicableNodes).isEmpty()){
//                            // Matching node not found
//                            System.out.println("Error: Matching nodes not found!");
//                            System.out.println("node: " + nodeCopy.getName());
//                            System.out.println("parent: " + parentNodOfApplicableNodes.getName());
//                            throw new IllegalStateException();
//                        }

                        Connective parentNode = (Connective) expressionHandler.findMatchingNodes(nodeCopy, parentNodOfApplicableNodes).get(0);

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

                        // Modify the nodes using the given argument
                        operator.apply(rootCopy, parentNode, constraintSetter, matchingFilters, applicableLiteralsMap, opDescription);

                        // Print description if it is available
                        for(int i = 0; i < opDescription.size(); i++){
                            System.out.println(opDescription.get(i));
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
                        Feature thisFeature = new Feature(rootCopy.getName(), rootCopy.getMatches(), metrics[0], metrics[1], metrics[2], metrics[3]);

                        if(Utils.dominates(inputFeature, thisFeature, comparators)){
                            // The new feature is dominated by the input feature
                            continue;

                        }else{
                            if(Utils.dominates(thisFeature, inputFeature, comparators)){
                                // The new feature dominates the input feature
                                dominatingFeatures.add(thisFeature);
                                dominatingFeaturesDesc.add(opDescription);
                            }
                            nonDominatedFeatures.add(thisFeature);
                            nonDominatedFeaturesDesc.add(opDescription);
                        }

                        System.out.println(rootCopy.getName());
                        System.out.println("thisFeature: " + metrics[2] + ", " + metrics[3]);
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

    public List<FeatureWithDescription> apply(AbstractLogicOperator operators, Connective root, Formula node, int numTrials){
        List<AbstractLogicOperator> operatorsList = new ArrayList<>();
        operatorsList.add(operators);
        return this.apply(operatorsList, root, node, numTrials);
    }

    public List<FeatureWithDescription> apply(List<AbstractLogicOperator> operators, Connective root, Formula node, int numTrials){
        if(super.getExitFlag()){
            return new ArrayList<>();
        }

        // Number of trials for each operator
        int cnt = numTrials;

        Set<Integer> uniqueFeatureHashCode = new HashSet<>();
        List<Feature> nonDominatedFeatures = new ArrayList<>();
        List<Feature> dominatingFeatures = new ArrayList<>();
        List<List<String>> dominatingFeaturesDesc = new ArrayList<>();
        List<List<String>> nonDominatedFeaturesDesc = new ArrayList<>();

        Random random = new Random();
        FeatureMetricEpsilonComparator comparator1 = new FeatureMetricEpsilonComparator(FeatureMetric.PRECISION, 0.05);
        FeatureMetricEpsilonComparator comparator2 = new FeatureMetricEpsilonComparator(FeatureMetric.RECALL, 0.05);
        List<Comparator> comparators = new ArrayList<>(Arrays.asList(comparator1,comparator2));

        // Set input feature
        double[] metrics = Utils.computeMetricsSetNaNZero(root.getMatches(), this.labels, this.architectures.size());
        Feature inputFeature = new Feature(root.getName(), root.getMatches(), metrics[0], metrics[1], metrics[2], metrics[3]);

//        System.out.println("---- Input Feature ----");
//        System.out.println(root.getNames() + "| precision: " + metrics[2] + ", recall: " + metrics[3]);

        // Set flags
        boolean nodeIsLiteral = false;
        boolean nodeIsRoot = false;

        if(node instanceof Literal){
            nodeIsLiteral = true;

        }else if(node == null){
            nodeIsRoot = true;
        }

        for(AbstractLogicOperator operator: operators){

            for(int i = 0; i < cnt; i++){

                if(super.getExitFlag()){
                    return new ArrayList<>();
                }

                // Copy features
                Connective rootCopy = root.copy();
                Connective nodeCopy;
                Connective parent;

                if(nodeIsRoot){
                    // rootCopy can be modified directly
                    parent = null;

                    nodeCopy = rootCopy;

                }else{
                    // Node should be removed from its parent

                    // Find parent nodes
                    List<Formula> matchedNodes = expressionHandler.findMatchingNodes(rootCopy, node);
                    Collections.shuffle(matchedNodes);
                    Formula matchedNode = matchedNodes.get(0);

                    // Get parent node
                    parent = (Connective) matchedNode.getParent();

                    if(parent == null){
                        throw new IllegalStateException("Node should have a parent");
                    }

//                    // Remove the given node from its parent
//                    parent.removeNode(matchedNode);
//
//                    if(nodeIsLiteral){
//                        // Temporarily add a logical connective node as the parent node
//                        nodeCopy = new Connective(parent.getLogic());
//                        nodeCopy.addLiteral((Literal) matchedNode);
//
//                    }else{
                        nodeCopy = (Connective) matchedNode;
//                    }
                }

                // Find potential parent nodes
                List<Connective> parentNodesOfApplicableNodes = operator.getParentNodesOfApplicableNodes(nodeCopy, operator.getLogic());

                // Current operator not applicable
                if(parentNodesOfApplicableNodes.isEmpty()){
                    break;
                }

                // Select a parent node
                Connective parentNodeOfApplicableNode = parentNodesOfApplicableNodes.get(random.nextInt(parentNodesOfApplicableNodes.size()));

                // Find the applicable nodes under the parent node found
                Map<AbstractFilter, Set<AbstractFilter>> applicableFiltersMap = new HashMap<>();
                Map<AbstractFilter, Literal> applicableLiteralsMap = new HashMap<>();
                operator.findApplicableNodesUnderGivenParentNode(parentNodeOfApplicableNode, applicableFiltersMap, applicableLiteralsMap);

                // Randomly select one constraint setter node
                List<AbstractFilter> constraintSetters = new ArrayList<>(applicableFiltersMap.keySet());

                if(constraintSetters.isEmpty()){
                    String err;
                    if(parent == null){
                        err = operator.getClass().getSimpleName() + " cannot be applied to " + root.getName();
                    }else{
                        err = operator.getClass().getSimpleName() + " cannot be applied to " + parent.getName();
                    }

                    System.out.println("Parents of applicable nodes:");
                    for(Connective temp:parentNodesOfApplicableNodes){
                        System.out.println(temp.getName());
                    }
                    throw new RuntimeException(err);
                }

                AbstractFilter constraintSetter = constraintSetters.get(random.nextInt(constraintSetters.size()));
                Set<AbstractFilter> matchingNodes = applicableFiltersMap.get(constraintSetter);

                List<String> opDescription = new ArrayList<>();

                // Modify the nodes using the given argument
                operator.apply(rootCopy, parentNodeOfApplicableNode, constraintSetter, matchingNodes, applicableLiteralsMap, opDescription);

//                // Re-combine the modified parts of the tree with the rest of the tree
//                if(nodeIsRoot){
//                    rootCopy = nodeCopy;
//
//                }else{
//                    // Directly add all children nodes
//                    if(parent.getLogic() == nodeCopy.getLogic()){
//                        for(Formula child: nodeCopy.getChildNodes()){
//                            parent.addNode(child);
//                        }
//
//                    }else{
//                        // Add the subtree to the parent node
//                        parent.addNode(nodeCopy);
//                    }
//                }

                simplifier.simplify(rootCopy);

                // Retain only the unique set of features
                if(uniqueFeatureHashCode.contains(rootCopy.hashCode())){
                    continue;

                }else{
                    uniqueFeatureHashCode.add(rootCopy.hashCode());
                }

                // Compute metrics
                metrics = Utils.computeMetricsSetNaNZero(rootCopy.getMatches(), this.labels, this.architectures.size());
                Feature thisFeature = new Feature(rootCopy.getName(), rootCopy.getMatches(), metrics[0], metrics[1], metrics[2], metrics[3]);


//                System.out.println("output: " + rootCopy.getNames());
//                System.out.println(rootCopy.getNames() + "| precision: " +metrics[2] + ", recall: " + metrics[3]);



                if(Utils.dominates(inputFeature, thisFeature, comparators)){
                    // The new feature is dominated by the input feature
                    continue;

                }else{
                    if(Utils.dominates(thisFeature, inputFeature, comparators)){
                        // The new feature dominates the input feature
                        dominatingFeatures.add(thisFeature);
                        dominatingFeaturesDesc.add(opDescription);
                    }
                    nonDominatedFeatures.add(thisFeature);
                    nonDominatedFeaturesDesc.add(opDescription);
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

            System.out.println(sj.toString());

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
