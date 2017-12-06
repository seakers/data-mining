package server;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */



import java.util.ArrayList;
import java.util.List;
import java.util.BitSet;

import javaInterface.DataMiningInterface;
import javaInterface.BinaryInputArchitecture;
import javaInterface.Feature;


import org.apache.thrift.TException;

import ifeed_dm.EOSS.EOSSDataMining;
import ifeed_dm.BinaryInputFeature;
import ifeed_dm.EOSS.AutomatedEOSSLocalSearch;
import ifeed_dm.FeatureComparator;
import ifeed_dm.FeatureMetric;
import ifeed_dm.Utils;
import java.util.Arrays;
import java.util.Comparator;

public class DataMiningInterfaceHandler implements DataMiningInterface.Iface {
    
    @Override
    public void ping() {
      System.out.println("ping()");
    }
    
    
    
    public List<ifeed_dm.BinaryInputArchitecture> formatArchitectureInput(List<javaInterface.BinaryInputArchitecture> thrift_input_architecture){
            
        List<ifeed_dm.BinaryInputArchitecture> archs = new ArrayList<>();

        for(int i=0;i<thrift_input_architecture.size();i++){

            javaInterface.BinaryInputArchitecture input_arch = thrift_input_architecture.get(i);

            int id = input_arch.getId();
            List<Boolean> bitString = input_arch.getInputs();

            BitSet inputs = new BitSet(bitString.size());

            for(int j=0;j<bitString.size();j++){
                if(bitString.get(j)){
                    inputs.set(j);
                }
            }
            
            List<Double> _outputs = input_arch.getOutputs();
            double science = _outputs.get(0);
            double cost = _outputs.get(1);
            double[] outputs = {science, cost};

            archs.add(new ifeed_dm.BinaryInputArchitecture(id, inputs, outputs));
        }

        return archs;
    }
    
    
    public List<Feature> formatFeatureOutput(List<ifeed_dm.Feature> data_mining_output_features){
        
        List<Feature> out = new ArrayList<>();
        
        // Transform ifeed_dm.DrivingFeature into javaInterface.DrivingFeature
        for(int i=0;i<data_mining_output_features.size();i++){
            
            ifeed_dm.BinaryInputFeature f = (ifeed_dm.BinaryInputFeature) data_mining_output_features.get(i);
            
            if(i>800){
                break;
            }              

            String name  = f.getName();
            String expression = f.getName();
            ArrayList<Double> metrics = new ArrayList<>();
            metrics.add(f.getSupport());
            metrics.add(f.getLift());
            metrics.add(f.getFConfidence());
            metrics.add(f.getRConfidence());
            out.add(new javaInterface.Feature(i,name,expression,metrics));
        }
        return out;
    }

    
    @Override
    public List<Feature> getDrivingFeatures(java.util.List<Integer> behavioral, java.util.List<Integer> non_behavioral,
            java.util.List<javaInterface.BinaryInputArchitecture> all_archs, double supp, double conf, double lift){

        
        List<Feature> outputDrivingFeatures = new ArrayList<>();
        
        try{
            
            List<ifeed_dm.BinaryInputArchitecture> archs = formatArchitectureInput(all_archs);
            
            // Initialize DrivingFeaturesGenerator
            EOSSDataMining data_mining = new EOSSDataMining(behavioral,non_behavioral,archs,supp,conf,lift);
            // Run data mining
            List<ifeed_dm.Feature> extracted_features = data_mining.run();
            
            FeatureComparator comparator1 = new FeatureComparator(FeatureMetric.FCONFIDENCE);
            FeatureComparator comparator2 = new FeatureComparator(FeatureMetric.RCONFIDENCE);
            List<Comparator> comparators = new ArrayList<>(Arrays.asList(comparator1,comparator2));              

            extracted_features = Utils.getFeatureFuzzyParetoFront(extracted_features,comparators,3);
            
            outputDrivingFeatures = formatFeatureOutput(extracted_features);
            
        }catch(Exception TException){
            TException.printStackTrace();
        }
        
        return outputDrivingFeatures;
    }
    
    
    @Override
    public List<Feature> getMarginalDrivingFeaturesConjunctive(java.util.List<Integer> behavioral, java.util.List<Integer> non_behavioral,
            java.util.List<javaInterface.BinaryInputArchitecture> all_archs, String current_feature, java.util.List<Integer> archs_with_feature, double supp, double conf, double lift){
    
        // Feature: {id, name, expression, metrics}
        List<Feature> outputDrivingFeatures = new ArrayList<>();
        
        try{
            
            List<ifeed_dm.BinaryInputArchitecture> archs = formatArchitectureInput(all_archs);
            
            // Initialize DrivingFeaturesGenerator
            EOSSDataMining data_mining = new EOSSDataMining(behavioral,non_behavioral,archs,supp,conf,lift);
            
            List<ifeed_dm.Feature> extracted_features = data_mining.runLocalSearch(current_feature,archs_with_feature);

            outputDrivingFeatures = formatFeatureOutput(extracted_features);
            
            
        }catch(Exception TException){
            TException.printStackTrace();
        }
        
        return outputDrivingFeatures;
    }    
    
    @Override
    public List<Feature> runAutomatedLocalSearch(java.util.List<Integer> behavioral, java.util.List<Integer> non_behavioral,
            java.util.List<javaInterface.BinaryInputArchitecture> all_archs, double supp, double conf, double lift){
        
        List<Feature> outputDrivingFeatures = new ArrayList<>();
        
        try{
            List<ifeed_dm.BinaryInputArchitecture> archs = formatArchitectureInput(all_archs);
            
            // Initialize DrivingFeaturesGenerator
            AutomatedEOSSLocalSearch localSearch = new AutomatedEOSSLocalSearch(behavioral, non_behavioral, archs, supp, conf, lift);
            // Run data mining
            List<ifeed_dm.Feature> extracted_features = localSearch.run(3, 2); // Args: maxIter, numInitialFeatureToAdd            
            
            int num_of_features_to_return = 10;
            
            List<ifeed_dm.Feature> _most_general_feature = new ArrayList<>();
            
            if(extracted_features.size() > num_of_features_to_return){
                _most_general_feature = Utils.getTopFeatures(extracted_features, num_of_features_to_return, FeatureMetric.RCONFIDENCE);
            }
            
            outputDrivingFeatures = formatFeatureOutput(_most_general_feature);
            
        }catch(Exception TException){
            TException.printStackTrace();
        }
        
        return outputDrivingFeatures;
    }
    
    
    
    @Override
    public List<Feature> getMarginalDrivingFeatures(java.util.List<Integer> behavioral, java.util.List<Integer> non_behavioral,
            java.util.List<javaInterface.BinaryInputArchitecture> all_archs, String featureExpression, double supp, double conf, double lift){
    
        // Feature: {id, name, expression, metrics}
        List<Feature> outputDrivingFeatures = new ArrayList<>();
        
        try{
            
            List<ifeed_dm.BinaryInputArchitecture> archs = formatArchitectureInput(all_archs);
            
            // Initialize DrivingFeaturesGenerator
            EOSSDataMining data_mining = new EOSSDataMining(behavioral,non_behavioral,archs,supp,conf,lift);
            
            List<ifeed_dm.Feature> extracted_features = data_mining.runLocalSearch(featureExpression);
            
            outputDrivingFeatures = formatFeatureOutput(extracted_features);
            
            
        }catch(Exception TException){
            TException.printStackTrace();
        }
        
        return outputDrivingFeatures;
    }
    
    
}