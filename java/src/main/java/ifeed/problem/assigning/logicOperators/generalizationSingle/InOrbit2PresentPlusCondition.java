package ifeed.problem.assigning.logicOperators.generalizationSingle;

import ifeed.Utils;
import ifeed.feature.*;
import ifeed.feature.logic.Connective;
import ifeed.feature.logic.Literal;
import ifeed.feature.logic.LogicalConnectiveType;
import ifeed.filter.AbstractFilter;
import ifeed.filter.AbstractFilterFinder;
import ifeed.local.params.BaseParams;
import ifeed.mining.AbstractLocalSearch;
import ifeed.mining.moea.GPMOEABase;
import ifeed.mining.moea.operators.AbstractLogicOperatorWithLocalSearch;
import ifeed.problem.assigning.Params;
import ifeed.problem.assigning.filters.InOrbit;
import ifeed.problem.assigning.filters.NotInOrbit;
import ifeed.problem.assigning.filters.Present;

import java.util.*;

public class InOrbit2PresentPlusCondition extends AbstractLogicOperatorWithLocalSearch{

    private AbstractFeatureFetcher featureFetcher;

    public InOrbit2PresentPlusCondition(BaseParams params, GPMOEABase base, AbstractLocalSearch localSearch){
        super(params, base, localSearch);
        this.featureFetcher = localSearch.getFeatureFetcher();
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

        // Create new feature
        AbstractFilter newFilter = new Present(params, selectedInstrument);
        Feature newFeature = this.featureFetcher.fetch(newFilter);

        Connective targetParentNode;
        if(parent.getLogic() == LogicalConnectiveType.AND){
            targetParentNode = parent;
            targetParentNode.addLiteral(newFeature.getName(), newFeature.getMatches());

        }else{
            targetParentNode = new Connective(LogicalConnectiveType.AND);
            targetParentNode.addLiteral(newFeature.getName(), newFeature.getMatches());
            parent.addBranch(targetParentNode);
        }

        if(constraintSetter.getInstruments().size() > 1){
            int orbit = constraintSetter.getOrbit();
            ArrayList<Integer> instruments = new ArrayList<>(constraintSetter.getInstruments());
            int selectedArgumentIndex = instruments.indexOf(selectedInstrument);
            instruments.remove(selectedArgumentIndex);

            AbstractFilter modifiedFilter = new InOrbit(params, orbit, Utils.intCollection2Array(instruments));
            Feature modifiedFeature = this.featureFetcher.fetch(modifiedFilter);

            if(!instruments.isEmpty()){
                targetParentNode.addLiteral(modifiedFeature.getName(), modifiedFeature.getMatches());
            }
        }

        List<Feature> baseFeaturesToTest = new ArrayList<>();
        for(int o = 0; o < params.getRightSetCardinality() + params.getRightSetGeneralizedConcepts().size() - 1; o++){
            NotInOrbit notInOrbit = new NotInOrbit(params, o, selectedInstrument);
            baseFeaturesToTest.add(this.featureFetcher.fetch(notInOrbit));
        }

        // Add extra conditions to make smaller steps
        super.addExtraCondition(root, targetParentNode, null, baseFeaturesToTest, 3, FeatureMetric.FCONFIDENCE);
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
