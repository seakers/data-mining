package ifeed.problem.assigning.logicOperators.generalization.combined.localSearch;

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
import ifeed.problem.assigning.logicOperators.generalization.combined.NotInOrbitsGeneralizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NotInOrbitsGeneralizationWithLocalSearch extends NotInOrbitsGeneralizer{

    private AbstractLocalSearch localSearch;

    public NotInOrbitsGeneralizationWithLocalSearch(BaseParams params, AbstractMOEABase base, AbstractLocalSearch localSearch){
        super(params, base);
        this.localSearch = localSearch;
    }

    public void apply(Connective root,
                         Connective parent,
                         AbstractFilter constraintSetterAbstract,
                         Set<AbstractFilter> matchingFilters,
                         Map<AbstractFilter, Literal> nodes,
                      List<String> description
    ){
        Params params = (Params) super.params;

        super.apply(root, parent, constraintSetterAbstract, matchingFilters, nodes);

        List<Feature> baseFeaturesToTest = new ArrayList<>();
        Set<Integer> orbits = params.getRightSetInstantiation(super.selectedClass);

        Multiset<Integer> instruments = ((NotInOrbit) constraintSetterAbstract).getInstruments();
        for(int o: orbits){
            if(o == super.selectedOrbit){
                continue;
            }
            for(int i: instruments){
                InOrbit inOrbit = new InOrbit(params, o, i);
                baseFeaturesToTest.add(this.base.getFeatureFetcher().fetch(inOrbit));
            }
            InOrbit inOrbit = new InOrbit(params, o, instruments);
            baseFeaturesToTest.add(this.base.getFeatureFetcher().fetch(inOrbit));
        }

        // Add extra conditions to make smaller steps
        List<Feature> addedFeatures = localSearch.addExtraConditions(root, super.targetParentNode, super.newLiteral, baseFeaturesToTest, 3, FeatureMetric.RECALL);

        for(Feature feature: addedFeatures){
            AbstractFilter filter = this.localSearch.getFilterFetcher().fetch(feature.getName());
            description.add(filter.getDescription());
        }
    }

    @Override
    public void apply(Connective root,
                      Connective parent,
                      AbstractFilter constraintSetterAbstract,
                      Set<AbstractFilter> matchingFilters,
                      Map<AbstractFilter, Literal> nodes){

        this.apply(root, parent, constraintSetterAbstract, matchingFilters, nodes, new ArrayList<>());
    }
}
