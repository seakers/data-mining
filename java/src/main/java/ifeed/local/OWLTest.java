package ifeed.local;

import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class OWLTest {

    public static void main(String[] args){

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

        try{

            File file = new File("/Users/bang/workspace/EOSSOntology/ontology/EOSSOntology2.owl");

            OWLOntology o = manager.loadOntologyFromOntologyDocument(file);

//            IRI pizzaontology = IRI.create("http://protege.stanford.edu/ontologies/pizza/pizza.owl");
//            OWLOntology o = manager.loadOntology(pizzaontology);

            OWLOntologyID id = o.getOntologyID();
            System.out.println("Loaded ontology IRI: " + id.getOntologyIRI().get().toString());


            OWLReasonerFactory rf = new ReasonerFactory();
            OWLReasoner r = rf.createReasoner(o);
            r.precomputeInferences(InferenceType.CLASS_HIERARCHY);

            OWLDataFactory df = manager.getOWLDataFactory();

//            IRI RealItalianPizza = IRI.create("http://www.co−ode.org/ontologies/pizza/pizza.owl#RealItalianPizza");
//
//            IRI tempIRI = IRI.create(id.getOntologyIRI().get().toString() + "#Orbit");
//            r.getSubClasses(df.getOWLClass(tempIRI), false).forEach(System.out::println);
//
//            NodeSet<OWLNamedIndividual> instances = r.getInstances(df.getOWLClass(tempIRI));
//            Iterator<Node<OWLNamedIndividual>> instanceIter = instances.iterator();
//            while(instanceIter.hasNext()){
//                System.out.println("iterator next()");
//                Node<OWLNamedIndividual> instance = instanceIter.next();
//                System.out.println(instance.getRepresentativeElement().toString());
////                instance.entities().forEach((OWLNamedIndividual indiv) -> {
////                    System.out.println(indiv.toString());
////                });
//            }


            IRI tempIRI = IRI.create(id.getOntologyIRI().get().toString() + "#Orbit");
            IRI LEO_600_polar = IRI.create(id.getOntologyIRI().get().toString() + "#LEO-600-polar-NA");
            NodeSet<OWLNamedIndividual> instances = r.getInstances(df.getOWLClass(tempIRI));

            List<OWLNamedIndividual> orbit = new ArrayList<>();
            instances.entities().forEach((OWLNamedIndividual i) -> {
                if(i.getIRI().equals(LEO_600_polar)){
                    orbit.add(i);
                }
            });

            System.out.println("-------");

            NodeSet<OWLClass> types = r.getTypes(orbit.get(0));
            types.entities().forEach((OWLClass c) -> {

                System.out.println(c.getIRI().getShortForm());

            });

            System.out.println("-------");


            IRI alt600Orbit = IRI.create(id.getOntologyIRI().get().toString() + "#Altitude600Orbit");
            NodeSet<OWLNamedIndividual> instances2 = r.getInstances(df.getOWLClass(alt600Orbit));
            instances2.entities().forEach((OWLNamedIndividual i) -> {
                System.out.println(i.getIRI().getShortForm());
            });





//            OWLOntology ontology = manager.loadOntology(IRI.create("https://protege.stanford.edu/ontologies/pizza/pizza.owl"));
//            ontology.saveOntology(new FunctionalSyntaxDocumentFormat(), System.out);

        }catch (OWLOntologyCreationException e){
            e.printStackTrace();
        }
    }
}
