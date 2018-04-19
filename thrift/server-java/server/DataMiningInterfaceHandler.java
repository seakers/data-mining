package server;

import java.util.*;

import ifeed.*;
import ifeed.architecture.AbstractArchitecture;
import ifeed.architecture.BinaryInputArchitecture;
import ifeed.architecture.DiscreteInputArchitecture;
import ifeed.feature.*;
import ifeed.feature.logic.ConnectiveTester;
import ifeed.feature.logic.Literal;
import ifeed.feature.logic.Connective;
import ifeed.feature.logic.LogicalConnectiveType;

import ifeed.problem.eoss.*;

import ifeed.problem.gnc.*;

import javaInterface.DataMiningInterface;
import javaInterface.Feature;


public class DataMiningInterfaceHandler implements DataMiningInterface.Iface {
    
    @Override
    public void ping() {
      System.out.println("ping()");
    }

    public List<AbstractArchitecture> formatArchitectureInputBinary(List<javaInterface.BinaryInputArchitecture> thrift_input_architecture){
            
        List<AbstractArchitecture> archs = new ArrayList<>();

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

            double[] outputs = new double[2];
            if(_outputs.size() > 0){
                double science = _outputs.get(0);
                double cost = _outputs.get(1);
                outputs[0] = science;
                outputs[1] = cost;
            }

            archs.add(new BinaryInputArchitecture(id, inputs, outputs));
        }

        return archs;
    }

    public List<AbstractArchitecture> formatArchitectureInputDiscrete(List<javaInterface.DiscreteInputArchitecture> thrift_input_architecture){

        List<AbstractArchitecture> archs = new ArrayList<>();

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
            archs.add(new DiscreteInputArchitecture(id, inputs, outputs));
        }

        return archs;
    }
    
    public List<Feature> formatFeatureOutput(List<ifeed.feature.Feature> data_mining_output_features){
        
        List<Feature> out = new ArrayList<>();
        
        // Transform ifeed.DrivingFeature into javaInterface.DrivingFeature
        for(int i=0;i<data_mining_output_features.size();i++){
            
            ifeed.feature.Feature f = data_mining_output_features.get(i);
            
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
            double complexity;
            if(f.getAlgebraicComplexity() == Double.NaN){
                complexity = -1.0;
            }else{
                complexity = f.getAlgebraicComplexity();
            }
            out.add(new javaInterface.Feature(i,name,expression,metrics, complexity));
        }
        return out;
    }

    @Override
    public List<Feature> getDrivingFeaturesBinary(String problem, java.util.List<Integer> behavioral, java.util.List<Integer> non_behavioral,
            java.util.List<javaInterface.BinaryInputArchitecture> all_archs, double supp, double conf, double lift){

        
        List<Feature> outputDrivingFeatures = new ArrayList<>();

        List<ifeed.feature.Feature> extracted_features;
        
        try{

            List<AbstractArchitecture> archs = formatArchitectureInputBinary(all_archs);
            // Initialize DrivingFeaturesGenerator
            EOSSAssociationRuleMining data_mining = new EOSSAssociationRuleMining(archs, behavioral,non_behavioral,supp,conf,lift);
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
    public List<Feature> runAutomatedLocalSearchBinary(String problem, java.util.List<Integer> behavioral, java.util.List<Integer> non_behavioral,
            java.util.List<javaInterface.BinaryInputArchitecture> all_archs, double supp, double conf, double lift){
        
        List<Feature> outputDrivingFeatures = new ArrayList<>();
        
        //Set<Integer> restrictedInstrumentSet = new HashSet<>(Arrays.asList(0,1,2,3,4,5));
        //Set<Integer> restrictedInstrumentSet = new HashSet<>();
        
        try{
            List<AbstractArchitecture> archs = formatArchitectureInputBinary(all_archs);
            
            // Initialize DrivingFeaturesGenerator
            EOSSAutomatedLocalSearch automatedSearch = new EOSSAutomatedLocalSearch(archs, behavioral, non_behavioral, 5, supp, conf, lift);

            // Run data mining
            List<ifeed.feature.Feature> extracted_features = automatedSearch.run(); // Args: maxIter, numInitialFeatureToAdd

            System.out.println("Automated run finished with num of features: " + extracted_features.size());

//            int num_of_features_to_return = 200;
//
//            List<ifeed.feature.Feature> _most_general_feature = new ArrayList<>();
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

            List<AbstractArchitecture> archs = formatArchitectureInputBinary(all_archs);

            // Initialize DrivingFeaturesGenerator
            EOSSLocalSearch data_mining = new EOSSLocalSearch(null, archs, behavioral,non_behavioral);
            List<ifeed.feature.Feature> baseFeatures = data_mining.generateBaseFeatures();

            System.out.println("...[EOSSAssociationRuleMining] The number of candidate features: " + baseFeatures.size());

            FeatureFetcher featureFetcher = new EOSSFeatureFetcher(baseFeatures, archs);
            FeatureExpressionHandler filterExpressionHandler = new FeatureExpressionHandler(featureFetcher);

            // Create a tree structure based on the given feature expression
            ConnectiveTester root = (ConnectiveTester) filterExpressionHandler.generateFeatureTree(featureExpression, true);
            data_mining.setRoot(root);

            List<Connective> sameConnectives;
            List<Connective> oppositeConnectives;

            if(logicalConnective.equalsIgnoreCase("OR")){
                System.out.println("OR");
                sameConnectives = root.getDescendantConnectives(LogicalConnectiveType.OR, true);
                oppositeConnectives = root.getDescendantConnectives(LogicalConnectiveType.AND, true);
            }else{
                System.out.println("AND");
                sameConnectives = root.getDescendantConnectives(LogicalConnectiveType.AND, true);
                oppositeConnectives = root.getDescendantConnectives(LogicalConnectiveType.OR, true);
            }

            System.out.println("Num of same nodes found: " + sameConnectives.size());
            System.out.println("Num of opposite nodes found: " + oppositeConnectives.size());

            // Initialize the extracted features
            List<ifeed.feature.Feature> extracted_features = new ArrayList<>();

            for(Connective node: sameConnectives){
                ConnectiveTester tester = (ConnectiveTester) node;
                tester.setAddNewLiteral();
                List<ifeed.feature.Feature> tempFeatures = data_mining.run(baseFeatures);
                extracted_features.addAll(tempFeatures);
                tester.cancelAddNode();
            }

            for(Connective node: oppositeConnectives){
                ConnectiveTester tester = (ConnectiveTester) node;
                for(int i = 0; i < tester.getLiteralChildren().size(); i++){
                    tester.setAddNewLiteral(i);
                    List<ifeed.feature.Feature> tempFeatures = data_mining.run(baseFeatures);
                    extracted_features.addAll(tempFeatures);
                    tester.cancelAddNode();
                }
            }

            System.out.println(extracted_features.size());
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

        List<ifeed.feature.Feature> extracted_features;

        try{

            List<AbstractArchitecture> archs = formatArchitectureInputDiscrete(all_archs);
            // Initialize DrivingFeaturesGenerator
            GNCAssociationRuleMining data_mining = new GNCAssociationRuleMining(archs, behavioral,non_behavioral,supp,conf,lift);
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
    public List<Feature> runAutomatedLocalSearchDiscrete(String problem, java.util.List<Integer> behavioral, java.util.List<Integer> non_behavioral,
                                                       java.util.List<javaInterface.DiscreteInputArchitecture> all_archs, double supp, double conf, double lift){

        List<Feature> outputDrivingFeatures = new ArrayList<>();

        //Set<Integer> restrictedInstrumentSet = new HashSet<>(Arrays.asList(0,1,2,3,4,5));
        //Set<Integer> restrictedInstrumentSet = new HashSet<>();

        try{
            List<AbstractArchitecture> archs = formatArchitectureInputDiscrete(all_archs);

            // Initialize DrivingFeaturesGenerator
            GNCAutomatedLocalSearch automatedSearch = new GNCAutomatedLocalSearch(archs, behavioral, non_behavioral, 7, supp, conf, lift);

            // Run data mining
            List<ifeed.feature.Feature> extracted_features = automatedSearch.run(); // Args: maxIter, numInitialFeatureToAdd

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

            List<AbstractArchitecture> archs = formatArchitectureInputDiscrete(all_archs);

            // Initialize DrivingFeaturesGenerator
            GNCLocalSearch data_mining = new GNCLocalSearch(null, archs, behavioral,non_behavioral);

            List<ifeed.feature.Feature> baseFeatures = data_mining.generateBaseFeatures();

            System.out.println("...[GNCAssociationRuleMining] The number of candidate features: " + baseFeatures.size());

            GNCFeatureFetcher featureFetcher = new GNCFeatureFetcher(baseFeatures, archs);
            FeatureExpressionHandler filterExpressionHandler = new FeatureExpressionHandler(featureFetcher);

            // Create a tree structure based on the given feature expression
            ConnectiveTester root = (ConnectiveTester) filterExpressionHandler.generateFeatureTree(featureExpression, true);
            data_mining.setRoot(root);

            List<Connective> sameConnectives;
            List<Connective> oppositeConnectives;

            if(logicalConnective.equalsIgnoreCase("OR")){
                sameConnectives = root.getDescendantConnectives(LogicalConnectiveType.OR, true);
                oppositeConnectives = root.getDescendantConnectives(LogicalConnectiveType.AND, true);
            }else{
                sameConnectives = root.getDescendantConnectives(LogicalConnectiveType.AND, true);
                oppositeConnectives = root.getDescendantConnectives(LogicalConnectiveType.OR, true);
            }
            System.out.println("Number of " + logicalConnective + " nodes found: " + sameConnectives.size());
            System.out.println("Number of opposite nodes found: " + oppositeConnectives.size());

            // Initialize the extracted features
            List<ifeed.feature.Feature> extracted_features = new ArrayList<>();

            for(Connective node: sameConnectives){
                ConnectiveTester tester = (ConnectiveTester) node;
                tester.setAddNewLiteral();
                tester.computeMatchesLiteral();
                List<ifeed.feature.Feature> tempFeatures = data_mining.run(baseFeatures);
                extracted_features.addAll(tempFeatures);
                tester.cancelAddNode();
            }

            for(Connective node: oppositeConnectives){
                ConnectiveTester tester = (ConnectiveTester) node;
                for(Literal feature: node.getLiteralChildren()){
                    tester.setAddNewLiteral(feature);
                    tester.computeMatchesLiteral();
                    List<ifeed.feature.Feature> tempFeatures = data_mining.run(baseFeatures);
                    extracted_features.addAll(tempFeatures);
                    tester.cancelAddNode();
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
    public String convertToCNF(String expression){

        String out = "";

        try{
            FeatureExpressionHandler expressionHandler = new FeatureExpressionHandler();
            expressionHandler.setSkipMatchCalculation(true);
            out = expressionHandler.convertToCNF(expression);

        }catch(Exception TException){
            TException.printStackTrace();
        }
        return out;
    }

    @Override
    public String convertToDNF(String expression){

        String out = "";

        try{
            FeatureExpressionHandler expressionHandler = new FeatureExpressionHandler();
            expressionHandler.setSkipMatchCalculation(true);
            out = expressionHandler.convertToDNF(expression);

        }catch(Exception TException){
            TException.printStackTrace();
        }
        return out;
    }

    @Override
    public double computeComplexity(String expression){
        double out = -1;

        try{
            FeatureExpressionHandler expressionHandler = new FeatureExpressionHandler();
            expressionHandler.setSkipMatchCalculation(true);
            // Create tree
            Connective root = expressionHandler.generateFeatureTree(expression);
            // Convert it to CNF
            Connective CNF = expressionHandler.convertToCNF(root);
            // Obtain the power spectrum
            HashMap<Integer,Integer> powerSpectrum = expressionHandler.getPowerSpectrum(CNF);
            // Compute the algebraic complexity
            out = expressionHandler.computeAlgebraicComplexity(powerSpectrum);

        }catch(Exception TException){
            TException.printStackTrace();
        }
        return out;
    }

    @Override
    public List<Double> computeComplexityOfFeatures(List<String> expressions){
        ArrayList<Double> out = new ArrayList<>();
        for(String exp:expressions){
            out.add(this.computeComplexity(exp));
        }
        return out;
    }

    @Override
    public List<Integer> computeAlgebraicTypicality(javaInterface.BinaryInputArchitecture arch, String feature){

        List<javaInterface.BinaryInputArchitecture> tempList = Arrays.asList(arch);

        BinaryInputArchitecture a = (BinaryInputArchitecture) formatArchitectureInputBinary(tempList).get(0);
        BitSet input = a.getInputs();

        FeatureFetcher featureFetcher = new EOSSFeatureFetcher(new ArrayList<>());
        TypicalityCalculator calculator = new TypicalityCalculator(input, feature, featureFetcher);

        int[] out = calculator.run();
        return new ArrayList<>(Arrays.asList(out[0], out[1]));
    }

    @Override
    public List<Integer> computeAlgebraicTypicalityWithStringInput(String architecture, String feature){

//        System.out.println("Input: " + architecture);
//        System.out.println("Feature: " + feature);

        String input = architecture;

        String[] inputSplit = input.split("/");
        BitSet inputs = new BitSet(60);

        int norb = 5;
        int ninstr = 12;

        String[] instrumentsArray = {"A","B","C","D","E","F","G","H","I","J","K","L"};
        ArrayList<String> instruments = new ArrayList<>(Arrays.asList(instrumentsArray));

        for(int o = 0; o < inputSplit.length; o++){
            String thisOrbit = inputSplit[o];
            for(int i = 0; i < thisOrbit.length(); i++){
                String thisInstr = thisOrbit.charAt(i) + "";
                int instrIndex = instruments.indexOf(thisInstr);
                inputs.set(o * ninstr + instrIndex);
            }
        }

        FeatureFetcher featureFetcher = new EOSSFeatureFetcher(new ArrayList<>());
        TypicalityCalculator calculator = new TypicalityCalculator(inputs, feature, featureFetcher);

        int[] out = calculator.run();

        int diff = out[1] - out[0];

        System.out.println("Typicality diff: " + diff);

        return new ArrayList<>(Arrays.asList(out[0], out[1]));
    }

    @Override
    public List<Feature> getDrivingFeaturesEpsilonMOEABinary(String problem, java.util.List<Integer> behavioral, java.util.List<Integer> non_behavioral,
                                                  java.util.List<javaInterface.BinaryInputArchitecture> all_archs){

        List<Feature> outputDrivingFeatures = new ArrayList<>();
        List<ifeed.feature.Feature> extracted_features;

        try{

            System.out.println("EpsilonMOEA called");

            List<AbstractArchitecture> archs = formatArchitectureInputBinary(all_archs);
            // Initialize DrivingFeaturesGenerator
            EOSSMOEA data_mining = new EOSSMOEA(archs, behavioral, non_behavioral);
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
    public List<Feature> getDrivingFeaturesEpsilonMOEADiscrete(String problem, java.util.List<Integer> behavioral, java.util.List<Integer> non_behavioral,
                                                       java.util.List<javaInterface.DiscreteInputArchitecture> all_archs){

        List<Feature> outputDrivingFeatures = new ArrayList<>();
        List<ifeed.feature.Feature> extracted_features;

        try{

            System.out.println("EpsilonMOEA called");

            List<AbstractArchitecture> archs = formatArchitectureInputDiscrete(all_archs);
            // Initialize DrivingFeaturesGenerator
            GNCMOEA data_mining = new GNCMOEA(archs, behavioral, non_behavioral);
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
}