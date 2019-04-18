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
import ifeed.problem.assigning.filters.Absent;
import ifeed.problem.assigning.filters.NotInOrbit;

import java.util.*;

public class NotInOrbits2Absent extends AbstractLogicOperator {

    protected List<AbstractFilter> filtersToBeModified;
    protected int selectedInstrument;
    protected Connective targetParentNode;
    protected AbstractFilter newFilter;
    protected Literal newLiteral;
    protected Set<Integer> restrictedOrbits;

    public NotInOrbits2Absent(BaseParams params, AbstractMOEABase base) {
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

        Set<AbstractFilter> allFilters = new HashSet<>();
        allFilters.add(constraintSetterAbstract);
        allFilters.addAll(matchingFilters);

        // Count the number of appearances of each instrument
        Map<Integer, Integer> instrumentCounter = new HashMap<>();
        for(AbstractFilter filter: allFilters){
            NotInOrbit notInOrbit = (NotInOrbit) filter;
            for(int inst: notInOrbit.getInstruments()){
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
        this.targetParentNode = parent;

        // Remove nodes that share the instrument
        filtersToBeModified = new ArrayList<>();
        restrictedOrbits = new HashSet<>();
        for(AbstractFilter filter: allFilters){
            NotInOrbit notInOrbit = (NotInOrbit) filter;
            if(notInOrbit.getInstruments().contains(this.selectedInstrument)){

                restrictedOrbits.add(notInOrbit.getOrbit());

                // Remove matching literals
                Literal literal = nodes.get(filter);
                parent.removeNode(literal);
                filtersToBeModified.add(filter);
            }
        }

        // Create new feature
        newFilter = new Absent(params, this.selectedInstrument);
        Feature newFeature = this.base.getFeatureFetcher().fetch(newFilter);
        this.newLiteral = new Literal(newFeature.getName(), newFeature.getMatches());
        parent.addLiteral(this.newLiteral);

        for(int i = 0; i < filtersToBeModified.size(); i++){
            NotInOrbit notInOrbit = (NotInOrbit) filtersToBeModified.get(i);

            if(notInOrbit.getInstruments().size() > 1){
                ArrayList<Integer> instruments = new ArrayList<>(notInOrbit.getInstruments());
                int selectedArgumentIndex = instruments.indexOf(this.selectedInstrument);
                instruments.remove(selectedArgumentIndex);

                AbstractFilter modifiedFilter = new NotInOrbit(params, notInOrbit.getOrbit(), Utils.intCollection2Array(instruments));
                Feature modifiedFeature = base.getFeatureFetcher().fetch(modifiedFilter);

                parent.addLiteral(modifiedFeature.getName(), modifiedFeature.getMatches());
            }
        }
    }


    @Override
    public String getDescription(){

        Params params = (Params) this.params;

        StringBuilder sb = new StringBuilder();
        sb.append("Generalize ");
        sb.append("\"Instrument " + params.getLeftSetEntityName(this.selectedInstrument) + " is not assigned to any of the orbits {");

        StringJoiner orbitNamesJoiner = new StringJoiner(", ");
        for(AbstractFilter filter: this.filtersToBeModified){
            NotInOrbit tempNotInOrbit = (NotInOrbit) filter;
            orbitNamesJoiner.add(params.getRightSetEntityName(tempNotInOrbit.getOrbit()));
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
            super(NotInOrbit.class, NotInOrbit.class);
            this.instrumentsToBeIncluded = null;
        }

        @Override
        public void setConstraints(AbstractFilter constraintSetter){
            this.instrumentsToBeIncluded = ((NotInOrbit) constraintSetter).getInstruments();
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
            Multiset<Integer> instruments2 = ((NotInOrbit) filterToTest).getInstruments();

            for(int inst:instruments2){
                if(instruments1.contains(inst)) {
                    return true;
                }
            }
            return false;
        }
    }
}
