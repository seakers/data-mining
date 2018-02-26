/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed_dm.GNC.filters;

import ifeed_dm.GNC.GNCParams;
import ifeed_dm.Filter;

/**
 *
 * @author bang
 */
public class NumSensors extends Filter {
    
    private final int n;
    
    public NumSensors(int n){
        this.n = n;
    }
    
    @Override
    public boolean apply(int[] input){
        
        boolean out;
        if(input[GNCParams.NS_index]==n){
            out = true;
        }else{
            out = false;
        }
        return out;
    }
    
    @Override
    public String getName(){return "numSensors";}
    
    @Override
    public String toString(){
        return "{numSensors[" + this.n + "]}";
    }
}
