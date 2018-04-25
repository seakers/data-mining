/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.eoss.filters;

import java.util.BitSet;
import java.util.Objects;

import ifeed.architecture.AbstractArchitecture;
import ifeed.architecture.BinaryInputArchitecture;
import ifeed.filter.Filter;
import ifeed.problem.eoss.EOSSParams;

/**
 *
 * @author bang
 */
public class Present extends Filter {

    protected int instrument;
    
    public Present(int i){
        this.instrument = i;
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
        for(int o=0;o<EOSSParams.num_orbits;o++){
            if(input.get(o*EOSSParams.num_instruments + instrument)){
                // If any one of the instruments are not present
                out=true; 
                break;
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
