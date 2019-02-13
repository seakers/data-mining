package server;

import java.io.File;
import java.util.*;

import ifeed.*;
import ifeed.architecture.AbstractArchitecture;
import ifeed.architecture.BinaryInputArchitecture;
import ifeed.architecture.DiscreteInputArchitecture;
import ifeed.architecture.ContinuousInputArchitecture;
import ifeed.feature.*;
import ifeed.feature.logic.ConnectiveTester;
import ifeed.feature.logic.Literal;
import ifeed.feature.logic.Connective;
import ifeed.feature.logic.LogicalConnectiveType;
import ifeed.filter.AbstractFilter;
import ifeed.local.params.BaseParams;
import ifeed.mining.AbstractDataMiningAlgorithm;
import ifeed.mining.AbstractLocalSearch;
import ifeed.mining.arm.AbstractAssociationRuleMining;
import ifeed.mining.moea.MOEABase;
import ifeed.mining.moea.operators.AbstractGeneralizationOperator;
import ifeed.ontology.OntologyManager;
import ifeed.problem.assigning.Apriori;
<<<<<<< HEAD
import javaInterface.*;
import javaInterface.Feature;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
=======
import ifeed.server.*;
>>>>>>> master

public class DataMiningInterfaceHandler implements DataMiningInterface.Iface {

    private String path;

    private Map<String, OntologyManager> ontologyManagerMap;
    private Map<String, AssigningProblemParameters> assigningProblemParametersMap;
    private Map<String, AssigningProblemParameters> assigningProblemExtendedParametersMap;
    private HashMap<String, BaseParams> paramsMap;

    public DataMiningInterfaceHandler(){

        this.path = System.getProperty("user.dir");

        paramsMap = new HashMap<>();

        // TODO: Remove this part and use setAssigningProblemParameters() to set the names of the orbits and instruments
        assigningProblemParametersMap = new HashMap<>();
        assigningProblemExtendedParametersMap = new HashMap<>();
        ontologyManagerMap = new HashMap<>();
        List<String> instrumentList = new ArrayList<>();
        List<String> orbitList = new ArrayList<>();
        String[] instrumentArray = {
                "ACE_ORCA","ACE_POL","ACE_LID",
                "CLAR_ERB","ACE_CPR","DESD_SAR",
                "DESD_LID","GACM_VIS","GACM_SWIR",
                "HYSP_TIR","POSTEPS_IRS","CNES_KaRIN"};
        String[] orbitArray = {"LEO-600-polar-NA","SSO-600-SSO-AM","SSO-600-SSO-DD","SSO-800-SSO-DD","SSO-800-SSO-PM"};

        for(int i = 0; i < instrumentArray.length; i++){
            instrumentList.add(instrumentArray[i]);
        }
        for(int i = 0; i < orbitArray.length; i++){
            orbitList.add(orbitArray[i]);
        }
        assigningProblemParametersMap.put("ClimateCentric", new AssigningProblemParameters(orbitList, instrumentList));
        ontologyManagerMap.put("ClimateCentric", new OntologyManager(path + File.separator + "ontology","ClimateCentric"));
    }

    private BaseParams getParams(String problem){

        if(paramsMap.keySet().contains(problem)){
            return paramsMap.get(problem);

        }else{
            BaseParams out;
            switch (problem) {
                case "ClimateCentric":
                    out = new ifeed.problem.assigning.Params();
                    break;
                case "SMAP":
                    out = new ifeed.problem.assigning.Params();
                    break;
                case "GNC":
                    out = new ifeed.problem.gnc.Params();
                    break;
                case "Decadal2017Aerosols":
                    out = new ifeed.problem.partitioningAndAssigning.Params();
                    break;
                case "Constellation_10":
                    out = new ifeed.problem.constellation.FixedNumSatParams(10);
                    break;
                case "Constellation_variable":
                    out = new ifeed.problem.constellation.VariableNumSatParams();
                    break;

                default:
                    throw new NotImplementedException();
            }
            return out;
        }
    }

    private OntologyManager getOntologyManager(String problem){
        OntologyManager out;
        if(this.ontologyManagerMap.containsKey(problem)){
            out = this.ontologyManagerMap.get(problem);

        }else{
            out = this.ontologyManagerMap.put(problem, new OntologyManager(path + File.separator + "ontology", problem));
        }
        return out;
    }

    @Override
    public boolean setAssigningProblemParameters(String problem, AssigningProblemParameters parameters){
        this.assigningProblemParametersMap.put(problem, parameters);
        return true;
    }

    @Override
    public boolean setAssigningProblemExtendedParameters(String problem, AssigningProblemParameters parameters){
        this.assigningProblemExtendedParametersMap.put(problem, parameters);
        return true;
    }

    @Override
    public boolean setPartitioningAndAssigningProblemParameters(String problem, PartitioningAndAssigningProblemParameters parameters){
        throw new NotImplementedException();
    }

    @Override
    public AssigningProblemParameters getAssigningProblemParameters(String problem){

        if(this.assigningProblemExtendedParametersMap.containsKey(problem)){
            return this.assigningProblemExtendedParametersMap.get(problem);

        }else if (this.assigningProblemParametersMap.containsKey(problem)) {
            return this.assigningProblemParametersMap.get(problem);

        } else {
            throw new IllegalStateException("setAssigningProblemParameters() needs to be called first for \'" + problem + "\' problem.");
        }
    }

    @Override
    public PartitioningAndAssigningProblemParameters getPartitioningAndAssigningProblemParameters(String problem){
        throw new NotImplementedException();
    }

    private AbstractAssociationRuleMining getAssociationRuleMining(String problem,
                                                                               BaseParams params,
                                                                               List<AbstractArchitecture> architectures,
                                                                               List<Integer> behavioral,
                                                                               List<Integer> non_behavioral,
                                                                               double supp, double conf, double lift){

        int maxFeatureLength = 2;

        AbstractAssociationRuleMining out;
        switch (problem) {
            case "ClimateCentric":
                out = new Apriori(params, maxFeatureLength, architectures, behavioral, non_behavioral, supp, conf, lift);
                break;
            case "SMAP":
                out = new Apriori(params, maxFeatureLength, architectures, behavioral, non_behavioral, supp, conf, lift);
                break;
            case "GNC":
                out = new ifeed.problem.gnc.Apriori(params, maxFeatureLength, architectures, behavioral, non_behavioral, supp, conf, lift);
                break;
            case "Decadal2017Aerosols":
                out = new ifeed.problem.partitioningAndAssigning.Apriori(params, maxFeatureLength, architectures, behavioral, non_behavioral, supp, conf, lift);
                break;
            default:
                throw new NotImplementedException();
        }
        return out;
    }

    private AbstractLocalSearch getLocalSearch(String problem,
                                                BaseParams params,
                                                ConnectiveTester root,
                                                List<AbstractArchitecture> architectures,
                                                List<Integer> behavioral,
                                                List<Integer> non_behavioral){
        AbstractLocalSearch out;
        switch (problem) {
            case "ClimateCentric":
                out = new ifeed.problem.assigning.LocalSearch(params, root, architectures, behavioral, non_behavioral);
                break;
            case "SMAP":
                out = new ifeed.problem.assigning.LocalSearch(params, root, architectures, behavioral, non_behavioral);
                break;
            case "GNC":
                out = new ifeed.problem.gnc.LocalSearch(params, root, architectures, behavioral, non_behavioral);
                break;
            case "Decadal2017Aerosols":
                out = new ifeed.problem.partitioningAndAssigning.LocalSearch(params, root, architectures, behavioral, non_behavioral);
                break;
            default:
                throw new NotImplementedException();
        }
        return out;
    }

    private AbstractDataMiningAlgorithm getAutomatedLocalSearch(String problem,
                                                                       BaseParams params,
                                                                       List<AbstractArchitecture> archs,
                                                                       List<Integer> behavioral,
                                                                       List<Integer> non_behavioral,
                                                                       int maxIter,
                                                                       double supp,
                                                                       double conf,
                                                                       double lift){

        AbstractDataMiningAlgorithm out;
        switch (problem) {
            case "ClimateCentric":
                out = new ifeed.problem.assigning.AutomatedLocalSearch(params, archs, behavioral, non_behavioral, maxIter, supp, conf, lift);
                break;
            case "SMAP":
                out = new ifeed.problem.assigning.AutomatedLocalSearch(params, archs, behavioral, non_behavioral, maxIter, supp, conf, lift);
                break;
            case "GNC":
                out = new ifeed.problem.gnc.AutomatedLocalSearch(params, archs, behavioral, non_behavioral, maxIter, supp, conf, lift);
                break;
            case "Decadal2017Aerosols":
                out = new ifeed.problem.partitioningAndAssigning.AutomatedLocalSearch(params, archs, behavioral, non_behavioral, maxIter, supp, conf, lift);
                break;
            default:
                throw new NotImplementedException();
        }
        return out;
    }


    private AbstractFeatureGeneralizer getFeatureGeneralizer(String problem,
                                               BaseParams params,
                                               Connective root,
                                               List<AbstractArchitecture> architectures,
                                               List<Integer> behavioral,
                                               List<Integer> non_behavioral){
        AbstractFeatureGeneralizer out;
        switch (problem) {
            case "ClimateCentric":
                out = new ifeed.problem.assigning.FeatureGeneralizer(params);
                break;
            default:
                throw new NotImplementedException();
        }
        return out;
    }

    private AbstractDataMiningAlgorithm getMOEA(String problem,
                                    BaseParams params,
                                    List<AbstractArchitecture> architectures,
                                    List<Integer> behavioral,
                                    List<Integer> non_behavioral){

        AbstractDataMiningAlgorithm out;
        switch (problem) {
            case "ClimateCentric":
                out = new ifeed.problem.assigning.MOEA(params, architectures, behavioral, non_behavioral);
                break;
            case "SMAP":
                out = new ifeed.problem.assigning.MOEA(params, architectures, behavioral, non_behavioral);
                break;
            case "GNC":
                out = new ifeed.problem.gnc.MOEA(params, architectures, behavioral, non_behavioral);
                break;
            case "Decadal2017Aerosols":
                out = new ifeed.problem.partitioningAndAssigning.MOEA(params, architectures, behavioral, non_behavioral);
                break;
            case "Constellation_10":
                out = new ifeed.problem.constellation.MOEA(params, architectures, behavioral, non_behavioral);
                break;
            case "Constellation_variable":
                out = new ifeed.problem.constellation.MOEA(params, architectures, behavioral, non_behavioral);
                break;

            default:
                throw new UnsupportedOperationException();
        }
        return out;
    }

    private AbstractFeatureFetcher getFeatureFetcher(String problem,
                                                BaseParams params,
                                                List<ifeed.feature.Feature> baseFeatures,
                                                List<AbstractArchitecture> architectures){

        AbstractFeatureFetcher out;
        switch (problem) {
            case "ClimateCentric":
                out = new ifeed.problem.assigning.FeatureFetcher(params, baseFeatures, architectures);
                break;
            case "SMAP":
                out = new ifeed.problem.assigning.FeatureFetcher(params, baseFeatures, architectures);
                break;
            case "GNC":
                out = new ifeed.problem.gnc.FeatureFetcher(params, baseFeatures, architectures);
                break;
            case "Decadal2017Aerosols":
                out = new ifeed.problem.partitioningAndAssigning.FeatureFetcher(params, baseFeatures, architectures);
                break;
            case "Constellation_10":
                out = new ifeed.problem.constellation.FeatureFetcher(params, baseFeatures, architectures);
                break;
            case "Constellation_variable":
                out = new ifeed.problem.constellation.FeatureFetcher(params, baseFeatures, architectures);
                break;

            default:
                throw new UnsupportedOperationException();
        }
        return out;
    }


    @Override
    public void ping() {
      System.out.println("ping()");
    }

    private List<AbstractArchitecture> formatArchitectureInputBinary(List<ifeed.server.BinaryInputArchitecture> thrift_input_architecture){
            
        List<AbstractArchitecture> archs = new ArrayList<>();

        for(int i = 0; i < thrift_input_architecture.size(); i++){
            ifeed.server.BinaryInputArchitecture input_arch = thrift_input_architecture.get(i);
            int id = input_arch.getId();
            List<Boolean> bitString = input_arch.getInputs();

            BitSet inputs = new BitSet(bitString.size());
            for(int j=0;j<bitString.size();j++){
                if(bitString.get(j)){
                    inputs.set(j);
                }
            }
            
            List<Double> _outputs = input_arch.getOutputs();
            double[] outputs = new double[_outputs.size()];
            for(int j = 0; j < _outputs.size(); j++){
                outputs[j] = _outputs.get(j);
            }

            archs.add(new BinaryInputArchitecture(id, inputs, outputs));
        }

        return archs;
    }

    private List<AbstractArchitecture> formatArchitectureInputDiscrete(List<ifeed.server.DiscreteInputArchitecture> thrift_input_architecture){

        List<AbstractArchitecture> archs = new ArrayList<>();
        for(int i = 0; i < thrift_input_architecture.size(); i++){

            ifeed.server.DiscreteInputArchitecture input_arch = thrift_input_architecture.get(i);
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

    private List<AbstractArchitecture> formatArchitectureInputContinuous(List<ifeed.server.ContinuousInputArchitecture> thrift_input_architecture){

        List<AbstractArchitecture> archs = new ArrayList<>();
        for(int i = 0; i < thrift_input_architecture.size(); i++){

            ifeed.server.ContinuousInputArchitecture input_arch = thrift_input_architecture.get(i);
            int id = input_arch.getId();
            List<Double> _inputs = input_arch.getInputs();
            double[] inputs = new double[_inputs.size()];
            List<Double> _outputs = input_arch.getOutputs();
            double[] outputs = new double[_outputs.size()];

            for(int j = 0; j < _inputs.size(); j++){
                inputs[j] = _inputs.get(j);
            }
            for(int j = 0; j < _outputs.size(); j++){
                outputs[j] = _outputs.get(j);
            }
            archs.add(new ContinuousInputArchitecture(id, inputs, outputs));
        }

        return archs;
    }


    private List<Feature> formatFeatureOutput(List<ifeed.feature.Feature> data_mining_output_features){
        
        List<Feature> out = new ArrayList<>();
        for(int i=0;i<data_mining_output_features.size();i++){
            
            ifeed.feature.Feature f = data_mining_output_features.get(i);
            
            if(i>800){ // Threshold on the maximum number of features sent back
                break;
            }              

            String name  = f.getName();
            String expression = f.getName();
            ArrayList<Double> metrics = new ArrayList<>();
            metrics.add(f.getSupport());
            metrics.add(f.getLift());
            metrics.add(f.getPrecision());
            metrics.add(f.getRecall());

            double complexity;
            if(f.getAlgebraicComplexity() == Double.NaN){
                complexity = -1.0;
            }else{
                complexity = f.getAlgebraicComplexity();
            }
            out.add(new ifeed.server.Feature(i,name,expression,metrics, complexity));
        }
        return out;
    }

    @Override
    public List<Feature> getDrivingFeaturesBinary(String problem, java.util.List<Integer> behavioral, java.util.List<Integer> non_behavioral,
            java.util.List<ifeed.server.BinaryInputArchitecture> inputArchs, double supp, double conf, double lift){

        List<Feature> out = new ArrayList<>();
        List<ifeed.feature.Feature> extracted_features;
        
        try{
            BaseParams params = getParams(problem);

            List<AbstractArchitecture> archs = formatArchitectureInputBinary(inputArchs);

            // Initialize DrivingFeaturesGenerator
            AbstractAssociationRuleMining data_mining = getAssociationRuleMining(problem, params, archs, behavioral,non_behavioral,supp,conf,lift);

            // Run data mining
            extracted_features = data_mining.run();
            FeatureMetricComparator comparator1 = new FeatureMetricComparator(FeatureMetric.FCONFIDENCE);
            FeatureMetricComparator comparator2 = new FeatureMetricComparator(FeatureMetric.RCONFIDENCE);
            List<Comparator> comparators = new ArrayList<>(Arrays.asList(comparator1,comparator2));
            extracted_features = Utils.getFeatureFuzzyParetoFront(extracted_features,comparators,3);
            out = formatFeatureOutput(extracted_features);
            
        }catch(Exception TException){
            TException.printStackTrace();
        }
        
        return out;
    }

    @Override
    public List<Feature> runAutomatedLocalSearchBinary(String problem, java.util.List<Integer> behavioral, java.util.List<Integer> non_behavioral,
            java.util.List<ifeed.server.BinaryInputArchitecture> all_archs, double supp, double conf, double lift){

        List<Feature> out = new ArrayList<>();

        try{
            BaseParams params = getParams(problem);

            List<AbstractArchitecture> archs = formatArchitectureInputBinary(all_archs);

            // Initialize DrivingFeaturesGenerator
            AbstractDataMiningAlgorithm automatedSearch = getAutomatedLocalSearch(problem, params, archs, behavioral, non_behavioral, 5, supp, conf, lift);

            // Run data mining
            List<ifeed.feature.Feature> extracted_features = automatedSearch.run(); // Args: maxIter, numInitialFeatureToAdd

            System.out.println("Automated run finished with num of features: " + extracted_features.size());

            out = formatFeatureOutput(extracted_features);

        }catch(Exception TException){
            TException.printStackTrace();
        }

        return out;
    }

    @Override
    public List<Feature> getMarginalDrivingFeaturesBinary(String problem, java.util.List<Integer> behavioral, java.util.List<Integer> non_behavioral,
            java.util.List<ifeed.server.BinaryInputArchitecture> all_archs, String featureExpression, String logicalConnective, double supp, double conf, double lift){

        // Feature: {id, name, expression, metrics}
        List<Feature> out = new ArrayList<>();

        try{
            BaseParams params = getParams(problem);

            List<AbstractArchitecture> archs = formatArchitectureInputBinary(all_archs);

            // Initialize DrivingFeaturesGenerator
            AbstractLocalSearch data_mining = getLocalSearch(problem, params,null, archs, behavioral,non_behavioral);

            // If Ontology exists, add it to the AbstractConstellationProblemParams object
            if(this.ontologyManagerMap.containsKey(problem)){
                data_mining.getParams().setOntologyManager(this.ontologyManagerMap.get(problem));

                if(problem.equalsIgnoreCase("ClimateCentric")){
                    List<String> instrumentList = this.assigningProblemParametersMap.get(problem).instrumentList;
                    List<String> orbitList = this.assigningProblemParametersMap.get(problem).orbitList;
                    String[] orbitNameArray = orbitList.toArray(new String[orbitList.size()]);
                    String[] instrumentNameArray = instrumentList.toArray(new String[instrumentList.size()]);
                    ((ifeed.problem.assigning.Params) data_mining.getParams()).setOrbitList(orbitNameArray);
                    ((ifeed.problem.assigning.Params) data_mining.getParams()).setInstrumentList(instrumentNameArray);

                    if(this.assigningProblemExtendedParametersMap.containsKey(problem)){
                        List<String> extendedInstrumentList = this.assigningProblemExtendedParametersMap.get(problem).instrumentList;
                        List<String> extendedOrbitList = this.assigningProblemExtendedParametersMap.get(problem).orbitList;
                        for(int i = orbitList.size(); i < extendedOrbitList.size(); i++){
                            String orbitClass = extendedOrbitList.get(i);
                            ((ifeed.problem.assigning.Params) data_mining.getParams()).addOrbitClass(orbitClass);
                        }
                        for(int i = instrumentList.size(); i < extendedInstrumentList.size(); i++){
                            String instrumentClass = extendedInstrumentList.get(i);
                            ((ifeed.problem.assigning.Params) data_mining.getParams()).addInstrumentClass(instrumentClass);
                        }
                    }
                }
            }

            List<ifeed.feature.Feature> baseFeatures = data_mining.generateBaseFeatures();

            System.out.println("...["+ data_mining.getClass().getSimpleName() +"] The number of candidate features: " + baseFeatures.size());

            AbstractFeatureFetcher featureFetcher = getFeatureFetcher(problem, params, baseFeatures, archs);
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

            System.out.println("...["+ data_mining.getClass().getSimpleName() +"] Num of same nodes found: " + sameConnectives.size());
            System.out.println("...["+ data_mining.getClass().getSimpleName() +"] Num of opposite nodes found: " + oppositeConnectives.size());

            // Initialize the extracted features
            List<ifeed.feature.Feature> extracted_features = new ArrayList<>();
            FeatureMetricComparator comparator1 = new FeatureMetricComparator(FeatureMetric.FCONFIDENCE);
            FeatureMetricComparator comparator2 = new FeatureMetricComparator(FeatureMetric.RCONFIDENCE);
            List<Comparator> comparators = new ArrayList<>(Arrays.asList(comparator1,comparator2));

            for(Connective node: sameConnectives){
                ConnectiveTester tester = (ConnectiveTester) node;
                tester.setAddNewNode();
                List<ifeed.feature.Feature> tempFeatures = data_mining.run(baseFeatures);
                extracted_features.addAll(tempFeatures);
                tester.cancelAddNode();
            }

            for(Connective node: oppositeConnectives){
                ConnectiveTester tester = (ConnectiveTester) node;
                for(Literal literal: tester.getLiteralChildren()){
                    tester.setAddNewNode(literal);
                    List<ifeed.feature.Feature> tempFeatures = data_mining.run(baseFeatures);
                    extracted_features.addAll(tempFeatures);
                    tester.cancelAddNode();
                }
            }

            System.out.println(extracted_features.size());
            extracted_features = Utils.getFeatureFuzzyParetoFront(extracted_features,comparators,2);
            out = formatFeatureOutput(extracted_features);

        }catch(Exception TException){
            TException.printStackTrace();
        }

        return out;
    }

    @Override
    public List<Feature> getDrivingFeaturesDiscrete(String problem, java.util.List<Integer> behavioral, java.util.List<Integer> non_behavioral,
                                                  java.util.List<ifeed.server.DiscreteInputArchitecture> all_archs, double supp, double conf, double lift){

        List<Feature> out = new ArrayList<>();

        List<ifeed.feature.Feature> extracted_features;

        try{
            BaseParams params = getParams(problem);

            List<AbstractArchitecture> archs = formatArchitectureInputDiscrete(all_archs);

            // Initialize DrivingFeaturesGenerator
            AbstractAssociationRuleMining data_mining = getAssociationRuleMining(problem, params, archs, behavioral,non_behavioral,supp,conf,lift);

            // Run data mining
            extracted_features = data_mining.run();

            FeatureMetricComparator comparator1 = new FeatureMetricComparator(FeatureMetric.FCONFIDENCE);
            FeatureMetricComparator comparator2 = new FeatureMetricComparator(FeatureMetric.RCONFIDENCE);
            List<Comparator> comparators = new ArrayList<>(Arrays.asList(comparator1,comparator2));
            extracted_features = Utils.getFeatureFuzzyParetoFront(extracted_features,comparators,3);

            out = formatFeatureOutput(extracted_features);

        }catch(Exception TException){
            TException.printStackTrace();
        }

        return out;
    }

    @Override
    public List<Feature> runAutomatedLocalSearchDiscrete(String problem, java.util.List<Integer> behavioral, java.util.List<Integer> non_behavioral,
                                                       java.util.List<ifeed.server.DiscreteInputArchitecture> all_archs, double supp, double conf, double lift){

        List<Feature> out = new ArrayList<>();

        try{
            BaseParams params = getParams(problem);

            List<AbstractArchitecture> archs = formatArchitectureInputDiscrete(all_archs);
            List<ifeed.feature.Feature> extracted_features;

            // Initialize DrivingFeaturesGenerator
            AbstractDataMiningAlgorithm automatedSearch = getAutomatedLocalSearch(problem, params, archs, behavioral, non_behavioral, 7, supp, conf, lift);
            // Run data mining
            extracted_features = automatedSearch.run(); // Args: maxIter, numInitialFeatureToAdd

            System.out.println("Automated run finished with num of features: " + extracted_features.size());
            out = formatFeatureOutput(extracted_features);

        }catch(Exception TException){
            TException.printStackTrace();
        }

        return out;
    }

    @Override
    public List<Feature> getMarginalDrivingFeaturesDiscrete(String problem,
                                                            java.util.List<Integer> behavioral,
                                                            java.util.List<Integer> non_behavioral,
                                                            java.util.List<ifeed.server.DiscreteInputArchitecture> all_archs,
                                                            String featureExpression,
                                                            String logicalConnective,
                                                            double supp, double conf, double lift
    ){

        // A feature is defined from: {id, name, expression, metrics}
        List<Feature> out = new ArrayList<>();

        try{
            BaseParams params = getParams(problem);

            List<AbstractArchitecture> archs = formatArchitectureInputDiscrete(all_archs);

            AbstractLocalSearch data_mining;
            AbstractFeatureFetcher featureFetcher;
            List<ifeed.feature.Feature> baseFeatures;

            // Get data mining object
            data_mining = getLocalSearch(problem, params,null, archs, behavioral,non_behavioral);
            baseFeatures = data_mining.generateBaseFeatures();

            System.out.println("...[" + this.getClass().getSimpleName() + "] The number of candidate features: " + baseFeatures.size());
            featureFetcher = getFeatureFetcher(problem, params, baseFeatures, archs);

            FeatureExpressionHandler filterExpressionHandler = new FeatureExpressionHandler(featureFetcher);

            // Create a tree structure based on the given feature expression
            ConnectiveTester root = (ConnectiveTester) filterExpressionHandler.generateFeatureTree(featureExpression, true);
            data_mining.setRoot(root);

            List<Connective> sameConnectives;
            List<Connective> oppositeConnectives;

            // Count the occurrence of each type of connective nodes
            if(logicalConnective.equalsIgnoreCase("OR")){
                sameConnectives = root.getDescendantConnectives(LogicalConnectiveType.OR, true);
                oppositeConnectives = root.getDescendantConnectives(LogicalConnectiveType.AND, true);
            }else{
                sameConnectives = root.getDescendantConnectives(LogicalConnectiveType.AND, true);
                oppositeConnectives = root.getDescendantConnectives(LogicalConnectiveType.OR, true);
            }
            System.out.println("...["+ data_mining.getClass().getSimpleName() +"] Number of " + logicalConnective + " nodes found: " + sameConnectives.size());
            System.out.println("...["+ data_mining.getClass().getSimpleName() +"] Number of opposite nodes found: " + oppositeConnectives.size());

            // Initialize the extracted features
            List<ifeed.feature.Feature> extracted_features = new ArrayList<>();

            // For the connective nodes of the same type, simply try adding a new node to the parent
            for(Connective node: sameConnectives){
                ConnectiveTester tester = (ConnectiveTester) node;
                tester.setAddNewNode();
                tester.precomputeMatchesLiteral();
                List<ifeed.feature.Feature> tempFeatures = data_mining.run(baseFeatures);
                extracted_features.addAll(tempFeatures);
                tester.cancelAddNode();
            }

            for(Connective node: oppositeConnectives){
                ConnectiveTester tester = (ConnectiveTester) node;
                for(Literal feature: node.getLiteralChildren()){
                    tester.setAddNewNode(feature);
                    tester.precomputeMatchesLiteral();
                    List<ifeed.feature.Feature> tempFeatures = data_mining.run(baseFeatures);
                    extracted_features.addAll(tempFeatures);
                    tester.cancelAddNode();
                }
            }

            FeatureMetricComparator comparator1 = new FeatureMetricComparator(FeatureMetric.FCONFIDENCE);
            FeatureMetricComparator comparator2 = new FeatureMetricComparator(FeatureMetric.RCONFIDENCE);
            List<Comparator> comparators = new ArrayList<>(Arrays.asList(comparator1,comparator2));
            extracted_features = Utils.getFeatureFuzzyParetoFront(extracted_features,comparators,3);
            out = formatFeatureOutput(extracted_features);

        }catch(Exception TException){
            TException.printStackTrace();
        }

        return out;
    }


    @Override
    public List<Feature> getDrivingFeaturesContinuous(String problem, java.util.List<Integer> behavioral, java.util.List<Integer> non_behavioral,
                                                  java.util.List<ifeed.server.ContinuousInputArchitecture> inputArchs, double supp, double conf, double lift){

        List<Feature> out = new ArrayList<>();
        List<ifeed.feature.Feature> extracted_features;

        try{
            BaseParams params = getParams(problem);

            List<AbstractArchitecture> archs = formatArchitectureInputContinuous(inputArchs);

            // Initialize DrivingFeaturesGenerator
            AbstractAssociationRuleMining data_mining = getAssociationRuleMining(problem, params, archs, behavioral,non_behavioral,supp,conf,lift);

            // Run data mining
            extracted_features = data_mining.run();
            FeatureMetricComparator comparator1 = new FeatureMetricComparator(FeatureMetric.FCONFIDENCE);
            FeatureMetricComparator comparator2 = new FeatureMetricComparator(FeatureMetric.RCONFIDENCE);
            List<Comparator> comparators = new ArrayList<>(Arrays.asList(comparator1,comparator2));
            extracted_features = Utils.getFeatureFuzzyParetoFront(extracted_features,comparators,3);
            out = formatFeatureOutput(extracted_features);

        }catch(Exception TException){
            TException.printStackTrace();
        }

        return out;
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
    public double computeComplexity(String problem, String expression){
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
    public List<Double> computeComplexityOfFeatures(String problem, List<String> expressions){
        ArrayList<Double> out = new ArrayList<>();
        for(String exp:expressions){
            out.add(this.computeComplexity(problem, exp));
        }
        return out;
    }

    @Override
    public List<Integer> computeAlgebraicTypicality(String problem, ifeed.server.BinaryInputArchitecture arch, String feature){

        BaseParams params = getParams(problem);

        List<ifeed.server.BinaryInputArchitecture> tempList = Arrays.asList(arch);

        BinaryInputArchitecture a = (BinaryInputArchitecture) formatArchitectureInputBinary(tempList).get(0);
        BitSet input = a.getInputs();

        AbstractFeatureFetcher featureFetcher = getFeatureFetcher(problem, params, new ArrayList<>(), new ArrayList<>());
        ifeed.feature.TypicalityCalculator calculator = new ifeed.feature.TypicalityCalculator(input, feature, featureFetcher);

        int[] out = calculator.run();
        return new ArrayList<>(Arrays.asList(out[0], out[1]));
    }

    @Override
    public List<Integer> computeAlgebraicTypicalityWithStringInput(String problem, String architecture, String feature){

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

        BaseParams params = getParams(problem);

        AbstractFeatureFetcher featureFetcher = getFeatureFetcher(problem, params, new ArrayList<>(), new ArrayList<>());
        ifeed.feature.TypicalityCalculator calculator = new ifeed.feature.TypicalityCalculator(inputs, feature, featureFetcher);

        int[] out = calculator.run();

        int diff = out[1] - out[0];

        System.out.println("Typicality diff: " + diff);

        return new ArrayList<>(Arrays.asList(out[0], out[1]));
    }

    @Override
    public List<Feature> getDrivingFeaturesEpsilonMOEABinary(String problem, java.util.List<Integer> behavioral, java.util.List<Integer> non_behavioral,
                                                  java.util.List<ifeed.server.BinaryInputArchitecture> all_archs){

        List<Feature> out = new ArrayList<>();
        List<ifeed.feature.Feature> extracted_features;

        try{
            System.out.println("EpsilonMOEA called");

            BaseParams params = getParams(problem);

            List<AbstractArchitecture> archs = formatArchitectureInputBinary(all_archs);

            // Initialize DrivingFeaturesGenerator
            AbstractDataMiningAlgorithm data_mining = getMOEA(problem, params, archs, behavioral, non_behavioral);

            // Run data mining
            extracted_features = data_mining.run();

            FeatureMetricComparator comparator1 = new FeatureMetricComparator(FeatureMetric.FCONFIDENCE);
            FeatureMetricComparator comparator2 = new FeatureMetricComparator(FeatureMetric.RCONFIDENCE);
            List<Comparator> comparators = new ArrayList<>(Arrays.asList(comparator1,comparator2));
            extracted_features = Utils.getFeatureFuzzyParetoFront(extracted_features,comparators,3);

            out = formatFeatureOutput(extracted_features);

        }catch(Exception TException ){
            TException.printStackTrace();
        }

        return out;
    }

    @Override
    public List<Feature> getDrivingFeaturesEpsilonMOEADiscrete(String problem, java.util.List<Integer> behavioral, java.util.List<Integer> non_behavioral,
                                                       java.util.List<ifeed.server.DiscreteInputArchitecture> all_archs){

        List<Feature> out = new ArrayList<>();
        List<ifeed.feature.Feature> extracted_features;

        try{

            System.out.println("EpsilonMOEA called");

            BaseParams params = getParams(problem);

            List<AbstractArchitecture> archs = formatArchitectureInputDiscrete(all_archs);

            // Initialize DrivingFeaturesGenerator
            AbstractDataMiningAlgorithm data_mining = getMOEA(problem, params, archs, behavioral, non_behavioral);

            // Run data mining
            extracted_features = data_mining.run();

            FeatureMetricComparator comparator1 = new FeatureMetricComparator(FeatureMetric.FCONFIDENCE);
            FeatureMetricComparator comparator2 = new FeatureMetricComparator(FeatureMetric.RCONFIDENCE);
            List<Comparator> comparators = new ArrayList<>(Arrays.asList(comparator1,comparator2));
            extracted_features = Utils.getFeatureFuzzyParetoFront(extracted_features,comparators,3);

            out = formatFeatureOutput(extracted_features);

        }catch(Exception TException){
            TException.printStackTrace();
        }

        return out;
    }


    @Override
    public List<Feature> getDrivingFeaturesEpsilonMOEAContinuous(String problem, java.util.List<Integer> behavioral, java.util.List<Integer> non_behavioral,
                                                             java.util.List<ifeed.server.ContinuousInputArchitecture> all_archs){

        List<Feature> out = new ArrayList<>();
        List<ifeed.feature.Feature> extracted_features;

        try{
            System.out.println("EpsilonMOEA called");

            BaseParams params = getParams(problem);

            List<AbstractArchitecture> archs = formatArchitectureInputContinuous(all_archs);

            // Initialize DrivingFeaturesGenerator
            AbstractDataMiningAlgorithm data_mining = getMOEA(problem, params, archs, behavioral, non_behavioral);

            // Run data mining
            extracted_features = data_mining.run();

            FeatureMetricComparator comparator1 = new FeatureMetricComparator(FeatureMetric.FCONFIDENCE);
            FeatureMetricComparator comparator2 = new FeatureMetricComparator(FeatureMetric.RCONFIDENCE);
            List<Comparator> comparators = new ArrayList<>(Arrays.asList(comparator1,comparator2));
            extracted_features = Utils.getFeatureFuzzyParetoFront(extracted_features,comparators,3);

            out = formatFeatureOutput(extracted_features);

        }catch(Exception TException ){
            TException.printStackTrace();
        }

        return out;
    }


    @Override
    public List<Feature> getDrivingFeaturesWithGeneralizationBinary(String problem, java.util.List<Integer> behavioral, java.util.List<Integer> non_behavioral,
                                                                    java.util.List<ifeed.server.BinaryInputArchitecture> all_archs){

        List<Feature> out = new ArrayList<>();
        List<ifeed.feature.Feature> extracted_features;

        try{
            System.out.println("EpsilonMOEA with generalization");

            List<AbstractArchitecture> archs = formatArchitectureInputBinary(all_archs);

            // Generalization-enabled problem
            if(problem.equalsIgnoreCase("ClimateCentric")){

                List<String> instrumentList = this.assigningProblemParametersMap.get(problem).instrumentList;
                List<String> orbitList = this.assigningProblemParametersMap.get(problem).orbitList;
                String[] orbitNameArray = orbitList.toArray(new String[orbitList.size()]);
                String[] instrumentNameArray = instrumentList.toArray(new String[instrumentList.size()]);

                BaseParams params = getParams(problem);
                ifeed.problem.assigning.MOEA assigningMOEA = new ifeed.problem.assigning.MOEA(params, archs, behavioral, non_behavioral);
                assigningMOEA.setMode(ifeed.problem.assigning.MOEA.RUN_MODE.AOS_with_generalization_operators);
                assigningMOEA.setOrbitList(orbitNameArray);
                assigningMOEA.setInstrumentList(instrumentNameArray);
                assigningMOEA.setOntologyManager(getOntologyManager(problem));

                // Run data mining
                extracted_features = assigningMOEA.run();

                orbitNameArray = assigningMOEA.getOrbitList();
                instrumentNameArray = assigningMOEA.getInstrumentList();
                orbitList = new ArrayList<>();
                instrumentList = new ArrayList<>();
                for(int i = 0; i < orbitNameArray.length; i++){
                    orbitList.add(orbitNameArray[i]);
                }
                for(int i = 0; i < instrumentNameArray.length; i++){
                    instrumentList.add(instrumentNameArray[i]);
                }
                this.assigningProblemExtendedParametersMap.put(problem, new AssigningProblemParameters(orbitList, instrumentList));

            }else{
                throw new UnsupportedOperationException();
            }

            FeatureMetricComparator comparator1 = new FeatureMetricComparator(FeatureMetric.FCONFIDENCE);
            FeatureMetricComparator comparator2 = new FeatureMetricComparator(FeatureMetric.RCONFIDENCE);
            List<Comparator> comparators = new ArrayList<>(Arrays.asList(comparator1,comparator2));
            extracted_features = Utils.getFeatureFuzzyParetoFront(extracted_features,comparators,3);
            out = formatFeatureOutput(extracted_features);

        }catch(Exception TException ){
            TException.printStackTrace();
        }

        return out;
    }

    @Override
    public List<Feature> runInputGeneralizationLocalSearchBinary(String problem,
                                                                 java.util.List<Integer> behavioral,
                                                                 java.util.List<Integer> non_behavioral,
                                                                 java.util.List<ifeed.server.BinaryInputArchitecture> all_archs,
                                                                 String featureExpression){

        List<Feature> out = new ArrayList<>();
        List<ifeed.feature.Feature> extracted_features = new ArrayList<>();

        try{
            System.out.println("Local search through generalization of input variables");

            List<AbstractArchitecture> archs = formatArchitectureInputBinary(all_archs);

            BaseParams params = getParams(problem);

            // Generalization-enabled problem
            if(problem.equalsIgnoreCase("ClimateCentric")){

                ifeed.problem.assigning.Params assigningParams = (ifeed.problem.assigning.Params) params;
                OntologyManager ontologyManager = getOntologyManager(problem);
                assigningParams.setOntologyManager(ontologyManager);

                List<String> instrumentList = this.assigningProblemParametersMap.get(problem).instrumentList;
                List<String> orbitList = this.assigningProblemParametersMap.get(problem).orbitList;
                String[] orbitNameArray = orbitList.toArray(new String[orbitList.size()]);
                String[] instrumentNameArray = instrumentList.toArray(new String[instrumentList.size()]);
                assigningParams.setOrbitList(orbitNameArray);
                assigningParams.setInstrumentList(instrumentNameArray);

                if(this.assigningProblemExtendedParametersMap.containsKey(problem)){
                    List<String> extendedInstrumentList = this.assigningProblemExtendedParametersMap.get(problem).instrumentList;
                    List<String> extendedOrbitList = this.assigningProblemExtendedParametersMap.get(problem).orbitList;
                    for(int i = orbitList.size(); i < extendedOrbitList.size(); i++){
                        String orbitClass = extendedOrbitList.get(i);
                        assigningParams.addOrbitClass(orbitClass);
                    }
                    for(int i = instrumentList.size(); i < extendedInstrumentList.size(); i++){
                        String instrumentClass = extendedInstrumentList.get(i);
                        assigningParams.addInstrumentClass(instrumentClass);
                    }
                }

                ifeed.problem.assigning.MOEA assigningMOEA = new ifeed.problem.assigning.MOEA(assigningParams, archs, behavioral, non_behavioral);
                assigningMOEA.setOntologyManager(ontologyManager);

                AbstractFeatureFetcher featureFetcher = getFeatureFetcher(problem, assigningParams, new ArrayList<>(), archs);
                FeatureExpressionHandler filterExpressionHandler = new FeatureExpressionHandler(featureFetcher);

                // Create a tree structure based on the given feature expression
                Connective root = filterExpressionHandler.generateFeatureTree(featureExpression);

                ifeed.problem.assigning.logicOperators.generalization.InstrumentGeneralizer instrumentGeneralizer =
                        new ifeed.problem.assigning.logicOperators.generalization.InstrumentGeneralizer(assigningParams, assigningMOEA);

                ifeed.problem.assigning.logicOperators.generalization.OrbitGeneralizer orbitGeneralizer =
                        new ifeed.problem.assigning.logicOperators.generalization.OrbitGeneralizer(assigningParams, assigningMOEA);

                List<Connective> instrumentGeneralizationParentNodes = instrumentGeneralizer.getParentNodesOfApplicableNodes(root, null);
                List<Connective> orbitGeneralizationParentNodes = orbitGeneralizer.getParentNodesOfApplicableNodes(root, null);

                Random random = new Random();

                int cnt = 0;
                while(cnt < 80){

                    Connective testRoot = root.copy();
                    Connective parent;
                    AbstractGeneralizationOperator generalizer;
                    int randInt = random.nextInt(orbitGeneralizationParentNodes.size() + instrumentGeneralizationParentNodes.size());

                    if(randInt < orbitGeneralizationParentNodes.size()){
                        parent = orbitGeneralizationParentNodes.get(randInt);
                        generalizer = orbitGeneralizer;

                    }else{
                        parent = instrumentGeneralizationParentNodes.get(randInt - orbitGeneralizationParentNodes.size());
                        generalizer = instrumentGeneralizer;
                    }

                    // Find the applicable nodes under the parent node found
                    Map<AbstractFilter, Set<AbstractFilter>> applicableFiltersMap = new HashMap<>();
                    Map<AbstractFilter, Literal> applicableLiteralsMap = new HashMap<>();
                    generalizer.findApplicableNodesUnderGivenParentNode(parent, applicableFiltersMap, applicableLiteralsMap);

                    // Randomly select one constraint setter node
                    List<AbstractFilter> constraintSetters = new ArrayList<>(applicableFiltersMap.keySet());

                    if(constraintSetters.isEmpty()){
                        cnt++;
                        continue;
                    }

                    AbstractFilter constraintSetter = constraintSetters.get(random.nextInt(constraintSetters.size()));
                    Set<AbstractFilter> matchingNodes = applicableFiltersMap.get(constraintSetter);

                    // Modify the nodes using the given argument
                    generalizer.apply(testRoot, parent, constraintSetter, matchingNodes, applicableLiteralsMap);

                    double[] metrics = Utils.computeMetricsSetNaNZero(testRoot.getMatches(), assigningMOEA.getLabels(), all_archs.size());
                    ifeed.feature.Feature feature = new ifeed.feature.Feature(testRoot.getName(), testRoot.getMatches(), metrics[0], metrics[1], metrics[2], metrics[3]);
                    extracted_features.add(feature);

                    cnt++;
                }

                FeatureMetricComparator comparator1 = new FeatureMetricComparator(FeatureMetric.FCONFIDENCE);
                FeatureMetricComparator comparator2 = new FeatureMetricComparator(FeatureMetric.RCONFIDENCE);
                List<Comparator> comparators = new ArrayList<>(Arrays.asList(comparator1,comparator2));

                System.out.println(extracted_features.size());
                extracted_features = Utils.getFeatureFuzzyParetoFront(extracted_features,comparators,2);
                out = formatFeatureOutput(extracted_features);

            }else{
                throw new NotImplementedException();
            }

        }catch(Exception TException ){
            TException.printStackTrace();
        }

        return out;
    }

    @Override
    public List<Feature> runFeatureGeneralizationLocalSearchBinary(String problem,
                                                                 java.util.List<Integer> behavioral,
                                                                 java.util.List<Integer> non_behavioral,
                                                                 java.util.List<ifeed.server.BinaryInputArchitecture> all_archs,
                                                                 String featureExpression){
        throw new UnsupportedOperationException();
    }

    @Override
    public TaxonomicScheme getAssigningProblemTaxonomicScheme(String problem, AssigningProblemParameters params){
        OntologyManager manager = getOntologyManager(problem);
        List<String> orbitList = params.orbitList;
        List<String> instrumentList = params.instrumentList;

        for(String orbit: orbitList){
            if(!manager.getSuperclassMap().containsKey(orbit)){
                manager.getSuperClasses("Orbit",orbit);
            }
        }
        for(String instrument: instrumentList){
            if(!manager.getSuperclassMap().containsKey(instrument)){
                manager.getSuperClasses("Instrument",instrument);
            }
        }
        Map<String, List<String>> superclassesMap = manager.getSuperclassMap();
        Map<String, List<String>> instancesMap = manager.getInstanceMap();
        TaxonomicScheme scheme = new TaxonomicScheme(instancesMap, superclassesMap);
        return scheme;
    }

    @Override
    public List<Feature> generalizeFeature(String problem,
                                           java.util.List<Integer> behavioral,
                                           java.util.List<Integer> non_behavioral,
                                           java.util.List<javaInterface.BinaryInputArchitecture> all_archs,
                                           String featureExpression
    ){

        // Output: String explanation, String modifiedExpression

        List<Feature> out = new ArrayList<>();

        List<ifeed.feature.Feature> extracted_features = new ArrayList<>();

        try{
            List<AbstractArchitecture> archs = formatArchitectureInputBinary(all_archs);

            BaseParams params = getParams(problem);

            // Generalization-enabled problem
            if(problem.equalsIgnoreCase("ClimateCentric")){

                ifeed.problem.assigning.Params assigningParams = (ifeed.problem.assigning.Params) params;
                OntologyManager ontologyManager = getOntologyManager(problem);
                assigningParams.setOntologyManager(ontologyManager);

                List<String> instrumentList = this.assigningProblemParametersMap.get(problem).instrumentList;
                List<String> orbitList = this.assigningProblemParametersMap.get(problem).orbitList;
                String[] orbitNameArray = orbitList.toArray(new String[orbitList.size()]);
                String[] instrumentNameArray = instrumentList.toArray(new String[instrumentList.size()]);
                assigningParams.setOrbitList(orbitNameArray);
                assigningParams.setInstrumentList(instrumentNameArray);

                if(this.assigningProblemExtendedParametersMap.containsKey(problem)){
                    List<String> extendedInstrumentList = this.assigningProblemExtendedParametersMap.get(problem).instrumentList;
                    List<String> extendedOrbitList = this.assigningProblemExtendedParametersMap.get(problem).orbitList;
                    for(int i = orbitList.size(); i < extendedOrbitList.size(); i++){
                        String orbitClass = extendedOrbitList.get(i);
                        assigningParams.addOrbitClass(orbitClass);
                    }
                    for(int i = instrumentList.size(); i < extendedInstrumentList.size(); i++){
                        String instrumentClass = extendedInstrumentList.get(i);
                        assigningParams.addInstrumentClass(instrumentClass);
                    }
                }

                ifeed.problem.assigning.MOEA assigningMOEA = new ifeed.problem.assigning.MOEA(assigningParams, archs, behavioral, non_behavioral);
                assigningMOEA.setOntologyManager(ontologyManager);

                AbstractFeatureFetcher featureFetcher = getFeatureFetcher(problem, assigningParams, new ArrayList<>(), archs);
                FeatureExpressionHandler filterExpressionHandler = new FeatureExpressionHandler(featureFetcher);

                // Create a tree structure based on the given feature expression
                Connective root = filterExpressionHandler.generateFeatureTree(featureExpression);

                ifeed.problem.assigning.logicOperators.generalization.InstrumentGeneralizer instrumentGeneralizer =
                        new ifeed.problem.assigning.logicOperators.generalization.InstrumentGeneralizer(assigningParams, assigningMOEA);

                ifeed.problem.assigning.logicOperators.generalization.OrbitGeneralizer orbitGeneralizer =
                        new ifeed.problem.assigning.logicOperators.generalization.OrbitGeneralizer(assigningParams, assigningMOEA);

                List<Connective> instrumentGeneralizationParentNodes = instrumentGeneralizer.getParentNodesOfApplicableNodes(root, null);
                List<Connective> orbitGeneralizationParentNodes = orbitGeneralizer.getParentNodesOfApplicableNodes(root, null);

                Random random = new Random();

                int cnt = 0;
                while(cnt < 80){

                    Connective testRoot = root.copy();
                    Connective parent;
                    AbstractGeneralizationOperator generalizer;
                    int randInt = random.nextInt(orbitGeneralizationParentNodes.size() + instrumentGeneralizationParentNodes.size());

                    if(randInt < orbitGeneralizationParentNodes.size()){
                        parent = orbitGeneralizationParentNodes.get(randInt);
                        generalizer = orbitGeneralizer;

                    }else{
                        parent = instrumentGeneralizationParentNodes.get(randInt - orbitGeneralizationParentNodes.size());
                        generalizer = instrumentGeneralizer;
                    }

                    // Find the applicable nodes under the parent node found
                    Map<AbstractFilter, Set<AbstractFilter>> applicableFiltersMap = new HashMap<>();
                    Map<AbstractFilter, Literal> applicableLiteralsMap = new HashMap<>();
                    generalizer.findApplicableNodesUnderGivenParentNode(parent, applicableFiltersMap, applicableLiteralsMap);

                    // Randomly select one constraint setter node
                    List<AbstractFilter> constraintSetters = new ArrayList<>(applicableFiltersMap.keySet());

                    if(constraintSetters.isEmpty()){
                        cnt++;
                        continue;
                    }

                    AbstractFilter constraintSetter = constraintSetters.get(random.nextInt(constraintSetters.size()));
                    Set<AbstractFilter> matchingNodes = applicableFiltersMap.get(constraintSetter);

                    // Modify the nodes using the given argument
                    generalizer.apply(testRoot, parent, constraintSetter, matchingNodes, applicableLiteralsMap);

                    double[] metrics = Utils.computeMetricsSetNaNZero(testRoot.getMatches(), assigningMOEA.getLabels(), all_archs.size());
                    ifeed.feature.Feature feature = new ifeed.feature.Feature(testRoot.getName(), testRoot.getMatches(), metrics[0], metrics[1], metrics[2], metrics[3]);
                    extracted_features.add(feature);

                    cnt++;
                }

                FeatureMetricComparator comparator1 = new FeatureMetricComparator(FeatureMetric.FCONFIDENCE);
                FeatureMetricComparator comparator2 = new FeatureMetricComparator(FeatureMetric.RCONFIDENCE);
                List<Comparator> comparators = new ArrayList<>(Arrays.asList(comparator1,comparator2));

                System.out.println(extracted_features.size());
                extracted_features = Utils.getFeatureFuzzyParetoFront(extracted_features,comparators,2);
                out = formatFeatureOutput(extracted_features);

            }else{
                throw new NotImplementedException();
            }

        }catch(Exception TException ){
            TException.printStackTrace();
        }

        return out;
    }
}