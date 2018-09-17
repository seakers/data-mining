package ifeed.ontology;

import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OntologyManager {

    protected String problem;
    protected OWLOntologyManager manager;
    protected OWLOntology ontology;
    protected OWLOntologyID ontologyID;
    protected IRI ontologyIRI;
    protected OWLReasoner reasoner;
    protected OWLDataFactory dataFactory;
    protected String root;

    protected Map<String, List<String>> instanceMap;
    protected Map<String, List<String>> superclassMap;

    public OntologyManager(String path, String problem){

        this.problem = problem;
        this.manager = OWLManager.createOWLOntologyManager();
        path = path + File.separator + problem + ".owl";

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

        }catch (OWLOntologyCreationException e){
            e.printStackTrace();
            throw new RuntimeException("Error in loading ontology from: " + path);
        }
    }

    public String getProblem(){
        return this.problem;
    }

    public List<String> getIndividuals(String className){
        List<String> out = new ArrayList<>();
        try{
            IRI classIRI = IRI.create(ontologyIRI.toString() + "#" + className);
            NodeSet<OWLNamedIndividual> instances = reasoner.getInstances(dataFactory.getOWLClass(classIRI));

            instances.entities().forEach((OWLNamedIndividual i) -> {
                out.add(i.getIRI().getShortForm());
            });

        }catch (Exception e){
            e.printStackTrace();
        }
        return out;
    }

    public List<String> getIndividuals(String className, String individualName){

        String key = className + "_" + individualName;
        if(this.instanceMap.containsKey(key)){
            return this.instanceMap.get(key);
        }

        List<String> out = new ArrayList<>();
        try{
            IRI classIRI = IRI.create(ontologyIRI.toString() + "#" + className);
            NodeSet<OWLNamedIndividual> instances = reasoner.getInstances(dataFactory.getOWLClass(classIRI));

            IRI individualIRI = IRI.create(ontologyIRI.toString() + "#" + individualName);
            instances.entities().forEach((OWLNamedIndividual i) -> {
                if(i.getIRI().equals(individualIRI)){
                    out.add(i.getIRI().getShortForm());
                }
            });

        }catch (Exception e){
            e.printStackTrace();
        }

        this.instanceMap.put(key, out);
        return out;
    }

    public List<OWLNamedIndividual> getOWLNamedIndividuals(String className, String individualName){
        List<OWLNamedIndividual> out = new ArrayList<>();
        try{
            IRI classIRI = IRI.create(ontologyIRI.toString() + "#" + className);
            NodeSet<OWLNamedIndividual> instances = reasoner.getInstances(dataFactory.getOWLClass(classIRI));

            IRI individualIRI = IRI.create(ontologyIRI.toString() + "#" + individualName);
            instances.entities().forEach((OWLNamedIndividual i) -> {
                if(i.getIRI().equals(individualIRI)){
                    out.add(i);
                }
            });

        }catch (Exception e){
            e.printStackTrace();
        }
        return out;
    }

    public List<String> getSuperClasses(String className, String individualName){

        String key = className + "_" + individualName;
        if(this.superclassMap.containsKey(key)){
            return this.superclassMap.get(key);
        }

        List<String> out = new ArrayList<>();
        try{
            List<OWLNamedIndividual> individuals = getOWLNamedIndividuals(className, individualName);

            if(!individuals.isEmpty()){
                NodeSet<OWLClass> types = reasoner.getTypes(individuals.get(0));
                types.entities().forEach((OWLClass c) -> {
                    if(!c.getIRI().getShortForm().equalsIgnoreCase("Thing") &&
                            !c.getIRI().getShortForm().equalsIgnoreCase(className)){
                        out.add(c.getIRI().getShortForm());
                    }
                });
            }

        }catch (Exception e){
            e.printStackTrace();
        }

        superclassMap.put(key, out);
        return out;
    }

    public Map<String, List<String>> getInstanceMap(){
        return this.instanceMap;
    }

    public Map<String, java.util.List<String>> getSuperclassMap() {
        return superclassMap;
    }
}
