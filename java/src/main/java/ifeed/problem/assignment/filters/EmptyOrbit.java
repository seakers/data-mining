/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.assignment.filters;

import java.util.BitSet;
import java.util.Objects;

import ifeed.architecture.AbstractArchitecture;
import ifeed.architecture.BinaryInputArchitecture;
import ifeed.filter.AbstractFilter;
import ifeed.problem.assignment.Params;

/**
 * 
 *
 * @author bang
 */
public class EmptyOrbit extends AbstractFilter {
    
    protected int orbit;
    
    public EmptyOrbit(int o){
        this.orbit = o;
    }

    public int getOrbit(){ return this.orbit; }

    @Override
    public boolean apply(AbstractArchitecture a){
        return this.apply(((BinaryInputArchitecture) a).getInputs());
    }

    @Override
    public boolean apply(BitSet input){
        
        boolean out = true; // empty
        for(int i = 0; i< Params.num_instruments; i++){
            if(input.get(orbit* Params.num_instruments+i)){
                out=false; // instrument found inside the orbit
                break;
            }
        }
        return out;
    }
    
    @Override
    public String getName(){return "emptyOrbit";}    
    
    @Override
    public String toString(){
        return "{emptyOrbit[" + this.orbit + ";;]}";
    }

    @Override
    public int hashCode() {
        int hash = 13;
        hash = 31 * hash + this.orbit;
        hash = 31 * hash + Objects.hashCode(this.getName());
        return hash;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof EmptyOrbit){
            EmptyOrbit other = (EmptyOrbit) o;
            return this.orbit == other.getOrbit();
        }
        return false;
    }
}
