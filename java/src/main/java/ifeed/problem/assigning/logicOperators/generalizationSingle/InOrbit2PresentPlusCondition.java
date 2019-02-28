//package ifeed.problem.assigning.logicOperators.generalizationSingle;
//
//import ifeed.Utils;
//import ifeed.feature.*;
//import ifeed.feature.logic.Connective;
//import ifeed.feature.logic.ConnectiveTester;
//import ifeed.feature.logic.Literal;
//import ifeed.feature.logic.LogicalConnectiveType;
//import ifeed.filter.AbstractFilter;
//import ifeed.filter.AbstractFilterFinder;
//import ifeed.local.params.BaseParams;
//import ifeed.mining.AbstractLocalSearch;
//import ifeed.mining.moea.operators.AbstractLogicOperatorWithLocalSearch;
//import ifeed.problem.assigning.Params;
//import ifeed.problem.assigning.filters.InOrbit;
//import ifeed.problem.assigning.filters.NotInOrbit;
//import ifeed.problem.assigning.filters.Present;
//
//import java.util.*;
//
//public class InOrbit2PresentPlusCondition extends AbstractLogicOperatorWithLocalSearch{
//
//    private AbstractFeatureFetcher featureFetcher;
//    private FeatureExpressionHandler featureHandler;
//
//    public InOrbit2PresentPlusCondition(BaseParams params, AbstractLocalSearch localSearch){
//        super(params, localSearch);
//        this.featureFetcher = localSearch.getFeatureFetcher();
//        this.featureHandler = localSearch.getFeatureHandler();
//    }
//
//    public void apply(Connective root,
//                         Connective parent,
//                         AbstractFilter constraintSetterAbstract,
//                         Set<AbstractFilter> matchingFilters,
//                         Map<AbstractFilter, Literal> nodes
//    ){
//
//        Params params = (Params) super.params;
//        InOrbit constraintSetter = (InOrbit) constraintSetterAbstract;
//
//        // Select an instrument randomly
//        List<Integer> instrumentList = new ArrayList<>();
//        for(int instrument: constraintSetter.getInstruments()){
//            instrumentList.add(instrument);
//        }
//        Collections.shuffle(instrumentList);
//        int selectedInstrument = instrumentList.get(0);
//
//        // Remove the given node
//        Literal constraintSetterLiteral = nodes.get(constraintSetter);
//        parent.removeLiteral(constraintSetterLiteral);
//
//        // Add new feature
//        AbstractFilter newFilter = new Present(params, selectedInstrument);
//        Feature newFeature = this.featureFetcher.fetch(newFilter);
//
//        if(parent.getLogic() == LogicalConnectiveType.AND){
//            parent.addLiteral(newFeature.getName(), newFeature.getMatches());
//
//        }else{
//            Connective newBranch = new Connective(LogicalConnectiveType.AND);
//            newBranch.addLiteral(newFeature.getName(), newFeature.getMatches());
//            parent.addBranch(newBranch);
//        }
//
//        if(constraintSetter.getInstruments().size() > 1){
//            int orbit = constraintSetter.getOrbit();
//            ArrayList<Integer> instruments = new ArrayList<>(constraintSetter.getInstruments());
//            int selectedArgumentIndex = instruments.indexOf(selectedInstrument);
//            instruments.remove(selectedArgumentIndex);
//
//            AbstractFilter modifiedFilter = new InOrbit(params, orbit, Utils.intCollection2Array(instruments));
//            Feature modifiedFeature = this.featureFetcher.fetch(modifiedFilter);
//
//            if(!instruments.isEmpty()){
//                if(parent.getLogic() == LogicalConnectiveType.AND){
//                    parent.addLiteral(modifiedFeature.getName(), modifiedFeature.getMatches());
//
//                }else{
//                    Connective newBranch = parent.getConnectiveChildren().get(0);
//                    newBranch.addLiteral(modifiedFeature.getName(), modifiedFeature.getMatches());
//                }
//            }
//        }
//
//        if(super.localSearch == null){
//            throw new IllegalStateException("Local search needs to be defined to use this operator");
//        }
//
//
//
//
//
//
//
//
//
//        AbstractLocalSearch localSearch = super.localSearch;
//        ConnectiveTester tester = new ConnectiveTester(root);
//        localSearch.setRoot(tester);
//
//        ConnectiveTester testerParentNode = null;
//        for(Connective node: tester.getDescendantConnectives(true)){
//            if(this.featureHandler.featureTreeEquals(grandParent, node)){
//                testerParentNode = (ConnectiveTester) node;
//            }
//        }
//
//        testerParentNode.addLiteral(presentLiteral);
//        testerParentNode.setAddNewNode();
//        FeatureMetricComparator comparator = new FeatureMetricComparator(FeatureMetric.FCONFIDENCE);
//
//        List<Feature> testFeatures = new ArrayList<>();
//        for(int o = 0; o < params.getRightSetCardinality() + params.getRightSetGeneralizedConcepts().size() - 1; o++){
//            NotInOrbit notInOrbit = new NotInOrbit(params, o, selectedArgument);
//            testFeatures.add(this.featureFetcher.fetch(notInOrbit));
//        }
//        Feature localSearchOutput = localSearch.runArgmax(testFeatures, comparator);
//
//        grandParent.addLiteral(presentLiteral);
//        grandParent.addLiteral(localSearchOutput.getName(), localSearchOutput.getMatches());
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
//
//
//
//
//
//    }
//
//    @Override
//    public void findApplicableNodesUnderGivenParentNode(Connective parent,
//                                                        Map<AbstractFilter, Set<AbstractFilter>> applicableFiltersMap,
//                                                        Map<AbstractFilter, Literal> applicableLiteralsMap
//    ){
//        // All Literals and their corresponding Filters are not returned, but the lists are filled up as side effects
//        FilterFinder finder = new FilterFinder();
//        super.findApplicableNodesUnderGivenParentNode(parent, applicableFiltersMap, applicableLiteralsMap, finder);
//    }
//
//    /**
//     * Find any InOrbit literal
//     */
//    public class FilterFinder extends AbstractFilterFinder {
//
//        public FilterFinder(){
//            super(InOrbit.class);
//        }
//
//        @Override
//        public void setConstraints(AbstractFilter constraintSetter){
//        }
//
//        @Override
//        public void clearConstraints(){
//        }
//
//        @Override
//        public boolean check(){
//            return true;
//        }
//    }
//}
