//package ifeed.problem.assigning;
//
//import ifeed.Utils;
//import ifeed.architecture.AbstractArchitecture;
//import ifeed.feature.*;
//import ifeed.feature.logic.Connective;
//import ifeed.feature.logic.Literal;
//import ifeed.filter.AbstractFilter;
//import ifeed.local.params.BaseParams;
//import ifeed.mining.moea.operators.AbstractGeneralizationOperator;
//import ifeed.ontology.OntologyManager;
//
//import java.util.*;
//
//public class FeatureGeneralizer extends AbstractFeatureGeneralizer{
//
//
//    public FeatureGeneralizer(BaseParams params,
//                              List<AbstractArchitecture> architectures,
//                              List<Integer> behavioral,
//                              List<Integer> non_behavioral,
//                              OntologyManager ontologyManager){
//
//        super(params, architectures, behavioral, non_behavioral, ontologyManager);
//    }
//
//    public Set<Connective> generalize(String featureExpression){
//
//        Set<Connective> out = new HashSet<>();
//
//
//
//
//
//
//        MOEA assigningMOEA = new MOEA(params, architectures, behavioral, non_behavioral);
//        assigningMOEA.setOntologyManager(ontologyManager);
//
//        AbstractFeatureFetcher featureFetcher = new FeatureFetcher(params, architectures);
//        FeatureExpressionHandler filterExpressionHandler = new FeatureExpressionHandler(featureFetcher);
//
//        // Create a tree structure based on the given feature expression
//        Connective root = filterExpressionHandler.generateFeatureTree(featureExpression);
//
//        ifeed.problem.assigning.logicOperators.generalization.InstrumentGeneralizer instrumentGeneralizer =
//                new ifeed.problem.assigning.logicOperators.generalization.InstrumentGeneralizer(params, assigningMOEA);
//
//        ifeed.problem.assigning.logicOperators.generalization.OrbitGeneralizer orbitGeneralizer =
//                new ifeed.problem.assigning.logicOperators.generalization.OrbitGeneralizer(params, assigningMOEA);
//
//        List<Connective> instrumentGeneralizationParentNodes = instrumentGeneralizer.getParentNodesOfApplicableNodes(root, null);
//        List<Connective> orbitGeneralizationParentNodes = orbitGeneralizer.getParentNodesOfApplicableNodes(root, null);
//
//        Random random = new Random();
//
//        int cnt = 0;
//        while(cnt < 80){
//
//            Connective testRoot = root.copy();
//            Connective parent;
//            AbstractGeneralizationOperator generalizer;
//            int randInt = random.nextInt(orbitGeneralizationParentNodes.size() + instrumentGeneralizationParentNodes.size());
//
//            if(randInt < orbitGeneralizationParentNodes.size()){
//                parent = orbitGeneralizationParentNodes.get(randInt);
//                generalizer = orbitGeneralizer;
//
//            }else{
//                parent = instrumentGeneralizationParentNodes.get(randInt - orbitGeneralizationParentNodes.size());
//                generalizer = instrumentGeneralizer;
//            }
//
//            // Find the applicable nodes under the parent node found
//            Map<AbstractFilter, Set<AbstractFilter>> applicableFiltersMap = new HashMap<>();
//            Map<AbstractFilter, Literal> applicableLiteralsMap = new HashMap<>();
//            generalizer.findApplicableNodesUnderGivenParentNode(parent, applicableFiltersMap, applicableLiteralsMap);
//
//            // Randomly select one constraint setter node
//            List<AbstractFilter> constraintSetters = new ArrayList<>(applicableFiltersMap.keySet());
//
//            if(constraintSetters.isEmpty()){
//                cnt++;
//                continue;
//            }
//
//            AbstractFilter constraintSetter = constraintSetters.get(random.nextInt(constraintSetters.size()));
//            Set<AbstractFilter> matchingNodes = applicableFiltersMap.get(constraintSetter);
//
//            // Modify the nodes using the given argument
//            generalizer.apply(testRoot, parent, constraintSetter, matchingNodes, applicableLiteralsMap);
//
//            double[] metrics = Utils.computeMetricsSetNaNZero(testRoot.getMatches(), assigningMOEA.getLabels(), architectures.size());
//            ifeed.feature.Feature feature = new ifeed.feature.Feature(testRoot.getName(), testRoot.getMatches(), metrics[0], metrics[1], metrics[2], metrics[3]);
//            extracted_features.add(feature);
//
//            cnt++;
//        }
//
//        FeatureMetricComparator comparator1 = new FeatureMetricComparator(FeatureMetric.FCONFIDENCE);
//        FeatureMetricComparator comparator2 = new FeatureMetricComparator(FeatureMetric.RCONFIDENCE);
//        List<Comparator> comparators = new ArrayList<>(Arrays.asList(comparator1,comparator2));
//
//        System.out.println(extracted_features.size());
//        extracted_features = Utils.getFeatureFuzzyParetoFront(extracted_features,comparators,2);
//        out = formatFeatureOutput(extracted_features);
//    }
//
//
//
//    public Set<Connective> generalizationSingle(){
//        return new HashSet<>();
//    }
//
//    public Set<Connective> generalizationWithCondition(){
//        return new HashSet<>();
//    }
//
//    public Set<Connective> generalizationPlusCondition(){
//        return new HashSet<>();
//    }
//
//}
