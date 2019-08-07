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

    private Set<String> leftSetIgnoredClasses;
    private Set<String> rightSetIgnoredClasses;

    protected Map<Integer, Set<Integer>> leftSetSuperclassMap;
    protected Map<Integer, Set<Integer>> leftSetDirectSuperclassMap;
    protected Map<Integer, Set<Integer>> leftSetInstantiationMap;

    protected Map<Integer, Set<Integer>> rightSetSuperclassMap;
    protected Map<Integer, Set<Integer>> rightSetDirectSuperclassMap;
    protected Map<Integer, Set<Integer>> rightSetInstantiationMap;


    public Params(){
        this.leftSet = new ArrayList<>();
        this.rightSet = new ArrayList<>();

        this.useOnlyInputFeatures = false;
        this.ontologyManager = null;

        this.leftSetGeneralizedConcepts = new ArrayList<>();
        this.rightSetGeneralizedConcepts = new ArrayList<>();

        this.leftSetIgnoredClasses = new HashSet<>();
        this.leftSetIgnoredClasses.add("Thing");
        this.leftSetIgnoredClasses.add("Instrument");
        this.leftSetIgnoredClasses.add("NamedInstrument");

        this.rightSetIgnoredClasses= new HashSet<>();
        this.rightSetIgnoredClasses.add("Thing");
        this.rightSetIgnoredClasses.add("Orbit");
        this.rightSetIgnoredClasses.add("NamedOrbit");
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
        this.leftSetDirectSuperclassMap = params.leftSetDirectSuperclassMap;
        this.rightSetInstantiationMap = params.rightSetInstantiationMap;
        this.rightSetSuperclassMap = params.rightSetSuperclassMap;
        this.rightSetDirectSuperclassMap = params.rightSetDirectSuperclassMap;
    }

    @Override
    public void setOntologyManager(OntologyManager ontologyManager){
        leftSetInstantiationMap = new HashMap<>();
        rightSetInstantiationMap = new HashMap<>();
        this.resetRightSetSuperclassMap();
        this.resetLeftSetSuperclassMap();
        super.setOntologyManager(ontologyManager);
    }

    public void resetRightSetSuperclassMap(){
        this.rightSetSuperclassMap = new HashMap<>();
        this.rightSetDirectSuperclassMap = new HashMap<>();
    }

    public void resetLeftSetSuperclassMap(){
        this.leftSetSuperclassMap = new HashMap<>();
        this.leftSetDirectSuperclassMap = new HashMap<>();
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
            this.resetLeftSetSuperclassMap();
        }
    }

    public void addRightSetGeneralizedConcept(String className){
        if(!this.rightSetGeneralizedConcepts.contains(className)){
            this.rightSetGeneralizedConcepts.add(className);
            int index = this.rightSet.size() + this.rightSetGeneralizedConcepts.size() - 1;
            System.out.println("New right set class " + className + " added with index: " + index);
            this.resetRightSetSuperclassMap();
        }
    }

    public String combineLeftSetClasses(String className1, String className2){
        String newClassName = this.getOntologyManager().combineClasses(className1, className2);
        this.addLeftSetGeneralizedConcept(newClassName);
        return newClassName;
    }

    public String combineRightSetClasses(String className1, String className2){
        String newClassName = this.getOntologyManager().combineClasses(className1, className2);
        this.addRightSetGeneralizedConcept(newClassName);
        return newClassName;
    }

    public Set<Integer> getLeftSetSuperclass(int index){
        return this.getLeftSetSuperclass(index, false);
    }

    public Set<Integer> getLeftSetSuperclass(int index, boolean direct){

        Set<Integer> out;
        if(this.leftSetDirectSuperclassMap.containsKey(index) && direct){
            out = this.leftSetDirectSuperclassMap.get(index);

        }else if(this.leftSetSuperclassMap.containsKey(index) && !direct){
            out = this.leftSetSuperclassMap.get(index);

        }else{
            // Get the entity name
            String entityName = this.getLeftSetEntityName(index);

            // Get individual OWL instances
            List<String> superClassNames = this.getOntologyManager().getSuperClasses(entityName, direct, this.leftSetIgnoredClasses);
            Set<Integer> classSet = new HashSet<>();

            for(String className: superClassNames){
                this.addLeftSetGeneralizedConcept(className);
                int classIndex = this.leftSet.size() + this.leftSetGeneralizedConcepts.indexOf(className);
                classSet.add(classIndex);
            }

            if(direct){
                this.leftSetDirectSuperclassMap.put(index, classSet);
            }else{
                this.leftSetSuperclassMap.put(index, classSet);
            }
            out = classSet;
        }

        return setCopy(out);
    }

    public Set<Integer> getRightSetSuperclass(int index){
        return this.getRightSetSuperclass(index, false);
    }

    public Set<Integer> getRightSetSuperclass(int index, boolean direct){

        Set<Integer> out;
        if(this.rightSetDirectSuperclassMap.containsKey(index) && direct){
            out = this.rightSetDirectSuperclassMap.get(index);

        }else if(this.rightSetSuperclassMap.containsKey(index) && !direct){
            out = this.rightSetSuperclassMap.get(index);

        }else{
            // Get the entity name
            String entityName = this.getRightSetEntityName(index);

            // Get individual OWL instances
            List<String> instanceClassNamesList = this.getOntologyManager().getSuperClasses(entityName, direct,  this.rightSetIgnoredClasses);
            Set<Integer> classSet = new HashSet<>();

            for(String className: instanceClassNamesList){
                this.addRightSetGeneralizedConcept(className);
                int classIndex = this.rightSet.size() + this.rightSetGeneralizedConcepts.indexOf(className);
                classSet.add(classIndex);
            }

            if(direct){
                this.rightSetDirectSuperclassMap.put(index, classSet);
            }else{
                this.rightSetSuperclassMap.put(index, classSet);
            }

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

    public List<Integer> getRightSetClassesCoveringGivenIndividuals(Set<Integer> orbitInstances){
        return this.getRightSetClassesCoveringGivenIndividuals(orbitInstances, false);
    }

    public List<Integer> getRightSetClassesCoveringGivenIndividuals(Set<Integer> orbitInstances, boolean direct){

        Set<String> orbitInstanceNames = new HashSet<>();
        for(int o: orbitInstances){
            orbitInstanceNames.add(this.getRightSetEntityName(o));
        }
        List<String> outputClassNames = this.getOntologyManager().getClassesCoveringGivenIndividuals(orbitInstanceNames, direct, this.rightSetIgnoredClasses);

        List<Integer> out = new ArrayList<>();
        for(String c: outputClassNames){
            out.add(this.getRightSetEntityIndex(c));
        }
        return out;
    }

    public List<Integer> getLeftSetClassesCoveringGivenIndividuals(Set<Integer> instrumentInstances){
        return this.getLeftSetClassesCoveringGivenIndividuals(instrumentInstances, false);
    }

    public List<Integer> getLeftSetClassesCoveringGivenIndividuals(Set<Integer> instrumentInstances, boolean direct){

        Set<String> instrumentInstanceNames = new HashSet<>();
        for(int i: instrumentInstances){
            instrumentInstanceNames.add(this.getLeftSetEntityName(i));
        }
        List<String> outputClassNames = this.getOntologyManager().getClassesCoveringGivenIndividuals(instrumentInstanceNames, direct, this.leftSetIgnoredClasses);

        List<Integer> out = new ArrayList<>();
        for(String c: outputClassNames){
            out.add(this.getLeftSetEntityIndex(c));
        }
        return out;
    }

    public boolean isGeneralizedConceptLeftSet(int index){
        if(index >= this.leftSet.size()){
            return true;
        }else{
            return false;
        }
    }

    public boolean isGeneralizedConceptRightSet(int index){
        if(index >= this.rightSet.size()){
            return true;
        }else{
            return false;
        }
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
