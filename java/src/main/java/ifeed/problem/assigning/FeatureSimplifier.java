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
import ifeed.problem.assigning.filters.*;

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

    public FeatureSimplifier(BaseParams params,
                             FeatureFetcher featureFetcher,
                             FeatureExpressionHandler expressionHandler){

        super(params, featureFetcher, featureFetcher.getFilterFetcher(), expressionHandler);
    }

    public FeatureSimplifier(BaseParams params,
                             FeatureFetcher featureFetcher,
                             FilterFetcher filterFetcher,
                             FeatureExpressionHandler expressionHandler){

        super(params, featureFetcher, filterFetcher, expressionHandler);
    }

    @Override
    public boolean simplify(Connective root){

        boolean modified = false;
        if(root.getLogic()== LogicalConnectiveType.AND){
            // Combine inOrbits and notInOrbits
            if(combineFiltersUnderAND(root)){
                modified = true;
            }

            // Remove absent instruments from notInOrbits
            if(removeAbsentInstruments(root)){
                modified = true;
            }

        }else{
            // Combine inOrbit or notInOrbit having overlapping instruments
            if(removeSupersetUnderOR(root)){
                modified = true;
            }

            if(extractSharedArgumentUnderOR(root)){
                modified = true;
            }

//            if(extractSharedArgumentUnderBranchesOR(root)){
//                modified = true;
//            }
        }

        // Recursively simplify subtrees
        for(Connective branch: root.getConnectiveChildren()){
            if(simplify(branch)){
                modified = true;
            }
        }

        return modified;
    }

    public boolean sortFeatures(Connective parent){

        List<Literal> literals = parent.getLiteralChildren();
        List<AbstractFilter> filters = new ArrayList<>();

        boolean alreadySorted = true;

        List<String> featureTypes = new ArrayList<>();
        for(Literal literal: literals){
            AbstractFilter thisFilter = this.filterFetcher.fetch(literal.getName());

            if(featureTypes.contains(thisFilter.getClass().getSimpleName())){
                if(!filters.get(filters.size() - 1).getClass().getSimpleName().equalsIgnoreCase(thisFilter.getClass().getSimpleName())){
                    alreadySorted = false;
                }
            }else{
                featureTypes.add(thisFilter.getClass().getSimpleName());
            }
            filters.add(thisFilter);
        }

        if(alreadySorted){
            return false;
        }

        List<Literal> sortedLiterals = new ArrayList<>();
        for(String featureType: featureTypes){
            for(int i = 0; i < literals.size(); i++){
                if(filters.get(i).getClass().getSimpleName().equalsIgnoreCase(featureType)){
                    sortedLiterals.add(literals.get(i));
                }
            }
        }

        parent.removeLiterals();
        for(Literal literal: sortedLiterals){
            parent.addLiteral(literal);
        }
        return true;
    }

    /**
     * Combines inOrbits/notInOrbits sharing the same orbit under AND
     * @param parent
     * @return
     */
    public boolean combineFiltersUnderAND(Connective parent){

        List<Literal> literals = parent.getLiteralChildren();

        List<Integer> inOrbitOrbits = new ArrayList<>();
        List<Integer> notInOrbitOrbits = new ArrayList<>();
        List<HashSet<Integer>> inOrbitInstruments = new ArrayList<>();
        List<HashSet<Integer>> notInOrbitInstruments = new ArrayList<>();
        List<HashSet<Integer>> inOrbitInstrumentClass = new ArrayList<>();
        List<HashSet<Integer>> notInOrbitInstrumentClass = new ArrayList<>();
        Set<Formula> nodesToRemove = new HashSet<>();

        boolean modify = false;

        for(Literal node:literals){
            AbstractFilter thisFilter = this.filterFetcher.fetch(node.getName());

            int orbit;
            Multiset<Integer> instruments;
            boolean isInOrbit;
            if(thisFilter instanceof InOrbit) {
                orbit = ((InOrbit) thisFilter).getOrbit();
                instruments = ((InOrbit) thisFilter).getInstruments();
                isInOrbit = true;
            }else if(thisFilter instanceof NotInOrbit){
                orbit = ((NotInOrbit) thisFilter).getOrbit();
                instruments = ((NotInOrbit) thisFilter).getInstruments();
                isInOrbit = false;
            }else{
                continue;
            }

            Set<Integer> givenInstrumentSetTemp = instruments.elementSet();
            HashSet<Integer> givenInstrumentSet = new HashSet<>();
            HashSet<Integer> instrumentClassSet = new HashSet<>();
            for(int i: givenInstrumentSetTemp){
                if(i >= ((Params)params).getLeftSetCardinality()){
                    instrumentClassSet.add(i);
                }else{
                    givenInstrumentSet.add(i);
                }
            }

            if(isInOrbit){
                if (inOrbitOrbits.contains(orbit)) {
                    modify = true;
                    int index = inOrbitOrbits.indexOf(orbit);
                    inOrbitInstruments.get(index).addAll(givenInstrumentSet);
                    inOrbitInstrumentClass.get(index).addAll(instrumentClassSet);
                } else {
                    inOrbitOrbits.add(orbit);
                    inOrbitInstruments.add(givenInstrumentSet);
                    inOrbitInstrumentClass.add(instrumentClassSet);
                }
            }else{
                if (notInOrbitOrbits.contains(orbit)) {
                    modify = true;
                    int index = notInOrbitOrbits.indexOf(orbit);
                    notInOrbitInstruments.get(index).addAll(givenInstrumentSet);
                    notInOrbitInstrumentClass.get(index).addAll(instrumentClassSet);

                } else {
                    notInOrbitOrbits.add(orbit);
                    notInOrbitInstruments.add(givenInstrumentSet);
                    notInOrbitInstrumentClass.add(instrumentClassSet);
                }
            }
            nodesToRemove.add(node);
        }

        if(modify){
            parent.removeNodes(nodesToRemove);
            for(int i = 0; i < inOrbitOrbits.size(); i++){
                Set<Integer> instruments = new HashSet<>();
                instruments.addAll(inOrbitInstruments.get(i));
                instruments.addAll(inOrbitInstrumentClass.get(i));
                Feature newFeature = featureFetcher.fetch(new InOrbit(params, inOrbitOrbits.get(i), instruments));
                parent.addLiteral(newFeature.getName(), newFeature.getMatches());
            }
            for(int i = 0; i < notInOrbitOrbits.size(); i++){
                HashSet<Integer> instrumentsToInclude = new HashSet<>();
                HashSet<Integer> instrumentsToExclude = new HashSet<>();
                HashSet<Integer> instruments = notInOrbitInstruments.get(i);
                HashSet<Integer> instrumentClasses = notInOrbitInstrumentClass.get(i);
                for(int cls: instrumentClasses){
                    Set<Integer> instances = ((Params)params).getLeftSetInstantiation(cls);
                    for(int instr: instruments){
                        if(instances.contains(instr)){
                            instrumentsToExclude.add(instr);
                        }
                    }
                }
                instrumentsToInclude.addAll(instruments);
                instrumentsToInclude.addAll(instrumentClasses);
                instrumentsToInclude.removeAll(instrumentsToExclude);
                Feature newFeature = featureFetcher.fetch(new NotInOrbit(params, notInOrbitOrbits.get(i), instrumentsToInclude));
                parent.addLiteral(newFeature.getName(), newFeature.getMatches());
            }
        }
        return modify;
    }

    /**
     * If there exist multiple InOrbit/NotInOrbits sharing the same orbit, check if any of the instruments are used in both
     * literals. If there exists a shared instrument, then create a new InOrbit/NotInOrbit at the parent level.
     * @param parent
     * @return
     */
    public boolean extractSharedArgumentUnderOR(Connective parent){

        List<Literal> literals = parent.getLiteralChildren();
        List<AbstractFilter> filters = new ArrayList<>();

        // Check if all literals are of the same type
        boolean allSameType = true;
        boolean allSameOrbit = true;
        boolean isInOrbit = true;
        int orbit = -1;

        if(literals.size() <= 1){
            return false;
        }

        for(int i = 0; i < literals.size(); i++){
            Literal literal = literals.get(i);
            AbstractFilter thisFilter = this.filterFetcher.fetch(literal.getName());

            int thisOrbit;
            boolean thisFilterIsInOrbit;
            if(thisFilter instanceof InOrbit){
                thisOrbit = ((InOrbit) thisFilter).getOrbit();
                thisFilterIsInOrbit = true;
            }else if(thisFilter instanceof NotInOrbit){
                thisOrbit = ((NotInOrbit) thisFilter).getOrbit();
                thisFilterIsInOrbit = false;
            }else{
                allSameType = false;
                break;
            }

            if(i == 0){
                orbit = thisOrbit;
                isInOrbit = thisFilterIsInOrbit;
            }else{
                if(orbit != thisOrbit){
                    allSameOrbit = false;
                    break;
                }
                if(isInOrbit != thisFilterIsInOrbit){
                    allSameType = false;
                    break;
                }
            }
            filters.add(thisFilter);
        }

        if(!allSameType || !allSameOrbit){
            return false;
        }

        Set<Integer> sharedInstruments = new HashSet<>();
        for(AbstractFilter filter: filters){
            Multiset<Integer> instruments;
            if(filter instanceof InOrbit){
                instruments = ((InOrbit) filter).getInstruments();
            }else if(filter instanceof NotInOrbit){
                instruments = ((NotInOrbit) filter).getInstruments();
            }else{
                continue;
            }

            if(sharedInstruments.isEmpty()){
                sharedInstruments.addAll(instruments.elementSet());
            }else{
                sharedInstruments.retainAll(instruments.elementSet());
            }
        }

        if(sharedInstruments.isEmpty()){
            return false;
        }

        // Remove all existing literals
        parent.removeLiterals();

        Connective grandParent = (Connective) parent.getParent();
        if(grandParent == null){ // Parent node is the root node since it doesn't have a parent node
            super.expressionHandler.createNewRootNode(parent);
            grandParent = parent;

            // Store the newly generated node to parent
            parent = grandParent.getConnectiveChildren().get(0);
        }

        // Add new literal
        Feature newFeature;
        if(isInOrbit){
            newFeature = featureFetcher.fetch(new InOrbit(params, orbit, sharedInstruments));
        }else{
            newFeature = featureFetcher.fetch(new NotInOrbit(params, orbit, sharedInstruments));
        }
        grandParent.addLiteral(newFeature.getName(), newFeature.getMatches());


        for(AbstractFilter filter: filters){
            Feature featureToBeAdded = null;
            if(filter instanceof InOrbit){
                Set<Integer> instruments = ((InOrbit) filter).getInstruments().elementSet();
                instruments.removeAll(sharedInstruments);
                if(!instruments.isEmpty()){
                    featureToBeAdded = featureFetcher.fetch(new InOrbit(params, orbit, instruments));
                }
            }else if(filter instanceof NotInOrbit){
                Set<Integer> instruments = ((NotInOrbit) filter).getInstruments().elementSet();
                instruments.removeAll(sharedInstruments);
                if(!instruments.isEmpty()){
                    featureToBeAdded = featureFetcher.fetch(new NotInOrbit(params, orbit, instruments));
                }
            }else{
                continue;
            }
            if(featureToBeAdded != null){
                parent.addLiteral(featureToBeAdded.getName(), featureToBeAdded.getMatches());
            }
        }

        if(parent.getChildNodes().isEmpty()){
            grandParent.removeNode(parent);
        }
        return true;
    }

    /**
     * If there exist multiple InOrbit/NotInOrbits sharing the same orbit, check if any of the instruments are used in both
     * literals. If there exists a shared instrument, then create a new InOrbit/NotInOrbit at the parent level.
     * @param parent
     * @return
     */
    public boolean extractSharedArgumentUnderBranchesOR(Connective parent){

        List<Connective> branches = parent.getConnectiveChildren();
        List<Literal> literals = parent.getLiteralChildren();

        List<Literal> literalsToBeModified = new ArrayList<>();
        List<AbstractFilter> filtersToBeModified = new ArrayList<>();

        // Check if all literals are of the same type
        boolean allSameType = true;
        boolean allSameOrbit = true;
        boolean isInOrbit = true;
        int orbit = -1;

        if(literals.size() <= 1){
            if(branches.isEmpty()){
                return false;
            }
        }

        for(int i = 0; i < literals.size(); i++){
            Literal literal = literals.get(i);
            AbstractFilter thisFilter = this.filterFetcher.fetch(literal.getName());

            int thisOrbit;
            boolean thisFilterIsInOrbit;
            if(thisFilter instanceof InOrbit){
                thisOrbit = ((InOrbit) thisFilter).getOrbit();
                thisFilterIsInOrbit = true;
            }else if(thisFilter instanceof NotInOrbit){
                thisOrbit = ((NotInOrbit) thisFilter).getOrbit();
                thisFilterIsInOrbit = false;
            }else{
                allSameType = false;
                break;
            }

            if(i == 0){
                orbit = thisOrbit;
                isInOrbit = thisFilterIsInOrbit;
            }else{
                if(orbit != thisOrbit){
                    allSameOrbit = false;
                    break;
                }
                if(isInOrbit != thisFilterIsInOrbit){
                    allSameType = false;
                    break;
                }
            }
            literalsToBeModified.add(literal);
            filtersToBeModified.add(thisFilter);
        }

        if(!allSameType || !allSameOrbit){
            return false;
        }

        for(int i = 0; i < branches.size(); i++){
            Connective branch = branches.get(i);
            boolean matchingFilterFound = false;
            for(int j = 0; j < branch.getLiteralChildren().size(); j++){
                AbstractFilter thisFilter = this.filterFetcher.fetch(branch.getLiteralChildren().get(j).getName());

                int thisOrbit;
                boolean thisFilterIsInOrbit;
                if(thisFilter instanceof InOrbit){
                    thisOrbit = ((InOrbit) thisFilter).getOrbit();
                    thisFilterIsInOrbit = true;
                }else if(thisFilter instanceof NotInOrbit){
                    thisOrbit = ((NotInOrbit) thisFilter).getOrbit();
                    thisFilterIsInOrbit = false;
                }else{
                    continue;
                }

                if(orbit == -1){
                    orbit = thisOrbit;
                    isInOrbit = thisFilterIsInOrbit;
                    break;

                }else if(orbit == thisOrbit && isInOrbit == thisFilterIsInOrbit){
                    matchingFilterFound = true;
                    literalsToBeModified.add(branch.getLiteralChildren().get(j));
                    filtersToBeModified.add(thisFilter);
                    break;
                }

            }
            if(!matchingFilterFound){
                return false;
            }
        }

        Set<Integer> sharedInstruments = new HashSet<>();
        for(AbstractFilter filter: filtersToBeModified){
            Multiset<Integer> instruments;
            if(filter instanceof InOrbit){
                instruments = ((InOrbit) filter).getInstruments();
            }else if(filter instanceof NotInOrbit){
                instruments = ((NotInOrbit) filter).getInstruments();
            }else{
                continue;
            }

            if(sharedInstruments.isEmpty()){
                sharedInstruments.addAll(instruments.elementSet());
            }else{
                sharedInstruments.retainAll(instruments.elementSet());
            }
        }

        if(sharedInstruments.isEmpty()){
            return false;
        }

        // Remove all existing literals
        List<Connective> parentNodes = new ArrayList<>();
        for(Literal node:literalsToBeModified){
            Connective parentNode = (Connective)node.getParent();
            parentNode.removeLiteral(node);
            parentNodes.add(parentNode);
        }

        Connective grandParent = (Connective) parent.getParent();
        if(grandParent == null){ // Parent node is the root node since it doesn't have a parent node
            super.expressionHandler.createNewRootNode(parent);
            grandParent = parent;

            // Store the newly generated node to parent
            parent = grandParent.getConnectiveChildren().get(0);
        }

        // Add new literal
        Feature newFeature;
        if(isInOrbit){
            newFeature = featureFetcher.fetch(new InOrbit(params, orbit, sharedInstruments));
        }else{
            newFeature = featureFetcher.fetch(new NotInOrbit(params, orbit, sharedInstruments));
        }
        grandParent.addLiteral(newFeature.getName(), newFeature.getMatches());

        for(int i = 0; i < literalsToBeModified.size(); i++){
            Connective parentNode = parentNodes.get(i);
            AbstractFilter filter = filtersToBeModified.get(i);

            Feature featureToBeAdded = null;
            if(filter instanceof InOrbit){
                Set<Integer> instruments = ((InOrbit) filter).getInstruments().elementSet();
                instruments.removeAll(sharedInstruments);
                if(!instruments.isEmpty()){
                    featureToBeAdded = featureFetcher.fetch(new InOrbit(params, orbit, instruments));
                }
            }else if(filter instanceof NotInOrbit){
                Set<Integer> instruments = ((NotInOrbit) filter).getInstruments().elementSet();
                instruments.removeAll(sharedInstruments);
                if(!instruments.isEmpty()){
                    featureToBeAdded = featureFetcher.fetch(new NotInOrbit(params, orbit, instruments));
                }
            }else{
                continue;
            }
            if(featureToBeAdded != null){
                parentNode.addLiteral(featureToBeAdded.getName(), featureToBeAdded.getMatches());
            }
        }

        for(Connective parentNode: parentNodes){
            if(parentNode.getChildNodes().isEmpty()){
                ((Connective)parentNode.getParent()).removeNode(parentNode);
            }
        }
        return true;
    }

    /**
     * If the instrument set of one InOrbit/NotInOrbit is a subset of the instrument set of the other InOrbit/NotInOrbit,
     * remove the InOrbit/NotInOrbit literal with the superset.
     * @param parent
     * @return
     */
    public boolean removeSupersetUnderOR(Connective parent){

        boolean modify = false;

        List<Literal> literals = parent.getLiteralChildren();

        if(literals.size() <= 1){
            return false;
        }

        List<Integer> inOrbitIndices = new ArrayList<>();
        List<Integer> notInOrbitIndices = new ArrayList<>();
        List<Integer> inOrbitOrbits = new ArrayList<>();
        List<Integer> notInOrbitOrbits = new ArrayList<>();
        List<Set<Integer>> inOrbitInstruments = new ArrayList<>();
        List<Set<Integer>> notInOrbitInstruments = new ArrayList<>();
        Set<Formula> nodesToRemove = new HashSet<>();

        for(int i = 0; i < literals.size(); i++){
            Literal literal = literals.get(i);
            AbstractFilter thisFilter = this.filterFetcher.fetch(literal.getName());

            if(thisFilter instanceof InOrbit){
                int orbit = ((InOrbit) thisFilter).getOrbit();
                Multiset<Integer> instruments = ((InOrbit) thisFilter).getInstruments();
                inOrbitIndices.add(i);
                inOrbitOrbits.add(orbit);
                inOrbitInstruments.add(instruments.elementSet());

            }else if(thisFilter instanceof NotInOrbit){
                int orbit = ((NotInOrbit) thisFilter).getOrbit();
                Multiset<Integer> instruments = ((NotInOrbit) thisFilter).getInstruments();
                notInOrbitIndices.add(i);
                notInOrbitOrbits.add(orbit);
                notInOrbitInstruments.add(instruments.elementSet());

            }else{
                continue;
            }
        }

        for(int i = 0; i < inOrbitIndices.size(); i++){
            Set<Integer> inst1 = inOrbitInstruments.get(i);
            for(int j = i + 1; j < inOrbitIndices.size(); j++){
                Set<Integer> inst2 = inOrbitInstruments.get(j);
                if(inOrbitOrbits.get(i) == inOrbitOrbits.get(j)){
                    if(this.checkSubset(inst1, inst2)){
                        if(inst1.size() > inst2.size()){ // Remove the literal with the bigger instrument set
                            nodesToRemove.add(literals.get(inOrbitIndices.get(i)));
                        }else{
                            nodesToRemove.add(literals.get(inOrbitIndices.get(j)));
                        }
                    }
                }
            }
        }
        for(int i = 0; i < notInOrbitIndices.size(); i++){
            Set<Integer> inst1 = notInOrbitInstruments.get(i);
            for(int j = i + 1; j < notInOrbitIndices.size(); j++){
                Set<Integer> inst2 = notInOrbitInstruments.get(j);
                if(notInOrbitOrbits.get(i) == notInOrbitOrbits.get(j)){
                    if(this.checkSubset(inst1, inst2)){ // Remove the literal with the bigger instrument set
                        if(inst1.size() > inst2.size()){
                            nodesToRemove.add(literals.get(notInOrbitIndices.get(i)));
                        }else {
                            nodesToRemove.add(literals.get(notInOrbitIndices.get(j)));
                        }
                    }
                }
            }
        }

        if(!nodesToRemove.isEmpty()){
            parent.removeNodes(nodesToRemove);
            if(parent.getChildNodes().isEmpty()){
//                Connective grandParent = (Connective) parent.getParent();
//                if(grandParent == null){ // Parent node is the root node since it doesn't have a parent node
//                    super.expressionHandler.createNewRootNode(parent);
//                    grandParent = parent;
//
//                    // Store the newly generated node to parent
//                    parent = grandParent.getConnectiveChildren().get(0);
//                }
//                grandParent.removeNode(parent);
                throw new IllegalStateException();

            } else if(parent.getChildNodes().size() == 1){
                Connective grandParent = (Connective) parent.getParent();
                if(grandParent != null){
                    grandParent.addNode(parent.getChildNodes().get(0));
                }
            }

            modify = true;
        }
        return modify;
    }

    public boolean removeAbsentInstruments(Connective parent){

        List<Literal> literals = parent.getLiteralChildren();

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

                } else if(thisFilter instanceof Separate) {
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


    private Set<Integer> getSharedElement(Multiset<Integer> instruments1, Multiset<Integer> instruments2){
        Set<Integer> sharedElements = new HashSet<>();
        for(int i:instruments1){
            if(instruments2.contains(i)){
                sharedElements.add(i);
            }
        }
        return sharedElements;
    }

    private boolean checkSubset(Set<Integer> instruments1, Set<Integer> instruments2){
        Set<Integer> biggerSet;
        Set<Integer> smallerSet;
        if(instruments1.size() > instruments2.size()){
            biggerSet = new HashSet<>(instruments1);
            smallerSet = new HashSet<>(instruments2);
        }else{
            biggerSet = new HashSet<>(instruments2);
            smallerSet = new HashSet<>(instruments1);
        }

        smallerSet.removeAll(biggerSet);
        if(smallerSet.isEmpty()){
            return true;
        }else{
            return false;
        }
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
}
