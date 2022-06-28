/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.dshield.filters;

import ifeed.architecture.AbstractArchitecture;
import ifeed.architecture.ContinuousInputArchitecture;
import ifeed.filter.AbstractFilter;
import ifeed.local.params.BaseParams;
import ifeed.problem.dshield_opt2.Params;

/**
 *
 * @author bang
 */
public class ChirpBW extends AbstractFilter {

    protected Params params;
    private final double n;

    public ChirpBW(BaseParams params, double n){
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
        if(input[5]>n){
            out = true;
        }else{
            out = false;
        }
        return out;
    }

    @Override
    public String getName(){return "ChirpBW";}

    @Override
    public String toString(){
        return "{ChirpBW[" + this.n + "]}";
    }
}
