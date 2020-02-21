package ifeed.local;

import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OWLTest {

    public static void main(String[] args){

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

        try{

            File file = new File("/Users/bang/workspace/daphne/data-mining/ontology/ClimateCentric-extended_domain_knowledge.owl");
            OWLOntology o = manager.loadOntologyFromOntologyDocument(file);

            OWLOntologyID id = o.getOntologyID();
            System.out.println("Loaded ontology IRI: " + id.getOntologyIRI().get().toString());

            OWLReasonerFactory rf = new ReasonerFactory();
            OWLReasoner r = rf.createReasoner(o);
            r.precomputeInferences(InferenceType.CLASS_HIERARCHY);

            OWLDataFactory df = manager.getOWLDataFactory();

//            IRI RealItalianPizza = IRI.create("http://www.co−ode.org/ontologies/pizza/pizza.owl#RealItalianPizza");
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








            IRI tempIRI = IRI.create(id.getOntologyIRI().get().toString() + "#Dawn-DuskOrbit");
//            IRI LEO_600_polar = IRI.create(id.getOntologyIRI().get().toString() + "#LEO-600-polar-NA");




            OWLClass owlClass = df.getOWLClass(tempIRI);

//
//
//
//            NodeSet<OWLNamedIndividual> instances = r.getInstances(df.getOWLClass(tempIRI));
//            instances.entities().forEach((OWLNamedIndividual i) -> {
//                System.out.println(i.getIRI().getShortForm());
//            });
//
//
//            df.getOWLSubClassOfAxiom()


//            OWLOntology ontology = manager.loadOntology(IRI.create("https://protege.stanford.edu/ontologies/pizza/pizza.owl"));
//            ontology.saveOntology(new FunctionalSyntaxDocumentFormat(), System.out);


            o.axioms(owlClass).collect(Collectors.toSet());

            // load ontology
            System.out.println( "Read and classes their axioms...\n" );

            // get all classes in the ontology
                System.out.println( "Class: " + owlClass.toString() );

                // get all axioms for each class
                for( OWLAxiom axiom : o.axioms( owlClass ).collect( Collectors.toSet() ) ) {
                    System.out.println( "\tAxiom: " + axiom.toString() );

                    // create an object visitor to get to the subClass restrictions
                    axiom.accept( new OWLObjectVisitor() {

                        // found the subClassOf axiom
                        public void visit( OWLSubClassOfAxiom subClassAxiom ) {

                            // create an object visitor to read the underlying (subClassOf) restrictions
                            subClassAxiom.getSuperClass().accept( new OWLObjectVisitor() {

                                public void visit( OWLObjectSomeValuesFrom someValuesFromAxiom ) {
                                    printQuantifiedRestriction( owlClass, someValuesFromAxiom );
                                }

                                public void visit( OWLObjectExactCardinality exactCardinalityAxiom ) {
                                    printCardinalityRestriction( owlClass, exactCardinalityAxiom );
                                }

                                public void visit( OWLObjectMinCardinality minCardinalityAxiom ) {
                                    printCardinalityRestriction( owlClass, minCardinalityAxiom );
                                }

                                public void visit( OWLObjectMaxCardinality maxCardinalityAxiom ) {
                                    printCardinalityRestriction( owlClass, maxCardinalityAxiom );
                                }

                                // TODO: same for AllValuesFrom etc.
                            });
                        }
                    });

                }

                System.out.println();








        }catch (OWLOntologyCreationException e){
            e.printStackTrace();
        }
    }


    public static void printQuantifiedRestriction( OWLClass oc, OWLQuantifiedObjectRestriction restriction ) {
        System.out.println( "\t\tClass: " + oc.toString() );
        System.out.println( "\t\tClassExpressionType: " + restriction.getClassExpressionType().toString() );
        System.out.println( "\t\tProperty: "+ restriction.getProperty().toString() );
        System.out.println( "\t\tObject: " + restriction.getFiller().toString() );
        System.out.println();
    }

    public static void printCardinalityRestriction( OWLClass oc, OWLObjectCardinalityRestriction restriction ) {
        System.out.println( "\t\tClass: " + oc.toString() );
        System.out.println( "\t\tClassExpressionType: " + restriction.getClassExpressionType().toString() );
        System.out.println( "\t\tCardinality: " + restriction.getCardinality() );
        System.out.println( "\t\tProperty: "+ restriction.getProperty().toString() );
        System.out.println( "\t\tObject: " + restriction.getFiller().toString() );
        System.out.println();
    }
}
