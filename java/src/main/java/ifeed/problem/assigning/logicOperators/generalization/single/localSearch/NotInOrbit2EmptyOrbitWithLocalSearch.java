package ifeed.problem.assigning.logicOperators.generalization.single.localSearch;

import ifeed.feature.*;
import ifeed.feature.logic.Connective;
import ifeed.feature.logic.Literal;
import ifeed.filter.AbstractFilter;
import ifeed.local.params.BaseParams;
import ifeed.mining.AbstractLocalSearch;
import ifeed.mining.moea.AbstractMOEABase;
import ifeed.problem.assigning.Params;
import ifeed.problem.assigning.filters.InOrbit;
import ifeed.problem.assigning.logicOperators.generalization.single.NotInOrbit2EmptyOrbit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NotInOrbit2EmptyOrbitWithLocalSearch extends NotInOrbit2EmptyOrbit {

    AbstractLocalSearch localSearch;

    public NotInOrbit2EmptyOrbitWithLocalSearch(BaseParams params, AbstractMOEABase base, AbstractLocalSearch localSearch) {
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
        for(int i = 0; i < params.getRightSetCardinality() + params.getRightSetGeneralizedConcepts().size() - 1; i++){
            InOrbit inOrbit = new InOrbit(params, super.orbit, i);
            baseFeaturesToTest.add(this.base.getFeatureFetcher().fetch(inOrbit));
        }

        // Add an exception to make smaller steps
        // The operation "notInOrbit -> emptyOrbit" improves precision, so look for exception that improves recall
        List<Feature> addedFeatures = localSearch.addExtraConditions(root, parent, super.newLiteral, baseFeaturesToTest, 3, FeatureMetric.RECALL);

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
                      Map<AbstractFilter, Literal> nodes
    ){
        this.apply(root, parent, constraintSetterAbstract, matchingFilters, nodes, new ArrayList<>());
    }
}
