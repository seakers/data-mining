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

    protected Map<Integer, Set<Integer>> leftSetSuperclassMap;
    protected Map<Integer, Set<Integer>> leftSetInstantiationMap;
    protected Map<Integer, Set<Integer>> rightSetSuperclassMap;
    protected Map<Integer, Set<Integer>> rightSetInstantiationMap;

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

    public List<String> getLeftSet(){ return setCopy(this.leftSet); }

    public List<String> getLeftSetGeneralizedConcepts(){ return setCopy(this.leftSetGeneralizedConcepts); }

    public List<String> getRightSet(){ return setCopy(this.rightSet); }

    public List<String> getRightSetGeneralizedConcepts(){ return setCopy(this.rightSetGeneralizedConcepts); }

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

    public Set<Integer> getLeftSetSuperclass(String inputClassName, int index){

        Set<Integer> out;
        if(this.leftSetSuperclassMap.containsKey(index)){
            out = this.leftSetSuperclassMap.get(index);

        }else{
            // Get the entity name
            String entityName = this.getLeftSetEntityName(index);

            // Get individual OWL instances
            List<String> instanceClassNamesList = this.getOntologyManager().getSuperClasses(inputClassName, entityName);
            Set<Integer> classSet = new HashSet<>();

            for(String className: instanceClassNamesList){
                this.addLeftSetGeneralizedConcept(className);
                int classIndex = this.leftSet.size() + this.leftSetGeneralizedConcepts.indexOf(className);
                classSet.add(classIndex);
            }

            this.leftSetSuperclassMap.put(index, classSet);
            out = classSet;
        }

        return setCopy(out);
    }

    public Set<Integer> getRightSetSuperclass(String inputClassName, int index){

        Set<Integer> out;
        if(this.rightSetSuperclassMap.containsKey(index)){
            out = this.rightSetSuperclassMap.get(index);

        }else{
            // Get the entity name
            String entityName = this.getRightSetEntityName(index);

            // Get individual OWL instances
            List<String> instanceClassNamesList = this.getOntologyManager().getSuperClasses(inputClassName, entityName);
            Set<Integer> classSet = new HashSet<>();

            for(String className: instanceClassNamesList){
                this.addRightSetGeneralizedConcept(className);
                int classIndex = this.rightSet.size() + this.rightSetGeneralizedConcepts.indexOf(className);
                classSet.add(classIndex);
            }

            this.rightSetSuperclassMap.put(index, classSet);
            out = classSet;
        }

        return setCopy(out);
    }

    public Set<Integer> getLeftSetInstantiation(int classIndex){

        Set<Integer> out;
        if(this.leftSetInstantiationMap.containsKey(classIndex)){
            out = this.leftSetInstantiationMap.get(classIndex);

        }else{
            // Get the class name
            String className = this.getLeftSetEntityName(classIndex);

            // Get individual OWL instances
            List<String> instanceNamesList = this.getOntologyManager().getIndividuals(className);
            Set<Integer> instanceSet = new HashSet<>();

            for(String instanceName: instanceNamesList){
                int instanceIndex = this.getLeftSetEntityIndex(instanceName);
                instanceSet.add(instanceIndex);
            }

            this.leftSetInstantiationMap.put(classIndex, instanceSet);
            out = instanceSet;
        }

        return setCopy(out);
    }

    public Set<Integer> getRightSetInstantiation(int classIndex){

        Set<Integer> out;
        if(this.rightSetInstantiationMap.containsKey(classIndex)){
            out = this.rightSetInstantiationMap.get(classIndex);

        }else{
            // Get the class name
            String className = this.getRightSetEntityName(classIndex);

            // Get individual OWL instances
            List<String> instanceNamesList = this.getOntologyManager().getIndividuals(className);
            Set<Integer> instanceSet = new HashSet<>();

            for(String instanceName: instanceNamesList){
                int instanceIndex = this.getRightSetEntityIndex(instanceName);
                instanceSet.add(instanceIndex);
            }

            this.rightSetInstantiationMap.put(classIndex, instanceSet);

            out = instanceSet;
        }

        return setCopy(out);
    }

    private Set<Integer> setCopy(Set<Integer> input){
        Set<Integer> output = new HashSet<>(input.size());
        for(int i: input){
            output.add(i);
        }
        return output;
    }

    private List<String> setCopy(List<String> input){
        List<String> output = new ArrayList<>(input);
        Collections.copy(output, input);
        return output;
    }
}
