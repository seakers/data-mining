package ifeed.problem.assigning.logicOperators.generalizationSingle;

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
import ifeed.mining.moea.operators.AbstractLogicOperator;
import ifeed.mining.moea.operators.AbstractLogicOperatorWithLocalSearch;
import ifeed.problem.assigning.Params;
import ifeed.problem.assigning.filters.EmptyOrbit;
import ifeed.problem.assigning.filters.InOrbit;
import ifeed.problem.assigning.filters.NotInOrbit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NotInOrbit2EmptyOrbitWithException extends AbstractLogicOperatorWithLocalSearch {

    private AbstractFeatureFetcher featureFetcher;
    private FeatureExpressionHandler featureHandler;

    public NotInOrbit2EmptyOrbitWithException(BaseParams params, MOEABase base, AbstractLocalSearch localSearch) {
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

        NotInOrbit constraintSetter = (NotInOrbit) constraintSetterAbstract;
        int orbit = constraintSetter.getOrbit();

        // Remove NotInOrbit node
        Literal constraintSetterLiteral = nodes.get(constraintSetter);
        parent.removeLiteral(constraintSetterLiteral);

        AbstractFilter emptyOrbitFilter = new EmptyOrbit(params, orbit);
        Feature newFeature = this.base.getFeatureFetcher().fetch(emptyOrbitFilter);
        Literal newLiteral = new Literal(newFeature.getName(), newFeature.getMatches());
        parent.addLiteral(newLiteral);

        if(super.localSearch == null){
            throw new IllegalStateException("Local search needs to be defined to use this operator");
        }

        AbstractLocalSearch localSearch = super.localSearch;
        ConnectiveTester tester = new ConnectiveTester(root);
        localSearch.setRoot(tester);

        ConnectiveTester targetParentNodeTester = null;
        Literal targetLiteral = null;

        for(Connective testerNode: tester.getDescendantConnectives(true)){
            if(this.featureHandler.featureTreeEquals(parent, testerNode)){
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

        // Add the exception under OR
        if(parent.getLogic() == LogicalConnectiveType.OR){
            targetParentNodeTester.setAddNewNode();
        }else{
            targetParentNodeTester.setAddNewNode(targetLiteral);
        }

        // The operation "notInOrbit -> emptyOrbit" improves precision, so look for exception that improves recall
        FeatureMetricComparator comparator = new FeatureMetricComparator(FeatureMetric.RCONFIDENCE);
        List<Feature> testFeatures = new ArrayList<>();
        for(int i = 0; i < params.getRightSetCardinality() + params.getRightSetGeneralizedConcepts().size() - 1; i++){
            InOrbit inOrbit = new InOrbit(params, orbit, i);
            testFeatures.add(this.featureFetcher.fetch(inOrbit));
        }

        Feature localSearchOutput = localSearch.runArgmax(testFeatures, comparator);
        if(localSearchOutput != null){

            // Add the exception under OR
            if(parent.getLogic() == LogicalConnectiveType.OR){
                parent.addLiteral(localSearchOutput.getName(), localSearchOutput.getMatches());

            }else{
                parent.removeNode(newLiteral);
                Connective tempBranch = new Connective(LogicalConnectiveType.OR);
                tempBranch.addNode(newLiteral);
                tempBranch.addLiteral(localSearchOutput.getName(), localSearchOutput.getMatches());
                parent.addBranch(tempBranch);
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
