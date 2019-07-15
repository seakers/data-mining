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
import ifeed.problem.assigning.filters.InOrbit;

import java.util.*;

public class InOrbitsInstrGeneralizer extends AbstractGeneralizationOperator {

    protected int selectedOrbit;
    protected Set<Integer> selectedInstruments;
    protected int selectedClass;
    protected AbstractFilter newFilter;
    protected Literal newLiteral;
    protected List<Connective> targetParentNodes;
    protected List<AbstractFilter> filtersToBeModified;

    @Override
    public void initialize(){
        this.selectedOrbit = -1;
        this.selectedInstruments = new HashSet<>();
        this.selectedClass = -1;
        this.newLiteral = null;
        this.filtersToBeModified = new ArrayList<>();
        this.targetParentNodes = new ArrayList<>();
        this.newFilter = null;
    }

    public InOrbitsInstrGeneralizer(BaseParams params, AbstractMOEABase base) {
        super(params, base, LogicalConnectiveType.OR);
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

        this.targetParentNodes = new ArrayList<>();

        InOrbit constraintSetter = (InOrbit) constraintSetterAbstract;

        this.selectedOrbit = constraintSetter.getOrbit();

        // Check all shared instruments, so that they are not used during the generalization process
        Set<Integer> sharedInstruments = new HashSet<>();
        for(int i: constraintSetter.getInstruments()){
            for(AbstractFilter filter: matchingFilters){
                if(((InOrbit)filter).getInstruments().contains(i)){
                    sharedInstruments.add(i);
                }
            }
        }

        List<AbstractFilter> allFilters = new ArrayList<>();
        allFilters.add(constraintSetterAbstract);
        for(AbstractFilter filter: matchingFilters){
            allFilters.add(filter);
        }

        // Count the number of appearances of each instrument class
        Map<Integer, Set<Integer>> instrumentClass2FilterMap = new HashMap<>();
        Map<Integer, Set<Integer>> instrumentClass2InstanceMap = new HashMap<>();
        for(int i = 0; i < allFilters.size(); i++){
            InOrbit filter = (InOrbit) allFilters.get(i);
            for(int instr: filter.getInstruments()){

                if(sharedInstruments.contains(instr)){ // skip all shared instruments
                    continue;
                }

                for(int c: params.getLeftSetSuperclass(instr, true)){
                    Set<Integer> correspondingFilterSet;
                    Set<Integer> correspondingInstanceSet;
                    if(instrumentClass2FilterMap.containsKey(c)){
                        correspondingFilterSet = instrumentClass2FilterMap.get(c);
                        correspondingInstanceSet = instrumentClass2InstanceMap.get(c);
                    }else{
                        if(i == 0){
                            correspondingFilterSet = new HashSet<>();
                            correspondingInstanceSet = new HashSet<>();
                        }else{
                            continue;
                        }
                    }
                    correspondingFilterSet.add(i);
                    correspondingInstanceSet.add(instr);
                    instrumentClass2FilterMap.put(c, correspondingFilterSet);
                    instrumentClass2InstanceMap.put(c, correspondingInstanceSet);
                }
            }
        }

        // Find the most frequent instrument class and its associated instances
        List<Integer> mostFrequentInstrumentClass = new ArrayList<>();
        int highestFrequency = 0;
        for(int cl1: instrumentClass2FilterMap.keySet()){

            if(super.getRestrictedVariables().contains(cl1)){
                continue;

            }else if(instrumentClass2FilterMap.get(cl1).size() > highestFrequency){
                highestFrequency = instrumentClass2FilterMap.get(cl1).size();
                mostFrequentInstrumentClass = new ArrayList<>();
                mostFrequentInstrumentClass.add(cl1);

            }else if(instrumentClass2FilterMap.get(cl1).size() == highestFrequency){

                boolean skip = false;
                Set<Integer> classesToBeRemoved = new HashSet<>();
                for(int cl2: mostFrequentInstrumentClass){
                    if(params.getLeftSetSuperclass(cl2).contains(cl1)){
                        // cl1 is a superclass of cl2 -> skip cl1
                        skip = true;
                    }else if(params.getLeftSetSuperclass(cl1).contains(cl2)){
                        // cl2 is a superclass of cl1 -> remove cl2
                        classesToBeRemoved.add(cl2);
                    }
                }

                if(!skip){
                    mostFrequentInstrumentClass.removeAll(classesToBeRemoved);
                    mostFrequentInstrumentClass.add(cl1);
                }
            }
        }

        Collections.shuffle(mostFrequentInstrumentClass);
        this.selectedClass = mostFrequentInstrumentClass.get(0);

        // Remove the selected class from future search, in order to do exhaustive search
        super.addVariableRestriction(this.selectedClass);
        if(super.getRestrictedVariables().size() >= mostFrequentInstrumentClass.size()){
            super.setExhaustiveSearchFinished();
        }

        Set<Integer> instanceSet = instrumentClass2InstanceMap.get(this.selectedClass);
        this.selectedInstruments = instanceSet;

        // Find the class that covers
        List<Integer> coveringClasses = params.getLeftSetClassesCoveringGivenIndividuals(instanceSet, true);
        if(coveringClasses.size() > 1){ // Select the class with the minimum number of instances
            int minNumInstances = 99;
            int minNumInstanceClass = -1;
            for(int c: coveringClasses){
                int numInstances = params.getLeftSetInstantiation(c).size();
                if(numInstances < minNumInstances){
                    minNumInstances = numInstances;
                    minNumInstanceClass = c;
                }
            }
            this.selectedClass = minNumInstanceClass;
        }

        // Remove nodes whose instruments are in the selected class
        filtersToBeModified = new ArrayList<>();
        for(AbstractFilter filter: allFilters){
            boolean subclassInstrumentFound = false;
            for(int i: ((InOrbit) filter).getInstruments()){
                if(params.getLeftSetSuperclass(i).contains(this.selectedClass)){
                    subclassInstrumentFound = true;
                    break;
                }
            }

            if(subclassInstrumentFound){
                // Remove matching literals
                Literal literal = nodes.get(filter);
                parent.removeNode(literal);
                filtersToBeModified.add(filter);
            }
        }

        boolean sharedByAll = false;
        if(parent.getChildNodes().isEmpty()){
            sharedByAll = true;
        }

        // Create new feature
        newFilter = new InOrbit(params, this.selectedOrbit, this.selectedClass);
        Feature newFeature = this.base.getFeatureFetcher().fetch(newFilter);

        if(sharedByAll){
            Connective grandParent = (Connective) parent.getParent();

            if(grandParent == null){ // Parent node is the root node since it doesn't have a parent node
                super.base.getFeatureHandler().createNewRootNode(root);
                grandParent = root;

                // Store the newly generated node to parent
                parent = grandParent.getConnectiveChildren().get(0);
            }
            grandParent.addLiteral(newFeature.getName(), newFeature.getMatches());
            this.targetParentNodes.add(grandParent);

        }else{
            for(int i = 0; i < filtersToBeModified.size(); i++){
                Connective newBranch = new Connective(LogicalConnectiveType.AND);
                newBranch.addLiteral(newFeature.getName(), newFeature.getMatches());

                parent.addBranch(newBranch);
                this.targetParentNodes.add(newBranch);
            }
        }

        for(int i = 0; i < filtersToBeModified.size(); i++){
            AbstractFilter filter = filtersToBeModified.get(i);
            int orbit = ((InOrbit) filter).getOrbit();
            Multiset<Integer> instruments  = ((InOrbit) filter).getInstruments();

            if(instruments.size() > 1){
                ArrayList<Integer> instrumentList = new ArrayList<>(instruments);

                Set<Integer> instrumentsToBeRemoved = new HashSet<>();
                for(int instr: instrumentList){
                    if(params.getLeftSetSuperclass(instr).contains(this.selectedClass)){
                        instrumentsToBeRemoved.add(instr);
                    }
                }
                instrumentList.removeAll(instrumentsToBeRemoved);

                AbstractFilter modifiedFilter = new InOrbit(params, orbit, Utils.intCollection2Array(instrumentList));
                Feature modifiedFeature = base.getFeatureFetcher().fetch(modifiedFilter);

                if(!instrumentList.isEmpty()){
                    if(sharedByAll){
                        parent.addLiteral(modifiedFeature.getName(), modifiedFeature.getMatches());
                    }else{
                        this.targetParentNodes.get(i).addLiteral(modifiedFeature.getName(), modifiedFeature.getMatches());
                    }
                }
            }
        }

        if(parent.getChildNodes().isEmpty()){
            Connective grandParent = (Connective) parent.getParent();
            grandParent.removeNode(parent);
        }
    }

    @Override
    public String getDescription(){

        Params params = (Params) this.params;
        StringBuilder sb = new StringBuilder();
        sb.append("Generalize ");
        sb.append("\"Either one of the instrument sets ");

        StringJoiner instrumentSetNamesJoiner = new StringJoiner(", ");
        for(int i = 0; i < filtersToBeModified.size(); i++){
            InOrbit filter = (InOrbit) filtersToBeModified.get(i);
            List<Integer> generalizedInstruments = new ArrayList<>();
            Multiset<Integer> instruments = filter.getInstruments();
            for(int instr:instruments){
                if(this.selectedInstruments.contains(instr)){
                    generalizedInstruments.add(instr);
                }
            }

            if(generalizedInstruments.isEmpty()){
                continue;
            }else{
                StringJoiner instrumentNamesJoiner = new StringJoiner(", ");
                for(int instr: generalizedInstruments){
                    instrumentNamesJoiner.add(params.getLeftSetEntityName(instr));
                }
                instrumentSetNamesJoiner.add("{" + instrumentNamesJoiner.toString()+ "}");
            }
        }
        sb.append(instrumentSetNamesJoiner.toString());
        sb.append(" are assigned to orbit " + params.getRightSetEntityName(this.selectedOrbit));
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
        private int orbit;
        private Multiset<Integer> instruments;

        public FilterFinder(Params params){
            super(InOrbit.class, InOrbit.class);
            this.params = params;
            this.clearConstraints();
        }

        @Override
        public void setConstraints(AbstractFilter constraintSetter){
            this.orbit = ((InOrbit)constraintSetter).getOrbit();
            this.instruments = ((InOrbit)constraintSetter).getInstruments();
        }

        @Override
        public void clearConstraints(){
            this.orbit = -1;
            this.instruments = null;
        }

        @Override
        public boolean check(AbstractFilter filterToTest){

            int orb1 = this.orbit;
            Multiset<Integer> inst1 = this.instruments;

            InOrbit filter = (InOrbit) filterToTest;
            int orb2 = filter.getOrbit();
            Multiset<Integer> inst2 = filter.getInstruments();

            // Orbit variable should not be a high-level class
            if(orb1 >= params.getRightSetCardinality()){
                return false;
            }

            // Check if the orbit variable is shared by the two filters
            if(orb1 != orb2){
                return false;
            }

            // Get all instruments shared
            Set<Integer> sharedInstruments = new HashSet<>();
            sharedInstruments.addAll(inst1);
            sharedInstruments.retainAll(inst2);

            // Check if any of the instruments in two filters are in the same instrument class
            Set<Integer> superclasses1 = new HashSet<>();
            for(int i: inst1){
                if(!sharedInstruments.contains(i)){
                    superclasses1.addAll(params.getLeftSetSuperclass(i, true));
                }
            }
            Set<Integer> superclasses2 = new HashSet<>();
            for(int i: inst2){
                if(!sharedInstruments.contains(i)){
                    superclasses2.addAll(params.getLeftSetSuperclass(i, true));
                }
            }

            superclasses1.retainAll(superclasses2);
            if(superclasses1.isEmpty()){
                return false;
            }
            return true;
        }
    }
}
