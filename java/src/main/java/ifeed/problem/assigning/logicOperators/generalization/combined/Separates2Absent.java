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
import ifeed.problem.assigning.Params;
import ifeed.problem.assigning.filters.Absent;
import ifeed.problem.assigning.filters.NotInOrbit;
import ifeed.problem.assigning.filters.Separate;

import java.util.*;

public class Separates2Absent extends AbstractGeneralizationOperator {

    protected List<AbstractFilter> filtersToBeModified;
    protected int selectedInstrument;
    protected Connective targetParentNode;
    protected AbstractFilter newFilter;
    protected Feature newFeature;
    protected Literal newLiteral;

    public Separates2Absent(BaseParams params, AbstractMOEABase base) {
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
        Separate constraintSetter = (Separate) constraintSetterAbstract;

        // Count the number of appearances of each instrument
        Map<Integer, Integer> instrumentCounter = new HashMap<>();
        for(int instr: constraintSetter.getInstruments()){
            if(super.getRestrictedVariables().contains(instr)){
                continue;
            }else{
                instrumentCounter.put(instr, 1);
            }
        }
        for(AbstractFilter filter: matchingFilters){
            for(int instr: ((Separate) filter).getInstruments()){
                if(instrumentCounter.containsKey(instr)){
                    instrumentCounter.put(instr, instrumentCounter.get(instr) + 1);
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
        for(int instr: keySet){
            if(instrumentCounter.get(instr) > highestFrequency){
                highestFrequency = instrumentCounter.get(instr);
                mostFrequentInstrument = instr;
            }
        }

        this.selectedInstrument = mostFrequentInstrument;
        this.targetParentNode = parent;

        // Remove the selected instrument from future search, in order to do exhaustive search
        super.addVariableRestriction(this.selectedInstrument);
        if(super.getRestrictedVariables().size() >= instrumentCounter.size()){
            super.setExhaustiveSearchFinished();
        }

        Set<AbstractFilter> allFilters = new HashSet<>();
        allFilters.add(constraintSetterAbstract);
        allFilters.addAll(matchingFilters);

        // Remove nodes that share the instrument
        filtersToBeModified = new ArrayList<>();
        for(AbstractFilter filter: allFilters){
            if(((Separate) filter).getInstruments().contains(this.selectedInstrument)){

                // Remove matching literals
                Literal literal = nodes.get(filter);
                parent.removeNode(literal);
                filtersToBeModified.add(filter);
            }
        }

        // Create new feature
        newFilter = new Absent(params, this.selectedInstrument);
        newFeature = this.base.getFeatureFetcher().fetch(newFilter);
        newLiteral = new Literal(newFeature.getName(), newFeature.getMatches());
        parent.addLiteral(this.newLiteral);

        for(int i = 0; i < filtersToBeModified.size(); i++){
            Separate separate = (Separate) filtersToBeModified.get(i);

            if(separate.getInstruments().size() > 2){
                ArrayList<Integer> instruments = new ArrayList<>(separate.getInstruments());
                int selectedArgumentIndex = instruments.indexOf(this.selectedInstrument);
                instruments.remove(selectedArgumentIndex);

                AbstractFilter modifiedFilter = new Separate(params, Utils.intCollection2Array(instruments));
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
//        sb.append("\"Instrument " + params.getLeftSetEntityName(this.selectedInstrument) + " is not assigned to any of the orbits {");
//
//        StringJoiner orbitNamesJoiner = new StringJoiner(", ");
//        for(AbstractFilter filter: this.filtersToBeModified){
//            NotInOrbit notInOrbit = (NotInOrbit) filter;
//            orbitNamesJoiner.add(params.getRightSetEntityName(notInOrbit.getOrbit()));
//        }
//
//        sb.append(orbitNamesJoiner.toString() + "}\"");
//        sb.append(" to ");
//        sb.append("\"" + this.newFilter.getDescription() + "\"");
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
            super(Separate.class, Separate.class);
            this.instrumentsToBeIncluded = null;
        }

        @Override
        public void setConstraints(AbstractFilter constraintSetter){
            this.instrumentsToBeIncluded = ((Separate) constraintSetter).getInstruments();
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
            Multiset<Integer> instruments2 = ((Separate) filterToTest).getInstruments();

            for(int inst:instruments2){
                if(instruments1.contains(inst)) {
                    return true;
                }
            }
            return false;
        }
    }
}
