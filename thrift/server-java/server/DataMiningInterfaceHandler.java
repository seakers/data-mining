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
import javaInterface.Architecture;
import javaInterface.Feature;


import org.apache.thrift.TException;

import ifeed_dm.EOSS.EOSSDataMining;
import ifeed_dm.BinaryInputArchitecture;
import ifeed_dm.BinaryInputFeature;

public class DataMiningInterfaceHandler implements DataMiningInterface.Iface {
    
    @Override
    public void ping() {
      System.out.println("ping()");
    }
    
    
    
    public List<BinaryInputArchitecture> formatArchitectureInput(List<Architecture> thrift_input_architecture){
            
        ArrayList<BinaryInputArchitecture> archs = new ArrayList<>();

        for(int i=0;i<thrift_input_architecture.size();i++){

            Architecture input_arch = thrift_input_architecture.get(i);

            int id = input_arch.getId();
            String bitString = input_arch.getBitString();

            BitSet inputs = new BitSet(bitString.length());

            for(int j=0;j<bitString.length();j++){
                if(bitString.charAt(j)=='1'){
                    inputs.set(j);
                }
            }
            double science = input_arch.getScience();
            double cost = input_arch.getCost();
            double[] outputs = {science, cost};

            archs.add(new BinaryInputArchitecture(id, inputs, outputs));
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
            java.util.List<Architecture> all_archs, double supp, double conf, double lift){

        
        List<Feature> outputDrivingFeatures = new ArrayList<>();
        
        try{
            
            List<BinaryInputArchitecture> archs = formatArchitectureInput(all_archs);
            
            // Initialize DrivingFeaturesGenerator
            EOSSDataMining data_mining = new EOSSDataMining(behavioral,non_behavioral,archs,supp,conf,lift);
            
            // Run data mining
            List<ifeed_dm.Feature> extracted_features = data_mining.run();

            outputDrivingFeatures = formatFeatureOutput(extracted_features);
            
        }catch(Exception TException){
            TException.printStackTrace();
        }
        
        return outputDrivingFeatures;
    }
    
    
    
    @Override
    public List<Feature> getMarginalDrivingFeatures(java.util.List<Integer> behavioral, java.util.List<Integer> non_behavioral,
            java.util.List<Architecture> all_archs, String current_feature, java.util.List<Integer> archs_with_feature, double supp, double conf, double lift){
    
       // Feature: {id, name, expression, metrics}
        List<Feature> outputDrivingFeatures = new ArrayList<>();
        
        try{
            
            List<BinaryInputArchitecture> archs = formatArchitectureInput(all_archs);
            
            // Initialize DrivingFeaturesGenerator
            EOSSDataMining data_mining = new EOSSDataMining(behavioral,non_behavioral,archs,supp,conf,lift);
            
            List<ifeed_dm.Feature> extracted_features = data_mining.run_local_search(current_feature,archs_with_feature);

            outputDrivingFeatures = formatFeatureOutput(extracted_features);
            
            
        }catch(Exception TException){
            TException.printStackTrace();
        }
        
        return outputDrivingFeatures;
    }
    
    
}