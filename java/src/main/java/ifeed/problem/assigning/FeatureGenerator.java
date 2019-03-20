/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.assigning;

import ifeed.feature.AbstractFeatureGenerator;
import ifeed.local.params.BaseParams;
import ifeed.problem.assigning.filters.*;
import ifeed.filter.AbstractFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 *
 * @author bang
 */
public class FeatureGenerator extends AbstractFeatureGenerator{

    private Params params;
    private Set<Integer> restrictedInstrumentSet;
    private int norb;
    private int ninstr;

    public FeatureGenerator(BaseParams params){
        super(params);
        this.params = (Params) params;
        this.norb = this.params.getRightSetCardinality();
        this.ninstr = this.params.getLeftSetCardinality();
        restrictedInstrumentSet = new HashSet<>();
    }

    public FeatureGenerator(BaseParams params, Set<Integer> restrictedInstrumentSet) {
        super(params);
        this.params = (Params) params;
        this.norb = this.params.getRightSetCardinality();
        this.ninstr = this.params.getLeftSetCardinality();
        this.restrictedInstrumentSet = restrictedInstrumentSet;
    }

    @Override
    public List<AbstractFilter> generateCandidates(){
        
        ArrayList<AbstractFilter> candidate_features = new ArrayList<>();
        // Types
        // Present, Absent, InOrbit, NotInOrbit,
        // Separate, Together, EmptyOrbit
        // NumOrbits, NumOfInstruments

        // Preset filter expression example:
        // {presetName[orbits;instruments;numbers]}    

        if(params.useOnlyInputFeatures){

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
        } else {
            for (int i = 0; i < ninstr; i++) {
                if(restrictedInstrumentSet.contains(i)){
                    continue;
                }                
                // present, absent
                candidate_features.add(new Present(params, i));
                candidate_features.add(new Absent(params, i));
                

                for (int n = 1; n < norb + 1; n++) {
                    // numOfInstruments (number of specified instruments across all orbits)
                    candidate_features.add(new NumOfInstruments(params, -1, i, n));
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

                for (int n = 1; n < 6; n++) {
                    // numOfInstruments (number of instruments in a given orbit)
                    candidate_features.add(new NumOfInstruments(params, o, -1, n));
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
            for (int n = 1; n < 16; n++) {
                // numOfInstruments (across all orbits)
                candidate_features.add(new NumOfInstruments(params, -1, -1, n));
            }
        }
        return candidate_features;
    }

    public List<AbstractFilter> generateCandidatesWithGeneralizedVariables() {

        if(this.params.getOntologyManager() == null){
            throw new IllegalStateException();
        }

        Set<Integer> orbitClassSet = new HashSet<>();
        Set<Integer> instrumentClassSet = new HashSet<>();
        for(int o = 0; o < norb; o++){
            orbitClassSet.addAll(params.getRightSetSuperclass("Orbit",o));
        }
        for(int i = 0; i < ninstr; i++){
            instrumentClassSet.addAll(params.getLeftSetSuperclass("Instrument",i));
        }

        List<Integer> orbitClasses = new ArrayList<>(orbitClassSet);
        List<Integer> instrumentClasses = new ArrayList<>(instrumentClassSet);
        ArrayList<AbstractFilter> candidate_features = new ArrayList<>();
        // Types
        // present, absent, inOrbit, notInOrbit, together2,
        // separate2, separate3, together3, emptyOrbit
        // NumOrbits, numOfInstruments, subsetOfInstruments
        // Preset filter expression example:
        // {presetName[orbits;instruments;numbers]}


        for (int i = 0; i < instrumentClasses.size(); i++) {
            int instClass1 = instrumentClasses.get(i);

            // present, absent
            candidate_features.add(new Present(params, instClass1));
            candidate_features.add(new Absent(params, instClass1));

            for (int j = 0; j < ninstr; j++) { // Combination of a high-level class and a single instrument variable
                int inst2 = j;

                // together2, separate2
                int[] instruments_2 = {instClass1, inst2};
                candidate_features.add(new Together(params, instruments_2));
                candidate_features.add(new Separate(params, instruments_2));

                for (int k = j+1; k < ninstr; k++) { // Combination of one high-level class and two isntrument variables
                    int inst3 = k;

                    // together3, separate3
                    int[] instruments_3 = {instClass1, inst2, inst3};
                    candidate_features.add(new Together(params, instruments_3));
                    candidate_features.add(new Separate(params, instruments_3));
                }
            }

            for (int j = 0; j < i; j++) {
                int instClass2 = instrumentClasses.get(j);

                // together2, separate2
                int[] instruments_2 = {instClass1, instClass2};
                candidate_features.add(new Together(params, instruments_2));
                candidate_features.add(new Separate(params, instruments_2));

                for (int k = 0; k < ninstr; k++) { // Combination of two high-level classes and a single isntrument variable
                    int inst3 = k;

                    // together3, separate3
                    int[] instruments_3 = {instClass1, instClass2, inst3};
                    candidate_features.add(new Together(params, instruments_3));
                    candidate_features.add(new Separate(params, instruments_3));
                }
            }
        }

        for (int o = 0; o <  orbitClasses.size(); o++) {
            int orbitClass = orbitClasses.get(o);

            // emptyOrbit
            candidate_features.add(new EmptyOrbit(params, orbitClass));

            // NumOrbits
            for (int i = 0; i < ninstr; i++) {

                // inOrbit, notInOrbit
                candidate_features.add(new InOrbit(params, orbitClass,i));
                candidate_features.add(new NotInOrbit(params, orbitClass,i));

                for (int j = 0; j < i; j++) {

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

            for (int i = 0; i < instrumentClasses.size(); i++) {

                int instClass1 = instrumentClasses.get(i);

                // inOrbit, notInOrbit
                candidate_features.add(new InOrbit(params, orbitClass,instClass1));
                candidate_features.add(new NotInOrbit(params, orbitClass,instClass1));

                for (int j = 0; j < ninstr; j++){
                    int inst2 = j;

                    // togetherInOrbit2
                    int[] instruments_2 = {instClass1,inst2};
                    candidate_features.add(new InOrbit(params, orbitClass,instruments_2));
                    candidate_features.add(new NotInOrbit(params, orbitClass,instruments_2));
                }

                for (int j = 0; j < i; j++) {
                    int instClass2 = instrumentClasses.get(j);

                    // togetherInOrbit2
                    int[] instruments_2 = {instClass1, instClass2};
                    candidate_features.add(new InOrbit(params, orbitClass, instruments_2));
                    candidate_features.add(new NotInOrbit(params, orbitClass, instruments_2));

                    for (int k = 0; k < ninstr; k++) {
                        int inst3 = k;

                        // togetherInOrbit3
                        int[] instruments_3 = {instClass1, instClass2, inst3};
                        candidate_features.add(new InOrbit(params, o,instruments_3));
                        candidate_features.add(new NotInOrbit(params, o,instruments_3));
                    }
                }
            }
        }

        for (int o = 0; o <  norb; o++) {

            for (int i = 0; i < instrumentClasses.size(); i++) {

                int instClass1 = instrumentClasses.get(i);

                // inOrbit, notInOrbit
                candidate_features.add(new InOrbit(params, o,instClass1));
                candidate_features.add(new NotInOrbit(params, o,instClass1));

                for (int j = 0; j < ninstr; j++){
                    int inst2 = j;

                    // togetherInOrbit2
                    int[] instruments_2 = {instClass1,inst2};
                    candidate_features.add(new InOrbit(params, o,instruments_2));
                    candidate_features.add(new NotInOrbit(params, o,instruments_2));

                    for (int k = j+1; k < ninstr; k++) {
                        int inst3 = k;

                        // togetherInOrbit3
                        int[] instruments_3 = {instClass1, inst2, inst3};
                        candidate_features.add(new InOrbit(params, o,instruments_3));
                        candidate_features.add(new NotInOrbit(params, o,instruments_3));
                    }
                }

                for (int j = 0; j < i; j++) {
                    int instClass2 = instrumentClasses.get(j);

                    // togetherInOrbit2
                    int[] instruments_2 = {instClass1, instClass2};
                    candidate_features.add(new InOrbit(params, o, instruments_2));
                    candidate_features.add(new NotInOrbit(params, o, instruments_2));

                    for (int k = 0; k < ninstr; k++) {
                        int inst3 = k;

                        // togetherInOrbit3
                        int[] instruments_3 = {instClass1, instClass2, inst3};
                        candidate_features.add(new InOrbit(params, o,instruments_3));
                        candidate_features.add(new NotInOrbit(params, o,instruments_3));
                    }
                }
            }
        }

        candidate_features.addAll(this.generateCandidates());

        System.out.println("Total " + candidate_features.size() + " candidate base features generated");

        return candidate_features;
    }
}
