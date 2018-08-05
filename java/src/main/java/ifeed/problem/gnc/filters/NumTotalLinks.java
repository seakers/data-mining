/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.gnc.filters;

import ifeed.architecture.AbstractArchitecture;
import ifeed.architecture.DiscreteInputArchitecture;
import ifeed.filter.AbstractFilter;
import ifeed.local.params.BaseParams;
import ifeed.problem.gnc.Params;

/**
 *
 * @author bang
 */
public class NumTotalLinks extends AbstractFilter {

    protected Params params;
    private final int n;
    
    public NumTotalLinks(BaseParams params, int n){
        super(params);
        this.params = (Params) params;
        this.n = n;
    }

    @Override
    public boolean apply(AbstractArchitecture a){
        return this.apply(((DiscreteInputArchitecture) a).getInputs());
    }

    @Override
    public boolean apply(int[] input){
        
        int cnt = 0;
        for(int i = params.getIbin_1_index(); i < params.getIbin_9_index() + 1; i++){
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
