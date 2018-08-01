package ifeed.problem.assigning.logicOperators.generalization;

import ifeed.feature.Feature;
import ifeed.feature.logic.Connective;
import ifeed.feature.logic.Literal;
import ifeed.feature.logic.LogicalConnectiveType;
import ifeed.filter.AbstractFilter;
import ifeed.filter.AbstractFilterConstraint;
import ifeed.mining.moea.MOEABase;
import ifeed.mining.moea.operators.AbstractGeneralizationOperator;
import ifeed.problem.assigning.filters.InOrbit;
import ifeed.problem.assigning.filters.Together;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InOrbit2Together extends AbstractGeneralizationOperator{

    public InOrbit2Together(MOEABase base) {
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

        // Mapping from combinations of instruments to the indices of the nodes that contain those instruments
        HashMap<int[], HashSet<Integer>> out = new HashMap<>();

        for(int i = 0; i < filters.size(); i++){
            for(int j = i+1; j < filters.size(); j++){

                Set<Integer> combo = new HashSet<>();

                for(int instr:((InOrbit) filters.get(i)).getInstruments()){
                    HashSet<Integer> instruments = ((InOrbit) filters.get(j)).getInstruments();
                    if(instruments.contains(instr)){
                        combo.add(instr);
                    }
                }

                if(combo.size() > 1){

                    HashSet<Integer> indices = new HashSet<>();
                    indices.add(i);
                    indices.add(j);

                    int[] args = new int[combo.size()];
                    int k = 0;
                    for(int instr:combo){
                        args[k] = instr;
                        k++;
                    }

                    out.put(args, indices);
                }
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
            for(int instr:selectedArguments){
                // Remove the instruments
                instrHashSet.remove(instr);
            }

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
        AbstractFilter togetherFilter = new Together(selectedArguments);
        Feature togetherFeature = base.getFeatureFetcher().fetch(togetherFilter);
        grandParent.addLiteral(togetherFeature.getName(), togetherFeature.getMatches());
    }

    @Override
    public void findApplicableNodesUnderGivenParentNode(Connective parent, List<Literal> applicableLiterals, List<AbstractFilter> applicableFilters){
        // Find two InOrbit literals sharing a combination of instruments inside the current node (parent).
        // All Literals and their corresponding Filters are not returned, but the lists are filled up as side effects

        Constraint constraint = new Constraint();
        super.findApplicableNodesUnderGivenParentNode(parent, applicableLiterals, applicableFilters, constraint);
    }

    public class Constraint extends AbstractFilterConstraint {

        HashSet<Integer> instrumentsSet1;

        public Constraint(){
            super(InOrbit.class, InOrbit.class);
        }

        @Override
        public void setConstraints(AbstractFilter constraintSetter){
            InOrbit temp = (InOrbit) constraintSetter;
            this.instrumentsSet1 = temp.getInstruments();
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
            HashSet<Integer> instruments1 = this.instrumentsSet1;
            HashSet<Integer> instruments2 = temp.getInstruments();

            int cnt = 0;
            for(int inst:instruments2){
                if(instruments1.contains(inst)) {
                    cnt++;
                }
            }

            // There should be at least two instruments shared by the two Filters
            return cnt > 1;
        }
    }
}


