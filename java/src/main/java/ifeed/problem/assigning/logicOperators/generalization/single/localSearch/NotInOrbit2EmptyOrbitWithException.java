package ifeed.problem.assigning.logicOperators.generalization.single.localSearch;

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
import ifeed.problem.assigning.filters.NotInOrbitExceptInstrument;
import ifeed.problem.assigning.filtersWithException.EmptyOrbitWithException;
import ifeed.problem.assigning.logicOperators.generalization.single.NotInOrbit2EmptyOrbit;
import ifeed.problem.assigning.logicOperators.generalization.single.NotInOrbitInstrGeneralizer;

import java.util.*;

public class NotInOrbit2EmptyOrbitWithException extends NotInOrbit2EmptyOrbit{

    AbstractLocalSearch localSearch;
    private List<Feature> addedFeatures;

    public NotInOrbit2EmptyOrbitWithException(BaseParams params, AbstractMOEABase base, AbstractLocalSearch localSearch){
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

        // Remove EmptyOrbit node
        parent.removeLiteral(super.newLiteral);

        List<Feature> baseFeaturesToTest = new ArrayList<>();
        int orbit = ((NotInOrbit) constraintSetterAbstract).getOrbit();

        Set<Integer> restrictedInstrumentSet = super.constraintSetter.getInstruments().elementSet();
        for(int i = 0; i < params.getLeftSetCardinality() + params.getLeftSetGeneralizedConcepts().size(); i++){
            if(restrictedInstrumentSet.contains(i)){
                continue;
            }

            Set<Integer> orbitException = new HashSet<>();
            Set<Integer> instrumentException = new HashSet<>();
            instrumentException.add(i);
            EmptyOrbitWithException emptyOrbitWithException = new EmptyOrbitWithException(params, orbit, orbitException, instrumentException);
            Feature baseFeature = this.base.getFeatureFetcher().fetch(emptyOrbitWithException);
            baseFeature.setNumExceptions(1);
            baseFeaturesToTest.add(baseFeature);

            for(int j = i + 1; j < params.getLeftSetCardinality(); j++){
                if(restrictedInstrumentSet.contains(j)){
                    continue;
                }
                instrumentException = new HashSet<>();
                instrumentException.add(i);
                instrumentException.add(j);
                emptyOrbitWithException = new EmptyOrbitWithException(params, orbit, orbitException, instrumentException);
                baseFeature = this.base.getFeatureFetcher().fetch(emptyOrbitWithException);
                baseFeature.setNumExceptions(2);
                baseFeaturesToTest.add(baseFeature);
            }
        }

        addedFeatures = this.localSearch.addExtraConditions(root, parent, null, baseFeaturesToTest, 1, FeatureMetric.DISTANCE2UP);
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

//        StringBuilder sb = new StringBuilder();
//        sb.append("Generalize ");
//        sb.append("\"" + constraintSetter.getDescription() + "\"");
//        sb.append(" to ");
//
//        if(addedFeatures.isEmpty()){
//            sb.append("\"" + this.newFilter.getDescription() + "\"");
//        }else{
//            sb.append("\"" +
//                    this.localSearch.getFilterFetcher().fetch(addedFeatures.get(0).getName()).getDescription() + "\"");
//        }
//        description.add(sb.toString());
    }
}
