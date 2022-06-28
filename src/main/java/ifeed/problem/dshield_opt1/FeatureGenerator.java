/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.dshield_opt1;

import ifeed.filter.AbstractFilter;
import ifeed.local.params.BaseParams;
import ifeed.problem.dshield_opt1.filters.*;

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


        return candidate_features;
    }  
}
