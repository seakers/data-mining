/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed_dm.EOSS.filters;

import java.util.BitSet;
import ifeed_dm.BinaryInput.BinaryInputFilter;
import ifeed_dm.EOSS.EOSSParams;
/**
 *
 * @author bang
 */
public class InOrbit implements BinaryInputFilter {
    
    private final int orbit;
    private final int[] instruments;
    
    public InOrbit(int o, int instrument){
        this.orbit = o;
        this.instruments = new int[1];
        instruments[0]=instrument;
    }       
    
    public InOrbit(int o, int[] instruments){
        this.orbit = o;
        this.instruments = instruments;
    }
    
    @Override
    public boolean apply(BitSet input){
        boolean out = true;
        for(int instr:instruments){
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
        for(int i=0;i<this.instruments.length;i++){
            if(i!=0) sb.append(",");
            sb.append(instruments[i]);
        }        
        return "{inOrbit[" + orbit + ";" + sb.toString() + ";]}";
    } 
    
}
