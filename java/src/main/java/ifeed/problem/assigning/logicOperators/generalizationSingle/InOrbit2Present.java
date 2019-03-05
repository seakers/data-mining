package ifeed.problem.assigning.logicOperators.generalizationSingle;

import ifeed.Utils;
import ifeed.feature.logic.Connective;
import ifeed.feature.logic.Literal;
import ifeed.feature.Feature;
import ifeed.feature.logic.LogicalConnectiveType;
import ifeed.filter.AbstractFilter;
import ifeed.filter.AbstractFilterFinder;
import ifeed.local.params.BaseParams;
import ifeed.mining.moea.AbstractMOEABase;
import ifeed.mining.moea.GPMOEABase;
import ifeed.mining.moea.operators.AbstractLogicOperator;
import ifeed.problem.assigning.Params;
import ifeed.problem.assigning.filters.InOrbit;
import ifeed.problem.assigning.filters.Present;
import java.util.*;

public class InOrbit2Present extends AbstractLogicOperator {

    public InOrbit2Present(BaseParams params, AbstractMOEABase base) {
        super(params, base);
    }

    public void apply(Connective root,
                         Connective parent,
                         AbstractFilter constraintSetterAbstract,
                         Set<AbstractFilter> matchingFilters,
                         Map<AbstractFilter, Literal> nodes
    ){

        Params params = (Params) super.params;
        InOrbit constraintSetter = (InOrbit) constraintSetterAbstract;

        // Select an instrument randomly
        List<Integer> instrumentList = new ArrayList<>();
        for(int instrument: constraintSetter.getInstruments()){
            instrumentList.add(instrument);
        }
        Collections.shuffle(instrumentList);
        int selectedInstrument = instrumentList.get(0);

        // Remove the given node
        Literal constraintSetterLiteral = nodes.get(constraintSetter);
        parent.removeLiteral(constraintSetterLiteral);

        // Add new feature
        AbstractFilter newFilter = new Present(params, selectedInstrument);
        Feature newFeature = this.base.getFeatureFetcher().fetch(newFilter);

        Connective newBranch;
        if(parent.getLogic() == LogicalConnectiveType.AND){
            parent.addLiteral(newFeature.getName(), newFeature.getMatches());
            newBranch = null;
        }else{
            newBranch = new Connective(LogicalConnectiveType.AND);
            newBranch.addLiteral(newFeature.getName(), newFeature.getMatches());
            parent.addBranch(newBranch);
        }

        if(constraintSetter.getInstruments().size() > 1){
            int orbit = constraintSetter.getOrbit();
            ArrayList<Integer> instruments = new ArrayList<>(constraintSetter.getInstruments());
            int selectedArgumentIndex = instruments.indexOf(selectedInstrument);
            instruments.remove(selectedArgumentIndex);

            AbstractFilter modifiedFilter = new InOrbit(params, orbit, Utils.intCollection2Array(instruments));
            Feature modifiedFeature = this.base.getFeatureFetcher().fetch(modifiedFilter);

            if(!instruments.isEmpty()){
                if(parent.getLogic() == LogicalConnectiveType.AND){
                    parent.addLiteral(modifiedFeature.getName(), modifiedFeature.getMatches());

                }else{
                    newBranch.addLiteral(modifiedFeature.getName(), modifiedFeature.getMatches());
                }
            }
        }
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

    /**
     * Find any InOrbit literal
     */
    public class FilterFinder extends AbstractFilterFinder {

        public FilterFinder(){
            super(InOrbit.class);
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
