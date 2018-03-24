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
public class Separate extends Filter {

    protected HashSet<Integer> instruments;
    
    public Separate(int[] instruments){
        this.instruments = new HashSet<>();
        for(int inst:instruments){
            this.instruments.add(inst);
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
        boolean out = true;
        for(int o=0;o<EOSSParams.num_orbits;o++){
            boolean sep = true;
            boolean found = false;
            for(int i:instruments){
                if(input.get(o*EOSSParams.num_instruments+i)){
                    if(found){
                        sep=false;
                        break;
                    }else{
                        found=true;
                    }
                }
            }
            if(!sep){
                out=false;
                break;
            }
        }
        return out;
    }
    
    @Override
    public String getName(){return "separate";}
    
    @Override
    public String toString(){
        StringJoiner sj = new StringJoiner(",");
        for(int i:this.instruments){
            sj.add(Integer.toString(i));
        }        
        return "{separate[;" + sj.toString() + ";]}";
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof Separate){
            Separate other = (Separate) o;
            return this.instruments.equals(other.getInstruments());
        }
        return false;
    }

}
