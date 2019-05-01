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
import ifeed.problem.assigning.filters.NotInOrbitExceptInstrument;
import ifeed.problem.assigning.logicOperators.generalization.single.NotInOrbitInstrGeneralizer;

import java.util.*;

public class NotInOrbitInstrGeneralizationWithException extends NotInOrbitInstrGeneralizer{

    AbstractLocalSearch localSearch;
    private List<Feature> addedFeatures;

    public NotInOrbitInstrGeneralizationWithException(BaseParams params, AbstractMOEABase base, AbstractLocalSearch localSearch){
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

        Multiset<Integer> originalInstrumentSet = ((NotInOrbit)constraintSetterAbstract).getInstruments();

        super.apply(root, parent, constraintSetterAbstract, matchingFilters, nodes);

        // Remove NotInOrbit node
        parent.removeLiteral(super.newLiteral);

        List<Feature> baseFeaturesToTest = new ArrayList<>();
        int orbit = ((NotInOrbit) constraintSetterAbstract).getOrbit();
        Set<Integer> instrumentInstances = params.getLeftSetInstantiation(super.selectedClass);
        List<Integer> instrumentInstancesList = new ArrayList<>();
        instrumentInstancesList.addAll(instrumentInstances);

        for(int i = 0; i < instrumentInstancesList.size(); i++){
            int instr = instrumentInstancesList.get(i);
            if(originalInstrumentSet.contains(instr)){
                continue;
            }
            NotInOrbitExceptInstrument notInOrbitExceptInstrument = new NotInOrbitExceptInstrument(params, orbit, super.selectedClass, instr);
            baseFeaturesToTest.add(this.base.getFeatureFetcher().fetch(notInOrbitExceptInstrument));

            for(int j = 0; j < instrumentInstancesList.size(); j++){
                int instr2 = instrumentInstancesList.get(j);
                if(originalInstrumentSet.contains(instr2)){
                    continue;
                }
                Set<Integer> instrumentExceptions = new HashSet<>();
                instrumentExceptions.add(instr);
                instrumentExceptions.add(instr2);
                NotInOrbitExceptInstrument notInOrbitExceptInstrument2 = new NotInOrbitExceptInstrument(params, orbit, super.selectedClass, instrumentExceptions);
                baseFeaturesToTest.add(this.base.getFeatureFetcher().fetch(notInOrbitExceptInstrument2));
            }
        }

        addedFeatures = this.localSearch.addExtraConditions(root, super.targetParentNode, null, baseFeaturesToTest, 1, FeatureMetric.DISTANCE2UP);
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

        StringBuilder sb = new StringBuilder();
        sb.append("Generalize ");
        sb.append("\"" + constraintSetter.getDescription() + "\"");
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
