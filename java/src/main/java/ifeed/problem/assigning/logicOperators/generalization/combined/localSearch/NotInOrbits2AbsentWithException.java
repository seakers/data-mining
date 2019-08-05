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
import ifeed.problem.assigning.filtersWithException.AbsentWithException;
import ifeed.problem.assigning.logicOperators.generalization.combined.NotInOrbits2Absent;

import java.util.*;

public class NotInOrbits2AbsentWithException extends NotInOrbits2Absent{

    private AbstractLocalSearch localSearch;
    private List<Feature> addedFeatures;

    public NotInOrbits2AbsentWithException(BaseParams params, AbstractMOEABase base, AbstractLocalSearch localSearch){
        super(params, base);
        this.localSearch = localSearch;
    }

    @Override
    public boolean apply(Connective root,
                      Connective parent,
                      AbstractFilter constraintSetterAbstract,
                      Set<AbstractFilter> matchingFilters,
                      Map<AbstractFilter, Literal> nodes
    ){
        if(!super.apply(root, parent, constraintSetterAbstract, matchingFilters, nodes)){
            return false;
        }

        Params params = (Params) super.params;
        this.addedFeatures = new ArrayList<>();

        // Remove Absent node
        parent.removeLiteral(super.newLiteral);

        Set<Integer> restrictedOrbits = new HashSet<>();
        for(AbstractFilter filter: filtersToBeModified){
            int orbit = ((NotInOrbit) filter).getOrbit();
            restrictedOrbits.add(orbit);
            restrictedOrbits.addAll(params.getRightSetSuperclass(orbit, true));
        }

        List<Feature> baseFeaturesToTest = new ArrayList<>();
        for(int o = 0; o < params.getRightSetCardinality() + params.getRightSetGeneralizedConcepts().size(); o++){
            if(restrictedOrbits.contains(o)){
                continue;
            }
            HashSet<Integer> orbitExceptions = new HashSet<>();
            orbitExceptions.add(o);
            AbsentWithException absentWithException = new AbsentWithException(params, super.selectedInstrument, orbitExceptions, new HashSet<>());
            GeneralizableFeature baseFeature = new GeneralizableFeature(this.base.getFeatureFetcher().fetch(absentWithException));
            baseFeature.setNumGeneralizations(1);
            baseFeature.setNumExceptionVariables(1);
            baseFeaturesToTest.add(baseFeature);
        }
        GeneralizableFeature baseFeature = new GeneralizableFeature(super.newFeature);
        baseFeature.setNumGeneralizations(1);
        baseFeature.setNumExceptionVariables(0);
        baseFeaturesToTest.add(baseFeature);

        addedFeatures = this.localSearch.addExtraConditions(root, super.targetParentNode, null, baseFeaturesToTest, 1, FeatureMetric.DISTANCE2UP, false);
        return true;
    }

    @Override
    public boolean apply(Connective root,
                      Connective parent,
                      AbstractFilter constraintSetterAbstract,
                      Set<AbstractFilter> matchingFilters,
                      Map<AbstractFilter, Literal> nodes,
                      List<String> description
    ){
        if(!this.apply(root, parent, constraintSetterAbstract, matchingFilters, nodes)){
            return false;
        }

        Params params = (Params) this.params;
        StringBuilder sb = new StringBuilder();
        sb.append("Generalize ");
        sb.append("\"Instrument " + params.getLeftSetEntityName(this.selectedInstrument) + " is not assigned to any of the orbits {");
        StringJoiner orbitNamesJoiner = new StringJoiner(", ");
        for(AbstractFilter filter: this.filtersToBeModified){
            NotInOrbit notInOrbit = (NotInOrbit) filter;
            orbitNamesJoiner.add(params.getRightSetEntityName(notInOrbit.getOrbit()));
        }
        sb.append(orbitNamesJoiner.toString() + "}\"");
        sb.append(" to ");

        if(this.addedFeatures.isEmpty()){
            throw new IllegalStateException();
        }else{
            AbstractFilter filter = this.localSearch.getFilterFetcher().fetch(this.addedFeatures.get(0).getName());
            sb.append("\"" + filter.getDescription() + "\"");
        }
        description.add(sb.toString());

        return true;
    }
}
