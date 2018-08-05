/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.gnc;

import ifeed.local.params.BaseParams;
import ifeed.problem.gnc.filters.*;

import java.util.ArrayList;
import java.util.List;
import ifeed.filter.AbstractFilter;

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
        // numSensors, numComputers, numLinks, minNSNC
        for(int n = 1; n < 4; n++){
            candidate_features.add(new NumSensors(params, n));
            candidate_features.add(new NumComputers(params, n));
            candidate_features.add(new MinNSNC(params, n));
        }

        for(int n = 1; n < 10 ; n++){
            candidate_features.add(new NumTotalLinks(params, n));
        }

        // numSensorOfType, numSensorOfType
        for(int i = 1; i < 4; i++){
            for(int n = 0; n < 4; n++){
                candidate_features.add(new NumSensorOfType(params, i, n));
                candidate_features.add(new NumComputerOfType(params, i, n));
            }
            for(int n = 1; n < 4; n++){
                candidate_features.add(new SensorWithSpecificNumLinks(params, i, n));
                candidate_features.add(new ComputerWithSpecificNumLinks(params, i, n));
            }
        }

        return candidate_features;
    }  
}
