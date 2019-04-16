package ifeed.server;

import java.io.File;
import java.util.*;

import ifeed.*;
import ifeed.architecture.AbstractArchitecture;
import ifeed.architecture.BinaryInputArchitecture;
import ifeed.architecture.DiscreteInputArchitecture;
import ifeed.architecture.ContinuousInputArchitecture;
import ifeed.feature.*;
import ifeed.feature.logic.Connective;
import ifeed.feature.logic.LogicalConnectiveType;
import ifeed.local.params.BaseParams;
import ifeed.mining.AbstractDataMiningAlgorithm;
import ifeed.mining.AbstractLocalSearch;
import ifeed.mining.arm.AbstractAssociationRuleMining;
import ifeed.mining.moea.AbstractMOEABase;
import ifeed.ontology.OntologyManager;
import ifeed.problem.assigning.FeatureSimplifier;
import ifeed.problem.partitioningAndAssigning.GPMOEA;

public class DataMiningInterfaceHandler implements DataMiningInterface.Iface {

    private String path;

    private Map<String, OntologyManager> ontologyManagerMap;

    private Map<String, AssigningProblemEntities> assigningProblemEntitiesMap;
    private Map<String, AssigningProblemEntities> assigningProblemGeneralizedConceptsMap;

    private HashMap<String, BaseParams> paramsMap;

    public DataMiningInterfaceHandler(){

        this.path = System.getProperty("user.dir");

        // Initialize a mappings between problems and their corresponding classes
        this.paramsMap = new HashMap<>();
        this.ontologyManagerMap = new HashMap<>();

        this.assigningProblemEntitiesMap = new HashMap<>();
        this.assigningProblemGeneralizedConceptsMap = new HashMap<>();

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
                    throw new UnsupportedOperationException();
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
    public boolean setAssigningProblemEntities(String problem, AssigningProblemEntities entities){
        this.assigningProblemEntitiesMap.put(problem, entities);
        return true;
    }

    @Override
    public boolean setAssigningProblemGeneralizedConcepts(String problem, AssigningProblemEntities generalizedConcepts){
        this.assigningProblemGeneralizedConceptsMap.put(problem, generalizedConcepts);
        return true;
    }

    @Override
    public AssigningProblemEntities getAssigningProblemEntities(String problem){

        if (this.assigningProblemEntitiesMap.containsKey(problem)) {

            AssigningProblemEntities entities = this.assigningProblemEntitiesMap.get(problem);

            if(this.assigningProblemGeneralizedConceptsMap.containsKey(problem)){

                List<String> leftSet = new ArrayList<>();
                List<String> rightSet = new ArrayList<>();

                for(String entity: entities.getLeftSet()){
                    leftSet.add(entity);
                }
                for(String entity: entities.getRightSet()){
                    rightSet.add(entity);
                }

                AssigningProblemEntities generalizedConcepts = this.assigningProblemGeneralizedConceptsMap.get(problem);
                for(String entity: generalizedConcepts.getLeftSet()){
                    leftSet.add(entity);
                }
                for(String entity: generalizedConcepts.getRightSet()){
                    rightSet.add(entity);
                }

                AssigningProblemEntities combined = new AssigningProblemEntities(leftSet, rightSet);

                entities = combined;
            }

            return entities;

        } else {
            throw new IllegalStateException("setAssigningProblemEntities() needs to be called first for \'" + problem + "\' problem.");
        }
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
                out = new ifeed.problem.assigning.Apriori(params, maxFeatureLength, architectures, behavioral, non_behavioral, supp, conf, lift);
                break;
            case "SMAP":
                out = new ifeed.problem.assigning.Apriori(params, maxFeatureLength, architectures, behavioral, non_behavioral, supp, conf, lift);
                break;
            case "GNC":
                out = new ifeed.problem.gnc.Apriori(params, maxFeatureLength, architectures, behavioral, non_behavioral, supp, conf, lift);
                break;
            case "Decadal2017Aerosols":
                out = new ifeed.problem.partitioningAndAssigning.Apriori(params, maxFeatureLength, architectures, behavioral, non_behavioral, supp, conf, lift);
                break;
            default:
                throw new UnsupportedOperationException();
        }
        return out;
    }

    private AbstractLocalSearch getLocalSearch(String problem,
                                                BaseParams params,
                                                String rootFeatureExpression,
                                                LogicalConnectiveType logic,
                                                List<AbstractArchitecture> architectures,
                                                List<Integer> behavioral,
                                                List<Integer> non_behavioral){
        AbstractLocalSearch out;
        switch (problem) {
            case "ClimateCentric":
                out = new ifeed.problem.assigning.LocalSearch(params, rootFeatureExpression, logic, architectures, behavioral, non_behavioral);
                break;
            case "SMAP":
                out = new ifeed.problem.assigning.LocalSearch(params, rootFeatureExpression, logic, architectures, behavioral, non_behavioral);
                break;
            case "GNC":
                out = new ifeed.problem.gnc.LocalSearch(params, rootFeatureExpression, logic, architectures, behavioral, non_behavioral);
                break;
            case "Decadal2017Aerosols":
                out = new ifeed.problem.partitioningAndAssigning.LocalSearch(params, rootFeatureExpression, logic, architectures, behavioral, non_behavioral);
                break;
            default:
                throw new UnsupportedOperationException();
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
                out = new ifeed.problem.assigning.RuleSetMOEA(params, architectures, behavioral, non_behavioral);
                break;
            case "SMAP":
                out = new ifeed.problem.assigning.GPMOEA(params, architectures, behavioral, non_behavioral);
                break;
            case "GNC":
                out = new ifeed.problem.gnc.GPMOEA(params, architectures, behavioral, non_behavioral);
                break;
            case "Decadal2017Aerosols":
                out = new GPMOEA(params, architectures, behavioral, non_behavioral);
                break;
            case "Constellation_10":
                out = new ifeed.problem.constellation.GPMOEA(params, architectures, behavioral, non_behavioral);
                break;
            case "Constellation_variable":
                out = new ifeed.problem.constellation.GPMOEA(params, architectures, behavioral, non_behavioral);
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

    private AbstractFeatureGeneralizer getFeatureGeneralizer(String problem,
                                                             BaseParams params,
                                                             List<AbstractArchitecture> architectures,
                                                             List<Integer> behavioral,
                                                             List<Integer> non_behavioral){

        AbstractFeatureGeneralizer out;
        switch (problem) {
            case "ClimateCentric":
                out = new ifeed.problem.assigning.FeatureGeneralizer(params, architectures, behavioral, non_behavioral, this.getOntologyManager(problem));
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
        for(int i = 0; i < data_mining_output_features.size(); i++){
            
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

            String description = null;
            if(f instanceof ifeed.feature.FeatureWithDescription){
                description = ((ifeed.feature.FeatureWithDescription) f).getDescription();
            }
            out.add(new ifeed.server.Feature(i,name,expression,metrics, complexity, description));
        }
        return out;
    }

    @Override
    public List<Feature> getDrivingFeaturesBinary(String problem, List<Integer> behavioral, List<Integer> non_behavioral,
            List<ifeed.server.BinaryInputArchitecture> inputArchs, double supp, double conf, double lift){

        List<Feature> out = new ArrayList<>();
        List<ifeed.feature.Feature> extracted_features;
        
        try{
            BaseParams params = getParams(problem);

            List<AbstractArchitecture> archs = formatArchitectureInputBinary(inputArchs);

            // Initialize DrivingFeaturesGenerator
            AbstractAssociationRuleMining data_mining = getAssociationRuleMining(problem, params, archs, behavioral,non_behavioral,supp,conf,lift);

            // Run data mining
            extracted_features = data_mining.run();
            FeatureMetricComparator comparator1 = new FeatureMetricComparator(FeatureMetric.PRECISION);
            FeatureMetricComparator comparator2 = new FeatureMetricComparator(FeatureMetric.RECALL);
            List<Comparator> comparators = new ArrayList<>(Arrays.asList(comparator1,comparator2));
            extracted_features = Utils.getFeatureFuzzyParetoFront(extracted_features,comparators,3);
            out = formatFeatureOutput(extracted_features);
            
        }catch(Exception TException){
            TException.printStackTrace();
        }
        
        return out;
    }

    @Override
    public List<Feature> getMarginalDrivingFeaturesBinary(String problem, List<Integer> behavioral, List<Integer> non_behavioral,
            List<ifeed.server.BinaryInputArchitecture> all_archs, String featureExpression, String logicalConnective, double supp, double conf, double lift){

        // Feature: {id, name, expression, metrics}
        List<Feature> out = new ArrayList<>();

        try{
            BaseParams params = getParams(problem);

            // If Ontology exists
            if(this.ontologyManagerMap.containsKey(problem)){
                params.setOntologyManager(this.ontologyManagerMap.get(problem));

                if(problem.equalsIgnoreCase("ClimateCentric")){

                    ifeed.problem.assigning.Params assigningParams = (ifeed.problem.assigning.Params) params;

                    List<String> instrumentList = this.assigningProblemEntitiesMap.get(problem).leftSet;
                    List<String> orbitList = this.assigningProblemEntitiesMap.get(problem).rightSet;
                    assigningParams.setLeftSet(instrumentList);
                    assigningParams.setRightSet(orbitList);

                    if(this.assigningProblemGeneralizedConceptsMap.containsKey(problem)){
                        AssigningProblemEntities entities = this.assigningProblemGeneralizedConceptsMap.get(problem);

                        for(String concept: entities.getLeftSet()){
                            assigningParams.addLeftSetGeneralizedConcept(concept);
                        }
                        for(String concept: entities.getRightSet()){
                            assigningParams.addRightSetGeneralizedConcept(concept);
                        }
                    }
                }
            }

            List<AbstractArchitecture> archs = formatArchitectureInputBinary(all_archs);

            LogicalConnectiveType logic;
            if(logicalConnective.equalsIgnoreCase("OR")){
                logic = LogicalConnectiveType.OR;
            }else{
                logic = LogicalConnectiveType.AND;
            }

            AbstractLocalSearch localSearch = getLocalSearch(problem, params, featureExpression, logic, archs, behavioral, non_behavioral);

            List<ifeed.feature.Feature> extracted_features = localSearch.run();
            FeatureMetricComparator comparator1 = new FeatureMetricComparator(FeatureMetric.PRECISION);
            FeatureMetricComparator comparator2 = new FeatureMetricComparator(FeatureMetric.RECALL);
            List<Comparator> comparators = new ArrayList<>(Arrays.asList(comparator1,comparator2));
            extracted_features = Utils.getFeatureFuzzyParetoFront(extracted_features,comparators,2);

            if(problem.equalsIgnoreCase("ClimateCentric")){

                List<ifeed.feature.Feature> simplified_features = new ArrayList<>();
                FeatureSimplifier simplifier = new FeatureSimplifier(params, (ifeed.problem.assigning.FeatureFetcher) localSearch.getFeatureFetcher());

                for(ifeed.feature.Feature feat: extracted_features){
                    String expression = feat.getName();
                    Connective root = localSearch.getFeatureHandler().generateFeatureTree(expression);

                    boolean modified = simplifier.simplify(root);
                    if(modified){
                        simplified_features.add(new ifeed.feature.Feature(root.getName(), feat.getMatches(), feat.getSupport(), feat.getLift(), feat.getPrecision(), feat.getRecall(), feat.getAlgebraicComplexity()));
                    }else{
                        simplified_features.add(feat);
                    }
                }
                extracted_features = simplified_features;
            }

            out = formatFeatureOutput(extracted_features);

        }catch(Exception TException){
            TException.printStackTrace();
        }

        return out;
    }

    @Override
    public List<Feature> getDrivingFeaturesDiscrete(String problem, List<Integer> behavioral, List<Integer> non_behavioral,
                                                  List<ifeed.server.DiscreteInputArchitecture> all_archs, double supp, double conf, double lift){

        List<Feature> out = new ArrayList<>();

        List<ifeed.feature.Feature> extracted_features;

        try{
            BaseParams params = getParams(problem);

            List<AbstractArchitecture> archs = formatArchitectureInputDiscrete(all_archs);

            // Initialize DrivingFeaturesGenerator
            AbstractAssociationRuleMining data_mining = getAssociationRuleMining(problem, params, archs, behavioral,non_behavioral,supp,conf,lift);

            // Run data mining
            extracted_features = data_mining.run();

            FeatureMetricComparator comparator1 = new FeatureMetricComparator(FeatureMetric.PRECISION);
            FeatureMetricComparator comparator2 = new FeatureMetricComparator(FeatureMetric.RECALL);
            List<Comparator> comparators = new ArrayList<>(Arrays.asList(comparator1,comparator2));
            extracted_features = Utils.getFeatureFuzzyParetoFront(extracted_features,comparators,3);

            out = formatFeatureOutput(extracted_features);

        }catch(Exception TException){
            TException.printStackTrace();
        }

        return out;
    }

    @Override
    public List<Feature> getMarginalDrivingFeaturesDiscrete(String problem,
                                                            List<Integer> behavioral,
                                                            List<Integer> non_behavioral,
                                                            List<ifeed.server.DiscreteInputArchitecture> all_archs,
                                                            String featureExpression,
                                                            String logicalConnective,
                                                            double supp, double conf, double lift
    ){

        // A feature is defined from: {id, name, expression, metrics}
        List<Feature> out = new ArrayList<>();

        try{
            BaseParams params = getParams(problem);

            List<AbstractArchitecture> archs = formatArchitectureInputDiscrete(all_archs);

            LogicalConnectiveType logic;
            if(logicalConnective.equalsIgnoreCase("OR")){
                logic = LogicalConnectiveType.OR;
            }else{
                logic = LogicalConnectiveType.AND;
            }

            // Initialize DrivingFeaturesGenerator
            AbstractLocalSearch data_mining = getLocalSearch(problem, params, featureExpression, logic, archs, behavioral, non_behavioral);

            List<ifeed.feature.Feature> extracted_features = data_mining.run();
            System.out.println(extracted_features.size());

            FeatureMetricComparator comparator1 = new FeatureMetricComparator(FeatureMetric.PRECISION);
            FeatureMetricComparator comparator2 = new FeatureMetricComparator(FeatureMetric.RECALL);
            List<Comparator> comparators = new ArrayList<>(Arrays.asList(comparator1,comparator2));

            extracted_features = Utils.getFeatureFuzzyParetoFront(extracted_features,comparators,2);
            out = formatFeatureOutput(extracted_features);

        }catch(Exception TException){
            TException.printStackTrace();
        }

        return out;
    }


    @Override
    public List<Feature> getDrivingFeaturesContinuous(String problem, List<Integer> behavioral, List<Integer> non_behavioral,
                                                  List<ifeed.server.ContinuousInputArchitecture> inputArchs, double supp, double conf, double lift){

        List<Feature> out = new ArrayList<>();
        List<ifeed.feature.Feature> extracted_features;

        try{
            BaseParams params = getParams(problem);

            List<AbstractArchitecture> archs = formatArchitectureInputContinuous(inputArchs);

            // Initialize DrivingFeaturesGenerator
            AbstractAssociationRuleMining data_mining = getAssociationRuleMining(problem, params, archs, behavioral,non_behavioral,supp,conf,lift);

            // Run data mining
            extracted_features = data_mining.run();
            FeatureMetricComparator comparator1 = new FeatureMetricComparator(FeatureMetric.PRECISION);
            FeatureMetricComparator comparator2 = new FeatureMetricComparator(FeatureMetric.RECALL);
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
    public List<Feature> getDrivingFeaturesEpsilonMOEABinary(String problem, List<Integer> behavioral, List<Integer> non_behavioral,
                                                  List<ifeed.server.BinaryInputArchitecture> all_archs){

        List<Feature> out = new ArrayList<>();
        List<ifeed.feature.Feature> extracted_features;

        try{
            System.out.println("EpsilonMOEA called");

            BaseParams params = getParams(problem);

            if(problem.equalsIgnoreCase("ClimateCentric")){
                ifeed.problem.assigning.Params assigningParams = (ifeed.problem.assigning.Params) params;
                assigningParams.setLeftSet(this.assigningProblemEntitiesMap.get(problem).leftSet);
                assigningParams.setRightSet(this.assigningProblemEntitiesMap.get(problem).rightSet);
            }

            List<AbstractArchitecture> archs = formatArchitectureInputBinary(all_archs);

            // Initialize DrivingFeaturesGenerator
            AbstractDataMiningAlgorithm data_mining = getMOEA(problem, params, archs, behavioral, non_behavioral);

            // Run data mining
            extracted_features = data_mining.run();

            FeatureMetricComparator comparator1 = new FeatureMetricComparator(FeatureMetric.PRECISION);
            FeatureMetricComparator comparator2 = new FeatureMetricComparator(FeatureMetric.RECALL);
            List<Comparator> comparators = new ArrayList<>(Arrays.asList(comparator1,comparator2));
            extracted_features = Utils.getFeatureFuzzyParetoFront(extracted_features,comparators,3);

            if(problem.equalsIgnoreCase("ClimateCentric")){

                AbstractMOEABase base = (AbstractMOEABase) data_mining;

                List<ifeed.feature.Feature> simplified_features = new ArrayList<>();

                FeatureSimplifier simplifier = new FeatureSimplifier(params, (ifeed.problem.assigning.FeatureFetcher)base.getFeatureFetcher());

                for(ifeed.feature.Feature feat: extracted_features){
                    String expression = feat.getName();
                    Connective root = base.getFeatureHandler().generateFeatureTree(expression);

                    boolean modified = simplifier.simplify(root);
                    if(modified){
                        simplified_features.add(new ifeed.feature.Feature(root.getName(), feat.getMatches(), feat.getSupport(), feat.getLift(), feat.getPrecision(), feat.getRecall(), feat.getAlgebraicComplexity()));
                    }else{
                        simplified_features.add(feat);
                    }
                }

                extracted_features = simplified_features;
            }

            out = formatFeatureOutput(extracted_features);

        }catch(Exception TException ){
            TException.printStackTrace();
        }

        return out;
    }

    @Override
    public List<Feature> getDrivingFeaturesEpsilonMOEADiscrete(String problem, List<Integer> behavioral, List<Integer> non_behavioral,
                                                       List<ifeed.server.DiscreteInputArchitecture> all_archs){

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

            FeatureMetricComparator comparator1 = new FeatureMetricComparator(FeatureMetric.PRECISION);
            FeatureMetricComparator comparator2 = new FeatureMetricComparator(FeatureMetric.RECALL);
            List<Comparator> comparators = new ArrayList<>(Arrays.asList(comparator1,comparator2));
            extracted_features = Utils.getFeatureFuzzyParetoFront(extracted_features,comparators,3);

            out = formatFeatureOutput(extracted_features);

        }catch(Exception TException){
            TException.printStackTrace();
        }

        return out;
    }


    @Override
    public List<Feature> getDrivingFeaturesEpsilonMOEAContinuous(String problem, List<Integer> behavioral, List<Integer> non_behavioral,
                                                             List<ifeed.server.ContinuousInputArchitecture> all_archs){

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

            FeatureMetricComparator comparator1 = new FeatureMetricComparator(FeatureMetric.PRECISION);
            FeatureMetricComparator comparator2 = new FeatureMetricComparator(FeatureMetric.RECALL);
            List<Comparator> comparators = new ArrayList<>(Arrays.asList(comparator1,comparator2));
            extracted_features = Utils.getFeatureFuzzyParetoFront(extracted_features,comparators,3);

            out = formatFeatureOutput(extracted_features);

        }catch(Exception TException ){
            TException.printStackTrace();
        }

        return out;
    }


    @Override
    public List<Feature> getDrivingFeaturesWithGeneralizationBinary(String problem, List<Integer> behavioral, List<Integer> non_behavioral,
                                                                    List<ifeed.server.BinaryInputArchitecture> all_archs){

        List<Feature> out = new ArrayList<>();
//        List<ifeed.feature.Feature> extracted_features;
//
//        try{
//            System.out.println("EpsilonMOEA with single");
//
//            List<AbstractArchitecture> archs = formatArchitectureInputBinary(all_archs);
//
//            // Generalization-enabled problem
//            if(problem.equalsIgnoreCase("ClimateCentric")){
//
//                List<String> instrumentList = this.assigningProblemParametersMap.get(problem).instrumentList;
//                List<String> orbitList = this.assigningProblemParametersMap.get(problem).orbitList;
//                String[] orbitNameArray = orbitList.toArray(new String[orbitList.size()]);
//                String[] instrumentNameArray = instrumentList.toArray(new String[instrumentList.size()]);
//
//                BaseParams params = getParams(problem);
//                ifeed.problem.assigning.GPMOEA assigningMOEA = new ifeed.problem.assigning.GPMOEA(params, archs, behavioral, non_behavioral);
//                assigningMOEA.setMode(ifeed.problem.assigning.GPMOEA.RUN_MODE.AOS);
//                assigningMOEA.setOrbitList(orbitNameArray);
//                assigningMOEA.setInstrumentList(instrumentNameArray);
//                assigningMOEA.setOntologyManager(getOntologyManager(problem));
//
//                // Run data mining
//                extracted_features = assigningMOEA.run();
//
//                orbitNameArray = assigningMOEA.getOrbitList();
//                instrumentNameArray = assigningMOEA.getInstrumentList();
//                orbitList = new ArrayList<>();
//                instrumentList = new ArrayList<>();
//                for(int i = 0; i < orbitNameArray.length; i++){
//                    orbitList.add(orbitNameArray[i]);
//                }
//                for(int i = 0; i < instrumentNameArray.length; i++){
//                    instrumentList.add(instrumentNameArray[i]);
//                }
//                this.assigningProblemExtendedParametersMap.put(problem, new AssigningProblemParameters(orbitList, instrumentList));
//
//            }else{
//                throw new UnsupportedOperationException();
//            }
//
//            FeatureMetricComparator comparator1 = new FeatureMetricComparator(FeatureMetric.PRECISION);
//            FeatureMetricComparator comparator2 = new FeatureMetricComparator(FeatureMetric.RECALL);
//            List<Comparator> comparators = new ArrayList<>(Arrays.asList(comparator1,comparator2));
//            extracted_features = Utils.getFeatureFuzzyParetoFront(extracted_features,comparators,3);
//            out = formatFeatureOutput(extracted_features);
//
//        }catch(Exception TException ){
//            TException.printStackTrace();
//        }

        return out;
    }

    @Override
    public FlattenedConceptHierarchy getAssigningProblemConceptHierarchy(String problem, AssigningProblemEntities params){

        OntologyManager manager = getOntologyManager(problem);
        List<String> orbitList = params.getRightSet();
        List<String> instrumentList = params.getLeftSet();

        Set<String> ignoredClassNames = new HashSet<>();
        ignoredClassNames.add("Thing");
        ignoredClassNames.add("Orbit");
        ignoredClassNames.add("Instrument");

        for(String orbit: orbitList){
            if(!manager.getSuperclassMap().containsKey(orbit)){
                manager.getSuperClasses(orbit, ignoredClassNames);
            }
        }
        for(String instrument: instrumentList){
            if(!manager.getSuperclassMap().containsKey(instrument)){
                manager.getSuperClasses(instrument, ignoredClassNames);
            }
        }
        Map<String, List<String>> superclassesMap = manager.getSuperclassMap();
        Map<String, List<String>> instancesMap = manager.getInstanceMap();
        FlattenedConceptHierarchy conceptHierarchy = new FlattenedConceptHierarchy(instancesMap, superclassesMap);
        return conceptHierarchy;
    }

    @Override
    public List<Feature> generalizeFeatureBinary(String problem,
                                           java.util.List<Integer> behavioral,
                                           java.util.List<Integer> non_behavioral,
                                           java.util.List<ifeed.server.BinaryInputArchitecture> all_archs,
                                           String rootFeatureExpression,
                                           String nodeFeatureExpression
    ){

        // Output: String explanation, String modifiedExpression
        List<Feature> out = new ArrayList<>();

        Set<ifeed.feature.FeatureWithDescription> extractedFeatures;

        try{
            List<AbstractArchitecture> architectures = formatArchitectureInputBinary(all_archs);

            BaseParams params = getParams(problem);

            // Generalization-enabled problem
            if(problem.equalsIgnoreCase("ClimateCentric")){

                ifeed.problem.assigning.Params assigningParams = (ifeed.problem.assigning.Params) params;
                assigningParams.setOntologyManager(getOntologyManager(problem));

                List<String> instrumentList = this.assigningProblemEntitiesMap.get(problem).leftSet;
                List<String> orbitList = this.assigningProblemEntitiesMap.get(problem).rightSet;
                assigningParams.setLeftSet(instrumentList);
                assigningParams.setRightSet(orbitList);

                if(this.assigningProblemGeneralizedConceptsMap.containsKey(problem)){
                    AssigningProblemEntities entities = this.assigningProblemGeneralizedConceptsMap.get(problem);

                    for(String concept: entities.getLeftSet()){
                        assigningParams.addLeftSetGeneralizedConcept(concept);
                    }
                    for(String concept: entities.getRightSet()){
                        assigningParams.addRightSetGeneralizedConcept(concept);
                    }
                }

                AbstractFeatureGeneralizer generalizer = this.getFeatureGeneralizer(problem, params, architectures, behavioral, non_behavioral);

                extractedFeatures = generalizer.generalize(rootFeatureExpression, nodeFeatureExpression);

                System.out.println("Generalized features found: " + extractedFeatures.size());

                List<ifeed.feature.Feature> extractedFeaturesList = new ArrayList<>();
                for(ifeed.feature.Feature feature: extractedFeatures){
                    extractedFeaturesList.add(feature);
                }

                out = formatFeatureOutput(extractedFeaturesList);

                if(!assigningParams.getRightSetGeneralizedConcepts().isEmpty() || !assigningParams.getLeftSetGeneralizedConcepts().isEmpty()){
                    List<String> leftSet = new ArrayList<>();
                    List<String> rightSet = new ArrayList<>();
                    for(String concept: assigningParams.getLeftSetGeneralizedConcepts()){
                        leftSet.add(concept);
                    }
                    for(String concept: assigningParams.getRightSetGeneralizedConcepts()){
                        rightSet.add(concept);
                    }
                    assigningProblemGeneralizedConceptsMap.put(problem, new AssigningProblemEntities(leftSet, rightSet));
                }

            }else{
                throw new UnsupportedOperationException();
            }

        }catch(Exception TException ){
            TException.printStackTrace();
        }

        return out;
    }
}