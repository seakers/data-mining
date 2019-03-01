package ifeed.problem.assigning.logicOperators.generalizationSingle;

import ifeed.Utils;
import ifeed.feature.*;
import ifeed.feature.logic.Connective;
import ifeed.feature.logic.ConnectiveTester;
import ifeed.feature.logic.Literal;
import ifeed.feature.logic.LogicalConnectiveType;
import ifeed.filter.AbstractFilter;
import ifeed.filter.AbstractFilterFinder;
import ifeed.local.params.BaseParams;
import ifeed.mining.AbstractLocalSearch;
import ifeed.mining.moea.MOEABase;
import ifeed.mining.moea.operators.AbstractLogicOperatorWithLocalSearch;
import ifeed.problem.assigning.Params;
import ifeed.problem.assigning.filters.InOrbit;
import ifeed.problem.assigning.filters.NotInOrbit;
import ifeed.problem.assigning.filters.Present;
import ifeed.problem.assigning.filters.Together;

import java.util.*;

public class InOrbit2TogetherPlusCondition extends AbstractLogicOperatorWithLocalSearch{

    private AbstractFeatureFetcher featureFetcher;
    private FeatureExpressionHandler featureHandler;

    public InOrbit2TogetherPlusCondition(BaseParams params, MOEABase base, AbstractLocalSearch localSearch){
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
            targetParentNode = parent;
            targetParentNode.addLiteral(newFeature.getName(), newFeature.getMatches());

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

        if(super.localSearch == null){
            throw new IllegalStateException("Local search needs to be defined to use this operator");
        }

        AbstractLocalSearch localSearch = super.localSearch;
        ConnectiveTester tester = new ConnectiveTester(root);
        localSearch.setRoot(tester);

        ConnectiveTester targetParentNodeTester = null;
        for(Connective node: tester.getDescendantConnectives(true)){
            if(this.featureHandler.featureTreeEquals(targetParentNode, node)){
                targetParentNodeTester = (ConnectiveTester) node;
            }
        }

        targetParentNodeTester.setAddNewNode();
        FeatureMetricComparator comparator = new FeatureMetricComparator(FeatureMetric.FCONFIDENCE);

        List<Feature> testFeatures = new ArrayList<>();
        for(int o = 0; o < params.getRightSetCardinality() + params.getRightSetGeneralizedConcepts().size() - 1; o++){
            NotInOrbit notInOrbit = new NotInOrbit(params, o, selectedInstruments);
            testFeatures.add(this.featureFetcher.fetch(notInOrbit));
        }
        Feature localSearchOutput = localSearch.runArgmax(testFeatures, comparator);
        if(localSearchOutput != null){
            targetParentNode.addLiteral(localSearchOutput.getName(), localSearchOutput.getMatches());
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

        int numInstruments = 0;

        public FilterFinder(){
            super(InOrbit.class);
        }

        @Override
        public void setConstraints(AbstractFilter constraintSetter){
            InOrbit inOrbit = (InOrbit) constraintSetter;
            this.numInstruments = inOrbit.getInstruments().size();
        }

        @Override
        public void clearConstraints(){
        }

        @Override
        public boolean check(){
            if(numInstruments > 1){
                return true;
            }else{
                return false;
            }
        }
    }
}
