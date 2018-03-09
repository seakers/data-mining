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
public class Together extends Filter {

    protected final int[] instruments;
    
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
    
    @Override
    public String getName(){return "together";}
    
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<this.instruments.length;i++){
            if(i!=0) sb.append(",");
            sb.append(instruments[i]);
        }        
        return "{together[;" + sb.toString() + ";]}";
    }    

    
}
