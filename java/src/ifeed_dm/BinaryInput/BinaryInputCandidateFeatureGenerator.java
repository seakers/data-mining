/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed_dm.binaryInput;

import ifeed_dm.Filter;

import java.util.List;
/**
 *
 * @author bang
 */
public interface BinaryInputCandidateFeatureGenerator {
    
    public List<Filter> generateCandidates();
    
}



