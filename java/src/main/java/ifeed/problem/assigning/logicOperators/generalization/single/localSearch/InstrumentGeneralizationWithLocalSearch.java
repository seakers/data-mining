package ifeed.problem.assigning.logicOperators.generalization.single.localSearch;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InstrumentGeneralizationWithLocalSearch extends InstrumentGeneralizer{

    private AbstractLocalSearch localSearch;
    private List<Feature> addedFeatures;

    public InstrumentGeneralizationWithLocalSearch(BaseParams params, AbstractMOEABase base, AbstractLocalSearch localSearch){
        super(params, base);
        this.localSearch = localSearch;
    }

    @Override
    public boolean apply(Connective root,
                         Connective parent,
                         AbstractFilter constraintSetterAbstract,
                         Set<AbstractFilter> matchingFilters,
                         Map<AbstractFilter, Literal> nodes
    ){
        Params params = (Params) super.params;

        super.apply(root, parent, constraintSetterAbstract, matchingFilters, nodes);

        List<Feature> baseFeaturesToTest = new ArrayList<>();
        Set<Integer> instruments = params.getLeftSetInstantiation(super.selectedClass);

        LogicalConnectiveType logic;
        FeatureMetric metric;

        if(constraintSetterAbstract instanceof InOrbit){
            int orbit = ((InOrbit) constraintSetterAbstract).getOrbit();
            for(int instr: instruments){
                if(instr == super.selectedInstrument){
                    continue;
                }
                NotInOrbit notInOrbit = new NotInOrbit(params, orbit, instr);
                baseFeaturesToTest.add(this.base.getFeatureFetcher().fetch(notInOrbit));
            }
            logic = LogicalConnectiveType.AND;
            metric = FeatureMetric.PRECISION;

        }else if(constraintSetterAbstract instanceof NotInOrbit) {
            int orbit = ((NotInOrbit) constraintSetterAbstract).getOrbit();
            for(int instr: instruments){
                if(instr == super.selectedInstrument){
                    continue;
                }
                InOrbit inOrbit = new InOrbit(params, orbit, instr);
                baseFeaturesToTest.add(this.base.getFeatureFetcher().fetch(inOrbit));
            }
            logic = LogicalConnectiveType.OR;
            metric = FeatureMetric.RECALL;

        }else if(constraintSetterAbstract instanceof Together) {
            Together together = (Together) constraintSetterAbstract;
            for(int instr1: instruments){
                if(instr1 == super.selectedInstrument){
                    continue;
                }
                for(int instr2: together.getInstruments()){
                    if(instr2 == super.selectedInstrument || instr2 == instr1){
                        continue;
                    }
                    List<Integer> temp = new ArrayList<>();
                    temp.add(instr1);
                    temp.add(instr2);
                    Separate separate = new Separate(params, temp);
                    baseFeaturesToTest.add(this.base.getFeatureFetcher().fetch(separate));
                }
            }
            logic = LogicalConnectiveType.AND;
            metric = FeatureMetric.PRECISION;

        }else if(constraintSetterAbstract instanceof Separate) {
            Separate separate = (Separate) constraintSetterAbstract;
            for(int instr1: instruments){
                if(instr1 == super.selectedInstrument){
                    continue;
                }
                for(int instr2: separate.getInstruments()){
                    if(instr2 == super.selectedInstrument || instr2 == instr1){
                        continue;
                    }
                    List<Integer> temp = new ArrayList<>();
                    temp.add(instr1);
                    temp.add(instr2);
                    Together together = new Together(params, temp);
                    baseFeaturesToTest.add(this.base.getFeatureFetcher().fetch(together));
                }
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
