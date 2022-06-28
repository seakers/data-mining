package ifeed.ontology;

import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import java.io.File;
import java.util.*;

public class OntologyManager {

    protected int problem_id;
    protected OWLOntologyManager manager;
    protected OWLOntology ontology;
    protected OWLOntologyID ontologyID;
    protected IRI ontologyIRI;
    protected OWLReasoner reasoner;
    protected OWLDataFactory dataFactory;
    protected String root;

    protected Set<String> combinedClasses;
    protected Map<String, List<String>> instanceMap;
    protected Map<String, List<String>> superclassMap;
    protected Map<String, List<String>> directSuperclassMap;

    public OntologyManager(String path, int problem_id){

        this.problem_id = problem_id;
        this.manager = OWLManager.createOWLOntologyManager();
        path = path + File.separator + problem_id + ".owl";

        try{
            ontology = manager.loadOntologyFromOntologyDocument(new File(path));
            ontologyID = ontology.getOntologyID();
            ontologyIRI = ontologyID.getOntologyIRI().get();
            System.out.println("Loaded ontology IRI: " + ontologyIRI.toString());

            OWLReasonerFactory rf = new ReasonerFactory();
            reasoner = rf.createReasoner(ontology);
            reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
            dataFactory = manager.getOWLDataFactory();

            instanceMap = new HashMap<>();
            superclassMap = new HashMap<>();
            directSuperclassMap = new HashMap<>();
            combinedClasses = new HashSet<>();

        }catch (OWLOntologyCreationException e){
            e.printStackTrace();
            throw new RuntimeException("Error in loading ontology from: " + path);
        }
    }

    public void resetSuperclassMap(){
        this.superclassMap = new HashMap<>();
        this.directSuperclassMap = new HashMap<>();
    }

    public int getProblemId(){
        return this.problem_id;
    }

    public String getCombinedClassName(String class1Name, String class2Name){

        StringJoiner sj = new StringJoiner("_and_");

        // Define a string array in order to sort class names
        String[] tempStringArray = new String[2];
        tempStringArray[0] = class1Name;
        tempStringArray[1] = class2Name;

        // Sort class names alphabetically
        Arrays.sort(tempStringArray);
        for(String className: tempStringArray){
            sj.add(className);
        }

        return sj.toString();
    }

    public String combineClasses(String class1Name, String class2Name){

        String combinedClassName = this.getCombinedClassName(class1Name, class2Name);

        try{
            if(!this.combinedClasses.contains(combinedClassName)){

                // Define a new class
                OWLClass newClass = dataFactory.getOWLClass(ontologyIRI + "#" + combinedClassName);
                OWLDeclarationAxiom declarationAxiom = dataFactory.getOWLDeclarationAxiom(newClass);
                ontology.add(declarationAxiom);

                IRI class1IRI = IRI.create(ontologyIRI.toString() + "#" + class1Name);
                IRI class2IRI = IRI.create(ontologyIRI.toString() + "#" + class2Name);
                OWLClass class1 = dataFactory.getOWLClass(class1IRI);
                OWLClass class2 = dataFactory.getOWLClass(class2IRI);
                OWLSubClassOfAxiom subClassOfAxiom1 = dataFactory.getOWLSubClassOfAxiom(newClass, class1);
                OWLSubClassOfAxiom subClassOfAxiom2 = dataFactory.getOWLSubClassOfAxiom(newClass, class2);
                ontology.add(subClassOfAxiom1);
                ontology.add(subClassOfAxiom2);

                NodeSet<OWLNamedIndividual> class1Instances = reasoner.getInstances(class1);
                NodeSet<OWLNamedIndividual> class2Instances = reasoner.getInstances(class2);

                List<OWLNamedIndividual> class1IndividualList = new ArrayList<>();
                class1Instances.entities().forEach((OWLNamedIndividual individual) -> {
                    class1IndividualList.add(individual);
                });
                List<OWLNamedIndividual> class2IndividualList = new ArrayList<>();
                class2Instances.entities().forEach((OWLNamedIndividual individual) -> {
                    class2IndividualList.add(individual);
                });

                List<OWLNamedIndividual> intersection = class1IndividualList;
                intersection.retainAll(class2IndividualList);

                for(OWLNamedIndividual individual: intersection){
                    OWLClassAssertionAxiom classAssertionAxiom = dataFactory.getOWLClassAssertionAxiom(newClass, individual);
                    ontology.add(classAssertionAxiom);
                }

//                ontology.saveOntology();
                reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
                reasoner.flush();
                this.combinedClasses.add(combinedClassName);
                this.resetSuperclassMap();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return combinedClassName;
    }

    public List<String> getIndividuals(String className){

        if(this.instanceMap.containsKey(className)){
            return this.instanceMap.get(className);
        }

        List<String> out = new ArrayList<>();
        List<OWLNamedIndividual> individuals = this.getOWLNamedIndividuals(className);

        for(OWLNamedIndividual individual: individuals){
            out.add(individual.getIRI().getShortForm());
        }

        this.instanceMap.put(className, out);
        return out;
    }

    public List<OWLNamedIndividual> getOWLNamedIndividuals(String className){

        List<OWLNamedIndividual> out = new ArrayList<>();
        try{
            IRI classIRI = IRI.create(ontologyIRI.toString() + "#" + className);
            OWLClass owlClass = dataFactory.getOWLClass(classIRI);

            NodeSet<OWLNamedIndividual> instances = reasoner.getInstances(owlClass);
            instances.entities().forEach((OWLNamedIndividual i) -> {
                out.add(i);
            });

        }catch (Exception e){
            e.printStackTrace();
        }

        return out;
    }

    public List<String> getClassesCoveringGivenIndividuals(Set<String> individualNames, boolean direct, Set<String> excludedClasses){

        Set<String> testedClasses = new HashSet<>();
        Set<String> superClassSet = new HashSet<>();

        for(String entityName: individualNames){
            List<String> superClassNames = this.getSuperClasses(entityName, direct, excludedClasses);

            for(String className: superClassNames){
                if(testedClasses.contains(className)){
                    continue;

                }else{
                    testedClasses.add(className);
                    boolean containsAllIndividuals = true;
                    List<String> testIndividuals = this.getIndividuals(className);
                    for(String indivName: individualNames){
                        if(!testIndividuals.contains(indivName)){
                            containsAllIndividuals = false;
                        }
                    }

                    if(containsAllIndividuals){

                        // Check and remove if both the parent class and the child class are included
                        List<String> testSuperClasses = this.getSuperClasses(className, false, excludedClasses);

                        Set<String> classesToBeRemoved = new HashSet<>();
                        boolean skip = false;
                        for(String addedClass: superClassSet){
                            List<String> testSuperClasses2 = this.getSuperClasses(addedClass, false, excludedClasses);

                            if(testSuperClasses2.contains(className)){
                                skip = true;
                            }else if(testSuperClasses.contains(addedClass)){
                                classesToBeRemoved.add(addedClass);
                            }
                        }

                        if(!skip){
                            superClassSet.removeAll(classesToBeRemoved);
                            superClassSet.add(className);
                        }
                    }
                }
            }
        }

        List<String> out = new ArrayList<>();
        out.addAll(superClassSet);
        return out;
    }

    public List<String> getSuperClasses(String entityName, Set<String> excludedClasses){
        return getSuperClasses(entityName, false, excludedClasses);
    }

    public List<String> getSuperClasses(String entityName, boolean direct, Set<String> excludedClasses){

        if(direct){
            if(this.directSuperclassMap.containsKey(entityName)){
                return this.directSuperclassMap.get(entityName);
            }
        }else{
            if(this.superclassMap.containsKey(entityName)){
                return this.superclassMap.get(entityName);
            }
        }

        excludedClasses.add("Thing");

        List<String> out = new ArrayList<>();
        try{

            IRI entityIRI = IRI.create(ontologyIRI.toString() + "#" + entityName);

            OWLClass owlClass = dataFactory.getOWLClass(entityIRI);
            OWLNamedIndividual owlIndividual = dataFactory.getOWLNamedIndividual(entityIRI);

            if(ontology.isDeclared(owlIndividual)){
                NodeSet<OWLClass> types = reasoner.getTypes(owlIndividual, direct);
                types.entities().forEach((OWLClass c) -> {
                    if(!excludedClasses.contains(c.getIRI().getShortForm())){
                        out.add(c.getIRI().getShortForm());
                    }
                });

            } else if(ontology.isDeclared(owlClass)){
                NodeSet<OWLClass> superClassNodeSet = reasoner.getSuperClasses(owlClass, direct);
                superClassNodeSet.entities().forEach((OWLClass c) -> {
                    if(!excludedClasses.contains(c.getIRI().getShortForm())){
                        out.add(c.getIRI().getShortForm());
                    }
                });

            } else{
                throw new IllegalStateException("Unrecognized OWL entity: " + entityName);
            }

        }catch (Exception e){
            e.printStackTrace();
        }

        if(direct){
            directSuperclassMap.put(entityName, out);
        }else{
            superclassMap.put(entityName, out);
        }
        return out;
    }

    public Map<String, List<String>> getInstanceMap(){
        return this.instanceMap;
    }

    public Map<String, java.util.List<String>> getSuperclassMap() {
        return superclassMap;
    }
}
