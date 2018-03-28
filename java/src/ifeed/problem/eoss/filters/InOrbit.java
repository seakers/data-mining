/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.eoss.filters;

import java.util.BitSet;
import java.util.HashSet;
import java.util.StringJoiner;

import ifeed.architecture.AbstractArchitecture;
import ifeed.architecture.BinaryInputArchitecture;
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
        return this.apply(((BinaryInputArchitecture) a).getInputs());
    }


    @Override
    public boolean apply(BitSet input){
        boolean out = true;
        for(int instr:this.instruments){
            if(!input.get(orbit*EOSSParams.num_instruments+instr)){
                // If any one of the instruments are not present
                out=false; 
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
    public boolean equals(Object o){
        if(o instanceof InOrbit){
            InOrbit other = (InOrbit) o;
            return this.orbit == other.getOrbit() && this.instruments.equals(other.getInstruments());
        }
        return false;
    }
    
}