package ifeed.problem.assigning.logicOperators.generalization;

import com.google.common.collect.Multiset;
import ifeed.Utils;
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

    public InOrbit2Together(BaseParams params, MOEABase base) {
        super(params, base);
    }

    public void apply(Connective root,
                         Connective parent,
                         AbstractFilter constraintSetterAbstract,
                         Set<AbstractFilter> matchingFilters,
                         Map<AbstractFilter, Literal> nodes
    ){

        Connective grandParent = super.base.getFeatureHandler().findParentNode(root, parent);

        if(grandParent == null){ // Parent node is the root node since it doesn't have a parent node
            super.base.getFeatureHandler().createNewRootNode(root);
            grandParent = root;

            // Store the newly generated node to parent
            parent = grandParent.getConnectiveChildren().get(0);
        }

        InOrbit constraintSetter = (InOrbit) constraintSetterAbstract;

        // Select two instruments randomly
        List<Integer> instrumentList = new ArrayList<>();
        for(int instrument: constraintSetter.getInstruments()){
            instrumentList.add(instrument);
        }
        Collections.shuffle(instrumentList);
        Set<Integer> selectedInstruments = new HashSet<>();
        selectedInstruments.add(instrumentList.get(0));
        selectedInstruments.add(instrumentList.get(0));

        // Remove nodes that share an instrument
        Literal constraintSetterLiteral = nodes.get(constraintSetter);
        parent.removeLiteral(constraintSetterLiteral);

        if(constraintSetter.getInstruments().size() > 2){
            int orbit = constraintSetter.getOrbit();
            ArrayList<Integer> instruments = new ArrayList<>(constraintSetter.getInstruments());
            instruments.removeAll(selectedInstruments);

            AbstractFilter newFilter = new InOrbit(params, orbit, Utils.intCollection2Array(instruments));
            Feature newFeature = base.getFeatureFetcher().fetch(newFilter);
            parent.addLiteral(newFeature.getName(), newFeature.getMatches());
        }

        // Add the Present feature to the grandparent node
        AbstractFilter presentFilter = new Together(params, Utils.intCollection2Array(new ArrayList<>(selectedInstruments)));
        Feature presentFeature = base.getFeatureFetcher().fetch(presentFilter);
        grandParent.addLiteral(presentFeature.getName(), presentFeature.getMatches());
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


