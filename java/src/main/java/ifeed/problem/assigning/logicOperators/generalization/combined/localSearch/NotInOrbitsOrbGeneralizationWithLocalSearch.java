package ifeed.problem.assigning.logicOperators.generalization.combined.localSearch;

import com.google.common.collect.Multiset;
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
import ifeed.problem.assigning.filters.InOrbit;
import ifeed.problem.assigning.filters.NotInOrbit;
import ifeed.problem.assigning.logicOperators.generalization.combined.NotInOrbitsOrbGeneralizer;

import java.util.*;

public class NotInOrbitsOrbGeneralizationWithLocalSearch extends NotInOrbitsOrbGeneralizer {

    private AbstractLocalSearch localSearch;
    private List<Feature> addedFeatures;

    public NotInOrbitsOrbGeneralizationWithLocalSearch(BaseParams params, AbstractMOEABase base, AbstractLocalSearch localSearch){
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

        List<Feature> baseFeaturesToTest = new ArrayList<>();
        Set<Integer> orbits = params.getRightSetInstantiation(super.selectedClass);

        Set<Integer> restrictedOrbits = new HashSet<>();
        for(AbstractFilter filter: filtersToBeModified){
            restrictedOrbits.add(((NotInOrbit)filter).getOrbit());
        }

        Multiset<Integer> instruments = ((NotInOrbit) constraintSetterAbstract).getInstruments();
        for(int o: orbits){
            if(restrictedOrbits.contains(o)){
                continue;
            }

            InOrbit inOrbit = new InOrbit(params, o, super.selectedInstrument);
            GeneralizableFeature baseFeature = new GeneralizableFeature(this.base.getFeatureFetcher().fetch(inOrbit));
            baseFeature.setNumGeneralizations(1);
            baseFeaturesToTest.add(baseFeature);

            inOrbit = new InOrbit(params, o, instruments);
            baseFeature = new GeneralizableFeature(this.base.getFeatureFetcher().fetch(inOrbit));
            baseFeature.setNumGeneralizations(1);
            baseFeaturesToTest.add(baseFeature);
        }

        // Add extra conditions to make smaller steps
        addedFeatures = localSearch.addExtraConditions(root, super.targetParentNode, super.newLiteral, baseFeaturesToTest, 1, FeatureMetric.RECALL);
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

        StringJoiner sj = new StringJoiner(" AND ");
        for(Feature feature: this.addedFeatures){
            AbstractFilter filter = this.localSearch.getFilterFetcher().fetch(feature.getName());
            sj.add(filter.getDescription());
        }
        StringBuilder sb = new StringBuilder();
        if(!this.addedFeatures.isEmpty()){
            sb.append("with an exception: ");
        }
        sb.append(sj.toString());
        description.add(sb.toString());
    }
}
