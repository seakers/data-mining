package ifeed.problem.assigning.logicOperators.generalizationSingle;

import com.google.common.collect.Multiset;
import ifeed.Utils;
import ifeed.feature.Feature;
import ifeed.feature.logic.Connective;
import ifeed.feature.logic.Literal;
import ifeed.feature.logic.LogicalConnectiveType;
import ifeed.filter.AbstractFilter;
import ifeed.filter.AbstractFilterFinder;
import ifeed.local.params.BaseParams;
import ifeed.mining.moea.AbstractMOEABase;
import ifeed.mining.moea.GPMOEABase;
import ifeed.mining.moea.operators.AbstractLogicOperator;
import ifeed.problem.assigning.filters.InOrbit;
import ifeed.problem.assigning.filters.Together;
import java.util.*;

public class InOrbit2Together extends AbstractLogicOperator {

    public InOrbit2Together(BaseParams params, AbstractMOEABase base) {
        super(params, base);
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
        Feature newFeature = this.base.getFeatureFetcher().fetch(newFilter);

        Connective targetParentNode;
        if(parent.getLogic() == LogicalConnectiveType.AND){
            parent.addLiteral(newFeature.getName(), newFeature.getMatches());
            targetParentNode = parent;
        }else{
            targetParentNode = new Connective(LogicalConnectiveType.AND);
            targetParentNode.addLiteral(newFeature.getName(), newFeature.getMatches());
            parent.addBranch(targetParentNode);
        }

        if(constraintSetter.getInstruments().size() > 2){
            int orbit = constraintSetter.getOrbit();
            ArrayList<Integer> instruments = new ArrayList<>(constraintSetter.getInstruments());
            instruments.removeAll(selectedInstruments);

            AbstractFilter modifiedFilter = new InOrbit(params, orbit, Utils.intCollection2Array(instruments));
            Feature modifiedFeature = this.base.getFeatureFetcher().fetch(modifiedFilter);

            if(!instruments.isEmpty()){
                targetParentNode.addLiteral(modifiedFeature.getName(), modifiedFeature.getMatches());
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


