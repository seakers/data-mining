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

public class DataMiningInterfaceHandler implements DataMiningInterface.Iface {
    
    ArrayList<String> user_def_filters = new ArrayList<>();
    
    
    @Override
    public void ping() {
      System.out.println("ping()");
    }
    
    @Override
    public ArrayList<DrivingFeature> getDrivingFeatures(java.util.List<Integer> behavioral, java.util.List<Integer> non_behavioral,
            java.util.List<Architecture> all_archs, double supp, double conf, double lift){
        
        
        System.out.println("getDrivingFeatures");
        ArrayList<javaInterface.DrivingFeature> outputDrivingFeatures = new ArrayList<>();
        
        try{

            // Initialize DrivingFeaturesGenerator
            DrivingFeaturesGenerator dfsGen = new DrivingFeaturesGenerator();
            dfsGen.initialize((ArrayList<Integer>)behavioral, (ArrayList<Integer>) non_behavioral,
                                                        (ArrayList<Architecture>)all_archs,supp,conf,lift);


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


            ArrayList<ifeed_dm.DrivingFeature> DFs = new ArrayList<>();            
            DFs = dfsGen.getPrimitiveDrivingFeatures();
            Collections.sort(DFs,ifeed_dm.DrivingFeature.DrivingFeatureComparator);

            System.out.println("Driving features mined: " + DFs.size());

            // Transform ifeed_dm.DrivingFeature into javaInterface.DrivingFeature

            for(ifeed_dm.DrivingFeature f:DFs){
                int id = f.getID();
                String name  = f.getName();
                String expression = f.getExpression();
                double[] metricsArray = f.getMetrics();
                ArrayList<Double> metrics = new ArrayList<>();
                for(double m:metricsArray){
                    metrics.add(m);
                }
                outputDrivingFeatures.add(new javaInterface.DrivingFeature(id,name,expression,metrics));
            }



            
        }catch(Exception TException){
            TException.printStackTrace();
        }
        
        

        
        return outputDrivingFeatures;
    }

}

