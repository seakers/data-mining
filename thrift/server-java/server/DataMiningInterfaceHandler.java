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
import java.util.Collections;

import javaInterface.DataMiningInterface;
import javaInterface.Architecture;
import javaInterface.DrivingFeature;


import org.apache.thrift.TException;

import ifeed_dm.DrivingFeaturesGenerator;
import ifeed_dm.DrivingFeature;

public class DataMiningInterfaceHandler implements DataMiningInterface.Iface {
    
    @Override
    public void ping() {
      System.out.println("ping()");
    }
    
    @Override
    public ArrayList<DrivingFeature> getDrivingFeatures(java.util.List<Integer> behavioral, java.util.List<Integer> non_behavioral,
            java.util.List<Architecture> all_archs, double supp, double conf, double lift){
        
       
        ArrayList<DrivingFeature> outputDrivingFeatures = new ArrayList<>();
        
        try{
            
            
            // Initialize DrivingFeaturesGenerator
            DrivingFeaturesGenerator dfsGen = new DrivingFeaturesGenerator();
            
            ArrayList<DrivingFeaturesGenerator.Architecture> archs = new ArrayList<>();
            
            for(int i=0;i<all_archs.size();i++){
                Architecture input_arch = all_archs.get(i);
                int id = input_arch.getId();
                String bitString = input_arch.getBitString();
                double science = input_arch.getScience();
                double cost = input_arch.getCost();
                boolean label = false;
                if(behavioral.contains(id)){
                    label = true;
                }else{
                    label = false;
                }
                archs.add(dfsGen.new Architecture(id, label, bitString));
            }


            dfsGen.setInputData((ArrayList<Integer>)behavioral, (ArrayList<Integer>) non_behavioral,
                                                        archs,supp,conf,lift);


    //            String user_def_features_raw = request.getParameter("userDefFilters");
    //            user_def_features_raw = user_def_features_raw.substring(1,user_def_features_raw.length()-1);
    //            String [] user_def_features = user_def_features_raw.split("\",\"");
    //            
    //            for(int i=0;i<user_def_features.length;i++){
    //            	String user_def_feature = user_def_features[i];
    //            	if(user_def_feature.length()==0){continue;}
    //            	else{
    //            		if(user_def_feature.startsWith("\"")) user_def_feature = user_def_feature.substring(1);
    //            		if(user_def_feature.endsWith("\"")) user_def_feature = user_def_feature.substring(0,user_def_feature.length()-1);
    //        		}
    //            	System.out.println(user_def_feature);
    //            	dfsGen.addUserDefFeature(user_def_feature);
    //            }

           
            ArrayList<DrivingFeature> drivingFeatures = (ArrayList) dfsGen.run(500);

            System.out.println("Driving features mined: " + drivingFeatures.size());

            // Transform ifeed_dm.DrivingFeature into javaInterface.DrivingFeature

            
            
            int cnt = 0;
            for(ifeed_dm.DrivingFeature f:drivingFeatures){
                
                if(cnt>500){
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
                outputDrivingFeatures.add(new javaInterface.DrivingFeature(id,name,expression,metrics));
            }



            
        }catch(Exception TException){
            TException.printStackTrace();
        }
        
        

        
        return outputDrivingFeatures;
    }
    
    
    
    @Override
    public ArrayList<DrivingFeature> getMarginalDrivingFeatures(java.util.List<Integer> behavioral, java.util.List<Integer> non_behavioral,
            java.util.List<Architecture> all_archs, java.util.List<DrivingFeature> current_features, double supp, double conf, double lift){
    
        
        
        return new ArrayList<DrivingFeature>();
        
    }
    
    
    
    

}

