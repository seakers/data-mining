package ifeed.problem.assigning.logicOperators.generalization.single;

import ifeed.Utils;
import ifeed.feature.FeatureMetric;
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
import ifeed.problem.assigning.filters.InOrbit;
import ifeed.problem.assigning.filters.NotInOrbit;
import ifeed.problem.assigning.filters.Present;
import java.util.*;

public class InOrbit2Present extends AbstractLogicOperator {

    protected InOrbit constraintSetter;
    protected int selectedInstrument;
    protected Connective targetParentNode;
    protected AbstractFilter newFilter;

    public InOrbit2Present(BaseParams params, AbstractMOEABase base) {
        super(params, base);
    }

    public void apply(Connective root,
                         Connective parent,
                         AbstractFilter constraintSetterAbstract,
                         Set<AbstractFilter> matchingFilters,
                         Map<AbstractFilter, Literal> nodes
    ){
        Params params = (Params) super.params;
        constraintSetter = (InOrbit) constraintSetterAbstract;

        // Select an instrument randomly
        List<Integer> instrumentList = new ArrayList<>();
        for(int instrument: constraintSetter.getInstruments()){
            instrumentList.add(instrument);
        }
        Collections.shuffle(instrumentList);
        this.selectedInstrument = instrumentList.get(0);

        // Remove the given node
        Literal constraintSetterLiteral = nodes.get(constraintSetter);
        parent.removeLiteral(constraintSetterLiteral);

        // Create new feature
        this.newFilter = new Present(params, this.selectedInstrument);
        Feature newFeature = this.base.getFeatureFetcher().fetch(this.newFilter);

        if(parent.getLogic() == LogicalConnectiveType.AND){
            this.targetParentNode = parent;
            this.targetParentNode.addLiteral(newFeature.getName(), newFeature.getMatches());

        }else{
            this.targetParentNode = new Connective(LogicalConnectiveType.AND);
            this.targetParentNode.addLiteral(newFeature.getName(), newFeature.getMatches());
            parent.addBranch(this.targetParentNode);
        }

        if(constraintSetter.getInstruments().size() > 1){
            int orbit = constraintSetter.getOrbit();
            ArrayList<Integer> instruments = new ArrayList<>(constraintSetter.getInstruments());
            int selectedArgumentIndex = instruments.indexOf(this.selectedInstrument);
            instruments.remove(selectedArgumentIndex);

            AbstractFilter modifiedFilter = new InOrbit(params, orbit, Utils.intCollection2Array(instruments));
            Feature modifiedFeature = this.base.getFeatureFetcher().fetch(modifiedFilter);

            if(!instruments.isEmpty()){
                this.targetParentNode.addLiteral(modifiedFeature.getName(), modifiedFeature.getMatches());
            }
        }
    }

    @Override
    public String getDescription(){
        InOrbit tempInOrbit = new InOrbit(super.params, constraintSetter.getOrbit(), this.selectedInstrument);
        StringBuilder sb = new StringBuilder();
        sb.append("Generalize ");
        sb.append("\"" + tempInOrbit.getDescription() + "\"");
        sb.append(" to ");
        sb.append("\"" + this.newFilter.getDescription() + "\"");
        return sb.toString();
    }

    @Override
    public void findApplicableNodesUnderGivenParentNode(Connective parent,
                                                        Map<AbstractFilter, Set<AbstractFilter>> applicableFiltersMap,
                                                        Map<AbstractFilter, Literal> applicableLiteralsMap
    ){
        // All Literals and their corresponding Filters are not returned, but the lists are filled up as side effects
        FilterFinder finder = new FilterFinder();
        super.findApplicableNodesUnderGivenParentNode(parent, applicableFiltersMap, applicableLiteralsMap, finder);
    }

    /**
     * Find any InOrbit literal
     */
    public class FilterFinder extends AbstractFilterFinder {

        public FilterFinder(){
            super(InOrbit.class);
        }

        @Override
        public void setConstraints(AbstractFilter constraintSetter){
        }

        @Override
        public void clearConstraints(){
        }

        @Override
        public boolean check(){
            return true;
        }
    }
}
