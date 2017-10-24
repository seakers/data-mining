/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed_dm.EOSS;

import ifeed_dm.BinaryInputArchitecture;
import ifeed_dm.BinaryInputFeature;
import ifeed_dm.Feature;
import ifeed_dm.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author bang
 */
public class AutomatedEOSSLocalSearch {
    
    private static final Utils UTILS = new Utils();
    
    public AutomatedEOSSLocalSearch(){
    
        
    }
    
    
    public void run(List<Integer> behavioral, List<Integer> non_behavioral, List<BinaryInputArchitecture> archs, double supp, double conf, double lift){

        // Initialize DrivingFeaturesGenerator
        EOSSDataMining data_mining = new EOSSDataMining(behavioral,non_behavioral,archs,supp,conf,lift);

        // Run data mining
        List<Feature> extracted_features = data_mining.run();
        
        while(true){
            
            
            
        }
        
        

        extracted_features = data_mining.run_local_search(current_feature,archs_with_feature);

        outputDrivingFeatures = formatFeatureOutput(extracted_features);
        
    }
    
    
}
