package ifeed.problem.assigning.logicOperators.simplification;

import ifeed.Utils;
import ifeed.feature.logic.Connective;
import ifeed.feature.logic.Literal;
import ifeed.feature.logic.LogicalConnectiveType;
import ifeed.feature.Feature;
import ifeed.filter.AbstractFilter;
import ifeed.filter.AbstractFilterFinder;
import ifeed.local.params.BaseParams;
import ifeed.mining.moea.MOEABase;

import ifeed.mining.moea.operators.AbstractSimplificationOperator;
import ifeed.problem.assigning.filters.NotInOrbit;

import java.util.*;

public class CombineNotInOrbits extends AbstractSimplificationOperator{

    public CombineNotInOrbits(BaseParams params, MOEABase base) {
        super(params, base, LogicalConnectiveType.AND);
    }


    protected void apply(Connective root,
                         Connective parent,
                         AbstractFilter constraintSetterAbstract,
                         Set<AbstractFilter> matchingFilters,
                         Map<AbstractFilter, Literal> nodes
    ){

        NotInOrbit constraintSetter = (NotInOrbit) constraintSetterAbstract;

        // Select one matching filter
        List<AbstractFilter> matchingFiltersList = new ArrayList<>(matchingFilters);

        int orbit = constraintSetter.getOrbit();
        HashSet<Integer> instruments = new HashSet<>(constraintSetter.getInstruments());

        // Remove nodes that share an instrument
        Literal constraintSetterLiteral = nodes.get(constraintSetter);
        parent.removeLiteral(constraintSetterLiteral);

        for(AbstractFilter filter:matchingFiltersList){
            Literal matchingLiteral = nodes.get(filter);
            parent.removeLiteral(matchingLiteral);

            instruments.addAll(((NotInOrbit) filter).getInstruments());
        }

        // Add the Present feature to the grandparent node
        AbstractFilter presentFilter = new NotInOrbit(params, orbit, Utils.listArray2IntegerArray(new ArrayList<>(instruments)));
        Feature presentFeature = base.getFeatureFetcher().fetch(presentFilter);
        parent.addLiteral(presentFeature.getName(), presentFeature.getMatches());
    }

    @Override
    public void findApplicableNodesUnderGivenParentNode(Connective parent,
                                                        Map<AbstractFilter, Set<AbstractFilter>> applicableFiltersMap,
                                                        Map<AbstractFilter, Literal> applicableLiteralsMap
    ){
        // Find all InOrbit literals sharing at least one common instrument argument inside the current node (parent).
        // All Literals and their corresponding Filters are not returned, but the lists are filled up as side effects
        FilterFinder finder = new FilterFinder();
        super.findApplicableNodesUnderGivenParentNode(parent, applicableFiltersMap, applicableLiteralsMap, finder);
    }

    public class FilterFinder extends AbstractFilterFinder {

        public int orbit;

        public FilterFinder(){
            super(NotInOrbit.class, NotInOrbit.class);
        }

        @Override
        public void setConstraints(AbstractFilter constraintSetter){
            NotInOrbit temp = (NotInOrbit) constraintSetter;
            this.orbit = temp.getOrbit();
        }

        @Override
        public void clearConstraints(){
            this.orbit = -1;
        }

        /**
         * Both features should assign instruments to the same orbit
         * @param filterToTest
         * @return
         */
        @Override
        public boolean check(AbstractFilter filterToTest){
            NotInOrbit temp = (NotInOrbit) filterToTest;

            // Check if two literals share the same instrument
            return this.orbit == temp.getOrbit();
        }
    }
}
