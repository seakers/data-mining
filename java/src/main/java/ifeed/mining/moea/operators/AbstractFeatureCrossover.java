package ifeed.mining.moea.operators;

import ifeed.mining.moea.MOEABase;
import ifeed.feature.logic.LogicalConnectiveType;
import ifeed.feature.logic.Connective;
import ifeed.feature.logic.Literal;
import ifeed.feature.logic.Formula;
import org.moeaframework.core.Variation;

public abstract class AbstractFeatureCrossover implements Variation{

    protected double probability;
    protected MOEABase base;

    public AbstractFeatureCrossover(double probability, MOEABase base){
        this.probability = probability;
        this.base = base;
    }

    public void swapBranches(Connective root1, Connective root2, Formula subtree1, Formula subtree2){

        Connective parent1 = base.getFeatureSelector().findParentNode(root1, subtree1);
        Connective parent2 = base.getFeatureSelector().findParentNode(root2, subtree2);

        if(parent1 == null){ // subtree1 is root1
            LogicalConnectiveType temp;
            if(root1.getLogic() == LogicalConnectiveType.AND){
                temp = LogicalConnectiveType.OR;
            }else{
                temp = LogicalConnectiveType.AND;
            }
            parent1 = new Connective(temp);
            parent1.addChild( (Connective) subtree1);
        }

        if(parent2 == null){// subtree2 is root2
            LogicalConnectiveType temp;
            if(root2.getLogic() == LogicalConnectiveType.AND){
                temp = LogicalConnectiveType.OR;
            }else{
                temp = LogicalConnectiveType.AND;
            }
            parent2 = new Connective(temp);
            parent2.addChild( (Connective) subtree2);
        }

        // Swap the subtrees
        if(subtree1 instanceof Connective){
            Connective thisNode = (Connective) subtree1;
            parent1.getConnectiveChildren().remove(thisNode);
            parent2.addChild(thisNode);
        }else{
            Literal thisNode = (Literal) subtree1;
            parent1.getLiteralChildren().remove(thisNode);
            parent2.addLiteral(thisNode);
        }

        // Swap the subtrees
        if(subtree2 instanceof Connective){
            Connective thisNode = (Connective) subtree2;
            parent2.getConnectiveChildren().remove(thisNode);
            parent1.addChild(thisNode);
        }else{
            Literal thisNode = (Literal) subtree2;
            parent2.getLiteralChildren().remove(thisNode);
            parent1.addLiteral(thisNode);
        }
    }

    @Override
    public int getArity(){
        return 2;
    }
}
