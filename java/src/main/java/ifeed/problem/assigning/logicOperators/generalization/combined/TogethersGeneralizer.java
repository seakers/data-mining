//package ifeed.problem.assigning.logicOperators.generalization.combined;
//
//import com.google.common.collect.HashMultiset;
//import com.google.common.collect.Multiset;
//import ifeed.Utils;
//import ifeed.feature.Feature;
//import ifeed.feature.logic.Connective;
//import ifeed.feature.logic.Literal;
//import ifeed.feature.logic.LogicalConnectiveType;
//import ifeed.filter.AbstractFilter;
//import ifeed.filter.AbstractFilterFinder;
//import ifeed.local.params.BaseParams;
//import ifeed.mining.moea.AbstractMOEABase;
//import ifeed.mining.moea.operators.AbstractExhaustiveSearchOperator;
//import ifeed.problem.assigning.Params;
//import ifeed.problem.assigning.filters.Together;
//
//import java.util.*;
//
//public class TogethersGeneralizer extends AbstractExhaustiveSearchOperator {
//
//    protected int selectedClass;
//    protected int selectedInstrument;
//
//    protected List<Connective> targetParentNodes;
//    protected List<AbstractFilter> filtersToBeModified;
//
//    protected AbstractFilter newFilter;
//    protected List<AbstractFilter> modifiedFilters;
//
//
//    public TogethersGeneralizer(BaseParams params, AbstractMOEABase base) {
//        super(params, base, LogicalConnectiveType.OR);
//    }
//
//    @Override
//    public void initialize(){
//        this.selectedClass = -1;
//        this.selectedInstrument = -1;
//        this.targetParentNodes = new ArrayList<>();
//        this.filtersToBeModified = new ArrayList<>();
//        this.modifiedFilters = new ArrayList<>();
//        this.newFilter = null;
//    }
//
//    @Override
//    public boolean apply(Connective root,
//                      Connective parent,
//                      AbstractFilter constraintSetterAbstract,
//                      Set<AbstractFilter> matchingFilters,
//                      Map<AbstractFilter, Literal> nodes
//    ){
//        this.initialize();
//        Params params = (Params) super.params;
//
//        Set<AbstractFilter> allFilters = new HashSet<>();
//        allFilters.add(constraintSetterAbstract);
//        allFilters.addAll(matchingFilters);
//
//        // Count the number of appearances of each instrument
//        Map<Integer, Integer> instrumentCounter = new HashMap<>();
//        for(AbstractFilter filter: allFilters){
//            for(int inst: ((Together) filter).getInstruments()){
//                if(instrumentCounter.containsKey(inst)){
//                    instrumentCounter.put(inst, instrumentCounter.get(inst) + 1);
//                }else{
//                    instrumentCounter.put(inst, 1);
//                }
//            }
//        }
//
//        // Shuffle instrument orders
//        List<Integer> keySet = new ArrayList<>();
//        keySet.addAll(instrumentCounter.keySet());
//        Collections.shuffle(keySet);
//
//        // Find the most frequent instrument
//        int mostFrequentInstrument = -1;
//        int highestFrequency = 0;
//        for(int inst: keySet){
//            if(instrumentCounter.get(inst) > highestFrequency){
//                highestFrequency = instrumentCounter.get(inst);
//                mostFrequentInstrument = inst;
//            }
//        }
//
//        this.selectedInstrument = mostFrequentInstrument;
//
//        // Count the number of appearances of each class
//        Multiset<Integer> classMultiset = HashMultiset.create();
//        for(AbstractFilter filter: allFilters){
//            Set<Integer> withinClassSet = new HashSet<>();
//            for(int inst: ((Together) filter).getInstruments()){
//                if(inst == this.selectedInstrument){
//                    continue;
//
//                }else{
//                    Set<Integer> instrumentClasses = params.getLeftSetSuperclass(inst);
//                    for(int c: instrumentClasses){
//                        withinClassSet.add(c);
//                    }
//                }
//            }
//            for(int c: withinClassSet){
//                classMultiset.add(c);
//            }
//        }
//
//        // Shuffle instrument orders
//        keySet = new ArrayList<>();
//        keySet.addAll(classMultiset);
//        Collections.shuffle(keySet);
//
//        // Find the most frequent instrument
//        int mostFrequentClass = -1;
//        highestFrequency = 0;
//        for(int c: keySet){
//
//            // If the class is found only in one filter, then pass
//            if(classMultiset.count(c) == 1){
//                continue;
//            }
//
//            if(classMultiset.count(c) > highestFrequency){
//                highestFrequency = classMultiset.count(c);
//                mostFrequentClass = c;
//            }
//        }
//        this.selectedClass = mostFrequentClass;
//
//        // Remove nodes that share the selected instrument and the selected class
//        filtersToBeModified = new ArrayList<>();
//        for(AbstractFilter filter: allFilters){
//            Multiset<Integer> testInstr = ((Together) filter).getInstruments();
//
//            if(!testInstr.contains(this.selectedInstrument)){
//                continue;
//            }
//
//            boolean containsClass = false;
//            for(int instr: testInstr){
//                if(instr == this.selectedInstrument){
//                    continue;
//                }
//                Set<Integer> instrumentClasses = params.getLeftSetSuperclass(instr);
//                if(instrumentClasses.contains(this.selectedClass)){
//                    containsClass = true;
//                    break;
//                }
//            }
//            if(!containsClass){
//                continue;
//            }
//
//            // Remove matching literals
//            Literal literal = nodes.get(filter);
//            parent.removeNode(literal);
//            filtersToBeModified.add(filter);
//        }
//
//        boolean sharedByAll = false;
//        if(parent.getChildNodes().isEmpty()){
//            sharedByAll = true;
//        }
//
//        // Create new feature
//        Set<Integer> togetherInstruments = new HashSet<>();
//        togetherInstruments.add(this.selectedClass);
//        togetherInstruments.add(this.selectedInstrument);
//        this.newFilter = new Together(params, togetherInstruments);
//        Feature newFeature = this.base.getFeatureFetcher().fetch(newFilter);
//
//        if(sharedByAll){
//            Connective grandParent = (Connective) parent.getParent();
//
//            if(grandParent == null){ // Parent node is the root node since it doesn't have a parent node
//                super.base.getFeatureHandler().createNewRootNode(root);
//                grandParent = root;
//
//                // Store the newly generated node to parent
//                parent = grandParent.getConnectiveChildren().get(0);
//            }
//
//            grandParent.addLiteral(newFeature.getName(), newFeature.getMatches());
//            this.targetParentNodes.add(grandParent);
//
//        }else{
//            for(int i = 0; i < filtersToBeModified.size(); i++){
//                Connective newBranch = new Connective(LogicalConnectiveType.AND);
//                newBranch.addLiteral(newFeature.getName(), newFeature.getMatches());
//
//                parent.addBranch(newBranch);
//                this.targetParentNodes.add(newBranch);
//            }
//        }
//
//        modifiedFilters = new ArrayList<>();
//        for(int i = 0; i < filtersToBeModified.size(); i++){
//            AbstractFilter filter = filtersToBeModified.get(i);
//            Multiset<Integer> instruments  = ((Together) filter).getInstruments();
//
//            if(instruments.size() > 2){
//                int otherInstrument = -1;
//                for(int instr: instruments){
//                    Set<Integer> instrumentClasses = params.getLeftSetSuperclass(instr);
//                    if(instrumentClasses.contains(this.selectedClass)){
//                        continue;
//                    }else if(instr == this.selectedInstrument){
//                        continue;
//                    }else{
//                        otherInstrument = instr;
//                    }
//                }
//
//                if(otherInstrument != -1){
//                    for(int inst: instruments){
//                        if(inst == otherInstrument){
//                            continue;
//                        }else{
//                            ArrayList<Integer> instrumentList = new ArrayList<>();
//                            instrumentList.add(otherInstrument);
//                            instrumentList.add(inst);
//                            AbstractFilter modifiedFilter = new Together(params, Utils.intCollection2Array(instrumentList));
//                            modifiedFilters.add(modifiedFilter);
//                            Feature modifiedFeature = base.getFeatureFetcher().fetch(modifiedFilter);
//
//                            if(sharedByAll){
//                                parent.addLiteral(modifiedFeature.getName(), modifiedFeature.getMatches());
//                            }else{
//                                this.targetParentNodes.get(i).addLiteral(modifiedFeature.getName(), modifiedFeature.getMatches());
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        if(parent.getChildNodes().isEmpty()){
//            Connective grandParent = (Connective) parent.getParent();
//            grandParent.removeNode(parent);
//        }
//
//        return true;
//    }
//
//    @Override
//    public String getDescription(){
//
//        Params params = (Params) this.params;
//
//        StringBuilder sb = new StringBuilder();
//        sb.append("Generalize ");
//        sb.append("\"Instruments in at least one of the sets ");
//
//        StringJoiner instrumentSetJoiner = new StringJoiner(", ");
//        for(AbstractFilter filter: this.filtersToBeModified){
//            Multiset<Integer> instruments = ((Together) filter).getInstruments();
//            StringJoiner instrumentNamesJoiner = new StringJoiner(", ");
//            for(int instr: instruments){
//                instrumentNamesJoiner.add(params.getLeftSetEntityName(instr));
//            }
//            instrumentSetJoiner.add("{"+ instrumentNamesJoiner.toString() +"}");
//        }
//        sb.append(instrumentSetJoiner.toString());
//        sb.append(" are assigned to the same orbit\"");
//        sb.append(" to ");
//
//        sb.append("\"Instruments in at least one of the sets ");
//        List<AbstractFilter> tempFilterList = new ArrayList<>();
//        tempFilterList.add(this.newFilter);
//        tempFilterList.addAll(this.modifiedFilters);
//
//        instrumentSetJoiner = new StringJoiner(", ");
//        for(AbstractFilter filter: tempFilterList){
//            Multiset<Integer> instruments = ((Together) filter).getInstruments();
//            StringJoiner instrumentNamesJoiner = new StringJoiner(", ");
//            for(int instr: instruments){
//                instrumentNamesJoiner.add(params.getLeftSetEntityName(instr));
//            }
//            instrumentSetJoiner.add("{"+ instrumentNamesJoiner.toString() +"}");
//        }
//        sb.append(instrumentSetJoiner.toString());
//        sb.append(" are assigned together in the same orbit\"");
//        return sb.toString();
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
//            this.clearConstraints();
//            Set<Class> allowedClasses = new HashSet<>();
//            allowedClasses.add(Together.class);
//            super.setConstraintSetterClasses(allowedClasses);
//            super.setMatchingClasses(allowedClasses);
//        }
//
//        @Override
//        public void setConstraints(AbstractFilter constraintSetter){
//            this.instruments = ((Together)constraintSetter).getInstruments();
//        }
//
//        @Override
//        public void clearConstraints(){
//            this.instruments = null;
//        }
//
//        @Override
//        public boolean check(AbstractFilter filterToTest){
//
//            Multiset<Integer> inst1 = this.instruments;
//            Multiset<Integer> inst2 = ((Together) filterToTest).getInstruments();
//
//            // Check if two literals share at least one common instrument
//            Set<Integer> sharedInstruments = new HashSet<>();
//            for(int inst:inst2){
//                if(inst1.contains(inst)) {
//                    sharedInstruments.add(inst);
//                }
//            }
//            if(sharedInstruments.isEmpty()){
//                return false;
//            }
//
//            // Check if unshared instruments from both filters share a class
//            boolean foundSharedClass = false;
//            Set<Integer> savedClasses = new HashSet<>();
//            for(int i:inst1){
//                if(sharedInstruments.contains(i)){
//                    continue;
//                }else{
//                    Set<Integer> instrumentClasses = params.getLeftSetSuperclass(i);
//                    savedClasses.addAll(instrumentClasses);
//                }
//            }
//
//            for(int i:inst2){
//                if(sharedInstruments.contains(i)){
//                    continue;
//                }else{
//                    Set<Integer> instrumentClasses = params.getLeftSetSuperclass(i);
//                    for(int thisClass: instrumentClasses){
//                        if(savedClasses.contains(thisClass)){
//                            foundSharedClass = true;
//                            break;
//                        }
//                    }
//                }
//                if(foundSharedClass){
//                    break;
//                }
//            }
//
//            if(foundSharedClass){
//                return true;
//            }else{
//                return false;
//            }
//        }
//    }
//}
