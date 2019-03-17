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

public class NotInOrbitsGeneralizer extends AbstractLogicOperator {

    protected int selectedOrbit;
    protected int selectedClass;
    protected Literal newLiteral;

    protected int selectedInstrument;
    protected Connective targetParentNode;

    protected List<AbstractFilter> filtersToBeModified;
    protected AbstractFilter newFilter;

    public NotInOrbitsGeneralizer(BaseParams params, AbstractMOEABase base) {
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

        this.selectedOrbit = ((NotInOrbit) constraintSetterAbstract).getOrbit();

        Set<Integer> superclasses = params.getRightSetSuperclass("Orbit", this.selectedOrbit);
        List<Integer> superclassesList = new ArrayList<>();
        for(int i:superclasses){
            superclassesList.add(i);
        }
        Collections.shuffle(superclassesList);
        this.selectedClass = superclassesList.get(0);

        List<AbstractFilter> allFilters = new ArrayList<>();
        allFilters.add(constraintSetterAbstract);

        // Find all matching filters whose orbits are in the selected class
        for(AbstractFilter filter: matchingFilters){
            int testOrb = ((NotInOrbit) filter).getOrbit();
            Set<Integer> orbClasses = params.getRightSetSuperclass("Orbit", testOrb);
            if(orbClasses.contains(this.selectedClass)){
                allFilters.add(filter);
            }
        }

        // Count the number of appearances of each instrument
        Map<Integer, Integer> instrumentCounter = new HashMap<>();
        for(AbstractFilter filter: allFilters){
            Multiset<Integer> testInstr = ((NotInOrbit) filter).getInstruments();

            for(int inst: testInstr){
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
            if(instrumentCounter.get(inst) > highestFrequency){
                highestFrequency = instrumentCounter.get(inst);
                mostFrequentInstrument = inst;
            }
        }

        this.selectedInstrument = mostFrequentInstrument;

        // Remove nodes that share the instrument
        filtersToBeModified = new ArrayList<>();
        for(AbstractFilter filter: allFilters){
            Multiset<Integer> testInstr = ((NotInOrbit) filter).getInstruments();

            if(testInstr.contains(this.selectedInstrument)){
                // Remove matching literals
                Literal literal = nodes.get(filter);
                parent.removeNode(literal);
                filtersToBeModified.add(filter);
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
        private String matchingClassName;
        private int orbit;
        private Multiset<Integer> instruments;

        public FilterFinder(Params params){
            super();
            this.params = params;
            this.clearConstraints();

            Set<Class> allowedClasses = new HashSet<>();
            allowedClasses.add(NotInOrbit.class);
            super.setConstraintSetterClasses(allowedClasses);
            super.setMatchingClasses(allowedClasses);
        }

        @Override
        public void setConstraints(AbstractFilter constraintSetter){
            this.matchingClassName = NotInOrbit.class.getSimpleName();
            this.orbit = ((NotInOrbit)constraintSetter).getOrbit();
            this.instruments = ((NotInOrbit)constraintSetter).getInstruments();
        }

        @Override
        public void clearConstraints(){
            this.matchingClassName = null;
            this.orbit = -1;
            this.instruments = null;
        }

        @Override
        public boolean check(AbstractFilter filterToTest){

            if(!filterToTest.getClass().getSimpleName().equals(this.matchingClassName)){
                return false;
            }

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
                }
            }
            if(!sharesAnInstrument){
                return false;
            }

            // Check if the orbit is not a generalized concept
            if(orb1 >= params.getRightSetCardinality() || orb2 >= params.getRightSetCardinality()){
                return false;
            }

            // Check if orb1 and orb2 are in the same orbit class
            Set<Integer> orb1Classes = params.getRightSetSuperclass("Orbit",orb1);
            Set<Integer> orb2Classes = params.getRightSetSuperclass("Orbit",orb2);
            orb1Classes.retainAll(orb2Classes);
            if(orb1Classes.isEmpty()){
                return false;
            }

            return true;
        }
    }
}
