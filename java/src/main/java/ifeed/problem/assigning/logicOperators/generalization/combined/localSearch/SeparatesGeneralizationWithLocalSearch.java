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
import ifeed.problem.assigning.filters.Separate;
import ifeed.problem.assigning.filters.Together;
import ifeed.problem.assigning.logicOperators.generalization.combined.SeparatesGeneralizer;

import java.util.*;

public class SeparatesGeneralizationWithLocalSearch extends SeparatesGeneralizer{

    private AbstractLocalSearch localSearch;
    private List<Feature> addedFeatures;

    public SeparatesGeneralizationWithLocalSearch(BaseParams params, AbstractMOEABase base, AbstractLocalSearch localSearch){
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

        Multiset<Integer> instruments = ((Separate) constraintSetterAbstract).getInstruments();
        Set<Integer> instrumentInstantiation = params.getLeftSetInstantiation(super.selectedClass);

        for(int instr: instrumentInstantiation){
            if(instruments.contains(instr)){
                continue;
            }
            Set<Integer> newInstr = new HashSet<>();
            newInstr.add(super.selectedInstrument);
            newInstr.add(instr);
            Together together = new Together(params, newInstr);
            baseFeaturesToTest.add(this.base.getFeatureFetcher().fetch(together));
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

        for(Feature feature: this.addedFeatures){
            AbstractFilter filter = this.localSearch.getFilterFetcher().fetch(feature.getName());
            description.add(filter.getDescription());
        }
    }
}
