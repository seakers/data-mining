/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed_dm.GNC.filters;

import ifeed_dm.GNC.GNCParams;
import ifeed_dm.discreteInput.DiscreteInputFilter;

/**
 *
 * @author bang
 */
public class NumComputers implements DiscreteInputFilter {
    
    private final int n;
    
    public NumComputers(int n){
        this.n = n;
    }
    
    @Override
    public boolean apply(int[] input){
        
        boolean out;
        if(input[GNCParams.NC_index]==n){
            out = true;
        }else{
            out = false;
        }
        return out;
    }

    @Override
    public String getName(){return "numComputers";}
    
    @Override
    public String toString(){
        return "{numComputers[" + this.n + "]}";
    }
}
