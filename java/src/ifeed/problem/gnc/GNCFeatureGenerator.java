/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.gnc;

import ifeed.problem.gnc.filters.*;

import java.util.ArrayList;
import java.util.List;
import ifeed.filter.Filter;

/**
 *
 * @author bang
 */

public class GNCFeatureGenerator {
    
    private final String[] input_list;

    public GNCFeatureGenerator() {
        this.input_list = GNCParams.input_list;
    }

    public List<Filter> generateCandidates(){
        
        ArrayList<Filter> candidate_features = new ArrayList<>();
      
        // Types
        // numSensors, numComputers, numLinks, minNSNC
        for(int n = 1; n < 4; n++){
            candidate_features.add(new NumSensors(n));
            candidate_features.add(new NumComputers(n));
            candidate_features.add(new MinNSNC(n));
        }

        for(int n = 1; n < 10 ; n++){
            candidate_features.add(new NumTotalLinks(n));
        }

        // numSensorOfType, numSensorOfType
        for(int i = 1; i < 4; i++){
            for(int n = 0; n < 4; n++){
                candidate_features.add(new NumSensorOfType(i, n));
                candidate_features.add(new NumComputerOfType(i, n));
            }
            for(int n = 1; n < 4; n++){
                candidate_features.add(new SensorWithSpecificNumLinks(i, n));
                candidate_features.add(new ComputerWithSpecificNumLinks(i, n));
            }
        }

        return candidate_features;
    }  
}
