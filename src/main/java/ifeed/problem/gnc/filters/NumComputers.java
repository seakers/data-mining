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
public class NumComputers extends AbstractFilter {

    protected Params params;
    private final int n;
    
    public NumComputers(BaseParams params, int n){
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
        if(input[params.getNC_index()]==n){
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
