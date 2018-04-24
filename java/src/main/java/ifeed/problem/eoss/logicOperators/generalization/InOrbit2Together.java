package ifeed.problem.eoss.logicOperators.generalization;

import ifeed.feature.Feature;
import ifeed.feature.logic.Connective;
import ifeed.feature.logic.Literal;
import ifeed.feature.logic.LogicalConnectiveType;
import ifeed.filter.Filter;
import ifeed.filter.FilterFetcher;
import ifeed.local.MOEAParams;
import ifeed.mining.moea.FeatureTreeSolution;
import ifeed.mining.moea.FeatureTreeVariable;
import ifeed.mining.moea.MOEABase;
import ifeed.mining.moea.operators.LogicOperator;
import ifeed.problem.eoss.filters.InOrbit;
import ifeed.problem.eoss.filters.Present;
import org.moeaframework.core.PRNG;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class InOrbit2Together extends LogicOperator implements Variation{

    protected MOEABase base;
    protected FilterFetcher fetcher;

    public InOrbit2Together(MOEABase base) {
        this.base = base;
        this.fetcher = base.getFeatureFetcher().getFilterFetcher();
    }

    @Override
    public Solution[] evolve(Solution[] parents){

        FeatureTreeVariable tree = (FeatureTreeVariable) parents[0].getVariable(0);

        Connective root = tree.getRoot().copy();

        Connective parent = this.getParentNodeOfApplicableNodes(root, LogicalConnectiveType.OR);

        List<Literal> nodes = new ArrayList<>();
        List<Filter> filters = new ArrayList<>();
        this.findApplicableNodesUnderGivenParentNode(parent, nodes, filters);

        HashMap<Integer, HashSet<Integer>> instrument2LiteralIndices = new HashMap<>();

        for(int i = 0; i < filters.size(); i++){

            Filter filter = filters.get(i);
            HashSet<Integer> instruments = ((InOrbit) filter).getInstruments();

            for(int instr: instruments){
                if(instrument2LiteralIndices.keySet().contains(instr)){
                    instrument2LiteralIndices.get(instr).add(i);

                }else{
                    HashSet<Integer> indices = new HashSet<>();
                    indices.add(i);
                    instrument2LiteralIndices.put(instr, indices);
                }
            }
        }

        int randInstrInd = PRNG.nextInt(instrument2LiteralIndices.keySet().size());
        int selectedInstr = 0;

        int i = 0;
        for(int instr: instrument2LiteralIndices.keySet()){
            if (i == randInstrInd){
                selectedInstr = instr;
            }
            i++;
        }

        // Remove InOrbit nodes that share an instrument
        HashSet<Integer> literalIndices = instrument2LiteralIndices.get(selectedInstr);

        for(int index: literalIndices){
            Literal node = nodes.get(index);
            Filter filter = filters.get(index);

            HashSet<Integer> instrHashSet = ((InOrbit) filter).getInstruments();
            instrHashSet.remove(selectedInstr);

            if(!instrHashSet.isEmpty()){ // If instruments still exist

                int orbit = ((InOrbit) filter).getOrbit();
                int[] instruments = new int[instrHashSet.size()];

                i = 0;
                for(int instr: instrHashSet){
                    instruments[i] = instr;
                    i++;
                }

                Filter newFilter = new InOrbit(orbit, instruments);
                Feature newFeature = base.getFeatureFetcher().fetch(newFilter);
                parent.addLiteral(newFeature.getName(), newFeature.getMatches());
            }

            parent.removeLiteral(node);

        }

        Filter presentFilter = new Present(selectedInstr);
        Feature presentFeature = base.getFeatureFetcher().fetch(presentFilter);
        parent.addLiteral(presentFeature.getName(), presentFeature.getMatches());

        FeatureTreeVariable newTree = new FeatureTreeVariable(root, this.base);
        Solution sol = new FeatureTreeSolution(newTree, MOEAParams.numberOfObjectives);

        return new Solution[]{sol};
    }

    @Override
    public boolean checkApplicability(Connective root){
        return this.getParentNodeOfApplicableNodes(root, LogicalConnectiveType.OR) != null;
    }

    @Override
    public void findApplicableNodesUnderGivenParentNode(Connective parent, List<Literal> applicableLiterals, List<Filter> applicableFilters){
        // Find all InOrbit literals that contain the same two instruments inside the current node.
        // All Literals and their corresponding Filters are not returned, but the lists are filled up as side effects

        if(!applicableLiterals.isEmpty() || !applicableFilters.isEmpty()){
            throw new IllegalStateException("Input argument lists should be empty. These lists are to be filled automatically, as side effects instead of returning.");
        }

        List<Literal> allInOrbitLiterals = new ArrayList<>();
        List<InOrbit> allInOrbitFilters = new ArrayList<>();

        // Iterate over literals in the current node
        for(Literal node: parent.getLiteralChildren()){
            String[] nameAndArgs = this.fetcher.getNameAndArgs(node.getName());

            // Check for InOrbit filter
            if(nameAndArgs[0].equalsIgnoreCase(InOrbit.class.getName())){
                // Current node represents InOrbit feature

                InOrbit thisFilter = (InOrbit) this.fetcher.fetch(node.getName());

                // Compare with all other InOrbit features
                for(InOrbit otherFilter: allInOrbitFilters){

                    // Check if two literals share the same instrument
                    HashSet<Integer> instruments1 = thisFilter.getInstruments();
                    HashSet<Integer> instruments2 = otherFilter.getInstruments();

                    int cnt = 0;
                    for(int inst:instruments1){
                        if(instruments2.contains(inst)){
                            cnt++;
                        }
                    }

                    if(cnt > 1){ // The number of instruments that are shared is greater than or equal to 2
                        if(!applicableFilters.contains(thisFilter)){
                            // Add the current literal and filter
                            applicableLiterals.add(node);
                            applicableFilters.add(thisFilter);

                        }

                        if(!applicableFilters.contains(otherFilter)){
                            // Add the other literal and filter if it was not added before
                            int index = allInOrbitFilters.indexOf(otherFilter);
                            applicableLiterals.add(allInOrbitLiterals.get(index));
                            applicableFilters.add(otherFilter);
                        }

                    }
                }

                // Add all nodes into a list
                allInOrbitLiterals.add(node);
                allInOrbitFilters.add(thisFilter);
            }
        }
    }

    @Override
    public int getArity(){
        return 1;
    }
}
