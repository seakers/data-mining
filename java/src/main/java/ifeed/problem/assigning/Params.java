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

    protected boolean useOnlyInputFeatures;

    protected List<String> leftSet;
    protected List<String> rightSet;
    protected List<String> leftSetGeneralizedConcepts;
    protected List<String> rightSetGeneralizedConcepts;

    protected Map<Integer, List<Integer>> leftSetSuperclassMap;
    protected Map<Integer, List<Integer>> leftSetInstantiationMap;
    protected Map<Integer, List<Integer>> rightSetSuperclassMap;
    protected Map<Integer, List<Integer>> rightSetInstantiationMap;

    public Params(){

        this.leftSet = new ArrayList<>();
        this.rightSet = new ArrayList<>();

        this.useOnlyInputFeatures = false;
        this.ontologyManager = null;

        this.leftSetGeneralizedConcepts = new ArrayList<>();
        this.rightSetGeneralizedConcepts = new ArrayList<>();
    }

    public Params(List<String> leftSet, List<String> rightSet){
        this();
        this.leftSet = leftSet;
        this.rightSet = rightSet;
    }

    public Params(Params params){

        this(params.leftSet, params.rightSet);
        this.useOnlyInputFeatures = params.useOnlyInputFeatures;
        this.ontologyManager = params.ontologyManager;

        this.leftSetInstantiationMap = params.leftSetInstantiationMap;
        this.leftSetSuperclassMap = params.leftSetSuperclassMap;
        this.rightSetInstantiationMap = params.rightSetInstantiationMap;
        this.rightSetSuperclassMap = params.rightSetSuperclassMap;
    }

    @Override
    public void setOntologyManager(OntologyManager ontologyManager){
        leftSetInstantiationMap = new HashMap<>();
        rightSetInstantiationMap = new HashMap<>();
        leftSetSuperclassMap = new HashMap<>();
        rightSetSuperclassMap = new HashMap<>();
        super.setOntologyManager(ontologyManager);
    }

    public int getRightSetCardinality(){
        return this.rightSet.size();
    }

    public int getLeftSetCardinality(){
        return this.leftSet.size();
    }

    public List<String> getLeftSet(){ return this.leftSet; }

    public List<String> getLeftSetGeneralizedConcepts(){ return this.leftSetGeneralizedConcepts; }

    public List<String> getRightSet(){ return this.rightSet; }

    public List<String> getRightSetGeneralizedConcepts(){ return this.rightSetGeneralizedConcepts; }

    public void setUseOnlyInputFeatures(){ this.useOnlyInputFeatures = true; }

    public boolean isUseOnlyInputFeatures(){
        return this.useOnlyInputFeatures;
    }

    public void setLeftSet(String[] leftSet){
        this.leftSet = new ArrayList<>();
        for(String entity:leftSet){
            this.leftSet.add(entity);
        }
    }

    public void setLeftSet(List<String> leftSet){
        this.leftSet = leftSet;
    }

    public void setRightSet(String[] rightSet){
        this.rightSet = new ArrayList<>();
        for(String entity:rightSet){
            this.rightSet.add(entity);
        }
    }

    public void setRightSet(List<String> rightSet){
        this.rightSet = rightSet;
    }

    public String getLeftSetEntityName(int index){
        // Get the entity name
        String entityName;
        if(index < this.leftSet.size()){
            entityName = this.leftSet.get(index);
        }else{
            entityName = this.leftSetGeneralizedConcepts.get(index - this.leftSet.size());
        }
        return entityName;
    }

    public String getRightSetEntityName(int index){
        // Get the entity name
        String entityName;
        if(index < this.rightSet.size()){
            entityName = this.rightSet.get(index);
        }else{
            entityName = this.rightSetGeneralizedConcepts.get(index - this.rightSet.size());
        }
        return entityName;
    }

    public int getLeftSetEntityIndex(String name){
        int index;
        if(this.leftSet.contains(name)){
            index = this.leftSet.indexOf(name);

        }else if(this.leftSetGeneralizedConcepts.contains(name)){
            index = this.leftSet.size() + this.leftSetGeneralizedConcepts.indexOf(name);

        }else{
            throw new IllegalStateException("Unrecognized entity name: " + name);
        }

        return index;
    }

    public int getRightSetEntityIndex(String name){
        int index;
        if(this.rightSet.contains(name)){
            index = this.rightSet.indexOf(name);

        }else if(this.rightSetGeneralizedConcepts.contains(name)){
            index = this.rightSet.size() + this.rightSetGeneralizedConcepts.indexOf(name);

        }else{
            throw new IllegalStateException("Unrecognized entity name: " + name);
        }

        return index;
    }

    public void addLeftSetGeneralizedConcept(String className){

        if(!this.leftSetGeneralizedConcepts.contains(className)){
            this.leftSetGeneralizedConcepts.add(className);
            int index = this.leftSet.size() + this.leftSetGeneralizedConcepts.size() - 1;
            System.out.println("New left set class " + className + " added with index: " + index);
        }
    }

    public void addRightSetGeneralizedConcept(String className){
        if(!this.rightSetGeneralizedConcepts.contains(className)){
            this.rightSetGeneralizedConcepts.add(className);
            int index = this.rightSet.size() + this.rightSetGeneralizedConcepts.size() - 1;
            System.out.println("New right set class " + className + " added with index: " + index);
        }
    }

    public List<Integer> getLeftSetSuperclass(String inputClassName, int index){

        if(this.leftSetSuperclassMap.containsKey(index)){
            return this.leftSetSuperclassMap.get(index);
        }

        // Get the entity name
        String entityName = this.getLeftSetEntityName(index);

        // Get individual OWL instances
        List<String> instanceClassNamesList = this.getOntologyManager().getSuperClasses(inputClassName, entityName);
        List<Integer> classList = new ArrayList<>();

        for(String className: instanceClassNamesList){
            this.addLeftSetGeneralizedConcept(className);
            int instanceClassIndex = this.leftSet.size() + this.leftSetGeneralizedConcepts.size() - 1;
            classList.add(instanceClassIndex);
        }

        this.leftSetSuperclassMap.put(index, classList);
        return classList;
    }

    public List<Integer> getRightSetSuperclass(String inputClassName, int index){

        if(this.rightSetSuperclassMap.containsKey(index)){
            return this.rightSetSuperclassMap.get(index);
        }

        // Get the entity name
        String entityName = this.getRightSetEntityName(index);

        // Get individual OWL instances
        List<String> instanceClassNamesList = this.getOntologyManager().getSuperClasses(inputClassName, entityName);
        List<Integer> classList = new ArrayList<>();

        for(String className: instanceClassNamesList){
            this.addRightSetGeneralizedConcept(className);
            int instanceClassIndex = this.rightSet.size() + this.rightSetGeneralizedConcepts.size() - 1;
            classList.add(instanceClassIndex);
        }

        this.rightSetSuperclassMap.put(index, classList);
        return classList;
    }

    public List<Integer> getLeftSetInstantiation(int classIndex){

        if(this.leftSetInstantiationMap.containsKey(classIndex)){
            return this.leftSetInstantiationMap.get(classIndex);
        }

        // Get the class name
        String className = this.getLeftSetEntityName(classIndex);

        // Get individual OWL instances
        List<String> instanceNamesList = this.getOntologyManager().getIndividuals(className);
        List<Integer> instanceList = new ArrayList<>();

        for(String instanceName: instanceNamesList){
            int instanceIndex = this.getLeftSetEntityIndex(instanceName);
            instanceList.add(instanceIndex);
        }

        this.leftSetInstantiationMap.put(classIndex, instanceList);
        return instanceList;
    }

    public List<Integer> getRightSetInstantiation(int classIndex){

        if(this.rightSetInstantiationMap.containsKey(classIndex)){
            return this.rightSetInstantiationMap.get(classIndex);
        }

        // Get the class name
        String className = this.getRightSetEntityName(classIndex);

        // Get individual OWL instances
        List<String> instanceNamesList = this.getOntologyManager().getIndividuals(className);
        List<Integer> instanceList = new ArrayList<>();

        for(String instanceName: instanceNamesList){
            int instanceIndex = this.getRightSetEntityIndex(instanceName);
            instanceList.add(instanceIndex);
        }

        this.rightSetInstantiationMap.put(classIndex, instanceList);
        return instanceList;
    }
}
