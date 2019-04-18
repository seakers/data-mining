package ifeed.problem.assigning.logicOperators.generalization.combined.localSearch;

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
import ifeed.problem.assigning.logicOperators.generalization.combined.InOrbitsInstrGeneralizer;
import ifeed.problem.assigning.logicOperators.generalization.combined.InOrbitsOrbGeneralizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InOrbitsInstrGeneralizationWithLocalSearch extends InOrbitsInstrGeneralizer{

    private List<Feature> addedFeatures;
    private AbstractLocalSearch localSearch;

    public InOrbitsInstrGeneralizationWithLocalSearch(BaseParams params, AbstractMOEABase base, AbstractLocalSearch localSearch){
        super(params, base);
        this.localSearch = localSearch;
    }

    @Override
    public void apply(Connective root,
                      Connective parent,
                      AbstractFilter constraintSetterAbstract,
                      Set<AbstractFilter> matchingFilters,
                      Map<AbstractFilter, Literal> nodes){

        Params params = (Params) super.params;

        super.apply(root, parent, constraintSetterAbstract, matchingFilters, nodes);

        List<Feature> baseFeaturesToTest = new ArrayList<>();

        int orbit = ((InOrbit) constraintSetterAbstract).getOrbit();
        Set<Integer> classInstances = params.getLeftSetInstantiation(super.selectedClass);

        for(int instr: classInstances){

            if(super.selectedInstruments.contains(instr)){
                continue;
            }

            NotInOrbit notInOrbit = new NotInOrbit(params, orbit, instr);
            baseFeaturesToTest.add(this.base.getFeatureFetcher().fetch(notInOrbit));
        }

        System.out.println("root: " + root.getName());

        // Add extra conditions to make smaller steps
        addedFeatures = localSearch.addExtraConditions(root, super.targetParentNodes, null, baseFeaturesToTest, 2, FeatureMetric.PRECISION);
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

        for(Feature feature: this.addedFeatures){
            AbstractFilter filter = this.localSearch.getFilterFetcher().fetch(feature.getName());
            description.add(filter.getDescription());
        }
    }
}
