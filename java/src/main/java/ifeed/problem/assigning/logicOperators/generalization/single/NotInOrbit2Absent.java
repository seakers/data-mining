package ifeed.problem.assigning.logicOperators.generalization.single;

import ifeed.Utils;
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
import ifeed.problem.assigning.filters.Absent;
import ifeed.problem.assigning.filters.NotInOrbit;
import java.util.*;

public class NotInOrbit2Absent extends AbstractLogicOperator {

    protected NotInOrbit constraintSetter;
    protected int selectedInstrument;
    protected Connective targetParentNode;
    protected AbstractFilter newFilter;
    protected Literal newLiteral;

    public NotInOrbit2Absent(BaseParams params, AbstractMOEABase base) {
        super(params, base);
    }

    @Override
    public boolean apply(Connective root,
                      Connective parent,
                      AbstractFilter constraintSetterAbstract,
                      Set<AbstractFilter> matchingFilters,
                      Map<AbstractFilter, Literal> nodes
    ){
        Params params = (Params) super.params;
        constraintSetter = (NotInOrbit) constraintSetterAbstract;

        // Select an instrument randomly
        List<Integer> instrumentList = new ArrayList<>();
        for(int instrument: constraintSetter.getInstruments()){
            instrumentList.add(instrument);
        }
        Collections.shuffle(instrumentList);
        this.selectedInstrument = instrumentList.get(0);

        // Remove node
        Literal constraintSetterLiteral = nodes.get(constraintSetter);
        parent.removeLiteral(constraintSetterLiteral);

        // Define the new literal
        this.newFilter = new Absent(params, this.selectedInstrument);
        Feature newFeature = this.base.getFeatureFetcher().fetch(newFilter);
        this.newLiteral = new Literal(newFeature.getName(), newFeature.getMatches());

        // Add the new node under AND
        if(parent.getLogic() == LogicalConnectiveType.AND){
            this.targetParentNode = parent;
            this.targetParentNode.addNode(newLiteral);

        }else{
            this.targetParentNode = new Connective(LogicalConnectiveType.AND);
            this.targetParentNode.addNode(newLiteral);
            parent.addBranch(this.targetParentNode);
        }

        if(constraintSetter.getInstruments().size() > 1){
            int orbit = constraintSetter.getOrbit();
            ArrayList<Integer> instruments = new ArrayList<>(constraintSetter.getInstruments());
            int selectedArgumentIndex = instruments.indexOf(this.selectedInstrument);
            instruments.remove(selectedArgumentIndex);

            AbstractFilter modifiedFilter = new NotInOrbit(params, orbit, Utils.intCollection2Array(instruments));
            Feature modifiedFeature = this.base.getFeatureFetcher().fetch(modifiedFilter);

            if(!instruments.isEmpty()){
                this.targetParentNode.addLiteral(modifiedFeature.getName(), modifiedFeature.getMatches());
            }
        }
        return true;
    }

    @Override
    public String getDescription(){
        NotInOrbit tempNotInOrbit = new NotInOrbit(super.params, constraintSetter.getOrbit(), this.selectedInstrument);
        StringBuilder sb = new StringBuilder();
        sb.append("Generalize ");
        sb.append("\"" + tempNotInOrbit.getDescription() + "\"");
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
            super(NotInOrbit.class);
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
