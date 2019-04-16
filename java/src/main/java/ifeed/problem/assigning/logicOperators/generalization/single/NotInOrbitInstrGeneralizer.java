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

public class NotInOrbitInstrGeneralizer extends AbstractLogicOperator {

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

        constraintSetter = (NotInOrbit) constraintSetterAbstract;
        Multiset<Integer> instruments = constraintSetter.getInstruments();

        Map<Integer, Set<Integer>> instrumentClass2InstanceMap = new HashMap<>();
        for(int instr: instruments.elementSet()){
            Set<Integer> superclasses = params.getLeftSetSuperclass(instr);

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

//        System.out.println(instrumentClass2InstanceMap);

        // Find the most frequent instrument
        List<Integer> mostFrequentClass = new ArrayList<>();
        int highestFrequency = 0;
        for(int cl: instrumentClass2InstanceMap.keySet()){
            if(instrumentClass2InstanceMap.get(cl).size() > highestFrequency){
                highestFrequency = instrumentClass2InstanceMap.get(cl).size();
                mostFrequentClass = new ArrayList<>();
                mostFrequentClass.add(cl);

            }else if(instrumentClass2InstanceMap.get(cl).size() == highestFrequency){
                mostFrequentClass.add(cl);
            }
        }

//        System.out.println("most frequent classes:" + mostFrequentClass);

        // Randomly select one of the classes
        Collections.shuffle(mostFrequentClass);
        this.selectedClass = mostFrequentClass.get(0);
        Set<Integer> instanceSet = instrumentClass2InstanceMap.get(this.selectedClass);

        this.selectedInstruments = instanceSet;

//        System.out.println("selected class: " + this.selectedClass + ", instance: " + instanceSet);

        List<Integer> coveringClasses = params.getLeftSetClassesCoveringGivenIndividuals(instanceSet);

//        System.out.println("covering classes:" + coveringClasses);

        if(coveringClasses.size() > 1){
            // Create a new instrument class
            String newClassName = params.getLeftSetEntityName(coveringClasses.get(0));
            for (int i = 1; i < coveringClasses.size(); i++){
                String classToBeCombined = params.getLeftSetEntityName(coveringClasses.get(i));

                if(newClassName.contains(classToBeCombined) || classToBeCombined.contains(newClassName)){
                    continue;
                }

                newClassName = params.combineLeftSetClasses(newClassName, classToBeCombined);
            }
            this.selectedClass = params.getLeftSetEntityIndex(newClassName);

        }else{
            this.selectedClass = coveringClasses.get(0);
        }


        Multiset<Integer> modifiedInstrumentSet = HashMultiset.create();
        for(int instr: instruments){
            if(params.getLeftSetSuperclass(instr).contains(this.selectedClass)){
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
                Set<Integer> superclasses = params.getLeftSetSuperclass(instr);
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
