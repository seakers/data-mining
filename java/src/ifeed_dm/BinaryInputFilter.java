/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed_dm;

import java.util.List;
import java.util.BitSet;

/**
 *
 * @author bang
 */
public interface BinaryInputFilter{
    
    public boolean apply(BitSet input);
    
    @Override
    public String toString();

}
