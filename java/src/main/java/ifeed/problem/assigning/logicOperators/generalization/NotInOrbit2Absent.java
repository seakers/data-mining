package ifeed.problem.assigning.logicOperators.generalization;

import com.google.common.collect.Multiset;
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
import ifeed.problem.assigning.filters.NotInOrbit;
import ifeed.problem.assigning.filters.Absent;

import java.util.*;

public class NotInOrbit2Absent extends AbstractGeneralizationOperator{

    public NotInOrbit2Absent(BaseParams params, MOEABase base) {
        super(params, base, LogicalConnectiveType.AND);
    }


    public void apply(Connective root,
                         Connective parent,
                         AbstractFilter constraintSetterAbstract,
                         Set<AbstractFilter> matchingFilters,
                         Map<AbstractFilter, Literal> nodes
    ){

        NotInOrbit constraintSetter = (NotInOrbit) constraintSetterAbstract;

        // Select one matching filter
        List<AbstractFilter> matchingFiltersList = new ArrayList<>(matchingFilters);
        Collections.shuffle(matchingFiltersList);
        AbstractFilter selectedFilter = matchingFiltersList.get(0);

        // Find instruments that are shared in two nodes
        Set<Integer> sharedInstruments = new HashSet<>(constraintSetter.getInstruments());
        sharedInstruments.retainAll(((NotInOrbit) selectedFilter).getInstruments());

        // If there are multiple instruments that are shared select one
        int selectedArgument;
        if(sharedInstruments.size() > 1){
            ArrayList<Integer> sharedInstrumentsList = new ArrayList<>(sharedInstruments);
            Collections.shuffle(sharedInstrumentsList);
            selectedArgument = sharedInstrumentsList.get(0);
        }else{
            selectedArgument = sharedInstruments.iterator().next();
        }

        // Remove nodes that share an instrument
        Literal constraintSetterLiteral = nodes.get(constraintSetter);
        Literal matchingLiteral = nodes.get(selectedFilter);
        NotInOrbit matchingFilter = (NotInOrbit) selectedFilter;

        parent.removeLiteral(constraintSetterLiteral);
        parent.removeLiteral(matchingLiteral);

        if(constraintSetter.getInstruments().size() > 1){
            int orbit = constraintSetter.getOrbit();
            ArrayList<Integer> instruments = new ArrayList<>(constraintSetter.getInstruments());
            int selectedArgumentIndex = instruments.indexOf(selectedArgument);
            instruments.remove(selectedArgumentIndex);

            AbstractFilter newFilter = new NotInOrbit(params, orbit, Utils.intCollection2Array(instruments));
            Feature newFeature = base.getFeatureFetcher().fetch(newFilter);
            parent.addLiteral(newFeature.getName(), newFeature.getMatches());
        }

        if(matchingFilter.getInstruments().size() > 1){
            int orbit = matchingFilter.getOrbit();
            ArrayList<Integer> instruments = new ArrayList<>(matchingFilter.getInstruments());
            int selectedArgumentIndex = instruments.indexOf(selectedArgument);
            instruments.remove(selectedArgumentIndex);

            AbstractFilter newFilter = new NotInOrbit(params, orbit, Utils.intCollection2Array(instruments));
            Feature newFeature = base.getFeatureFetcher().fetch(newFilter);
            parent.addLiteral(newFeature.getName(), newFeature.getMatches());
        }

        // Add the Present feature to the grandparent node
        AbstractFilter presentFilter = new Absent(params, selectedArgument);
        Feature presentFeature = base.getFeatureFetcher().fetch(presentFilter);
        parent.addLiteral(presentFeature.getName(), presentFeature.getMatches());
    }


    @Override
    public void findApplicableNodesUnderGivenParentNode(Connective parent,
                                                        Map<AbstractFilter, Set<AbstractFilter>> applicableFiltersMap,
                                                        Map<AbstractFilter, Literal> applicableLiteralsMap
    ){
        // Find all InOrbit literals sharing at least two common instrument arguments inside the current node (parent).
        // All Literals and their corresponding Filters are not returned, but the lists are filled up as side effects
        FilterFinder finder = new FilterFinder();
        super.findApplicableNodesUnderGivenParentNode(parent, applicableFiltersMap, applicableLiteralsMap, finder);
    }

    public class FilterFinder extends AbstractFilterFinder {

        Multiset<Integer> instrumentsToBeIncluded;

        public FilterFinder(){
            super(NotInOrbit.class, NotInOrbit.class);
        }

        @Override
        public void setConstraints(AbstractFilter constraintSetter){
            this.instrumentsToBeIncluded = ((NotInOrbit) constraintSetter).getInstruments();
        }

        @Override
        public void clearConstraints(){
            this.instrumentsToBeIncluded = null;
        }

        /**
         * One of the instruments in the tested filter should be included in the constraint instrument set
         * @param filterToTest
         * @return
         */
        @Override
        public boolean check(AbstractFilter filterToTest){
            NotInOrbit temp = (NotInOrbit) filterToTest;

            // Check if two literals share the same instrument
            Multiset<Integer> instruments1 = this.instrumentsToBeIncluded;
            Multiset<Integer> instruments2 = temp.getInstruments();

            for(int inst:instruments2){
                if(instruments1.contains(inst)) {
                    return true;
                }
            }
            return false;
        }
    }
}
