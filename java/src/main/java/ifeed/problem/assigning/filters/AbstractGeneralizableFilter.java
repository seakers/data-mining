package ifeed.problem.assigning.filters;

import ifeed.filter.AbstractFilter;
import ifeed.local.params.BaseParams;
import ifeed.problem.assigning.Params;

import java.util.*;

public abstract class AbstractGeneralizableFilter extends AbstractFilter {

    Params params;

    public AbstractGeneralizableFilter(BaseParams params){
        super(params);
        this.params = (Params) params;
    }

    public boolean isOrbitClass(int orbitIndex){
        if(orbitIndex >= this.params.getRightSetCardinality()){
            return true;
        }else{
            return false;
        }
    }

    public boolean isInstrumentClass(int instrIndex){
        if(instrIndex >= this.params.getLeftSetCardinality()){
            return true;
        }else{
            return false;
        }
    }

    public Set<Integer> instantiateOrbitClass(int classIndex){

        // If the given instrument is not included in the original set
        if(this.params.generalizationEnabled()){

            return this.params.getRightSetInstantiation(classIndex);
        }else {
            throw new IllegalStateException("Orbit specification out of range: " + classIndex);
        }
    }

    public Set<Integer> instantiateInstrumentClass(int classIndex){

        // If the given instrument is not included in the original set
        if(this.params.generalizationEnabled()){
            return this.params.getLeftSetInstantiation(classIndex);
        }else {
            throw new IllegalStateException("Instrument specification out of range: " + classIndex);
        }
    }
}
