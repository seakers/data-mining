/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed_dm;

import java.util.BitSet;
import java.util.List;

/**
 * A compound feature that combines two or more features with a conjunction. These features are immutable objects.
 * @author nozomihitomi
 */
public class CompoundFeature extends BinaryInputFeature {
    
    public CompoundFeature(List<BinaryInputFeature> features) {
        super(combinedMatches(features), Double.NaN, Double.NaN, Double.NaN, Double.NaN);
    }
    
    public CompoundFeature(List<BinaryInputFeature> features, double support, double lift, double fconfidence, double rconfidence) {
        super(combinedMatches(features), support, lift, fconfidence, rconfidence);
    }
    
    private static BitSet combinedMatches(List<BinaryInputFeature> features){
        
        BitSet matches = (BitSet) features.get(0).getMatches().clone();
        
        for(int i=1;i<features.size();i++){
            matches.and(features.get(i).getMatches());
        }
        
        return matches;
    }

    
}
