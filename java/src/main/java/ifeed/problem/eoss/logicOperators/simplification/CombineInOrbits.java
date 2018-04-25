//package ifeed.problem.eoss.logicOperators.simplification;
//
//import ifeed.feature.logic.Connective;
//import ifeed.feature.logic.Literal;
//import ifeed.feature.logic.LogicalConnectiveType;
//import ifeed.feature.Feature;
//import ifeed.filter.FilterFetcher;
//import ifeed.filter.Filter;
//import ifeed.mining.moea.operators.AbstractLogicOperator;
//import ifeed.mining.moea.FeatureTreeVariable;
//import ifeed.mining.moea.MOEABase;
//import ifeed.mining.moea.FeatureTreeSolution;
//import ifeed.local.MOEAParams;
//
//import ifeed.problem.eoss.filters.InOrbit;
//
//import org.moeaframework.core.Variation;
//import org.moeaframework.core.Solution;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
//public class CombineInOrbits extends AbstractLogicOperator implements Variation{
//
//    protected MOEABase base;
//    protected FilterFetcher fetcher;
//    protected static LogicalConnectiveType logic = LogicalConnectiveType.AND;
//
//    public CombineInOrbits(MOEABase base) {
//        this.base = base;
//        this.fetcher = base.getFeatureFetcher().getFilterFetcher();
//    }
//
//    @Override
//    public Solution[] evolve(Solution[] parents){
//
//        FeatureTreeVariable tree = (FeatureTreeVariable) parents[0].getVariable(0);
//
//        Connective root = tree.getRoot().copy();
//
//        Connective parent = this.getParentNodeOfApplicableNodes(root, this.logic);
//
//        List<Literal> nodes = new ArrayList<>();
//        List<Filter> filters = new ArrayList<>();
//        this.findApplicableNodesUnderGivenParentNode(parent, nodes, filters);
//
//        Set<Integer> applicableOrbits = new HashSet<>();
//        HashMap<Integer, HashSet<Integer>> orbit2LiteralIndices = new HashMap<>();
//
//        for(int i = 0; i < filters.size(); i++){
//
//            Filter filter = filters.get(i);
//            int orb = ((InOrbit) filter).getOrbit();
//
//            if(orbit2LiteralIndices.keySet().contains(orb)){
//                orbit2LiteralIndices.get(orb).add(i);
//
//            }else{
//                HashSet<Integer> indices = new HashSet<>();
//                indices.add(i);
//                orbit2LiteralIndices.put(orb, indices);
//            }
//        }
//
//        for(int orb: applicableOrbits){
//            // Combine multiple InOrbits with a single one
//            HashSet<Integer> literalIndices = orbit2LiteralIndices.get(orb);
//
//            Set<Integer> instruments = new HashSet<>();
//
//            for(int index: literalIndices){
//                Literal node = nodes.get(index);
//                InOrbit InOrbitFilter = (InOrbit) filters.get(index);
//
//                instruments.addAll(InOrbitFilter.getInstruments());
//                parent.removeLiteral(node);
//            }
//
//            int[] instrumentsArr = new int[instruments.size()];
//            int i = 0;
//            for(int instr: instruments){
//                instrumentsArr[i] = instr;
//                i++;
//            }
//
//            Filter InOrbitFilter = new InOrbit(orb, instrumentsArr);
//            Feature InOrbitFeature = base.getFeatureFetcher().fetch(InOrbitFilter);
//            parent.addLiteral(InOrbitFeature.getName(), InOrbitFeature.getMatches());
//        }
//
//        FeatureTreeVariable newTree = new FeatureTreeVariable(root, this.base);
//        Solution sol = new FeatureTreeSolution(newTree, MOEAParams.numberOfObjectives);
//
//        return new Solution[]{sol};
//    }
//
//    @Override
//    public boolean checkApplicability(Connective root){
//        return this.getParentNodeOfApplicableNodes(root, this.logic) != null;
//    }
//
//    @Override
//    public void findApplicableNodesUnderGivenParentNode(Connective parent, List<Literal> applicableLiterals, List<Filter> applicableFilters){
//        // Find all InOrbit literals sharing the same orbit argument inside the current node (parent).
//        // All Literals and their corresponding Filters are not returned, but the lists are filled up as side effects
//
//        if(!applicableLiterals.isEmpty() || !applicableFilters.isEmpty()){
//            throw new IllegalStateException("Input argument lists should be empty. These lists are to be filled automatically, as side effects instead of returning.");
//        }
//
//        InOrbit tempTargetFilter = new InOrbit(0, 0);
//        List<Literal> allNotInOrbitLiterals = new ArrayList<>();
//        List<InOrbit> allNotInOrbitFilters = new ArrayList<>();
//
//        // Iterate over literals in the current node
//        for(Literal node: parent.getLiteralChildren()){
//            String[] nameAndArgs = this.fetcher.getNameAndArgs(node.getName());
//
//            // Check for NotInOrbit filter
//            if(nameAndArgs[0].equalsIgnoreCase(tempTargetFilter.getName())){
//                // Current node represents NotInOrbit feature
//
//                InOrbit thisFilter = (InOrbit) this.fetcher.fetch(node.getName());
//
//                // Compare with all other NotInOrbit features
//                for(InOrbit otherFilter: allNotInOrbitFilters){
//
//                    // Check if two literals share the same orbit
//                    if(thisFilter.getOrbit() == otherFilter.getOrbit()){
//
//                        if(!applicableFilters.contains(thisFilter)){
//                            // Add the current literal and filter
//                            applicableLiterals.add(node);
//                            applicableFilters.add(thisFilter);
//                        }
//
//                        if(!applicableFilters.contains(otherFilter)){
//                            // Add the other literal and filter if it was not added before
//                            int index = allNotInOrbitFilters.indexOf(otherFilter);
//                            applicableLiterals.add(allNotInOrbitLiterals.get(index));
//                            applicableFilters.add(otherFilter);
//                        }
//                    }
//                }
//
//                // Add all nodes into a list
//                allNotInOrbitLiterals.add(node);
//                allNotInOrbitFilters.add(thisFilter);
//            }
//        }
//    }
//
//    @Override
//    public int getArity(){
//        return 1;
//    }
//}
