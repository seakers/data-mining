//package ifeed.problem.eoss.logicOperators.generalization;
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
//import ifeed.problem.eoss.filters.NotInOrbit;
//import ifeed.problem.eoss.filters.EmptyOrbit;
//
//import org.moeaframework.core.Variation;
//import org.moeaframework.core.Solution;
//import org.moeaframework.core.PRNG;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Set;
//import java.util.List;
//
//public class NotInOrbit2EmptyOrbit extends AbstractLogicOperator{
//
//    public NotInOrbit2EmptyOrbit(MOEABase base) {
//        super(base, LogicalConnectiveType.AND);
//    }
//
//    /**
//     * Creates a HashMap that maps arguments to different indices of the literals that have those arguments
//     * @param nodes
//     * @param filters
//     * @return
//     */
//    @Override
//    protected HashMap<Integer, HashSet<Integer>> mapArgumentTypes2LiteralIndices(List<Literal> nodes, List<Filter> filters){
//
//        HashMap<Integer, HashSet<Integer>> instrument2LiteralIndices = new HashMap<>();
//
//        for(int i = 0; i < filters.size(); i++){
//
//            Filter filter = filters.get(i);
//            HashSet<Integer> instruments = ((NotInOrbit) filter).getInstruments();
//
//            for(int instr: instruments){
//                if(instrument2LiteralIndices.keySet().contains(instr)){
//                    instrument2LiteralIndices.get(instr).add(i);
//
//                }else{
//                    HashSet<Integer> indices = new HashSet<>();
//                    indices.add(i);
//                    instrument2LiteralIndices.put(instr, indices);
//                }
//            }
//        }
//
//        HashMap<Integer, HashSet<Integer>> out = new HashMap<>();
//        for(int instr: instrument2LiteralIndices.keySet()){
//
//            if(instrument2LiteralIndices.get(instr).size() >= 2){
//                out.put(instr, instrument2LiteralIndices.get(instr));
//            }
//        }
//
//        return out;
//    }
//
//
//
//
//
//
//    @Override
//    public Solution[] evolve(Solution[] parents){
//
//        FeatureTreeVariable tree = (FeatureTreeVariable) parents[0].getVariable(0);
//
//        Connective root = tree.getRoot().copy();
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
//            int orb = ((NotInOrbit) filter).getOrbit();
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
//        int randOrbitInd = PRNG.nextInt(orbit2LiteralIndices.keySet().size());
//        int selectedOrbit = 0;
//
//        int i = 0;
//        for(int orbit: orbit2LiteralIndices.keySet()){
//            if (i == randOrbitInd){
//                selectedOrbit = orbit;
//            }
//            i++;
//        }
//
//        // Replace NotInOrbit nodes with EmptyOrbit
//        HashSet<Integer> literalIndices = orbit2LiteralIndices.get(selectedOrbit);
//        for(int index: literalIndices){
//            Literal node = nodes.get(index);
//            parent.removeLiteral(node);
//        }
//
//        Filter emptyOrbitFilter = new EmptyOrbit(selectedOrbit);
//        Feature emptyOrbitFeature = base.getFeatureFetcher().fetch(emptyOrbitFilter);
//        parent.addLiteral(emptyOrbitFeature.getName(), emptyOrbitFeature.getMatches());
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
//        // Find all NotInOrbit literals sharing the same orbit argument inside the current node (parent).
//        // The list of all Literals and their corresponding Filters are not returned, but rather the lists are filled up as side effects
//
//        if(!applicableLiterals.isEmpty() || !applicableFilters.isEmpty()){
//            throw new IllegalStateException("Input argument lists should be empty. These lists are to be filled automatically, as side effects instead of returning.");
//        }
//
//        NotInOrbit tempTargetFilter = new NotInOrbit(0, 0);
//        List<Literal> allNotInOrbitLiterals = new ArrayList<>();
//        List<NotInOrbit> allNotInOrbitFilters = new ArrayList<>();
//
//        // Iterate over literals in the current node
//        for(Literal node: parent.getLiteralChildren()){
//            String[] nameAndArgs = this.fetcher.getNameAndArgs(node.getName());
//
//            // Check for NotInOrbit filter
//            if(nameAndArgs[0].equalsIgnoreCase(tempTargetFilter.getName())){
//                // Current node represents NotInOrbit feature
//
//                NotInOrbit thisFilter = (NotInOrbit) this.fetcher.fetch(node.getName());
//
//                // Compare with all other NotInOrbit features
//                for(NotInOrbit otherFilter: allNotInOrbitFilters){
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
//
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
