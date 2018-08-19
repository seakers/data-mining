package ifeed.problem.assigning.filters;

import com.google.common.collect.Multiset;
import ifeed.filter.AbstractFilter;
import ifeed.local.params.BaseParams;
import ifeed.problem.assigning.Params;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

import java.util.*;

public abstract class AbstractGeneralizableFilter extends AbstractFilter {

    Params params;

    public AbstractGeneralizableFilter(BaseParams params){
        super(params);
        this.params = (Params) params;
    }

    public List<Integer> instantiateOrbitClass(int classIndex){

        List<Integer> orbitInstances = new ArrayList<>();

        // If the given instrument is not included in the original set
        if(this.params.generalizationEnabled()){

            // Get the class name
            String orbitClass = this.params.getOrbitIndex2Name().get(classIndex);

            // Get individual OWL instances
            List<OWLNamedIndividual> instanceList = this.params.getOntologyManager().getIndividuals(orbitClass);
            for(OWLNamedIndividual instance: instanceList){

                int instanceIndex = this.params.getOrbitName2Index().get(instance.getIRI().getShortForm());
                orbitInstances.add(instanceIndex);
            }
        }else {
            throw new IllegalStateException("Instrument specification out of range: " + classIndex);
        }

        return orbitInstances;
    }

    public List<Integer> instantiateInstrumentClass(int classIndex){

        List<Integer> instrumentInstances = new ArrayList<>();

        // If the given instrument is not included in the original set
        if(this.params.generalizationEnabled()){

            // Get the class name
            String instrumentClass = this.params.getInstrumentIndex2Name().get(classIndex);

            // Get individual OWL instances
            List<OWLNamedIndividual> instanceList = this.params.getOntologyManager().getIndividuals(instrumentClass);
            for(OWLNamedIndividual instance: instanceList){
                int instanceIndex = this.params.getInstrumentName2Index().get(instance.getIRI().getShortForm());
                instrumentInstances.add(instanceIndex);
            }
        }else {
            throw new IllegalStateException("Instrument specification out of range: " + classIndex);
        }

        return instrumentInstances;
    }

    public Map<Integer, List<Integer>> instantiateInstrumentClass(Multiset<Integer> classIndices){

        Map<Integer, List<Integer>> instrumentInstancesMap = new HashMap<>();
        for(int classIndex: classIndices){
            if(this.params.generalizationEnabled()){

                // Get the name of the given class
                String instrumentClass = this.params.getInstrumentIndex2Name().get(classIndex);

                // Get all instances of the current class
                List<OWLNamedIndividual> instanceList = this.params.getOntologyManager().getIndividuals(instrumentClass);
                List<Integer> instanceIndices = new ArrayList<>();
                for(OWLNamedIndividual instance: instanceList){
                    int instanceIndex = this.params.getInstrumentName2Index().get(instance.getIRI().getShortForm());
                    instanceIndices.add(instanceIndex);
                }
                instrumentInstancesMap.put(classIndex, instanceIndices);

            }else{
                throw new IllegalStateException("Instrument specification out of range: " + classIndex);
            }
        }
        if(instrumentInstancesMap.isEmpty()){
            instrumentInstancesMap = null;
        }

        return instrumentInstancesMap;
    }
}
