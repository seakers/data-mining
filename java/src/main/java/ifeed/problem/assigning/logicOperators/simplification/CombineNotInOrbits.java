package ifeed.problem.assigning.logicOperators.simplification;

import ifeed.feature.logic.Connective;
import ifeed.feature.logic.Literal;
import ifeed.feature.logic.LogicalConnectiveType;
import ifeed.feature.Feature;
import ifeed.filter.AbstractFilter;
import ifeed.filter.AbstractFilterConstraint;
import ifeed.mining.moea.MOEABase;

import ifeed.mining.moea.operators.AbstractSimplificationOperator;
import ifeed.problem.assigning.filters.NotInOrbit;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CombineNotInOrbits extends AbstractSimplificationOperator{

    public CombineNotInOrbits(MOEABase base) {
        super(base, LogicalConnectiveType.AND);
    }

    /**
     * Creates a HashMap that maps arguments to different indices of the literals that have those arguments
     * @param nodes
     * @param filters
     * @return
     */
    @Override
    protected HashMap<int[], HashSet<Integer>> mapArguments2LiteralIndices(List<Literal> nodes, List<AbstractFilter> filters){

        HashMap<Integer, HashSet<Integer>> orbit2LiteralIndices = new HashMap<>();

        for(int i = 0; i < filters.size(); i++){

            AbstractFilter filter = filters.get(i);
            int orb = ((NotInOrbit) filter).getOrbit();

            if(orbit2LiteralIndices.keySet().contains(orb)){
                orbit2LiteralIndices.get(orb).add(i);

            }else{
                HashSet<Integer> indices = new HashSet<>();
                indices.add(i);
                orbit2LiteralIndices.put(orb, indices);
            }
        }

        HashMap<int[], HashSet<Integer>> out = new HashMap<>();
        for(int instr: orbit2LiteralIndices.keySet()){

            if(orbit2LiteralIndices.get(instr).size() >= 2){
                int[] args = new int[]{instr};
                out.put(args, orbit2LiteralIndices.get(instr));
            }
        }

        return out;
    }

    @Override
    protected void apply(Connective root,
                         Connective parent,
                         List<Literal> nodes,
                         List<AbstractFilter> filters,
                         int[] selectedArguments,
                         HashSet<Integer> applicableNodeIndices
    ){

        Set<Integer> combinedInstruments = new HashSet<>();

        // Remove NotInOrbit nodes that assign instruments in the same orbit
        for(int index: applicableNodeIndices){

            Literal node = nodes.get(index);
            AbstractFilter filter = filters.get(index);

            // Add all instruments into one set
            combinedInstruments.addAll(((NotInOrbit) filter).getInstruments());
            parent.removeLiteral(node);
        }

        int[] combinedInstrumentsArray = new int[combinedInstruments.size()];
        int i = 0;
        for(int instr:combinedInstruments){
            combinedInstrumentsArray[i] = instr;
            i++;
        }

        int selectedArgument = selectedArguments[0];

        // Add the combined NotInOrbit feature to the parent node
        AbstractFilter notInOrbitFilter = new NotInOrbit(selectedArgument, combinedInstrumentsArray);
        Feature notInOrbitFeature = base.getFeatureFetcher().fetch(notInOrbitFilter);
        parent.addLiteral(notInOrbitFeature.getName(), notInOrbitFeature.getMatches());
    }


    @Override
    public void findApplicableNodesUnderGivenParentNode(Connective parent, List<Literal> applicableLiterals, List<AbstractFilter> applicableFilters){
        // Find all NotInOrbit literals sharing the same instrument argument inside the current node (parent).
        // All Literals and their corresponding Filters are not returned, but the lists are filled up as side effects

        Constraint constraint = new Constraint();
        super.findApplicableNodesUnderGivenParentNode(parent, applicableLiterals, applicableFilters, constraint);
    }

    public class Constraint extends AbstractFilterConstraint {

        public int orbit;

        public Constraint(){
            super(NotInOrbit.class, NotInOrbit.class);
        }

        @Override
        public void setConstraints(AbstractFilter constraintSetter){
            NotInOrbit temp = (NotInOrbit) constraintSetter;
            this.orbit = temp.getOrbit();
        }

        /**
         * Both features should assign instruments to the same orbit
         * @param filterToTest
         * @return
         */
        @Override
        public boolean check(AbstractFilter filterToTest){
            NotInOrbit temp = (NotInOrbit) filterToTest;

            // Check if two literals share the same instrument
            return this.orbit == temp.getOrbit();
        }
    }
}
