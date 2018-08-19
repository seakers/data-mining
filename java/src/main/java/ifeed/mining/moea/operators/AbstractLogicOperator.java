package ifeed.mining.moea.operators;

import ifeed.local.params.BaseParams;
import ifeed.local.params.MOEAParams;
import ifeed.mining.moea.FeatureTreeSolution;
import org.moeaframework.core.Solution;
import aos.operator.AbstractCheckParent;
import ifeed.filter.AbstractFilter;
import ifeed.filter.AbstractFilterFinder;
import ifeed.filter.AbstractFilterFetcher;
import ifeed.mining.moea.MOEABase;
import ifeed.mining.moea.FeatureTreeVariable;
import ifeed.feature.logic.Connective;
import ifeed.feature.logic.Literal;
import ifeed.feature.logic.LogicalConnectiveType;
import java.util.*;

public abstract class AbstractLogicOperator extends AbstractCheckParent{

    protected BaseParams params;
    protected MOEABase base;
    protected AbstractFilterFetcher fetcher;
    protected static LogicalConnectiveType logic;
    protected Random random;

    public AbstractLogicOperator(BaseParams params, MOEABase base){
        this.params = params;
        this.base = base;
        this.fetcher = base.getFeatureFetcher().getFilterFetcher();
        this.logic = null;
        this.random = new Random();
    }

    public AbstractLogicOperator(BaseParams params, MOEABase base, LogicalConnectiveType targetLogic){
        this.params = params;
        this.base = base;
        this.fetcher = base.getFeatureFetcher().getFilterFetcher();
        this.logic = targetLogic;
        this.random = new Random();
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
     * @param parent Logical connective node that contains the applicable nodes.
     * @param constraintSetter
     * @param matchingFilters
     * @param nodes
     */
    protected abstract void apply(Connective root,
                                  Connective parent,
                                  AbstractFilter constraintSetter,
                                  Set<AbstractFilter> matchingFilters,
                                  Map<AbstractFilter, Literal> nodes);

    public abstract void findApplicableNodesUnderGivenParentNode(Connective root,
                                                                 Map<AbstractFilter, Set<AbstractFilter>> applicableFiltersMap,
                                                                 Map<AbstractFilter, Literal> applicableLiteralsMap);

    /**
     *
     * @param parent The node whose child nodes will be tested for applicability
     * @param applicableFiltersMap Mapping from the constraint setter filter to matching filters
     * @param applicableLiteralsMap Mapping from applicable filters to their corresponding literals
     * @param finder
     */
    protected void findApplicableNodesUnderGivenParentNode(
            Connective parent,
            Map<AbstractFilter, Set<AbstractFilter>> applicableFiltersMap,
            Map<AbstractFilter, Literal> applicableLiteralsMap,
            AbstractFilterFinder finder
    ){

        // Find all literals that satisfy certain conditions
        // All Literals and their corresponding Filters are not returned, but the lists are filled up as side effects
        if(!applicableFiltersMap.isEmpty() || !applicableLiteralsMap.isEmpty()){
            throw new IllegalStateException("Input argument lists should be empty. These lists are to be filled automatically, " +
                    "as side effects instead of being returned explicitly.");
        }

        // Create empty sets
        Set<AbstractFilter> allConstraintSetterFilters = new HashSet<>();
        Set<AbstractFilter> allMatchingFilters = new HashSet<>();
        Map<AbstractFilter, Literal> filter2LiteralMap = new HashMap<>();

        // For each child nodes, check if it can be used as a constraint setter or matching node
        for(Literal node: parent.getLiteralChildren()){

            // Get the corresponding filter
            AbstractFilter thisFilter = this.fetcher.fetch(node.getName());
            filter2LiteralMap.put(thisFilter, node);

            // Find all filters that can be used as constraint setters
            if(finder.isConstraintSetterType(thisFilter.getClass())){
                allConstraintSetterFilters.add(thisFilter);
            }

            // Find all filters that can be matched
            if(finder.isMatchingType(thisFilter.getClass())){
                allMatchingFilters.add(thisFilter);
            }
        }

        // For each constraint setter
        for(AbstractFilter constraintSetter: allConstraintSetterFilters){
            finder.setConstraints(constraintSetter);

            Set<AbstractFilter> matchedFilters = new HashSet<>();
            Map<AbstractFilter, Literal> tempMap = new HashMap<>();

            if(finder.hasMatchingClass()){
                for(AbstractFilter testFilter: allMatchingFilters){

                    if(constraintSetter.equals(testFilter)){
                        continue;

                    } else if(finder.check(testFilter)){
                        matchedFilters.add(testFilter);
                        tempMap.put(testFilter, filter2LiteralMap.get(testFilter));
                    }
                }

                if(matchedFilters.isEmpty()){
                    continue;
                }

            }else{
                if(!finder.check()){
                    continue;
                }
            }

            if(finder.allConditionsSatisfied()){
                applicableFiltersMap.put(constraintSetter, matchedFilters);
                applicableLiteralsMap.putAll(tempMap);
                applicableLiteralsMap.put(constraintSetter, filter2LiteralMap.get(constraintSetter));
            }

            finder.clearConstraints();
        }
    }

    /**
     * Returns the node whose child literals satisfy the condition needed to apply the current operator (uses depth-first search)
     * @param root
     * @param targetLogic LogicalConnectiveType.OR or LogicalConnectiveType.AND
     * @return
     */
    protected Connective getParentNodeOfApplicableNodes(Connective root, LogicalConnectiveType targetLogic){

        boolean checkThisNode = false;
        if(targetLogic == null){ // Target logic is not given
            checkThisNode = true;

        }else if(root.getLogic() == targetLogic){ // Target logic matches the current logical connective type
            checkThisNode = true;
        }

        if(checkThisNode){
            Map<AbstractFilter, Literal> applicableLiterals = new HashMap<>();

            // Check if there exist applicable nodes. When applicable nodes are found, nodes and filters are filled in as side effects
            this.findApplicableNodesUnderGivenParentNode(root, new HashMap<>(), applicableLiterals);

            if(!applicableLiterals.isEmpty()){
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
    public Solution[] evolveParents(Solution[] parents){

        Solution[] offsprings = new Solution[parents.length];

        int i = 0;
        for(Solution sol:parents){

            FeatureTreeVariable tree = (FeatureTreeVariable) sol.getVariable(0);

            Connective root = tree.getRoot().copy();

            // Find the parent node
            Connective parent = this.getParentNodeOfApplicableNodes(root, this.logic);

            Map<AbstractFilter, Set<AbstractFilter>> applicableFiltersMap = new HashMap<>();
            Map<AbstractFilter, Literal> applicableLiteralsMap = new HashMap<>();

            if(parent == null){
                offsprings[i] = sol;
                i++;
                continue;
            }

            System.out.println(this.getClass().getSimpleName() + " applied to: " + root.getName());

            // Find the applicable nodes under the parent node found
            this.findApplicableNodesUnderGivenParentNode(parent, applicableFiltersMap, applicableLiteralsMap);

            // Randomly select one constraint setter node
            List<AbstractFilter> constraintSetters = new ArrayList<>(applicableFiltersMap.keySet());
            Collections.shuffle(constraintSetters);
            AbstractFilter constraintSetter = constraintSetters.get(0);
            Set<AbstractFilter> matchingNodes = applicableFiltersMap.get(constraintSetter);

            // Modify the nodes using the given argument
            this.apply(root, parent, constraintSetter, matchingNodes, applicableLiteralsMap);

            // Re-package the tree in a Solution
            FeatureTreeVariable newTree = new FeatureTreeVariable(this.base, root);
            offsprings[i] = new FeatureTreeSolution(newTree, MOEAParams.numberOfObjectives);
            i++;
        }

        return offsprings;
    }

    @Override
    public int getArity(){
        return 1;
    }
}
