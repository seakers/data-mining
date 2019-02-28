package ifeed.problem.assigning;

import ifeed.architecture.AbstractArchitecture;
import ifeed.feature.Feature;
import ifeed.feature.logic.ConnectiveTester;
import ifeed.feature.logic.Literal;
import ifeed.feature.logic.LogicalConnectiveType;
import ifeed.filter.AbstractFilter;
import ifeed.local.params.BaseParams;
import ifeed.mining.AbstractLocalSearch;
import ifeed.problem.assigning.filters.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LocalSearch extends AbstractLocalSearch {

    private Params assigningParams;

    public LocalSearch(BaseParams params, String root, LogicalConnectiveType logic, List<AbstractArchitecture> architectures, List<Integer> behavioral, List<Integer> non_behavioral){
        super(params, root, logic, architectures, behavioral, non_behavioral, new FeatureFetcher(params, architectures));
        assigningParams = (Params) params;
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
    public List<Feature> filterBaseFeatures(ConnectiveTester testNode, List<Feature> baseFeatures){

        if(!testNode.getAddNewNode()){
            throw new IllegalStateException("The selected test node should be set to add new nodes");
        }

        List<Feature> out;

        boolean combineLiteral = false;
        if(testNode.getLiteralToBeCombined() != null){
            combineLiteral = true;
        }

        // Determine the logical connective
        LogicalConnectiveType logic;
        if(testNode.getLogic() == LogicalConnectiveType.AND){
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

        if(logic == LogicalConnectiveType.OR){
            // If the logical connective is OR, only allow adding new literals that have some
            // commonality with the sibling nodes

            List<Literal> literals;
            if(combineLiteral){
                literals = new ArrayList<>();
                literals.add(testNode.getLiteralToBeCombined());
            }else{
                literals = testNode.getLiteralChildren();
            }
            out = this.imposeFilterOR(literals, baseFeatures);

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

        if(assigningParams.getOntologyManager() != null){
            Set<Integer> generalizedOrbits = new HashSet<>();
            for(int o: orbits){
                generalizedOrbits.addAll(assigningParams.getRightSetSuperclass("Orbit", o));
            }

            orbits.addAll(generalizedOrbits);
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

        if(assigningParams.getOntologyManager() != null){
            Set<Integer> generalizedInstruments = new HashSet<>();
            for(int i: instruments){
                generalizedInstruments.addAll(assigningParams.getLeftSetSuperclass("Instrument", i));
            }

            instruments.addAll(generalizedInstruments);
        }
        return instruments;
    }

    public Set<Class> allowedClassesUnderORNode(AbstractFilter filter){

        Set<Class> allowedSetOfClasses = new HashSet<>();
        if(filter instanceof InOrbit){
            allowedSetOfClasses.add(InOrbit.class);

        }else if(filter instanceof NotInOrbit){
            allowedSetOfClasses.add(NotInOrbit.class);

        }else if(filter instanceof Present){
            allowedSetOfClasses.add(Present.class);

        }else if(filter instanceof Absent){
            allowedSetOfClasses.add(Absent.class);
            allowedSetOfClasses.add(InOrbit.class);
            allowedSetOfClasses.add(Together.class);

        }else if(filter instanceof Separate){
            allowedSetOfClasses.add(Separate.class);

        }else if(filter instanceof Together) {
            allowedSetOfClasses.add(Together.class);

        }else if(filter instanceof EmptyOrbit){
            allowedSetOfClasses.add(EmptyOrbit.class);
            allowedSetOfClasses.add(InOrbit.class);

        }else{
            allowedSetOfClasses.add(InOrbit.class);
            allowedSetOfClasses.add(NotInOrbit.class);
            allowedSetOfClasses.add(Present.class);
            allowedSetOfClasses.add(Absent.class);
            allowedSetOfClasses.add(Together.class);
            allowedSetOfClasses.add(Separate.class);
            allowedSetOfClasses.add(EmptyOrbit.class);
        }
        return allowedSetOfClasses;
    }

    /**
     *
     * @param nodes
     * @param baseFeatures
     * @return
     */
    public List<Feature> imposeFilterOR(List<Literal> nodes, List<Feature> baseFeatures){

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
            allowedClasses.addAll(this.allowedClassesUnderORNode(filter));
        }

        // Check all base features
        for(Feature feature: baseFeatures){

            boolean sharesArgument = false;
            boolean isAllowedClass = false;

            AbstractFilter filter = super.getFilterFetcher().fetch(feature.getName());

            // Check if the given filter class is allowed
            for(Class filterClass: allowedClasses){
                if(filterClass.isInstance(filter)){
                    isAllowedClass = true;
                    break;
                }
            }

            // Check if any variable is shared
            Set<Integer> orbits = this.extractOrbits(filter);
            orbits.retainAll(sharedOrbits);
            if(!orbits.isEmpty()){
                sharesArgument = true;
            }

            Set<Integer> instruments = this.extractInstruments(filter);
            instruments.retainAll(sharedInstruments);
            if(!instruments.isEmpty()){
                sharesArgument = true;
            }

            if(isAllowedClass){
                if(filter instanceof Present || filter instanceof Absent || filter instanceof EmptyOrbit){
                    out.add(feature);
                } else if(sharesArgument){
                    out.add(feature);
                }
            }
        }

        return out;
    }
}

