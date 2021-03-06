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
import ifeed.problem.assigning.filters.Present;
import java.util.*;

public class InOrbits2Present extends AbstractExhaustiveSearchOperator {

    protected List<AbstractFilter> filtersToBeModified;
    protected AbstractFilter newFilter;
    protected int selectedInstrument;
    protected List<Connective> targetParentNodes;

    public InOrbits2Present(BaseParams params, AbstractMOEABase base) {
        super(params, base, LogicalConnectiveType.OR, 1);
    }

    @Override
    public void initialize(){
        this.selectedInstrument = -1;
        this.filtersToBeModified = new ArrayList<>();
        this.newFilter = null;
        this.targetParentNodes = new ArrayList<>();
    }

    @Override
    public boolean apply(Connective root,
                         Connective parent,
                         AbstractFilter constraintSetterAbstract,
                         Set<AbstractFilter> matchingFilters,
                         Map<AbstractFilter, Literal> nodes
    ){
        initialize();

        Params params = (Params) super.params;
        this.targetParentNodes = new ArrayList<>();

        // Count the number of appearances of each instrument
        Map<Integer, Integer> instrumentCounter = new HashMap<>();
        InOrbit constraintSetter = (InOrbit) constraintSetterAbstract;
        for(int i: constraintSetter.getInstruments()){
            if(super.checkIfVisited(i)){
                continue;
            }
            instrumentCounter.put(i, 1);
        }
        for(AbstractFilter filter: matchingFilters){
            for(int inst: ((InOrbit) filter).getInstruments()){
                if(instrumentCounter.containsKey(inst)){
                    instrumentCounter.put(inst, instrumentCounter.get(inst) + 1);
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

        // Remove the selected instrument from future search, in order to perform exhaustive search
        super.setVisitedVariable(this.selectedInstrument);
        if(super.getVisitedVariables().size() >= instrumentCounter.size()){
            super.setSearchFinished();
        }

        Set<AbstractFilter> allFilters = new HashSet<>();
        allFilters.add(constraintSetterAbstract);
        allFilters.addAll(matchingFilters);

        // Remove nodes that share the instrument
        filtersToBeModified = new ArrayList<>();
        for(AbstractFilter filter: allFilters){
            if(((InOrbit) filter).getInstruments().contains(this.selectedInstrument)){

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
        newFilter = new Present(params, this.selectedInstrument);
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
            InOrbit inOrbit = (InOrbit) filtersToBeModified.get(i);

            if(inOrbit.getInstruments().size() > 1){
                ArrayList<Integer> instruments = new ArrayList<>(inOrbit.getInstruments());
                int selectedArgumentIndex = instruments.indexOf(this.selectedInstrument);
                instruments.remove(selectedArgumentIndex);

                AbstractFilter modifiedFilter = new InOrbit(params, inOrbit.getOrbit(), Utils.intCollection2Array(instruments));
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
        sb.append("\"Instrument " + params.getLeftSetEntityName(this.selectedInstrument) + " is assigned to either one of the orbits {");
        StringJoiner orbitNamesJoiner = new StringJoiner(", ");
        for(AbstractFilter filter: this.filtersToBeModified){
            InOrbit tempInOrbit = (InOrbit) filter;
            orbitNamesJoiner.add(params.getRightSetEntityName(tempInOrbit.getOrbit()));
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
        // Find all InOrbit literals sharing at least one common instrument argument inside the current node (parent).
        // All Literals and their corresponding Filters are not returned, but the lists are filled up as side effects
        FilterFinder finder = new FilterFinder();
        super.findApplicableNodesUnderGivenParentNode(parent, applicableFiltersMap, applicableLiteralsMap, finder);
    }

    public class FilterFinder extends AbstractFilterFinder {

        Multiset<Integer> instrumentsToBeIncluded;

        public FilterFinder(){
            super(InOrbit.class, InOrbit.class);
            this.instrumentsToBeIncluded = null;
        }

        @Override
        public void setConstraints(AbstractFilter constraintSetter){
            this.instrumentsToBeIncluded = ((InOrbit) constraintSetter).getInstruments();
        }

        @Override
        public void clearConstraints(){
            this.instrumentsToBeIncluded = null;
        }

        /**
         * One of the instruments in the tested filter should be in the constraint instrument set
         * @param filterToTest
         * @return
         */
        @Override
        public boolean check(AbstractFilter filterToTest){

            // Check if two literals share at least one common instrument
            Multiset<Integer> instruments1 = this.instrumentsToBeIncluded;
            Multiset<Integer> instruments2 = ((InOrbit) filterToTest).getInstruments();

            for(int inst:instruments2){
                if(instruments1.contains(inst)) {
                    return true;
                }
            }
            return false;
        }
    }
}
