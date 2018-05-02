package ifeed.problem.eoss.logicOperators.generalization;

import ifeed.feature.logic.Connective;
import ifeed.feature.logic.Literal;
import ifeed.feature.logic.LogicalConnectiveType;
import ifeed.feature.Feature;
import ifeed.filter.FilterConstraint;
import ifeed.filter.Filter;
import ifeed.mining.moea.operators.AbstractGeneralizationOperator;
import ifeed.mining.moea.MOEABase;
import ifeed.problem.eoss.filters.NotInOrbit;
import ifeed.problem.eoss.filters.Absent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class NotInOrbit2Absent extends AbstractGeneralizationOperator{

    public NotInOrbit2Absent(MOEABase base) {
        super(base, LogicalConnectiveType.AND);
    }

    /**
     * Creates a HashMap that maps arguments to different indices of the literals that have those arguments
     * @param nodes
     * @param filters
     * @return
     */
    @Override
    protected HashMap<int[], HashSet<Integer>> mapArguments2LiteralIndices(List<Literal> nodes, List<Filter> filters){

        HashMap<Integer, HashSet<Integer>> instrument2LiteralIndices = new HashMap<>();

        for(int i = 0; i < filters.size(); i++){

            Filter filter = filters.get(i);
            HashSet<Integer> instruments = ((NotInOrbit) filter).getInstruments();

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

        HashMap<int[], HashSet<Integer>> out = new HashMap<>();
        for(int instr: instrument2LiteralIndices.keySet()){

            if(instrument2LiteralIndices.get(instr).size() >= 2){
                int[] args = new int[]{instr};
                out.put(args, instrument2LiteralIndices.get(instr));
            }
        }

        return out;
    }

    /**
     * Apply the given operator to a feature tree
     * @param root
     * @param parent
     * @param selectedArguments
     * @param nodes
     * @param filters
     * @param applicableNodeIndices
     */
    @Override
    protected void apply(Connective root,
                         Connective parent,
                         List<Literal> nodes,
                         List<Filter> filters,
                         int[] selectedArguments,
                         HashSet<Integer> applicableNodeIndices
    ){

        int selectedArgument = selectedArguments[0];

        // Remove NotInOrbit nodes that share an instrument
        for(int index: applicableNodeIndices){

            Literal node = nodes.get(index);
            Filter filter = filters.get(index);

            HashSet<Integer> instrHashSet = ((NotInOrbit) filter).getInstruments();
            instrHashSet.remove(selectedArgument);

            if(!instrHashSet.isEmpty()){ // If instruments still exist

                int orbit = ((NotInOrbit) filter).getOrbit();
                int[] instruments = new int[instrHashSet.size()];

                int i = 0;
                for(int instr: instrHashSet){
                    instruments[i] = instr;
                    i++;
                }

                Filter newFilter = new NotInOrbit(orbit, instruments);
                Feature newFeature = base.getFeatureFetcher().fetch(newFilter);
                parent.addLiteral(newFeature.getName(), newFeature.getMatches());
            }

            parent.removeLiteral(node);
        }

        // Add the Absent feature to the parent node
        Filter absentFilter = new Absent(selectedArgument);
        Feature absentFeature = base.getFeatureFetcher().fetch(absentFilter);
        parent.addLiteral(absentFeature.getName(), absentFeature.getMatches());
    }


    @Override
    public void findApplicableNodesUnderGivenParentNode(Connective parent, List<Literal> applicableLiterals, List<Filter> applicableFilters){
        // Find all InOrbit literals sharing the same instrument argument inside the current node (parent).
        // All Literals and their corresponding Filters are not returned, but the lists are filled up as side effects

        Constraint constraint = new Constraint();
        super.findApplicableNodesUnderGivenParentNode(parent, applicableLiterals, applicableFilters, constraint);
    }

    public class Constraint extends FilterConstraint {

        HashSet<Integer> instrumentsToBeIncluded;

        public Constraint(){
            super(NotInOrbit.class, NotInOrbit.class);
        }

        @Override
        public void setConstraints(Filter constraintSetter){
            NotInOrbit temp = (NotInOrbit) constraintSetter;
            this.instrumentsToBeIncluded = temp.getInstruments();
        }

        /**
         * One of the instruments in the tested filter should be included in the constraint instrument set
         * @param filterToTest
         * @return
         */
        @Override
        public boolean check(Filter filterToTest){
            NotInOrbit temp = (NotInOrbit) filterToTest;

            // Check if two literals share the same instrument
            HashSet<Integer> instruments1 = this.instrumentsToBeIncluded;
            HashSet<Integer> instruments2 = temp.getInstruments();

            for(int inst:instruments2){
                if(instruments1.contains(inst)) {
                    return true;
                }
            }
            return false;
        }
    }
}
