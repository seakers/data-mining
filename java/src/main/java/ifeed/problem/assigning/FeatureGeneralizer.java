//package ifeed.problem.assigning;
//
//import ifeed.Utils;
//import ifeed.architecture.AbstractArchitecture;
//import ifeed.feature.*;
//import ifeed.feature.logic.Connective;
//import ifeed.feature.logic.Formula;
//import ifeed.feature.logic.Literal;
//import ifeed.feature.logic.LogicalConnectiveType;
//import ifeed.filter.AbstractFilter;
//import ifeed.filter.AbstractFilterFetcher;
//import ifeed.filter.AbstractFilterFinder;
//import ifeed.local.params.BaseParams;
//import ifeed.mining.moea.operators.AbstractGeneralizationOperator;
//import ifeed.ontology.OntologyManager;
//import java.util.*;
//
//public class FeatureGeneralizer extends AbstractFeatureGeneralizer{
//
//    private AbstractFeatureFetcher featureFetcher;
//    private AbstractFilterFetcher filterFetcher;
//    private FeatureExpressionHandler expressionHandler;
//    private BitSet labels;
//
//    public FeatureGeneralizer(BaseParams params,
//                              List<AbstractArchitecture> architectures,
//                              List<Integer> behavioral,
//                              List<Integer> non_behavioral,
//                              OntologyManager ontologyManager){
//
//        super(params, architectures, behavioral, non_behavioral, ontologyManager);
//
//        // Set label
//        this.labels = new BitSet(architectures.size());
//        for (int i = 0; i < architectures.size(); i++) {
//            AbstractArchitecture a = architectures.get(i);
//            if (behavioral.contains(a.getID())) {
//                this.labels.set(i);
//            }
//        }
//    }
//
//    @Override
//    public Set<Feature> generalize(String rootFeatureExpression, String nodeFeatureExpression){
//
//        Set<Feature> out = new HashSet<>();
//
//        this.featureFetcher = new FeatureFetcher(params, architectures);
//        this.filterFetcher = featureFetcher.getFilterFetcher();
//        this.expressionHandler = new FeatureExpressionHandler(featureFetcher);
//
//        // Create a tree structure based on the given feature expression
//        Connective root = expressionHandler.generateFeatureTree(rootFeatureExpression);
//
//        Formula node;
//        if(rootFeatureExpression == nodeFeatureExpression || nodeFeatureExpression == null || nodeFeatureExpression == ""){
//            // The whole feature tree is used for generalization
//            node = null;
//
//        }else{
//            // Only a part of feature tree is used for generalization
//            Connective tempRoot = expressionHandler.generateFeatureTree(nodeFeatureExpression);
//
//            Formula nodeToBeSearched;
//            if(tempRoot.getConnectiveChildren().isEmpty() && tempRoot.getLiteralChildren().size() == 1){
//                // node is a literal
//                nodeToBeSearched = tempRoot.getLiteralChildren().get(0);
//
//            }else{
//                // node is a logical connective node
//                nodeToBeSearched = tempRoot;
//            }
//
//            // Get node
//            List<Formula> nodes = expressionHandler.findMatchingNodes(root, nodeToBeSearched);
//            if(nodes.isEmpty()){
//                throw new IllegalStateException("Node " + nodeFeatureExpression + " not found from " + root.getName());
//
//            }else if(nodes.size() > 1){
//                //Collections.shuffle(nodes);
//                throw new IllegalStateException("Node " + nodeFeatureExpression + " found more than once from " + root.getName());
//
//            }
//            node = nodes.get(0);
//        }
//
//        // Create empty lists
//        List<Feature> generalizedFeatures = new ArrayList<>();
//        List<String> explanation = new ArrayList<>();
//
//        if(!(node instanceof Literal)){
//
//            generalizationWithCondition(root, node, generalizedFeatures, explanation);
//
//            generalizationPlusCondition(root, node, generalizedFeatures, explanation);
//
//        }
//
//        generalizationSingle(root, node, generalizedFeatures, explanation);
//
//        return out;
//    }
//
//    public void generalizationSingle(Connective root, Formula node, List<Feature> output, List<String> explanation){
//
//        int cnt = 80;
//
//        List<Feature> nonDominatedFeatures = new ArrayList<>();
//        List<Feature> dominatingFeatures = new ArrayList<>();
//
//        Random random = new Random();
//        FeatureMetricComparator comparator1 = new FeatureMetricComparator(FeatureMetric.FCONFIDENCE);
//        FeatureMetricComparator comparator2 = new FeatureMetricComparator(FeatureMetric.RCONFIDENCE);
//        List<Comparator> comparators = new ArrayList<>(Arrays.asList(comparator1,comparator2));
//
//        // Set input feature
//        Feature inputFeature = this.featureFetcher.fetch(root.getName());
//
//        // Initialize generalization operators
//        List<AbstractGeneralizationOperator> generalizationOperators = new ArrayList<>();
//        generalizationOperators.add(new ifeed.problem.assigning.logicOperators.generalization.InstrumentGeneralizer(params, featureFetcher));
//        generalizationOperators.add(new ifeed.problem.assigning.logicOperators.generalization.OrbitGeneralizer(params, featureFetcher));
//
//        // Set flags
//        boolean nodeIsLiteral = false;
//        boolean nodeIsRoot = false;
//
//        if(node instanceof Literal){
//            nodeIsLiteral = true;
//
//        }else if(node == null){
//            nodeIsRoot = true;
//        }
//
//        for(AbstractGeneralizationOperator operator: generalizationOperators){
//
//            for(int i = 0; i < cnt; i++){
//
//                // Copy features
//                Connective rootCopy = root.copy();
//
//                List<Formula> nodes = expressionHandler.findMatchingNodes(rootCopy, node);
//                Collections.shuffle(nodes);
//
//                Connective nodeCopy;
//                if(nodeIsLiteral){
//                    Connective tempRoot;
//                    if(((Connective)node.getParent()).getLogic() == LogicalConnectiveType.AND){
//                        tempRoot = new Connective(LogicalConnectiveType.AND);
//
//                    }else{
//                        tempRoot = new Connective(LogicalConnectiveType.OR);
//                    }
//
//                    tempRoot.addLiteral((Literal)nodes.get(0));
//                    nodeCopy = tempRoot;
//
//                } else if(nodeIsRoot){
//                    nodeCopy = rootCopy;
//
//                }else{
//                    nodeCopy = (Connective) nodes.get(0);
//                }
//
//                List<Connective> parentNodes = operator.getParentNodesOfApplicableNodes(nodeCopy, null);
//
//                // Current operator not applicable
//                if(parentNodes.isEmpty()){
//                    break;
//                }
//
//                // Select a parent node
//                Connective parent = parentNodes.get(random.nextInt(parentNodes.size()));
//
//                // Find the applicable nodes under the parent node found
//                Map<AbstractFilter, Set<AbstractFilter>> applicableFiltersMap = new HashMap<>();
//                Map<AbstractFilter, Literal> applicableLiteralsMap = new HashMap<>();
//                operator.findApplicableNodesUnderGivenParentNode(parent, applicableFiltersMap, applicableLiteralsMap);
//
//                // Randomly select one constraint setter node
//                List<AbstractFilter> constraintSetters = new ArrayList<>(applicableFiltersMap.keySet());
//
//                if(constraintSetters.isEmpty()){
//                    String err = operator.getClass().toString() + " cannot be applied to " + parent.getName();
//                    throw new RuntimeException(err);
//                }
//
//                AbstractFilter constraintSetter = constraintSetters.get(random.nextInt(constraintSetters.size()));
//                Set<AbstractFilter> matchingNodes = applicableFiltersMap.get(constraintSetter);
//
//                // Modify the nodes using the given argument
//                operator.apply(nodeCopy, parent, constraintSetter, matchingNodes, applicableLiteralsMap);
//
//                if(){
//
//                }
//
//
//
//
//
//
//
//                double[] metrics = Utils.computeMetricsSetNaNZero(nodeCopy.getMatches(), this.labels, this.architectures.size());
//                Feature thisFeature = new Feature(nodeCopy.getName(), nodeCopy.getMatches(), metrics[0], metrics[1], metrics[2], metrics[3]);
//
//                if(Utils.dominates(inputFeature, thisFeature, comparators)){
//                    // The new feature is dominated by the input feature
//                    continue;
//
//                }else{
//                    if(Utils.dominates(thisFeature, inputFeature, comparators)){
//                        // The new feature dominated the input feature
//                        dominatingFeatures.add(thisFeature);
//                    }
//                    nonDominatedFeatures.add(thisFeature);
//                }
//            }
//
//
//
//
//
//
//
//
//
//
//        }
//
//        // If there are features that dominate the input feature, return those.
//        List<Feature> extractedFeatures;
//        if(!dominatingFeatures.isEmpty()){
//            extractedFeatures = dominatingFeatures;
//
//        }else{
//            // Otherwise, add all non-dominated features
//            extractedFeatures = nonDominatedFeatures;
//        }
//
//        if(extractedFeatures.size() > 6){
//            extractedFeatures = Utils.getFeatureFuzzyParetoFront(extractedFeatures, comparators,0);
//        }
//
//        // Add to the output list
//        for(Feature f: extractedFeatures){
//            output.add(f);
//        }
//    }
//
//    public void generalizationWithCondition(Connective root, Formula node, List<Feature> output, List<String> explanation){}
//
//    public void generalizationPlusCondition(Connective root, Formula node, List<Feature> output, List<String> explanation){}
//
//}
