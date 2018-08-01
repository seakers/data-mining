/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.partitioningAndAssigning.filters;

import java.util.HashSet;
import java.util.Objects;
import java.util.StringJoiner;

import ifeed.architecture.AbstractArchitecture;
import ifeed.architecture.DiscreteInputArchitecture;
import ifeed.filter.AbstractFilter;
import ifeed.problem.partitioningAndAssigning.Params;
/**
 *
 * @author bang
 */
public class NotInOrbit extends AbstractFilter {

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
        return this.apply(((DiscreteInputArchitecture) a).getInputs());
    }

    @Override
    public boolean apply(int[] input){
        boolean out = true;

        int satIndex = -1;
        for(int i = 0; i < Params.num_instruments; i++){
            if(input[i + Params.num_instruments] == this.orbit){
                satIndex = i;
                break;
            }
        }

        for(int instr:this.instruments){
            if(input[instr] == satIndex){
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
