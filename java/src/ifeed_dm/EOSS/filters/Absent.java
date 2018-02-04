/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed_dm.EOSS.filters;

import java.util.BitSet;

import ifeed_dm.binaryInput.BinaryInputFilter;
import ifeed_dm.EOSS.EOSSParams;

/**
 *
 * @author bang
 */
public class Absent implements BinaryInputFilter {
    
    private final int instrument;
    
    public Absent(int i){
        this.instrument = i;
    }
    
    @Override
    public boolean apply(BitSet input){
        
        boolean out = true;
        for(int o=0;o<EOSSParams.num_orbits;o++){
            if(input.get(o*EOSSParams.num_instruments + instrument)){
                // If any one of the instruments are not present
                out=false; 
                break;
            }
        }
        return out;
    }
    
    @Override
    public String getName(){return "absent";}
    
    @Override
    public String toString(){
        return "{absent[;" + this.instrument + ";]}";
    }
}
