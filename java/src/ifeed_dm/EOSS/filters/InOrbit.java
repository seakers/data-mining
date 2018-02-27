/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed_dm.EOSS.filters;

import java.util.BitSet;
import java.util.HashSet;

import ifeed_dm.Filter;
import ifeed_dm.EOSS.EOSSParams;

/**
 *
 * @author bang
 */
public class InOrbit extends Filter {
    
    private final int orbit;
    private HashSet<Integer> instruments;
    
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
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for(int instr:this.instruments){
            if(first){
                first = false;
            } else{
                sb.append(",");
            }
            sb.append(instr);
        }
        return "{inOrbit[" + orbit + ";" + sb.toString() + ";]}";
    } 
    
}
