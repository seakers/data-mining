/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed_dm.GNC;

import ifeed_dm.GNC.filters.NumSensors;
import ifeed_dm.GNC.filters.NumComputers;
import ifeed_dm.GNC.filters.NumLinks;
import ifeed_dm.GNC.filters.MinNSNC;

import java.util.ArrayList;
import java.util.List;
import ifeed_dm.DiscreteInput.DiscreteInputFilter;
import ifeed_dm.DiscreteInput.DiscreteInputCandidateFeatureGenerator;

/**
 *
 * @author bang
 */
public class GNCFeatureGenerator implements DiscreteInputCandidateFeatureGenerator{
    
    private final String[] input_list;
    
    public GNCFeatureGenerator(){
        this.input_list = GNCParams.input_list;
    }

    @Override
    public List<DiscreteInputFilter> generateCandidates(){
        
        ArrayList<DiscreteInputFilter> candidate_features = new ArrayList<>();
      
        // Types
        // numSensors, numComputers, numLinks, minNSNC
        for(int n = 1; n < 4; n++){
            candidate_features.add(new NumSensors(n));
            candidate_features.add(new NumComputers(n));
            candidate_features.add(new MinNSNC(n));
        }
        for(int n = 1; n < 10 ; n++){
            candidate_features.add(new NumLinks(n));
        }
        return candidate_features;
    }  
}
