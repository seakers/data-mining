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
    
    
    
    
    
    
    @Override
    public ArrayList<Feature> getDrivingFeatures(java.util.List<Integer> behavioral, java.util.List<Integer> non_behavioral,
            java.util.List<Architecture> all_archs, double supp, double conf, double lift){
        
       
        ArrayList<Feature> outputDrivingFeatures = new ArrayList<>();
        
        try{
            
            ArrayList<BinaryInputArchitecture> archs = new ArrayList<>();
            
            for(int i=0;i<all_archs.size();i++){
                
                Architecture input_arch = all_archs.get(i);
                
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
            
            // Initialize DrivingFeaturesGenerator
            EOSSDataMining data_mining = new EOSSDataMining(behavioral,non_behavioral,archs,supp,conf,lift);
            
            List<BinaryInputFeature> extracted_features = data_mining.run();


            System.out.println("Driving features mined: " + extracted_features.size());

            // Transform ifeed_dm.DrivingFeature into javaInterface.DrivingFeature
            
            int cnt = 0;
            for(BinaryInputFeature f:extracted_features){
                
                if(cnt>800){
                    break;
                }              
                
                int id = cnt++;
                String name  = f.getName();
                String expression = f.getName();
                ArrayList<Double> metrics = new ArrayList<>();
                metrics.add(f.getSupport());
                metrics.add(f.getLift());
                metrics.add(f.getFConfidence());
                metrics.add(f.getRConfidence());
                outputDrivingFeatures.add(new javaInterface.Feature(id,name,expression,metrics));
            }

            
        }catch(Exception TException){
            TException.printStackTrace();
        }
        
        

        return outputDrivingFeatures;
    }
    
    
    
    @Override
    public ArrayList<Feature> getMarginalDrivingFeatures(java.util.List<Integer> behavioral, java.util.List<Integer> non_behavioral,
            java.util.List<Architecture> all_archs, String current_feature, java.util.List<Integer> archs_with_feature, double supp, double conf, double lift){
    
       // Feature: {id, name, expression, metrics}
        ArrayList<Feature> outputDrivingFeatures = new ArrayList<>();
        
        try{
            
            ArrayList<BinaryInputArchitecture> archs = new ArrayList<>();
            
            for(int i=0;i<all_archs.size();i++){
                
                Architecture input_arch = all_archs.get(i);
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
            
            // Initialize DrivingFeaturesGenerator
            EOSSDataMining data_mining = new EOSSDataMining(behavioral,non_behavioral,archs,supp,conf,lift);
            
            List<BinaryInputFeature> extracted_features = data_mining.run_local_search(current_feature,archs_with_feature);

            System.out.println("Driving features mined: " + extracted_features.size());

            // Transform ifeed_dm.DrivingFeature into javaInterface.DrivingFeature
            
            int cnt = 0;
            for(BinaryInputFeature f:extracted_features){
                
                if(cnt>800){
                    break;
                }   
                
                int id = cnt++;
                String name  = f.getName();
                String expression = f.getName();
                ArrayList<Double> metrics = new ArrayList<>();
                metrics.add(f.getSupport());
                metrics.add(f.getLift());
                metrics.add(f.getFConfidence());
                metrics.add(f.getRConfidence());
                outputDrivingFeatures.add(new javaInterface.Feature(id,name,expression,metrics));
                
            }

            
        }catch(Exception TException){
            TException.printStackTrace();
        }
        
        return outputDrivingFeatures;
        
    }
    
    
    
    

}