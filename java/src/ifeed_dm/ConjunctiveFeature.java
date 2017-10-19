/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed_dm;

import java.util.BitSet;
import java.util.Collection;

/**
 * A compound feature that combines two or more features with a conjunction. These features are immutable objects.
 * @author nozomihitomi
 */
public class ConjunctiveFeature extends BinaryInputFeature {
    
    public ConjunctiveFeature(Collection<BinaryInputFeature> features) {
        super(combinedMatches(features), Double.NaN, Double.NaN, Double.NaN, Double.NaN);
    }
    
    public ConjunctiveFeature(Collection<BinaryInputFeature> features, double support, double lift, double fconfidence, double rconfidence) {
        super(combinedMatches(features), support, lift, fconfidence, rconfidence);
    }
    
    private static BitSet combinedMatches(Collection<BinaryInputFeature> features){
        BitSet template = features.iterator().next().getMatches();
        for(BinaryInputFeature f : features){
            template.and(f.getMatches());
        }
        return template;
    }

    
}
