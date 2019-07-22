package ifeed.problem.assigning.logicOperators.generalization.single.localSearch;

import ifeed.feature.Feature;
import ifeed.feature.FeatureMetric;
import ifeed.feature.GeneralizableFeature;
import ifeed.feature.logic.Connective;
import ifeed.feature.logic.Literal;
import ifeed.filter.AbstractFilter;
import ifeed.local.params.BaseParams;
import ifeed.mining.AbstractLocalSearch;
import ifeed.mining.moea.AbstractMOEABase;
import ifeed.problem.assigning.Params;
import ifeed.problem.assigning.filters.NotInOrbit;
import ifeed.problem.assigning.filtersWithException.EmptyOrbitWithException;
import ifeed.problem.assigning.logicOperators.generalization.single.NotInOrbit2EmptyOrbit;

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
            Set<Integer> instrumentException = new HashSet<>();
            instrumentException.add(i);
            EmptyOrbitWithException emptyOrbitWithException = new EmptyOrbitWithException(params, orbit, new HashSet<>(), instrumentException);
            GeneralizableFeature baseFeature = new GeneralizableFeature(this.base.getFeatureFetcher().fetch(emptyOrbitWithException));
            baseFeature.setNumGeneralizations(1);
            baseFeature.setNumExceptionVariables(1);
            baseFeaturesToTest.add(baseFeature);

            for(int j = i + 1; j < params.getLeftSetCardinality(); j++){
                if(restrictedInstrumentSet.contains(j)){
                    continue;
                }
                Set<Integer> instrumentException2 = new HashSet<>();
                instrumentException2.add(i);
                instrumentException2.add(j);
                EmptyOrbitWithException emptyOrbitWithException2 = new EmptyOrbitWithException(params, orbit, new HashSet<>(), instrumentException2);
                GeneralizableFeature baseFeature2 = new GeneralizableFeature(this.base.getFeatureFetcher().fetch(emptyOrbitWithException2));
                baseFeature2.setNumGeneralizations(1);
                baseFeature2.setNumExceptionVariables(2);
                baseFeaturesToTest.add(baseFeature2);
            }
        }
        baseFeaturesToTest.add(super.newFeature);

        addedFeatures = this.localSearch.addExtraConditions(root, parent, null, baseFeaturesToTest, 1, FeatureMetric.DISTANCE2UP, false);
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

        StringBuilder sb = new StringBuilder();
        sb.append("Generalize ");
        sb.append("\"" + constraintSetter.getDescription() + "\"");
        sb.append(" to ");

        if(addedFeatures.isEmpty()){
            throw new IllegalStateException();
        }else{
            sb.append("\"" +
                    this.localSearch.getFilterFetcher().fetch(addedFeatures.get(0).getName()).getDescription() + "\"");
        }
        description.add(sb.toString());
    }
}
