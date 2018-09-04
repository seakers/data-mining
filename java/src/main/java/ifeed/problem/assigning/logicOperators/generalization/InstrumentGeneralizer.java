package ifeed.problem.assigning.logicOperators.generalization;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import ifeed.feature.Feature;
import ifeed.feature.logic.Connective;
import ifeed.feature.logic.Literal;
import ifeed.filter.AbstractFilter;
import ifeed.filter.AbstractFilterFinder;
import ifeed.local.params.BaseParams;
import ifeed.mining.moea.MOEABase;
import ifeed.mining.moea.operators.AbstractGeneralizationOperator;
import ifeed.problem.assigning.Params;
import ifeed.problem.assigning.filters.InOrbit;
import ifeed.problem.assigning.filters.NotInOrbit;
import ifeed.problem.assigning.filters.Separate;
import ifeed.problem.assigning.filters.Together;
import java.util.*;

public class InstrumentGeneralizer extends AbstractGeneralizationOperator{

    public InstrumentGeneralizer(BaseParams params, MOEABase base) {
        super(params, base);
    }

    public void apply(Connective root,
                         Connective parent,
                         AbstractFilter constraintSetterAbstract,
                         Set<AbstractFilter> matchingFilters,
                         Map<AbstractFilter, Literal> nodes
    ){

        Params params = (Params) super.params;

        Multiset<Integer> instruments;
        switch (constraintSetterAbstract.getClass().getSimpleName()){
            case "InOrbit":
                instruments = ((InOrbit) constraintSetterAbstract).getInstruments();
                break;
            case "NotInOrbit":
                instruments = ((NotInOrbit) constraintSetterAbstract).getInstruments();
                break;
            case "Together":
                instruments = ((Together) constraintSetterAbstract).getInstruments();
                break;
            case "Separate":
                instruments = ((Separate) constraintSetterAbstract).getInstruments();
                break;
            default:
                throw new UnsupportedOperationException();
        }

        List<Integer> instrumentList = new ArrayList<>();
        for(int instrument: instruments){
            instrumentList.add(instrument);
        }

        Collections.shuffle(instrumentList);
        int selectedInstrument = instrumentList.get(0);
        for(int instrument: instrumentList){
            if(instrument < params.getNumInstruments()){
                selectedInstrument = instrument;
            }
        }

        String instrumentName = params.getInstrumentIndex2Name().get(selectedInstrument);
        List<String> superclasses = params.getOntologyManager().getSuperClasses("Instrument", instrumentName);

        Collections.shuffle(superclasses);
        String selectedClass = superclasses.get(0);
        params.addInstrumentClass(selectedClass);
        int classIndex = params.getInstrumentName2Index().get(selectedClass);

        Multiset<Integer> modifiedInstrumentSet = HashMultiset.create(instruments);
        modifiedInstrumentSet.remove(selectedInstrument);
        modifiedInstrumentSet.add(classIndex);

        AbstractFilter newFilter;
        switch (constraintSetterAbstract.getClass().getSimpleName()){
            case "InOrbit":
                newFilter = new InOrbit(params, ((InOrbit)constraintSetterAbstract).getOrbit(), modifiedInstrumentSet);
                break;
            case "NotInOrbit":
                newFilter = new NotInOrbit(params, ((NotInOrbit)constraintSetterAbstract).getOrbit(), modifiedInstrumentSet);
                break;
            case "Together":
                newFilter = new Together(params, modifiedInstrumentSet);
                break;
            case "Separate":
                newFilter = new Separate(params, modifiedInstrumentSet);
                break;
            default:
                throw new UnsupportedOperationException();
        }

        // Remove the current node
        Literal constraintSetterLiteral = nodes.get(constraintSetterAbstract);
        int nodeIndex = parent.getNodeIndex(constraintSetterLiteral);
        parent.removeLiteral(constraintSetterLiteral);

        // Add the new feature to the parent node
        Feature newFeature = base.getFeatureFetcher().fetch(newFilter);
        parent.addLiteral(nodeIndex, newFeature.getName(), newFeature.getMatches());
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
            super();
            this.params = params;
            Set<Class> constraintSetter = new HashSet<>();
            constraintSetter.add(InOrbit.class);
            constraintSetter.add(NotInOrbit.class);
            constraintSetter.add(Together.class);
            constraintSetter.add(Separate.class);
            super.setConstraintSetterClasses(constraintSetter);
        }

        @Override
        public void setConstraints(AbstractFilter constraintSetter){
            switch (constraintSetter.getClass().getSimpleName()){
                case "InOrbit":
                    instruments = ((InOrbit) constraintSetter).getInstruments();
                    break;
                case "NotInOrbit":
                    instruments = ((NotInOrbit) constraintSetter).getInstruments();
                    break;
                case "Together":
                    instruments = ((Together) constraintSetter).getInstruments();
                    break;
                case "Separate":
                    instruments = ((Separate) constraintSetter).getInstruments();
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
        }

        @Override
        public void clearConstraints(){
            instruments = null;
        }

        @Override
        public boolean check(){
            for(int instrument: instruments){
                if(instrument < params.getNumInstruments()){
                    return true;
                }
            }
            return false;
        }
    }
}
