package ifeed.problem.assigning;

import com.google.common.collect.Multiset;
import ifeed.feature.*;
import ifeed.feature.logic.Connective;
import ifeed.feature.logic.Formula;
import ifeed.feature.logic.Literal;
import ifeed.feature.logic.LogicalConnectiveType;
import ifeed.filter.AbstractFilter;
import ifeed.local.params.BaseParams;
import ifeed.problem.assigning.filters.InOrbit;
import ifeed.problem.assigning.filters.NotInOrbit;

import java.util.*;

public class FeatureSimplifier extends AbstractFeatureSimplifier{

    public FeatureSimplifier(BaseParams params,
                             FeatureFetcher featureFetcher,
                             FilterFetcher filterFetcher){

        super(params, featureFetcher, filterFetcher);
    }

    public void simplify(Connective root){

        List<Literal> childNodes = root.getLiteralChildren();

        if(root.getLogic()== LogicalConnectiveType.AND){
            combineInOrbits(root, childNodes);
            combineNotInOrbits(root, childNodes);
        }

        for(Connective branch: root.getConnectiveChildren()){
            simplify(branch);
        }
    }

    public void combineInOrbits(Connective parent, List<Literal> literals){

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
    }

    public void combineNotInOrbits(Connective parent, List<Literal> literals){

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
    }
}
