/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.assigning.filters;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Objects;
import java.util.StringJoiner;

import com.sun.xml.internal.rngom.parse.host.Base;
import ifeed.architecture.AbstractArchitecture;
import ifeed.architecture.BinaryInputArchitecture;
import ifeed.filter.AbstractFilter;
import ifeed.local.params.BaseParams;
import ifeed.problem.assigning.Params;

/**
 *
 * @author bang
 */
public class InOrbit extends AbstractFilter {

    protected Params params;
    protected int orbit;
    protected HashSet<Integer> instruments;
    
    public InOrbit(BaseParams params, int o, int instrument){
        super(params);
        this.params = (Params) params;
        this.orbit = o;
        this.instruments = new HashSet<>();
        this.instruments.add(instrument);
    }
    
    public InOrbit(BaseParams params, int o, int[] instruments){
        super(params);
        this.params = (Params) params;
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
            if(!input.get(orbit* this.params.getNumInstruments() +instr)){
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
