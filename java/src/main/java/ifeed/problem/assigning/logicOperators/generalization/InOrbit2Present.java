package ifeed.problem.assigning.logicOperators.generalization;

import ifeed.feature.logic.Connective;
import ifeed.feature.logic.Literal;
import ifeed.feature.logic.LogicalConnectiveType;
import ifeed.feature.Feature;
import ifeed.filter.AbstractFilter;
import ifeed.filter.AbstractFilterConstraint;
import ifeed.mining.moea.operators.AbstractGeneralizationOperator;
import ifeed.mining.moea.MOEABase;
import ifeed.problem.assigning.filters.InOrbit;
import ifeed.problem.assigning.filters.Present;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class InOrbit2Present extends AbstractGeneralizationOperator{

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
    protected HashMap<int[], HashSet<Integer>> mapArguments2LiteralIndices(List<Literal> nodes, List<AbstractFilter> filters){

        HashMap<Integer, HashSet<Integer>> instrument2LiteralIndices = new HashMap<>();

        for(int i = 0; i < filters.size(); i++){

            AbstractFilter filter = filters.get(i);
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
     * @param nodes
     * @param filters
     * * @param selectedArgument
     * @param applicableNodeIndices
     */
    @Override
    protected void apply(Connective root,
                         Connective parent,
                         List<Literal> nodes,
                         List<AbstractFilter> filters,
                         int[] selectedArguments,
                         HashSet<Integer> applicableNodeIndices
        ){

        Connective grandParent = super.base.getFeatureHandler().findParentNode(root, parent);

        int selectedArgument = selectedArguments[0];

        if(grandParent == null){ // Parent node is the root node since it doesn't have a parent node
            super.base.getFeatureHandler().createNewRootNode(root);
            grandParent = root;

            // Store the newly generated node to parent
            parent = grandParent.getConnectiveChildren().get(0);
        }

        // Remove InOrbit nodes that share an instrument
        for(int index: applicableNodeIndices){

            Literal node = nodes.get(index);
            AbstractFilter filter = filters.get(index);

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

                AbstractFilter newFilter = new InOrbit(orbit, instruments);
                Feature newFeature = base.getFeatureFetcher().fetch(newFilter);
                parent.addLiteral(newFeature.getName(), newFeature.getMatches());
            }

            parent.removeLiteral(node);
        }

        // Add the Present feature to the grandparent node
        AbstractFilter presentFilter = new Present(selectedArgument);
        Feature presentFeature = base.getFeatureFetcher().fetch(presentFilter);
        grandParent.addLiteral(presentFeature.getName(), presentFeature.getMatches());
    }

    @Override
    public void findApplicableNodesUnderGivenParentNode(Connective parent, List<Literal> applicableLiterals, List<AbstractFilter> applicableFilters){
        // Find all InOrbit literals sharing the same instrument argument inside the current node (parent).
        // All Literals and their corresponding Filters are not returned, but the lists are filled up as side effects

        Constraint constraint = new Constraint();
        super.findApplicableNodesUnderGivenParentNode(parent, applicableLiterals, applicableFilters, constraint);
    }

    public class Constraint extends AbstractFilterConstraint {

        HashSet<Integer> instrumentsToBeIncluded;

        public Constraint(){
            super(InOrbit.class, InOrbit.class);
        }

        @Override
        public void setConstraints(AbstractFilter constraintSetter){
            InOrbit temp = (InOrbit) constraintSetter;
            this.instrumentsToBeIncluded = temp.getInstruments();
        }

        /**
         * One of the instruments in the tested filter should be included in the constraint instrument set
         * @param filterToTest
         * @return
         */
        @Override
        public boolean check(AbstractFilter filterToTest){

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
