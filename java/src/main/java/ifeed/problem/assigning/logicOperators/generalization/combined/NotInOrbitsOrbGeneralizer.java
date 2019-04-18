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
import ifeed.mining.moea.operators.AbstractLogicOperator;
import ifeed.problem.assigning.Params;
import ifeed.problem.assigning.filters.NotInOrbit;
import java.util.*;

public class NotInOrbitsOrbGeneralizer extends AbstractLogicOperator {

    protected int selectedOrbit;
    protected int selectedClass;
    protected Literal newLiteral;

    protected int selectedInstrument;
    protected Connective targetParentNode;

    protected List<AbstractFilter> filtersToBeModified;
    protected AbstractFilter newFilter;

    protected Set<Integer> restrictedOrbits;

    public NotInOrbitsOrbGeneralizer(BaseParams params, AbstractMOEABase base) {
        super(params, base, LogicalConnectiveType.AND);
    }

    @Override
    public void apply(Connective root,
                      Connective parent,
                      AbstractFilter constraintSetterAbstract,
                      Set<AbstractFilter> matchingFilters,
                      Map<AbstractFilter, Literal> nodes
    ){

        Params params = (Params) super.params;

        List<AbstractFilter> allFilters = new ArrayList<>();
        allFilters.add(constraintSetterAbstract);
        for(AbstractFilter filter: matchingFilters){
            allFilters.add(filter);
        }

        // Count the number of appearances of each instrument
        Map<Integer, Integer> sharedInstrumentCounter = new HashMap<>();
        for(AbstractFilter filter: allFilters){
            Multiset<Integer> testInstr = ((NotInOrbit) filter).getInstruments();
            for(int i: testInstr){

                if(!((NotInOrbit) constraintSetterAbstract).getInstruments().contains(i)){
                    // Include only the instruments that are included in the constraint setter filter
                    continue;
                }else{
                    if(sharedInstrumentCounter.containsKey(i)){
                        sharedInstrumentCounter.put(i, sharedInstrumentCounter.get(i) + 1);
                    }else{
                        sharedInstrumentCounter.put(i, 1);
                    }
                }
            }
        }

        // Shuffle instrument orders
        List<Integer> keySet = new ArrayList<>();
        keySet.addAll(sharedInstrumentCounter.keySet());
        Collections.shuffle(keySet);

        // Find the most frequent instrument
        int mostFrequentInstrument = -1;
        int highestFrequency = 0;
        for(int inst: keySet){
            if(sharedInstrumentCounter.get(inst) > highestFrequency){
                highestFrequency = sharedInstrumentCounter.get(inst);
                mostFrequentInstrument = inst;
            }
        }
        this.selectedInstrument = mostFrequentInstrument;

        // Find all filters that contains the selected instrument
        List<AbstractFilter> tempFilters = new ArrayList<>();
        for(AbstractFilter filter: allFilters){
            if(((NotInOrbit) filter).getInstruments().contains(this.selectedInstrument)){
                tempFilters.add(filter);
            }
        }
        allFilters = tempFilters;

        this.selectedOrbit = ((NotInOrbit) constraintSetterAbstract).getOrbit();
        Set<Integer> constraintSetterOrbitSuperclasses = params.getRightSetSuperclass(this.selectedOrbit);

        // Count the number of appearances of each orbit class
        Map<Integer, Integer> orbitClassCounter = new HashMap<>();
        for(AbstractFilter filter: allFilters){
            int orb = ((NotInOrbit) filter).getOrbit();
            Set<Integer> tempClassSet = params.getRightSetSuperclass(orb);
            for(int o: tempClassSet){
                if(orbitClassCounter.containsKey(o)){
                    orbitClassCounter.put(o, orbitClassCounter.get(o) + 1);
                }else{
                    orbitClassCounter.put(o, 1);
                }
            }
        }

        // Find the most frequent orbit class
        List<Integer> mostFrequentOrbitClass = new ArrayList<>();
        highestFrequency = 0;
        for(int o: orbitClassCounter.keySet()){
            if(constraintSetterOrbitSuperclasses.contains(o)){

                if(orbitClassCounter.get(o) > highestFrequency){
                    highestFrequency = orbitClassCounter.get(o);
                    mostFrequentOrbitClass = new ArrayList<>();
                    mostFrequentOrbitClass.add(o);

                }else if(orbitClassCounter.get(o) == highestFrequency){

                    boolean skip = false;
                    Set<Integer> classesToBeRemoved = new HashSet<>();
                    for(int c: mostFrequentOrbitClass){
                        if(params.getRightSetSuperclass(c).contains(o)){
                            // o is a superclass of c -> skip o
                            skip = true;
                        }else if(params.getRightSetSuperclass(o).contains(c)){
                            // c is a superclass of o -> remove c
                            classesToBeRemoved.add(c);
                        }
                    }

                    if(!skip){
                        mostFrequentOrbitClass.removeAll(classesToBeRemoved);
                        mostFrequentOrbitClass.add(o);
                    }
                }
            }
        }

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

        // Remove nodes whose orbits are in the selected class
        filtersToBeModified = new ArrayList<>();
        restrictedOrbits = new HashSet<>();
        for(AbstractFilter filter: allFilters){

            NotInOrbit notInOrbit = (NotInOrbit) filter;
            Multiset<Integer> instruments = notInOrbit.getInstruments();
            int orbit = notInOrbit.getOrbit();

            if(params.getRightSetSuperclass(orbit).contains(this.selectedClass) && instruments.contains(this.selectedInstrument)){
                // Remove matching literals
                Literal literal = nodes.get(filter);
                parent.removeNode(literal);
                filtersToBeModified.add(filter);

                restrictedOrbits.add(orbit);
            }
        }

        // Create new feature
        this.targetParentNode = parent;
        newFilter = new NotInOrbit(params, this.selectedClass, this.selectedInstrument);
        Feature newFeature = this.base.getFeatureFetcher().fetch(newFilter);
        this.newLiteral = new Literal(newFeature.getName(), newFeature.getMatches());
        parent.addLiteral(this.newLiteral);

        for(int i = 0; i < filtersToBeModified.size(); i++){
            AbstractFilter filter = filtersToBeModified.get(i);
            int orbit = ((NotInOrbit) filter).getOrbit();
            Multiset<Integer> instruments  = ((NotInOrbit) filter).getInstruments();

            if(instruments.size() > 1){
                ArrayList<Integer> instrumentList = new ArrayList<>(instruments);
                int selectedArgumentIndex = instrumentList.indexOf(this.selectedInstrument);
                instrumentList.remove(selectedArgumentIndex);

                AbstractFilter modifiedFilter = new NotInOrbit(params, orbit, Utils.intCollection2Array(instrumentList));
                Feature modifiedFeature = base.getFeatureFetcher().fetch(modifiedFilter);

                if(!instruments.isEmpty()){
                    this.targetParentNode.addLiteral(modifiedFeature.getName(), modifiedFeature.getMatches());
                }
            }
        }
    }

    @Override
    public String getDescription(){

        Params params = (Params) this.params;

        StringBuilder sb = new StringBuilder();
        sb.append("Generalize ");
        sb.append("\"Instrument " + params.getLeftSetEntityName(this.selectedInstrument));
        sb.append(" is not assigned to any of the orbits {");

        StringJoiner orbitNamesJoiner = new StringJoiner(", ");
        for(AbstractFilter filter: this.filtersToBeModified){
            int orbit = ((NotInOrbit) filter).getOrbit();
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
            super(NotInOrbit.class, NotInOrbit.class);
            this.params = params;
            this.clearConstraints();
        }

        @Override
        public void setConstraints(AbstractFilter constraintSetter){
            this.orbit = ((NotInOrbit)constraintSetter).getOrbit();
            this.instruments = ((NotInOrbit)constraintSetter).getInstruments();
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

            NotInOrbit filter = (NotInOrbit) filterToTest;
            int orb2 = filter.getOrbit();
            Multiset<Integer> inst2 = filter.getInstruments();

            // Check if two literals share at least one common instrument
            boolean sharesAnInstrument = false;
            for(int inst:inst2){
                if(inst1.contains(inst)) {
                    sharesAnInstrument = true;
                    break;
                }
            }
            if(!sharesAnInstrument){
                return false;
            }

            // Check if orb1 and orb2 are in the same orbit class
            Set<Integer> orb1Classes = params.getRightSetSuperclass(orb1);
            Set<Integer> orb2Classes = params.getRightSetSuperclass(orb2);
            orb1Classes.retainAll(orb2Classes);
            if(orb1Classes.isEmpty()){
                return false;
            }

            return true;
        }
    }
}
