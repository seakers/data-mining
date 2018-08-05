//package ifeed.mining.moea.operators;
//
//import aos.operator.AbstractCheckParent;
//import ifeed.filter.AbstractFilter;
//import ifeed.filter.AbstractFilterFinder;
//import ifeed.filter.AbstractFilterFetcher;
//import ifeed.mining.moea.MOEABase;
//import org.moeaframework.core.Solution;
//
//import ifeed.mining.moea.FeatureTreeVariable;
//
//import ifeed.feature.logic.Connective;
//import ifeed.feature.logic.Literal;
//import ifeed.feature.logic.LogicalConnectiveType;
//
//import java.util.*;
//
//public abstract class AbstractLogicOperator extends AbstractCheckParent{
//
//    protected MOEABase base;
//    protected AbstractFilterFetcher fetcher;
//    protected static LogicalConnectiveType logic;
//
//    public AbstractLogicOperator(MOEABase base, LogicalConnectiveType targetLogic){
//        this.base = base;
//        this.fetcher = base.getFeatureFetcher().getFilterFetcher();
//        this.logic = targetLogic;
//    }
//
//    @Override
//    public boolean check(Solution[] parents) {
//        for(Solution sol: parents){
//            FeatureTreeVariable tree = (FeatureTreeVariable) sol.getVariable(0);
//            Connective root = tree.getRoot();
//            if(this.checkApplicability(root)){
//                return true;
//            }
//        }
//
//        return false;
//    }
//
//    /**
//     * Checks whether this operator can be applied to the given feature tree
//     * @param root
//     * @return
//     */
//    public boolean checkApplicability(Connective root){
//        return this.getParentNodeOfApplicableNodes(root, this.logic) != null;
//    }
//
//    /**
//     * Applies this operator to the given feature tree
//     * @param root The input feature tree
//     * @param parent Logical connective node that contains the applicable nodes
//     * @param nodes The applicable nodes
//     * @param filters The Filters corresponding to the applicable nodes
//     * @param selectedArgs Numeric representation of the argument selected to be used for checking conditions
//     * @param nodeIndices The indices of the applicable nodes under the parent node
//     */
//    protected abstract void apply(Connective root, Connective parent, List<Literal> nodes, List<AbstractFilter> filters, int[] selectedArgs, HashSet<Integer> nodeIndices);
//
//    public abstract void findApplicableNodesUnderGivenParentNode(Connective root, Map<AbstractFilter, Set<AbstractFilter>> applicableFiltersMap, Map<AbstractFilter, Literal> applicableLiteralsMap);
//
//    protected void findApplicableNodesUnderGivenParentNode(
//
//            Connective parent,
//            Map<AbstractFilter, Set<AbstractFilter>> applicableFiltersMap,
//            Map<AbstractFilter, Literal> applicableLiterals,
//            AbstractFilterFinder finder
//
//            ){
//
//        // Find all literals that satisfy certain conditions
//        // All Literals and their corresponding Filters are not returned, but the lists are filled up as side effects
//        if(!applicableFiltersMap.isEmpty() || !applicableLiterals.isEmpty()){
//            throw new IllegalStateException("Input argument lists should be empty. These lists are to be filled automatically, " +
//                    "as side effects instead of being returned explicitly.");
//        }
//
//        applicableFiltersMap = new HashMap<>();
//        applicableLiterals = new HashMap<>();
//
//        // Create empty sets
//        Set<AbstractFilter> allConstraintSetterFilters = new HashSet<>();
//        Set<AbstractFilter> allMatchingFilters = new HashSet<>();
//        Map<AbstractFilter, Literal> filter2LiteralMap = new HashMap<>();
//
//        for(Literal node: parent.getLiteralChildren()){
//
//            AbstractFilter thisFilter = this.fetcher.fetch(node.getName());
//            filter2LiteralMap.put(thisFilter, node);
//
//            // Find all filters that can be used as constraint setters
//            if(finder.isConstraintSetterType(thisFilter.getClass())){
//                allConstraintSetterFilters.add(thisFilter);
//            }
//
//            // Find all filters that can be matched
//            if(finder.isConstraintSetterType(thisFilter.getClass())){
//                allMatchingFilters.add(thisFilter);
//            }
//        }
//
//        for(AbstractFilter constraintSetter: allConstraintSetterFilters){ // For each constraint setter
//            finder.setConstraints(constraintSetter);
//
//            Set<AbstractFilter> matchedFilters = new HashSet<>();
//
//            for(AbstractFilter testFilter: allMatchingFilters){
//                if(finder.check(testFilter)){
//                    matchedFilters.add(testFilter);
//                    applicableLiterals.put(testFilter, filter2LiteralMap.get(testFilter));
//                }
//            }
//
//            applicableFiltersMap.put(constraintSetter, matchedFilters);
//            applicableLiterals.put(constraintSetter, filter2LiteralMap.get(constraintSetter));
//
//            finder.clearConstraints();
//        }
//    }
//
//    /**
//     * Returns the node whose child literals satisfy the condition needed to apply the current operator (uses depth-first search)
//     * @param root
//     * @param targetLogic LogicalConnectiveType.OR or LogicalConnectiveType.AND
//     * @return
//     */
//    protected Connective getParentNodeOfApplicableNodes(Connective root, LogicalConnectiveType targetLogic){
//
//        // Inspect the children literals only if the current logical connective matches the target logic
//        if(root.getLogic() == targetLogic){
//
//            List<Literal> nodes = new ArrayList<>();
//            List<AbstractFilter> filters = new ArrayList<>();
//
//            // Check if there exist applicable nodes. When applicable nodes are found, nodes and filters are filled in as side effects
//            this.findApplicableNodesUnderGivenParentNode(root, nodes, filters);
//
//            if(!nodes.isEmpty()){
//                // Applicable nodes are found under the current node
//                return root;
//            }
//        }
//
//        for(Connective branch: root.getConnectiveChildren()){
//            Connective temp = this.getParentNodeOfApplicableNodes(branch, targetLogic);
//            if(temp != null){
//                // Applicable node is found in one of the child branches
//                return temp;
//            }
//        }
//
//        return null;
//    }
//
//    @Override
//    public int getArity(){
//        return 1;
//    }
//}
