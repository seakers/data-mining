package ifeed.problem.assigning;

import ifeed.Utils;
import ifeed.feature.*;
import ifeed.feature.logic.Connective;
import ifeed.feature.logic.Formula;
import ifeed.feature.logic.Literal;
import ifeed.feature.logic.LogicalConnectiveType;
import ifeed.filter.AbstractFilter;
import ifeed.local.params.BaseParams;
import ifeed.problem.assigning.filters.Absent;
import ifeed.problem.assigning.filters.InOrbit;
import ifeed.problem.assigning.filters.NotInOrbit;
import ifeed.problem.assigning.filters.Separate;

import java.util.*;

public class FeatureSimplifier extends AbstractFeatureSimplifier{

    public FeatureSimplifier(BaseParams params,
                             FeatureFetcher featureFetcher){

        super(params, featureFetcher, featureFetcher.getFilterFetcher());
    }

    public FeatureSimplifier(BaseParams params,
                             FeatureFetcher featureFetcher,
                             FilterFetcher filterFetcher){

        super(params, featureFetcher, filterFetcher);
    }

    public boolean simplify(Connective root){

        boolean modified = false;

        // Combine inOrbits and notInOrbits
        if(root.getLogic()== LogicalConnectiveType.AND){
            if(combineInOrbits(root, root.getLiteralChildren())){
                modified = true;
            }

            if(combineNotInOrbits(root, root.getLiteralChildren())){
                modified = true;
            }

            // Remove absent instruments from notInOrbits
            if(removeAbsentInstruments(root, root.getLiteralChildren())){
                modified = true;
            }
        }

        // Recursively simplify subtrees
        for(Connective branch: root.getConnectiveChildren()){
            if(simplify(branch)){
                modified = true;
            }
        }

        return modified;
    }

    /**
     * Combines inOrbits sharing the same orbit
     * @param parent
     * @param literals
     * @return
     */
    public boolean combineInOrbits(Connective parent, List<Literal> literals){

        List<Integer> orbits = new ArrayList<>();
        List<HashSet<Integer>> sharedInstruments = new ArrayList<>();
        Set<Formula> nodesToRemove = new HashSet<>();
        boolean modify = false;

        for(Literal node:literals){

            AbstractFilter thisFilter = this.filterFetcher.fetch(node.getName());

            if(thisFilter instanceof InOrbit) {
                InOrbit inOrbit = (InOrbit) thisFilter;

                int orbit = inOrbit.getOrbit();
                Set<Integer> givenInstrumentSetTemp = inOrbit.getInstruments().elementSet();
                HashSet<Integer> givenInstrumentSet = new HashSet<>();
                for(int i: givenInstrumentSetTemp){
                    givenInstrumentSet.add(i);
                }

                // Save all orbits
                if (orbits.contains(orbit)) {
                    modify = true;
                    int index = orbits.indexOf(orbit);
                    sharedInstruments.get(index).addAll(givenInstrumentSet);

                } else {
                    orbits.add(orbit);
                    sharedInstruments.add(givenInstrumentSet);
                }

                nodesToRemove.add(node);
            }
        }

        if(modify){
            parent.removeNodes(nodesToRemove);

            for(int i = 0; i < orbits.size(); i++){
                Feature newFeature = featureFetcher.fetch(new InOrbit(params, orbits.get(i), sharedInstruments.get(i)));
                parent.addLiteral(newFeature.getName(), newFeature.getMatches());
            }
        }

        if(modify){
            return true;
        }else{
            return false;
        }
    }

    public boolean combineNotInOrbits(Connective parent, List<Literal> literals){

        List<Integer> orbits = new ArrayList<>();
        List<HashSet<Integer>> sharedInstruments = new ArrayList<>();
        Set<Formula> nodesToRemove = new HashSet<>();
        boolean modify = false;

        for(Literal node:literals){

            AbstractFilter thisFilter = this.filterFetcher.fetch(node.getName());

            if(thisFilter instanceof NotInOrbit) {
                NotInOrbit notInOrbit = (NotInOrbit) thisFilter;

                int orbit = notInOrbit.getOrbit();
                Set<Integer> givenInstrumentSetTemp = notInOrbit.getInstruments().elementSet();
                HashSet<Integer> givenInstrumentSet = new HashSet<>();
                for(int i: givenInstrumentSetTemp){
                    givenInstrumentSet.add(i);
                }

                if (orbits.contains(orbit)) {
                    modify = true;
                    int index = orbits.indexOf(orbit);
                    sharedInstruments.get(index).addAll(givenInstrumentSet);

                } else {
                    orbits.add(orbit);
                    sharedInstruments.add(givenInstrumentSet);
                }

                nodesToRemove.add(node);
            }
        }

        if(modify){
            parent.removeNodes(nodesToRemove);

            for(int i = 0; i < orbits.size(); i++){
                Feature newFeature = featureFetcher.fetch(new NotInOrbit(params, orbits.get(i), sharedInstruments.get(i)));
                parent.addLiteral(newFeature.getName(), newFeature.getMatches());
            }
        }

        if(modify){
            return true;
        }else{
            return false;
        }
    }

    public boolean removeAbsentInstruments(Connective parent, List<Literal> literals){

        Set<Integer> absentInstruments = new HashSet<>();

        // Find all instruments that must be absent
        for(Literal node:literals){
            AbstractFilter thisFilter = this.filterFetcher.fetch(node.getName());

            if(thisFilter instanceof Absent) {
                Absent absent = (Absent) thisFilter;
                absentInstruments.add(absent.getInstrument());
            }
        }

        boolean modify = false;
        if(!absentInstruments.isEmpty()){

            Set<Formula> nodesToRemove = new HashSet<>();
            List<Formula> nodesToAdd = new ArrayList<>();

            for(Literal node:literals){

                AbstractFilter thisFilter = this.filterFetcher.fetch(node.getName());

                if(thisFilter instanceof NotInOrbit) {
                    NotInOrbit notInOrbit = (NotInOrbit) thisFilter;

                    ArrayList<Integer> instruments = new ArrayList<>(notInOrbit.getInstruments());
                    for(int instrumentToBeRemoved: absentInstruments){
                        if(instruments.contains(instrumentToBeRemoved)){
                            instruments.remove(instruments.indexOf(instrumentToBeRemoved));
                        }
                    }

                    if(!instruments.isEmpty()){
                        AbstractFilter modifiedFilter = new NotInOrbit(params, notInOrbit.getOrbit(), Utils.intCollection2Array(instruments));
                        Feature modifiedFeature = super.featureFetcher.fetch(modifiedFilter);
                        nodesToAdd.add(new Literal(modifiedFeature.getName(), modifiedFeature.getMatches()));
                    }
                    nodesToRemove.add(node);
                    modify = true;
                }

                if(thisFilter instanceof Separate) {
                    Separate separate = (Separate) thisFilter;

                    ArrayList<Integer> instruments = new ArrayList<>(separate.getInstruments());
                    for(int instrumentToBeRemoved: absentInstruments){
                        if(instruments.contains(instrumentToBeRemoved)){
                            instruments.remove(instruments.indexOf(instrumentToBeRemoved));
                        }
                    }

                    if(instruments.size() > 1){
                        AbstractFilter modifiedFilter = new Separate(params, Utils.intCollection2Array(instruments));
                        Feature modifiedFeature = super.featureFetcher.fetch(modifiedFilter);
                        nodesToAdd.add(new Literal(modifiedFeature.getName(), modifiedFeature.getMatches()));
                    }
                    nodesToRemove.add(node);
                    modify = true;
                }
            }

            if(modify){
                parent.removeNodes(nodesToRemove);
                for(Formula node: nodesToAdd){
                    parent.addNode(node);
                }
            }
        }

        return modify;
    }
}