//package ifeed.problem.assigning.logicOperators.generalizationWithCondition;
//
//import ifeed.feature.Feature;
//import ifeed.feature.logic.Connective;
//import ifeed.feature.logic.Literal;
//import ifeed.feature.logic.LogicalConnectiveType;
//import ifeed.filter.AbstractFilter;
//import ifeed.filter.AbstractFilterFinder;
//import ifeed.local.params.BaseParams;
//import ifeed.mining.moea.GPMOEABase;
//import ifeed.mining.moea.operators.AbstractGeneralizationOperator;
//import ifeed.problem.assigning.filters.EmptyOrbit;
//import ifeed.problem.assigning.filters.NotInOrbit;
//
//import java.util.Map;
//import java.util.Set;
//
//public class NotInOrbit2EmptyOrbit extends AbstractGeneralizationOperator{
//
//    public NotInOrbit2EmptyOrbit(BaseParams params, GPMOEABase base) {
//        super(params, base, LogicalConnectiveType.AND);
//    }
//
//    public void apply(Connective root,
//                         Connective parent,
//                         AbstractFilter constraintSetterAbstract,
//                         Set<AbstractFilter> matchingFilters,
//                         Map<AbstractFilter, Literal> nodes
//    ){
//
//        NotInOrbit constraintSetter = (NotInOrbit) constraintSetterAbstract;
//        int orbit = constraintSetter.getOrbit();
//
//        // Remove notInOrbit nodes having the same orbit
//        Literal constraintSetterLiteral = nodes.get(constraintSetter);
//        parent.removeLiteral(constraintSetterLiteral);
//
//        for(AbstractFilter filter: matchingFilters){
//            if(((NotInOrbit)filter).getOrbit() == orbit){
//                Literal literalToBeRemoved = nodes.get(filter);
//                parent.removeLiteral(literalToBeRemoved);
//            }
//        }
//
//        AbstractFilter emptyOrbitFilter = new EmptyOrbit(params, orbit);
//        Feature presentFeature = base.getFeatureFetcher().fetch(emptyOrbitFilter);
//        parent.addLiteral(presentFeature.getName(), presentFeature.getMatches());
//    }
//
//    @Override
//    public void findApplicableNodesUnderGivenParentNode(Connective parent,
//                                                        Map<AbstractFilter, Set<AbstractFilter>> applicableFiltersMap,
//                                                        Map<AbstractFilter, Literal> applicableLiteralsMap
//    ){
//
//        // All Literals and their corresponding Filters are not returned, but the lists are filled up as side effects
//        FilterFinder finder = new FilterFinder();
//        super.findApplicableNodesUnderGivenParentNode(parent, applicableFiltersMap, applicableLiteralsMap, finder);
//    }
//
//    public class FilterFinder extends AbstractFilterFinder {
//
//        private int unusedOrbit;
//
//        public FilterFinder(){
//            super(NotInOrbit.class, NotInOrbit.class);
//        }
//
//        @Override
//        public void setConstraints(AbstractFilter constraintSetter){
//            NotInOrbit temp = (NotInOrbit) constraintSetter;
//            this.unusedOrbit = temp.getOrbit();
//        }
//
//        @Override
//        public void clearConstraints(){
//            this.unusedOrbit = -1;
//        }
//
//        /**
//         * One of the instruments in the tested filter should be included in the constraint instrument set
//         * @param filterToTest
//         * @return
//         */
//        @Override
//        public boolean check(AbstractFilter filterToTest){
//            // Check if two literals share the same orbit
//            return this.unusedOrbit  == ((NotInOrbit) filterToTest).getOrbit();
//        }
//    }
//}
