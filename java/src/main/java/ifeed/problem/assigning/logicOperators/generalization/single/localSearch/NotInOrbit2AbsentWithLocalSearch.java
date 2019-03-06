package ifeed.problem.assigning.logicOperators.generalization.single.localSearch;

import ifeed.Utils;
import ifeed.feature.*;
import ifeed.feature.logic.Connective;
import ifeed.feature.logic.Literal;
import ifeed.filter.AbstractFilter;
import ifeed.local.params.BaseParams;
import ifeed.mining.AbstractLocalSearch;
import ifeed.mining.moea.AbstractMOEABase;
import ifeed.problem.assigning.Params;
import ifeed.problem.assigning.filters.InOrbit;
import ifeed.problem.assigning.logicOperators.generalization.single.NotInOrbit2Absent;

import java.util.*;

public class NotInOrbit2AbsentWithLocalSearch extends NotInOrbit2Absent{

    AbstractLocalSearch localSearch;

    public NotInOrbit2AbsentWithLocalSearch(BaseParams params, AbstractMOEABase base, AbstractLocalSearch localSearch){
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

        List<Feature> baseFeaturesToTest = new ArrayList<>();
        for(int o = 0; o < params.getRightSetCardinality() + params.getRightSetGeneralizedConcepts().size() - 1; o++){
            InOrbit inOrbit = new InOrbit(params, o, super.selectedInstrument);
            baseFeaturesToTest.add(this.base.getFeatureFetcher().fetch(inOrbit));
        }
        // Add an exception to make smaller steps
        // The operation "notInOrbit -> absent" improves precision, so look for exception that improves recall
        this.localSearch.addExtraCondition(root, super.targetParentNode, super.newLiteral, baseFeaturesToTest, 3, FeatureMetric.RCONFIDENCE);

    }
}
