/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.assigning;

import ifeed.local.params.BaseParams;
import ifeed.ontology.OntologyManager;

import java.util.*;

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

    protected Map<Integer, List<Integer>> instrumentSuperclassMap;
    protected Map<Integer, List<Integer>> instrumentInstantiationMap;
    protected Map<Integer, List<Integer>> orbitSuperclassMap;
    protected Map<Integer, List<Integer>> orbitInstantiationMap;

    public Params(){
        numInstruments = 12;
        numOrbits = 5;
        useOnlyInputFeatures = false;
        ontologyManager = null;
    }

    public Params(Params params){
        this.numInstruments = params.numInstruments;
        this.numOrbits = params.numOrbits;
        this.useOnlyInputFeatures = params.useOnlyInputFeatures;
        this.ontologyManager = params.ontologyManager;

        this.setOrbitList(params.orbitList);
        this.setInstrumentList(params.instrumentList);
        this.instrumentName2Index = params.getInstrumentName2Index();
        this.instrumentIndex2Name = params.getInstrumentIndex2Name();
        this.orbitName2Index = params.getOrbitName2Index();
        this.orbitIndex2Name = params.getOrbitIndex2Name();

        this.instrumentInstantiationMap = params.instrumentInstantiationMap;
        this.instrumentSuperclassMap = params.instrumentSuperclassMap;
        this.orbitInstantiationMap = params.orbitInstantiationMap;
        this.orbitSuperclassMap = params.orbitSuperclassMap;
    }

    @Override
    public void setOntologyManager(OntologyManager ontologyManager){
        instrumentInstantiationMap = new HashMap<>();
        orbitInstantiationMap = new HashMap<>();
        instrumentSuperclassMap = new HashMap<>();
        orbitSuperclassMap = new HashMap<>();
        super.setOntologyManager(ontologyManager);
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

    public void setUseOnlyInputFeatures(){ this.useOnlyInputFeatures = true; }

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

    public void addInstrumentClass(String className){
        if(!this.instrumentName2Index.containsKey(className)){
            int index = Collections.max(this.instrumentIndex2Name.keySet()) + 1;
            System.out.println("New instrument class " + className + " added with index: " + index);
            this.instrumentIndex2Name.put(index, className);
            this.instrumentName2Index.put(className, index);
        }
    }

    public void addOrbitClass(String className){
        if(!this.orbitName2Index.containsKey(className)){
            int index = Collections.max(this.orbitIndex2Name.keySet()) + 1;
            System.out.println("New orbit class " + className + " added with index: " + index);
            this.orbitIndex2Name.put(index, className);
            this.orbitName2Index.put(className, index);
        }
    }

    public List<Integer> getInstrumentSuperclass(int index){

        if(this.instrumentSuperclassMap.containsKey(index)){
            return this.instrumentSuperclassMap.get(index);
        }

        // Get the class name
        String instrumentName = this.getInstrumentIndex2Name().get(index);

        // Get individual OWL instances
        List<String> instanceClassNamesList = this.getOntologyManager().getSuperClasses("Instrument", instrumentName);
        List<Integer> classList = new ArrayList<>();

        for(String className: instanceClassNamesList){
            this.addInstrumentClass(className);
            int instanceClassIndex = this.getInstrumentName2Index().get(className);
            classList.add(instanceClassIndex);
        }

        this.instrumentSuperclassMap.put(index, classList);
        return classList;
    }

    public List<Integer> getInstrumentInstantiation(int classIndex){

        if(this.instrumentInstantiationMap.containsKey(classIndex)){
            return this.instrumentInstantiationMap.get(classIndex);
        }

        // Get the class name
        String instrumentClass = this.getInstrumentIndex2Name().get(classIndex);

        // Get individual OWL instances
        List<String> instanceNamesList = this.getOntologyManager().getIndividuals(instrumentClass);
        List<Integer> instanceList = new ArrayList<>();

        for(String instanceName: instanceNamesList){
            int instanceIndex = this.getInstrumentName2Index().get(instanceName);
            instanceList.add(instanceIndex);
        }

        this.instrumentInstantiationMap.put(classIndex, instanceList);
        return instanceList;
    }

    public List<Integer> getOrbitSuperclass(int index){

        if(this.orbitSuperclassMap.containsKey(index)){
            return this.orbitSuperclassMap.get(index);
        }

        // Get the class name
        String orbitName = this.getOrbitIndex2Name().get(index);

        // Get individual OWL instances
        List<String> instanceClassNamesList = this.getOntologyManager().getSuperClasses("Orbit", orbitName);
        List<Integer> classList = new ArrayList<>();

        for(String className: instanceClassNamesList){
            this.addOrbitClass(className);
            int instanceClassIndex = this.getOrbitName2Index().get(className);
            classList.add(instanceClassIndex);
        }

        this.orbitSuperclassMap.put(index, classList);
        return classList;
    }

    public List<Integer> getOrbitInstantiation(int classIndex){

        if(this.orbitInstantiationMap.containsKey(classIndex)){
            return this.orbitInstantiationMap.get(classIndex);
        }

        // Get the class name
        String orbitClass = this.getOrbitIndex2Name().get(classIndex);

        // Get individual OWL instances
        List<String> instanceNamesList = this.getOntologyManager().getIndividuals(orbitClass);
        List<Integer> instanceList = new ArrayList<>();

        for(String instanceName: instanceNamesList){
            int instanceIndex = this.getOrbitName2Index().get(instanceName);
            instanceList.add(instanceIndex);
        }

        this.orbitInstantiationMap.put(classIndex, instanceList);
        return instanceList;
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
}
