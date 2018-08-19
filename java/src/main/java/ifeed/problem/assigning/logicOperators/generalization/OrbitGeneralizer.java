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
import org.semanticweb.owlapi.model.OWLClass;

import java.util.*;

public class OrbitGeneralizer extends AbstractGeneralizationOperator{

    public OrbitGeneralizer(BaseParams params, MOEABase base) {
        super(params, base);
    }

    protected void apply(Connective root,
                         Connective parent,
                         AbstractFilter constraintSetterAbstract,
                         Set<AbstractFilter> matchingFilters,
                         Map<AbstractFilter, Literal> nodes
    ){

        Params params = (Params) super.params;

        int orbit;
        Multiset<Integer> instruments;
        switch (constraintSetterAbstract.getClass().getSimpleName()){
            case "InOrbit":
                orbit = ((InOrbit) constraintSetterAbstract).getOrbit();
                instruments = ((InOrbit) constraintSetterAbstract).getInstruments();
                break;
            case "NotInOrbit":
                orbit = ((NotInOrbit) constraintSetterAbstract).getOrbit();
                instruments = ((NotInOrbit) constraintSetterAbstract).getInstruments();
                break;
            default:
                throw new UnsupportedOperationException();
        }

        String orbitName = params.getOrbitIndex2Name().get(orbit);
        List<OWLClass> superclasses = params.getOntologyManager().getSuperClasses("Orbit", orbitName);
        Collections.shuffle(superclasses);
        OWLClass orbitClass = superclasses.get(0);
        String orbitClassName = orbitClass.getIRI().getShortForm();

        params.addOrbitClass(orbitClassName);
        int orbitIndex = params.getOrbitName2Index().get(orbitClassName);

        AbstractFilter newFilter;
        switch (constraintSetterAbstract.getClass().getSimpleName()){
            case "InOrbit":
                newFilter = new InOrbit(params, orbitIndex, instruments);
                break;
            case "NotInOrbit":
                newFilter = new NotInOrbit(params, orbitIndex, instruments);
                break;
            default:
                throw new UnsupportedOperationException();
        }

        // Remove literal
        Literal constraintSetterLiteral = nodes.get(constraintSetterAbstract);
        parent.removeLiteral(constraintSetterLiteral);

        // Add the new feature to the parent node
        Feature presentFeature = base.getFeatureFetcher().fetch(newFilter);
        parent.addLiteral(presentFeature.getName(), presentFeature.getMatches());
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
            if(this.orbit >= params.getNumOrbits()){
                return false;
            }
            return true;
        }
    }
}