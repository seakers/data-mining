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

        Formula subtree1;
        Formula subtree2;

        do {
            subtree1 = super.base.getFeatureSelector().selectRandomNode(root1, null);
        } while (subtree1.getParent() == null);

        do {
            subtree2 = super.base.getFeatureSelector().selectRandomNode(root2, null);
        } while (subtree2.getParent() == null);

        // Swap two branches
        this.swapBranches(subtree1, subtree2);

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
     * @param subtree1 branch of the first feature tree
     * @param subtree2 branch of the second feature tree
     */
    public void swapBranches(Formula subtree1, Formula subtree2){

        Connective parent1 = (Connective) subtree1.getParent();
        Connective parent2 = (Connective) subtree2.getParent();

        // Swap the subtrees
        parent1.removeNode(subtree1);
        parent1.addNode(subtree2);
        parent2.removeNode(subtree2);
        parent2.addNode(subtree1);
    }
}
