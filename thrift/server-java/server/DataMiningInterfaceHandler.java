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



import java.util.*;

import ifeed_dm.*;
import ifeed_dm.EOSS.EOSSFilterExpressionHandler;
import ifeed_dm.GNC.GNCFilterExpressionHandler;
import ifeed_dm.logic.Literal;
import ifeed_dm.logic.Connective;
import javaInterface.DataMiningInterface;
import javaInterface.Feature;

import ifeed_dm.EOSS.EOSSDataMining;
import ifeed_dm.EOSS.AutomatedEOSSLocalSearch;
import ifeed_dm.GNC.GNCDataMining;
import ifeed_dm.GNC.AutomatedGNCLocalSearch;

public class DataMiningInterfaceHandler implements DataMiningInterface.Iface {
    
    @Override
    public void ping() {
      System.out.println("ping()");
    }
    
    
    
    public List<ifeed_dm.binaryInput.BinaryInputArchitecture> formatArchitectureInputBinary(List<javaInterface.BinaryInputArchitecture> thrift_input_architecture){
            
        List<ifeed_dm.binaryInput.BinaryInputArchitecture> archs = new ArrayList<>();

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

            archs.add(new ifeed_dm.binaryInput.BinaryInputArchitecture(id, inputs, outputs));
        }

        return archs;
    }

    public List<ifeed_dm.discreteInput.DiscreteInputArchitecture> formatArchitectureInputDiscrete(List<javaInterface.DiscreteInputArchitecture> thrift_input_architecture){

        List<ifeed_dm.discreteInput.DiscreteInputArchitecture> archs = new ArrayList<>();

        for(int i = 0; i < thrift_input_architecture.size(); i++){

            javaInterface.DiscreteInputArchitecture input_arch = thrift_input_architecture.get(i);

            int id = input_arch.getId();
            List<Integer> _inputs = input_arch.getInputs();
            int[] inputs = new int[_inputs.size()];
            List<Double> _outputs = input_arch.getOutputs();
            double[] outputs = new double[_outputs.size()];

            for(int j = 0; j < _inputs.size(); j++){
                inputs[j] = _inputs.get(j);
            }
            for(int j = 0; j < _outputs.size(); j++){
                outputs[j] = _outputs.get(j);
            }
            archs.add(new ifeed_dm.discreteInput.DiscreteInputArchitecture(id, inputs, outputs));
        }

        return archs;
    }
    
    public List<Feature> formatFeatureOutput(List<ifeed_dm.Feature> data_mining_output_features){
        
        List<Feature> out = new ArrayList<>();
        
        // Transform ifeed_dm.DrivingFeature into javaInterface.DrivingFeature
        for(int i=0;i<data_mining_output_features.size();i++){
            
            ifeed_dm.Feature f = data_mining_output_features.get(i);
            
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
    public List<Feature> getDrivingFeaturesBinary(String problem, java.util.List<Integer> behavioral, java.util.List<Integer> non_behavioral,
            java.util.List<javaInterface.BinaryInputArchitecture> all_archs, double supp, double conf, double lift){

        
        List<Feature> outputDrivingFeatures = new ArrayList<>();

        List<ifeed_dm.Feature> extracted_features;
        
        try{

            List<ifeed_dm.binaryInput.BinaryInputArchitecture> archs = formatArchitectureInputBinary(all_archs);
            // Initialize DrivingFeaturesGenerator
            EOSSDataMining data_mining = new EOSSDataMining(behavioral,non_behavioral,archs,supp,conf,lift);
            // Run data mining
            extracted_features = data_mining.run();

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
    public List<Feature> getMarginalDrivingFeaturesConjunctiveBinary(String problem, java.util.List<Integer> behavioral, java.util.List<Integer> non_behavioral,
            java.util.List<javaInterface.BinaryInputArchitecture> all_archs, String current_feature, java.util.List<Integer> archs_with_feature, double supp, double conf, double lift){
    
        // Feature: {id, name, expression, metrics}
        List<Feature> outputDrivingFeatures = new ArrayList<>();
        
        try{
            
            List<ifeed_dm.binaryInput.BinaryInputArchitecture> archs = formatArchitectureInputBinary(all_archs);
            
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
    public List<Feature> runAutomatedLocalSearchBinary(String problem, java.util.List<Integer> behavioral, java.util.List<Integer> non_behavioral,
            java.util.List<javaInterface.BinaryInputArchitecture> all_archs, double supp, double conf, double lift){
        
        List<Feature> outputDrivingFeatures = new ArrayList<>();
        
        //Set<Integer> restrictedInstrumentSet = new HashSet<>(Arrays.asList(0,1,2,3,4,5));
        //Set<Integer> restrictedInstrumentSet = new HashSet<>();
        
        try{
            List<ifeed_dm.binaryInput.BinaryInputArchitecture> archs = formatArchitectureInputBinary(all_archs);
            
            // Initialize DrivingFeaturesGenerator
            AutomatedEOSSLocalSearch automatedSearch = new AutomatedEOSSLocalSearch(behavioral, non_behavioral, archs, supp, conf, lift);

            // Run data mining
            List<ifeed_dm.Feature> extracted_features = automatedSearch.run(5); // Args: maxIter, numInitialFeatureToAdd

            System.out.println("Automated run finished with num of features: " + extracted_features.size());

//            int num_of_features_to_return = 200;
//
//            List<ifeed_dm.Feature> _most_general_feature = new ArrayList<>();
//
//            // Get the most general features
//            if(extracted_features.size() > num_of_features_to_return){
//                _most_general_feature = Utils.getTopFeatures(extracted_features, num_of_features_to_return, FeatureMetric.DISTANCE2UP);
//            }

            outputDrivingFeatures = formatFeatureOutput(extracted_features);
            
        }catch(Exception TException){
            TException.printStackTrace();
        }
        
        return outputDrivingFeatures;
    }

    @Override
    public List<Feature> getMarginalDrivingFeaturesBinary(String problem, java.util.List<Integer> behavioral, java.util.List<Integer> non_behavioral,
            java.util.List<javaInterface.BinaryInputArchitecture> all_archs, String featureExpression, String logicalConnective, double supp, double conf, double lift){
    
        // Feature: {id, name, expression, metrics}
        List<Feature> outputDrivingFeatures = new ArrayList<>();
        
        try{

            List<ifeed_dm.binaryInput.BinaryInputArchitecture> archs = formatArchitectureInputBinary(all_archs);

            // Initialize DrivingFeaturesGenerator
            EOSSDataMining data_mining = new EOSSDataMining(behavioral,non_behavioral,archs,supp,conf,lift);

            List<ifeed_dm.Feature> baseFeatures = data_mining.generateBaseFeatures(false);

            System.out.println("...[EOSSDataMining] The number of candidate features: " + baseFeatures.size());

            EOSSFilterExpressionHandler filterExpressionHandler = new EOSSFilterExpressionHandler(baseFeatures);

            // Create a tree structure based on the given feature expression
            Connective root = filterExpressionHandler.generateFeatureTree(featureExpression);

            List<Connective> sameConnectives;
            List<Connective> oppositeConnectives;

            if(logicalConnective.equalsIgnoreCase("OR")){
                System.out.println("OR");
                sameConnectives = root.getDescendants(LogicOperator.OR);
                oppositeConnectives = root.getDescendants(LogicOperator.AND);
            }else{
                System.out.println("AND");
                sameConnectives = root.getDescendants(LogicOperator.AND);
                oppositeConnectives = root.getDescendants(LogicOperator.OR);
            }

            System.out.println("Num of same nodes found: " + sameConnectives.size());
            System.out.println("Num of opposite nodes found: " + oppositeConnectives.size());

            // Initialize the extracted features
            List<ifeed_dm.Feature> extracted_features = new ArrayList<>();

            for(Connective node: sameConnectives){
                node.setAddNewLiteral();
                node.precomputeMatches();
                List<ifeed_dm.Feature> tempFeatures = data_mining.runLocalSearch(root, baseFeatures);
                extracted_features.addAll(tempFeatures);
                node.cancelAddNode();
            }

            for(Connective node: oppositeConnectives){
                for(Literal feature: node.getLiteralChildren()){
                    node.setAddNewLiteral(feature);
                    node.precomputeMatches();
                    List<ifeed_dm.Feature> tempFeatures = data_mining.runLocalSearch(root, baseFeatures);
                    extracted_features.addAll(tempFeatures);
                    node.cancelAddNode();
                }
            }

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
    public List<Feature> getDrivingFeaturesDiscrete(String problem, java.util.List<Integer> behavioral, java.util.List<Integer> non_behavioral,
                                                  java.util.List<javaInterface.DiscreteInputArchitecture> all_archs, double supp, double conf, double lift){


        List<Feature> outputDrivingFeatures = new ArrayList<>();

        List<ifeed_dm.Feature> extracted_features;

        try{

            List<ifeed_dm.discreteInput.DiscreteInputArchitecture> archs = formatArchitectureInputDiscrete(all_archs);
            // Initialize DrivingFeaturesGenerator
            ifeed_dm.GNC.GNCDataMining data_mining = new ifeed_dm.GNC.GNCDataMining(behavioral,non_behavioral,archs,supp,conf,lift);
            // Run data mining
            extracted_features = data_mining.run();

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
    public List<Feature> getMarginalDrivingFeaturesConjunctiveDiscrete(String problem, java.util.List<Integer> behavioral, java.util.List<Integer> non_behavioral,
                                                                     java.util.List<javaInterface.DiscreteInputArchitecture> all_archs, String current_feature, java.util.List<Integer> archs_with_feature, double supp, double conf, double lift){

        // Feature: {id, name, expression, metrics}
        List<Feature> outputDrivingFeatures = new ArrayList<>();

        try{

            List<ifeed_dm.discreteInput.DiscreteInputArchitecture> archs = formatArchitectureInputDiscrete(all_archs);

            // Initialize DrivingFeaturesGenerator
            ifeed_dm.GNC.GNCDataMining data_mining = new ifeed_dm.GNC.GNCDataMining(behavioral,non_behavioral,archs,supp,conf,lift);

            List<ifeed_dm.Feature> extracted_features = data_mining.runLocalSearch(current_feature,archs_with_feature);

            outputDrivingFeatures = formatFeatureOutput(extracted_features);


        }catch(Exception TException){
            TException.printStackTrace();
        }

        return outputDrivingFeatures;
    }

    @Override
    public List<Feature> runAutomatedLocalSearchDiscrete(String problem, java.util.List<Integer> behavioral, java.util.List<Integer> non_behavioral,
                                                       java.util.List<javaInterface.DiscreteInputArchitecture> all_archs, double supp, double conf, double lift){

        List<Feature> outputDrivingFeatures = new ArrayList<>();

        //Set<Integer> restrictedInstrumentSet = new HashSet<>(Arrays.asList(0,1,2,3,4,5));
        //Set<Integer> restrictedInstrumentSet = new HashSet<>();

        try{
            List<ifeed_dm.discreteInput.DiscreteInputArchitecture> archs = formatArchitectureInputDiscrete(all_archs);

            // Initialize DrivingFeaturesGenerator
            AutomatedGNCLocalSearch automatedSearch = new AutomatedGNCLocalSearch(behavioral, non_behavioral, archs, supp, conf, lift);

            // Run data mining
            List<ifeed_dm.Feature> extracted_features = automatedSearch.run(7); // Args: maxIter, numInitialFeatureToAdd

            System.out.println("Automated run finished with num of features: " + extracted_features.size());

            outputDrivingFeatures = formatFeatureOutput(extracted_features);

        }catch(Exception TException){
            TException.printStackTrace();
        }

        return outputDrivingFeatures;
    }

    @Override
    public List<Feature> getMarginalDrivingFeaturesDiscrete(String problem, java.util.List<Integer> behavioral, java.util.List<Integer> non_behavioral,
                                                          java.util.List<javaInterface.DiscreteInputArchitecture> all_archs, String featureExpression, String logicalConnective, double supp, double conf, double lift){

        // Feature: {id, name, expression, metrics}
        List<Feature> outputDrivingFeatures = new ArrayList<>();

        try{

            List<ifeed_dm.discreteInput.DiscreteInputArchitecture> archs = formatArchitectureInputDiscrete(all_archs);

            // Initialize DrivingFeaturesGenerator
            GNCDataMining data_mining = new GNCDataMining(behavioral,non_behavioral,archs,supp,conf,lift);

            List<ifeed_dm.Feature> baseFeatures = data_mining.generateBaseFeatures(false);

            System.out.println("...[GNCDataMining] The number of candidate features: " + baseFeatures.size());

            GNCFilterExpressionHandler filterExpressionHandler = new GNCFilterExpressionHandler(archs.size(), baseFeatures);

            // Create a tree structure based on the given feature expression
            Connective root = filterExpressionHandler.generateFeatureTree(featureExpression);

            List<Connective> sameConnectives;
            List<Connective> oppositeConnectives;

            if(logicalConnective.equalsIgnoreCase("OR")){
                sameConnectives = root.getDescendants(LogicOperator.OR);
                oppositeConnectives = root.getDescendants(LogicOperator.AND);
            }else{
                sameConnectives = root.getDescendants(LogicOperator.AND);
                oppositeConnectives = root.getDescendants(LogicOperator.OR);
            }
            System.out.println("Number of " + logicalConnective + " nodes found: " + sameConnectives.size());
            System.out.println("Number of opposite nodes found: " + oppositeConnectives.size());

            // Initialize the extracted features
            List<ifeed_dm.Feature> extracted_features = new ArrayList<>();

            for(Connective node: sameConnectives){
                node.setAddNewLiteral();
                node.precomputeMatches();
                List<ifeed_dm.Feature> tempFeatures = data_mining.runLocalSearch(root, baseFeatures);
                extracted_features.addAll(tempFeatures);
                node.cancelAddNode();
            }

            for(Connective node: oppositeConnectives){
                for(Literal feature: node.getLiteralChildren()){
                    node.setAddNewLiteral(feature);
                    node.precomputeMatches();
                    List<ifeed_dm.Feature> tempFeatures = data_mining.runLocalSearch(root, baseFeatures);
                    extracted_features.addAll(tempFeatures);
                    node.cancelAddNode();
                }
            }

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


}