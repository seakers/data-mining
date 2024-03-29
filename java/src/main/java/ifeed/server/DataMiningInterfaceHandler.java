package ifeed.server;

import java.io.File;
import java.util.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
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
import ifeed.mining.InteractiveSearch;
import ifeed.mining.arm.AbstractAssociationRuleMining;
import ifeed.mining.moea.AbstractMOEABase;
import ifeed.ontology.OntologyManager;
import ifeed.problem.assigning.FeatureFetcher;
import ifeed.problem.assigning.FeatureGeneralizer;
import ifeed.problem.assigning.FeatureSimplifier;
import ifeed.problem.partitioningAndAssigning.GPMOEA;

public class DataMiningInterfaceHandler implements DataMiningInterface.Iface {

    private String path;
    private Map<Integer, OntologyManager> ontologyManagerMap;
    private Map<String, AssigningProblemEntities> assigningProblemEntitiesMap;
    private Map<String, AssigningProblemEntities> assigningProblemGeneralizedConceptsMap;
    private HashMap<String, BaseParams> paramsMap;
    private Map<String, InteractiveSearch> interactiveSearchMap;

    private final int CLIMATE_CENTRIC_ID = 3;  

    public DataMiningInterfaceHandler(){
        this.path = System.getProperty("user.dir");

        // Initialize a mappings between problems and their corresponding classes
        this.paramsMap = new HashMap<>();
        this.ontologyManagerMap = new HashMap<>();

        this.assigningProblemEntitiesMap = new HashMap<>();
        this.assigningProblemGeneralizedConceptsMap = new HashMap<>();
        this.interactiveSearchMap = new HashMap<>();

        this.ontologyManagerMap.put(CLIMATE_CENTRIC_ID, new OntologyManager(path + File.separator + "ontology", CLIMATE_CENTRIC_ID));
    }

    public int stopSearch(String session){
        if(this.interactiveSearchMap.containsKey(session)){
            if(!this.interactiveSearchMap.get(session).getExitFlag()){
                System.out.println("Interrupting the search in session: " + session);
                this.interactiveSearchMap.get(session).stop();
                this.interactiveSearchMap.remove(session);
                return 0;
            }
        }
        return 1;
    }

    private BaseParams getParams(String problem_type) {
        if (paramsMap.keySet().contains(problem_type)) {
            return paramsMap.get(problem_type);
        }
        else {
            BaseParams out;
            switch (problem_type) {
                case "assignation":
                    out = new ifeed.problem.assigning.Params();
                    break;
                case "discrete":
                    out = new ifeed.problem.partitioningAndAssigning.Params();
                    break;
                case "GNC":
                    out = new ifeed.problem.gnc.Params();
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

    private OntologyManager getOntologyManager(int problem_id){
        OntologyManager out;
        if (this.ontologyManagerMap.containsKey(problem_id)) {
            out = this.ontologyManagerMap.get(problem_id);
        }
        else {
            out = this.ontologyManagerMap.put(problem_id, new OntologyManager(path + File.separator + "ontology", problem_id));
        }
        return out;
    }

    // AWS Adapted: in progress
    @Override
    public boolean setAssigningProblemEntities(String session, int problem_id, AssigningProblemEntities entities){
        String key = session + "_" + problem_id;
        System.out.println("setAssigningProblemEntities(): " + key);
        this.assigningProblemEntitiesMap.put(key, entities);
        if (this.assigningProblemGeneralizedConceptsMap.containsKey(key)) {
            this.assigningProblemGeneralizedConceptsMap.remove(key);
        }
        this.sendAssigningProblemEntities(session, problem_id);
        return true;
    }

    @Override
    public boolean setAssigningProblemGeneralizedConcepts(String session, int problem_id, AssigningProblemEntities generalizedConcepts){
        String key = session + "_" + problem_id;
        System.out.println("setAssigningProblemGeneralizedConcepts(): " + key);
        this.assigningProblemGeneralizedConceptsMap.put(key, generalizedConcepts);
        this.sendAssigningProblemEntities(session, problem_id);
        return true;
    }

    @Override
    public AssigningProblemEntities getAssigningProblemEntities(String session, String problem){
        System.out.println("getAssigningProblemEntities()");
        String key = session + "_" + problem;
        if (this.assigningProblemEntitiesMap.containsKey(key)) {
            AssigningProblemEntities entities = this.assigningProblemEntitiesMap.get(key);

            if(this.assigningProblemGeneralizedConceptsMap.containsKey(key)){
                List<String> leftSet = new ArrayList<>();
                List<String> rightSet = new ArrayList<>();
                for(String entity: entities.getLeftSet()){
                    leftSet.add(entity);
                }
                for(String entity: entities.getRightSet()){
                    rightSet.add(entity);
                }
                AssigningProblemEntities generalizedConcepts = this.assigningProblemGeneralizedConceptsMap.get(key);
                for(String entity: generalizedConcepts.getLeftSet()){
                    leftSet.add(entity);
                }
                for(String entity: generalizedConcepts.getRightSet()){
                    rightSet.add(entity);
                }
                AssigningProblemEntities extendedEntities = new AssigningProblemEntities(leftSet, rightSet);
                entities = extendedEntities;
            }
            return entities;

        } else {
            throw new IllegalStateException("AssigningProblemEntities not found for session " + session);
        }
    }

    @Override
    public FlattenedConceptHierarchy getAssigningProblemConceptHierarchy(String session, int problem_id, AssigningProblemEntities params){
        OntologyManager manager = getOntologyManager(problem_id);
        List<String> rightSet = params.getRightSet();
        List<String> leftSet = params.getLeftSet();

        Set<String> ignoredClassNames = new HashSet<>();
        if(problem_id == CLIMATE_CENTRIC_ID){
            ignoredClassNames.add("Thing");
            ignoredClassNames.add("Orbit");
            ignoredClassNames.add("Instrument");
        }

        Set<String> classsNames = new HashSet<>();
        for(String entity: rightSet){
            classsNames.addAll(manager.getSuperClasses(entity, ignoredClassNames));
        }
        for(String entity: leftSet){
            classsNames.addAll(manager.getSuperClasses(entity, ignoredClassNames));
        }
        for(String className: classsNames){
            manager.getIndividuals(className);
        }

        Map<String, List<String>> superclassesMap = manager.getSuperclassMap();
        Map<String, List<String>> instancesMap = manager.getInstanceMap();

        FlattenedConceptHierarchy conceptHierarchy = new FlattenedConceptHierarchy(instancesMap, superclassesMap);
        return conceptHierarchy;
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
        out.init();
        return out;
    }

    private AbstractDataMiningAlgorithm getMOEA(String problem_type,
                                    BaseParams params,
                                    List<AbstractArchitecture> architectures,
                                    List<Integer> behavioral,
                                    List<Integer> non_behavioral){

        AbstractDataMiningAlgorithm out;
        switch (problem_type) {
            case "assignation":
                out = new ifeed.problem.assigning.RuleSetMOEA(params, architectures, behavioral, non_behavioral);
                break;
            case "discrete":
                out = new GPMOEA(params, architectures, behavioral, non_behavioral);
                break;
            case "GNC":
                out = new ifeed.problem.gnc.GPMOEA(params, architectures, behavioral, non_behavioral);
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
            case "SMAP_JPL1":
            case "SMAP_JPL2":
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
            if(f.getComplexity() == Double.NaN){
                complexity = -1.0;
            }else{
                complexity = f.getComplexity();
            }

            String description = null;
            if(f instanceof ifeed.feature.FeatureWithDescription){
                description = ((ifeed.feature.FeatureWithDescription) f).getDescription();
            }
            out.add(new ifeed.server.Feature(i,name,expression,metrics, complexity, description));
        }
        return out;
    }

    // AWS Adapted: in progress
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
    public int getMarginalDrivingFeaturesBinary(String session, String problem,
                                                          List<Integer> behavioral, List<Integer> non_behavioral,
                                                          List<ifeed.server.BinaryInputArchitecture> all_archs,
                                                          String featureExpression,
                                                          String logicalConnective
    ){
        String key = session + "_" + problem;

        try{
            BaseParams params = getParams(problem);

            // If Ontology exists
            if(this.ontologyManagerMap.containsKey(problem)){
                params.setOntologyManager(this.ontologyManagerMap.get(problem));

                if(problem.equalsIgnoreCase("ClimateCentric")){
                    ifeed.problem.assigning.Params assigningParams = (ifeed.problem.assigning.Params) params;
                    List<String> instrumentList = this.assigningProblemEntitiesMap.get(key).leftSet;
                    List<String> orbitList = this.assigningProblemEntitiesMap.get(key).rightSet;
                    assigningParams.setLeftSet(instrumentList);
                    assigningParams.setRightSet(orbitList);

                    if(this.assigningProblemGeneralizedConceptsMap.containsKey(key)){
                        AssigningProblemEntities entities = this.assigningProblemGeneralizedConceptsMap.get(key);
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
            }else if(logicalConnective.equalsIgnoreCase("AND")){
                logic = LogicalConnectiveType.AND;
            }else if(logicalConnective.equalsIgnoreCase("BOTH")){
                logic = null;
            }else{
                throw new IllegalStateException();
            }

            AbstractLocalSearch localSearch = getLocalSearch(problem, params, featureExpression, logic, archs, behavioral, non_behavioral);
            InteractiveLocalSearch interactiveSearch = new InteractiveLocalSearch(params, problem, session, localSearch);
            this.interactiveSearchMap.put(session, interactiveSearch);

            Thread generalizationSearchThread = new Thread(interactiveSearch);
            generalizationSearchThread.start();

        }catch(Exception TException){
            TException.printStackTrace();
            return 1;
        }
        return 0;
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
    public List<Feature> getMarginalDrivingFeaturesDiscrete(String session,
                                                            String problem,
                                                            List<Integer> behavioral,
                                                            List<Integer> non_behavioral,
                                                            List<ifeed.server.DiscreteInputArchitecture> all_archs,
                                                            String featureExpression,
                                                            String logicalConnective
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
            out = expressionHandler.convertToCNFJBool(expression);

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
            out = expressionHandler.convertToDNFJBool(expression);

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
    public List<Feature> getDrivingFeaturesEpsilonMOEABinary(String session,
                                                             int problem_id,
                                                             String problem_type,
                                                             List<Integer> behavioral,
                                                             List<Integer> non_behavioral,
                                                             List<ifeed.server.BinaryInputArchitecture> all_archs){

        List<Feature> out = new ArrayList<>();
        List<ifeed.feature.Feature> extracted_features;
        String key = session + "_" + problem_id;

        try {
            System.out.println("EpsilonMOEA called");

            BaseParams params = getParams(problem_type);

            if (problem_type.equals("assignation")) {
                ifeed.problem.assigning.Params assigningParams = (ifeed.problem.assigning.Params) params;
                assigningParams.setLeftSet(this.assigningProblemEntitiesMap.get(key).leftSet);
                assigningParams.setRightSet(this.assigningProblemEntitiesMap.get(key).rightSet);
            }
            List<AbstractArchitecture> archs = formatArchitectureInputBinary(all_archs);

            // Initialize DrivingFeaturesGenerator
            AbstractDataMiningAlgorithm data_mining = getMOEA(problem_type, params, archs, behavioral, non_behavioral);

            // Run data mining
            extracted_features = data_mining.run();

            FeatureMetricComparator comparator1 = new FeatureMetricComparator(FeatureMetric.PRECISION);
            FeatureMetricComparator comparator2 = new FeatureMetricComparator(FeatureMetric.RECALL);
            List<Comparator> comparators = new ArrayList<>(Arrays.asList(comparator1, comparator2));
            extracted_features = Utils.getFeatureFuzzyParetoFront(extracted_features, comparators, 3);

            if (problem_type.equals("assignation")) {
                AbstractMOEABase base = (AbstractMOEABase) data_mining;
                List<ifeed.feature.Feature> simplified_features = new ArrayList<>();

                FeatureSimplifier simplifier = new FeatureSimplifier(params, (ifeed.problem.assigning.FeatureFetcher)base.getFeatureFetcher());

                for(ifeed.feature.Feature feat: extracted_features){
                    String expression = feat.getName();
                    Connective root = base.getFeatureHandler().generateFeatureTree(expression);

                    boolean modified = simplifier.simplify(root);
                    if(modified){
                        simplified_features.add(new ifeed.feature.Feature(root.getName(), feat.getMatches(), feat.getSupport(), feat.getLift(), feat.getPrecision(), feat.getRecall(), feat.getComplexity()));
                    }else{
                        simplified_features.add(feat);
                    }
                }
                extracted_features = simplified_features;
            }
            out = formatFeatureOutput(extracted_features);
        }
        catch (Exception TException) {
            TException.printStackTrace();
        }
        return out;
    }

    @Override
    public List<Feature> getDrivingFeaturesEpsilonMOEADiscrete(String session,
                                                               int problem_id,
                                                               String problem_type,
                                                               List<Integer> behavioral,
                                                               List<Integer> non_behavioral,
                                                               List<ifeed.server.DiscreteInputArchitecture> all_archs){

        List<Feature> out = new ArrayList<>();
        List<ifeed.feature.Feature> extracted_features;

        try{

            System.out.println("EpsilonMOEA called");

            BaseParams params = getParams(problem_type);

            List<AbstractArchitecture> archs = formatArchitectureInputDiscrete(all_archs);

            // Initialize DrivingFeaturesGenerator
            AbstractDataMiningAlgorithm data_mining = getMOEA(problem_type, params, archs, behavioral, non_behavioral);

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
    public List<Feature> getDrivingFeaturesWithGeneralizationBinary(String session, int problem_id, String problem_type, List<Integer> behavioral, List<Integer> non_behavioral,
                                                                    List<ifeed.server.BinaryInputArchitecture> all_archs){
        String key = session + "_" + problem_id;

        List<Feature> out = new ArrayList<>();
        List<ifeed.feature.Feature> extracted_features;
        try{
            System.out.println("EpsilonMOEA with generalization");

            List<AbstractArchitecture> archs = formatArchitectureInputBinary(all_archs);
            BaseParams params = getParams(problem_type);

            // Generalization-enabled problem
            if (problem_id == CLIMATE_CENTRIC_ID) {
                ifeed.problem.assigning.Params assigningParams = (ifeed.problem.assigning.Params) params;
                assigningParams.setOntologyManager(getOntologyManager(problem_id));

                List<String> instrumentList = this.assigningProblemEntitiesMap.get(key).leftSet;
                List<String> orbitList = this.assigningProblemEntitiesMap.get(key).rightSet;
                assigningParams.setLeftSet(instrumentList);
                assigningParams.setRightSet(orbitList);

                if(this.assigningProblemGeneralizedConceptsMap.containsKey(key)){
                    AssigningProblemEntities entities = this.assigningProblemGeneralizedConceptsMap.get(key);
                    for(String concept: entities.getLeftSet()){
                        assigningParams.addLeftSetGeneralizedConcept(concept);
                    }
                    for(String concept: entities.getRightSet()){
                        assigningParams.addRightSetGeneralizedConcept(concept);
                    }
                }

                ifeed.problem.assigning.RuleSetMOEA ruleSetMOEA = new ifeed.problem.assigning.RuleSetMOEA(params, archs, behavioral, non_behavioral);
                ruleSetMOEA.setOntologyManager(getOntologyManager(problem_id));
                ruleSetMOEA.setUseGeneralizedVariables();

                // Run data mining
                extracted_features = ruleSetMOEA.run();

                if(!assigningParams.getRightSetGeneralizedConcepts().isEmpty() || !assigningParams.getLeftSetGeneralizedConcepts().isEmpty()){
                    List<String> leftSet = new ArrayList<>();
                    List<String> rightSet = new ArrayList<>();
                    for(String concept: assigningParams.getLeftSetGeneralizedConcepts()){
                        leftSet.add(concept);
                    }
                    for(String concept: assigningParams.getRightSetGeneralizedConcepts()){
                        rightSet.add(concept);
                    }
                    assigningProblemGeneralizedConceptsMap.put(key, new AssigningProblemEntities(leftSet, rightSet));
                    sendAssigningProblemEntities(session, problem_id);
                }
            }else{
                throw new UnsupportedOperationException();
            }

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
    public String simplifyFeatureExpression(String session, int problem_id, String problem_type, String expression){
        String key = session + "_" + problem_id;
        String out = "";
        try{
            BaseParams params = getParams(problem_type);

            ifeed.problem.assigning.Params assigningParams = (ifeed.problem.assigning.Params) params;
            assigningParams.setOntologyManager(getOntologyManager(problem_id));

            List<String> instrumentList = this.assigningProblemEntitiesMap.get(key).leftSet;
            List<String> orbitList = this.assigningProblemEntitiesMap.get(key).rightSet;
            assigningParams.setLeftSet(instrumentList);
            assigningParams.setRightSet(orbitList);

            if(this.assigningProblemGeneralizedConceptsMap.containsKey(key)){
                AssigningProblemEntities entities = this.assigningProblemGeneralizedConceptsMap.get(key);

                for(String concept: entities.getLeftSet()){
                    assigningParams.addLeftSetGeneralizedConcept(concept);
                }
                for(String concept: entities.getRightSet()){
                    assigningParams.addRightSetGeneralizedConcept(concept);
                }
            }

            FeatureExpressionHandler expressionHandler = new FeatureExpressionHandler();
            expressionHandler.setSkipMatchCalculation(true);
            Connective root = expressionHandler.generateFeatureTree(expression);

            AbstractFeatureSimplifier featureSimplifier;

            if(problem_id == CLIMATE_CENTRIC_ID){
                FeatureFetcher featureFetcher = new FeatureFetcher(params);
                featureSimplifier = new FeatureSimplifier(params, featureFetcher);

            }else{
                throw new IllegalStateException();
            }

            featureSimplifier.simplify(root);
            out = root.getName();

        }catch(Exception TException){
            TException.printStackTrace();
        }
        return out;
    }

    @Override
    public int generalizeFeatureBinary(String session,
                                            int problem_id,
                                            String problem_type,
                                            java.util.List<Integer> behavioral,
                                            java.util.List<Integer> non_behavioral,
                                            java.util.List<ifeed.server.BinaryInputArchitecture> all_archs,
                                            String rootFeatureExpression,
                                            String nodeFeatureExpression

    ) {
        String key = session + "_" + problem_id;

        // Output: String explanation, String modifiedExpression
        List<Feature> out = new ArrayList<>();
        BaseParams params = getParams(problem_type);

        List<AbstractArchitecture> architectures = formatArchitectureInputBinary(all_archs);

        // Generalization-enabled problem
        if (problem_id == CLIMATE_CENTRIC_ID) {

            ifeed.problem.assigning.Params assigningParams = (ifeed.problem.assigning.Params) params;
            OntologyManager ontologyManager = getOntologyManager(problem_id);
            assigningParams.setOntologyManager(ontologyManager);

            List<String> instrumentList = this.assigningProblemEntitiesMap.get(key).leftSet;
            List<String> orbitList = this.assigningProblemEntitiesMap.get(key).rightSet;
            assigningParams.setLeftSet(instrumentList);
            assigningParams.setRightSet(orbitList);

            if (this.assigningProblemGeneralizedConceptsMap.containsKey(key)) {
                AssigningProblemEntities entities = this.assigningProblemGeneralizedConceptsMap.get(key);

                for (String concept : entities.getLeftSet()) {
                    assigningParams.addLeftSetGeneralizedConcept(concept);
                }
                for (String concept : entities.getRightSet()) {
                    assigningParams.addRightSetGeneralizedConcept(concept);
                }
            }

            FeatureGeneralizer generalizer = new ifeed.problem.assigning.FeatureGeneralizer(params, architectures, behavioral, non_behavioral, ontologyManager);

            InteractiveGeneralizationSearch search = new InteractiveGeneralizationSearch(assigningParams, problem_id, session, generalizer, rootFeatureExpression, nodeFeatureExpression);
            this.interactiveSearchMap.put(session, search);

            Thread generalizationSearchThread = new Thread(search);
            generalizationSearchThread.start();

        } else {
            throw new UnsupportedOperationException();
        }
        return 0;
    }

    public void sendAssigningProblemEntities(String session, int problem_id){
        String key = session + "_" + problem_id;
        if (this.assigningProblemEntitiesMap.containsKey(key)) {
            AssigningProblemEntities entities = this.assigningProblemEntitiesMap.get(key);

            boolean generalizedVariableExists = false;

            List<String> leftSet = new ArrayList<>();
            List<String> rightSet = new ArrayList<>();
            if (this.assigningProblemGeneralizedConceptsMap.containsKey(key)) {
                for (String entity: entities.getLeftSet()) {
                    leftSet.add(entity);
                }
                for (String entity: entities.getRightSet()) {
                    rightSet.add(entity);
                }
                AssigningProblemEntities generalizedConcepts = this.assigningProblemGeneralizedConceptsMap.get(key);
                for (String entity: generalizedConcepts.getLeftSet()) {
                    generalizedVariableExists = true;
                    leftSet.add(entity);
                }
                for (String entity: generalizedConcepts.getRightSet()) {
                    generalizedVariableExists = true;
                    rightSet.add(entity);
                }
            }
            else {
                if (problem_id == CLIMATE_CENTRIC_ID) { // TODO: Find out climate-centric in a better way
                    ifeed.problem.assigning.Params assigningParams = (ifeed.problem.assigning.Params) getParams("assignation");
                    assigningParams.setOntologyManager(getOntologyManager(problem_id));
                    assigningParams.setLeftSet(entities.getLeftSet());
                    assigningParams.setRightSet(entities.getRightSet());

                    leftSet.addAll(entities.getLeftSet());
                    for (int i = 0; i < leftSet.size(); i++) {
                        assigningParams.getLeftSetSuperclass(i, false);
                    }
                    rightSet.addAll(entities.getRightSet());
                    for (int i = 0; i < rightSet.size(); i++) {
                        assigningParams.getRightSetSuperclass(i, false);
                    }
                    for (String entity: assigningParams.getLeftSetGeneralizedConcepts()) {
                        generalizedVariableExists = true;
                        leftSet.add(entity);
                    }
                    for (String entity: assigningParams.getRightSetGeneralizedConcepts()) {
                        generalizedVariableExists = true;
                        rightSet.add(entity);
                    }
                    this.assigningProblemGeneralizedConceptsMap.put(key, new AssigningProblemEntities(assigningParams.getLeftSetGeneralizedConcepts(), assigningParams.getRightSetGeneralizedConcepts()));
                }
            }

            JsonObject messageBack = new JsonObject();
            messageBack.addProperty("type", "entities");

            JsonArray leftSetJsonArray = new JsonArray();
            for (String entity: leftSet) {
                leftSetJsonArray.add(entity);
            }
            JsonArray rightSetJsonArray = new JsonArray();
            for (String entity: rightSet) {
                rightSetJsonArray.add(entity);
            }
            messageBack.add("leftSet", leftSetJsonArray);
            messageBack.add("rightSet", rightSetJsonArray);

            if (generalizedVariableExists) {
                OntologyManager manager = getOntologyManager(problem_id);

                Set<String> ignoredClassNames = new HashSet<>();
                if (problem_id == CLIMATE_CENTRIC_ID) {
                    ignoredClassNames.add("Thing");
                    ignoredClassNames.add("Orbit");
                    ignoredClassNames.add("Instrument");
                }

                Set<String> classsNames = new HashSet<>();
                for (String entity: rightSet) {
                    classsNames.addAll(manager.getSuperClasses(entity, ignoredClassNames));
                }
                for (String entity: leftSet) {
                    classsNames.addAll(manager.getSuperClasses(entity, ignoredClassNames));
                }
                for (String className: classsNames) {
                    manager.getIndividuals(className);
                }

                Map<String, List<String>> superclassesMap = manager.getSuperclassMap();
                Map<String, List<String>> instancesMap = manager.getInstanceMap();
                JsonObject superclassMapJson = new JsonObject();
                for (String entity: superclassesMap.keySet()) {
                    JsonArray jsonArray = new JsonArray();
                    for (String cls: superclassesMap.get(entity)) {
                        jsonArray.add(cls);
                    }
                    superclassMapJson.add(entity, jsonArray);
                }
                JsonObject instanceMapJson = new JsonObject();
                for (String cls: instancesMap.keySet()) {
                    JsonArray jsonArray = new JsonArray();
                    for (String indiv: instancesMap.get(cls)) {
                        jsonArray.add(indiv);
                    }
                    instanceMapJson.add(cls, jsonArray);
                }
                messageBack.add("superclassMap", superclassMapJson);
                messageBack.add("instanceMap", instanceMapJson);
            }
            sendMessageQueue(session, "problemSetting", messageBack.toString());
        }
        else {
            throw new IllegalStateException("setAssigningProblemEntities() needs to be called first for \'" + problem_id + "\' problem.");
        }
    }

    private void sendMessageQueue(String sessionKey, String messageType, String message){
        // Message queue
        // Notify listeners of new search starting with the session channel
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(System.getenv("RABBITMQ_HOST")); // System.getenv("VASSAR_HOST")
        String sendbackQueueName = sessionKey + "_" + messageType;
        System.out.println("Queue name: " + sendbackQueueName);

        try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
            channel.queueDeclare(sendbackQueueName, false, false, false, null);
            channel.basicPublish("", sendbackQueueName, null, message.getBytes("UTF-8"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class InteractiveLocalSearch implements Runnable, InteractiveSearch{
        private BaseParams params;
        private String problem;
        private String sessionKey;
        private AbstractLocalSearch localSearch;
        private List<Feature> result;
        private boolean finishedRunning;
        private volatile boolean exit;

        public InteractiveLocalSearch(BaseParams params,
                                       String problem,
                                       String session,
                                       AbstractLocalSearch localSearch
        ){
            this.params = params;
            this.problem = problem;
            this.sessionKey = session;
            this.localSearch = localSearch;
            this.result = new ArrayList<>();
            this.exit = false;
        }

        public void run(){
            sendMessageQueue(this.sessionKey, "localSearch", "{ \"type\": \"search_started\" }");

            String initialFeatureExpression = this.localSearch.getRoot().getName();

            System.out.println("Starting local search for: " + sessionKey);
            this.result = new ArrayList<>();
            this.finishedRunning = false;
            List<ifeed.feature.Feature> extractedFeatures;

            try{
                if(localSearch.getLogic() == null){
                    extractedFeatures = localSearch.runBothLogic();
                }else{
                    extractedFeatures = localSearch.run();
                }
                extractedFeatures = Utils.getOneFeaturePerEpsilonBox(extractedFeatures, 0.03, 0.03);

                FeatureMetricComparator comparator1 = new FeatureMetricComparator(FeatureMetric.PRECISION);
                FeatureMetricComparator comparator2 = new FeatureMetricComparator(FeatureMetric.RECALL);
                List<Comparator> comparators = new ArrayList<>(Arrays.asList(comparator1,comparator2));
                extractedFeatures = Utils.getFeatureFuzzyParetoFront(extractedFeatures,comparators,0);

                if(problem.equalsIgnoreCase("ClimateCentric")){
                    List<ifeed.feature.Feature> simplifiedFeatures = new ArrayList<>();
                    FeatureSimplifier simplifier = new FeatureSimplifier(params, (ifeed.problem.assigning.FeatureFetcher) localSearch.getFeatureFetcher());

                    for(ifeed.feature.Feature feat: extractedFeatures){
                        String expression = feat.getName();
                        Connective root = localSearch.getFeatureHandler().generateFeatureTree(expression);

                        boolean modified = simplifier.simplify(root);
                        if(modified){
                            simplifiedFeatures.add(new ifeed.feature.Feature(root.getName(), feat.getMatches(), feat.getSupport(), feat.getLift(), feat.getPrecision(), feat.getRecall(), feat.getComplexity()));
                        }else{
                            simplifiedFeatures.add(feat);
                        }
                    }
                    extractedFeatures = simplifiedFeatures;
                }

                this.result = formatFeatureOutput(extractedFeatures);

            }catch(Exception TException){
                TException.printStackTrace();
            }

            this.finishedRunning = true;
            this.exit = true;
            System.out.println("Finishing local search for: " + sessionKey);

            JsonObject messageBack = new JsonObject();
            messageBack.addProperty("type","search_finished");
            if(!this.result.isEmpty()){
                JsonArray featuresInJson =  new JsonArray();
                for(Feature feature: this.result){
                    JsonObject featureJson = new JsonObject();
                    JsonArray metrics = new JsonArray();
                    for(double d: feature.getMetrics()){
                        metrics.add(d);
                    }
                    featureJson.addProperty("id", feature.getId());
                    featureJson.addProperty("name", feature.getName());
                    featureJson.addProperty("description", feature.getDescription());
                    featureJson.addProperty("expression", feature.getExpression());
                    featureJson.addProperty("complexity", feature.getComplexity());
                    featureJson.add("metrics", metrics);
                    featuresInJson.add(featureJson);
                }
                messageBack.add("features", featuresInJson);
                messageBack.addProperty("initialFeature", initialFeatureExpression);
            }
            sendMessageQueue(this.sessionKey, "localSearch", messageBack.toString());
        }

        public void stop(){
            this.localSearch.stop();
            this.exit = true;
        }

        public List<Feature> getResult(){
            return this.result;
        }

        public boolean getExitFlag(){ return this.exit; }

        public boolean hasFinishedRunning(){
            return this.finishedRunning;
        }
    }

    class InteractiveGeneralizationSearch implements Runnable, InteractiveSearch{
        private ifeed.problem.assigning.Params params;
        private int problemId;
        private String sessionKey;
        private FeatureGeneralizer generalizer;
        private String rootFeatureExpression;
        private String nodeFeatureExpression;
        private List<Feature> result;
        private boolean finishedRunning;
        private volatile boolean exit;

        public InteractiveGeneralizationSearch(ifeed.problem.assigning.Params params,
                                               int problemId,
                                               String session,
                                               FeatureGeneralizer generalizer,
                                               String rootFeatureExpression,
                                               String nodeFeatureExpression){

            this.params = params;
            this.problemId = problemId;
            this.sessionKey = session;
            this.generalizer = generalizer;
            this.rootFeatureExpression = rootFeatureExpression;
            this.nodeFeatureExpression = nodeFeatureExpression;
            this.result = new ArrayList<>();
            this.exit = false;
        }

        public void run(){
            sendMessageQueue(this.sessionKey, "generalization", "{ \"type\": \"search_started\" }");

            String initialFeatureExpression = rootFeatureExpression;

            System.out.println("Starting generalization search for: " + sessionKey);
            this.result = new ArrayList<>();
            this.finishedRunning = false;
            Set<ifeed.feature.FeatureWithDescription> extractedFeatures;

            try {
                extractedFeatures = generalizer.generalize(rootFeatureExpression, nodeFeatureExpression);

                System.out.println("Number of generalized features found: " + extractedFeatures.size());
                List<ifeed.feature.Feature> extractedFeaturesList = new ArrayList<>();
                for(ifeed.feature.Feature feature: extractedFeatures){
                    extractedFeaturesList.add(feature);
                }
                this.result = formatFeatureOutput(extractedFeaturesList);

                if (!params.getRightSetGeneralizedConcepts().isEmpty() || !params.getLeftSetGeneralizedConcepts().isEmpty()) {
                    List<String> leftSet = new ArrayList<>();
                    List<String> rightSet = new ArrayList<>();
                    for (String concept : params.getLeftSetGeneralizedConcepts()) {
                        leftSet.add(concept);
                    }
                    for (String concept : params.getRightSetGeneralizedConcepts()) {
                        rightSet.add(concept);
                    }
                    String key = sessionKey + "_" + problemId;
                    assigningProblemGeneralizedConceptsMap.put(key, new AssigningProblemEntities(leftSet, rightSet));
                }

            } catch (Exception TException) {
                TException.printStackTrace();
            }

            this.finishedRunning = true;
            this.exit = true;
            System.out.println("Finishing generalization search for: " + sessionKey);

            JsonObject messageBack = new JsonObject();
            messageBack.addProperty("type","search_finished");
            if(!this.result.isEmpty()){
                JsonArray featuresInJson =  new JsonArray();
                for(Feature feature: this.result){
                    JsonObject featureJson = new JsonObject();
                    JsonArray metrics = new JsonArray();
                    for(double d: feature.getMetrics()){
                        metrics.add(d);
                    }
                    featureJson.addProperty("id", feature.getId());
                    featureJson.addProperty("name", feature.getName());
                    featureJson.addProperty("description", feature.getDescription());
                    featureJson.addProperty("expression", feature.getExpression());
                    featureJson.addProperty("complexity", feature.getComplexity());
                    featureJson.add("metrics", metrics);
                    featuresInJson.add(featureJson);
                }
                messageBack.add("features", featuresInJson);
                messageBack.addProperty("initialFeature", initialFeatureExpression);

                sendAssigningProblemEntities(this.sessionKey, problemId);
            }
            sendMessageQueue(this.sessionKey, "generalization", messageBack.toString());
        }

        public void stop(){
            this.generalizer.stop();
            this.exit = true;
        }

        public List<Feature> getResult(){
            return this.result;
        }

        public boolean getExitFlag(){ return this.exit; }

        public boolean hasFinishedRunning(){
            return this.finishedRunning;
        }
    }
}

