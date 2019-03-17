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

public class InstrumentGeneralizer extends AbstractLogicOperator {

    protected int selectedInstrument;
    protected int selectedClass;
    protected Literal newLiteral;

    public InstrumentGeneralizer(BaseParams params, AbstractMOEABase base) {
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

        Multiset<Integer> instruments;
        if(constraintSetterAbstract instanceof InOrbit){
            instruments = ((InOrbit) constraintSetterAbstract).getInstruments();
        }else if(constraintSetterAbstract instanceof NotInOrbit) {
            instruments = ((NotInOrbit) constraintSetterAbstract).getInstruments();
        }else if(constraintSetterAbstract instanceof Together) {
            instruments = ((Together) constraintSetterAbstract).getInstruments();
        }else if(constraintSetterAbstract instanceof Separate) {
            instruments = ((Separate) constraintSetterAbstract).getInstruments();
        }else{
            throw new UnsupportedOperationException();
        }

        List<Integer> instrumentList = new ArrayList<>();
        for(int instrument: instruments){
            instrumentList.add(instrument);
        }

        Collections.shuffle(instrumentList);
        this.selectedInstrument = instrumentList.get(0);
        for(int instrument: instrumentList){
            if(instrument < params.getLeftSetCardinality()){
                this.selectedInstrument = instrument;
            }
        }

        Set<Integer> superclasses = params.getLeftSetSuperclass("Instrument", this.selectedInstrument);
        List<Integer> superclassesList = new ArrayList<>();
        for(int i:superclasses){
            superclassesList.add(i);
        }
        Collections.shuffle(superclassesList);
        this.selectedClass = superclassesList.get(0);

        Multiset<Integer> modifiedInstrumentSet = HashMultiset.create();
        for(int inst: instruments){
            if(this.selectedInstrument == inst) {
                continue;
            }else{
                if(constraintSetterAbstract instanceof NotInOrbit || constraintSetterAbstract instanceof Separate){
                    Set<Integer> tempSuperclassSet = params.getLeftSetSuperclass("Instrument", inst);
                    if(tempSuperclassSet.contains(this.selectedClass)){
                        continue;
                    }
                }else{
                    modifiedInstrumentSet.add(inst);
                }
            }
        }
        modifiedInstrumentSet.add(this.selectedClass);

        AbstractFilter newFilter;
        if(constraintSetterAbstract instanceof InOrbit){
            newFilter = new InOrbit(params, ((InOrbit)constraintSetterAbstract).getOrbit(), modifiedInstrumentSet);
        }else if(constraintSetterAbstract instanceof NotInOrbit) {
            if(modifiedInstrumentSet.count(this.selectedClass) > 1){
                modifiedInstrumentSet.remove(this.selectedClass);
            }
            newFilter = new NotInOrbit(params, ((NotInOrbit)constraintSetterAbstract).getOrbit(), modifiedInstrumentSet);
        }else if(constraintSetterAbstract instanceof Together) {
            newFilter = new Together(params, modifiedInstrumentSet);
        }else if(constraintSetterAbstract instanceof Separate) {
            if(modifiedInstrumentSet.count(this.selectedClass) > 1){
                modifiedInstrumentSet.remove(this.selectedClass);
            }
            newFilter = new Separate(params, modifiedInstrumentSet);
        }else{
            throw new UnsupportedOperationException();
        }

        // Remove the current node
        Literal constraintSetterLiteral = nodes.get(constraintSetterAbstract);
        parent.removeLiteral(constraintSetterLiteral);

        // Add the new feature to the parent node
        Feature newFeature = this.base.getFeatureFetcher().fetch(newFilter);
        this.newLiteral = new Literal(newFeature.getName(), newFeature.getMatches());
        parent.addLiteral(newLiteral);
    }


    @Override
    public String getDescription(){
        Params params = (Params) super.params;
        StringBuilder sb = new StringBuilder();
        sb.append("Generalize instrument " + params.getLeftSetEntityName(this.selectedInstrument));
        sb.append(" to ");
        sb.append(params.getLeftSetEntityName(this.selectedClass));
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
                if(instrument < params.getLeftSetCardinality()){
                    return true;
                }
            }
            return false;
        }
    }
}
