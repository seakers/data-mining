package ifeed.problem.assigning.logicOperators.generalization.single;

import com.google.common.collect.Multiset;
import ifeed.feature.logic.Connective;
import ifeed.feature.logic.Literal;
import ifeed.feature.Feature;
import ifeed.feature.logic.LogicalConnectiveType;
import ifeed.filter.AbstractFilter;
import ifeed.filter.AbstractFilterFinder;
import ifeed.local.params.BaseParams;
import ifeed.mining.moea.AbstractMOEABase;
import ifeed.mining.moea.operators.AbstractExhaustiveSearchOperator;
import ifeed.mining.moea.operators.AbstractLogicOperator;
import ifeed.problem.assigning.Params;
import ifeed.problem.assigning.filters.EmptyOrbit;
import ifeed.problem.assigning.filters.InOrbit;
import ifeed.problem.assigning.filters.NotInOrbit;

import java.util.*;

public class NotInOrbit2EmptyOrbit extends AbstractExhaustiveSearchOperator {

    protected NotInOrbit constraintSetter;
    protected int orbit;
    protected AbstractFilter newFilter;
    protected Feature newFeature;
    protected Literal newLiteral;

    @Override
    public void initialize(){
        this.constraintSetter = null;
        this.orbit = -1;
        this.newFilter = null;
        this.newFeature = null;
        this.newLiteral = null;
    }

    public NotInOrbit2EmptyOrbit(BaseParams params, AbstractMOEABase base) {
        super(params, base, 1);
    }

    @Override
    public boolean apply(Connective root,
                         Connective parent,
                         AbstractFilter constraintSetterAbstract,
                         Set<AbstractFilter> matchingFilters,
                         Map<AbstractFilter, Literal> nodes
    ){
        Params params = (Params) super.params;
        this.constraintSetter = (NotInOrbit) constraintSetterAbstract;
        this.orbit = constraintSetter.getOrbit();

        if(super.checkIfVisited(this.orbit)){
            super.setSearchFinished();
            return false;
        }else{
            super.setVisitedVariable(this.orbit);
        }

        // Remove NotInOrbit node
        Literal constraintSetterLiteral = nodes.get(constraintSetter);
        parent.removeLiteral(constraintSetterLiteral);

        this.newFilter = new EmptyOrbit(params, this.orbit);
        this.newFeature = this.base.getFeatureFetcher().fetch(newFilter);
        this.newLiteral = new Literal(newFeature.getName(), newFeature.getMatches());
        parent.addLiteral(this.newLiteral);
        return true;
    }

    @Override
    public String getDescription(){
        StringBuilder sb = new StringBuilder();
        sb.append("Generalize ");
        sb.append("\"" + constraintSetter.getDescription() + "\"");
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
        Params params;
        Multiset<Integer> instruments;

        public FilterFinder(Params params){
            super(NotInOrbit.class);
            this.params = params;
            this.clearConstraints();
        }

        @Override
        public void setConstraints(AbstractFilter constraintSetter){
            this.instruments = ((NotInOrbit) constraintSetter).getInstruments();
        }

        @Override
        public void clearConstraints(){
            this.instruments = null;
        }

        @Override
        public boolean check(){
            if(instruments.size() >= this.params.getLeftSetCardinality() / 4){
                return true;

            } else {
                Set<Integer> instrumentInstances = new HashSet<>();
                for(int i: instruments){
                    if(params.isGeneralizedConceptRightSet(i)){
                        instrumentInstances.addAll(params.getRightSetInstantiation(i));
                    } else {
                        instrumentInstances.add(i);
                    }
                }
                if(instrumentInstances.size() >= this.params.getLeftSetCardinality() / 4){
                    return true;
                }

                return false;
            }
        }
    }
}
