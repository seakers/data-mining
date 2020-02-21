//package ifeed.problem.assigning.logicOperators.generalization.combined.localSearch;
//
//import com.google.common.collect.Multiset;
//import ifeed.feature.Feature;
//import ifeed.feature.FeatureMetric;
//import ifeed.feature.logic.Connective;
//import ifeed.feature.logic.Literal;
//import ifeed.filter.AbstractFilter;
//import ifeed.local.params.BaseParams;
//import ifeed.mining.AbstractLocalSearch;
//import ifeed.mining.moea.AbstractMOEABase;
//import ifeed.problem.assigning.Params;
//import ifeed.problem.assigning.filters.NumInstruments;
//import ifeed.problem.assigning.filters.Separate;
//import ifeed.problem.assigning.filters.Together;
//import ifeed.problem.assigning.logicOperators.generalization.combined.TogethersGeneralizer;
//
//import java.util.*;
//
//public class TogethersGeneralizationWithLocalSearch extends TogethersGeneralizer{
//
//    private AbstractLocalSearch localSearch;
//    private List<Feature> addedFeatures;
//
//    public TogethersGeneralizationWithLocalSearch(BaseParams params, AbstractMOEABase base, AbstractLocalSearch localSearch){
//        super(params, base);
//        this.localSearch = localSearch;
//    }
//
//    public void apply(Connective root,
//                         Connective parent,
//                         AbstractFilter constraintSetterAbstract,
//                         Set<AbstractFilter> matchingFilters,
//                         Map<AbstractFilter, Literal> nodes
//    ){
//        Params params = (Params) super.params;
//
//        super.apply(root, parent, constraintSetterAbstract, matchingFilters, nodes);
//
//        List<Feature> baseFeaturesToTest = new ArrayList<>();
//
//        Multiset<Integer> instruments = ((Together) constraintSetterAbstract).getInstruments();
//        Set<Integer> instrumentInstantiation = params.getLeftSetInstantiation(super.selectedClass);
//
//        for(int instr: instrumentInstantiation){
//            if(instruments.contains(instr)){
//                continue;
//            }
//            Set<Integer> newInstr = new HashSet<>();
//            newInstr.add(super.selectedInstrument);
//            newInstr.add(instr);
//            Separate separate = new Separate(params, newInstr);
//            baseFeaturesToTest.add(this.base.getFeatureFetcher().fetch(separate));
//        }
//
//        for(int i = 1; i < params.getRightSetCardinality(); i++){
//            int[] nBounds = new int[2];
//            nBounds[0] = 1;
//            nBounds[1] = i;
//            NumInstruments numInstruments = new NumInstruments(params, -1, super.selectedClass, nBounds);
//            baseFeaturesToTest.add(this.base.getFeatureFetcher().fetch(numInstruments));
//            numInstruments = new NumInstruments(params, -1, super.selectedInstrument, nBounds);
//            baseFeaturesToTest.add(this.base.getFeatureFetcher().fetch(numInstruments));
//            if(i > 1){
//                numInstruments = new NumInstruments(params, -1, super.selectedClass, i);
//                baseFeaturesToTest.add(this.base.getFeatureFetcher().fetch(numInstruments));
//                numInstruments = new NumInstruments(params, -1, super.selectedInstrument, i);
//                baseFeaturesToTest.add(this.base.getFeatureFetcher().fetch(numInstruments));
//            }
//        }
//
//        // Add extra conditions to make smaller steps
//        addedFeatures = localSearch.addExtraConditions(root, super.targetParentNodes, null, baseFeaturesToTest, 1, FeatureMetric.PRECISION);
//    }
//
//
//    @Override
//    public void apply(Connective root,
//                      Connective parent,
//                      AbstractFilter constraintSetterAbstract,
//                      Set<AbstractFilter> matchingFilters,
//                      Map<AbstractFilter, Literal> nodes,
//                      List<String> description
//    ){
//        this.apply(root, parent, constraintSetterAbstract, matchingFilters, nodes);
//        description.add(this.getDescription());
//
//        StringJoiner sj = new StringJoiner(" AND ");
//        for(Feature feature: this.addedFeatures){
//            AbstractFilter filter = this.localSearch.getFilterFetcher().fetch(feature.getName());
//            sj.add(filter.getDescription());
//        }
//        StringBuilder sb = new StringBuilder();
//        if(!this.addedFeatures.isEmpty()){
//            sb.append("with an extra condition: ");
//        }
//        sb.append(sj.toString());
//        description.add(sb.toString());
//    }
//}
