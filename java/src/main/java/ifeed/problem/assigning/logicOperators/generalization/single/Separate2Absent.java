package ifeed.problem.assigning.logicOperators.generalization.single;

import ifeed.Utils;
import ifeed.feature.Feature;
import ifeed.feature.logic.Connective;
import ifeed.feature.logic.Literal;
import ifeed.feature.logic.LogicalConnectiveType;
import ifeed.filter.AbstractFilter;
import ifeed.filter.AbstractFilterFinder;
import ifeed.local.params.BaseParams;
import ifeed.mining.moea.AbstractMOEABase;
import ifeed.mining.moea.operators.AbstractLogicOperator;
import ifeed.problem.assigning.Params;
import ifeed.problem.assigning.filters.Absent;
import ifeed.problem.assigning.filters.Separate;
import java.util.*;

public class Separate2Absent extends AbstractLogicOperator {

    protected int selectedInstrument;
    protected Connective targetParentNode;
    protected Literal newLiteral;

    public Separate2Absent(BaseParams params, AbstractMOEABase base) {
        super(params, base);
    }

    public void apply(Connective root,
                      Connective parent,
                      AbstractFilter constraintSetterAbstract,
                      Set<AbstractFilter> matchingFilters,
                      Map<AbstractFilter, Literal> nodes
    ){

        Params params = (Params) super.params;

        Separate constraintSetter = (Separate) constraintSetterAbstract;

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

        // Add new node
        AbstractFilter newFilter = new Absent(params, this.selectedInstrument);
        Feature newFeature = this.base.getFeatureFetcher().fetch(newFilter);
        this.newLiteral = new Literal(newFeature.getName(), newFeature.getMatches());

        if(parent.getLogic() == LogicalConnectiveType.AND){
            this.targetParentNode = parent;
            this.targetParentNode.addLiteral(this.newLiteral);

        }else{
            this.targetParentNode = new Connective(LogicalConnectiveType.AND);
            this.targetParentNode.addLiteral(this.newLiteral);
            parent.addBranch(this.targetParentNode);
        }

        if(constraintSetter.getInstruments().size() > 2){
            ArrayList<Integer> instruments = new ArrayList<>(constraintSetter.getInstruments());
            int selectedArgumentIndex = instruments.indexOf(this.selectedInstrument);
            instruments.remove(selectedArgumentIndex);

            AbstractFilter modifiedFilter = new Separate(params, Utils.intCollection2Array(instruments));
            Feature modifiedFeature = this.base.getFeatureFetcher().fetch(modifiedFilter);

            if(!instruments.isEmpty()){
                this.targetParentNode.addLiteral(modifiedFeature.getName(), modifiedFeature.getMatches());
            }
        }
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
            super(Separate.class);
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
