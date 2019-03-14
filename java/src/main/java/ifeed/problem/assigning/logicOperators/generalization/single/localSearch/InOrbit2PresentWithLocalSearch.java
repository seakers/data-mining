package ifeed.problem.assigning.logicOperators.generalization.single.localSearch;

import ifeed.feature.*;
import ifeed.feature.logic.Connective;
import ifeed.feature.logic.Literal;
import ifeed.filter.AbstractFilter;
import ifeed.local.params.BaseParams;
import ifeed.mining.AbstractLocalSearch;
import ifeed.mining.moea.AbstractMOEABase;
import ifeed.problem.assigning.Params;
import ifeed.problem.assigning.logicOperators.generalization.single.InOrbit2Present;
import ifeed.problem.assigning.filters.NotInOrbit;

import java.util.*;

public class InOrbit2PresentWithLocalSearch extends InOrbit2Present{

    private AbstractLocalSearch localSearch;

    public InOrbit2PresentWithLocalSearch(BaseParams params, AbstractMOEABase base, AbstractLocalSearch localSearch){
        super(params, base);
        this.localSearch = localSearch;
    }

    @Override
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
        for(int o = 0; o < params.getRightSetCardinality() + params.getRightSetGeneralizedConcepts().size() - 1; o++){
            NotInOrbit notInOrbit = new NotInOrbit(params, o, super.selectedInstrument);
            baseFeaturesToTest.add(this.base.getFeatureFetcher().fetch(notInOrbit));
        }

//        for(int i = 0; i < params.getLeftSetCardinality() + params.getLeftSetGeneralizedConcepts().size() - 1; i++){
//
//            if(i == super.selectedInstrument){
//                continue;
//            }
//            int[] instruments2 = new int[2];
//            instruments2[0] = super.selectedInstrument;
//            instruments2[1] = i;
//            Separate separate2 = new Separate(params, instruments2);
//            baseFeaturesToTest.add(this.base.getFeatureFetcher().fetch(separate2));
//
//            for(int j = i+1; j < params.getLeftSetCardinality() + params.getLeftSetGeneralizedConcepts().size() - 1; j++){
//
//                if(j == super.selectedInstrument){
//                    continue;
//                }
//                int[] instruments3 = new int[3];
//                instruments3[0] = super.selectedInstrument;
//                instruments3[1] = i;
//                instruments3[2] = j;
//                Separate separate3 = new Separate(params, instruments3);
//                baseFeaturesToTest.add(this.base.getFeatureFetcher().fetch(separate3));
//            }
//        }

        // Add extra conditions to make smaller steps
        List<Feature> addedFeatures = this.localSearch.addExtraConditions(root, super.targetParentNode, null, baseFeaturesToTest, 3, FeatureMetric.PRECISION);

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
