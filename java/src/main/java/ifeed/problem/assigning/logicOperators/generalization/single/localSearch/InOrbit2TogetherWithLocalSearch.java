package ifeed.problem.assigning.logicOperators.generalization.single.localSearch;

import ifeed.feature.*;
import ifeed.feature.logic.Connective;
import ifeed.feature.logic.Literal;
import ifeed.filter.AbstractFilter;
import ifeed.local.params.BaseParams;
import ifeed.mining.AbstractLocalSearch;
import ifeed.mining.moea.AbstractMOEABase;
import ifeed.problem.assigning.Params;
import ifeed.problem.assigning.filters.NotInOrbit;
import ifeed.problem.assigning.logicOperators.generalization.single.InOrbit2Together;

import java.util.*;

public class InOrbit2TogetherWithLocalSearch extends InOrbit2Together{

    AbstractLocalSearch localSearch;

    public InOrbit2TogetherWithLocalSearch(BaseParams params, AbstractMOEABase base, AbstractLocalSearch localSearch){
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
        for(int o = 0; o < params.getRightSetCardinality() + params.getRightSetGeneralizedConcepts().size() - 1; o++){
            NotInOrbit notInOrbit = new NotInOrbit(params, o, super.selectedInstruments);
            baseFeaturesToTest.add(this.base.getFeatureFetcher().fetch(notInOrbit));
        }

//        for(int instr: super.selectedInstruments){
//            for(int i = 0; i < params.getLeftSetCardinality() + params.getLeftSetGeneralizedConcepts().size() - 1; i++){
//                if(instr == i){
//                    continue;
//                }
//                List<Integer> instruments = new ArrayList<>();
//                instruments.add(instr);
//                instruments.add(i);
//                Separate separate = new Separate(params, instruments);
//                baseFeaturesToTest.add(this.base.getFeatureFetcher().fetch(separate));
//            }
//        }

        // Add extra conditions to make smaller steps
        List<Feature> addedFeatures = localSearch.addExtraConditions(root, super.targetParentNode, null, baseFeaturesToTest, 3, FeatureMetric.PRECISION);

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
