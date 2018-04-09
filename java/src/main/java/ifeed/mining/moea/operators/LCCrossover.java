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

import java.util.ArrayList;
import java.util.List;

/**
 * Looseness-Controlled Crossover (LCC) operator introduced in:
 * Zhang, M., Gao, X. and Lou, W., 2007. A new crossover operator in genetic programming for object classification.
 * IEEE Transactions on Systems, Man, and Cybernetics, Part B (Cybernetics), 37(5), pp.1332-1343.
 */

public class LCCrossover extends AbstractHillClimbingCrossover implements Variation{

    public LCCrossover(double probability, MOEABase base, int maxIter){
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

        List<Formula> nodes1 = tree1.getRoot().getDescendantNodes(true);
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

        List<Formula> nodes2 = tree2.getRoot().getDescendantNodes(true);
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

            Formula candidateNode1 = max_weight_nodes1.get(PRNG.nextInt(max_weight_nodes1.size()));
            Formula candidateNode2 = max_weight_nodes2.get(PRNG.nextInt(max_weight_nodes2.size()));

            Formula subtree1 = base.getFeatureSelector().findEquivalentNode(base.getFeatureHandler(), root1, candidateNode1);
            Formula subtree2 = base.getFeatureSelector().findEquivalentNode(base.getFeatureHandler(), root2, candidateNode2);

            super.swapBranches(root1, root2, subtree1, subtree2);

            FeatureTreeVariable newTree1 = new FeatureTreeVariable(root1, this.base);
            FeatureTreeVariable newTree2 = new FeatureTreeVariable(root2, this.base);

            Solution sol1 = new FeatureTreeSolution(newTree1, MOEAParams.numberOfObjectives);
            Solution sol2 = new FeatureTreeSolution(newTree2, MOEAParams.numberOfObjectives);

            offsprings[0] = sol1;
            offsprings[1] = sol2;

            if(super.nonDominated(parents, offsprings)){
                // At least one of the offsprings is not dominated by the parents
                // Increase the "looseness" of all nodes except for the nodes that were swapped

                for(Formula node: root1.getDescendantNodes(true)){
                    if(node != subtree2){
                        node.addWeight();
                    }
                }

                for(Formula node: root2.getDescendantNodes(true)){
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
