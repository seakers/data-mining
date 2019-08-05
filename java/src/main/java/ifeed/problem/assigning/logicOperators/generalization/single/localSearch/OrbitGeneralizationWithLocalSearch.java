package ifeed.problem.assigning.logicOperators.generalization.single.localSearch;

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
import ifeed.problem.assigning.filters.Separate;
import ifeed.problem.assigning.filters.Together;
import ifeed.problem.assigning.logicOperators.generalization.single.InstrumentGeneralizer;
import ifeed.problem.assigning.logicOperators.generalization.single.OrbitGeneralizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OrbitGeneralizationWithLocalSearch extends OrbitGeneralizer{

    private AbstractLocalSearch localSearch;
    private List<Feature> addedFeatures;


    public OrbitGeneralizationWithLocalSearch(BaseParams params, AbstractMOEABase base, AbstractLocalSearch localSearch){
        super(params, base);
        this.localSearch = localSearch;
    }

    public boolean apply(Connective root,
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
        addedFeatures = localSearch.addExtraConditions(root, parent, literalToBeCombined, baseFeaturesToTest, 3, metric);

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
        this.apply(root, parent, constraintSetterAbstract, matchingFilters, nodes);
        description.add(this.getDescription());

        for(Feature feature: this.addedFeatures){
            AbstractFilter filter = this.localSearch.getFilterFetcher().fetch(feature.getName());
            description.add(filter.getDescription());
        }

        return true;
    }
}
