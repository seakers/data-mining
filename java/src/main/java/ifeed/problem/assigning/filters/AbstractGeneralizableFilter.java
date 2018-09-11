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

        // If the given instrument is not included in the original set
        if(this.params.generalizationEnabled()){

            return this.params.getOrbitInstantiation(classIndex);
        }else {
            throw new IllegalStateException("Orbit specification out of range: " + classIndex);
        }
    }

    public List<Integer> instantiateInstrumentClass(int classIndex){

        // If the given instrument is not included in the original set
        if(this.params.generalizationEnabled()){

            if(!this.params.getInstrumentIndex2Name().containsKey(classIndex)){
                throw new IllegalArgumentException("Unrecognized instrument class index: " + classIndex);
            }

            return this.params.getInstrumentInstantiation(classIndex);
        }else {
            throw new IllegalStateException("Instrument specification out of range: " + classIndex);
        }
    }
}
