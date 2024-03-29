/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.assigning.filters;
import java.util.*;
import ifeed.architecture.AbstractArchitecture;
import ifeed.architecture.BinaryInputArchitecture;
import ifeed.local.params.BaseParams;
import ifeed.problem.assigning.Params;

/**
 *
 * @author bang
 */
public class Absent extends AbstractGeneralizableFilter {

    protected int instrument;
    protected Params params;
    protected Set<Integer> instrumentInstances;

    public Absent(BaseParams params, int i){
        super(params);
        this.params = (Params) params;
        this.instrument = i;

        // If the given instrument is not included in the original set
        if(super.isInstrumentClass(this.instrument)){
            this.instrumentInstances = this.instantiateInstrumentClass(this.instrument);
        }else{
            instrumentInstances = null;
        }
    }

    public int getInstrument(){
        return this.instrument;
    }

    @Override
    public boolean apply(AbstractArchitecture a){
        return this.apply(((BinaryInputArchitecture) a).getInputs());
    }

    @Override
    public boolean apply(BitSet input){
        return apply(input, this.instrument);
    }

    public boolean apply(BitSet input, int instrument){
        boolean out = true;

        if(instrument >= this.params.getLeftSetCardinality()){
            // For each OWL instances that are members of a class
            for(int instrumentIndex: this.instrumentInstances){
                if(!this.apply(input, instrumentIndex)){
                    // If at least one of the tests fail, return false
                    out = false;
                    break;
                }
            }

        }else{
            for(int o = 0; o< this.params.getRightSetCardinality(); o++){
                if(input.get(o * this.params.getLeftSetCardinality() + instrument)){
                    // If any one of the instruments are not present
                    out = false;
                    break;
                }
            }
        }

        return out;
    }

    @Override
    public String getDescription(){
        return "Instrument " + this.params.getLeftSetEntityName(this.instrument) + " is not used";
    }

    @Override
    public String getName(){return "absent";}

    @Override
    public String toString(){
        return "{absent[;" + this.instrument + ";]}";
    }

    @Override
    public int hashCode() {
        int hash = 13;
        hash = 31 * hash + Objects.hashCode(this.instrument);
        hash = 31 * hash + Objects.hashCode(this.getName());
        return hash;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof Absent){
            Absent other = (Absent) o;
            return this.instrument == other.getInstrument();
        }
        return false;
    }
}
