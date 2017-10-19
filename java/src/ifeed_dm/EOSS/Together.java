/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed_dm.EOSS;

import java.util.BitSet;
import ifeed_dm.BinaryInputFilter;
/**
 *
 * @author bang
 */
public class Together implements BinaryInputFilter {
    
    private final int[] instruments;
    
    public Together(int[] instruments){
        this.instruments = instruments;
    }
    
    @Override
    public boolean apply(BitSet input){
        boolean out = false;
        for(int o=0;o<EOSSParams.num_orbits;o++){
            boolean sat = true;
            for(int i:instruments){
                if(!input.get(o*EOSSParams.num_instruments+i)){
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
}
