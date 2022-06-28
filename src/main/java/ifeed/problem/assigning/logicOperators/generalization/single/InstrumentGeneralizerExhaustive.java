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
import ifeed.mining.moea.operators.AbstractExhaustiveSearchOperator;
import ifeed.problem.assigning.Params;
import ifeed.problem.assigning.filters.InOrbit;
import ifeed.problem.assigning.filters.NotInOrbit;
import ifeed.problem.assigning.filters.Separate;
import ifeed.problem.assigning.filters.Together;

import java.util.*;

public class InstrumentGeneralizerExhaustive extends AbstractExhaustiveSearchOperator {

    AbstractFilter constraintSetter;
    protected int selectedInstrument;
    protected int selectedClass;
    protected Literal newLiteral;
    protected AbstractFilter newFilter;

    public InstrumentGeneralizerExhaustive(BaseParams params, AbstractMOEABase base) {
        super(params, base, 2);
    }

    @Override
    public void initialize(){
        this.selectedInstrument = -1;
        this.selectedClass = -1;
        this.newLiteral = null;
        this.newFilter = null;
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

        this.constraintSetter = constraintSetterAbstract;
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
        for(int i: instruments){
            if(super.checkIfVisited(i)){
                continue;
            }
            if(i < params.getLeftSetCardinality()){ // Only consider instrument instances
                instrumentList.add(i);
            }
        }

        if(instrumentList.isEmpty()){
            super.setSearchFinished();
            return false;
        }

        Collections.shuffle(instrumentList);
        this.selectedInstrument = instrumentList.get(0);

        // Search for the super classes
        Set<Integer> superclasses = params.getLeftSetSuperclass(this.selectedInstrument);
        List<Integer> superclassesList = new ArrayList<>();
        for(int c: superclasses){
            if(super.checkIfVisited(this.selectedInstrument, c)){
                continue;
            }
            superclassesList.add(c);
        }

        if(superclassesList.isEmpty()){
            super.setVisitedVariable(this.selectedInstrument);
            return false;
        }

        Collections.shuffle(superclassesList);
        this.selectedClass = superclassesList.get(0);
        super.setVisitedVariable(this.selectedInstrument, this.selectedClass);

        Multiset<Integer> modifiedInstrumentSet = HashMultiset.create();
        for(int inst: instruments){
            if(this.selectedInstrument == inst) {
                continue;
            }else{
                if(constraintSetterAbstract instanceof NotInOrbit){
                    Set<Integer> tempSuperclassSet = params.getLeftSetSuperclass(inst);
                    if(tempSuperclassSet.contains(this.selectedClass)){
                        continue;
                    }
                }else{
                    modifiedInstrumentSet.add(inst);
                }
            }
        }
        modifiedInstrumentSet.add(this.selectedClass);

        if(constraintSetterAbstract instanceof InOrbit){
            this.newFilter = new InOrbit(params, ((InOrbit)constraintSetterAbstract).getOrbit(), modifiedInstrumentSet);
        }else if(constraintSetterAbstract instanceof NotInOrbit) {
            if(modifiedInstrumentSet.count(this.selectedClass) > 1){
                modifiedInstrumentSet.remove(this.selectedClass);
            }
            this.newFilter = new NotInOrbit(params, ((NotInOrbit)constraintSetterAbstract).getOrbit(), modifiedInstrumentSet);
        }else if(constraintSetterAbstract instanceof Together) {
            this.newFilter = new Together(params, modifiedInstrumentSet);
        }else if(constraintSetterAbstract instanceof Separate) {
            if (modifiedInstrumentSet.count(this.selectedClass) > 2) {
                modifiedInstrumentSet.remove(this.selectedClass);
            }
            this.newFilter = new Separate(params, modifiedInstrumentSet);
        }else{
            throw new UnsupportedOperationException();
        }

        // Remove the current node
        Literal constraintSetterLiteral = nodes.get(constraintSetterAbstract);
        parent.removeLiteral(constraintSetterLiteral);

        // Add the new feature to the parent node
        Feature newFeature = this.base.getFeatureFetcher().fetch(this.newFilter);
        this.newLiteral = new Literal(newFeature.getName(), newFeature.getMatches());
        parent.addLiteral(this.newLiteral);
        return true;
    }

    @Override
    public String getDescription(){
        StringBuilder sb = new StringBuilder();
        sb.append("Generalize ");
        sb.append("\"" + this.constraintSetter.getDescription() + "\"");
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
