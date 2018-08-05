/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.filter;

import java.util.BitSet;
import ifeed.architecture.AbstractArchitecture;
import ifeed.local.params.BaseParams;

/**
 *
 * @author bang
 */
public abstract class AbstractFilter {

    protected BaseParams params;

    public AbstractFilter(BaseParams params){
        this.params = params;
    }
        
    public boolean apply(BitSet input){
        throw new UnsupportedOperationException("AbstractFilter not defined");
    }

    public boolean apply(int[] input){
        throw new UnsupportedOperationException("AbstractFilter not defined");
    }

    public boolean apply(AbstractArchitecture a){
        throw new UnsupportedOperationException("AbstractFilter not defined");
    }

    public abstract String getName();
    
    @Override
    public abstract String toString();

}
