/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.local;

import ifeed.architecture.AbstractArchitecture;
import ifeed.feature.Feature;
import ifeed.io.InputDatasetReader;
import ifeed.local.params.MOEAParams;
import ifeed.mining.arm.AbstractApriori;
import ifeed.mining.arm.AbstractFPGrowth;
import ifeed.mining.moea.FeatureExtractionInitialization;
import ifeed.mining.moea.FeatureExtractionProblem;
import ifeed.mining.moea.GPMOEABase;
import ifeed.mining.moea.InstrumentedSearch;
import ifeed.mining.moea.operators.FeatureMutation;
import ifeed.mining.moea.operators.GPType.BranchSwapCrossover;
import ifeed.mining.moea.operators.RuleSetType.CutAndSpliceCrossover;
import ifeed.ontology.OntologyManager;
import ifeed.problem.assigning.*;
import ifeed.problem.assigning.logicOperators.generalization.single.*;
import org.moeaframework.algorithm.EpsilonMOEA;
import org.moeaframework.core.*;
import org.moeaframework.core.comparator.DominanceComparator;
import org.moeaframework.core.comparator.ParetoDominanceComparator;
import org.moeaframework.core.operator.CompoundVariation;
import org.moeaframework.core.operator.GAVariation;
import org.moeaframework.core.operator.TournamentSelection;
import org.moeaframework.util.TypedProperties;
import seakers.aos.aos.AOSMOEA;
import seakers.aos.creditassignment.setimprovement.SetImprovementDominance;
import seakers.aos.operator.AOSVariation;
import seakers.aos.operator.AOSVariationSI;
import seakers.aos.operatorselectors.AdaptivePursuit;
import seakers.aos.operatorselectors.OperatorSelector;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 *
 * @author hsbang
 */

public class DataMiningWithGeneralization2019Summer {

    // Instruments and orbits
    public static String[] instrumentList = {
            "ACE_ORCA","ACE_POL","ACE_LID",
            "CLAR_ERB","ACE_CPR","DESD_SAR",
            "DESD_LID","GACM_VIS","GACM_SWIR",
            "HYSP_TIR","POSTEPS_IRS","CNES_KaRIN"};

    public static String[] orbitList = {"LEO-600-polar-NA","SSO-600-SSO-AM","SSO-600-SSO-DD","SSO-800-SSO-DD","SSO-800-SSO-PM"};

    /**
     * pool of resources
     */
    private static ExecutorService pool;

    /**
     * List of future tasks to perform
     */
    private static ArrayList<Future<Algorithm>> futures;

    /**
     * First argument is the path to the project folder. Second argument is the
     * mode. Third argument is the number of ArchitecturalEvaluators to
     * initialize.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        // Basic setups
        RUN_MODE mode = RUN_MODE.MOEA_RULESET;
        String path = System.getProperty("user.dir");
        int numCPU = 1;
        int numRuns = 1;

        // Settings for Association rule mining algorithms
        int maxFeatureLength = 2;
        double supp = 0.05;
        double conf = 0.2;

        // Settings for MOEA paramaters
        int popSize = 400;
        int maxEvals = 100000;
        double crossoverProbability = 1.0;
        double mutationProbability = 0.90;
        double pmin = 0.05;
        double[] epsilonDouble = new double[]{0.025, 0.025, 1};

        // Set run name
        String runName = "";
        String timestamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm").format(new Date());
        if(runName.isEmpty()){
            runName = timestamp;
        }else{
            runName = runName + "_" + timestamp;
        }

        // Set params obejct
        OntologyManager manager = new OntologyManager(path + File.separator + "ontology", 3);
        Params params = new Params();
        params.setOntologyManager(manager);
        params.setLeftSet(instrumentList);
        params.setRightSet(orbitList);

        // Set path to the input data file
//        String inputDataFile = path + File.separator + "data" + File.separator + "fuzzy_pareto_7.selection";
//        String inputDataFile = path + File.separator + "data" + File.separator + "experiment_tutorial_data.selection";
//        String inputDataFile = path + File.separator + "data" + File.separator + "6655_fp4.selection";
        String inputDataFile = path + File.separator + "data" + File.separator + "6655_fuzzy8_24_7500.selection";

        InputDatasetReader reader = new InputDatasetReader(inputDataFile);
        reader.setInputType(InputDatasetReader.InputType.BINARY_BITSTRING);
        reader.setColumnInfo(InputDatasetReader.ColumnType.CLASSLABEL,1);
        reader.setColumnInfo(InputDatasetReader.ColumnType.DECISION, 2);
        reader.readData();

        List<AbstractArchitecture> architectures = reader.getArchs();
        BitSet label = reader.getLabel();
        List<Integer> behavioral = new ArrayList<>();
        List<Integer> non_behavioral = new ArrayList<>();
        for(int i = 0; i < architectures.size(); i++){
            if(label.get(i)){
                behavioral.add(i);
            }else{
                non_behavioral.add(i);
            }
        }

        pool = Executors.newFixedThreadPool(numCPU);
        futures = new ArrayList<>(numRuns);

        //parameters and operators for search
        TypedProperties properties = new TypedProperties();

        // Add description of the run
        if(mode == RUN_MODE.AOS_GP){
            properties.setString("description","AOS with generalization operators - GP");

        }else if(mode == RUN_MODE.MOEA_GP){
            properties.setString("description","MOEA_GP");

        }else if(mode == RUN_MODE.AOS_RULESET){
            properties.setString("description","AOS with generalization operators - rule set");

        }else if(mode == RUN_MODE.MOEA_RULESET){
            properties.setString("description","MOEA_RULESET");

        } else if(mode == RUN_MODE.APRIORI){
            properties.setString("description","APRIORI algorithm");

        }else if(mode == RUN_MODE.FP_GROWTH){
            properties.setString("description","FP_GROWTH algorithm");

        }else if(mode == RUN_MODE.FP_GROWTH_WITH_GENERALIZED_VARIABLES){
            properties.setString("description","FP_GROWTH algorithm with generalized variables");
        }

        System.out.println("Path set to " + path);
        if( mode == RUN_MODE.APRIORI || mode == RUN_MODE.FP_GROWTH || mode == RUN_MODE.FP_GROWTH_WITH_GENERALIZED_VARIABLES) {
            properties.setDouble("supportThreshold", supp);
            properties.setDouble("confidenceThreshold", conf);
            properties.setInt("maxFeatureLength", maxFeatureLength);

        }else if(mode == RUN_MODE.MOEA_GP || mode == RUN_MODE.AOS_GP
                || mode == RUN_MODE.MOEA_RULESET || mode == RUN_MODE.AOS_RULESET){
            System.out.println("Will get " + numCPU + " resources");
            System.out.println("Will do " + numRuns + " runs");

            //setup for saving results
            properties.setBoolean("saveQuality", true);
            properties.setBoolean("saveCredits", true);
            properties.setBoolean("saveSelection", true);

            properties.setInt("maxEvaluations", maxEvals);
            properties.setInt("populationSize", popSize);

            properties.setDouble("mutationProbability", mutationProbability);
            properties.setDouble("crossoverProbability", crossoverProbability);
        }

        //setup for epsilon MOEA
        DominanceComparator comparator = new ParetoDominanceComparator();
        //final TournamentSelection selection = new TournamentSelection(2, comparator);
        //ChainedComparator comparator = new ChainedComparator(new ParetoObjectiveComparator());
        TournamentSelection selection = new TournamentSelection(2, comparator);

        switch (mode) {
            case AOS_GP:
                for (int i = 0; i < numRuns; i++) {

                    GPMOEABase base = new GPMOEA(params, architectures, behavioral, non_behavioral);
                    base.init();
                    base.saveResult();

                    Problem problem = new FeatureExtractionProblemWithSimplification(base, 1, MOEAParams.numberOfObjectives, base.getFeatureHandler());
                    Initialization initialization = new FeatureExtractionInitialization(problem, popSize, "random");

                    // Knowledge-independent operators
                    List<Variation> operators = new ArrayList<>();
                    Variation mutation = new FeatureMutation(mutationProbability, base);
                    Variation crossover = new BranchSwapCrossover(crossoverProbability, base);
                    Variation gaVariation = new GAVariation(crossover, mutation);
                    operators.add(gaVariation);

                    // Generalization applied after mutation
                    // Variable-generalization operators
                    CompoundVariation instrumentGeneralizer = new CompoundVariation(mutation, new InstrumentGeneralizer(params, base));
                    CompoundVariation orbitGeneralizer = new CompoundVariation(mutation, new OrbitGeneralizer(params, base));
                    instrumentGeneralizer.setName("InstrumentGeneralizer");
                    orbitGeneralizer.setName("OrbitGeneralizer");
                    operators.add(instrumentGeneralizer);
                    operators.add(orbitGeneralizer);

                    // Feature-generalization operators
                    CompoundVariation inOrbit2Present = new CompoundVariation(mutation, new InOrbit2Present(params, base));
                    CompoundVariation inOrbit2Together = new CompoundVariation(mutation, new InOrbit2Together(params, base));
                    CompoundVariation notInOrbit2Absent = new CompoundVariation(mutation, new NotInOrbit2Absent(params, base));
                    CompoundVariation notInOrbit2EmptyOrbit = new CompoundVariation(mutation, new NotInOrbit2EmptyOrbit(params, base));
                    CompoundVariation separate2Absent = new CompoundVariation(mutation, new Separate2Absent(params, base));
                    inOrbit2Present.setName("InOrbit2Present");
                    inOrbit2Together.setName("InOrbits2Together");
                    notInOrbit2Absent.setName("NotInOrbit2Absent");
                    notInOrbit2EmptyOrbit.setName("NotInOrbit2EmptyOrbit");
                    separate2Absent.setName("Separate2Absent");
                    operators.add(inOrbit2Present);
                    operators.add(inOrbit2Together);
                    operators.add(notInOrbit2Absent);
                    operators.add(notInOrbit2EmptyOrbit);
                    operators.add(separate2Absent);

                    properties.setDouble("pmin", pmin);
                    properties.setDouble("epsilon", epsilonDouble[0]);

                    // Create operator selector
                    OperatorSelector operatorSelector = new AdaptivePursuit(operators, 0.8, 0.8, pmin);
//                    OperatorSelector operatorSelector = new RandomSelect(operators);

                    if(operatorSelector instanceof AdaptivePursuit) {
                        properties.setString("selector", "aos");
                    }else{
                        properties.setString("selector", "random");
                    }

                    // Save operator names
                    StringJoiner sj = new StringJoiner(",");
                    for(Variation operator: operators){

                        String operatorName;
                        if(operator instanceof CompoundVariation){
                            operatorName = ((CompoundVariation)operator).getName();

                        }else{
                            String[] str = operator.toString().split("operator.");
                            String[] splitName = str[str.length - 1].split("@");
                            operatorName = splitName[0];
                        }
                        sj.add(operatorName);
                    }
                    properties.setString("operators", sj.toString());


                    //initialize samples structure for algorithm
                    Population population = new Population();
                    EpsilonBoxDominanceArchive archive = new EpsilonBoxDominanceArchive(epsilonDouble);

                    // Create credit assigning
                    SetImprovementDominance creditAssignment = new SetImprovementDominance(archive, 1, 0);

                    // Create AOS strategy
                    AOSVariation aosStrategy = new AOSVariationSI(operatorSelector, creditAssignment, popSize);

                    EpsilonMOEA emoea = new EpsilonMOEA(problem, population, archive, selection, aosStrategy, initialization, comparator);

                    AOSMOEA aos = new AOSMOEA(emoea, aosStrategy, false);

                    InstrumentedSearch run = new ifeed.problem.assigning.InstrumentedSearch(aos, properties, path + File.separator + "results" + File.separator + runName, runName + "_" + String.valueOf(i), base);

                    futures.add(pool.submit(run));
                }

                for (Future<Algorithm> run : futures) {
                    try {
                        Algorithm alg = run.get();

                    } catch (InterruptedException | ExecutionException ex) {
                        Logger.getLogger("main").log(Level.SEVERE, null, ex);
                    }
                }

                pool.shutdown();
                break;

            case MOEA_GP:
                for (int i = 0; i < numRuns; i++) {

                    GPMOEABase base = new GPMOEA(params, architectures, behavioral, non_behavioral);
                    base.saveResult();
                    Problem problem = new FeatureExtractionProblem(base, 1, MOEAParams.numberOfObjectives);
                    Initialization initialization = new FeatureExtractionInitialization(problem, popSize, "random");

                    Variation mutation = new FeatureMutation(mutationProbability, base);
                    Variation crossover = new BranchSwapCrossover(crossoverProbability, base);
                    Variation gaVariation = new GAVariation(crossover, mutation);

                    properties.setDouble("epsilon",epsilonDouble[0]);

                    //initialize samples structure for algorithm
                    Population population = new Population();
                    EpsilonBoxDominanceArchive archive = new EpsilonBoxDominanceArchive(epsilonDouble);

                    Algorithm eMOEA = new EpsilonMOEA(problem, population, archive, selection, gaVariation, initialization);

                    InstrumentedSearch run;

                    run = new InstrumentedSearch(eMOEA, properties, path + File.separator + "results" + File.separator + runName, runName + "_" + String.valueOf(i), base);
                    futures.add(pool.submit(run));
                }

                for (Future<Algorithm> run : futures) {
                    try {
                        run.get();

                    } catch (InterruptedException | ExecutionException ex) {
                        Logger.getLogger(EOSSMOEA.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                pool.shutdown();
                break;

            case AOS_RULESET:
                for (int i = 0; i < numRuns; i++) {
                    RuleSetMOEA base = new RuleSetMOEA(params, architectures, behavioral, non_behavioral);
                    base.init();
                    base.saveResult();

                    Problem problem = new FeatureExtractionProblemWithSimplification(base, 1, MOEAParams.numberOfObjectives, base.getFeatureHandler());
                    Initialization initialization = new FeatureExtractionInitialization(problem, popSize, "random");

                    // Knowledge-independent operators
                    List<Variation> operators = new ArrayList<>();
                    Variation mutation = new FeatureMutation(mutationProbability, base);
                    Variation crossover = new CutAndSpliceCrossover(crossoverProbability, base);
                    Variation gaVariation = new GAVariation(crossover, mutation);
                    operators.add(gaVariation);

                    // Generalization applied after mutation
                    // Variable-generalization operators
                    CompoundVariation instrumentGeneralizer = new CompoundVariation(mutation, new InstrumentGeneralizer(params, base));
                    CompoundVariation orbitGeneralizer = new CompoundVariation(mutation, new OrbitGeneralizer(params, base));
                    instrumentGeneralizer.setName("InstrumentGeneralizer");
                    orbitGeneralizer.setName("OrbitGeneralizer");
                    operators.add(instrumentGeneralizer);
                    operators.add(orbitGeneralizer);

                    properties.setDouble("pmin", pmin);
                    properties.setDouble("epsilon", epsilonDouble[0]);

                    // Create operator selector
                    OperatorSelector operatorSelector = new AdaptivePursuit(operators, 0.8, 0.8, pmin);
//                    OperatorSelector operatorSelector = new RandomSelect(operators);

                    if(operatorSelector instanceof AdaptivePursuit) {
                        properties.setString("selector", "aos");
                    }else{
                        properties.setString("selector", "random");
                    }

                    // Save operator names
                    StringJoiner sj = new StringJoiner(",");
                    for(Variation operator: operators){
                        String operatorName;
                        if(operator instanceof CompoundVariation){
                            operatorName = ((CompoundVariation)operator).getName();

                        }else{
                            String[] str = operator.toString().split("operator.");
                            String[] splitName = str[str.length - 1].split("@");
                            operatorName = splitName[0];
                        }
                        sj.add(operatorName);
                    }
                    properties.setString("operators", sj.toString());

                    //initialize sample structure for algorithm
                    Population population = new Population();
                    EpsilonBoxDominanceArchive archive = new EpsilonBoxDominanceArchive(epsilonDouble);

                    // Create credit assigning
                    SetImprovementDominance creditAssignment = new SetImprovementDominance(archive, 1, 0);

                    // Create AOS strategy
                    AOSVariation aosStrategy = new AOSVariationSI(operatorSelector, creditAssignment, popSize);

                    EpsilonMOEA emoea = new EpsilonMOEA(problem, population, archive, selection, aosStrategy, initialization, comparator);

                    AOSMOEA aos = new AOSMOEA(emoea, aosStrategy, false);

                    InstrumentedSearch run = new ifeed.problem.assigning.InstrumentedSearch(aos, properties, path + File.separator + "results" + File.separator + runName, runName + "_" + String.valueOf(i), base);

                    futures.add(pool.submit(run));
                }

                for (Future<Algorithm> run : futures) {
                    try {
                        Algorithm alg = run.get();

                    } catch (InterruptedException | ExecutionException ex) {
                        Logger.getLogger("main").log(Level.SEVERE, null, ex);
                    }
                }

                pool.shutdown();
                break;

            case MOEA_RULESET:
                for (int i = 0; i < numRuns; i++) {
                    RuleSetMOEA base = new RuleSetMOEA(params, architectures, behavioral, non_behavioral);
                    base.init();
                    base.saveResult();

                    Problem problem = new FeatureExtractionProblemWithSimplification(base, 1, MOEAParams.numberOfObjectives, base.getFeatureHandler());
                    Initialization initialization = new FeatureExtractionInitialization(problem, popSize, "random");

                    Variation mutation = new FeatureMutation(mutationProbability, base);
                    Variation crossover = new CutAndSpliceCrossover(crossoverProbability, base);
                    Variation gaVariation = new GAVariation(crossover, mutation);

                    properties.setDouble("epsilon", epsilonDouble[0]);

                    //initialize samples structure for algorithm
                    Population population = new Population();
                    EpsilonBoxDominanceArchive archive = new EpsilonBoxDominanceArchive(epsilonDouble);

                    Algorithm eMOEA = new EpsilonMOEA(problem, population, archive, selection, gaVariation, initialization);

                    InstrumentedSearch run = new InstrumentedSearch(eMOEA, properties, path + File.separator + "results" + File.separator + runName, runName + "_" + String.valueOf(i), base);
                    futures.add(pool.submit(run));
                }

                for (Future<Algorithm> run : futures) {
                    try {
                        run.get();

                    } catch (InterruptedException | ExecutionException ex) {
                        Logger.getLogger(EOSSMOEA.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                pool.shutdown();
                break;


            case MOEA_RULESET_WITH_GENERALIZED_VARIABLES:
                for (int i = 0; i < numRuns; i++) {

                    RuleSetMOEA base = new RuleSetMOEA(params, architectures, behavioral, non_behavioral);
                    base.setUseGeneralizedVariables();
                    base.saveResult();

                    Problem problem = new FeatureExtractionProblemWithCoverageCount(base, 1, MOEAParams.numberOfObjectives);
                    Initialization initialization = new FeatureExtractionInitialization(problem, popSize, "random");

                    Variation mutation = new FeatureMutation(mutationProbability, base);
                    Variation crossover = new CutAndSpliceCrossover(crossoverProbability, base);
                    Variation gaVariation = new GAVariation(crossover, mutation);

                    properties.setDouble("epsilon", epsilonDouble[0]);

                    //initialize samples structure for algorithm
                    Population population = new Population();
                    EpsilonBoxDominanceArchive archive = new EpsilonBoxDominanceArchive(epsilonDouble);

                    Algorithm eMOEA = new EpsilonMOEA(problem, population, archive, selection, gaVariation, initialization);

                    ifeed.problem.assigning.InstrumentedSearch run = new ifeed.problem.assigning.InstrumentedSearch(eMOEA, properties, path + File.separator + "results" + File.separator + runName, runName + "_" + String.valueOf(i), base);
                    futures.add(pool.submit(run));
                }

                for (Future<Algorithm> run : futures) {
                    try {
                        run.get();

                    } catch (InterruptedException | ExecutionException ex) {
                        Logger.getLogger(EOSSMOEA.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                pool.shutdown();
                break;

            case APRIORI:

                boolean useOnlyInputFeatures = false;

                if(useOnlyInputFeatures){
                    params.setUseOnlyInputFeatures();
                }

                Apriori arm = new Apriori(params, maxFeatureLength, architectures, behavioral, non_behavioral, supp, conf, 1.0);

                List<Feature> features = arm.run();

                String dirname = path + File.separator + "results" + File.separator + runName;
                String filename = dirname + File.separator + AbstractApriori.class.getSimpleName() + "_" + runName;

//                List<Feature> subsetOfFeatures = new ArrayList<>();
//                Random random = new Random();
//                for(int i = 0; i < features.size(); i++){
//                    if(random.nextDouble() < 0.3){
//                        subsetOfFeatures.add(features.get(i));
//                    }
//                }

//                ARMFeatureIO featureIO = new ARMFeatureIO(params, properties);
//                featureIO.saveFeaturesCSV(  filename + ".all_features" , features, true, true);

                //featureIO.saveFeaturesCSV(  filename + ".all_features" , features, true);

                break;

            case FP_GROWTH:

//                FP_GROWTH fpGrowth = new FP_GROWTH(params, maxFeatureLength, architectures, behavioral, non_behavioral, supp, conf, 1.0);
//
//                dirname = path + File.separator + "results" + File.separator + runName;
//                filename = dirname + File.separator + FP_GROWTH.class.getSimpleName() + "_" + runName;
//
//                fpGrowth.setSaveData(properties, filename);
//                fpGrowth.run();

                ArrayList<Future<AbstractFPGrowth>> futures = new ArrayList<>(numCPU);
                for (int i = 0; i < numCPU; i++) {

                    FPGrowth fpGrowth = new FPGrowth(params, maxFeatureLength, architectures, behavioral, non_behavioral, supp, conf, 1.0);

                    dirname = path + File.separator + "results" + File.separator + runName;
                    filename = dirname + File.separator + FPGrowth.class.getSimpleName() + "_" + runName;

                    fpGrowth.setSaveData(properties, filename);
                    fpGrowth.setRunIndex(i, numCPU);
                    futures.add(pool.submit(fpGrowth));
                }

                for (Future<AbstractFPGrowth> run : futures) {
                    try {
                        run.get();

                    } catch (InterruptedException | ExecutionException ex) {
                        Logger.getLogger(EOSSMOEA.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                pool.shutdown();
                break;

            case FP_GROWTH_WITH_GENERALIZED_VARIABLES:

                FPGrowthWithGeneralizedVariables fpGrowthWithGeneralizedVariables = new FPGrowthWithGeneralizedVariables(params, maxFeatureLength, architectures, behavioral, non_behavioral, supp, conf, 1.0);

                dirname = path + File.separator + "results" + File.separator + runName;
                filename = dirname + File.separator + FPGrowth.class.getSimpleName() + "_" + runName;

                fpGrowthWithGeneralizedVariables.setSaveData(properties, filename);
                fpGrowthWithGeneralizedVariables.run();

                break;

            default:
                throw new UnsupportedOperationException();
        }
    }

    public enum RUN_MODE{
        AOS_GP,
        AOS_RULESET,
        MOEA_GP,
        MOEA_RULESET,
        MOEA_RULESET_WITH_GENERALIZED_VARIABLES,
        APRIORI,
        FP_GROWTH,
        FP_GROWTH_WITH_GENERALIZED_VARIABLES,
    }
}
