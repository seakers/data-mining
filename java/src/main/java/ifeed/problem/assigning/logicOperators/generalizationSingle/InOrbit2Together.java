package ifeed.problem.assigning.logicOperators.generalizationSingle;

import com.google.common.collect.Multiset;
import ifeed.Utils;
import ifeed.feature.AbstractFeatureFetcher;
import ifeed.feature.Feature;
import ifeed.feature.logic.Connective;
import ifeed.feature.logic.Literal;
import ifeed.feature.logic.LogicalConnectiveType;
import ifeed.filter.AbstractFilter;
import ifeed.filter.AbstractFilterFinder;
import ifeed.local.params.BaseParams;
import ifeed.mining.moea.MOEABase;
import ifeed.mining.moea.operators.AbstractGeneralizationOperator;
import ifeed.problem.assigning.filters.InOrbit;
import ifeed.problem.assigning.filters.Together;
import java.util.*;

public class InOrbit2Together extends AbstractGeneralizationOperator{

    private AbstractFeatureFetcher featureFetcher;

    public InOrbit2Together(BaseParams params, MOEABase base) {
        super(params, base);
        this.featureFetcher = base.getFeatureFetcher();
    }

    public InOrbit2Together(BaseParams params, AbstractFeatureFetcher featureFetcher){
        super(params, featureFetcher.getFilterFetcher());
        this.featureFetcher = featureFetcher;
    }

    public void apply(Connective root,
                         Connective parent,
                         AbstractFilter constraintSetterAbstract,
                         Set<AbstractFilter> matchingFilters,
                         Map<AbstractFilter, Literal> nodes
    ){

        InOrbit constraintSetter = (InOrbit) constraintSetterAbstract;

        // Select two instruments randomly
        List<Integer> instrumentList = new ArrayList<>();
        for(int instrument: constraintSetter.getInstruments()){
            instrumentList.add(instrument);
        }
        Collections.shuffle(instrumentList);
        Set<Integer> selectedInstruments = new HashSet<>();
        selectedInstruments.add(instrumentList.get(0));
        selectedInstruments.add(instrumentList.get(1));

        // Remove node
        Literal constraintSetterLiteral = nodes.get(constraintSetter);
        parent.removeLiteral(constraintSetterLiteral);

        // Add new node
        AbstractFilter newFilter = new Together(params, Utils.intCollection2Array(new ArrayList<>(selectedInstruments)));
        Feature newFeature = this.featureFetcher.fetch(newFilter);

        if(parent.getLogic() == LogicalConnectiveType.AND){
            parent.addLiteral(newFeature.getName(), newFeature.getMatches());

        }else{
            Connective newBranch = new Connective(LogicalConnectiveType.AND);
            newBranch.addLiteral(newFeature.getName(), newFeature.getMatches());
            parent.addBranch(newBranch);
        }

        if(constraintSetter.getInstruments().size() > 2){
            int orbit = constraintSetter.getOrbit();
            ArrayList<Integer> instruments = new ArrayList<>(constraintSetter.getInstruments());
            instruments.removeAll(selectedInstruments);

            AbstractFilter modifiedFilter = new InOrbit(params, orbit, Utils.intCollection2Array(instruments));
            Feature modifiedFeature = this.featureFetcher.fetch(modifiedFilter);

            if(parent.getLogic() == LogicalConnectiveType.AND){
                parent.addLiteral(modifiedFeature.getName(), modifiedFeature.getMatches());

            }else{
                Connective newBranch = parent.getConnectiveChildren().get(0);
                newBranch.addLiteral(modifiedFeature.getName(), modifiedFeature.getMatches());
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
     * Find all InOrbit literals sharing at least two common instrument arguments inside the current node (parent).
     */
    public class FilterFinder extends AbstractFilterFinder {

        public FilterFinder(){
            super(InOrbit.class);
        }

        Multiset<Integer> instrumentSet;

        @Override
        public void setConstraints(AbstractFilter constraintSetter){
            this.instrumentSet = ((InOrbit) constraintSetter).getInstruments();
        }

        @Override
        public void clearConstraints(){
        }

        /**
         * Check if there are at least two instruments
         * @return
         */
        @Override
        public boolean check(){
            if(this.instrumentSet.size() >= 2){
                return true;

            }else{
                return false;
            }
        }
    }
}


