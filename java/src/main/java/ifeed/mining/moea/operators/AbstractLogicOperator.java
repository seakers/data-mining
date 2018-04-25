package ifeed.mining.moea.operators;

import aos.operator.CheckParents;
import ifeed.filter.FilterConstraint;
import ifeed.filter.FilterFetcher;
import ifeed.mining.moea.MOEABase;
import org.moeaframework.core.PRNG;
import org.moeaframework.core.Solution;

import ifeed.mining.moea.FeatureTreeVariable;
import ifeed.mining.moea.FeatureTreeSolution;
import ifeed.local.MOEAParams;

import ifeed.filter.Filter;
import ifeed.feature.logic.Connective;
import ifeed.feature.logic.Literal;
import ifeed.feature.logic.LogicalConnectiveType;
import org.moeaframework.core.Variation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;

public abstract class AbstractLogicOperator implements CheckParents, Variation{

    protected MOEABase base;
    protected FilterFetcher fetcher;
    protected static LogicalConnectiveType logic;

    public AbstractLogicOperator(MOEABase base, LogicalConnectiveType targetLogic){
        this.base = base;
        this.fetcher = base.getFeatureFetcher().getFilterFetcher();
        this.logic = targetLogic;
    }

    @Override
    public boolean check(Solution[] parents) {
        for(Solution sol: parents){
            FeatureTreeVariable tree = (FeatureTreeVariable) sol.getVariable(0);
            Connective root = tree.getRoot();
            if(this.checkApplicability(root)){
                return true;
            }
        }
        return false;
    }

    public boolean checkApplicability(Connective root){
        return this.getParentNodeOfApplicableNodes(root, this.logic) != null;
    }

    @Override
    public Solution[] evolve(Solution[] parents){

        FeatureTreeVariable tree = (FeatureTreeVariable) parents[0].getVariable(0);

        Connective root = tree.getRoot().copy();
        Connective parent = this.getParentNodeOfApplicableNodes(root, this.logic);

        List<Literal> nodes = new ArrayList<>();
        List<Filter> filters = new ArrayList<>();

        this.findApplicableNodesUnderGivenParentNode(parent, nodes, filters);

        HashMap<Integer, HashSet<Integer>> arg2LiteralIndices = mapArgumentTypes2LiteralIndices(nodes, filters);

        int selectedArgument = randomlySelectArgument(arg2LiteralIndices);

        HashSet<Integer> applicableNodeIndices = arg2LiteralIndices.get(selectedArgument);

        this.apply(root, parent, selectedArgument, nodes, filters, applicableNodeIndices);

        FeatureTreeVariable newTree = new FeatureTreeVariable(root, this.base);
        Solution sol = new FeatureTreeSolution(newTree, MOEAParams.numberOfObjectives);

        return new Solution[]{sol};
    }

    protected abstract void apply(Connective root, Connective parent, int selectedArg, List<Literal> nodes, List<Filter> filters, HashSet<Integer> nodeIndices);

    /**
     * Randomly selects an argument from a list of arguments that satisfy the given constraint
     * @param arg2LiteralIndices
     * @return
     */
    protected int randomlySelectArgument(HashMap<Integer, HashSet<Integer>> arg2LiteralIndices){

        int randInd = PRNG.nextInt(arg2LiteralIndices.keySet().size());
        int selectedInd = 0;

        int i = 0;
        for(int key: arg2LiteralIndices.keySet()){
            if (i == randInd){
                selectedInd = key;
                break;
            }
            i++;
        }

        return selectedInd;
    }

    protected abstract HashMap<Integer, HashSet<Integer>> mapArgumentTypes2LiteralIndices(List<Literal> nodes, List<Filter> filters);

    public abstract void findApplicableNodesUnderGivenParentNode(Connective root, List<Literal> nodes, List<Filter> filters);

    protected void findApplicableNodesUnderGivenParentNode(

            Connective parent,
            List<Literal> applicableLiterals, List<Filter> applicableFilters,
            FilterConstraint constraints

            ){

        // Find all literals that satisfy certain conditions
        // All Literals and their corresponding Filters are not returned, but the lists are filled up as side effects

        if(!applicableLiterals.isEmpty() || !applicableFilters.isEmpty()){
            throw new IllegalStateException("Input argument lists should be empty. These lists are to be filled automatically, as side effects instead of returning.");
        }

        List<Literal> allTargetLiterals = new ArrayList<>();
        List<Filter> allTargetFilters = new ArrayList<>();

        // Iterate over literals in the current node
        for(Literal node: parent.getLiteralChildren()){
            String[] nameAndArgs = this.fetcher.getNameAndArgs(node.getName());

            String className = constraints.getConstraintSetterClassName();
            String[] classNameSplit = className.split("\\.");
            String classNameShort = classNameSplit[classNameSplit.length-1];

            // Check for the target feature type
            if(nameAndArgs[0].equalsIgnoreCase(classNameShort)){

                // Current node is used as the constraint setter
                Filter thisFilter = this.fetcher.fetch(node.getName());

                // Set constraints
                constraints.setConstraints(thisFilter);

                // Test all other features against the constraints
                for(Filter otherFilter: allTargetFilters){
                    if(constraints.check(otherFilter)){

                        // Add the current literal and filter
                        if(!applicableFilters.contains(thisFilter)){
                            applicableLiterals.add(node);
                            applicableFilters.add(thisFilter);

                        }

                        // Add the other literal and filter
                        if(!applicableFilters.contains(otherFilter)){
                            int index = allTargetFilters.indexOf(otherFilter);
                            applicableLiterals.add(allTargetLiterals.get(index));
                            applicableFilters.add(otherFilter);
                        }
                    }
                }

                // Add all nodes into a list
                allTargetLiterals.add(node);
                allTargetFilters.add(thisFilter);
            }
        }
    }

    protected Connective getParentNodeOfApplicableNodes(Connective root, LogicalConnectiveType targetLogic){
        // Return the node whose child literals satisfy the condition needed to apply the current operator (uses depth-first search)

        // Inspect the literals only if the logical connective matches the target
        if(root.getLogic() == targetLogic){

            List<Literal> nodes = new ArrayList<>();
            List<Filter> filters = new ArrayList<>();

            // Check if there exist applicable nodes. When applicable nodes are found, nodes and filters are filled in as side effects
            this.findApplicableNodesUnderGivenParentNode(root, nodes, filters);

            if(!nodes.isEmpty()){
                // Applicable nodes are found under the current node
                return root;
            }
        }

        for(Connective branch: root.getConnectiveChildren()){
            Connective temp = this.getParentNodeOfApplicableNodes(branch, targetLogic);
            if(temp != null){
                // Applicable node is found in one of the child branches
                return temp;
            }
        }

        return null;
    }

    @Override
    public int getArity(){
        return 1;
    }
}
