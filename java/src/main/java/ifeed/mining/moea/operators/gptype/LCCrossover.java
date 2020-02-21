package ifeed.mining.moea.operators.GPType;

import ifeed.local.params.MOEAParams;
import ifeed.mining.moea.FeatureTreeSolution;
import ifeed.mining.moea.FeatureTreeVariable;
import ifeed.mining.moea.GPMOEABase;
import ifeed.feature.logic.Connective;
import ifeed.feature.logic.Formula;
import org.moeaframework.core.PRNG;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;

import java.util.ArrayList;
import java.util.List;

/**
 * Looseness-Controlled Crossover (LCC) operator introduced in:
 * Zhang, M., Gao, X. and Lou, W., 2007. A new crossover operator in genetic programming for object classification.
 * IEEE Transactions on Systems, Man, and Cybernetics, Part B (Cybernetics), 37(5), pp.1332-1343.
 */

public class LCCrossover extends AbstractHillClimbingCrossover implements Variation{

    public LCCrossover(double probability, GPMOEABase base, int maxIter){
        super(probability, base, maxIter);
    }

    @Override
    public Solution[] evolve(Solution[] parents){

        if( PRNG.nextDouble() > this.probability){
            return parents;
        }

        Solution[] offsprings = new Solution[2];
        FeatureTreeVariable tree1 = (FeatureTreeVariable) parents[0].getVariable(0);
        FeatureTreeVariable tree2 = (FeatureTreeVariable) parents[1].getVariable(0);

        // Select nodes that have the maximum weights (looseness)
        List<Formula> nodes1 = tree1.getRoot().getDescendantNodes();
        List<Formula> max_weight_nodes1 = new ArrayList<>();
        int maxWeight = 0;
        for(Formula node:nodes1){
            if(node.checkWeightType(Formula.WeightType.LOOSENESS)){
                int thisWeight = node.getWeight();
                if(thisWeight > maxWeight){
                    max_weight_nodes1 = new ArrayList<>();
                    max_weight_nodes1.add(node);
                    maxWeight = thisWeight;
                }else if(thisWeight == maxWeight){
                    max_weight_nodes1.add(node);
                }

            } else{
                throw new IllegalStateException("More than one type of weight used in feature trees");
            }
        }

        // Select nodes that have the maximum weights (looseness)
        List<Formula> nodes2 = tree2.getRoot().getDescendantNodes();
        List<Formula> max_weight_nodes2 = new ArrayList<>();
        maxWeight = 0;
        for(Formula node:nodes2){
            if(node.checkWeightType(Formula.WeightType.LOOSENESS)){
                int thisWeight = node.getWeight();
                if(thisWeight > maxWeight){
                    max_weight_nodes2 = new ArrayList<>();
                    max_weight_nodes2.add(node);
                    maxWeight = thisWeight;
                }else if(thisWeight == maxWeight){
                    max_weight_nodes2.add(node);
                }
            } else{
                throw new IllegalStateException("More than one type of weight used in feature trees");
            }
        }

        int i = 0;
        while( i < super.maxIter){

            // Copy the root nodes
            Connective root1 = tree1.getRoot().copy();
            Connective root2 = tree2.getRoot().copy();

            // Select nodes to be swapped from the parent trees
            Formula candidateNode1 = max_weight_nodes1.get(PRNG.nextInt(max_weight_nodes1.size()));
            Formula candidateNode2 = max_weight_nodes2.get(PRNG.nextInt(max_weight_nodes2.size()));

            // Get the copied nodes that are equivalent to the selected nodes
            Formula subtree1 = base.getFeatureHandler().findMatchingNodes(root1, candidateNode1).get(0);
            Formula subtree2 = base.getFeatureHandler().findMatchingNodes(root2, candidateNode2).get(0);

            // Swap branches
            super.swapBranches(subtree1, subtree2);

            base.getFeatureHandler().repairFeatureTreeStructure(root1);
            base.getFeatureHandler().repairFeatureTreeStructure(root2);

            // Define new variables
            FeatureTreeVariable newTree1 = new FeatureTreeVariable(this.base, root1);
            FeatureTreeVariable newTree2 = new FeatureTreeVariable(this.base, root2);
            Solution sol1 = new FeatureTreeSolution(newTree1, MOEAParams.numberOfObjectives);
            Solution sol2 = new FeatureTreeSolution(newTree2, MOEAParams.numberOfObjectives);

            offsprings[0] = sol1;
            offsprings[1] = sol2;

            if(super.nonDominated(parents, offsprings)){
                // At least one of the offsprings is not dominated by the parents
                // Increase the "looseness" of all nodes except for the nodes that were swapped

                for(Formula node: root1.getDescendantNodes()){
                    if(node != subtree2){
                        node.addWeight();
                    }
                }

                for(Formula node: root2.getDescendantNodes()){
                    if(node != subtree1){
                        node.addWeight();
                    }
                }

                break;
            }
            i++;
        }

        return offsprings;
    }
}
