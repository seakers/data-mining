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
import ifeed.problem.assigning.filters.NotInOrbit;
import ifeed.problem.assigning.filters.Separate;
import ifeed.problem.assigning.filters.Together;
import ifeed.problem.assigning.filtersWithException.AbsentWithException;
import ifeed.problem.assigning.filtersWithException.SeparateWithException;
import ifeed.problem.assigning.logicOperators.generalization.combined.SeparatesGeneralizer;

import java.util.*;

public class SeparatesGeneralizationWithException extends SeparatesGeneralizer{

    private AbstractLocalSearch localSearch;
    private List<Feature> addedFeatures;

    public SeparatesGeneralizationWithException(BaseParams params, AbstractMOEABase base, AbstractLocalSearch localSearch){
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
        if(super.selectedClass == -1){
            return;
        }

        // Remove Separate node
        parent.removeLiteral(super.newLiteral);

        Set<Integer> instrumentInstantiation = params.getLeftSetInstantiation(super.selectedClass);

        Set<Integer> restrictedInstruments = new HashSet<>();
        for(AbstractFilter filter: modifiedFilters){
            restrictedInstruments.addAll(((Separate)filter).getInstruments());
        }

        Set<Integer> separateInstrumentSet = new HashSet<>();
        separateInstrumentSet.add(super.selectedInstrument);
        separateInstrumentSet.add(super.selectedClass);

        List<Feature> baseFeaturesToTest = new ArrayList<>();
        for(int instr: instrumentInstantiation){
            if(restrictedInstruments.contains(instr)){
                continue;
            }

            HashSet<Integer> instrumentExceptions = new HashSet<>();
            instrumentExceptions.add(instr);

            SeparateWithException separateWithException = new SeparateWithException(params, separateInstrumentSet , new HashSet<>(), instrumentExceptions);
            GeneralizableFeature baseFeature = new GeneralizableFeature(this.base.getFeatureFetcher().fetch(separateWithException).copy());
            baseFeature.setNumGeneralizations(1);
            baseFeature.setNumExceptionVariables(1);
            baseFeaturesToTest.add(baseFeature);
        }
        GeneralizableFeature baseFeature = new GeneralizableFeature(super.newFeature);
        baseFeature.setNumGeneralizations(1);
        baseFeature.setNumExceptionVariables(0);
        baseFeaturesToTest.add(baseFeature);

        // Add extra conditions to make smaller steps
        addedFeatures = localSearch.addExtraConditions(root, super.targetParentNode, null, baseFeaturesToTest, 1, FeatureMetric.DISTANCE2UP, false);
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
        StringJoiner sj = new StringJoiner(" AND ");
        for(AbstractFilter filter: this.filtersToBeModified){
            Separate separate = (Separate) filter;
            sj.add(separate.getDescription());
        }
        sb.append("\""+ sj.toString() +"\"");
        sb.append(" to ");

        if(this.addedFeatures.isEmpty()){
            throw new IllegalStateException();
        }else{
            AbstractFilter filter = this.localSearch.getFilterFetcher().fetch(this.addedFeatures.get(0).getName());
            sb.append("\"" + filter.getDescription() + "\"");
        }
        description.add(sb.toString());
    }
}
