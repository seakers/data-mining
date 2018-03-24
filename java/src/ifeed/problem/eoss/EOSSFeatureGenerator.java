/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.eoss;

import ifeed.problem.eoss.filters.NumOrbits;
import ifeed.problem.eoss.filters.Together;
import ifeed.problem.eoss.filters.Separate;
import ifeed.problem.eoss.filters.NotInOrbit;
import ifeed.problem.eoss.filters.InOrbit;
import ifeed.problem.eoss.filters.Absent;
import ifeed.problem.eoss.filters.Present;
import ifeed.problem.eoss.filters.EmptyOrbit;
import ifeed.filter.Filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import ifeed.feature.CandidateFeatureGenerator;

/**
 *
 * @author bang
 */
public class EOSSFeatureGenerator implements CandidateFeatureGenerator {
    
    private int norb;
    private int ninstr;
    private Set<Integer> restrictedInstrumentSet;
    
    public EOSSFeatureGenerator(){
        
        this.norb = EOSSParams.num_orbits;
        this.ninstr = EOSSParams.num_instruments;
        restrictedInstrumentSet = new HashSet<>();
    }
    
    public EOSSFeatureGenerator(Set<Integer> restrictedInstrumentSet){
        this.norb = EOSSParams.num_orbits;
        this.ninstr = EOSSParams.num_instruments;
        this.restrictedInstrumentSet = restrictedInstrumentSet;
    }    

    
    @Override
    public List<Filter> generateCandidates(){
        
        ArrayList<Filter> candidate_features = new ArrayList<>();
        
        // Types
        // present, absent, inOrbit, notInOrbit, together2, 
        // separate2, separate3, together3, emptyOrbit
        // NumOrbits, numOfInstruments, subsetOfInstruments
        // Preset filter expression example:
        // {presetName[orbits;instruments;numbers]}    

        if(EOSSParams.use_only_input_features){

            for (int o = 0; o < norb; o++) {
                for (int i = 0; i < ninstr; i++) {
                    // inOrbit, notInOrbit 
                    if(restrictedInstrumentSet.contains(i)){
                        continue;
                    }
                    candidate_features.add(new InOrbit(o,i));
                    candidate_features.add(new NotInOrbit(o,i));
                }
            }

        }else{
            for (int i = 0; i < ninstr; i++) {
                if(restrictedInstrumentSet.contains(i)){
                    continue;
                }                
                // present, absent
                candidate_features.add(new Present(i));
                candidate_features.add(new Absent(i));
                
                
                for (int o = 1; o < norb + 1; o++) {
                    // numOfInstruments (number of specified instruments across all orbits)
                    //candidate_features.add("{numOfInstruments[;" + i + ";" + j + "]}");
                }

                for (int j = 0; j < i; j++) {
                    if(restrictedInstrumentSet.contains(j)){
                        continue;
                    }                        
                    
                    // together2, separate2
                    int[] instruments_2 = {i,j};
                    candidate_features.add(new Together(instruments_2));
                    candidate_features.add(new Separate(instruments_2));

                    for (int k = 0; k < j; k++) {
                        if(restrictedInstrumentSet.contains(k)){
                            continue;
                        }                            
                        
                        // together3, separate3
                        int[] instruments_3 = {i,j,k};
                        candidate_features.add(new Together(instruments_3));
                        candidate_features.add(new Separate(instruments_3));
                    }
                }
            }

            
            for (int o = 0; o < norb; o++) {
                
                for (int n = 1; n < 9; n++) {
                    // numOfInstruments (number of instruments in a given orbit)
                    //candidate_features.add("{numOfInstruments[" + i + ";;" + j + "]}");
                }
                // emptyOrbit
                candidate_features.add(new EmptyOrbit(o));
                // NumOrbits
                int numOrbitsTemp = o + 1;
                candidate_features.add(new NumOrbits(numOrbitsTemp));
                for (int i = 0; i < ninstr; i++) {
                    if(restrictedInstrumentSet.contains(i)){
                        continue;
                    }                        
                    // inOrbit, notInOrbit
                    candidate_features.add(new InOrbit(o,i));
                    candidate_features.add(new NotInOrbit(o,i));

                    for (int j = 0; j < i; j++) {
                        if(restrictedInstrumentSet.contains(j)){
                            continue;
                        }                            
                        // togetherInOrbit2
                        int[] instruments_2 = {i,j};
                        candidate_features.add(new InOrbit(o,instruments_2));
                        candidate_features.add(new NotInOrbit(o,instruments_2));

                        for (int k = 0; k < j; k++) {
                            if(restrictedInstrumentSet.contains(k)){
                                continue;
                            }                                
                            // togetherInOrbit3
                            int[] instruments_3 = {i,j,k};
                            candidate_features.add(new InOrbit(o,instruments_3));
                            candidate_features.add(new NotInOrbit(o,instruments_3));
                        }
                    }
                }
            }
            for (int i = 1; i < 16; i++) {
                // numOfInstruments (across all orbits)
                //candidate_features.add("{numOfInstruments[;;" + i + "]}");
            }
        }
        
       
        return candidate_features;
    }

    
}
