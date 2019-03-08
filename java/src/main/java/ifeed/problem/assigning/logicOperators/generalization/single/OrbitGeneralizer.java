package ifeed.problem.assigning.logicOperators.generalization.single;

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

import java.util.*;

public class OrbitGeneralizer extends AbstractLogicOperator {

    protected int selectedOrbit;
    protected int selectedClass;
    protected Literal newLiteral;

    public OrbitGeneralizer(BaseParams params, AbstractMOEABase base) {
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
        if(constraintSetterAbstract instanceof InOrbit){
            this.selectedOrbit = ((InOrbit) constraintSetterAbstract).getOrbit();
            instruments = ((InOrbit) constraintSetterAbstract).getInstruments();

        }else if(constraintSetterAbstract instanceof NotInOrbit){
            this.selectedOrbit = ((NotInOrbit) constraintSetterAbstract).getOrbit();
            instruments = ((NotInOrbit) constraintSetterAbstract).getInstruments();
        }else{
            throw new UnsupportedOperationException();
        }

        Set<Integer> superclasses = params.getRightSetSuperclass("Orbit", this.selectedOrbit);
        List<Integer> superclassesList = new ArrayList<>();
        for(int i:superclasses){
            superclassesList.add(i);
        }
        Collections.shuffle(superclassesList);
        this.selectedClass = superclassesList.get(0);

        AbstractFilter newFilter;
        if(constraintSetterAbstract instanceof InOrbit){
            newFilter = new InOrbit(params, selectedClass, instruments);
        }else if(constraintSetterAbstract instanceof NotInOrbit){
            newFilter = new NotInOrbit(params, selectedClass, instruments);
        }else{
            throw new UnsupportedOperationException();
        }

        // Remove literal
        Literal constraintSetterLiteral = nodes.get(constraintSetterAbstract);
        parent.removeLiteral(constraintSetterLiteral);

        // Add the new feature to the parent node
        Feature newFeature = this.base.getFeatureFetcher().fetch(newFilter);
        this.newLiteral = new Literal(newFeature.getName(), newFeature.getMatches());
        parent.addLiteral(this.newLiteral);
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

        public FilterFinder(Params params){
            super();
            this.orbit = -1;
            this.params = params;
            Set<Class> constraintSetter = new HashSet<>();
            constraintSetter.add(InOrbit.class);
            constraintSetter.add(NotInOrbit.class);
            super.setConstraintSetterClasses(constraintSetter);
        }

        @Override
        public void setConstraints(AbstractFilter constraintSetter){
            if(constraintSetter.getClass() == InOrbit.class){
                orbit = ((InOrbit)constraintSetter).getOrbit();

            }else if(constraintSetter.getClass() == NotInOrbit.class){
                orbit = ((NotInOrbit)constraintSetter).getOrbit();
            }
        }

        @Override
        public void clearConstraints(){
            this.orbit = -1;
        }

        @Override
        public boolean check(){
            if(this.orbit >= params.getRightSetCardinality()){
                return false;
            }
            return true;
        }
    }
}
