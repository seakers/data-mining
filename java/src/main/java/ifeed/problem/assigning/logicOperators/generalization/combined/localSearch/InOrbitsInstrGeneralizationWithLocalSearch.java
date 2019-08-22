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

import java.util.*;

public class InOrbitsInstrGeneralizationWithLocalSearch extends InOrbitsInstrGeneralizer{

    private List<Feature> addedFeatures;
    private AbstractLocalSearch localSearch;

    public InOrbitsInstrGeneralizationWithLocalSearch(BaseParams params, AbstractMOEABase base, AbstractLocalSearch localSearch){
        super(params, base);
        this.localSearch = localSearch;
    }

    @Override
    public boolean apply(Connective root,
                      Connective parent,
                      AbstractFilter constraintSetterAbstract,
                      Set<AbstractFilter> matchingFilters,
                      Map<AbstractFilter, Literal> nodes){

        Params params = (Params) super.params;

        if(!super.apply(root, parent, constraintSetterAbstract, matchingFilters, nodes)){
            return false;
        }

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

        // Add extra conditions to make smaller steps
        addedFeatures = localSearch.addExtraConditions(root, super.targetParentNodes, null, baseFeaturesToTest, 1, FeatureMetric.PRECISION);
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
        description.add(this.getDescription());

        StringJoiner sj = new StringJoiner(" AND ");
        for(Feature feature: this.addedFeatures){
            AbstractFilter filter = this.localSearch.getFilterFetcher().fetch(feature.getName());
            sj.add(filter.getDescription());
        }
        StringBuilder sb = new StringBuilder();
        if(!this.addedFeatures.isEmpty()){
            sb.append("with an extra condition: ");
        }
        sb.append(sj.toString());
        description.add(sb.toString());

        return true;
    }
}
