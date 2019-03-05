//package ifeed.problem.assigning.logicOperators.generalization;
//
//import com.google.common.collect.HashMultiset;
//import com.google.common.collect.Multiset;
//import ifeed.feature.logic.Connective;
//import ifeed.feature.logic.Literal;
//import ifeed.feature.Feature;
//import ifeed.filter.AbstractFilter;
//import ifeed.filter.AbstractFilterFinder;
//import ifeed.local.params.BaseParams;
//import ifeed.mining.moea.operators.AbstractGeneralizationOperator;
//import ifeed.mining.moea.GPMOEABase;
//import ifeed.problem.assigning.Params;
//import ifeed.problem.assigning.filters.InOrbit;
//import ifeed.problem.assigning.filters.NotInOrbit;
//import ifeed.problem.assigning.filters.Together;
//import ifeed.problem.assigning.filters.Separate;
//import org.semanticweb.owlapi.model.OWLClass;
//import java.util.*;
//
//public class InstrumentSetGeneralizer extends AbstractGeneralizationOperator{
//
//    public InstrumentSetGeneralizer(BaseParams params, GPMOEABase base) {
//        super(params, base);
//    }
//
//    public void apply(Connective root,
//                         Connective parent,
//                         AbstractFilter constraintSetterAbstract,
//                         Set<AbstractFilter> matchingFilters,
//                         Map<AbstractFilter, Literal> nodes
//    ){
//
//        Params params = (Params) super.params;
//
//        Multiset<Integer> instruments;
//        switch (constraintSetterAbstract.getClass().getSimpleName()){
//            case "InOrbit":
//                instruments = ((InOrbit) constraintSetterAbstract).getInstruments();
//                break;
//            case "NotInOrbit":
//                instruments = ((NotInOrbit) constraintSetterAbstract).getInstruments();
//                break;
//            case "Together":
//                instruments = ((Together) constraintSetterAbstract).getInstruments();
//                break;
//            case "Separate":
//                instruments = ((Separate) constraintSetterAbstract).getInstruments();
//                break;
//            default:
//                throw new UnsupportedOperationException();
//        }
//
//        int maxCount = -1;
//        String maxCountClass = "";
//        Map<String, Set<Integer>> classInstances = new HashMap<>();
//        for(int instrument: instruments){
//
//            if(instrument >= params.getLeftSetCardinality()){
//                continue;
//            }
//
//            String instrumentName = params.getInstrumentIndex2Name().get(instrument);
//            List<String> superclasses = params.getOntologyManager().getSuperClasses("Instrument", instrumentName);
//
//            for(String className: superclasses){
//                if(classInstances.containsKey(className)){
//                    Set<Integer> instances = classInstances.get(className);
//                    instances.add(instrument);
//                    int count = instances.size();
//                    classInstances.put(className, instances);
//
//                    if(count > maxCount){
//                        maxCount = count;
//                        maxCountClass = className;
//                    }
//
//                }else{
//                    Set<Integer> temp = new HashSet<>();
//                    temp.add(instrument);
//                    classInstances.put(className, temp);
//                }
//            }
//        }
//
//        params.addLeftSetGeneralizedConcept(maxCountClass);
//        Set<Integer> instances = classInstances.get(maxCountClass);
//        Multiset<Integer> modifiedInstrumentSet = HashMultiset.create(instruments);
//
//        Set<Integer> toBeRemoved = new HashSet<>();
//        for(int instrument: modifiedInstrumentSet){
//            if(instances.contains(instrument)){
//                toBeRemoved.add(instrument);
//            }
//        }
//
//        modifiedInstrumentSet.removeAll(toBeRemoved);
//        int classIndex = params.getInstrumentName2Index().get(maxCountClass);
//        for(int i = 0; i < toBeRemoved.size(); i++){
//            modifiedInstrumentSet.add(classIndex);
//        }
//
//        AbstractFilter newFilter;
//        switch (constraintSetterAbstract.getClass().getSimpleName()){
//            case "InOrbit":
//                newFilter = new InOrbit(params, ((InOrbit)constraintSetterAbstract).getOrbit(), modifiedInstrumentSet);
//                break;
//            case "NotInOrbit":
//                newFilter = new NotInOrbit(params, ((NotInOrbit)constraintSetterAbstract).getOrbit(), modifiedInstrumentSet);
//                break;
//            case "Together":
//                newFilter = new Together(params, modifiedInstrumentSet);
//                break;
//            case "Separate":
//                newFilter = new Separate(params, modifiedInstrumentSet);
//                break;
//            default:
//                throw new UnsupportedOperationException();
//        }
//
//        // Remove nodes that share an instrument
//        Literal constraintSetterLiteral = nodes.get(constraintSetterAbstract);
//        parent.removeLiteral(constraintSetterLiteral);
//
//        // Add the new feature to the grandparent node
//        Feature presentFeature = base.getFeatureFetcher().fetch(newFilter);
//        parent.addLiteral(presentFeature.getName(), presentFeature.getMatches());
//    }
//
//    @Override
//    public void findApplicableNodesUnderGivenParentNode(Connective parent,
//                                                        Map<AbstractFilter, Set<AbstractFilter>> applicableFiltersMap,
//                                                        Map<AbstractFilter, Literal> applicableLiteralsMap
//    ){
//        Params params = (Params) super.params;
//
//        // Find all literals that contain sets of instruments as arguments
//        FilterFinder finder = new FilterFinder(params);
//        super.findApplicableNodesUnderGivenParentNode(parent, applicableFiltersMap, applicableLiteralsMap, finder);
//    }
//
//    public class FilterFinder extends AbstractFilterFinder {
//
//        private Params params;
//        private Multiset<Integer> instruments;
//
//        public FilterFinder(Params params){
//            super();
//            this.params = params;
//            Set<Class> constraintSetter = new HashSet<>();
//            constraintSetter.add(InOrbit.class);
//            constraintSetter.add(NotInOrbit.class);
//            constraintSetter.add(Together.class);
//            constraintSetter.add(Separate.class);
//            super.setConstraintSetterClasses(constraintSetter);
//        }
//
//        @Override
//        public void setConstraints(AbstractFilter constraintSetter){
//            switch (constraintSetter.getClass().getSimpleName()){
//                case "InOrbit":
//                    instruments = ((InOrbit) constraintSetter).getInstruments();
//                    break;
//                case "NotInOrbit":
//                    instruments = ((NotInOrbit) constraintSetter).getInstruments();
//                    break;
//                case "Together":
//                    instruments = ((Together) constraintSetter).getInstruments();
//                    break;
//                case "Separate":
//                    instruments = ((Separate) constraintSetter).getInstruments();
//                    break;
//                default:
//                    throw new UnsupportedOperationException();
//            }
//        }
//
//        @Override
//        public void clearConstraints(){
//            instruments = null;
//        }
//
//        @Override
//        public boolean check(){
//            Map<String, Integer> classCounter = new HashMap<>();
//            for(int instrument: instruments){
//
//                if(instrument >= params.getLeftSetCardinality()){
//                    continue;
//                }
//
//                String instrumentName = params.getInstrumentIndex2Name().get(instrument);
//                List<String> superclasses = params.getOntologyManager().getSuperClasses("Instrument", instrumentName);
//
//                for(String className: superclasses){
//                    if(classCounter.containsKey(className)){
//                        int count = classCounter.get(className) + 1;
//                        classCounter.put(className, count);
//
//                        if(count >= 2){
//                            return true;
//                        }
//
//                    }else{
//                        classCounter.put(className, 1);
//                    }
//                }
//            }
//
//            return false;
//        }
//    }
//}
