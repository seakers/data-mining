/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed_dm.EOSS;

import java.util.BitSet;
import ifeed_dm.EOSS.EOSSParams;
import ifeed_dm.BinaryInputFilter;

/**
 * 
 *
 * @author bang
 */
public class EmptyOrbit implements BinaryInputFilter {
    
    private final int orbit;
    
    public EmptyOrbit(int o){
        this.orbit = o;
    }
    
    @Override
    public boolean apply(BitSet input){
        
        boolean out = true; // empty
        for(int i=0;i<EOSSParams.num_instruments;i++){
            if(input.get(orbit*EOSSParams.num_instruments+i)){
                out=false; // instrument found inside the orbit
                break;
            }
        }
        return out;
    }
    
    @Override
    public String toString(){
        return "{emptyOrbit[" + this.orbit + ";;]}";
    }    
    
}
