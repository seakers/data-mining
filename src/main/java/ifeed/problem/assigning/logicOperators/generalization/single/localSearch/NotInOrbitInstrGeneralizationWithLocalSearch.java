package ifeed.problem.assigning.logicOperators.generalization.single.localSearch;

import com.google.common.collect.Multiset;
import ifeed.feature.Feature;
import ifeed.feature.FeatureMetric;
import ifeed.feature.logic.Connective;
import ifeed.feature.logic.Literal;
import ifeed.feature.logic.LogicalConnectiveType;
import ifeed.filter.AbstractFilter;
import ifeed.local.params.BaseParams;
import ifeed.mining.AbstractLocalSearch;
import ifeed.mining.moea.AbstractMOEABase;
import ifeed.problem.assigning.Params;
import ifeed.problem.assigning.filters.InOrbit;
import ifeed.problem.assigning.filters.NotInOrbit;
import ifeed.problem.assigning.logicOperators.generalization.single.NotInOrbitInstrGeneralizer;

import java.util.*;

public class NotInOrbitInstrGeneralizationWithLocalSearch extends NotInOrbitInstrGeneralizer{

    AbstractLocalSearch localSearch;
    private List<Feature> addedFeatures;

    public NotInOrbitInstrGeneralizationWithLocalSearch(BaseParams params, AbstractMOEABase base, AbstractLocalSearch localSearch){
        super(params, base);
        this.localSearch = localSearch;
    }

    public boolean apply(Connective root,
                         Connective parent,
                         AbstractFilter constraintSetterAbstract,
                         Set<AbstractFilter> matchingFilters,
                         Map<AbstractFilter, Literal> nodes
    ){
        Params params = (Params) super.params;

        Multiset<Integer> originalInstrumentSet = ((NotInOrbit)constraintSetterAbstract).getInstruments();

        super.apply(root, parent, constraintSetterAbstract, matchingFilters, nodes);

        List<Feature> baseFeaturesToTest = new ArrayList<>();
        int orbit = ((NotInOrbit) constraintSetterAbstract).getOrbit();
        Set<Integer> instrumentInstances = params.getLeftSetInstantiation(super.selectedClass);

        for(int instr: instrumentInstances){
            if(originalInstrumentSet.contains(instr)){
                continue;
            }
            InOrbit inOrbit = new InOrbit(params, orbit, instr);
            baseFeaturesToTest.add(this.base.getFeatureFetcher().fetch(inOrbit));
        }

        Literal literalToBeCombined;
        if(parent.getLogic() == LogicalConnectiveType.OR){
            literalToBeCombined = null;
        }else{
            literalToBeCombined = super.newLiteral;
        }

        addedFeatures = this.localSearch.addExtraConditions(root, super.targetParentNode, literalToBeCombined, baseFeaturesToTest, 1, FeatureMetric.RECALL);

        return true;
    }

    @Override
    public boolean apply(Connective root,
                      Connective parent,
                      AbstractFilter constraintSetterAbstract,
                      Set<AbstractFilter> matchingFilters,
                      Map<AbstractFilter, Literal> nodes,
                      List<String> description
    ){
        this.apply(root, parent, constraintSetterAbstract, matchingFilters, nodes);
        description.add(this.getDescription());

        StringJoiner sj = new StringJoiner(" AND ");
        for(Feature feature: this.addedFeatures){
            AbstractFilter filter = this.localSearch.getFilterFetcher().fetch(feature.getName());
            sj.add(filter.getDescription());
        }
        StringBuilder sb = new StringBuilder();
        if(!this.addedFeatures.isEmpty()){
            sb.append("with an exception: ");
        }
        sb.append(sj.toString());
        description.add(sb.toString());

        return true;
    }
}
