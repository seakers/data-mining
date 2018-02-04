/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed_dm.discreteInput;

/**
 *
 * @author bang
 */
public interface DiscreteInputFilter{
        
    public boolean apply(int[] a);
    
    public String getName();
    
    @Override
    public String toString();

}
