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
import ifeed.problem.assigning.filters.AbsentExceptInOrbit;
import ifeed.problem.assigning.filters.InOrbit;
import ifeed.problem.assigning.filters.NotInOrbit;
import ifeed.problem.assigning.filters.Together;
import ifeed.problem.assigning.logicOperators.generalization.combined.NotInOrbits2Absent;
import ifeed.problem.assigning.logicOperators.generalization.single.NotInOrbit2Absent;

import java.util.*;

public class NotInOrbits2AbsentWithException extends NotInOrbits2Absent{

    private AbstractLocalSearch localSearch;
    private List<Feature> addedFeatures;

    public NotInOrbits2AbsentWithException(BaseParams params, AbstractMOEABase base, AbstractLocalSearch localSearch){
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

        Set<Integer> restrictedOrbits = new HashSet<>();
        for(AbstractFilter filter: filtersToBeModified){
            int orbit = ((NotInOrbit) filter).getOrbit();
            restrictedOrbits.add(orbit);
            restrictedOrbits.addAll(params.getRightSetSuperclass(orbit));
        }

        List<Feature> baseFeaturesToTest = new ArrayList<>();
        for(int o = 0; o < params.getRightSetCardinality() - 1; o++){
            if(restrictedOrbits.contains(o)){
                continue;
            }
            AbsentExceptInOrbit absentExceptInOrbit = new AbsentExceptInOrbit(params, o, super.selectedInstrument);
            baseFeaturesToTest.add(this.base.getFeatureFetcher().fetch(absentExceptInOrbit));

            for(int p = o + 1; p < params.getRightSetCardinality(); p++){
                if(restrictedOrbits.contains(p)){
                    continue;
                }
                Set<Integer> orbitSet = new HashSet<>();
                orbitSet.add(o);
                orbitSet.add(p);
                AbsentExceptInOrbit absentExceptInOrbit2 = new AbsentExceptInOrbit(params, orbitSet, super.selectedInstrument);
                baseFeaturesToTest.add(this.base.getFeatureFetcher().fetch(absentExceptInOrbit2));
            }
        }

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
            sb.append("\"" + this.newFilter.getDescription() + "\"");
        }else{
            AbstractFilter filter = this.localSearch.getFilterFetcher().fetch(this.addedFeatures.get(0).getName());
            sb.append("\"" + filter.getDescription() + "\"");
        }
        description.add(sb.toString());
    }
}
