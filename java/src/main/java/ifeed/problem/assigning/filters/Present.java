/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.assigning.filters;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Objects;

import ifeed.architecture.AbstractArchitecture;
import ifeed.architecture.BinaryInputArchitecture;
import ifeed.filter.AbstractFilter;
import ifeed.local.params.BaseParams;
import ifeed.problem.assigning.Params;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

/**
 *
 * @author bang
 */
public class Present extends AbstractGeneralizableFilter {

    protected Params params;
    protected int instrument;

    protected List<Integer> instrumentInstances;
    
    public Present(BaseParams params, int i){
        super(params);
        this.params = (Params) params;
        this.instrument = i;

        // If the given instrument is not included in the original set
        if(this.instrument >= this.params.getNumInstruments()){
            this.instrumentInstances = this.instantiateInstrumentClass(this.instrument);
        }else{
            instrumentInstances = null;
        }
    }

    public int getInstrument() {
        return instrument;
    }

    @Override
    public boolean apply(AbstractArchitecture a){
        return this.apply(((BinaryInputArchitecture) a).getInputs());
    }

    @Override
    public boolean apply(BitSet input){
        boolean out = false;
        if(this.instrumentInstances != null){

            // For each OWL instances that are members of a class
            for(int instrumentIndex: this.instrumentInstances){
                if((new Present(this.params, instrumentIndex)).apply(input)){
                    // If at least one of the test is successful, return true
                    out = true;
                    break;
                }
            }

        }else{
            for(int o = 0; o< this.params.getNumOrbits(); o++){
                if(input.get(o* this.params.getNumInstruments() + instrument)){
                    // If any one of the instruments are not present
                    out=true;
                    break;
                }
            }
        }
        return out;
    }
    
    @Override
    public String getName(){return "present";}

    @Override
    public String toString(){
        return "{present[;" + this.instrument + ";]}";
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
        if(o instanceof Present){
            Present other = (Present) o;
            return this.instrument == other.getInstrument();
        }
        return false;
    }
}
