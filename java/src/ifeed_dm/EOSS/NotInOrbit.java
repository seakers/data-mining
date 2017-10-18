/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed_dm.EOSS;

import java.util.BitSet;
import java.util.List;
import ifeed_dm.AbstractFeatureBinary;
import ifeed_dm.EOSS.EOSSParams;
/**
 *
 * @author bang
 */
public class NotInOrbit implements AbstractFeatureBinary {
    
    private int orbit;
    private int[] instruments;
    
    public NotInOrbit(int o, int instrument){
        this.orbit = o;
        this.instruments = new int[1];
        instruments[0]=instrument;
    }    
   
    public NotInOrbit(int o, int[] instruments){
        this.orbit = o;
        this.instruments = instruments;
    }
    
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
}
