//package ifeed.problem.assigning.logicOperators.generalization;
//
//import com.google.common.collect.Multiset;
//import ifeed.Utils;
//import ifeed.feature.Feature;
//import ifeed.feature.logic.Connective;
//import ifeed.feature.logic.Literal;
//import ifeed.feature.logic.LogicalConnectiveType;
//import ifeed.filter.AbstractFilter;
//import ifeed.filter.AbstractFilterFinder;
//import ifeed.local.params.BaseParams;
//import ifeed.mining.moea.GPMOEABase;
//import ifeed.mining.moea.operators.AbstractGeneralizationOperator;
//import ifeed.problem.assigning.filters.InOrbit;
//import ifeed.problem.assigning.filters.Together;
//
//import java.util.*;
//
//public class InOrbit2Together extends AbstractGeneralizationOperator{
//
//    public InOrbit2Together(BaseParams params, GPMOEABase base) {
//        super(params, base, LogicalConnectiveType.OR);
//    }
//
//    public void apply(Connective root,
//                      Connective parent,
//                      AbstractFilter constraintSetterAbstract,
//                      Set<AbstractFilter> matchingFilters,
//                      Map<AbstractFilter, Literal> nodes
//    ){
//
//        Connective grandParent = super.base.getFeatureHandler().findParentNode(root, parent);
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
//        if(matchingFiltersList.size() > 1){
//            Collections.shuffle(matchingFiltersList);
//        }
//        AbstractFilter selectedFilter = matchingFiltersList.get(0);
//
//        // Find instruments that are shared in two nodes
//        Set<Integer> sharedInstruments = new HashSet<>(constraintSetter.getInstruments());
//        sharedInstruments.retainAll(((InOrbit) selectedFilter).getInstruments());
//
//        // Remove nodes that share an instrument
//        Literal constraintSetterLiteral = nodes.get(constraintSetter);
//        Literal matchingLiteral = nodes.get(selectedFilter);
//        InOrbit matchingFilter = (InOrbit) selectedFilter;
//
//        parent.removeLiteral(constraintSetterLiteral);
//        parent.removeLiteral(matchingLiteral);
//
//        if(constraintSetter.getInstruments().size() - sharedInstruments.size() > 0){
//            int orbit = constraintSetter.getOrbit();
//            ArrayList<Integer> instruments = new ArrayList<>(constraintSetter.getInstruments());
//            instruments.removeAll(sharedInstruments);
//
//            AbstractFilter newFilter = new InOrbit(params, orbit, Utils.intCollection2Array(instruments));
//            Feature newFeature = base.getFeatureFetcher().fetch(newFilter);
//            parent.addLiteral(newFeature.getName(), newFeature.getMatches());
//        }
//
//        if(matchingFilter.getInstruments().size() - sharedInstruments.size() > 0){
//            int orbit = matchingFilter.getOrbit();
//            ArrayList<Integer> instruments = new ArrayList<>(matchingFilter.getInstruments());
//            instruments.removeAll(sharedInstruments);
//
//            AbstractFilter newFilter = new InOrbit(params, orbit, Utils.intCollection2Array(instruments));
//            Feature newFeature = base.getFeatureFetcher().fetch(newFilter);
//            parent.addLiteral(newFeature.getName(), newFeature.getMatches());
//        }
//
//        // Add the Present feature to the grandparent node
//        AbstractFilter presentFilter = new Together(params, Utils.intCollection2Array(new ArrayList<>(sharedInstruments)));
//        Feature presentFeature = base.getFeatureFetcher().fetch(presentFilter);
//        grandParent.addLiteral(presentFeature.getName(), presentFeature.getMatches());
//    }
//
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
//     * Find all InOrbit literals sharing at least two common instrument arguments inside the current node (parent).
//     */
//    public class FilterFinder extends AbstractFilterFinder {
//
//        Multiset<Integer> instrumentsSet1;
//
//        public FilterFinder(){
//            super(InOrbit.class, InOrbit.class);
//        }
//
//        @Override
//        public void setConstraints(AbstractFilter constraintSetter){
//            InOrbit temp = (InOrbit) constraintSetter;
//            this.instrumentsSet1 = temp.getInstruments();
//        }
//
//        @Override
//        public void clearConstraints(){
//            this.instrumentsSet1 = null;
//        }
//
//        /**
//         * One of the instruments in the tested filter should be included in the constraint instrument set
//         * @param filterToTest
//         * @return
//         */
//        @Override
//        public boolean check(AbstractFilter filterToTest){
//
//            InOrbit temp = (InOrbit) filterToTest;
//
//            // Check if two literals share the same instrument
//            Multiset<Integer> instruments1 = this.instrumentsSet1;
//            Multiset<Integer> instruments2 = temp.getInstruments();
//
//            int cnt = 0;
//            for(int inst:instruments2){
//                if(instruments1.contains(inst)) {
//                    cnt++;
//                    if(cnt > 1){ // There should be at least two instruments shared by the two Filters
//                        return true;
//                    }
//                }
//            }
//            return false;
//        }
//    }
//}
//
//
