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
import ifeed.mining.moea.operators.AbstractLogicOperator;
import ifeed.problem.assigning.Params;
import ifeed.problem.assigning.filters.EmptyOrbit;
import ifeed.problem.assigning.filters.InOrbit;
import ifeed.problem.assigning.filters.NotInOrbit;

import java.util.*;

public class NotInOrbit2EmptyOrbit extends AbstractLogicOperator {

    protected NotInOrbit constraintSetter;
    protected int orbit;
    protected AbstractFilter newFilter;
    protected Literal newLiteral;

    public NotInOrbit2EmptyOrbit(BaseParams params, AbstractMOEABase base) {
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

        constraintSetter = (NotInOrbit) constraintSetterAbstract;
        this.orbit = constraintSetter.getOrbit();

        // Remove NotInOrbit node
        Literal constraintSetterLiteral = nodes.get(constraintSetter);
        parent.removeLiteral(constraintSetterLiteral);

        this.newFilter = new EmptyOrbit(params, this.orbit);
        Feature newFeature = this.base.getFeatureFetcher().fetch(newFilter);
        this.newLiteral = new Literal(newFeature.getName(), newFeature.getMatches());
        parent.addLiteral(this.newLiteral);

        // Add the exception under OR
        if(parent.getLogic() == LogicalConnectiveType.OR){
            // If the parent node is OR, add extra conditions as siblings
            this.newLiteral = null;
        } else {
            // If the parent node is AND, create an OR node and add extra conditions there
        }
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
            if(instruments.size() >= this.params.getLeftSetCardinality() - 3){
                return true;
            } else {
                return false;
            }
        }
    }
}
