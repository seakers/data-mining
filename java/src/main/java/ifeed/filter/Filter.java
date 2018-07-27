/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.filter;

import java.util.BitSet;
import ifeed.architecture.AbstractArchitecture;

/**
 *
 * @author bang
 */
public abstract class Filter {
        
    public boolean apply(BitSet input){
        throw new UnsupportedOperationException("Filter not defined");
    }

    public boolean apply(int[] input){
        throw new UnsupportedOperationException("Filter not defined");
    }

    public boolean apply(AbstractArchitecture a){
        throw new UnsupportedOperationException("Filter not defined");
    }

    public abstract String getName();
    
    @Override
    public abstract String toString();

}
