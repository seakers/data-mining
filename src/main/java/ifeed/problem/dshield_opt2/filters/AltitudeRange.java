/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.dshield_opt2.filters;

import ifeed.architecture.AbstractArchitecture;
import ifeed.architecture.ContinuousInputArchitecture;
import ifeed.filter.AbstractFilter;
import ifeed.local.params.BaseParams;
import ifeed.problem.dshield_opt2.Params;

/**
 *
 * @author bang
 */
public class AltitudeRange extends AbstractFilter {

    protected Params params;
    private final double n;

    public AltitudeRange(BaseParams params, double n){
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
        if(input[1]>n && input[1] < n+100.0){
            out = true;
        }else{
            out = false;
        }
        return out;
    }
    
    @Override
    public String getName(){return "Altitude";}
    
    @Override
    public String toString(){
        return "{Altitude[" + this.n + "]}";
    }
}
