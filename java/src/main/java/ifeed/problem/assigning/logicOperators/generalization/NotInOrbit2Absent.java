package ifeed.problem.assigning.logicOperators.generalization;

import com.google.common.collect.Multiset;
import ifeed.Utils;
import ifeed.feature.logic.Connective;
import ifeed.feature.logic.Literal;
import ifeed.feature.Feature;
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

    public NotInOrbit2Absent(BaseParams params, MOEABase base) {
        super(params, base);
    }

    public void apply(Connective root,
                      Connective parent,
                      AbstractFilter constraintSetterAbstract,
                      Set<AbstractFilter> matchingFilters,
                      Map<AbstractFilter, Literal> nodes
    ){

        Params params = (Params) super.params;
        Connective grandParent = (Connective) parent.getParent();

        if(grandParent == null){ // Parent node is the root node since it doesn't have a parent node
            super.base.getFeatureHandler().createNewRootNode(root);
            grandParent = root;

            // Store the newly generated node to parent
            parent = grandParent.getConnectiveChildren().get(0);
        }

        NotInOrbit constraintSetter = (NotInOrbit) constraintSetterAbstract;

        // Select an instrument randomly
        List<Integer> instrumentList = new ArrayList<>();
        for(int instrument: constraintSetter.getInstruments()){
            instrumentList.add(instrument);
        }
        Collections.shuffle(instrumentList);
        int selectedInstrument = instrumentList.get(0);

        // Remove nodes that contain the instrument
        Literal constraintSetterLiteral = nodes.get(constraintSetter);
        parent.removeLiteral(constraintSetterLiteral);

        if(constraintSetter.getInstruments().size() > 1){
            int orbit = constraintSetter.getOrbit();
            ArrayList<Integer> instruments = new ArrayList<>(constraintSetter.getInstruments());
            int selectedArgumentIndex = instruments.indexOf(selectedInstrument);
            instruments.remove(selectedArgumentIndex);

            AbstractFilter newFilter = new NotInOrbit(params, orbit, Utils.intCollection2Array(instruments));
            Feature newFeature = base.getFeatureFetcher().fetch(newFilter);
            parent.addLiteral(newFeature.getName(), newFeature.getMatches());
        }

        // Add the Present feature to the grandparent node
        AbstractFilter presentFilter = new Absent(params, selectedInstrument);
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
