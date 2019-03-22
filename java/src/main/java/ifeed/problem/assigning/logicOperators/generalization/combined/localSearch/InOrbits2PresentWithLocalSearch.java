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
import ifeed.problem.assigning.filters.NumInstruments;
import ifeed.problem.assigning.logicOperators.generalization.combined.InOrbits2Present;
import ifeed.problem.assigning.logicOperators.generalization.single.InOrbit2Present;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InOrbits2PresentWithLocalSearch extends InOrbits2Present{

    private AbstractLocalSearch localSearch;
    private List<Feature> addedFeatures;

    public InOrbits2PresentWithLocalSearch(BaseParams params, AbstractMOEABase base, AbstractLocalSearch localSearch){
        super(params, base);
        this.localSearch = localSearch;
    }

    @Override
    public void apply(Connective root,
                      Connective parent,
                      AbstractFilter constraintSetterAbstract,
                      Set<AbstractFilter> matchingFilters,
                      Map<AbstractFilter, Literal> nodes){

        Params params = (Params) super.params;

        super.apply(root, parent, constraintSetterAbstract, matchingFilters, nodes);

        List<Feature> baseFeaturesToTest = new ArrayList<>();
        for(int o = 0; o < params.getRightSetCardinality() + params.getRightSetGeneralizedConcepts().size() - 1; o++){
            NotInOrbit notInOrbit = new NotInOrbit(params, o, super.selectedInstrument);
            baseFeaturesToTest.add(this.base.getFeatureFetcher().fetch(notInOrbit));
        }

        for(int i = 1; i < params.getRightSetCardinality(); i++){
            int[] nBounds = new int[2];
            nBounds[0] = 1;
            nBounds[1] = i;
            NumInstruments numInstruments = new NumInstruments(params, -1, super.selectedInstrument, nBounds);
            baseFeaturesToTest.add(this.base.getFeatureFetcher().fetch(numInstruments));
            if(i > 1){
                numInstruments = new NumInstruments(params, -1, super.selectedInstrument, i);
                baseFeaturesToTest.add(this.base.getFeatureFetcher().fetch(numInstruments));
            }
        }

        // Add extra conditions to make smaller steps
        this.addedFeatures = localSearch.addExtraConditions(root, super.targetParentNodes, null, baseFeaturesToTest, 3, FeatureMetric.PRECISION);
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
