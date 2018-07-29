package ifeed.mining.moea.operators;

import ifeed.feature.logic.Connective;
import ifeed.feature.logic.Literal;
import ifeed.feature.logic.LogicalConnectiveType;
import ifeed.filter.AbstractFilter;
import ifeed.local.params.MOEAParams;
import ifeed.mining.moea.FeatureTreeSolution;
import ifeed.mining.moea.FeatureTreeVariable;
import ifeed.mining.moea.MOEABase;
import org.moeaframework.core.Solution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public abstract class AbstractSimplificationOperator extends AbstractLogicOperator{

    public AbstractSimplificationOperator(MOEABase base, LogicalConnectiveType targetLogic){
        super(base, targetLogic);
    }

    @Override
    public Solution[] evolveParents(Solution[] parents){

        Solution[] offsprings = new Solution[parents.length];

        int i = 0;
        for(Solution sol:parents){

            FeatureTreeVariable tree = (FeatureTreeVariable) sol.getVariable(0);

            Connective root = tree.getRoot().copy();

            // Find the parent node
            Connective parent = this.getParentNodeOfApplicableNodes(root, this.logic);

            List<Literal> nodes = new ArrayList<>();
            List<AbstractFilter> filters = new ArrayList<>();

            if(parent == null){
                offsprings[i] = sol;
                i++;
                continue;
            }

            System.out.println(this.getClass().getSimpleName() + " applied to: " + root.getName());

            // Find the applicable nodes under the parent node found
            this.findApplicableNodesUnderGivenParentNode(parent, nodes, filters);

            // Get mapping from the arguments to the indices of the literals that are applicable
            HashMap<int[], HashSet<Integer>> arg2LiteralIndices = mapArguments2LiteralIndices(nodes, filters);

            // Selects a single argument to be used to group literals
            int[] selectedArguments = randomlySelectArgument(arg2LiteralIndices);

            // Get the nodes to be modified
            HashSet<Integer> applicableNodeIndices = arg2LiteralIndices.get(selectedArguments);

            // Modify the nodes using the given argument
            this.apply(root, parent, nodes, filters, selectedArguments, applicableNodeIndices);

            // Re-package the tree in a Solution
            FeatureTreeVariable newTree = new FeatureTreeVariable(root, this.base);

            offsprings[i] = new FeatureTreeSolution(newTree, MOEAParams.numberOfObjectives);
            i++;
        }

        return offsprings;
    }
}
