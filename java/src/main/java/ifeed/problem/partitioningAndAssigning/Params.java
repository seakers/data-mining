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

    protected boolean tallMatrix;
    protected int numInstruments;
    protected int numOrbits;
    protected boolean useOnlyInputFeatures;

    protected String[] instrumentList;
    protected String[] orbitList;
    protected Map<String, Integer> instrumentName2Index;
    protected Map<Integer, String> instrumentIndex2Name;
    protected Map<String, Integer> orbitName2Index;
    protected Map<Integer, String> orbitIndex2Name;

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

    public void setInstrumentList(String[] instrumentList){
        this.instrumentList = instrumentList;
        this.numInstruments = instrumentList.length;
        this.instrumentName2Index = new HashMap<>();
        this.instrumentIndex2Name = new HashMap<>();
        for(int i = 0; i < instrumentList.length; i++){
            this.instrumentName2Index.put(instrumentList[i], i);
            this.instrumentIndex2Name.put(i, instrumentList[i]);
        }
    }

    public void setOrbitList(String[] orbitList){
        this.orbitList = orbitList;
        this.numOrbits = orbitList.length;
        this.orbitName2Index = new HashMap<>();
        this.orbitIndex2Name = new HashMap<>();
        for(int i = 0; i < orbitList.length; i++){
            this.orbitName2Index.put(orbitList[i], i);
            this.orbitIndex2Name.put(i, orbitList[i]);
        }
    }

    public String[] getInstrumentList(){
        return this.instrumentList;
    }

    public String[] getOrbitList(){
        return this.orbitList;
    }
}
