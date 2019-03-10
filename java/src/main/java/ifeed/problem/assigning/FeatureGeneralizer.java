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
import ifeed.mining.moea.operators.AbstractLogicOperator;
import ifeed.ontology.OntologyManager;
import ifeed.problem.assigning.logicOperators.generalization.combined.InOrbits2Present;
import ifeed.problem.assigning.logicOperators.generalization.combined.NotInOrbits2Absent;
import ifeed.problem.assigning.logicOperators.generalization.combined.localSearch.InOrbits2PresentWithLocalSearch;
import ifeed.problem.assigning.logicOperators.generalization.combined.localSearch.NotInOrbits2AbsentWithLocalSearch;
import ifeed.problem.assigning.logicOperators.generalization.combined.localSearch.OrbitsGeneralizationWithLocalSearch;
import ifeed.problem.assigning.logicOperators.generalization.single.InstrumentNotInOrbitGeneralizer;
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

        this.featureFetcher = new FeatureFetcher(params, architectures);
        this.filterFetcher = featureFetcher.getFilterFetcher();
        this.expressionHandler = new FeatureExpressionHandler(featureFetcher);

        this.base = new GPMOEA(params, architectures, behavioral, non_behavioral);
        this.localSearch = new LocalSearch(params, architectures, behavioral, non_behavioral);
        this.simplifier = new FeatureSimplifier(params, (FeatureFetcher) base.getFeatureFetcher());
    }

    @Override
    public Set<Feature> generalize(String rootFeatureExpression, String nodeFeatureExpression){



        // Create a tree structure based on the given feature expression
        Connective root = expressionHandler.generateFeatureTree(rootFeatureExpression);

        Formula node;
        if(rootFeatureExpression.equalsIgnoreCase(nodeFeatureExpression) || nodeFeatureExpression == null || nodeFeatureExpression.trim().length() == 0){
            // The whole feature tree is used for single
            node = null;

        }else{
            // Only a part of feature tree is used for single
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

        // Create empty lists
        List<Feature> generalizedFeatures = new ArrayList<>();
        List<String> explanation = new ArrayList<>();

        List<AbstractLogicOperator> combinedGeneralization = new ArrayList<>();
        combinedGeneralization.add(new InOrbits2PresentWithLocalSearch(params, base, localSearch));
        combinedGeneralization.add(new NotInOrbits2AbsentWithLocalSearch(params, base, localSearch));
//        combinedGeneralization.add(new OrbitsGeneralizationWithLocalSearch(params, base, localSearch));
        this.apply(combinedGeneralization, root, node, 5, generalizedFeatures, explanation);

        List<AbstractLogicOperator> generalizationPlusCondition = new ArrayList<>();
        generalizationPlusCondition.add(new InOrbit2PresentWithLocalSearch(params, base, localSearch));
        generalizationPlusCondition.add(new InOrbit2TogetherWithLocalSearch(params, base, localSearch));
        generalizationPlusCondition.add(new NotInOrbit2AbsentWithLocalSearch(params, base, localSearch));
        generalizationPlusCondition.add(new NotInOrbit2EmptyOrbitWithLocalSearch(params, base, localSearch));
        generalizationPlusCondition.add(new Separate2AbsentWithLocalSearch(params, base, localSearch));
        this.apply(generalizationPlusCondition, root, node, 15, generalizedFeatures, explanation);

        List<AbstractLogicOperator> variableGeneralization = new ArrayList<>();
        variableGeneralization.add(new InstrumentGeneralizationWithLocalSearch(params, base, localSearch));
        variableGeneralization.add(new OrbitGeneralizationWithLocalSearch(params, base, localSearch));
        variableGeneralization.add(new InstrumentNotInOrbitGeneralizer(params, base));
        this.apply(variableGeneralization, root, node, 10, generalizedFeatures, explanation);

        System.out.println("Total generalized features found: " + generalizedFeatures.size());

        return new HashSet<>(generalizedFeatures);
    }


    public void apply(List<AbstractLogicOperator> operators,
                      Connective root, Formula node, int numTrials,
                      List<Feature> output, List<String> explanation){

        // Number of trials for each operator
        int cnt = numTrials;

        Set<Integer> uniqueFeatureHashCode = new HashSet<>();
        List<Feature> nonDominatedFeatures = new ArrayList<>();
        List<Feature> dominatingFeatures = new ArrayList<>();

        Random random = new Random();
        FeatureMetricEpsilonComparator comparator1 = new FeatureMetricEpsilonComparator(FeatureMetric.PRECISION, 0.05);
        FeatureMetricEpsilonComparator comparator2 = new FeatureMetricEpsilonComparator(FeatureMetric.RECALL, 0.05);
        List<Comparator> comparators = new ArrayList<>(Arrays.asList(comparator1,comparator2));

        // Set input feature
        double[] metrics = Utils.computeMetricsSetNaNZero(root.getMatches(), this.labels, this.architectures.size());
        Feature inputFeature = new Feature(root.getName(), root.getMatches(), metrics[0], metrics[1], metrics[2], metrics[3]);

//        System.out.println("---- Input Feature ----");
//        System.out.println(root.getName() + "| precision: " + metrics[2] + ", recall: " + metrics[3]);

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

                    // Remove the given node from its parent
                    parent.removeNode(matchedNode);

                    if(nodeIsLiteral){
                        // Temporarily add a logical connective node as the parent node
                        nodeCopy = new Connective(parent.getLogic());
                        nodeCopy.addLiteral((Literal) matchedNode);

                    }else{
                        nodeCopy = (Connective) matchedNode;
                    }
                }

                // Find potential parent nodes
                List<Connective> parentNodesOfApplicableNodes = operator.getParentNodesOfApplicableNodes(nodeCopy, null);

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

                // Modify the nodes using the given argument
                operator.apply(nodeCopy, parentNodeOfApplicableNode, constraintSetter, matchingNodes, applicableLiteralsMap);

                // Re-combine the modified parts of the tree with the rest of the tree
                if(nodeIsRoot){
                    rootCopy = nodeCopy;

                }else{
                    // Directly add all children nodes
                    if(parent.getLogic() == nodeCopy.getLogic()){
                        for(Formula child: nodeCopy.getChildNodes()){
                            parent.addNode(child);
                        }

                    }else{
                        // Add the subtree to the parent node
                        parent.addNode(nodeCopy);
                    }
                }

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
                    }
                    nonDominatedFeatures.add(thisFeature);
                }
            }
        }

        // If there are features that dominate the input feature, return those.
        List<Feature> extractedFeatures;
        if(!dominatingFeatures.isEmpty()){
            extractedFeatures = dominatingFeatures;

        }else{
            // Otherwise, return all non-dominated features
            extractedFeatures = nonDominatedFeatures;
        }

        if(extractedFeatures.size() > 6){
            extractedFeatures = Utils.getFeatureFuzzyParetoFront(extractedFeatures, comparators,2);
        }

        // Add to the output list
        for(Feature f: extractedFeatures){
            output.add(f);
        }
    }
}