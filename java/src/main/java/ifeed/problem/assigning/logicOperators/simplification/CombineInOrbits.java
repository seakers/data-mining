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
import ifeed.problem.assigning.filters.InOrbit;

import java.util.*;

public class CombineInOrbits extends AbstractSimplificationOperator{

    public CombineInOrbits(BaseParams params, MOEABase base) {
        super(params, base, LogicalConnectiveType.AND);
    }

    public void apply(Connective root,
                         Connective parent,
                         AbstractFilter constraintSetterAbstract,
                         Set<AbstractFilter> matchingFilters,
                         Map<AbstractFilter, Literal> nodes
    ){

        InOrbit constraintSetter = (InOrbit) constraintSetterAbstract;

        // Select one matching filter
        List<AbstractFilter> matchingFiltersList = new ArrayList<>(matchingFilters);

        int orbit = constraintSetter.getOrbit();
        HashSet<Integer> instruments = new HashSet<>(constraintSetter.getInstruments());

        // Remove nodes that share an instrument
        Literal constraintSetterLiteral = nodes.get(constraintSetter);
        parent.removeLiteral(constraintSetterLiteral);

        for(AbstractFilter filter: matchingFiltersList){
            Literal matchingLiteral = nodes.get(filter);
            parent.removeLiteral(matchingLiteral);

            instruments.addAll(((InOrbit) filter).getInstruments());
        }

        // Add the Present feature to the grandparent node
        AbstractFilter presentFilter = new InOrbit(params, orbit, Utils.intCollection2Array(new ArrayList<>(instruments)));
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
            super(InOrbit.class, InOrbit.class);
        }

        @Override
        public void setConstraints(AbstractFilter constraintSetter){
            InOrbit temp = (InOrbit) constraintSetter;
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
            InOrbit temp = (InOrbit) filterToTest;

            // Check if two literals share the same instrument
            return this.orbit == temp.getOrbit();
        }
    }
}
