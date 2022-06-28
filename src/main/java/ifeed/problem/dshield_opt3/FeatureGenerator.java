/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.dshield_opt3;

import ifeed.filter.AbstractFilter;
import ifeed.local.params.BaseParams;
import ifeed.problem.dshield_opt3.filters.*;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author bang
 */

public class FeatureGenerator {

    protected Params params;
    private final String[] input_list;

    public FeatureGenerator(BaseParams params) {
        this.params = (Params) params;
        this.input_list = this.params.getInputList();
    }

    public List<AbstractFilter> generateCandidates(){
        
        ArrayList<AbstractFilter> candidate_features = new ArrayList<>();
      
        // Types
        for(int n = 1; n < 6; n++){
            candidate_features.add(new NumSatellites(params, n));
        }
        for(int n = 1; n < 5; n++){
            candidate_features.add(new NumPlanes(params, n));
        }
        for(int n = 4; n < 9; n++){
            candidate_features.add(new Inclination(params, n*10));
        }
        for(int n = 5; n < 8; n++){
            candidate_features.add(new ChirpBW(params, n*10));
        }
        for(int n = 1; n < 10; n++){
            candidate_features.add(new PulseWidth(params, n*10));
        }
        for(int n = 14; n < 15; n++){
            candidate_features.add(new DAz(params, n));
        }
        for(int n = 14; n < 15; n++){
            candidate_features.add(new DEl(params, n));
        }


        return candidate_features;
    }  
}
