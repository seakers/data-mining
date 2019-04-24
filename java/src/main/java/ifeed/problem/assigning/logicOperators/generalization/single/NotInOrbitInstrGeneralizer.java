package ifeed.problem.assigning.logicOperators.generalization.single;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import ifeed.feature.Feature;
import ifeed.feature.logic.Connective;
import ifeed.feature.logic.Literal;
import ifeed.filter.AbstractFilter;
import ifeed.filter.AbstractFilterFinder;
import ifeed.local.params.BaseParams;
import ifeed.mining.moea.AbstractMOEABase;
import ifeed.mining.moea.operators.AbstractGeneralizationOperator;
import ifeed.mining.moea.operators.AbstractLogicOperator;
import ifeed.problem.assigning.Params;
import ifeed.problem.assigning.filters.InOrbit;
import ifeed.problem.assigning.filters.NotInOrbit;
import ifeed.problem.assigning.filters.Separate;
import ifeed.problem.assigning.filters.Together;

import java.util.*;

public class NotInOrbitInstrGeneralizer extends AbstractGeneralizationOperator {

    protected NotInOrbit constraintSetter;
    protected int selectedClass;
    protected Set<Integer> selectedInstruments;
    protected Connective targetParentNode;
    protected AbstractFilter newFilter;
    protected Literal newLiteral;

    public NotInOrbitInstrGeneralizer(BaseParams params, AbstractMOEABase base) {
        super(params, base);
    }

    @Override
    public void apply(Connective root,
                         Connective parent,
                         AbstractFilter constraintSetterAbstract,
                         Set<AbstractFilter> matchingFilters,
                         Map<AbstractFilter, Literal> nodes
    ){
        Params params = (Params) super.params;

        this.constraintSetter = (NotInOrbit) constraintSetterAbstract;

        Map<Integer, Set<Integer>> instrumentClass2InstanceMap = new HashMap<>();
        for(int instr: this.constraintSetter.getInstruments()){
            Set<Integer> superclasses = params.getLeftSetSuperclass(instr, true);

            for(int cl: superclasses){
                Set<Integer> instanceSet;
                if(instrumentClass2InstanceMap.containsKey(cl)){
                    instanceSet = instrumentClass2InstanceMap.get(cl);
                }else{
                    instanceSet = new HashSet<>();
                }
                instanceSet.add(instr);
                instrumentClass2InstanceMap.put(cl, instanceSet);
            }
        }

        // Find the most frequent instrument
        List<Integer> mostFrequentClass = new ArrayList<>();
        int highestFrequency = 0;
        for(int cl: instrumentClass2InstanceMap.keySet()){

            if(super.getRestrictedVariables().contains(cl)){
                continue;
            } else if(instrumentClass2InstanceMap.get(cl).size() > highestFrequency){
                highestFrequency = instrumentClass2InstanceMap.get(cl).size();
                mostFrequentClass = new ArrayList<>();
                mostFrequentClass.add(cl);

            }else if(instrumentClass2InstanceMap.get(cl).size() == highestFrequency){
                mostFrequentClass.add(cl);
            }
        }

        if(mostFrequentClass.isEmpty() || highestFrequency == 1){
            super.setExhaustiveSearchFinished();
            return;
        }

        // Randomly select one of the classes
        Collections.shuffle(mostFrequentClass);
        this.selectedClass = mostFrequentClass.get(0);

        super.addVariableRestriction(this.selectedClass);

        Set<Integer> instanceSet = instrumentClass2InstanceMap.get(this.selectedClass);
        this.selectedInstruments = instanceSet;

        Multiset<Integer> modifiedInstrumentSet = HashMultiset.create();
        for(int instr: constraintSetter.getInstruments()){
            if(params.getLeftSetSuperclass(instr).contains(this.selectedClass)){
                continue;
            }else{
                modifiedInstrumentSet.add(instr);
            }
        }
        modifiedInstrumentSet.add(this.selectedClass);

        newFilter = new NotInOrbit(params, ((NotInOrbit)constraintSetterAbstract).getOrbit(), modifiedInstrumentSet);

        // Remove the current node
        Literal constraintSetterLiteral = nodes.get(constraintSetterAbstract);
        parent.removeLiteral(constraintSetterLiteral);

        // Add the new feature to the parent node
        Feature newFeature = this.base.getFeatureFetcher().fetch(newFilter);
        this.newLiteral = new Literal(newFeature.getName(), newFeature.getMatches());
        parent.addLiteral(newLiteral);

        targetParentNode = parent;
    }

    @Override
    public String getDescription(){
        StringBuilder sb = new StringBuilder();
        sb.append("Generalize ");
        sb.append("\"" + constraintSetter.getDescription() + "\"");
        sb.append(" to ");
        sb.append("\"" + this.newFilter.getDescription() + "\"");
        return sb.toString();
    }


    @Override
    public void findApplicableNodesUnderGivenParentNode(Connective parent,
                                                        Map<AbstractFilter, Set<AbstractFilter>> applicableFiltersMap,
                                                        Map<AbstractFilter, Literal> applicableLiteralsMap
    ){
        Params params = (Params) super.params;

        // Find all literals that contain sets of instruments as arguments
        FilterFinder finder = new FilterFinder(params);
        super.findApplicableNodesUnderGivenParentNode(parent, applicableFiltersMap, applicableLiteralsMap, finder);
    }

    public class FilterFinder extends AbstractFilterFinder {

        private Params params;
        private Multiset<Integer> instruments;

        public FilterFinder(Params params){
            super(NotInOrbit.class);
            this.params = params;
        }

        @Override
        public void setConstraints(AbstractFilter constraintSetter){
            instruments = ((NotInOrbit) constraintSetter).getInstruments();
        }

        @Override
        public void clearConstraints(){
            instruments = null;
        }

        @Override
        public boolean check(){
            Set<Integer> superclassSet = new HashSet<>();
            for(int instr: instruments){
                Set<Integer> superclasses = params.getLeftSetSuperclass(instr, true);
                superclassSet.addAll(superclasses);
            }

            // Check if there is at least one class that covers more than one instrument variable
            for(int cl: superclassSet){
                Set<Integer> instances = params.getLeftSetInstantiation(cl);
                instances.retainAll(instruments);
                if(instances.size() > 1){
                    return true;
                }
            }
            return false;
        }
    }
}
