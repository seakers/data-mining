/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed_dm;

import java.util.BitSet;

/**
 *
 * @author bang
 */
public abstract class Filter {
        
    public boolean apply(BitSet input){
        throw new RuntimeException("Filter application not defined");
    };

    public boolean apply(int[] input){
        throw new RuntimeException("Filter application not defined");
    };

    public abstract String getName();
    
    @Override
    public abstract String toString();

}
