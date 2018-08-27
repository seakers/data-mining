package ifeed.mining.moea.operators.gptype;

import ifeed.feature.logic.Literal;
import ifeed.feature.logic.LogicalConnectiveType;
import ifeed.local.params.MOEAParams;
import ifeed.mining.moea.FeatureTreeSolution;
import ifeed.mining.moea.FeatureTreeVariable;
import ifeed.mining.moea.MOEABase;
import ifeed.feature.logic.Connective;
import ifeed.feature.logic.Formula;
import ifeed.mining.moea.operators.AbstractFeatureCrossover;
import org.moeaframework.core.PRNG;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;

public class BranchSwapCrossover extends AbstractFeatureCrossover implements Variation{

    public BranchSwapCrossover(double probability, MOEABase base){
        super(probability, base);
    }

    @Override
    public Solution[] evolve(Solution[] parents){

        if( PRNG.nextDouble() > super.probability){
            return parents;
        }

        Solution[] out = new Solution[2];

        FeatureTreeVariable tree1 = (FeatureTreeVariable) parents[0].getVariable(0);
        FeatureTreeVariable tree2 = (FeatureTreeVariable) parents[1].getVariable(0);

        // Copy the root nodes
        Connective root1 = tree1.getRoot().copy();
        Connective root2 = tree2.getRoot().copy();

        Formula subtree1 = super.base.getFeatureSelector().selectRandomNode(root1, null);
        Formula subtree2 = super.base.getFeatureSelector().selectRandomNode(root2, null);

        // Swap two branches
        this.swapBranches(root1, root2, subtree1, subtree2);

        base.getFeatureHandler().repairFeatureTreeStructure(root1);
        base.getFeatureHandler().repairFeatureTreeStructure(root2);

        FeatureTreeVariable newTree1 = new FeatureTreeVariable(this.base, root1);
        FeatureTreeVariable newTree2 = new FeatureTreeVariable(this.base, root2);

        Solution sol1 = new FeatureTreeSolution(newTree1, MOEAParams.numberOfObjectives);
        Solution sol2 = new FeatureTreeSolution(newTree2, MOEAParams.numberOfObjectives);

        out[0] = sol1;
        out[1] = sol2;
        return out;
    }


    /**
     * Swaps the branches of two feature trees
     * @param root1 root of the first feature tree
     * @param root2 root of the second feature tree
     * @param subtree1 branch of the first feature tree
     * @param subtree2 branch of the second feature tree
     */
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
            parent1.addBranch( (Connective) subtree1);
        }

        if(parent2 == null){// subtree2 is root2
            LogicalConnectiveType temp;
            if(root2.getLogic() == LogicalConnectiveType.AND){
                temp = LogicalConnectiveType.OR;
            }else{
                temp = LogicalConnectiveType.AND;
            }
            parent2 = new Connective(temp);
            parent2.addBranch( (Connective) subtree2);
        }

        // Swap the subtrees
        parent1.removeNode(subtree1);
        parent1.addNode(subtree2);
        parent2.removeNode(subtree2);
        parent2.addNode(subtree1);
    }
}
