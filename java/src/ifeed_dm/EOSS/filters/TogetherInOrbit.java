/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed_dm.EOSS.filters;

import java.util.BitSet;
import java.util.HashSet;
import java.util.StringJoiner;

import ifeed_dm.Filter;
import ifeed_dm.EOSS.EOSSParams;

/**
 *
 * @author bang
 */
public class TogetherInOrbit extends Filter {

    protected final int orbit;
    protected HashSet<Integer> instruments;

    public TogetherInOrbit(int o, int[] instruments){
        this.orbit = o;
        this.instruments = new HashSet<>();
        for(int i:instruments){
            this.instruments.add(i);
        }
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
    public String getName(){return "togetherInOrbit";}

    @Override
    public String toString(){
        StringJoiner sj = new StringJoiner(",");
        for(int instr:this.instruments){
            sj.add(Integer.toString(instr));
        }
        return "{togetherInOrbit[" + orbit + ";" + sj.toString() + ";]}";
    }

}
