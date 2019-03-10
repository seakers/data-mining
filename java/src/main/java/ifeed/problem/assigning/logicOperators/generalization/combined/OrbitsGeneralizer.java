package ifeed.problem.assigning.logicOperators.generalization.combined;

import com.google.common.collect.Multiset;
import com.sun.org.apache.xpath.internal.operations.Mult;
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
import ifeed.problem.assigning.filters.InOrbit;
import ifeed.problem.assigning.filters.NotInOrbit;
import ifeed.problem.assigning.filters.Present;

import java.util.*;

public class OrbitsGeneralizer extends AbstractLogicOperator {

    protected int selectedOrbit;
    protected int selectedClass;
    protected Literal newLiteral;

    protected int selectedInstrument;
    protected List<Connective> targetParentNodes;

    public OrbitsGeneralizer(BaseParams params, AbstractMOEABase base) {
        super(params, base);
    }

    public void apply(Connective root,
                      Connective parent,
                      AbstractFilter constraintSetterAbstract,
                      Set<AbstractFilter> matchingFilters,
                      Map<AbstractFilter, Literal> nodes
    ){
        Params params = (Params) super.params;

        this.targetParentNodes = new ArrayList<>();

        LogicalConnectiveType targetLogic;
        if(constraintSetterAbstract instanceof InOrbit){
            this.selectedOrbit = ((InOrbit) constraintSetterAbstract).getOrbit();
            targetLogic = LogicalConnectiveType.OR;

        }else if(constraintSetterAbstract instanceof NotInOrbit){
            this.selectedOrbit = ((NotInOrbit) constraintSetterAbstract).getOrbit();
            targetLogic = LogicalConnectiveType.AND;
        }else{
            throw new UnsupportedOperationException();
        }

        // If the logic does not match, pass
        // TODO: Improve how this constraint is handled
        if(targetLogic != parent.getLogic()){
            return;
        }

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
            int testOrb;
            if(filter instanceof InOrbit){
                testOrb = ((InOrbit) filter).getOrbit();
            }else{
                testOrb = ((NotInOrbit) filter).getOrbit();
            }
            Set<Integer> orbClasses = params.getRightSetSuperclass("Orbit", testOrb);
            if(orbClasses.contains(this.selectedClass)){
                allFilters.add(filter);
            }
        }

        // Count the number of appearances of each instrument
        Map<Integer, Integer> instrumentCounter = new HashMap<>();
        for(AbstractFilter filter: allFilters){
            Multiset<Integer> testInstr;
            if(filter instanceof InOrbit){
                testInstr = ((InOrbit) filter).getInstruments();
            }else{
                testInstr = ((NotInOrbit) filter).getInstruments();
            }
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
        for(int inst: instrumentCounter.keySet()){
            if(instrumentCounter.get(inst) > highestFrequency){
                highestFrequency = instrumentCounter.get(inst);
                mostFrequentInstrument = inst;
            }
        }

        this.selectedInstrument = mostFrequentInstrument;

        // Remove nodes that share the instrument
        List<AbstractFilter> filtersToBeModified = new ArrayList<>();
        for(AbstractFilter filter: allFilters){
            Multiset<Integer> testInstr;
            if(filter instanceof InOrbit){
                testInstr = ((InOrbit) filter).getInstruments();
            }else{
                testInstr = ((NotInOrbit) filter).getInstruments();
            }

            if(testInstr.contains(this.selectedInstrument)){
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
        AbstractFilter newFilter;
        if(constraintSetterAbstract instanceof InOrbit){
            newFilter = new InOrbit(params, this.selectedClass, this.selectedInstrument);
        }else{
            newFilter = new NotInOrbit(params, this.selectedClass, this.selectedInstrument);
        }
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
            int orbit;
            Multiset<Integer> instruments;
            if(filter instanceof InOrbit){
                orbit = ((InOrbit) filter).getOrbit();
                instruments = ((InOrbit) filter).getInstruments();
            }else{
                orbit = ((NotInOrbit) filter).getOrbit();
                instruments = ((NotInOrbit) filter).getInstruments();
            }

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
            allowedClasses.add(InOrbit.class);
            allowedClasses.add(NotInOrbit.class);
            super.setConstraintSetterClasses(allowedClasses);
            super.setMatchingClasses(allowedClasses);
        }

        @Override
        public void setConstraints(AbstractFilter constraintSetter){
            if(constraintSetter instanceof InOrbit){
                this.matchingClassName = InOrbit.class.getSimpleName();
                this.orbit = ((InOrbit)constraintSetter).getOrbit();
                this.instruments = ((InOrbit)constraintSetter).getInstruments();

            }else if(constraintSetter instanceof NotInOrbit){
                this.matchingClassName = NotInOrbit.class.getSimpleName();
                this.orbit = ((NotInOrbit)constraintSetter).getOrbit();
                this.instruments = ((NotInOrbit)constraintSetter).getInstruments();
            }
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
            int orb2;
            Multiset<Integer> inst2;

            if(this.matchingClassName.equalsIgnoreCase(InOrbit.class.getSimpleName())){
                InOrbit filter = (InOrbit) filterToTest;
                orb2 = filter.getOrbit();
                inst2 = filter.getInstruments();

            }else if(this.matchingClassName.equalsIgnoreCase(NotInOrbit.class.getSimpleName())){
                NotInOrbit filter = (NotInOrbit) filterToTest;
                orb2 = filter.getOrbit();
                inst2 = filter.getInstruments();

            }else{
                throw new IllegalArgumentException();
            }

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