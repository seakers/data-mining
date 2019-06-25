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
import ifeed.problem.assigning.filters.InOrbit;
import ifeed.problem.assigning.filters.NotInOrbit;
import ifeed.problem.assigning.filters.Together;
import ifeed.problem.assigning.logicOperators.generalization.combined.NotInOrbits2Absent;
import ifeed.problem.assigning.logicOperators.generalization.single.NotInOrbit2Absent;

import java.util.*;

public class NotInOrbits2AbsentWithLocalSearch extends NotInOrbits2Absent{

    private AbstractLocalSearch localSearch;
    private List<Feature> addedFeatures;

    public NotInOrbits2AbsentWithLocalSearch(BaseParams params, AbstractMOEABase base, AbstractLocalSearch localSearch){
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

        Set<Integer> restrictedOrbits = new HashSet<>();
        for(AbstractFilter filter: filtersToBeModified){
            restrictedOrbits.add(((NotInOrbit)filter).getOrbit());
        }

        List<Feature> baseFeaturesToTest = new ArrayList<>();
        for(int o = 0; o < params.getRightSetCardinality() + params.getRightSetGeneralizedConcepts().size() - 1; o++){
            if(restrictedOrbits.contains(o)){
                continue;
            }
            if(params.getRightSetInstantiation(o).size() > 3){
                continue;
            }

            InOrbit inOrbit = new InOrbit(params, o, super.selectedInstrument);
            baseFeaturesToTest.add(this.base.getFeatureFetcher().fetch(inOrbit));
        }

        for(int i = 0; i < params.getLeftSetCardinality() + params.getLeftSetGeneralizedConcepts().size() - 1; i++){
            if(super.selectedInstrument == i){
                continue;
            }

            Set<Integer> instruments = new HashSet<>();
            instruments.add(super.selectedInstrument);
            instruments.add(i);
            Together together = new Together(params, instruments);
            baseFeaturesToTest.add(this.base.getFeatureFetcher().fetch(together));
        }

        // Add an exception to make smaller steps
        // The operation "notInOrbit -> absent" improves precision, so look for exception that improves recall
        addedFeatures = this.localSearch.addExtraConditions(root, super.targetParentNode, super.newLiteral, baseFeaturesToTest, 1, FeatureMetric.RECALL);
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
