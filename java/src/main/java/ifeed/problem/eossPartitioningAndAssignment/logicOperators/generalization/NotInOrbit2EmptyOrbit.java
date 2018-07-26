package ifeed.problem.eossPartitioningAndAssignment.logicOperators.generalization;

import ifeed.feature.logic.Connective;
import ifeed.feature.logic.Literal;
import ifeed.feature.logic.LogicalConnectiveType;
import ifeed.feature.Feature;
import ifeed.filter.FilterConstraint;
import ifeed.filter.Filter;
import ifeed.mining.moea.operators.AbstractGeneralizationOperator;
import ifeed.mining.moea.MOEABase;
import ifeed.problem.eoss.filters.Absent;
import ifeed.problem.eoss.filters.NotInOrbit;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class NotInOrbit2EmptyOrbit extends AbstractGeneralizationOperator{

    public NotInOrbit2EmptyOrbit(MOEABase base) {
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

        HashMap<Integer, HashSet<Integer>> orbit2LiteralIndices = new HashMap<>();

        for(int i = 0; i < filters.size(); i++){

            Filter filter = filters.get(i);
            int orbit = ((NotInOrbit) filter).getOrbit();

            if(orbit2LiteralIndices.keySet().contains(orbit)){
                orbit2LiteralIndices.get(orbit).add(i);

            }else{
                HashSet<Integer> indices = new HashSet<>();
                indices.add(i);
                orbit2LiteralIndices.put(orbit, indices);
            }
        }

        HashMap<int[], HashSet<Integer>> out = new HashMap<>();
        for(int orb: orbit2LiteralIndices.keySet()){

            if(orbit2LiteralIndices.get(orb).size() >= 2){
                int[] args = new int[]{orb};
                out.put(args, orbit2LiteralIndices.get(orb));
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

        // Remove NotInOrbit nodes
        for(int index: applicableNodeIndices){
            Literal node = nodes.get(index);
            parent.removeLiteral(node);
        }

        // Add the EmptyOrbit feature to the parent node
        Filter emptyOrbitFilter = new Absent(selectedArgument);
        Feature emptyOrbitFeature = base.getFeatureFetcher().fetch(emptyOrbitFilter);
        parent.addLiteral(emptyOrbitFeature.getName(), emptyOrbitFeature.getMatches());
    }

    @Override
    public void findApplicableNodesUnderGivenParentNode(Connective parent, List<Literal> applicableLiterals, List<Filter> applicableFilters){
        // Find all InOrbit literals sharing the same instrument argument inside the current node (parent).
        // All Literals and their corresponding Filters are not returned, but the lists are filled up as side effects

        Constraint constraint = new Constraint();
        super.findApplicableNodesUnderGivenParentNode(parent, applicableLiterals, applicableFilters, constraint);
    }

    public class Constraint extends FilterConstraint {

        private int unusedOrbit;

        public Constraint(){
            super(NotInOrbit.class, NotInOrbit.class);
        }

        @Override
        public void setConstraints(Filter constraintSetter){
            NotInOrbit temp = (NotInOrbit) constraintSetter;
            this.unusedOrbit = temp.getOrbit();
        }

        /**
         * One of the instruments in the tested filter should be included in the constraint instrument set
         * @param filterToTest
         * @return
         */
        @Override
        public boolean check(Filter filterToTest){
            // Check if two literals share the same orbit
            return this.unusedOrbit  == ((NotInOrbit) filterToTest).getOrbit();
        }
    }
}
