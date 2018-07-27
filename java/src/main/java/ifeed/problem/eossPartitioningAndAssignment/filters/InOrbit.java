/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.eossPartitioningAndAssignment.filters;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Objects;
import java.util.StringJoiner;

import ifeed.architecture.AbstractArchitecture;
import ifeed.architecture.DiscreteInputArchitecture;
import ifeed.filter.Filter;
import ifeed.problem.eoss.EOSSParams;

/**
 *
 * @author bang
 */
public class InOrbit extends Filter {

    protected int orbit;
    protected HashSet<Integer> instruments;
    
    public InOrbit(int o, int instrument){
        this.orbit = o;
        this.instruments = new HashSet<>();
        this.instruments.add(instrument);
    }
    
    public InOrbit(int o, int[] instruments){
        this.orbit = o;
        this.instruments = new HashSet<>();
        for(int i:instruments){
            this.instruments.add(i);
        }
    }

    public int getOrbit(){
        return this.orbit;
    }

    public HashSet<Integer> getInstruments(){ return this.instruments; }

    @Override
    public boolean apply(AbstractArchitecture a){
        return this.apply(((DiscreteInputArchitecture) a).getInputs());
    }


    @Override
    public boolean apply(int[] input){
        boolean out = true;

        int satIndex = -1;
        for(int i = 0; i < EOSSParams.num_instruments; i++){
            if(input[i + EOSSParams.num_instruments] == this.orbit){
                satIndex = i;
                break;
            }
        }

        for(int instr:this.instruments){
            if(input[instr] != satIndex){
                out = false;
                break;
            }
        }
        return out;
    }
    
    @Override
    public String getName(){return "inOrbit";}    
    
    @Override
    public String toString(){
        StringJoiner sj = new StringJoiner(",");
        for(int instr:this.instruments){
            sj.add(Integer.toString(instr));
        }
        return "{inOrbit[" + orbit + ";" + sj.toString() + ";]}";
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = 31 * hash + this.orbit;
        hash = 31 * hash + Objects.hashCode(this.instruments);
        hash = 31 * hash + Objects.hashCode(this.getName());
        return hash;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof InOrbit){
            InOrbit other = (InOrbit) o;
            return this.orbit == other.getOrbit() && this.instruments.equals(other.getInstruments());
        }
        return false;
    }
    
}
