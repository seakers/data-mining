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
public class NumTotalLinks implements DiscreteInputFilter {
    
    private final int n;
    
    public NumTotalLinks(int n){
        this.n = n;
    }
    
    @Override
    public boolean apply(int[] input){
        
        int cnt = 0;
        for(int i = GNCParams.Ibin_1_index; i < GNCParams.Ibin_9_index + 1; i++){
            if(input[i] == 1 || input[i] == 49){
                cnt++;
            }
        }

        return cnt == this.n;
    }
    
    @Override
    public String getName(){return "numTotalLinks";}
    
    @Override
    public String toString(){
        return "{numTotalLinks[" + this.n + "]}";
    }
}
