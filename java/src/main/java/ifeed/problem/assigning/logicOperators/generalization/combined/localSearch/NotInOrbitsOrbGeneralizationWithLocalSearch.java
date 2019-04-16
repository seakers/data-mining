package ifeed.problem.assigning.logicOperators.generalization.combined.localSearch;

import com.google.common.collect.Multiset;
import ifeed.feature.Feature;
import ifeed.feature.FeatureMetric;
import ifeed.feature.logic.Connective;
import ifeed.feature.logic.Literal;
import ifeed.filter.AbstractFilter;
import ifeed.local.params.BaseParams;
import ifeed.mining.AbstractLocalSearch;
import ifeed.mining.moea.AbstractMOEABase;
import ifeed.problem.assigning.Params;
import ifeed.problem.assigning.filters.InOrbit;
import ifeed.problem.assigning.filters.NotInOrbit;
import ifeed.problem.assigning.logicOperators.generalization.combined.NotInOrbitsOrbGeneralizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NotInOrbitsOrbGeneralizationWithLocalSearch extends NotInOrbitsOrbGeneralizer {

    private AbstractLocalSearch localSearch;
    private List<Feature> addedFeatures;

    public NotInOrbitsOrbGeneralizationWithLocalSearch(BaseParams params, AbstractMOEABase base, AbstractLocalSearch localSearch){
        super(params, base);
        this.localSearch = localSearch;
    }

    public void apply(Connective root,
                         Connective parent,
                         AbstractFilter constraintSetterAbstract,
                         Set<AbstractFilter> matchingFilters,
                         Map<AbstractFilter, Literal> nodes
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
            InOrbit inOrbit = new InOrbit(params, o, super.selectedInstrument);
            baseFeaturesToTest.add(this.base.getFeatureFetcher().fetch(inOrbit));

            inOrbit = new InOrbit(params, o, instruments);
            baseFeaturesToTest.add(this.base.getFeatureFetcher().fetch(inOrbit));
        }

        // Add extra conditions to make smaller steps
        addedFeatures = localSearch.addExtraConditions(root, super.targetParentNode, super.newLiteral, baseFeaturesToTest, 3, FeatureMetric.RECALL);
    }


    @Override
    public void apply(Connective root,
                      Connective parent,
                      AbstractFilter constraintSetterAbstract,
                      Set<AbstractFilter> matchingFilters,
                      Map<AbstractFilter, Literal> nodes,
                      List<String> description
    ){
        this.apply(root, parent, constraintSetterAbstract, matchingFilters, nodes);
        description.add(this.getDescription());

        for(Feature feature: this.addedFeatures){
            AbstractFilter filter = this.localSearch.getFilterFetcher().fetch(feature.getName());
            description.add(filter.getDescription());
        }
    }
}
