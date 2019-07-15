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
    protected Feature newFeature;
    protected Literal newLiteral;

    @Override
    public void initialize(){
        this.constraintSetter = null;
        this.selectedClass = -1;
        this.selectedInstruments = new HashSet<>();
        this.targetParentNode = null;
        this.newFilter = null;
        this.newFeature = null;
        this.newLiteral = null;
    }

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
        this.initialize();
        Params params = (Params) super.params;

        this.constraintSetter = (NotInOrbit) constraintSetterAbstract;

        // Counter the number of occurrences of each instrument class
        Map<Integer, Integer> instrumentClassCounter = new HashMap<>();
        for(int instr: this.constraintSetter.getInstruments()){
            Set<Integer> superclasses = params.getLeftSetSuperclass(instr, true);
            for(int cl: superclasses){
                if(instrumentClassCounter.containsKey(cl)){
                    instrumentClassCounter.put(cl, instrumentClassCounter.get(cl)+1);
                }else{
                    instrumentClassCounter.put(cl, 0);
                }
            }
        }

        // Find the most frequent instrument class
        List<Integer> mostFrequentClass = new ArrayList<>();
        int highestFrequency = 0;
        for(int cl: instrumentClassCounter.keySet()){
            if(super.getRestrictedVariables().contains(cl)){
                continue;
            } else if(instrumentClassCounter.get(cl) > highestFrequency){
                highestFrequency = instrumentClassCounter.get(cl);
                mostFrequentClass = new ArrayList<>();
                mostFrequentClass.add(cl);

            }else if(instrumentClassCounter.get(cl) == highestFrequency){
                mostFrequentClass.add(cl);
            }
        }

        if(mostFrequentClass.isEmpty() || highestFrequency == 1){
            super.setExhaustiveSearchFinished();
        }

        // Randomly select one of the classes
        Collections.shuffle(mostFrequentClass);
        this.selectedClass = mostFrequentClass.get(0);

        // Add a variable restriction to prevent the same class being tested later
        super.addVariableRestriction(this.selectedClass);

        Multiset<Integer> modifiedInstrumentSet = HashMultiset.create();
        this.selectedInstruments = new HashSet<>();
        for(int instr: this.constraintSetter.getInstruments()){
            if(params.getLeftSetSuperclass(instr, true).contains(this.selectedClass)){
                this.selectedInstruments.add(instr);
            }else{
                modifiedInstrumentSet.add(instr);
            }
        }
        modifiedInstrumentSet.add(this.selectedClass);

        // Remove the current node
        Literal constraintSetterLiteral = nodes.get(constraintSetterAbstract);
        parent.removeLiteral(constraintSetterLiteral);

        // Add the new feature to the parent node
        this.newFilter = new NotInOrbit(params, ((NotInOrbit)constraintSetterAbstract).getOrbit(), modifiedInstrumentSet);
        this.newFeature = this.base.getFeatureFetcher().fetch(newFilter);
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
                if(instances.size() > 2){
                    return true;
                }
            }
            return false;
        }
    }
}
