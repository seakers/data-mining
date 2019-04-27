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

public class InOrbitsOrbGeneralizer extends AbstractGeneralizationOperator {

    protected int selectedOrbit;
    protected int selectedInstrument;
    protected int selectedClass;

    protected Literal newLiteral;
    protected List<Connective> targetParentNodes;
    protected List<AbstractFilter> filtersToBeModified;
    protected AbstractFilter newFilter;

    public InOrbitsOrbGeneralizer(BaseParams params, AbstractMOEABase base) {
        super(params, base, LogicalConnectiveType.OR);
    }

    @Override
    public void apply(Connective root,
                      Connective parent,
                      AbstractFilter constraintSetterAbstract,
                      Set<AbstractFilter> matchingFilters,
                      Map<AbstractFilter, Literal> nodes
    ){
        Params params = (Params) super.params;

        InOrbit constraintSetter = (InOrbit) constraintSetterAbstract;

        this.targetParentNodes = new ArrayList<>();
        this.selectedOrbit = constraintSetter.getOrbit();

        // Count the number of appearances of each instrument
        Map<Integer, Integer> instrumentCounter = new HashMap<>();
        for(int i: constraintSetter.getInstruments()){
            instrumentCounter.put(i, 1);
        }
        for(AbstractFilter filter: matchingFilters){
            for(int i: ((InOrbit) filter).getInstruments()){
                if(instrumentCounter.containsKey(i)){
                    instrumentCounter.put(i, instrumentCounter.get(i) + 1);
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
            }else if(instrumentCounter.get(inst) > highestFrequency){
                highestFrequency = instrumentCounter.get(inst);
                mostFrequentInstrument = inst;
            }
        }
        if(mostFrequentInstrument == -1){
            super.setExhaustiveSearchFinished();
            return;
        }
        this.selectedInstrument = mostFrequentInstrument;

        // Find all filters that contains the selected instrument
        List<AbstractFilter> filtersWithSelectedInstrument = new ArrayList<>();
        for(AbstractFilter filter: matchingFilters){
            if(((InOrbit) filter).getInstruments().contains(this.selectedInstrument)){
                filtersWithSelectedInstrument.add(filter);
            }
        }
        Collections.shuffle(filtersWithSelectedInstrument);

        // Count the number of appearances of each orbit class
        Map<Integer, Integer> orbitClassCounter = new HashMap<>();
        Set<Integer> constraintSetterOrbitSuperclasses = params.getRightSetSuperclass(this.selectedOrbit);
        for(int c: constraintSetterOrbitSuperclasses){
            orbitClassCounter.put(c, 1);
        }
        for(AbstractFilter filter: filtersWithSelectedInstrument){
            int orb = ((InOrbit) filter).getOrbit();
            Set<Integer> tempClassSet = params.getRightSetSuperclass(orb);
            for(int o: tempClassSet){
                if(orbitClassCounter.containsKey(o)){
                    orbitClassCounter.put(o, orbitClassCounter.get(o) + 1);
                }
            }
        }

        // Find the most frequent orbit class
        List<Integer> mostFrequentOrbitClass = new ArrayList<>();
        highestFrequency = 0;
        for(int cl1: orbitClassCounter.keySet()){

            if(super.getRestrictedVariableCombination(this.selectedInstrument).contains(cl1)){
                continue;

            } else if(orbitClassCounter.get(cl1) > highestFrequency){
                highestFrequency = orbitClassCounter.get(cl1);
                mostFrequentOrbitClass = new ArrayList<>();
                mostFrequentOrbitClass.add(cl1);

            }else if(orbitClassCounter.get(cl1) == highestFrequency){

                boolean skip = false;
                Set<Integer> classesToBeRemoved = new HashSet<>();
                for(int cl2: mostFrequentOrbitClass){
                    if(params.getRightSetSuperclass(cl2).contains(cl1)){
                        // cl1 is a superclass of cl2 -> skip cl1
                        skip = true;
                    }else if(params.getRightSetSuperclass(cl1).contains(cl2)){
                        // cl2 is a superclass of cl1 -> remove cl2
                        classesToBeRemoved.add(cl2);
                    }
                }
                if(!skip){
                    mostFrequentOrbitClass.removeAll(classesToBeRemoved);
                    mostFrequentOrbitClass.add(cl1);
                }
            }
        }

        Collections.shuffle(mostFrequentOrbitClass);
//        if(mostFrequentOrbitClass.size() > 1){ // Select the class with the minimum number of instances
//            int minNumInstances = 99;
//            int minNumInstanceClass = -1;
//            for(int c: mostFrequentOrbitClass){
//                int numInstances = params.getLeftSetInstantiation(c).size();
//                if(numInstances < minNumInstances){
//                    minNumInstances = numInstances;
//                    minNumInstanceClass = c;
//                }
//            }
//            this.selectedClass = minNumInstanceClass;
//
//        }else{
//            this.selectedClass = mostFrequentOrbitClass.get(0);
//        }
//
//        // Remove the selected class from future search, in order to do exhaustive search
//        super.addVariableRestriction(this.selectedInstrument, this.selectedClass);
//        if(super.getRestrictedVariableCombination(this.selectedInstrument).size() >= mostFrequentOrbitClass.size()){
//            super.setExhaustiveSearchFinished(this.selectedInstrument);
//        }

        if(mostFrequentOrbitClass.size() > 1){
            // Create a new orbit class
            String newClassName = params.getRightSetEntityName(mostFrequentOrbitClass.get(0));
            for (int i = 1; i < mostFrequentOrbitClass.size(); i++){
                String classToBeCombined = params.getRightSetEntityName(mostFrequentOrbitClass.get(i));
                newClassName = params.combineRightSetClasses(newClassName, classToBeCombined);
            }
            this.selectedClass = params.getRightSetEntityIndex(newClassName);
        }else{
            this.selectedClass = mostFrequentOrbitClass.get(0);
        }
        super.setExhaustiveSearchFinished(this.selectedInstrument);

        List<AbstractFilter> allFilters = new ArrayList<>();
        allFilters.add(constraintSetter);
        allFilters.addAll(filtersWithSelectedInstrument);

        // Remove nodes whose orbits are in the selected class
        filtersToBeModified = new ArrayList<>();
        for(AbstractFilter filter: allFilters){
            InOrbit inOrbit = (InOrbit) filter;
            int orbit = inOrbit.getOrbit();

            if(params.getRightSetSuperclass(orbit).contains(this.selectedClass)){
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
        newFilter = new InOrbit(params, this.selectedClass, this.selectedInstrument);
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
                int selectedArgumentIndex = instrumentList.indexOf(this.selectedInstrument);
                instrumentList.remove(selectedArgumentIndex);

                AbstractFilter modifiedFilter = new InOrbit(params, orbit, Utils.intCollection2Array(instrumentList));
                Feature modifiedFeature = base.getFeatureFetcher().fetch(modifiedFilter);

                if(!instruments.isEmpty()){
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
        sb.append("\"Instrument " + params.getLeftSetEntityName(this.selectedInstrument));
        sb.append(" is assigned to either one of the orbits {");

        StringJoiner orbitNamesJoiner = new StringJoiner(", ");
        for(AbstractFilter filter: this.filtersToBeModified){
            int orbit = ((InOrbit) filter).getOrbit();
            orbitNamesJoiner.add(params.getRightSetEntityName(orbit));
        }
        sb.append(orbitNamesJoiner.toString() + "}\"");
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

            boolean sharesInstrument = false;

            // Check if at least one common instrument variable is shared by the two filters
            for(int inst:inst2){
                if(inst1.contains(inst)) {
                    sharesInstrument = true;
                    break;
                }
            }
            if(!sharesInstrument){
                return false;
            }

            // Check if orb1 and orb2 are in the same orbit class
            Set<Integer> orb1Classes = params.getRightSetSuperclass(orb1);
            Set<Integer> orb2Classes = params.getRightSetSuperclass(orb2);
            orb1Classes.retainAll(orb2Classes);
            if(orb1Classes.isEmpty()){
                return false;
            }

            if(orb1 == orb2){
                return false;
            }

            return true;
        }
    }
}
