package ifeed.problem.eoss.logicOperators.generalization;

import ifeed.feature.logic.Connective;
import ifeed.feature.logic.Literal;
import ifeed.feature.logic.LogicalConnectiveType;
import ifeed.feature.Feature;
import ifeed.filter.FilterConstraint;
import ifeed.filter.Filter;
import ifeed.mining.moea.operators.AbstractLogicOperator;
import ifeed.mining.moea.MOEABase;
import ifeed.problem.eoss.filters.InOrbit;
import ifeed.problem.eoss.filters.Present;

import org.moeaframework.core.PRNG;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class InOrbit2Present extends AbstractLogicOperator{

    public InOrbit2Present(MOEABase base) {
        super(base, LogicalConnectiveType.OR);
    }

    /**
     * Creates a HashMap that maps arguments to different indices of the literals that have those arguments
     * @param nodes
     * @param filters
     * @return
     */
    @Override
    protected HashMap<Integer, HashSet<Integer>> mapArgumentTypes2LiteralIndices(List<Literal> nodes, List<Filter> filters){

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

        HashMap<Integer, HashSet<Integer>> out = new HashMap<>();
        for(int instr: instrument2LiteralIndices.keySet()){

            if(instrument2LiteralIndices.get(instr).size() >= 2){
                out.put(instr, instrument2LiteralIndices.get(instr));
            }
        }

        return out;
    }

    /**
     * Randomly selects an argument from a list of arguments that satisfy the given constraint
     * @param arg2LiteralIndices
     * @return
     */
    @Override
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

    /**
     * Apply the given operator to a feature tree
     * @param root
     * @param parent
     * @param selectedArgument
     * @param nodes
     * @param filters
     * @param applicableNodeIndices
     */
    @Override
    protected void apply(Connective root,
                         Connective parent,
                         int selectedArgument,
                         List<Literal> nodes,
                         List<Filter> filters,
                         HashSet<Integer> applicableNodeIndices
        ){

        Connective grandParent = super.base.getFeatureHandler().findParentNode(root, parent);

        if(grandParent == null){ // Parent node is the root node since it doesn't have a parent node
            super.base.getFeatureHandler().createNewRootNode(root);
            grandParent = root;

            // Store the newly generated node to parent
            parent = grandParent.getConnectiveChildren().get(0);
        }

        // Remove InOrbit nodes that share an instrument
        for(int index: applicableNodeIndices){

            Literal node = nodes.get(index);
            Filter filter = filters.get(index);

            HashSet<Integer> instrHashSet = ((InOrbit) filter).getInstruments();
            instrHashSet.remove(selectedArgument);

            if(!instrHashSet.isEmpty()){ // If instruments still exist

                int orbit = ((InOrbit) filter).getOrbit();
                int[] instruments = new int[instrHashSet.size()];

                int i = 0;
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

        // Add the Present feature to the grandparent node
        Filter presentFilter = new Present(selectedArgument);
        Feature presentFeature = base.getFeatureFetcher().fetch(presentFilter);
        grandParent.addLiteral(presentFeature.getName(), presentFeature.getMatches());
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
            super(InOrbit.class, InOrbit.class);
        }

        @Override
        public void setConstraints(Filter constraintSetter){
            InOrbit temp = (InOrbit) constraintSetter;
            this.instrumentsToBeIncluded = temp.getInstruments();
        }

        /**
         * One of the instruments in the tested filter should be included in the constraint instrument set
         * @param filterToTest
         * @return
         */
        @Override
        public boolean check(Filter filterToTest){
            InOrbit temp = (InOrbit) filterToTest;

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
