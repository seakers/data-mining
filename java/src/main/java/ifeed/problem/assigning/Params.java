/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.assigning;

import ifeed.local.params.BaseParams;
import ifeed.ontology.OntologyManager;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author bang
 */
public class Params extends BaseParams {

    protected int numInstruments;
    protected int numOrbits;
    protected boolean useOnlyInputFeatures;

    protected String[] instrumentList;
    protected String[] orbitList;
    protected Map<String, Integer> instrumentName2Index;
    protected Map<Integer, String> instrumentIndex2Name;
    protected Map<String, Integer> orbitName2Index;
    protected Map<Integer, String> orbitIndex2Name;
    protected OntologyManager ontologyManager;

    public Params(){
        numInstruments = 12;
        numOrbits = 5;
        useOnlyInputFeatures = false;
        ontologyManager = null;
    }

    public void setNumInstruments(int numInstruments) {
        this.numInstruments = numInstruments;
    }

    public void setNumOrbits(int numOrbits){
        this.numOrbits = numOrbits;
    }

    public int getNumOrbits(){
        return this.numOrbits;
    }

    public int getNumInstruments(){
        return this.numInstruments;
    }

    public boolean isUseOnlyInputFeatures(){
        return this.useOnlyInputFeatures;
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

    public Map<String, Integer> getInstrumentName2Index(){
        return this.instrumentName2Index;
    }

    public Map<Integer, String> getInstrumentIndex2Name(){
        return this.instrumentIndex2Name;
    }

    public Map<String, Integer> getOrbitName2Index(){
        return this.orbitName2Index;
    }

    public Map<Integer, String> getOrbitIndex2Name(){
        return this.orbitIndex2Name;
    }

    public boolean generalizationEnabled(){
        if(this.ontologyManager != null){
            return true;
        }else{
            return false;
        }
    }

    public void setOntologyManager(OntologyManager ontologyManager){
        this.ontologyManager = ontologyManager;
    }

    public OntologyManager getOntologyManager() {
        return ontologyManager;
    }
}
