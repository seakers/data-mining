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
import ifeed.problem.assigning.filters.InOrbit;
import ifeed.problem.assigning.filters.Together;
import ifeed.problem.assigning.logicOperators.generalization.combined.NotInOrbits2Absent;
import ifeed.problem.assigning.logicOperators.generalization.single.NotInOrbit2Absent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NotInOrbits2AbsentWithLocalSearch extends NotInOrbits2Absent{

    private AbstractLocalSearch localSearch;
    private List<Feature> addedFeatures;

    public NotInOrbits2AbsentWithLocalSearch(BaseParams params, AbstractMOEABase base, AbstractLocalSearch localSearch){
        super(params, base);
        this.localSearch = localSearch;
    }

    @Override
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
            InOrbit inOrbit = new InOrbit(params, o, super.selectedInstrument);
            baseFeaturesToTest.add(this.base.getFeatureFetcher().fetch(inOrbit));
        }

        for(int i = 0; i < params.getLeftSetCardinality() + params.getLeftSetGeneralizedConcepts().size() - 1; i++){

            if(i == super.selectedInstrument){
                continue;
            }
            int[] instruments2 = new int[2];
            instruments2[0] = super.selectedInstrument;
            instruments2[1] = i;
            Together together2 = new Together(params, instruments2);
            baseFeaturesToTest.add(this.base.getFeatureFetcher().fetch(together2));

            for(int j = i+1; j < params.getLeftSetCardinality() + params.getLeftSetGeneralizedConcepts().size() - 1; j++){

                if(j == super.selectedInstrument){
                    continue;
                }
                int[] instruments3 = new int[3];
                instruments3[0] = super.selectedInstrument;
                instruments3[1] = i;
                instruments3[2] = j;
                Together together3 = new Together(params, instruments3);
                baseFeaturesToTest.add(this.base.getFeatureFetcher().fetch(together3));
            }
        }

        // Add an exception to make smaller steps
        // The operation "notInOrbit -> absent" improves precision, so look for exception that improves recall
        addedFeatures = this.localSearch.addExtraConditions(root, super.targetParentNode, super.newLiteral, baseFeaturesToTest, 3, FeatureMetric.RECALL);
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
