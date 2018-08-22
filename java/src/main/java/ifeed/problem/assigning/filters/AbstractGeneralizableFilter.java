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
            List<String> instanceList = this.params.getOntologyManager().getIndividuals(orbitClass);
            for(String instanceName: instanceList){
                int instanceIndex = this.params.getOrbitName2Index().get(instanceName);
                orbitInstances.add(instanceIndex);
            }
        }else {
            throw new IllegalStateException("Orbit specification out of range: " + classIndex);
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
            List<String> instanceList = this.params.getOntologyManager().getIndividuals(instrumentClass);
            for(String instanceName: instanceList){
                int instanceIndex = this.params.getInstrumentName2Index().get(instanceName);
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
                List<String> instanceList = this.params.getOntologyManager().getIndividuals(instrumentClass);
                List<Integer> instanceIndices = new ArrayList<>();
                for(String instanceName: instanceList){
                    int instanceIndex = this.params.getInstrumentName2Index().get(instanceName);
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
