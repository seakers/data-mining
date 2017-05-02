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
public class AprioriFeature extends AbstractFeature{
    
        /**
         *
         * @param bitset of the base features that create this feature
         * @param support
         * @param lift
         * @param fconfidence
         * @param rconfidence
         */
        public AprioriFeature(BitSet bitset, double support, double lift, double fconfidence, double rconfidence) {
            super(bitset, support, lift, fconfidence, rconfidence);
        }

    
}
