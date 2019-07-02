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
import ifeed.problem.assigning.filters.NotInOrbitExceptOrbit;
import ifeed.problem.assigning.logicOperators.generalization.combined.NotInOrbitsOrbGeneralizer;

import java.util.*;

public class NotInOrbitsOrbGeneralizationWithException extends NotInOrbitsOrbGeneralizer {

    private AbstractLocalSearch localSearch;
    private List<Feature> addedFeatures;

    public NotInOrbitsOrbGeneralizationWithException(BaseParams params, AbstractMOEABase base, AbstractLocalSearch localSearch){
        super(params, base);
        this.localSearch = localSearch;
    }

    public void apply(Connective root,
                         Connective parent,
                         AbstractFilter constraintSetter,
                         Set<AbstractFilter> matchingFilters,
                         Map<AbstractFilter, Literal> nodes
    ){
        Params params = (Params) super.params;

        super.apply(root, parent, constraintSetter, matchingFilters, nodes);

        // Remove NotInOrbit node
        parent.removeLiteral(super.newLiteral);

        List<Feature> baseFeaturesToTest = new ArrayList<>();
        Set<Integer> orbits = params.getRightSetInstantiation(super.selectedClass);

        Set<Integer> restrictedOrbits = new HashSet<>();
        for(AbstractFilter filter: filtersToBeModified){
            restrictedOrbits.add(((NotInOrbit)filter).getOrbit());
        }

        for(int o: orbits){
            if(restrictedOrbits.contains(o)){
                continue;
            }
            NotInOrbitExceptOrbit notInOrbitExceptOrbit = new NotInOrbitExceptOrbit(params, super.selectedClass, o, super.selectedInstrument);
            baseFeaturesToTest.add(this.base.getFeatureFetcher().fetch(notInOrbitExceptOrbit));
        }

        // Add extra conditions to make smaller steps
        addedFeatures = localSearch.addExtraConditions(root, super.targetParentNode, null, baseFeaturesToTest, 1, FeatureMetric.DISTANCE2UP);
    }


    @Override
    public void apply(Connective root,
                      Connective parent,
                      AbstractFilter constraintSetter,
                      Set<AbstractFilter> matchingFilters,
                      Map<AbstractFilter, Literal> nodes,
                      List<String> description
    ){
        this.apply(root, parent, constraintSetter, matchingFilters, nodes);

        Params params = (Params) this.params;
        StringBuilder sb = new StringBuilder();
        sb.append("Generalize ");
        sb.append("\"Instrument " + params.getLeftSetEntityName(this.selectedInstrument));
        sb.append(" is not assigned to any of the orbits {");
        StringJoiner orbitNamesJoiner = new StringJoiner(", ");
        for(AbstractFilter filter: this.filtersToBeModified){
            int orbit = ((NotInOrbit) filter).getOrbit();
            orbitNamesJoiner.add(params.getRightSetEntityName(orbit));
        }
        sb.append(orbitNamesJoiner.toString() + "}\"");
        sb.append(" to ");
        if(addedFeatures.isEmpty()){
            sb.append("\"" + this.newFilter.getDescription() + "\"");
        }else{
            sb.append("\"" +
                    this.localSearch.getFilterFetcher().fetch(addedFeatures.get(0).getName()).getDescription() + "\"");
        }
        description.add(sb.toString());
    }
}
