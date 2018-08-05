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

import ifeed.architecture.AbstractArchitecture;
import ifeed.architecture.BinaryInputArchitecture;
import ifeed.filter.AbstractFilter;
import ifeed.local.params.BaseParams;
import ifeed.problem.assigning.Params;
/**
 *
 * @author bang
 */
public class Together extends AbstractFilter {

    protected Params params;
    protected HashSet<Integer> instruments;
    
    public Together(BaseParams params, int[] instruments){
        super(params);
        this.params = (Params) params;
        this.instruments = new HashSet<>();
        for(int i:instruments){
            this.instruments.add(i);
        }
    }

    public HashSet<Integer> getInstruments() {
        return instruments;
    }

    @Override
    public boolean apply(AbstractArchitecture a){
        return this.apply(((BinaryInputArchitecture) a).getInputs());
    }

    @Override
    public boolean apply(BitSet input){
        boolean out = false;
        for(int o = 0; o < this.params.getNumOrbits(); o++){
            boolean sat = true;
            for(int i:instruments){
                if(!input.get(o * this.params.getNumInstruments() + i)){
                    // If any one of the instruments are not present
                    sat=false;
                    break;
                }
            }
            if(sat){
                out=true;
                break;
            }
        }
        return out;
    }
    
    @Override
    public String getName(){return "together";}
    
    @Override
    public String toString(){
        StringJoiner sj = new StringJoiner(",");
        for(int i:instruments){
            sj.add(Integer.toString(i));
        }        
        return "{together[;" + sj.toString() + ";]}";
    }

    @Override
    public int hashCode() {
        int hash = 11;
        hash = 43 * hash + Objects.hashCode(this.instruments);
        hash = 43 * hash + Objects.hashCode(this.getName());
        return hash;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof Together){
            Together other = (Together) o;
            return this.instruments.equals(other.getInstruments());
        }
        return false;
    }


}
