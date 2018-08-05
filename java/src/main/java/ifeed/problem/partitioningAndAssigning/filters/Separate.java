/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.partitioningAndAssigning.filters;

import java.util.HashSet;
import java.util.Objects;
import java.util.StringJoiner;

import com.sun.xml.internal.rngom.parse.host.Base;
import ifeed.architecture.AbstractArchitecture;
import ifeed.architecture.DiscreteInputArchitecture;
import ifeed.filter.AbstractFilter;
import ifeed.local.params.BaseParams;
import ifeed.problem.partitioningAndAssigning.Params;

/**
 *
 * @author bang
 */
public class Separate extends AbstractFilter {

    protected Params params;
    protected HashSet<Integer> instruments;
    
    public Separate(BaseParams params, int[] instruments){
        super(params);
        this.params = (Params) params;
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
        return this.apply(((DiscreteInputArchitecture) a).getInputs());
    }

    @Override
    public boolean apply(int[] input){

        boolean out = true;
        int satIndex = -1;
        for(int instr:this.instruments){
            if(satIndex == -1) {
                satIndex = input[instr];
            }else{
                if(satIndex == input[instr]){
                    out = false;
                    break;
                }
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
    public int hashCode() {
        int hash = 11;
        hash = 79 * hash + Objects.hashCode(this.instruments);
        hash = 79 * hash + Objects.hashCode(this.getName());
        return hash;
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
