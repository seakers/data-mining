package ifeed.mining.moea.operators;

import ifeed.feature.Feature;
import ifeed.feature.FeatureMetric;
import ifeed.feature.FeatureMetricComparator;
import ifeed.feature.logic.Connective;
import ifeed.feature.logic.ConnectiveTester;
import ifeed.feature.logic.Literal;
import ifeed.feature.logic.LogicalConnectiveType;
import ifeed.local.params.BaseParams;
import ifeed.mining.AbstractLocalSearch;
import ifeed.mining.moea.GPMOEABase;

import java.util.List;

public abstract class AbstractLogicOperatorWithLocalSearch extends AbstractLogicOperator{

    protected AbstractLocalSearch localSearch;

    public AbstractLogicOperatorWithLocalSearch(BaseParams params, GPMOEABase base, AbstractLocalSearch localSearch){
        super(params, base);
        this.localSearch = localSearch;
    }

    public AbstractLogicOperatorWithLocalSearch(BaseParams params, GPMOEABase base, AbstractLocalSearch localSearch, LogicalConnectiveType targetLogic){
        super(params, base, targetLogic);
        this.localSearch = localSearch;
    }

    /**
     * Adds an extra condition or exception using the local search
     * @param root
     * @param parent
     * @param literalToBeCombined
     * @param baseFeaturesToTest
     * @param maxNumConditions
     * @param metric
     */
    public void addExtraCondition(Connective root, Connective parent, Literal literalToBeCombined, List<Feature> baseFeaturesToTest, int maxNumConditions, FeatureMetric metric){

        if(this.localSearch == null){
            throw new IllegalStateException("Local search needs to be defined to use this operator");
        }

        // Create tester
        ConnectiveTester tester = new ConnectiveTester(root);
        localSearch.setRoot(tester);

        // Find the parent node within the tester tree
        ConnectiveTester parentNodeTester = null;
        for(Connective node: tester.getDescendantConnectives(true)){
            if(this.localSearch.getFeatureHandler().featureTreeEquals(parent, node)){
                parentNodeTester = (ConnectiveTester) node;
            }
        }

        boolean combineLiteral = false;
        Literal testerLiteralToBeCombined = null;
        if(literalToBeCombined != null){
            combineLiteral = true;
            for(Literal literal: parentNodeTester.getLiteralChildren()){
                if(literal.hashCode() == literalToBeCombined.hashCode()){
                    testerLiteralToBeCombined = literal;
                }
            }
        }

        // Define the comparator
        FeatureMetricComparator comparator = new FeatureMetricComparator(metric);

        for(int i = 0; i < maxNumConditions; i++){
            if(combineLiteral){
                parentNodeTester.setAddNewNode(testerLiteralToBeCombined);
            }else{
                parentNodeTester.setAddNewNode();
            }
            Feature localSearchOutput = localSearch.runArgmax(baseFeaturesToTest, comparator);

            if(localSearchOutput == null){
                break;

            }else{
                parentNodeTester.setNewNode(localSearchOutput.getName(), localSearchOutput.getMatches());
                parentNodeTester.finalizeNewNodeAddition();

                if(combineLiteral){
                    // Remove the target node from its parent
                    parent.removeNode(literalToBeCombined);

                    Connective tempBranch;
                    if(parent.getLogic() == LogicalConnectiveType.OR){
                        tempBranch = new Connective(LogicalConnectiveType.AND);
                    }else{
                        tempBranch = new Connective(LogicalConnectiveType.OR);
                    }
                    tempBranch.addNode(literalToBeCombined);
                    tempBranch.addLiteral(localSearchOutput.getName(), localSearchOutput.getMatches());
                    parent.addBranch(tempBranch);

                    // Change parent
                    parent = tempBranch;
                    boolean updatedParent = false;
                    for(Connective branch: parentNodeTester.getConnectiveChildren()){
                        if(branch.getLiteralChildren().size() == 2 && branch.getConnectiveChildren().isEmpty()){
                            for(Literal literal: branch.getLiteralChildren()){
                                if(literal.hashCode() == literalToBeCombined.hashCode()){
                                    parentNodeTester = (ConnectiveTester) branch;
                                    updatedParent = true;
                                    break;
                                }
                            }
                        }
                    }
                    if(!updatedParent){
                        throw new IllegalStateException("Error: parent not updated");
                    }
                    combineLiteral = false;

                }else{
                    parent.addLiteral(localSearchOutput.getName(), localSearchOutput.getMatches());

                }
            }
        }
    }
}
