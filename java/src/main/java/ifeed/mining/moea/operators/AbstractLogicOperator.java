package ifeed.mining.moea.operators;

import aos.operator.AbstractCheckParent;
import ifeed.filter.AbstractFilter;
import ifeed.filter.AbstractFilterConstraint;
import ifeed.filter.AbstractFilterFetcher;
import ifeed.mining.moea.MOEABase;
import org.moeaframework.core.Solution;

import ifeed.mining.moea.FeatureTreeVariable;

import ifeed.feature.logic.Connective;
import ifeed.feature.logic.Literal;
import ifeed.feature.logic.LogicalConnectiveType;

import java.util.*;

public abstract class AbstractLogicOperator extends AbstractCheckParent{

    protected MOEABase base;
    protected AbstractFilterFetcher fetcher;
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

    /**
     * Checks whether this operator can be applied to the given feature tree
     * @param root
     * @return
     */
    public boolean checkApplicability(Connective root){
        return this.getParentNodeOfApplicableNodes(root, this.logic) != null;
    }

    /**
     * Applies this operator to the given feature tree
     * @param root The input feature tree
     * @param parent Logical connective node that contains the applicable nodes
     * @param nodes The applicable nodes
     * @param filters The Filters corresponding to the applicable nodes
     * @param selectedArgs Numeric representation of the argument selected to be used for checking conditions
     * @param nodeIndices The indices of the applicable nodes under the parent node
     */
    protected abstract void apply(Connective root, Connective parent, List<Literal> nodes, List<AbstractFilter> filters, int[] selectedArgs, HashSet<Integer> nodeIndices);

    /**
     * Randomly selects an argument from a list of arguments that satisfy the given constraint
     * @param arg2LiteralIndices
     * @return
     */
    protected int[] randomlySelectArgument(HashMap<int[], HashSet<Integer>> arg2LiteralIndices){

        List<int[]> keySetList = new ArrayList<>(arg2LiteralIndices.keySet());
        Collections.shuffle(keySetList);

        return keySetList.get(0);
    }

    /**
     * Returns HashMap that maps different arguments to the indices of the nodes that contain those arguments
     * @param nodes
     * @param filters
     * @return
     */
    protected abstract HashMap<int[], HashSet<Integer>> mapArguments2LiteralIndices(List<Literal> nodes, List<AbstractFilter> filters);

    public abstract void findApplicableNodesUnderGivenParentNode(Connective root, List<Literal> nodes, List<AbstractFilter> filters);

    protected void findApplicableNodesUnderGivenParentNode(

            Connective parent,
            List<Literal> applicableLiterals, List<AbstractFilter> applicableFilters,
            AbstractFilterConstraint constraints

            ){

        // Find all literals that satisfy certain conditions
        // All Literals and their corresponding Filters are not returned, but the lists are filled up as side effects

        if(!applicableLiterals.isEmpty() || !applicableFilters.isEmpty()){
            throw new IllegalStateException("Input argument lists should be empty. These lists are to be filled automatically, as side effects instead of returning.");
        }

        // Create empty lists
        List<Literal> allTargetLiterals = new ArrayList<>();
        List<AbstractFilter> allTargetFilters = new ArrayList<>();

        // Iterate over literals in the current node
        for(Literal node: parent.getLiteralChildren()){

            String[] nameAndArgs = this.fetcher.getNameAndArgs(node.getName());
            String className = constraints.getConstraintSetterClassName();

            // Check for the target feature type
            if(nameAndArgs[0].equalsIgnoreCase(className)){

                // Current node is used as the constraint setter
                AbstractFilter thisFilter = this.fetcher.fetch(node.getName());

                // Set constraints
                constraints.setConstraints(thisFilter);

                // Test all other features against the constraints
                for(AbstractFilter otherFilter: allTargetFilters){
                    if(constraints.check(otherFilter) && !thisFilter.equals(otherFilter)){

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

    /**
     * Returns the node whose child literals satisfy the condition needed to apply the current operator (uses depth-first search)
     * @param root
     * @param targetLogic LogicalConnectiveType.OR or LogicalConnectiveType.AND
     * @return
     */
    protected Connective getParentNodeOfApplicableNodes(Connective root, LogicalConnectiveType targetLogic){

        // Inspect the children literals only if the current logical connective matches the target logic
        if(root.getLogic() == targetLogic){

            List<Literal> nodes = new ArrayList<>();
            List<AbstractFilter> filters = new ArrayList<>();

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
