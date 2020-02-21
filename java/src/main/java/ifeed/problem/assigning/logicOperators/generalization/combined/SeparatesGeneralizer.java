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
import ifeed.mining.moea.operators.AbstractExhaustiveSearchOperator;
import ifeed.problem.assigning.Params;
import ifeed.problem.assigning.filters.Separate;

import java.util.*;

public class SeparatesGeneralizer extends AbstractExhaustiveSearchOperator {

    protected int selectedClass;
    protected int selectedInstrument;
    protected Connective targetParentNode;
    protected List<AbstractFilter> filtersToBeModified;
    protected AbstractFilter newFilter;
    protected List<AbstractFilter> modifiedFilters;
    protected Literal newLiteral;
    protected Feature newFeature;

    public SeparatesGeneralizer(BaseParams params, AbstractMOEABase base) {
        super(params, base, LogicalConnectiveType.AND, 2);
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
    public boolean apply(Connective root,
                      Connective parent,
                      AbstractFilter constraintSetterAbstract,
                      Set<AbstractFilter> matchingFilters,
                      Map<AbstractFilter, Literal> nodes
    ){
        this.initialize();
        Params params = (Params) super.params;
        this.targetParentNode = parent;

        Separate constraintSetter = (Separate) constraintSetterAbstract;

        Set<AbstractFilter> allFilters = new HashSet<>();
        allFilters.add(constraintSetterAbstract);
        allFilters.addAll(matchingFilters);

        List<Integer> sharedInstruments = new ArrayList<>();
        for(AbstractFilter filter: matchingFilters){
            for(int inst: ((Separate) filter).getInstruments()){
                if(constraintSetter.getInstruments().contains(inst)){
                    if(!super.checkIfVisited(inst)){
                        sharedInstruments.add(inst);
                    }
                }
            }
        }

        if(sharedInstruments.isEmpty()){
            super.setSearchFinished();
            return false;
        }

        // Shuffle instrument orders
        this.selectedInstrument = sharedInstruments.get(0);

        // Get all classes that are shared
        Map<Integer, Set<AbstractFilter>> classToFilterMap = new HashMap<>();
        for(int inst: constraintSetter.getInstruments()){
            if(inst == this.selectedInstrument){
                continue;
            }

            Set<Integer> instrumentClasses = params.getLeftSetSuperclass(inst);
            for(int c: instrumentClasses){
                if(super.checkIfVisited(this.selectedInstrument, c)){
                    continue;
                }else{
                    Set<AbstractFilter> tempFilterSet = new HashSet<>();
                    tempFilterSet.add(constraintSetter);
                    classToFilterMap.put(c, tempFilterSet);
                }
            }
        }

        for(AbstractFilter filter: matchingFilters){
            for(int inst: ((Separate) filter).getInstruments()){
                if(inst == this.selectedInstrument){
                    continue;

                }else{
                    Set<Integer> instrumentClasses = params.getLeftSetSuperclass(inst);
                    for(int c: instrumentClasses){
                        if(classToFilterMap.containsKey(c)){
                            classToFilterMap.get(c).add(filter);
                        }
                    }
                }
            }
        }

        if(classToFilterMap.isEmpty()){
            super.setVisitedVariable(this.selectedInstrument);
            return false;
        }

        this.selectedClass = classToFilterMap.keySet().iterator().next();

        // Remove the selected instrument-class combination from future search, in order to do exhaustive search
        super.setVisitedVariable(this.selectedInstrument, this.selectedClass);

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
        return true;
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
            Set<Integer> sharedInstruments = new HashSet<>(inst1);
            sharedInstruments.retainAll(inst2);
            if(sharedInstruments.isEmpty()){
                return false;
            }

            // Check if unshared instruments from both filters share a class
            boolean foundSharedClass = false;
            Set<Integer> savedClasses = new HashSet<>();
            for(int i: inst1){
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
