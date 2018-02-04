/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed_dm.binaryInput;

import java.util.BitSet;

/**
 *
 * @author bang
 */
public interface BinaryInputFilter{
        
    public boolean apply(BitSet input);
    
    public String getName();
    
    @Override
    public String toString();

}
