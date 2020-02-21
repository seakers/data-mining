package ifeed.problem.assigning.logicOperators.generalization.single.localSearch;

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
import ifeed.problem.assigning.filters.NotInOrbit;
import ifeed.problem.assigning.filtersWithException.NotInOrbitWithException;
import ifeed.problem.assigning.logicOperators.generalization.single.NotInOrbitInstrGeneralizer;

import java.util.*;

public class NotInOrbitInstrGeneralizationWithException extends NotInOrbitInstrGeneralizer{

    AbstractLocalSearch localSearch;
    private List<Feature> addedFeatures;

    public NotInOrbitInstrGeneralizationWithException(BaseParams params, AbstractMOEABase base, AbstractLocalSearch localSearch){
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

        Multiset<Integer> restrictedInstrumentSet = ((NotInOrbit)constraintSetterAbstract).getInstruments();

        if(!super.apply(root, parent, constraintSetterAbstract, matchingFilters, nodes)){
            return false;
        }

        // Remove NotInOrbit node
        parent.removeLiteral(super.newLiteral);

        List<Feature> baseFeaturesToTest = new ArrayList<>();
        int orbit = ((NotInOrbit) constraintSetterAbstract).getOrbit();
        Set<Integer> instrumentInstances = params.getLeftSetInstantiation(super.selectedClass);
        List<Integer> instrumentInstancesList = new ArrayList<>();
        instrumentInstancesList.addAll(instrumentInstances);

        for(int i = 0; i < instrumentInstancesList.size(); i++){
            int instr1 = instrumentInstancesList.get(i);
            if(restrictedInstrumentSet.contains(instr1)){
                continue;
            }
            Set<Integer> instrumentException = new HashSet<>();
            instrumentException.add(instr1);

            NotInOrbitWithException notInOrbitWithException = new NotInOrbitWithException(params, orbit, super.selectedClass, new HashSet<>(), instrumentException);
            GeneralizableFeature baseFeature = new GeneralizableFeature(this.base.getFeatureFetcher().fetch(notInOrbitWithException));
            baseFeature.setNumGeneralizations(1);
            baseFeature.setNumExceptionVariables(1);
            baseFeaturesToTest.add(baseFeature);

            for(int j = i + 1; j < instrumentInstancesList.size(); j++){
                int instr2 = instrumentInstancesList.get(j);
                if(restrictedInstrumentSet.contains(instr2)){
                    continue;
                }
                Set<Integer> instrumentException2 = new HashSet<>();
                instrumentException2.add(instr1);
                instrumentException2.add(instr2);
                notInOrbitWithException = new NotInOrbitWithException(params, orbit, super.selectedClass, new HashSet<>(), instrumentException2);
                GeneralizableFeature baseFeature2 = new GeneralizableFeature(this.base.getFeatureFetcher().fetch(notInOrbitWithException));
                baseFeature2.setNumGeneralizations(1);
                baseFeature2.setNumExceptionVariables(2);
                baseFeaturesToTest.add(baseFeature2);
            }
        }
        baseFeaturesToTest.add(super.newFeature);
        addedFeatures = this.localSearch.addExtraConditions(root, super.targetParentNode, null, baseFeaturesToTest, 1, FeatureMetric.DISTANCE2UP, false);

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

        StringBuilder sb = new StringBuilder();
        sb.append("Generalize ");
        sb.append("\"" + constraintSetter.getDescription() + "\"");
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
