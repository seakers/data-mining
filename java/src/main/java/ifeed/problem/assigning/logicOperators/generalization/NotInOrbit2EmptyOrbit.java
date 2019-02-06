package ifeed.problem.assigning.logicOperators.generalization;

import ifeed.Utils;
import ifeed.feature.logic.Connective;
import ifeed.feature.logic.Literal;
import ifeed.feature.logic.LogicalConnectiveType;
import ifeed.feature.Feature;
import ifeed.filter.AbstractFilter;
import ifeed.filter.AbstractFilterFinder;
import ifeed.local.params.BaseParams;
import ifeed.mining.moea.operators.AbstractGeneralizationOperator;
import ifeed.mining.moea.MOEABase;
import ifeed.problem.assigning.filters.Absent;
import ifeed.problem.assigning.filters.EmptyOrbit;
import ifeed.problem.assigning.filters.NotInOrbit;

import java.util.*;

public class NotInOrbit2EmptyOrbit extends AbstractGeneralizationOperator{

    public NotInOrbit2EmptyOrbit(BaseParams params, MOEABase base) {
        super(params, base, LogicalConnectiveType.AND);
    }

    public void apply(Connective root,
                         Connective parent,
                         AbstractFilter constraintSetterAbstract,
                         Set<AbstractFilter> matchingFilters,
                         Map<AbstractFilter, Literal> nodes
    ){

        NotInOrbit constraintSetter = (NotInOrbit) constraintSetterAbstract;
        int orbit = constraintSetter.getOrbit();

        // Remove notInOrbit nodes having the same orbit
        Literal constraintSetterLiteral = nodes.get(constraintSetter);
        parent.removeLiteral(constraintSetterLiteral);

        AbstractFilter emptyOrbitFilter = new EmptyOrbit(params, orbit);
        Feature presentFeature = base.getFeatureFetcher().fetch(emptyOrbitFilter);
        parent.addLiteral(presentFeature.getName(), presentFeature.getMatches());
    }

    @Override
    public void findApplicableNodesUnderGivenParentNode(Connective parent,
                                                        Map<AbstractFilter, Set<AbstractFilter>> applicableFiltersMap,
                                                        Map<AbstractFilter, Literal> applicableLiteralsMap
    ){

        // All Literals and their corresponding Filters are not returned, but the lists are filled up as side effects
        FilterFinder finder = new FilterFinder();
        super.findApplicableNodesUnderGivenParentNode(parent, applicableFiltersMap, applicableLiteralsMap, finder);
    }

    public class FilterFinder extends AbstractFilterFinder {

        public FilterFinder(){
            super(NotInOrbit.class);
        }

        @Override
        public void setConstraints(AbstractFilter constraintSetter){
        }

        @Override
        public void clearConstraints(){
        }

        @Override
        public boolean check(){
            return true;
        }
    }
}
