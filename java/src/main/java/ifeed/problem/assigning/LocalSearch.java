package ifeed.problem.assigning;

import ifeed.architecture.AbstractArchitecture;
import ifeed.feature.Feature;
import ifeed.feature.logic.*;
import ifeed.filter.AbstractFilter;
import ifeed.local.params.BaseParams;
import ifeed.mining.AbstractLocalSearch;
import ifeed.problem.assigning.filters.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LocalSearch extends AbstractLocalSearch {

    public LocalSearch(BaseParams params, List<AbstractArchitecture> architectures, List<Integer> behavioral, List<Integer> non_behavioral){
        super(params, architectures, behavioral, non_behavioral, new FeatureFetcher(params, architectures));
    }

    public LocalSearch(BaseParams params, String root, LogicalConnectiveType logic, List<AbstractArchitecture> architectures, List<Integer> behavioral, List<Integer> non_behavioral){
        super(params, root, logic, architectures, behavioral, non_behavioral, new FeatureFetcher(params, architectures));
    }

    @Override
    public List<AbstractFilter> generateCandidates(){
        return new FeatureGenerator(params).generateCandidates();
    }

    /**
     * Method used to put constraints on base features for a given testNode
     * @param testNode
     * @param baseFeatures
     * @return
     */
    @Override
    public List<Feature> filterBaseFeatures(LocalSearchTester testNode, List<Feature> baseFeatures){

        if(!testNode.getAddNewNode()){
            throw new IllegalStateException("The selected test node should be set to add new nodes");
        }

        List<Feature> out;
        if(testNode instanceof ConnectiveTester){

            ConnectiveTester testNodeConnective = (ConnectiveTester) testNode;

            boolean combineLiteral = false;
            if(testNodeConnective.getLiteralToBeCombined() != null){
                combineLiteral = true;
            }

            if(testNodeConnective.getParent() == null && !combineLiteral){
                // If the parent node is the root, then pass all base features
                out = baseFeatures;

            }else{
                // Determine the logical connective
                LogicalConnectiveType logic;
                if(testNodeConnective.getLogic() == LogicalConnectiveType.AND){
                    if(combineLiteral){
                        logic = LogicalConnectiveType.OR;
                    }else{
                        logic = LogicalConnectiveType.AND;
                    }
                }else{
                    if(combineLiteral){
                        logic = LogicalConnectiveType.AND;
                    }else{
                        logic = LogicalConnectiveType.OR;
                    }
                }

                List<Literal> literals;
                if(logic == LogicalConnectiveType.OR){
                    // If the logical connective is OR, only allow adding new literals that have some
                    // commonality with the sibling nodes
                    if(combineLiteral){
                        literals = new ArrayList<>();
                        literals.add(testNodeConnective.getLiteralToBeCombined());
                    }else{
                        literals = testNodeConnective.getLiteralChildren();
                    }

                }else{
                    if(combineLiteral){
                        literals = new ArrayList<>();
                        literals.add(testNodeConnective.getLiteralToBeCombined());
                    }else{
                        literals = testNodeConnective.getLiteralChildren();
                    }
                }
                out = this.imposeFilterConstraint(logic, literals, baseFeatures);
            }
        }else if(testNode instanceof IfThenStatementTester){
            out = baseFeatures;

        }else{
            out = baseFeatures;
        }

        return out;
    }

    public Set<Integer> extractOrbits(AbstractFilter filter){

        Set<Integer> orbits = new HashSet<>();
        if(filter instanceof InOrbit){
            orbits.add(((InOrbit) filter).getOrbit());

        }else if(filter instanceof NotInOrbit){
            orbits.add(((NotInOrbit) filter).getOrbit());

        }else if(filter instanceof EmptyOrbit){
            orbits.add(((EmptyOrbit) filter).getOrbit());
        }
        return orbits;
    }

    public Set<Integer> extractInstruments(AbstractFilter filter){

        Set<Integer> instruments = new HashSet<>();
        if(filter instanceof InOrbit){
            instruments.addAll(((InOrbit) filter).getInstruments());

        }else if(filter instanceof NotInOrbit){
            instruments.addAll(((NotInOrbit) filter).getInstruments());

        }else if(filter instanceof Present){
            instruments.add(((Present) filter).getInstrument());

        }else if(filter instanceof Absent){
            instruments.add(((Absent) filter).getInstrument());

        }else if(filter instanceof Separate){
            instruments.addAll(((Separate) filter).getInstruments());

        }else if(filter instanceof Together){
            instruments.addAll(((Together) filter).getInstruments());
        }
        return instruments;
    }

    public Set<Class> allowedClassesUnderANDNode(AbstractFilter filter){

        Set<Class> allowedSetOfClasses = new HashSet<>();
        if(filter instanceof InOrbit){
            allowedSetOfClasses.add(InOrbit.class);
            allowedSetOfClasses.add(NotInOrbit.class);

        }else if(filter instanceof NotInOrbit){
            allowedSetOfClasses.add(InOrbit.class);
            allowedSetOfClasses.add(NotInOrbit.class);

        }else if(filter instanceof Present){
            allowedSetOfClasses.add(NotInOrbit.class);
            allowedSetOfClasses.add(Together.class);
            allowedSetOfClasses.add(Separate.class);

        }else if(filter instanceof Absent){
            // pass

        }else if(filter instanceof Separate){
            allowedSetOfClasses.add(Present.class);
            allowedSetOfClasses.add(Together.class);
            allowedSetOfClasses.add(Separate.class);

        }else if(filter instanceof Together) {
            allowedSetOfClasses.add(NotInOrbit.class);
            allowedSetOfClasses.add(Separate.class);

        }else if(filter instanceof EmptyOrbit) {
            // pass

        }else if(filter instanceof NumInstruments){
            // pass

        }else{
            allowedSetOfClasses.add(InOrbit.class);
            allowedSetOfClasses.add(NotInOrbit.class);
            allowedSetOfClasses.add(Present.class);
            allowedSetOfClasses.add(Absent.class);
            allowedSetOfClasses.add(Together.class);
            allowedSetOfClasses.add(Separate.class);
            allowedSetOfClasses.add(EmptyOrbit.class);
            allowedSetOfClasses.add(NumInstruments.class);
        }
        return allowedSetOfClasses;
    }

    public Set<Class> allowedClassesUnderORNode(AbstractFilter filter){

        Set<Class> allowedSetOfClasses = new HashSet<>();
        if(filter instanceof InOrbit){
            allowedSetOfClasses.add(InOrbit.class);

        }else if(filter instanceof NotInOrbit){
            allowedSetOfClasses.add(NotInOrbit.class);

        }else if(filter instanceof Present){
            // pass

        }else if(filter instanceof Absent){
            allowedSetOfClasses.add(InOrbit.class);
            allowedSetOfClasses.add(Together.class);

        }else if(filter instanceof Separate){
            allowedSetOfClasses.add(Separate.class);

        }else if(filter instanceof Together) {
            allowedSetOfClasses.add(Together.class);

        }else if(filter instanceof EmptyOrbit) {
            allowedSetOfClasses.add(InOrbit.class);

        }else if(filter instanceof NumInstruments){
            // pass

        }else{
            allowedSetOfClasses.add(InOrbit.class);
            allowedSetOfClasses.add(NotInOrbit.class);
            allowedSetOfClasses.add(Present.class);
            allowedSetOfClasses.add(Absent.class);
            allowedSetOfClasses.add(Together.class);
            allowedSetOfClasses.add(Separate.class);
            allowedSetOfClasses.add(EmptyOrbit.class);
            allowedSetOfClasses.add(NumInstruments.class);
        }
        return allowedSetOfClasses;
    }

    /**
     * Only pass filter that share all of the orbit arguments or all of the instrument arguments
     * @param filter
     * @param sharedOrbits
     * @param sharedInstruments
     * @return
     */
    public boolean satisfiesANDArgumentConstraint(AbstractFilter filter, Set<Integer> sharedOrbits, Set<Integer> sharedInstruments) {

        boolean satisfied = true;
        if(!sharedOrbits.isEmpty()){
            Set<Integer> orbits = this.extractOrbits(filter);
            for(int o: orbits){
                if(!sharedOrbits.contains(o)){
                    satisfied = false;
                    break;
                }
            }
        }else {
            Set<Integer> instruments = this.extractInstruments(filter);
            for(int i: instruments){
                if(!sharedInstruments.contains(i)){
                    satisfied = false;
                    break;
                }
            }
        }
        return satisfied;
    }

    /**
     * Only pass filter that share all of the orbit arguments or all of the instrument arguments
     * @param filter
     * @param sharedOrbits
     * @param sharedInstruments
     * @return
     */
    public boolean satisfiesORArgumentConstraint(AbstractFilter filter, Set<Integer> sharedOrbits, Set<Integer> sharedInstruments) {

        boolean allOrbitsShared = true;
        Set<Integer> orbits = this.extractOrbits(filter);
        for(int o: orbits){
            if(!sharedOrbits.contains(o)){
                allOrbitsShared = false;
                break;
            }
        }
        boolean allInstrumentsShared = true;
        Set<Integer> instruments = this.extractInstruments(filter);
        for(int i: instruments){
            if(!sharedInstruments.contains(i)){
                allInstrumentsShared = false;
                break;
            }
        }

        if(allOrbitsShared || allInstrumentsShared){
            return true;
        }else{
            return false;
        }
    }

        /**
         *
         * @param nodes
         * @param baseFeatures
         * @return
         */
    public List<Feature> imposeFilterConstraint(LogicalConnectiveType logic, List<Literal> nodes, List<Feature> baseFeatures){

        List<Feature> out = new ArrayList<>();
        Set<Class> allowedClasses = new HashSet<>();
        Set<Integer> sharedOrbits = new HashSet<>();
        Set<Integer> sharedInstruments = new HashSet<>();

        // Get all variables used
        for(Literal node: nodes) {
            AbstractFilter filter = super.getFilterFetcher().fetch(node.getName());

            // Add orbit
            sharedOrbits.addAll(this.extractOrbits(filter));

            // Add instruments
            sharedInstruments.addAll(this.extractInstruments(filter));

            // Add allowed classes
            if(logic == LogicalConnectiveType.AND){
                allowedClasses.addAll(this.allowedClassesUnderANDNode(filter));
            }else{
                allowedClasses.addAll(this.allowedClassesUnderORNode(filter));
            }
        }

        // Check all base features
        for(Feature feature: baseFeatures){

            AbstractFilter filter = super.getFilterFetcher().fetch(feature.getName());

            // Check if the given filter class is allowed
            if(!allowedClasses.contains(filter.getClass())){
                continue;
            }

            if(logic == LogicalConnectiveType.OR){
                if(!satisfiesORArgumentConstraint(filter, sharedOrbits, sharedInstruments)){
                    continue;
                }
            }else{
                if(!satisfiesANDArgumentConstraint(filter, sharedOrbits, sharedInstruments)){
                    continue;
                }
            }

            out.add(feature);
        }
        return out;
    }
}

