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
import ifeed.problem.assigning.filters.Absent;
import ifeed.problem.assigning.filters.InOrbit;
import ifeed.problem.assigning.filters.Separate;

import java.util.*;

public class Separate2AbsentPlusException extends AbstractLogicOperatorWithLocalSearch{

    private AbstractFeatureFetcher featureFetcher;
    private FeatureExpressionHandler featureHandler;

    public Separate2AbsentPlusException(BaseParams params, GPMOEABase base, AbstractLocalSearch localSearch){
        super(params, base, localSearch);
        this.featureFetcher = localSearch.getFeatureFetcher();
        this.featureHandler = localSearch.getFeatureHandler();
    }

    public void apply(Connective root,
                         Connective parent,
                         AbstractFilter constraintSetterAbstract,
                         Set<AbstractFilter> matchingFilters,
                         Map<AbstractFilter, Literal> nodes
    ){

        Params params = (Params) super.params;

        Separate constraintSetter = (Separate) constraintSetterAbstract;

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
        Feature newFeature = this.base.getFeatureFetcher().fetch(newFilter);
        Literal newLiteral = new Literal(newFeature.getName(), newFeature.getMatches());

        Connective targetParentNode;
        if(parent.getLogic() == LogicalConnectiveType.AND){
            targetParentNode = parent;
            targetParentNode.addLiteral(newLiteral);

        }else{
            targetParentNode = new Connective(LogicalConnectiveType.AND);
            targetParentNode.addLiteral(newLiteral);
            parent.addBranch(targetParentNode);
        }

        if(constraintSetter.getInstruments().size() > 2){
            ArrayList<Integer> instruments = new ArrayList<>(constraintSetter.getInstruments());
            int selectedArgumentIndex = instruments.indexOf(selectedInstrument);
            instruments.remove(selectedArgumentIndex);

            AbstractFilter modifiedFilter = new Separate(params, Utils.intCollection2Array(instruments));
            Feature modifiedFeature = this.base.getFeatureFetcher().fetch(modifiedFilter);

            if(!instruments.isEmpty()){
                targetParentNode.addLiteral(modifiedFeature.getName(), modifiedFeature.getMatches());
            }
        }

        List<Feature> baseFeaturesToTest = new ArrayList<>();
        for(int o = 0; o < params.getRightSetCardinality() + params.getRightSetGeneralizedConcepts().size() - 1; o++){
            InOrbit inOrbit = new InOrbit(params, o, selectedInstrument);
            baseFeaturesToTest.add(this.featureFetcher.fetch(inOrbit));
        }
        // Add an exception to make smaller steps
        // The operation "separate -> absent" improves precision, so look for exception that improves recall
        super.addExtraCondition(root, targetParentNode, newLiteral, baseFeaturesToTest, 3, FeatureMetric.RCONFIDENCE);

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
            super(Separate.class);
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
