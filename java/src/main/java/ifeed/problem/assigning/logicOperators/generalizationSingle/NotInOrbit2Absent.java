package ifeed.problem.assigning.logicOperators.generalizationSingle;

import ifeed.Utils;
import ifeed.feature.AbstractFeatureFetcher;
import ifeed.feature.logic.Connective;
import ifeed.feature.logic.Literal;
import ifeed.feature.Feature;
import ifeed.feature.logic.LogicalConnectiveType;
import ifeed.filter.AbstractFilter;
import ifeed.filter.AbstractFilterFinder;
import ifeed.local.params.BaseParams;
import ifeed.mining.moea.operators.AbstractGeneralizationOperator;
import ifeed.mining.moea.MOEABase;
import ifeed.problem.assigning.Params;
import ifeed.problem.assigning.filters.Absent;
import ifeed.problem.assigning.filters.NotInOrbit;
import java.util.*;

public class NotInOrbit2Absent extends AbstractGeneralizationOperator{

    private AbstractFeatureFetcher featureFetcher;

    public NotInOrbit2Absent(BaseParams params, MOEABase base) {
        super(params, base);
        this.featureFetcher = base.getFeatureFetcher();
    }

    public NotInOrbit2Absent(BaseParams params, AbstractFeatureFetcher featureFetcher){
        super(params, featureFetcher.getFilterFetcher());
        this.featureFetcher = featureFetcher;
    }

    public void apply(Connective root,
                      Connective parent,
                      AbstractFilter constraintSetterAbstract,
                      Set<AbstractFilter> matchingFilters,
                      Map<AbstractFilter, Literal> nodes
    ){

        Params params = (Params) super.params;
        NotInOrbit constraintSetter = (NotInOrbit) constraintSetterAbstract;

        // Select an instrument randomly
        List<Integer> instrumentList = new ArrayList<>();
        for(int instrument: constraintSetter.getInstruments()){
            instrumentList.add(instrument);
        }
        Collections.shuffle(instrumentList);
        int selectedInstrument = instrumentList.get(0);

        // Remove node
        Literal constraintSetterLiteral = nodes.get(constraintSetter);
        parent.removeLiteral(constraintSetterLiteral);

        // Add new node
        AbstractFilter newFilter = new Absent(params, selectedInstrument);
        Feature newFeature = this.featureFetcher.fetch(newFilter);

        Connective newBranch;
        if(parent.getLogic() == LogicalConnectiveType.AND){
            parent.addLiteral(newFeature.getName(), newFeature.getMatches());

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

            AbstractFilter modifiedFilter = new NotInOrbit(params, orbit, Utils.intCollection2Array(instruments));
            Feature modifiedFeature = this.featureFetcher.fetch(modifiedFilter);

            if(!instruments.isEmpty()){
                if(parent.getLogic() == LogicalConnectiveType.AND){
                    parent.addLiteral(modifiedFeature.getName(), modifiedFeature.getMatches());
                    newBranch = null;

                }else{
                    newBranch = parent.getConnectiveChildren().get(0);
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
