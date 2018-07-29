/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.partitioningAndAssignment.filters;

import java.util.Objects;

import ifeed.architecture.AbstractArchitecture;
import ifeed.architecture.DiscreteInputArchitecture;
import ifeed.problem.assignment.Params;
import ifeed.filter.AbstractFilter;

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
        return this.apply(((DiscreteInputArchitecture) a).getInputs());
    }

    @Override
    public boolean apply(int[] input){
        
        boolean out = true; // empty
        for(int i = 0; i < Params.num_instruments; i++){
            if(input[Params.num_instruments + i] == this.orbit){
                out = false;
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
