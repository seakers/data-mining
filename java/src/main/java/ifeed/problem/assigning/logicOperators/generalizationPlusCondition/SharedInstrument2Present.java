//package ifeed.problem.assigning.logicOperators.generalizationPlusCondition;
//
//import com.google.common.collect.Multiset;
//import ifeed.Utils;
//import ifeed.feature.Feature;
//import ifeed.feature.FeatureMetric;
//import ifeed.feature.FeatureMetricComparator;
//import ifeed.feature.logic.Connective;
//import ifeed.feature.logic.ConnectiveTester;
//import ifeed.feature.logic.Literal;
//import ifeed.feature.logic.LogicalConnectiveType;
//import ifeed.filter.AbstractFilter;
//import ifeed.filter.AbstractFilterFinder;
//import ifeed.local.params.BaseParams;
//import ifeed.mining.AbstractLocalSearch;
//import ifeed.mining.moea.MOEABase;
//import ifeed.mining.moea.operators.AbstractGeneralizationOperator;
//import ifeed.problem.assigning.Params;
//import ifeed.problem.assigning.filters.InOrbit;
//import ifeed.problem.assigning.filters.NotInOrbit;
//import ifeed.problem.assigning.filters.Present;
//
//import java.util.*;
//
//public class SharedInstrument2Present extends AbstractGeneralizationOperator{
//
//    public SharedInstrument2Present(BaseParams params, MOEABase base) {
//        super(params, base, LogicalConnectiveType.OR);
//    }
//
//    public void apply(Connective root,
//                         Connective parent,
//                         AbstractFilter constraintSetterAbstract,
//                         Set<AbstractFilter> matchingFilters,
//                         Map<AbstractFilter, Literal> nodes
//    ){
//
////        System.out.println("==================================");
////        System.out.println(this.getClass().getSimpleName());
////        System.out.println("logic: " + super.logic);
////        System.out.println("before: " + root.getName());
//
//        Params params = (Params) super.params;
//
//        Connective grandParent = (Connective) parent.getParent();
//
//        if(grandParent == null){ // Parent node is the root node since it doesn't have a parent node
//            super.base.getFeatureHandler().createNewRootNode(root);
//            grandParent = root;
//
//            // Store the newly generated node to parent
//            parent = grandParent.getConnectiveChildren().get(0);
//        }
//
//        InOrbit constraintSetter = (InOrbit) constraintSetterAbstract;
//
//        // Select one matching filter
//        List<AbstractFilter> matchingFiltersList = new ArrayList<>(matchingFilters);
//        Collections.shuffle(matchingFiltersList);
//        AbstractFilter selectedFilter = matchingFiltersList.get(0);
//
//        // Find instruments that are shared in two nodes
//        Set<Integer> sharedInstruments = new HashSet<>(constraintSetter.getInstruments());
//        sharedInstruments.retainAll(((InOrbit) selectedFilter).getInstruments());
//
//        // If there are multiple instruments that are shared select one
//        int selectedArgument;
//        if(sharedInstruments.size() > 1){
//            ArrayList<Integer> sharedInstrumentsList = new ArrayList<>(sharedInstruments);
//            Collections.shuffle(sharedInstrumentsList);
//            selectedArgument = sharedInstrumentsList.get(0);
//        }else{
//            selectedArgument = sharedInstruments.iterator().next();
//        }
//
//        // Remove nodes that share an instrument
//        Literal constraintSetterLiteral = nodes.get(constraintSetter);
//        Literal matchingLiteral = nodes.get(selectedFilter);
//        InOrbit matchingFilter = (InOrbit) selectedFilter;
//
//        int constraintSetterLiteralIndex = parent.getNodeIndex(constraintSetterLiteral);
//        if(constraintSetterLiteralIndex == -1){
//            int i = 0;
//            for(Literal literal: parent.getLiteralChildren()){
//                if(base.getFeatureHandler().literalEquals(literal, constraintSetterLiteral)){
//                    constraintSetterLiteralIndex = i;
//                    break;
//                }
//                i++;
//            }
//        }
//        parent.removeLiteral(constraintSetterLiteral);
//
//        if(constraintSetter.getInstruments().size() > 1){
//            int orbit = constraintSetter.getOrbit();
//            ArrayList<Integer> instruments = new ArrayList<>(constraintSetter.getInstruments());
//            int selectedArgumentIndex = instruments.indexOf(selectedArgument);
//            instruments.remove(selectedArgumentIndex);
//
//            AbstractFilter newFilter = new InOrbit(params, orbit, Utils.intCollection2Array(instruments));
//            Feature newFeature = base.getFeatureFetcher().fetch(newFilter);
//            parent.addLiteral(constraintSetterLiteralIndex, newFeature.getName(), newFeature.getMatches());
//        }
//
//        int matchingLiteralIndex = parent.getNodeIndex(matchingLiteral);
//        if(matchingLiteralIndex == -1){
//            int i = 0;
//            for(Literal literal: parent.getLiteralChildren()){
//                if(base.getFeatureHandler().literalEquals(literal, matchingLiteral)){
//                    matchingLiteralIndex = i;
//                    break;
//                }
//                i++;
//            }
//        }
//        parent.removeLiteral(matchingLiteral);
//
//        if(matchingFilter.getInstruments().size() > 1){
//            int orbit = matchingFilter.getOrbit();
//            ArrayList<Integer> instruments = new ArrayList<>(matchingFilter.getInstruments());
//            int selectedArgumentIndex = instruments.indexOf(selectedArgument);
//            instruments.remove(selectedArgumentIndex);
//
//            AbstractFilter newFilter = new InOrbit(params, orbit, Utils.intCollection2Array(instruments));
//            Feature newFeature = base.getFeatureFetcher().fetch(newFilter);
//            parent.addLiteral(matchingLiteralIndex, newFeature.getName(), newFeature.getMatches());
//        }
//
//        // Create absent feature with the selected argument
//        AbstractFilter presentFilter = new Present(params, selectedArgument);
//        Feature presentFeature = base.getFeatureFetcher().fetch(presentFilter);
//        Literal presentLiteral = new Literal(presentFeature.getName(), presentFeature.getMatches());
//
//        if(base.getLocalSearch() == null){
//            throw new IllegalStateException("Local search needs to be defined to use this operator");
//        }
//
//        AbstractLocalSearch localSearch = base.getLocalSearch();
//        ConnectiveTester tester = new ConnectiveTester(root);
//        localSearch.setRoot(tester);
//
//        ConnectiveTester testerParentNode = null;
//        for(Connective node: tester.getDescendantConnectives(true)){
//            if(base.getFeatureHandler().featureTreeEquals(grandParent, node)){
//                testerParentNode = (ConnectiveTester) node;
//            }
//        }
//
//        testerParentNode.addLiteral(presentLiteral);
//        testerParentNode.setAddNewNode();
//        FeatureMetricComparator comparator = new FeatureMetricComparator(FeatureMetric.FCONFIDENCE);
//
//        List<Feature> testFeatures = new ArrayList<>();
//        for(int testOrbit: params.getOrbitIndex2Name().keySet()){
//            NotInOrbit notInOrbit = new NotInOrbit(params, testOrbit, selectedArgument);
//            testFeatures.add(base.getFeatureFetcher().fetch(notInOrbit));
//        }
//        Feature localSearchOutput = localSearch.run_getSingleBest(testFeatures, comparator);
//
//        grandParent.addLiteral(presentLiteral);
//        grandParent.addLiteral(localSearchOutput.getName(), localSearchOutput.getMatches());
//
////        System.out.println("after: " + root.getName());
////        System.out.println("==================================");
//    }
//
//    @Override
//    public void findApplicableNodesUnderGivenParentNode(Connective parent,
//                                                        Map<AbstractFilter, Set<AbstractFilter>> applicableFiltersMap,
//                                                        Map<AbstractFilter, Literal> applicableLiteralsMap
//    ){
//        // Find all InOrbit literals sharing at least one common instrument argument inside the current node (parent).
//        // All Literals and their corresponding Filters are not returned, but the lists are filled up as side effects
//        FilterFinder finder = new FilterFinder();
//        super.findApplicableNodesUnderGivenParentNode(parent, applicableFiltersMap, applicableLiteralsMap, finder);
//    }
//
//    public class FilterFinder extends AbstractFilterFinder {
//
//        Multiset<Integer> instrumentsToBeIncluded;
//
//        public FilterFinder(){
//            super(InOrbit.class, InOrbit.class);
//            this.instrumentsToBeIncluded = null;
//        }
//
//        @Override
//        public void setConstraints(AbstractFilter constraintSetter){
//            this.instrumentsToBeIncluded = ((InOrbit) constraintSetter).getInstruments();
//        }
//
//        @Override
//        public void clearConstraints(){
//            this.instrumentsToBeIncluded = null;
//        }
//
//        /**
//         * One of the instruments in the tested filter should be in the constraint instrument set
//         * @param filterToTest
//         * @return
//         */
//        @Override
//        public boolean check(AbstractFilter filterToTest){
//
//            // Check if two literals share at least one common instrument
//            Multiset<Integer> instruments1 = this.instrumentsToBeIncluded;
//            Multiset<Integer> instruments2 = ((InOrbit) filterToTest).getInstruments();
//
//            for(int inst:instruments2){
//                if(instruments1.contains(inst)) {
//                    return true;
//                }
//            }
//            return false;
//        }
//    }
//}
