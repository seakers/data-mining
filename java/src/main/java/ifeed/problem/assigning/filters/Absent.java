/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.assigning.filters;

import java.util.BitSet;
import java.util.Objects;

import ifeed.architecture.AbstractArchitecture;
import ifeed.architecture.BinaryInputArchitecture;
import ifeed.filter.AbstractFilter;
import ifeed.local.params.BaseParams;
import ifeed.problem.assigning.Params;

/**
 *
 * @author bang
 */
public class Absent extends AbstractFilter {

    protected int instrument;
    protected Params params;

    public Absent(BaseParams params, int i){
        super(params);
        this.params = (Params) params;
        this.instrument = i;
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

        boolean out = true;
        for(int o = 0; o< this.params.getNumOrbits(); o++){
            if(input.get(o * this.params.getNumInstruments() + instrument)){
                // If any one of the instruments are not present
                out=false;
                break;
            }
        }
        return out;
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
