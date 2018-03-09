/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed_dm.EOSS.filters;

import java.util.BitSet;
import ifeed_dm.Filter;
import ifeed_dm.EOSS.EOSSParams;

/**
 *
 * @author bang
 */
public class Present extends Filter {

    protected final int instrument;
    
    public Present(int i){
        this.instrument = i;
    }
    
    @Override
    public boolean apply(BitSet input){
        
        boolean out = false;
        for(int o=0;o<EOSSParams.num_orbits;o++){
            if(input.get(o*EOSSParams.num_instruments + instrument)){
                // If any one of the instruments are not present
                out=true; 
                break;
            }
        }
        return out;
    }
    
    @Override
    public String getName(){return "present";}

    
    @Override
    public String toString(){
        return "{present[;" + this.instrument + ";]}";
    }
    
}
