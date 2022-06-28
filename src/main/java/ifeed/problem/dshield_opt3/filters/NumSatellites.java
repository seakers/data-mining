/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.dshield_opt3.filters;

import ifeed.architecture.AbstractArchitecture;
import ifeed.architecture.ContinuousInputArchitecture;
import ifeed.filter.AbstractFilter;
import ifeed.local.params.BaseParams;
import ifeed.problem.dshield_opt3.Params;

/**
 *
 * @author bang
 */
public class NumSatellites extends AbstractFilter {

    protected Params params;
    private final int n;

    public NumSatellites(BaseParams params, int n){
        super(params);
        this.params = (Params) params;
        this.n = n;
    }

    @Override
    public boolean apply(AbstractArchitecture a){
        return this.apply(((ContinuousInputArchitecture) a).getInputs());
    }

    @Override
    public boolean apply(double[] input){
        
        boolean out;
        if(input[params.getNS_index()]==n){
            out = true;
        }else{
            out = false;
        }
        return out;
    }
    
    @Override
    public String getName(){return "numSatellites";}
    
    @Override
    public String toString(){
        return "{numSatellites[" + this.n + "]}";
    }
}
