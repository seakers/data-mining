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
import ifeed.problem.assigning.filters.InOrbit;

import java.util.*;

public class InOrbitsOrbGeneralizer extends AbstractExhaustiveSearchOperator {

    protected int selectedOrbit;
    protected int selectedInstrument;
    protected int selectedClass;
    protected Literal newLiteral;
    protected List<Connective> targetParentNodes;
    protected List<AbstractFilter> filtersToBeModified;
    protected AbstractFilter newFilter;

    public InOrbitsOrbGeneralizer(BaseParams params, AbstractMOEABase base) {
        super(params, base, LogicalConnectiveType.OR, 2);
    }

    @Override
    public void initialize(){
        this.selectedOrbit = -1;
        this.selectedClass = -1;
        this.selectedInstrument = -1;
        this.newLiteral = null;
        this.targetParentNodes = new ArrayList<>();
        this.filtersToBeModified = new ArrayList<>();
        this.newFilter = null;
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

        InOrbit constraintSetter = (InOrbit) constraintSetterAbstract;

        this.targetParentNodes = new ArrayList<>();
        this.selectedOrbit = constraintSetter.getOrbit();

        List<Integer> sharedInstruments = new ArrayList<>();
        for(AbstractFilter filter: matchingFilters){
            for(int i: ((InOrbit) filter).getInstruments()){
                if(constraintSetter.getInstruments().contains(i)){
                    if(super.checkIfVisited(i)){
                        continue;
                    }
                    sharedInstruments.add(i);
                }
            }
        }

        if(sharedInstruments.isEmpty()){
            super.setSearchFinished();
            return false;
        }

        this.selectedInstrument = sharedInstruments.get(0);

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
        List<Integer> sharedOrbitClasses = new ArrayList<>();
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

        return true;
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

            // Check if at least one common instrument is shared by the two filters
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
