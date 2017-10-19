/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed_dm.EOSS;

import ifeed_dm.CandidateFeatureGenerator;
import ifeed_dm.BinaryInputFeature;
import ifeed_dm.BinaryInputFilter;
import ifeed_dm.DataMiningParams;
import ifeed_dm.EOSS.EOSSParams;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author bang
 */
public class EOSSFeatureGenerator implements CandidateFeatureGenerator{
    
    private int norb;
    private int ninstr;
    private boolean use_only_primitive_features;
    
    public EOSSFeatureGenerator(){
        
        this.norb = EOSSParams.num_orbits;
        this.ninstr = EOSSParams.num_instruments;
        
    }

    
    @Override
    public List<BinaryInputFilter> generateCandidates(){
        
        ArrayList<BinaryInputFilter> candidate_features = new ArrayList<>();
        
        // Types
        // present, absent, inOrbit, notInOrbit, together2, 
        // separate2, separate3, together3, emptyOrbit
        // NumOrbits, numOfInstruments, subsetOfInstruments
        // Preset filter expression example:
        // {presetName[orbits;instruments;numbers]}    

        if(use_only_primitive_features){

            for (int i = 0; i < norb; i++) {
                for (int j = 0; j < ninstr; j++) {
                    // inOrbit, notInOrbit 
                    candidate_features.add(new InOrbit(i,j));
                    candidate_features.add(new NotInOrbit(i,j));
                }
            }

        }else{
            for (int i = 0; i < norb; i++) {
                // present, absent
                candidate_features.add(new Present(i));
                candidate_features.add(new Absent(i));
                for (int j = 1; j < norb + 1; j++) {
                    // numOfInstruments (number of specified instruments across all orbits)
                    //candidate_features.add("{numOfInstruments[;" + i + ";" + j + "]}");
                }

                for (int j = 0; j < i; j++) {
                    // together2, separate2
                    int[] instruments_2 = {i,j};
                    candidate_features.add(new Together(instruments_2));
                    candidate_features.add(new Separate(instruments_2));

                    for (int k = 0; k < j; k++) {
                        // together3, separate3
                        int[] instruments_3 = {i,j,k};
                        candidate_features.add(new Together(instruments_3));
                        candidate_features.add(new Separate(instruments_3));
                    }
                }
            }

            for (int i = 0; i < norb; i++) {
                for (int j = 1; j < 9; j++) {
                    // numOfInstruments (number of instruments in a given orbit)
                    //candidate_features.add("{numOfInstruments[" + i + ";;" + j + "]}");
                }
                // emptyOrbit
                candidate_features.add(new EmptyOrbit(i));
                // NumOrbits
                int numOrbitsTemp = i + 1;
                candidate_features.add(new NumOrbits(numOrbitsTemp));
                for (int j = 0; j < ninstr; j++) {
                    // inOrbit, notInOrbit
                    candidate_features.add(new InOrbit(i,j));
                    candidate_features.add(new NotInOrbit(i,j));

                    for (int k = 0; k < j; k++) {
                        // togetherInOrbit2
                        int[] instruments_2 = {j,k};
                        candidate_features.add(new InOrbit(i,instruments_2));
                        candidate_features.add(new InOrbit(i,instruments_2));

                        for (int l = 0; l < k; l++) {
                            // togetherInOrbit3
                            int[] instruments_3 = {j,k,l};
                            candidate_features.add(new InOrbit(i,instruments_3));
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
