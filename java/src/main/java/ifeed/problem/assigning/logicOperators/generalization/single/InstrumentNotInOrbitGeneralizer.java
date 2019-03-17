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
import ifeed.mining.moea.operators.AbstractLogicOperator;
import ifeed.problem.assigning.Params;
import ifeed.problem.assigning.filters.InOrbit;
import ifeed.problem.assigning.filters.NotInOrbit;
import ifeed.problem.assigning.filters.Separate;
import ifeed.problem.assigning.filters.Together;

import java.util.*;

public class InstrumentNotInOrbitGeneralizer extends AbstractLogicOperator {

    protected NotInOrbit constraintSetter;
    protected int selectedClass;
    protected AbstractFilter newFilter;
    protected Literal newLiteral;

    public InstrumentNotInOrbitGeneralizer(BaseParams params, AbstractMOEABase base) {
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

        constraintSetter = (NotInOrbit) constraintSetterAbstract;
        Multiset<Integer> instruments = constraintSetter.getInstruments();

        Map<Integer, Integer> superclassCounter = new HashMap<>();
        Map<Integer, Set<Integer>> superclassMap = new HashMap<>();

        for(int i: instruments.elementSet()){
            Set<Integer> superclasses = params.getLeftSetSuperclass("Instrument", i);

            superclassMap.put(i, superclasses);

            for(int c: superclasses){
                if(superclassCounter.containsKey(c)){
                    superclassCounter.put(c, superclassCounter.get(c)+1);
                }else{
                    superclassCounter.put(c, 1);
                }
            }
        }

        // Shuffle class orders
        List<Integer> keySet = new ArrayList<>();
        keySet.addAll(superclassCounter.keySet());
        Collections.shuffle(keySet);

        // Find the most frequent instrument
        int mostFrequentClass = -1;
        int highestFrequency = 0;
        for(int c: superclassCounter.keySet()){
            if(superclassCounter.get(c) > highestFrequency){
                highestFrequency = superclassCounter.get(c);
                mostFrequentClass = c;
            }
        }
        this.selectedClass = mostFrequentClass;

        Multiset<Integer> modifiedInstrumentSet = HashMultiset.create();
        for(int instr: instruments){
            if(superclassMap.get(instr).contains(this.selectedClass)){
                continue;
            }else{
                modifiedInstrumentSet.add(instr);
            }
        }
        modifiedInstrumentSet.add(this.selectedClass);

        if(modifiedInstrumentSet.count(this.selectedClass) > 1){
            modifiedInstrumentSet.remove(this.selectedClass);
        }

        newFilter = new NotInOrbit(params, ((NotInOrbit)constraintSetterAbstract).getOrbit(), modifiedInstrumentSet);

        // Remove the current node
        Literal constraintSetterLiteral = nodes.get(constraintSetterAbstract);
        parent.removeLiteral(constraintSetterLiteral);

        // Add the new feature to the parent node
        Feature newFeature = this.base.getFeatureFetcher().fetch(newFilter);
        this.newLiteral = new Literal(newFeature.getName(), newFeature.getMatches());
        parent.addLiteral(newLiteral);
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
        }

        @Override
        public boolean check(){
            Set<Integer> superclassesFound = new HashSet<>();
            for(int instr: instruments){
                Set<Integer> superclasses = params.getLeftSetSuperclass("Instrument", instr);
                for(int thisClass: superclasses){
                    if(superclassesFound.contains(thisClass)){
                        return true;
                    }else{
                        superclassesFound.add(thisClass);
                    }
                }
            }
            return false;
        }
    }
}
