package ifeed.mining.moea.operators;

import ifeed.local.MOEAParams;
import ifeed.mining.moea.FeatureTreeSolution;
import ifeed.mining.moea.FeatureTreeVariable;
import ifeed.mining.moea.MOEABase;
import ifeed.feature.logic.Connective;
import ifeed.feature.logic.Formula;
import org.moeaframework.core.PRNG;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;

public class FeatureCrossover extends AbstractFeatureCrossover implements Variation{

    public FeatureCrossover(double probability, MOEABase base){
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

//        Solution sol1 = new FeatureTreeSolution((FeatureTreeVariable) tree1.copy(), MOEAParams.numberOfObjectives);
//        Solution sol2 = new FeatureTreeSolution((FeatureTreeVariable) tree2.copy(), MOEAParams.numberOfObjectives);

        // Copy the root nodes
        Connective root1 = tree1.getRoot().copy();
        Connective root2 = tree2.getRoot().copy();

        Formula subtree1 = super.base.getFeatureSelector().selectRandomNode(root1, null);
        Formula subtree2 = super.base.getFeatureSelector().selectRandomNode(root2, null);

        // Swap two branches
        super.swapBranches(root1, root2, subtree1, subtree2);

        base.getFeatureHandler().repairFeatureTreeStructure(root1);
        base.getFeatureHandler().repairFeatureTreeStructure(root2);

        FeatureTreeVariable newTree1 = new FeatureTreeVariable(root1, this.base);
        FeatureTreeVariable newTree2 = new FeatureTreeVariable(root2, this.base);

        Solution sol1 = new FeatureTreeSolution(newTree1, MOEAParams.numberOfObjectives);
        Solution sol2 = new FeatureTreeSolution(newTree2, MOEAParams.numberOfObjectives);

        out[0] = sol1;
        out[1] = sol2;
        return out;
    }
}
