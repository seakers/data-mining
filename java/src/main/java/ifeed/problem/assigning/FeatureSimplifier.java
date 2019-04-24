package ifeed.problem.assigning;

import com.google.common.collect.Multiset;
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

        String original = root.getName();

        boolean modified = false;

        // Combine inOrbits and notInOrbits
        if(root.getLogic()== LogicalConnectiveType.AND){
            if(combineInOrbitsAND(root, root.getLiteralChildren())){
                modified = true;
            }

            if(combineNotInOrbitsAND(root, root.getLiteralChildren())){
                modified = true;
            }

            // Remove absent instruments from notInOrbits
            if(removeAbsentInstruments(root, root.getLiteralChildren())){
                modified = true;
            }

        }else{
            if(combineInOrbitsOR(root, root.getLiteralChildren())){
                modified = true;
            }

            if(combineNotInOrbitsOR(root, root.getLiteralChildren())){
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
     * If the instrument set of one InOrbit literal is a subset of the instrument set of the other InOrbit literal,
     * remove the InOrbit literal with the superset.
     * @param parent
     * @param literals
     * @return
     */
    public boolean combineInOrbitsOR(Connective parent, List<Literal> literals){

        Map<Integer, List<Integer>> orbit2Index = new HashMap<>();
        Map<Integer, Multiset<Integer>> index2Instruments = new HashMap<>();
        Set<Formula> nodesToRemove = new HashSet<>();
        boolean modify = false;

        for(int i = 0; i < literals.size(); i++){
            Literal node = literals.get(i);
            AbstractFilter thisFilter = this.filterFetcher.fetch(node.getName());

            if(thisFilter instanceof InOrbit) {
                InOrbit inOrbit = (InOrbit) thisFilter;

                int orbit = inOrbit.getOrbit();
                Multiset<Integer> instruments = inOrbit.getInstruments();

                List<Integer> indexList;
                if(!orbit2Index.containsKey(orbit)){
                    indexList = new ArrayList<>();
                }else{
                    indexList = orbit2Index.get(orbit);
                }
                indexList.add(i);
                orbit2Index.put(orbit, indexList);
                index2Instruments.put(i, instruments);
            }
        }

        for(int o: orbit2Index.keySet()){
            if(orbit2Index.get(o).size() > 1){ // If there exist multiple literals sharing the same orbit
                List<Integer> indexList = orbit2Index.get(o);
                for(int i = 0; i < indexList.size(); i++){
                    int index1 = indexList.get(i);
                    Multiset<Integer> instruments1 = index2Instruments.get(index1);
                    for(int j = i + 1; j < indexList.size(); j++){
                        int index2 = indexList.get(j);
                        Multiset<Integer> instruments2 = index2Instruments.get(index2);
                        if(checkSubset(instruments1, instruments2)){ // If one instrument set is a subset of the other set
                            if(instruments1.size() > instruments2.size()){
                                nodesToRemove.add(literals.get(index1));
                                break;
                            }else{
                                nodesToRemove.add(literals.get(index2));
                            }
                        }
                    }
                }
            }
        }

        if(!nodesToRemove.isEmpty()){
            parent.removeNodes(nodesToRemove);

            if(parent.getChildNodes().isEmpty()){
                Connective grandParent = (Connective) parent.getParent();
                if(grandParent == null){ // Parent node is the root node since it doesn't have a parent node
                    super.expressionHandler.createNewRootNode(parent);
                    grandParent = parent;

                    // Store the newly generated node to parent
                    parent = grandParent.getConnectiveChildren().get(0);
                }
                grandParent.removeNode(parent);
                grandParent.addNode(nodesToRemove.iterator().next());
            }
            modify = true;
        }
        return modify;
    }

    /**
     * If there exist multiple NotInOrbits with the same orbit, check if any of the instruments are shared in both
     * literals. If there exists a shared instrument, then create a new NotInOrbit at the grandparent level.
     * @param parent
     * @param literals
     * @return
     */
    public boolean combineNotInOrbitsOR(Connective parent, List<Literal> literals){

        Map<Integer, List<Integer>> orbit2Index = new HashMap<>();
        Map<Integer, Multiset<Integer>> index2Instruments = new HashMap<>();
        Set<Formula> nodesToRemove = new HashSet<>();
        boolean modify = false;

        for(int i = 0; i < literals.size(); i++){
            Literal node = literals.get(i);
            AbstractFilter thisFilter = this.filterFetcher.fetch(node.getName());

            if(thisFilter instanceof NotInOrbit) {
                NotInOrbit inOrbit = (NotInOrbit) thisFilter;

                int orbit = inOrbit.getOrbit();
                Multiset<Integer> instruments = inOrbit.getInstruments();

                List<Integer> indexList;
                if(!orbit2Index.containsKey(orbit)){
                    indexList = new ArrayList<>();
                }else{
                    indexList = orbit2Index.get(orbit);
                }
                indexList.add(i);
                orbit2Index.put(orbit, indexList);
                index2Instruments.put(i, instruments);
            }
        }

        Map<Integer, Set<Integer>> orbit2SharedInstruments = new HashMap<>();
        Map<Integer, Set<Multiset<Integer>>> orbit2InstrumentSets = new HashMap<>();
        for(int o: orbit2Index.keySet()){
            if(orbit2Index.get(o).size() > 1){ // If there exist multiple literals sharing the same orbit
                List<Integer> indexList = orbit2Index.get(o);
                for(int i = 0; i < indexList.size(); i++){
                    int index1 = indexList.get(i);
                    Multiset<Integer> instruments1 = index2Instruments.get(index1);

                    boolean sharedInstrumentFound = false;
                    for(int j = i + 1; j < indexList.size(); j++){
                        int index2 = indexList.get(j);
                        Multiset<Integer> instruments2 = index2Instruments.get(index2);

                        // Check if there exists any shared instrument
                        Set<Integer> shared = getSharedElement(instruments1, instruments2);
                        if(!shared.isEmpty()) {
                            // Save the shared instruments
                            if (orbit2SharedInstruments.containsKey(o)) {
                                shared.addAll(orbit2SharedInstruments.get(o));
                            }
                            orbit2SharedInstruments.put(o, shared);
                            nodesToRemove.add(literals.get(index1));
                            nodesToRemove.add(literals.get(index2));

                            Set<Multiset<Integer>> setOfInstrumentSets;
                            if(orbit2InstrumentSets.containsKey(o)){
                                setOfInstrumentSets = orbit2InstrumentSets.get(o);
                            }else{
                                setOfInstrumentSets = new HashSet<>();
                            }
                            setOfInstrumentSets.add(instruments2);
                            orbit2InstrumentSets.put(o, setOfInstrumentSets);
                            sharedInstrumentFound = true;
                        }
                    }

                    if(sharedInstrumentFound){
                        Set<Multiset<Integer>> setOfInstrumentSets = orbit2InstrumentSets.get(o);
                        setOfInstrumentSets.add(instruments1);
                        orbit2InstrumentSets.put(o, setOfInstrumentSets);
                    }
                }
            }
        }

        if(!nodesToRemove.isEmpty()){
            parent.removeNodes(nodesToRemove);

            Connective grandParent = (Connective) parent.getParent();
            if(grandParent == null){ // Parent node is the root node since it doesn't have a parent node
                super.expressionHandler.createNewRootNode(parent);
                grandParent = parent;

                // Store the newly generated node to parent
                parent = grandParent.getConnectiveChildren().get(0);
            }

            for(int o: orbit2SharedInstruments.keySet()){
                Set<Integer> sharedInstruments = orbit2SharedInstruments.get(o);
                Feature newFeature = featureFetcher.fetch(new NotInOrbit(params, o, sharedInstruments));
                grandParent.addLiteral(newFeature.getName(), newFeature.getMatches());

                Set<Multiset<Integer>> instrumentSets = orbit2InstrumentSets.get(o);
                List<Feature> featuresToBeAdded = new ArrayList<>();
                for(Multiset<Integer> set: instrumentSets){
                    set.removeAll(sharedInstruments);
                    if(!set.isEmpty()){
                        Feature tempNewFeature = featureFetcher.fetch(new NotInOrbit(params, o, set));
                        featuresToBeAdded.add(tempNewFeature);
                    }
                }
                if(featuresToBeAdded.size() > 1){
                    for(Feature feat: featuresToBeAdded){
                        parent.addLiteral(feat.getName(), feat.getMatches());
                    }
                }else if(featuresToBeAdded.size() == 1){
                    Feature feat = featuresToBeAdded.get(0);
                    grandParent.addLiteral(feat.getName(), feat.getMatches());
                }
            }

            if(parent.getChildNodes().isEmpty()){
                grandParent.removeNode(parent);
            }

            modify = true;
        }
        return modify;
    }

    private Set<Integer> getSharedElement(Multiset<Integer> instruments1, Multiset<Integer> instruments2){

        Set<Integer> sharedElements = new HashSet<>();
        for(int i:instruments1){
            if(instruments2.contains(i)){
                sharedElements.add(i);
            }
        }
        return sharedElements;
    }

    private boolean checkSubset(Multiset<Integer> instruments1, Multiset<Integer> instruments2){
        Multiset<Integer> biggerSet;
        Multiset<Integer> smallerSet;
        if(instruments1.size() > instruments2.size()){
            biggerSet = instruments1;
            smallerSet = instruments2;
        }else{
            biggerSet = instruments2;
            smallerSet = instruments1;
        }
        for(int i: smallerSet){
            if(!biggerSet.contains(i)){
                return false;
            }
        }
        return true;
    }

    /**
     * Combines inOrbits sharing the same orbit
     * @param parent
     * @param literals
     * @return
     */
    public boolean combineInOrbitsAND(Connective parent, List<Literal> literals){

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

        return modify;
    }

    public boolean combineNotInOrbitsAND(Connective parent, List<Literal> literals){

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

        return modify;
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
