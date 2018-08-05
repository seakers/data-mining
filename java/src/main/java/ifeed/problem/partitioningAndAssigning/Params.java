/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.partitioningAndAssigning;


import ifeed.local.params.BaseParams;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Set;
import java.util.Map;

/**
 *
 * @author bang
 */
public class Params extends BaseParams{

    public boolean tallMatrix;
    public int numInstruments;
    public int numOrbits;
    public boolean useOnlyInputFeatures;

    public Params(){
        tallMatrix = false;
        numInstruments = 5;
        numOrbits = 5;
        useOnlyInputFeatures = false;
    }

    public int getNumInstruments() {
        return numInstruments;
    }

    public int getNumOrbits(){
        return numOrbits;
    }

    public boolean isUseOnlyInputFeatures(){
        return useOnlyInputFeatures;
    }

    public boolean isTallMatrix(){
        return tallMatrix;
    }

    public int[] repairInput(int[] input){

        int[] out = new int[input.length];

        Set<Integer> origianlSatIndices = new HashSet<>();
        Map<Integer, Integer> original2NewSatIndices= new HashMap<>();

        int index = 0;
        for(int i = 0; i < numInstruments; i++){
            int originalIndex = input[i];
            if(!origianlSatIndices.contains(originalIndex)){
                origianlSatIndices.add(originalIndex);
                original2NewSatIndices.put(originalIndex, index);
                index++;
            }

            out[i] = original2NewSatIndices.get(originalIndex);
        }

        for(int i = 0; i < numInstruments; i++){
            out[i + numInstruments] = -1;
        }
        for(int originalIndex:origianlSatIndices){
            int newIndex = original2NewSatIndices.get(originalIndex);
            out[newIndex + numInstruments] = input[originalIndex + numInstruments];
        }

        return out;
    }
}
