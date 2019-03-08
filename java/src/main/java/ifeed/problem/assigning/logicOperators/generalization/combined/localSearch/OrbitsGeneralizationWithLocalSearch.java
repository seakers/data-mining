package ifeed.problem.assigning.logicOperators.generalization.combined.localSearch;

import com.google.common.collect.Multiset;
import ifeed.feature.Feature;
import ifeed.feature.FeatureMetric;
import ifeed.feature.logic.Connective;
import ifeed.feature.logic.Literal;
import ifeed.feature.logic.LogicalConnectiveType;
import ifeed.filter.AbstractFilter;
import ifeed.local.params.BaseParams;
import ifeed.mining.AbstractLocalSearch;
import ifeed.mining.moea.AbstractMOEABase;
import ifeed.problem.assigning.Params;
import ifeed.problem.assigning.filters.InOrbit;
import ifeed.problem.assigning.filters.NotInOrbit;
import ifeed.problem.assigning.logicOperators.generalization.combined.InOrbits2Present;
import ifeed.problem.assigning.logicOperators.generalization.combined.OrbitsGeneralizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OrbitsGeneralizationWithLocalSearch extends OrbitsGeneralizer{

    private AbstractLocalSearch localSearch;

    public OrbitsGeneralizationWithLocalSearch(BaseParams params, AbstractMOEABase base, AbstractLocalSearch localSearch){
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

        LogicalConnectiveType logic;
        FeatureMetric metric;

        if(constraintSetterAbstract instanceof InOrbit){
            Multiset<Integer> instruments = ((InOrbit) constraintSetterAbstract).getInstruments();
            for(int o: orbits){
                if(o == super.selectedOrbit){
                    continue;
                }
                for(int i: instruments){
                    NotInOrbit notInOrbit = new NotInOrbit(params, o, i);
                    baseFeaturesToTest.add(this.base.getFeatureFetcher().fetch(notInOrbit));
                }
                NotInOrbit notInOrbit = new NotInOrbit(params, o, instruments);
                baseFeaturesToTest.add(this.base.getFeatureFetcher().fetch(notInOrbit));
            }
            logic = LogicalConnectiveType.AND;
            metric = FeatureMetric.PRECISION;

        }else if(constraintSetterAbstract instanceof NotInOrbit) {
            Multiset<Integer> instruments = ((NotInOrbit) constraintSetterAbstract).getInstruments();
            for(int o: orbits){
                if(o == super.selectedOrbit){
                    continue;
                }
                for(int i: instruments){
                    InOrbit notInOrbit = new InOrbit(params, o, i);
                    baseFeaturesToTest.add(this.base.getFeatureFetcher().fetch(notInOrbit));
                }
                InOrbit notInOrbit = new InOrbit(params, o, instruments);
                baseFeaturesToTest.add(this.base.getFeatureFetcher().fetch(notInOrbit));
            }
            logic = LogicalConnectiveType.OR;
            metric = FeatureMetric.RECALL;

        }else{
            throw new UnsupportedOperationException();
        }

        Literal literalToBeCombined;
        if(logic == parent.getLogic()){
            literalToBeCombined = null;
        }else{
            literalToBeCombined = super.newLiteral;
        }

        // Add extra conditions to make smaller steps
        localSearch.addExtraConditions(root, parent, literalToBeCombined, baseFeaturesToTest, 3, metric);
    }
}
