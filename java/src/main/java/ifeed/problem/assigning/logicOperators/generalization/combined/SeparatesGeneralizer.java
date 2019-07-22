package ifeed.problem.assigning.logicOperators.generalization.combined;

import com.google.common.collect.Multiset;
import ifeed.Utils;
import ifeed.feature.Feature;
import ifeed.feature.logic.Connective;
import ifeed.feature.logic.Literal;
import ifeed.feature.logic.LogicalConnectiveType;
import ifeed.filter.AbstractFilter;
import ifeed.filter.AbstractFilterFinder;
import ifeed.local.params.BaseParams;
import ifeed.mining.moea.AbstractMOEABase;
import ifeed.mining.moea.operators.AbstractGeneralizationOperator;
import ifeed.mining.moea.operators.AbstractLogicOperator;
import ifeed.problem.assigning.Params;
import ifeed.problem.assigning.filters.AbstractGeneralizableFilter;
import ifeed.problem.assigning.filters.Separate;

import java.util.*;

public class SeparatesGeneralizer extends AbstractGeneralizationOperator {

    protected int selectedClass;
    protected int selectedInstrument;
    protected Connective targetParentNode;
    protected List<AbstractFilter> filtersToBeModified;
    protected AbstractFilter newFilter;
    protected List<AbstractFilter> modifiedFilters;
    protected Literal newLiteral;
    protected Feature newFeature;

    public SeparatesGeneralizer(BaseParams params, AbstractMOEABase base) {
        super(params, base, LogicalConnectiveType.AND);
    }

    @Override
    public void initialize(){
        this.selectedClass = -1;
        this.selectedInstrument = -1;
        this.targetParentNode = null;
        this.filtersToBeModified = new ArrayList<>();
        this.newFilter = null;
        this.modifiedFilters = new ArrayList<>();
        this.newLiteral = null;
        this.newFeature = null;
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

        this.targetParentNode = parent;

        Set<AbstractFilter> allFilters = new HashSet<>();
        allFilters.add(constraintSetterAbstract);
        allFilters.addAll(matchingFilters);

        // Count the number of appearances of each instrument
        Map<Integer, Integer> instrumentCounter = new HashMap<>();
        for(AbstractFilter filter: allFilters){
            for(int inst: ((Separate) filter).getInstruments()){
                if(instrumentCounter.containsKey(inst)){
                    instrumentCounter.put(inst, instrumentCounter.get(inst) + 1);
                }else{
                    instrumentCounter.put(inst, 1);
                }
            }
        }

        // Shuffle instrument orders
        List<Integer> keySet = new ArrayList<>();
        keySet.addAll(instrumentCounter.keySet());
        Collections.shuffle(keySet);

        // Find the most frequent instrument
        int mostFrequentInstrument = -1;
        int highestFrequency = 0;
        for(int inst: keySet){
            if(super.isExhaustiveSearchFinished(inst)){
                continue;
            }

            if(instrumentCounter.get(inst) > highestFrequency){
                highestFrequency = instrumentCounter.get(inst);
                mostFrequentInstrument = inst;
            }
        }

        if(mostFrequentInstrument == -1){
            super.setExhaustiveSearchFinished();
            return;
        }

        this.selectedInstrument = mostFrequentInstrument;

        // Count the number of appearances of each class
        Map<Integer, Set<AbstractFilter>> classToFilterMap = new HashMap<>();
        Map<Integer, Integer> classCounter = new HashMap<>();
        for(AbstractFilter filter: allFilters){
            for(int inst: ((Separate) filter).getInstruments()){
                if(inst == this.selectedInstrument){
                    continue;

                }else{
                    Set<Integer> instrumentClasses = params.getLeftSetSuperclass(inst);
                    for(int c: instrumentClasses){
                        if(classCounter.containsKey(c)){
                            classCounter.put(c, classCounter.get(c) + 1);
                            classToFilterMap.get(c).add(filter);

                        }else{
                            classCounter.put(c, 1);
                            Set<AbstractFilter> tempFilterSet = new HashSet<>();
                            tempFilterSet.add(filter);
                            classToFilterMap.put(c, tempFilterSet);
                        }
                    }
                }
            }
        }

        // Shuffle instrument orders
        keySet = new ArrayList<>();
        keySet.addAll(classCounter.keySet());
        Collections.shuffle(keySet);

        // Find the most frequent instrument class
        int mostFrequentClass = -1;
        highestFrequency = 0;
        for(int c: keySet){
            if(super.getRestrictedVariableCombination(this.selectedInstrument).contains(c)){
                continue;
            }

            if(classCounter.get(c) > highestFrequency){
                highestFrequency = classCounter.get(c);
                mostFrequentClass = c;
            }
        }

        if(highestFrequency <= 1){
            super.setExhaustiveSearchFinished(this.selectedInstrument);
            return;
        }

        this.selectedClass = mostFrequentClass;

        // Remove the selected instrument-class combination from future search, in order to do exhaustive search
        super.addVariableRestriction(this.selectedInstrument, this.selectedClass);

        // Remove nodes that share the selected instrument and the selected class
        filtersToBeModified = new ArrayList<>();
        for(AbstractFilter filter: allFilters){
            Multiset<Integer> testInstr = ((Separate) filter).getInstruments();

            if(!testInstr.contains(this.selectedInstrument)){
                continue;
            }

            boolean containsClass = false;
            for(int instr: testInstr){
                if(instr == this.selectedInstrument){
                    continue;
                }
                Set<Integer> instrumentClasses = params.getLeftSetSuperclass(instr);
                if(instrumentClasses.contains(this.selectedClass)){
                    containsClass = true;
                    break;
                }
            }
            if(!containsClass){
                continue;
            }

            // Remove matching literals
            Literal literal = nodes.get(filter);
            parent.removeNode(literal);
            filtersToBeModified.add(filter);
        }

        // Create new feature
        Set<Integer> separateInstruments = new HashSet<>();
        separateInstruments.add(this.selectedClass);
        separateInstruments.add(this.selectedInstrument);

        this.newFilter = new Separate(params, separateInstruments);
        this.newFeature = this.base.getFeatureFetcher().fetch(newFilter);
        this.newLiteral = new Literal(newFeature.getName(), newFeature.getMatches());
        parent.addLiteral(this.newLiteral);

        modifiedFilters = new ArrayList<>();
        for(int i = 0; i < filtersToBeModified.size(); i++){
            AbstractFilter filter = filtersToBeModified.get(i);
            Multiset<Integer> instruments  = ((Separate) filter).getInstruments();

            if(instruments.size() > 2){
                int otherInstrument = -1;
                for(int instr: instruments){
                    Set<Integer> instrumentClasses = params.getLeftSetSuperclass(instr);
                    if(instrumentClasses.contains(this.selectedClass)){
                        continue;
                    }else if(instr == this.selectedInstrument){
                        continue;
                    }else{
                        otherInstrument = instr;
                    }
                }

                if(otherInstrument != -1){
                    for(int inst: instruments){
                        if(inst == otherInstrument){
                            continue;
                        }else{
                            ArrayList<Integer> instrumentList = new ArrayList<>();
                            instrumentList.add(otherInstrument);
                            instrumentList.add(inst);
                            AbstractFilter modifiedFilter = new Separate(params, Utils.intCollection2Array(instrumentList));
                            modifiedFilters.add(modifiedFilter);
                            Feature modifiedFeature = base.getFeatureFetcher().fetch(modifiedFilter);

                            this.targetParentNode.addLiteral(modifiedFeature.getName(), modifiedFeature.getMatches());
                        }
                    }
                }
            }
        }
    }

    @Override
    public String getDescription(){

        Params params = (Params) this.params;

        StringBuilder sb = new StringBuilder();
        sb.append("Generalize ");
        sb.append("\"Instruments in each set ");

        StringJoiner instrumentSetJoiner = new StringJoiner(", ");
        for(AbstractFilter filter: this.filtersToBeModified){
            Multiset<Integer> instruments = ((Separate) filter).getInstruments();
            StringJoiner instrumentNamesJoiner = new StringJoiner(", ");
            for(int instr: instruments){
                instrumentNamesJoiner.add(params.getLeftSetEntityName(instr));
            }
            instrumentSetJoiner.add("{"+ instrumentNamesJoiner.toString() +"}");
        }
        sb.append(instrumentSetJoiner.toString());
        sb.append(" are not assigned to the same orbit\"");
        sb.append(" to ");

        sb.append("\"Instruments in each set ");
        List<AbstractFilter> tempFilterList = new ArrayList<>();
        tempFilterList.add(this.newFilter);
        tempFilterList.addAll(this.modifiedFilters);

        instrumentSetJoiner = new StringJoiner(", ");
        for(AbstractFilter filter: tempFilterList){
            Multiset<Integer> instruments = ((Separate) filter).getInstruments();
            StringJoiner instrumentNamesJoiner = new StringJoiner(", ");
            for(int instr: instruments){
                instrumentNamesJoiner.add(params.getLeftSetEntityName(instr));
            }
            instrumentSetJoiner.add("{"+ instrumentNamesJoiner.toString() +"}");
        }
        sb.append(instrumentSetJoiner.toString());
        sb.append(" are not assigned to the same orbit\"");
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
            super(Separate.class, Separate.class);
            this.params = params;
            this.clearConstraints();
        }

        @Override
        public void setConstraints(AbstractFilter constraintSetter){
            this.instruments = ((Separate)constraintSetter).getInstruments();
        }

        @Override
        public void clearConstraints(){
            this.instruments = null;
        }

        @Override
        public boolean check(AbstractFilter filterToTest){
            Multiset<Integer> inst1 = this.instruments;
            Multiset<Integer> inst2 = ((Separate) filterToTest).getInstruments();

            // Check if two literals share at least one common instrument
            Set<Integer> sharedInstruments = new HashSet<>();
            for(int inst:inst2){
                if(inst1.contains(inst)) {
                    sharedInstruments.add(inst);
                }
            }
            if(sharedInstruments.isEmpty()){
                return false;
            }

            // Check if unshared instruments from both filters share a class
            boolean foundSharedClass = false;
            Set<Integer> savedClasses = new HashSet<>();
            for(int i:inst1){
                if(sharedInstruments.contains(i)){
                    continue;
                }else{
                    Set<Integer> instrumentClasses = params.getLeftSetSuperclass(i);
                    savedClasses.addAll(instrumentClasses);
                }
            }

            for(int i:inst2){
                if(sharedInstruments.contains(i)){
                    continue;
                }else{
                    Set<Integer> instrumentClasses = params.getLeftSetSuperclass(i);
                    instrumentClasses.retainAll(savedClasses);
                    if(!instrumentClasses.isEmpty()){
                        foundSharedClass = true;
                        break;
                    }
                }
                if(foundSharedClass){
                    break;
                }
            }

            if(foundSharedClass){
                return true;
            }else{
                return false;
            }
        }
    }
}
