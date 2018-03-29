package ifeed.mining.moea.operators;

import ifeed.local.MOEAParams;
import ifeed.mining.moea.FeatureTreeSolution;
import ifeed.mining.moea.FeatureTreeVariable;
import ifeed.mining.moea.MOEABase;
import ifeed.feature.logic.LogicOperator;
import ifeed.feature.logic.Connective;
import ifeed.feature.logic.Literal;
import ifeed.feature.logic.Formula;
import org.moeaframework.core.PRNG;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;

public class FeatureCrossOver implements Variation{

    private double probability;
    MOEABase base;

    public FeatureCrossOver(double probability, MOEABase base){
        this.probability = probability;
        this.base = base;
    }

    @Override
    public Solution[] evolve(Solution[] parents){

        if( PRNG.nextDouble() > this.probability){
            return parents;
        }

        Solution[] out = new Solution[2];

        FeatureTreeVariable tree1 = (FeatureTreeVariable) parents[0].getVariable(0);
        FeatureTreeVariable tree2 = (FeatureTreeVariable) parents[1].getVariable(0);

//        Solution sol1 = new FeatureTreeSolution((FeatureTreeVariable) tree1.copy(), MOEAParams.numberOfObjectives);
//        Solution sol2 = new FeatureTreeSolution((FeatureTreeVariable) tree2.copy(), MOEAParams.numberOfObjectives);

        // Copy the root nodes
        Connective root1 = tree1.getRoot().copy();
        Connective root2 = tree2.getRoot().copy();

        Formula subtree1 = base.getFeatureSelector().selectRandomNode(root1, null);
        Formula subtree2 = base.getFeatureSelector().selectRandomNode(root2, null);

        Connective parent1 = base.getFeatureSelector().findParentNode(root1, subtree1);
        Connective parent2 = base.getFeatureSelector().findParentNode(root2, subtree2);

        if(parent1 == null){ // Subtree1 is root1
//            System.out.println(root1.getName());
//            System.out.println(subtree1.getName());
            LogicOperator temp;
            if(root1.getLogic() == LogicOperator.AND){
                temp = LogicOperator.OR;
            }else{
                temp = LogicOperator.AND;
            }
            parent1 = new Connective(temp);
            parent1.addChild( (Connective) subtree1);
        }

        if(parent2 == null){
            LogicOperator temp;
            if(root2.getLogic() == LogicOperator.AND){
                temp = LogicOperator.OR;
            }else{
                temp = LogicOperator.AND;
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

        FeatureTreeVariable newTree1 = new FeatureTreeVariable(root1, this.base);
        FeatureTreeVariable newTree2 = new FeatureTreeVariable(root2, this.base);

        Solution sol1 = new FeatureTreeSolution(newTree1, MOEAParams.numberOfObjectives);
        Solution sol2 = new FeatureTreeSolution(newTree2, MOEAParams.numberOfObjectives);

        out[0] = sol1;
        out[1] = sol2;
        return out;
    }

    @Override
    public int getArity(){
        return 2;
    }
}
