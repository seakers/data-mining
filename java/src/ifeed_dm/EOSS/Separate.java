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
 * @author bang
 */
public class Separate implements BinaryInputFilter {
    
    private int[] instruments;
    
    public Separate(int[] instruments){
        this.instruments = instruments;
    }
    
    public boolean apply(BitSet input){
        boolean out = true;
        for(int o=0;o<EOSSParams.num_orbits;o++){
            boolean sat = true;
            boolean found = false;
            for(int i:instruments){
                if(input.get(o*EOSSParams.num_instruments+i)){
                    if(found){
                        sat=false;
                        break;
                    }else{
                        found=true;
                    }
                }
            }
            if(!sat){
                out=false;
                break;
            }
        }
        return out;
    }
}
