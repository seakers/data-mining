/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.local;

import aos.aos.AOSMOEA;
import aos.creditassignment.setimprovement.SetImprovementDominance;
import aos.operator.AOSVariation;
import aos.operator.AOSVariationSI;
import aos.operatorselectors.AdaptivePursuit;
import aos.operatorselectors.OperatorSelector;
import ifeed.architecture.AbstractArchitecture;
import ifeed.feature.Feature;
import ifeed.io.AprioriFeatureIO;
import ifeed.io.InputDatasetReader;
import ifeed.local.params.MOEAParams;
import ifeed.mining.arm.AbstractApriori;
import ifeed.mining.moea.FeatureExtractionInitialization;
import ifeed.mining.moea.FeatureExtractionProblem;
import ifeed.mining.moea.InstrumentedSearch;
import ifeed.mining.moea.MOEABase;
import ifeed.mining.moea.operators.FeatureMutation;
import ifeed.mining.moea.operators.gptype.BranchSwapCrossover;
import ifeed.ontology.OntologyManager;
import ifeed.problem.assigning.Apriori;
import ifeed.problem.assigning.LocalSearch;
import ifeed.problem.assigning.MOEA;
import ifeed.problem.assigning.Params;
import ifeed.problem.assigning.logicOperators.generalization.*;
import org.moeaframework.algorithm.EpsilonMOEA;
import org.moeaframework.core.*;
import org.moeaframework.core.comparator.DominanceComparator;
import org.moeaframework.core.comparator.ParetoDominanceComparator;
import org.moeaframework.core.operator.GAVariation;
import org.moeaframework.core.operator.TournamentSelection;
import org.moeaframework.util.TypedProperties;

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

public class DataMiningTest2018Fall {

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
        RUN_MODE mode = RUN_MODE.Apriori;
        String path = System.getProperty("user.dir");
        int numCPU = 1;
        int numRuns = 30;

        //PATH
        if (args.length != 0) {
            if(args[0].equalsIgnoreCase("1")){
                mode = RUN_MODE.MOEA;
            }else{
                mode = RUN_MODE.AOS_with_branch_swap_crossover;
            }
            numCPU = Integer.parseInt(args[1]);
            numRuns = Integer.parseInt(args[2]);
        }

        String runName = "";
        String timestamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm").format(new Date());

        if(runName.isEmpty()){
            runName = timestamp;

        }else{
            runName = runName + "_" + timestamp;
        }

        // Set params obejct
        OntologyManager manager = new OntologyManager(path + File.separator + "ontology", "ClimateCentric");
        Params params = new Params();
        params.setOntologyManager(manager);
        params.setInstrumentList(instrumentList);
        params.setOrbitList(orbitList);

        // Set path to the input data file
        String inputDataFile = path + File.separator + "data" + File.separator + "fuzzy_pareto_7.selection";
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

        System.out.println("Path set to " + path);
        System.out.println("Will get " + numCPU + " resources");
        System.out.println("Will do " + numRuns + " runs");

        pool = Executors.newFixedThreadPool(numCPU);
        futures = new ArrayList<>(numRuns);

        // Settings for AbstractApriori algorithm
        double supp = 0.158;
        double conf = 0.40;

        //parameters and operators for search
        TypedProperties properties = new TypedProperties();

        // Add description of the run
        if(mode == RUN_MODE.AOS_with_branch_swap_crossover){
            properties.setString("description","AOS with generalization and simplification operators");

        }else if(mode == RUN_MODE.MOEA){
            properties.setString("description","MOEA");

        }else if(mode == RUN_MODE.Apriori){
            properties.setString("description","AbstractApriori");

            properties.setDouble("supportThreshold", supp);
            properties.setDouble("confidenceThreshold", conf);
        }

        //setup for saving results
        properties.setBoolean("saveQuality", true);
        properties.setBoolean("saveCredits", true);
        properties.setBoolean("saveSelection", true);

        //search paramaters set here
        int popSize = 400;
        int maxEvals = 100000;
        properties.setInt("maxEvaluations", maxEvals);
        properties.setInt("populationSize", popSize);

        double crossoverProbability = 1.0;
        double mutationProbability = 0.90;
        properties.setDouble("mutationProbability", mutationProbability);
        properties.setDouble("crossoverProbability", crossoverProbability);

        //setup for epsilon MOEA
        DominanceComparator comparator = new ParetoDominanceComparator();
        double[] epsilonDouble = new double[]{0.025, 0.025, 1};
        //final TournamentSelection selection = new TournamentSelection(2, comparator);
        //ChainedComparator comparator = new ChainedComparator(new ParetoObjectiveComparator());

        TournamentSelection selection = new TournamentSelection(2, comparator);

        switch (mode) {
            case AOS_with_branch_swap_crossover:
                for (int i = 0; i < numRuns; i++) {

                    MOEABase base = new MOEA(params, architectures, behavioral, non_behavioral);
                    base.saveResult();
                    base.setLocalSearch(new LocalSearch(params, null, architectures, behavioral, non_behavioral));

                    Problem problem = new FeatureExtractionProblem(base, 1, MOEAParams.numberOfObjectives);
                    Initialization initialization = new FeatureExtractionInitialization(problem, popSize, "random");

                    // Knowledge-independent operators
                    List<Variation> operators = new ArrayList<>();
                    Variation mutation = new FeatureMutation(mutationProbability, base);
                    //Variation crossover = new ifeed.mining.moea.operators.vlctype.CutAndSpliceCrossover(crossoverProbability, base, LogicalConnectiveType.AND);
                    Variation crossover = new BranchSwapCrossover(crossoverProbability, base);
                    Variation gaVariation = new GAVariation(crossover, mutation);

                    // Generalization operators
                    Variation instrumentGeneralizer = new GAVariation(new InstrumentGeneralizer(params, base), mutation);
                    Variation orbitGeneralizer = new GAVariation(new OrbitGeneralizer(params, base), mutation);
//                    Variation sharedInstrument2Present = new GAVariation(new ifeed.problem.assigning.logicOperators.generalization.SharedInstrument2Present(params, base), mutation);
//                    Variation sharedInstrument2Absent = new GAVariation(new ifeed.problem.assigning.logicOperators.generalization.SharedInstrument2Absent(params, base), mutation);
//                    Variation inOrbit2PresentVariation = new GAVariation(new InOrbit2Present(params, base), mutation);
//                    Variation notInOrbit2EmptyOrbitVariation = new GAVariation(new NotInOrbit2EmptyOrbit(params, base), mutation);
                    Variation sharedInstrument2Present = new GAVariation(new ifeed.problem.assigning.logicOperators.generalizationPlusCondition.SharedInstrument2Present(params, base), mutation);
                    Variation sharedInstrument2Absent = new GAVariation(new ifeed.problem.assigning.logicOperators.generalizationPlusCondition.SharedInstrument2Absent(params, base), mutation);

                    operators.add(gaVariation);
                    operators.add(sharedInstrument2Absent);
                    operators.add(sharedInstrument2Present);
                    operators.add(instrumentGeneralizer);
                    operators.add(orbitGeneralizer);

                    double pmin = 0.09;
                    properties.setDouble("pmin", pmin);
                    properties.setDouble("epsilon", epsilonDouble[0]);

                    //initialize population structure for algorithm
                    Population population = new Population();
                    EpsilonBoxDominanceArchive archive = new EpsilonBoxDominanceArchive(epsilonDouble);

                    // Create operator selector
                    OperatorSelector operatorSelector = new AdaptivePursuit(operators, 0.8, 0.8, pmin);

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

            case MOEA:
                for (int i = 0; i < numRuns; i++) {

                    MOEABase base = new MOEA(params, architectures, behavioral, non_behavioral);
                    base.saveResult();
                    Problem problem = new FeatureExtractionProblem(base, 1, MOEAParams.numberOfObjectives);
                    Initialization initialization = new FeatureExtractionInitialization(problem, popSize, "random");

                    Variation mutation = new FeatureMutation(mutationProbability, base);
                    Variation crossover = new BranchSwapCrossover(crossoverProbability, base);
                    Variation gaVariation = new GAVariation(crossover, mutation);

                    properties.setDouble("epsilon",epsilonDouble[0]);

                    //initialize population structure for algorithm
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

            case Apriori:

                boolean useOnlyInputFeatures = false;

                if(useOnlyInputFeatures){
                    params.setUseOnlyInputFeatures();
                }

                Apriori arm = new Apriori(params, architectures, behavioral, non_behavioral, supp, conf, 1.0);

                List<Feature> features = arm.run();

                String savePath = path + File.separator + "results" + File.separator + runName;
                String filename = savePath + File.separator + AbstractApriori.class.getSimpleName() + "_" + runName;

                AprioriFeatureIO featureIO = new AprioriFeatureIO(params, properties);
                featureIO.saveFeaturesCSV(  filename + ".all_features" , features, true);

//                // Constrain the number of base features
//                public static boolean adjustRuleSize = false;
//
//                // Maximum number of iterations for adjusting the number of rules based on a given support threshold
//                public static int adjustRuleSizeMaxIter = 30;
//
//                // Number of rules required
//                public static int minRuleNum = 100;
//                public static int maxRuleNum = 1000;
//
//                // Maximum length of features
//                public static int maxLength = 2;
//
//                // Sorting metric
//                public static FeatureMetric sortBy = FeatureMetric.FCONFIDENCE;
//
//                // Use only inOrbit and notInOrbit
//                public static boolean use_only_primitive_features = false;

//                File file = new File(filename);
//                System.out.println("Writing configuration into a file");
//
//                try (FileWriter writer = new FileWriter(file)) {
//
//                    int populationSize = ((AbstractEvolutionaryAlgorithm) alg).getPopulation().size();
//                    int archiveSize = ((AbstractEvolutionaryAlgorithm) alg).getArchive().size();
//                    int maxEvals = properties.getInt("maxEvaluations", -1);
//
//                    double mutationProbability = properties.getDouble("mutationProbability",-1.0);
//                    double crossoverProbability = properties.getDouble("crossoverProbability",-1.0);
//
//                    double pmin = properties.getDouble("pmin", -1);
//                    double epsilon = properties.getDouble("epsilon", -1);
//
//                    StringJoiner content = new StringJoiner("\n");
//                    content.add("populationSize: " + populationSize);
//                    content.add("archiveSize: " + archiveSize);
//                    content.add("maxEvaluations: " + maxEvals);
//                    content.add("mutationProbability: " + mutationProbability);
//                    content.add("crossoverProbability: " + crossoverProbability);
//                    content.add("executionTime: " + executionTime);
//
//                    if(pmin > 0){
//                        content.add("pmin: " + pmin);
//                    }
//                    if(epsilon > 0){
//                        content.add("epsilon: " + epsilon);
//                    }
//
//                    writer.append(content.toString());
//                    writer.flush();
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }

                break;

            default:
                throw new UnsupportedOperationException();
        }
    }

    public enum RUN_MODE{
        AOS_with_branch_swap_crossover,
        MOEA,
        Apriori,
    }
}
