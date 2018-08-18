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
import java.util.List;

public class OntologyManager {

    protected OWLOntologyManager manager;
    protected OWLOntology ontology;
    protected OWLOntologyID ontologyID;
    protected IRI ontologyIRI;
    protected OWLReasoner reasoner;
    protected OWLDataFactory dataFactory;
    protected String root;

    public OntologyManager(String problem){
        manager = OWLManager.createOWLOntologyManager();
        root = System.getProperty("user.dir");

        String path = root + File.separator + "ontology" + File.separator + problem + ".owl";

        try{
            ontology = manager.loadOntologyFromOntologyDocument(new File(path));
            ontologyID = ontology.getOntologyID();
            ontologyIRI = ontologyID.getOntologyIRI().get();
            System.out.println("Loaded ontology IRI: " + ontologyIRI.toString());

            OWLReasonerFactory rf = new ReasonerFactory();
            reasoner = rf.createReasoner(ontology);
            reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
            dataFactory = manager.getOWLDataFactory();

        }catch (OWLOntologyCreationException e){
            e.printStackTrace();
            throw new RuntimeException("Error in loading ontology from: " + path);
        }
    }

    public List<OWLNamedIndividual> getIndividuals(String className){
        List<OWLNamedIndividual> out = new ArrayList<>();
        try{
            IRI classIRI = IRI.create(ontologyIRI.toString() + "#" + className);
            NodeSet<OWLNamedIndividual> instances = reasoner.getInstances(dataFactory.getOWLClass(classIRI));

            instances.entities().forEach((OWLNamedIndividual i) -> {
                out.add(i);
            });

        }catch (Exception e){
            e.printStackTrace();
        }
        return out;
    }

    public List<OWLNamedIndividual> getIndividuals(String className, String individualName){
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

    public List<OWLClass> getSuperClasses(String className, String individualName){

        List<OWLClass> out = new ArrayList<>();
        try{
            List<OWLNamedIndividual> individuals = getIndividuals(className, individualName);

            NodeSet<OWLClass> types = reasoner.getTypes(individuals.get(0));
            types.entities().forEach((OWLClass c) -> {
                out.add(c);
            });

        }catch (Exception e){
            e.printStackTrace();
        }
        return out;
    }

    public boolean isInstanceOf(String instanceClass, String instanceName, String targetClass){
        List<OWLClass> superClasses = getSuperClasses(instanceClass, instanceName);
        for(OWLClass c: superClasses){
            if(c.getIRI().getShortForm().equalsIgnoreCase(targetClass)){
                return true;
            }
        }
        return false;
    }

    public boolean isClassOf(String testClass, String instanceClass, String instanceName){
        if(isInstanceOf(instanceClass, instanceName, testClass)){
            return true;
        }
        return false;
    }
}
