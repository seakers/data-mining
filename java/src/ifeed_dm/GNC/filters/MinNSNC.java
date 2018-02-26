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
public class MinNSNC extends Filter {
    
    private final int n;
    
    public MinNSNC(int n){
        this.n = n;
    }
    
    @Override
    public boolean apply(int[] input){
        
        boolean out;
        int min = Math.min(input[GNCParams.NC_index],input[GNCParams.NS_index]);
        if(min==n){
            out = true;
        }else{
            out = false;
        }
        return out;
    }
    
    @Override
    public String getName(){return "minNSNC";}
    
    @Override
    public String toString(){
        return "{minNSNC[" + this.n + "]}";
    }
}
