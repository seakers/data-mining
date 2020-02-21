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
import ifeed.problem.assigning.filters.NotInOrbit;
import java.util.*;

public class NotInOrbitsOrbGeneralizer extends AbstractExhaustiveSearchOperator {

    protected int selectedOrbit;
    protected int selectedClass;
    protected Literal newLiteral;
    protected int selectedInstrument;
    protected Connective targetParentNode;
    protected List<AbstractFilter> filtersToBeModified;
    protected AbstractFilter newFilter;
    protected Feature newFeature;

    public NotInOrbitsOrbGeneralizer(BaseParams params, AbstractMOEABase base) {
        super(params, base, LogicalConnectiveType.AND, 2);
    }

    @Override
    public void initialize(){
        this.selectedOrbit = -1;
        this.selectedClass = -1;
        this.selectedInstrument = -1;
        this.newLiteral = null;
        this.targetParentNode = null;
        this.filtersToBeModified = new ArrayList<>();
        this.newFilter = null;
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
        NotInOrbit constraintSetter = (NotInOrbit) constraintSetterAbstract;

        this.selectedOrbit = constraintSetter.getOrbit();
        Multiset<Integer> constraintSetterInstruments = constraintSetter.getInstruments();

        List<Integer> sharedInstruments = new ArrayList<>();
        for(AbstractFilter filter: matchingFilters){
            for(int i: ((NotInOrbit) filter).getInstruments()){
                if(constraintSetterInstruments.contains(i)){
                    // Shared by both literals
                    if(super.checkIfVisited(i)){
                        continue;
                    }else{
                        sharedInstruments.add(i);
                    }
                }
            }
        }

        if(sharedInstruments.isEmpty()){
            super.setSearchFinished();
            return false;
        }

        // Select one instrument
        Collections.shuffle(sharedInstruments);
        this.selectedInstrument = sharedInstruments.get(0);

        // Find all filters that contains the selected instrument
        List<AbstractFilter> filtersWithSelectedInstrument = new ArrayList<>();
        for(AbstractFilter filter: matchingFilters){
            if(((NotInOrbit) filter).getInstruments().contains(this.selectedInstrument)){
                filtersWithSelectedInstrument.add(filter);
            }
        }

        // Count the number of appearances of each orbit class
        Map<Integer, Integer> orbitClassCounter = new HashMap<>();
        List<Integer> sharedOrbitClasses = new ArrayList<>();
        Set<Integer> constraintSetterOrbitSuperclasses = params.getRightSetSuperclass(this.selectedOrbit);
        for(int c: constraintSetterOrbitSuperclasses){
            orbitClassCounter.put(c, 1);
        }
        for(AbstractFilter filter: filtersWithSelectedInstrument){
            int orb = ((NotInOrbit) filter).getOrbit();
            Set<Integer> tempClassSet = params.getRightSetSuperclass(orb);
            for(int o: tempClassSet){
                if(orbitClassCounter.containsKey(o)){
                    orbitClassCounter.put(o, orbitClassCounter.get(o) + 1);
                    if(!super.checkIfVisited(this.selectedInstrument, o)){
                        sharedOrbitClasses.add(o);
                    }
                }
            }
        }

        if(sharedOrbitClasses.isEmpty()){
            super.setVisitedVariable(this.selectedInstrument);
            return false;
        }
        Collections.shuffle(sharedOrbitClasses);
        this.selectedClass = sharedOrbitClasses.get(0);
        super.setVisitedVariable(this.selectedInstrument, this.selectedClass);

        List<AbstractFilter> allFilters = new ArrayList<>();
        allFilters.add(constraintSetter);
        allFilters.addAll(filtersWithSelectedInstrument);

        // Remove nodes whose orbits are in the selected class
        filtersToBeModified = new ArrayList<>();
        for(AbstractFilter filter: allFilters){
            NotInOrbit notInOrbit = (NotInOrbit) filter;
            int orbit = notInOrbit.getOrbit();

            if(params.getRightSetSuperclass(orbit).contains(this.selectedClass)){
                // Remove matching literals
                Literal literal = nodes.get(filter);
                parent.removeNode(literal);
                filtersToBeModified.add(filter);
            }
        }

        // Create new feature
        this.targetParentNode = parent;
        this.newFilter = new NotInOrbit(params, this.selectedClass, this.selectedInstrument);
        this.newFeature = this.base.getFeatureFetcher().fetch(newFilter);
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
        return true;
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
            Set<Integer> orb1Classes = params.getRightSetSuperclass(orb1, true);
            Set<Integer> orb2Classes = params.getRightSetSuperclass(orb2, true);
            orb1Classes.retainAll(orb2Classes);
            if(orb1Classes.isEmpty()){
                return false;
            }
            return true;
        }
    }
}
