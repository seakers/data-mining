package ifeed.problem.assigning.logicOperators.generalization.combined.localSearch;

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
import ifeed.problem.assigning.filters.Separate;
import ifeed.problem.assigning.filtersWithException.AbsentWithException;
import ifeed.problem.assigning.filtersWithException.SeparateWithException;
import ifeed.problem.assigning.logicOperators.generalization.combined.NotInOrbits2Absent;
import ifeed.problem.assigning.logicOperators.generalization.combined.Separates2Absent;

import java.util.*;

public class Separates2AbsentWithException extends Separates2Absent{

    private AbstractLocalSearch localSearch;
    private List<Feature> addedFeatures;

    public Separates2AbsentWithException(BaseParams params, AbstractMOEABase base, AbstractLocalSearch localSearch){
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
        
        // Remove Absent node
        parent.removeLiteral(super.newLiteral);

        List<Feature> baseFeaturesToTest = new ArrayList<>();
        for(int o = 0; o < params.getRightSetCardinality() + params.getRightSetGeneralizedConcepts().size(); o++){
            HashSet<Integer> orbitExceptions = new HashSet<>();
            orbitExceptions.add(o);

            AbsentWithException absentWithException = new AbsentWithException(params, super.selectedInstrument, orbitExceptions, new HashSet<>());
            GeneralizableFeature baseFeature = new GeneralizableFeature(this.base.getFeatureFetcher().fetch(absentWithException).copy());
            baseFeature.setNumGeneralizations(1);
            baseFeature.setNumExceptionVariables(1);
            baseFeaturesToTest.add(baseFeature);
        }
        baseFeaturesToTest.add(super.newFeature);

        // The operation "notInOrbit -> absent" improves precision, so look for exception that improves recall
        addedFeatures = this.localSearch.addExtraConditions(root, super.targetParentNode, null, baseFeaturesToTest, 1, FeatureMetric.DISTANCE2UP);
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

//        Params params = (Params) this.params;
//        StringBuilder sb = new StringBuilder();
//        sb.append("Generalize ");
//        sb.append("\"Instrument " + params.getLeftSetEntityName(this.selectedInstrument) + " is not assigned to any of the orbits {");
//        StringJoiner orbitNamesJoiner = new StringJoiner(", ");
//        for(AbstractFilter filter: this.filtersToBeModified){
//            NotInOrbit notInOrbit = (NotInOrbit) filter;
//            orbitNamesJoiner.add(params.getRightSetEntityName(notInOrbit.getOrbit()));
//        }
//        sb.append(orbitNamesJoiner.toString() + "}\"");
//        sb.append(" to ");
//
//        if(this.addedFeatures.isEmpty()){
//            sb.append("\"" + this.newFilter.getDescription() + "\"");
//        }else{
//            AbstractFilter filter = this.localSearch.getFilterFetcher().fetch(this.addedFeatures.get(0).getName());
//            sb.append("\"" + filter.getDescription() + "\"");
//        }
//        description.add(sb.toString());
    }
}
