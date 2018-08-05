/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.gnc.filters;

import ifeed.architecture.AbstractArchitecture;
import ifeed.architecture.DiscreteInputArchitecture;
import ifeed.local.params.BaseParams;
import ifeed.problem.gnc.Params;
import ifeed.filter.AbstractFilter;

/**
 *
 * @author bang
 */
public class MinNSNC extends AbstractFilter {

    protected Params params;
    private final int n;
    
    public MinNSNC(BaseParams params, int n){
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
        
        boolean out;
        int min = Math.min(input[params.getNC_index()],input[params.getNS_index()]);
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
