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
import ifeed.problem.assigning.filtersWithException.NotInOrbitWithException;
import ifeed.problem.assigning.logicOperators.generalization.combined.NotInOrbitsOrbGeneralizer;

import java.util.*;

public class NotInOrbitsOrbGeneralizationWithException extends NotInOrbitsOrbGeneralizer {

    private AbstractLocalSearch localSearch;
    private List<Feature> addedFeatures;

    public NotInOrbitsOrbGeneralizationWithException(BaseParams params, AbstractMOEABase base, AbstractLocalSearch localSearch){
        super(params, base);
        this.localSearch = localSearch;
    }

    public boolean apply(Connective root,
                         Connective parent,
                         AbstractFilter constraintSetter,
                         Set<AbstractFilter> matchingFilters,
                         Map<AbstractFilter, Literal> nodes
    ){
        if(!super.apply(root, parent, constraintSetter, matchingFilters, nodes)){
            return false;
        }

        Params params = (Params) super.params;

        // Remove NotInOrbit node
        parent.removeLiteral(super.newLiteral);

        List<Feature> baseFeaturesToTest = new ArrayList<>();
        Set<Integer> possibleOrbitExceptions = params.getRightSetInstantiation(super.selectedClass);

        for(AbstractFilter filter: filtersToBeModified){
            int orb = ((NotInOrbit)filter).getOrbit();
            possibleOrbitExceptions.remove(Integer.valueOf(orb));
        }

        for(int o: possibleOrbitExceptions){
            Set<Integer> orbitException = new HashSet<>();
            orbitException.add(o);
            NotInOrbitWithException notInOrbitWithException = new NotInOrbitWithException(params, super.selectedClass, super.selectedInstrument, orbitException, new HashSet<>());
            GeneralizableFeature baseFeature = new GeneralizableFeature(this.base.getFeatureFetcher().fetch(notInOrbitWithException));
            baseFeature.setNumGeneralizations(1);
            baseFeature.setNumExceptionVariables(1);
            baseFeaturesToTest.add(baseFeature);
        }
        // Add the feature without any exception
        baseFeaturesToTest.add(super.newFeature);

        // Add exceptions
        addedFeatures = localSearch.addExtraConditions(root, super.targetParentNode, null, baseFeaturesToTest, 1, FeatureMetric.DISTANCE2UP, false);

        return true;
    }


    @Override
    public boolean apply(Connective root,
                      Connective parent,
                      AbstractFilter constraintSetter,
                      Set<AbstractFilter> matchingFilters,
                      Map<AbstractFilter, Literal> nodes,
                      List<String> description
    ){
        if(!this.apply(root, parent, constraintSetter, matchingFilters, nodes)){
            return false;
        }

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
            throw new IllegalStateException();
        }else{
            sb.append("\"" +
                    this.localSearch.getFilterFetcher().fetch(addedFeatures.get(0).getName()).getDescription() + "\"");
        }
        description.add(sb.toString());

        return true;
    }
}
