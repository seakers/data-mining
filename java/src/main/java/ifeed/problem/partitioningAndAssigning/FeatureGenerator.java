/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.partitioningAndAssigning;

import ifeed.filter.AbstractFilter;
import ifeed.local.params.BaseParams;
import ifeed.problem.partitioningAndAssigning.filters.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 *
 * @author bang
 */

public class FeatureGenerator {

    protected Params params;
    private int norb;
    private int ninstr;
    private Set<Integer> restrictedInstrumentSet;

    public FeatureGenerator(BaseParams params){
        this.params = (Params) params;
        this.norb = this.params.getNumOrbits();
        this.ninstr = this.params.getNumInstruments();
        restrictedInstrumentSet = new HashSet<>();
    }

    public FeatureGenerator(BaseParams params, Set<Integer> restrictedInstrumentSet) {
        this.params = (Params) params;
        this.norb = this.params.getNumOrbits();
        this.ninstr = this.params.getNumInstruments();
        this.restrictedInstrumentSet = restrictedInstrumentSet;
    }

    public List<AbstractFilter> generateCandidates(){
        
        ArrayList<AbstractFilter> candidate_features = new ArrayList<>();
        // Types
        // inOrbit, notInOrbit, together2,
        // separate2, separate3, together3, emptyOrbit
        // NumOrbits, numOfInstruments, subsetOfInstruments
        // Preset filter expression example:
        // {presetName[orbits;instruments;numbers]}    

        if(params.isUseOnlyInputFeatures()){

            for (int o = 0; o < norb; o++) {
                for (int i = 0; i < ninstr; i++) {
                    // inOrbit, notInOrbit 
                    if(restrictedInstrumentSet.contains(i)){
                        continue;
                    }
                    candidate_features.add(new InOrbit(params, o,i));
                    candidate_features.add(new NotInOrbit(params, o,i));
                }
            }

        }else{
            for (int i = 0; i < ninstr; i++) {
                if(restrictedInstrumentSet.contains(i)){
                    continue;
                }

                for (int j = 0; j < i; j++) {
                    if(restrictedInstrumentSet.contains(j)){
                        continue;
                    }

                    // together2, separate2
                    int[] instruments_2 = {i,j};
                    candidate_features.add(new Together(params, instruments_2));
                    candidate_features.add(new Separate(params, instruments_2));

                    for (int k = 0; k < j; k++) {
                        if(restrictedInstrumentSet.contains(k)){
                            continue;
                        }

                        // together3, separate3
                        int[] instruments_3 = {i,j,k};
                        candidate_features.add(new Together(params, instruments_3));
                        candidate_features.add(new Separate(params, instruments_3));
                    }
                }
            }

            for (int o = 0; o < norb; o++) {

                for (int n = 1; n < ninstr; n++) {
                    // numOfInstruments (number of instruments in a given orbit)
                    candidate_features.add(new NumOfInstruments(params, o,n));
                }
                // emptyOrbit
                candidate_features.add(new EmptyOrbit(params, o));
                // NumOrbits
                int numOrbitsTemp = o + 1;
                candidate_features.add(new NumOrbits(params, numOrbitsTemp));
                for (int i = 0; i < ninstr; i++) {
                    if(restrictedInstrumentSet.contains(i)){
                        continue;
                    }
                    // inOrbit, notInOrbit
                    candidate_features.add(new InOrbit(params, o,i));
                    candidate_features.add(new NotInOrbit(params, o,i));

                    for (int j = 0; j < i; j++) {
                        if(restrictedInstrumentSet.contains(j)){
                            continue;
                        }
                        // togetherInOrbit2
                        int[] instruments_2 = {i,j};
                        candidate_features.add(new InOrbit(params, o,instruments_2));
                        candidate_features.add(new NotInOrbit(params, o,instruments_2));

                        for (int k = 0; k < j; k++) {
                            if(restrictedInstrumentSet.contains(k)){
                                continue;
                            }
                            // togetherInOrbit3
                            int[] instruments_3 = {i,j,k};
                            candidate_features.add(new InOrbit(params, o,instruments_3));
                            candidate_features.add(new NotInOrbit(params, o,instruments_3));
                        }
                    }
                }
            }
        }
        return candidate_features;
    }
}
