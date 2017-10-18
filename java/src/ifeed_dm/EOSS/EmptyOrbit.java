/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed_dm.EOSS;

import java.util.BitSet;
import ifeed_dm.AbstractFeatureBinary;
import ifeed_dm.EOSS.EOSSParams;

/**
 * 
 *
 * @author bang
 */
public class EmptyOrbit implements AbstractFeatureBinary {
    
    private int orbit;
    
    public EmptyOrbit(int o){
        this.orbit = o;
    }
    
    public boolean apply(BitSet input){
        boolean out = true;
        for(int i=0;i<EOSSParams.num_instruments;i++){
            if(input.get(orbit*EOSSParams.num_instruments+i)){
                out=false;
                break;
            }
        }
        return out;
    }
}
