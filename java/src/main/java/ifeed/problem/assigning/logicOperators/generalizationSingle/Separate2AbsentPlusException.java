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
import ifeed.problem.assigning.filters.Absent;
import ifeed.problem.assigning.filters.InOrbit;
import ifeed.problem.assigning.filters.NotInOrbit;
import ifeed.problem.assigning.filters.Separate;

import java.util.*;

public class Separate2AbsentPlusException extends AbstractLogicOperatorWithLocalSearch{

    private AbstractFeatureFetcher featureFetcher;
    private FeatureExpressionHandler featureHandler;

    public Separate2AbsentPlusException(BaseParams params, MOEABase base, AbstractLocalSearch localSearch){
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

        if(super.localSearch == null){
            throw new IllegalStateException("Local search needs to be defined to use this operator");
        }

        AbstractLocalSearch localSearch = super.localSearch;
        ConnectiveTester tester = new ConnectiveTester(root);
        localSearch.setRoot(tester);

        ConnectiveTester targetParentNodeTester = null;
        Literal targetLiteral = null;

        for(Connective testerNode: tester.getDescendantConnectives(true)){
            if(this.featureHandler.featureTreeEquals(targetParentNode, testerNode)){
                targetParentNodeTester = (ConnectiveTester) testerNode;
                break;
            }
        }

        for(Literal literal: targetParentNodeTester.getLiteralChildren()){
            if(this.featureHandler.literalEquals(newLiteral, literal)){
                targetLiteral = literal;
                break;
            }
        }

        // As the parent node is AND, add a new branch that is OR
        targetParentNodeTester.setAddNewNode(targetLiteral);

        // The operation "separate -> absent" improves precision, so look for exception that improves recall
        FeatureMetricComparator comparator = new FeatureMetricComparator(FeatureMetric.RCONFIDENCE);
        List<Feature> testFeatures = new ArrayList<>();
        for(int o = 0; o < params.getRightSetCardinality() + params.getRightSetGeneralizedConcepts().size() - 1; o++){
            InOrbit inOrbit = new InOrbit(params, o, selectedInstrument);
            testFeatures.add(this.featureFetcher.fetch(inOrbit));
        }

        Feature localSearchOutput = localSearch.runArgmax(testFeatures, comparator);
        if(localSearchOutput != null){

            // The parent node is AND, so add a new branch OR
            targetParentNode.removeNode(newLiteral);
            Connective tempBranch = new Connective(LogicalConnectiveType.OR);
            tempBranch.addNode(newLiteral);
            tempBranch.addLiteral(localSearchOutput.getName(), localSearchOutput.getMatches());
            targetParentNode.addBranch(tempBranch);
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
