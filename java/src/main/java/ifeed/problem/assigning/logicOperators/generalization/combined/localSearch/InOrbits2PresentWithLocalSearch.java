package ifeed.problem.assigning.logicOperators.generalization.combined.localSearch;

import ifeed.feature.Feature;
import ifeed.feature.FeatureMetric;
import ifeed.feature.logic.Connective;
import ifeed.feature.logic.Literal;
import ifeed.filter.AbstractFilter;
import ifeed.local.params.BaseParams;
import ifeed.mining.AbstractLocalSearch;
import ifeed.mining.moea.AbstractMOEABase;
import ifeed.problem.assigning.Params;
import ifeed.problem.assigning.filters.NotInOrbit;
import ifeed.problem.assigning.logicOperators.generalization.combined.InOrbits2Present;
import ifeed.problem.assigning.logicOperators.generalization.single.InOrbit2Present;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InOrbits2PresentWithLocalSearch extends InOrbits2Present{

    private AbstractLocalSearch localSearch;

    public InOrbits2PresentWithLocalSearch(BaseParams params, AbstractMOEABase base, AbstractLocalSearch localSearch){
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
        for(int o = 0; o < params.getRightSetCardinality() + params.getRightSetGeneralizedConcepts().size() - 1; o++){
            NotInOrbit notInOrbit = new NotInOrbit(params, o, super.selectedInstrument);
            baseFeaturesToTest.add(this.base.getFeatureFetcher().fetch(notInOrbit));
        }

        // Add extra conditions to make smaller steps
        localSearch.addExtraConditions(root, super.targetParentNodes, null, baseFeaturesToTest, 3, FeatureMetric.PRECISION);
    }
}