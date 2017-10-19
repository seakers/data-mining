/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed_dm;

import java.util.BitSet;
import java.util.Collection;

/**
 *
 * @author bang
 */
public class PrimitiveFeature extends BinaryInputFeature{
    
    public PrimitiveFeature(BitSet matches, double support, double lift, double fconfidence, double rconfidence) {
        super(matches, support, lift, fconfidence, rconfidence);
    }
    
}
