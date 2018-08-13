package ifeed.ontology;

import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.io.File;

public class OntologyTemp {

    public void run(OWLOntology o){

        try{

            OWLOntologyID id = o.getOntologyID();

            id.getDefaultDocumentIRI();

            OWLReasonerFactory rf = new ReasonerFactory();
            OWLReasoner r = rf.createReasoner(o);
//            r.precomputeInferences(InferenceType.CLASS_HIERARCHY);

//            OWLDataFactory df = manager.getOWLDataFactory();
//            IRI RealItalianPizza = IRI.create("http://www.co−ode.org/ontologies/pizza/pizza.owl#RealItalianPizza");
//            r.getSubClasses(df.getOWLClass(RealItalianPizza), false).forEach(System.out::println);




//            OWLOntology ontology = manager.loadOntology(IRI.create("https://protege.stanford.edu/ontologies/pizza/pizza.owl"));
//            ontology.saveOntology(new FunctionalSyntaxDocumentFormat(), System.out);

        }catch (Exception e){
            e.printStackTrace();
        }


    }
}
