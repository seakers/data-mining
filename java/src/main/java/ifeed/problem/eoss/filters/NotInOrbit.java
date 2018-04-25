/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.eoss.filters;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Objects;
import java.util.StringJoiner;

import ifeed.architecture.AbstractArchitecture;
import ifeed.architecture.BinaryInputArchitecture;
import ifeed.filter.Filter;
import ifeed.problem.eoss.EOSSParams;
/**
 *
 * @author bang
 */
public class NotInOrbit extends Filter {

    protected int orbit;
    protected HashSet<Integer> instruments;
    
    public NotInOrbit(int o, int instrument){
        this.orbit = o;
        this.instruments = new HashSet<>();
        instruments.add(instrument);
    }    
   
    public NotInOrbit(int o, int[] instruments){
        this.orbit = o;
        this.instruments = new HashSet<>();
        for(int i:instruments){
            this.instruments.add(i);
        }
    }

    public int getOrbit(){ return this.orbit; }
    public HashSet<Integer> getInstruments(){ return this.instruments; }

    @Override
    public boolean apply(AbstractArchitecture a){
        return this.apply(((BinaryInputArchitecture) a).getInputs());
    }

    @Override
    public boolean apply(BitSet input){
        boolean out = true;
        for(int instr:instruments){
            if(input.get(orbit*EOSSParams.num_instruments+instr)){
                // If any one of the instruments is present, return false
                out = false;
                break;
            }
        }
        return out;
    }
    
    @Override
    public String getName(){return "notInOrbit";}    
    
    @Override
    public String toString(){
        StringJoiner sj = new StringJoiner(",");
        for(int instr:this.instruments){
            sj.add(Integer.toString(instr));
        }
        return "{notInOrbit[" + orbit + ";" + sj.toString() + ";]}";
    }

    @Override
    public int hashCode() {
        int hash = 13;
        hash = 23 * hash + this.orbit;
        hash = 23 * hash + Objects.hashCode(this.instruments);
        hash = 23 * hash + Objects.hashCode(this.getName());
        return hash;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof NotInOrbit){
            NotInOrbit other = (NotInOrbit) o;
            return this.orbit == other.getOrbit() && this.instruments.equals(other.getInstruments());
        }
        return false;
    }
    
}
