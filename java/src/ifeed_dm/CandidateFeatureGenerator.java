/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed_dm;

import java.util.List;
/**
 *
 * @author bang
 */
public interface CandidateFeatureGenerator {
    
    public List<BinaryInputFilter> generateCandidates();
    
}



