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
public class NumSensors extends AbstractFilter {

    protected Params params;
    private final int n;
    
    public NumSensors(BaseParams params, int n){
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
        if(input[params.getNS_index()]==n){
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
