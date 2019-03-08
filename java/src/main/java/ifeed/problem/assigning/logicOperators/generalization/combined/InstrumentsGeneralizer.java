//package ifeed.problem.assigning.logicOperators.generalization.combined;
//
//import com.google.common.collect.Multiset;
//import com.sun.org.apache.xpath.internal.operations.Mult;
//import ifeed.Utils;
//import ifeed.feature.Feature;
//import ifeed.feature.logic.Connective;
//import ifeed.feature.logic.Literal;
//import ifeed.feature.logic.LogicalConnectiveType;
//import ifeed.filter.AbstractFilter;
//import ifeed.filter.AbstractFilterFinder;
//import ifeed.local.params.BaseParams;
//import ifeed.mining.moea.AbstractMOEABase;
//import ifeed.mining.moea.operators.AbstractLogicOperator;
//import ifeed.problem.assigning.Params;
//import ifeed.problem.assigning.filters.*;
//
//import java.util.*;
//
//public class InstrumentsGeneralizer extends AbstractLogicOperator {
//
//    protected int selectedOrbit;
//    protected int selectedInstrument;
//    protected int selectedClass;
//
//    protected Literal newLiteral;
//    protected List<Connective> targetParentNodes;
//
//    public InstrumentsGeneralizer(BaseParams params, AbstractMOEABase base) {
//        super(params, base);
//    }
//
//    public void apply(Connective root,
//                      Connective parent,
//                      AbstractFilter constraintSetterAbstract,
//                      Set<AbstractFilter> matchingFilters,
//                      Map<AbstractFilter, Literal> nodes
//    ){
//        Params params = (Params) super.params;
//
//        this.targetParentNodes = new ArrayList<>();
//        this.selectedOrbit = -1;
//
//        LogicalConnectiveType targetLogic;
//        Multiset<Integer> constraintSetterInstruments;
//        if(constraintSetterAbstract instanceof InOrbit){
//            this.selectedOrbit = ((InOrbit) constraintSetterAbstract).getOrbit();
//            constraintSetterInstruments = ((InOrbit) constraintSetterAbstract).getInstruments();
//            targetLogic = LogicalConnectiveType.OR;
//
//        }else if(constraintSetterAbstract instanceof Together){
//            constraintSetterInstruments = ((Together) constraintSetterAbstract).getInstruments();
//            targetLogic = LogicalConnectiveType.OR;
//
//        }else if(constraintSetterAbstract instanceof Separate){
//            constraintSetterInstruments = ((Separate) constraintSetterAbstract).getInstruments();
//            targetLogic = LogicalConnectiveType.AND;
//
//        }else{
//            throw new UnsupportedOperationException();
//        }
//
//        // If the logic does not match, pass
//        // TODO: Improve how this constraint is handled
//        if(targetLogic != parent.getLogic()){
//            return;
//        }
//
//        List<Integer> instrumentSuperclasses = new ArrayList<>();
//        for(int inst: constraintSetterInstruments){
//            instrumentSuperclasses.addAll(params.getLeftSetSuperclass("Instrument", inst));
//        }
//        Collections.shuffle(instrumentSuperclasses);
//        this.selectedClass = instrumentSuperclasses.get(0);
//
//        List<AbstractFilter> filtersToBeModified = new ArrayList<>();
//        filtersToBeModified.add(constraintSetterAbstract);
//
//        // Find all matching filters whose orbits are in the selected class
//        for(AbstractFilter filter: matchingFilters){
//            Multiset<Integer> instruments;
//            if(filter instanceof InOrbit){
//                instruments = ((InOrbit) filter).getInstruments();
//            }else if(filter instanceof Together){
//                instruments = ((Together) filter).getInstruments();
//            }else {
//                instruments = ((Separate) filter).getInstruments();
//            }
//
//            for(int inst: instruments){
//                Set<Integer> superclasses = params.getLeftSetSuperclass("Instrument", inst);
//                if(superclasses.contains(this.selectedClass)){
//                    filtersToBeModified.add(filter);
//                }
//            }
//        }
//
//        // Remove nodes that share the instrument
//        for(AbstractFilter filter: filtersToBeModified){
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
//        AbstractFilter newFilter;
//        if(constraintSetterAbstract instanceof InOrbit){
//            newFilter = new InOrbit(params, this.selectedOrbit, this.selectedClass);
//        }else if(constraintSetterAbstract instanceof Together){
//            newFilter = new NotInOrbit(params, this.selectedClass, this.selectedInstrument);
//        }else{
//            newFilter = new NotInOrbit(params, this.selectedClass, this.selectedInstrument);
//        }
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
//        for(int i = 0; i < filtersToBeModified.size(); i++){
//            AbstractFilter filter = filtersToBeModified.get(i);
//            int orbit;
//            Multiset<Integer> instruments;
//            if(filter instanceof InOrbit){
//                orbit = ((InOrbit) filter).getOrbit();
//                instruments = ((InOrbit) filter).getInstruments();
//            }else{
//                orbit = ((NotInOrbit) filter).getOrbit();
//                instruments = ((NotInOrbit) filter).getInstruments();
//            }
//
//            if(instruments.size() > 1){
//                ArrayList<Integer> instrumentList = new ArrayList<>(instruments);
//                int selectedArgumentIndex = instrumentList.indexOf(this.selectedInstrument);
//                instrumentList.remove(selectedArgumentIndex);
//
//                AbstractFilter modifiedFilter = new InOrbit(params, orbit, Utils.intCollection2Array(instrumentList));
//                Feature modifiedFeature = base.getFeatureFetcher().fetch(modifiedFilter);
//
//                if(!instruments.isEmpty()){
//                    if(sharedByAll){
//                        parent.addLiteral(modifiedFeature.getName(), modifiedFeature.getMatches());
//                    }else{
//                        this.targetParentNodes.get(i).addLiteral(modifiedFeature.getName(), modifiedFeature.getMatches());
//                    }
//                }
//            }
//        }
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
//        private String matchingClassName;
//        private int orbit;
//        private Multiset<Integer> instruments;
//
//        public FilterFinder(Params params){
//            super();
//            this.params = params;
//            this.clearConstraints();
//
//            Set<Class> allowedClasses = new HashSet<>();
//            allowedClasses.add(InOrbit.class);
//            allowedClasses.add(Together.class);
//            allowedClasses.add(Separate.class);
//            super.setConstraintSetterClasses(allowedClasses);
//            super.setMatchingClasses(allowedClasses);
//        }
//
//        @Override
//        public void setConstraints(AbstractFilter constraintSetter){
//            if(constraintSetter instanceof InOrbit){
//                this.matchingClassName = InOrbit.class.getSimpleName();
//                this.orbit = ((InOrbit)constraintSetter).getOrbit();
//                this.instruments = ((InOrbit)constraintSetter).getInstruments();
//
//            }else if(constraintSetter instanceof Together){
//                this.matchingClassName = Together.class.getSimpleName();
//                this.instruments = ((Together)constraintSetter).getInstruments();
//
//            }else if(constraintSetter instanceof Separate){
//                this.matchingClassName = Separate.class.getSimpleName();
//                this.instruments = ((Separate)constraintSetter).getInstruments();
//            }
//        }
//
//        @Override
//        public void clearConstraints(){
//            this.matchingClassName = null;
//            this.orbit = -1;
//            this.instruments = null;
//        }
//
//        @Override
//        public boolean check(AbstractFilter filterToTest){
//
//            if(!filterToTest.getClass().getSimpleName().equals(this.matchingClassName)){
//                return false;
//            }
//
//            int orb1 = this.orbit;
//            Multiset<Integer> inst1 = this.instruments;
//            int orb2 = -1;
//            Multiset<Integer> inst2;
//
//            if(this.matchingClassName.equalsIgnoreCase(InOrbit.class.getSimpleName())){
//                InOrbit filter = (InOrbit) filterToTest;
//                orb2 = filter.getOrbit();
//                inst2 = filter.getInstruments();
//
//            }else if(this.matchingClassName.equalsIgnoreCase(Together.class.getSimpleName())){
//                Together filter = (Together) filterToTest;
//                inst2 = filter.getInstruments();
//
//            }else if(this.matchingClassName.equalsIgnoreCase(Separate.class.getSimpleName())){
//                Separate filter = (Separate) filterToTest;
//                inst2 = filter.getInstruments();
//
//            }else{
//                throw new IllegalArgumentException();
//            }
//
//            // Check if the orbit is shared (in the case of InOrbit only)
//            if(orb1 != orb2 && orb2 != -1){
//                return false;
//            }
//
//            // Check if inst1 and inst2 have instruments that are in the same class
//            Set<Integer> orb1Classes = new HashSet<>();
//            for(int i: inst1){
//                orb1Classes.addAll(params.getRightSetSuperclass("Instrument", i));
//            }
//            Set<Integer> orb2Classes = new HashSet<>();
//            for(int i: inst2){
//                orb2Classes.addAll(params.getRightSetSuperclass("Instrument", i));
//            }
//            orb1Classes.retainAll(orb2Classes);
//            if(orb1Classes.isEmpty()){
//                return false;
//            }
//
//            return true;
//        }
//    }
//}
