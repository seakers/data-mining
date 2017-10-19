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
public class Absent implements BinaryInputFilter {
    
    private int instrument;
    
    public Absent(int i){
        this.instrument = i;
    }
    
    public boolean apply(BitSet input){
        
        boolean out = false;
        if(!input.get(this.instrument)){
            out=true;
        }
        return out;
    }
}