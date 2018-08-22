///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package ifeed.local;
//
//import aos.aos.AOSMOEA;
//import aos.creditassignment.setimprovement.SetImprovementDominance;
//import aos.operator.AOSVariation;
//import aos.operator.AOSVariationSI;
//import aos.operatorselectors.OperatorSelector;
//import aos.operatorselectors.AdaptivePursuit;
//import ifeed.architecture.AbstractArchitecture;
//import ifeed.io.InputDatasetReader;
//import ifeed.local.params.MOEAParams;
//import ifeed.mining.moea.FeatureExtractionInitialization;
//import ifeed.mining.moea.FeatureExtractionProblem;
//import ifeed.mining.moea.MOEABase;
//import ifeed.mining.moea.operators.FeatureArgMutation;
//import ifeed.mining.moea.operators.gptype.BranchSwapCrossover;
//import ifeed.mining.moea.operators.FeatureMutation;
//import ifeed.mining.moea.search.InstrumentedSearch;
//import ifeed.ontology.OntologyManager;
//import ifeed.problem.assigning.MOEA;
//import ifeed.problem.assigning.Params;
//import ifeed.problem.assigning.logicOperators.generalization.*;
//import org.moeaframework.algorithm.EpsilonMOEA;
//import org.moeaframework.core.*;
//import org.moeaframework.core.comparator.DominanceComparator;
//import org.moeaframework.core.comparator.ParetoDominanceComparator;
//import org.moeaframework.core.comparator.ParetoObjectiveComparator;
//import org.moeaframework.core.comparator.ChainedComparator;
//import org.moeaframework.core.operator.GAVariation;
//import org.moeaframework.core.operator.TournamentSelection;
//import org.moeaframework.core.operator.CompoundVariation;
//import org.moeaframework.util.TypedProperties;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.BitSet;
//import java.util.List;
//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.Future;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//
///**
// *
// *
// * @author hsbang
// */
//
//public class EOSSMOEA_AOS {
//
//    // Instruments and orbits
//    public static String[] instrumentList = {
//            "ACE_ORCA","ACE_POL","ACE_LID",
//            "CLAR_ERB","ACE_CPR","DESD_SAR",
//            "DESD_LID","GACM_VIS","GACM_SWIR",
//            "HYSP_TIR","POSTEPS_IRS","CNES_KaRIN"};
//
//    public static String[] orbitList = {"LEO-600-polar-NA","SSO-600-SSO-AM","SSO-600-SSO-DD","SSO-800-SSO-DD","SSO-800-SSO-PM"};
//
//    /**
//     * pool of resources
//     */
//    private static ExecutorService pool;
//
//    /**
//     * List of future tasks to perform
//     */
//    private static ArrayList<Future<Algorithm>> futures;
//
//    /**
//     * First argument is the path to the project folder. Second argument is the
//     * mode. Third argument is the number of ArchitecturalEvaluators to
//     * initialize.
//     *
//     * @param args the command line arguments
//     */
//    public static void main(String[] args) {
//
//        RUN_MODE mode = RUN_MODE.AOS;
//
//        //PATH
//        if (args.length == 0) {
//            args = new String[4];
//            args[0] = "/Users/bang/workspace/daphne/data-mining"; // project directory
//            args[1] = "1"; //Mode
//            args[2] = "1"; //numCPU
//            args[3] = "1"; //numRuns
//        }
//
//        String inputDataFile = "/Users/bang/workspace/daphne/data-mining/data/data.csv";
//        InputDatasetReader reader = new InputDatasetReader(inputDataFile);
//        reader.setInputType(InputDatasetReader.InputType.BINARY_BITSTRING);
//        reader.setColumnInfo(InputDatasetReader.ColumnType.CLASSLABEL,0);
//        reader.setColumnInfo(InputDatasetReader.ColumnType.DECISION, 1);
//        reader.readData();
//
//        List<AbstractArchitecture> architectures = reader.getArchs();
//        BitSet label = reader.getLabel();
//        List<Integer> behavioral = new ArrayList<>();
//        List<Integer> non_behavioral = new ArrayList<>();
//        for(int i = 0; i < architectures.size(); i++){
//            if(label.get(i)){
//                behavioral.add(i);
//            }else{
//                non_behavioral.add(i);
//            }
//        }
//
//        OntologyManager manager = new OntologyManager("ClimateCentric");
//        Params params = new Params();
//        params.setOntologyManager(manager);
//        params.setInstrumentList(instrumentList);
//        params.setOrbitList(orbitList);
//
//        MOEABase base = new MOEA(params, architectures, behavioral, non_behavioral);
//        System.out.println("Path set to " + args[0]);
//        System.out.println("Running mode " + args[1]);
//        System.out.println("Will get " + args[2] + " resources");
//        System.out.println("Will do " + args[3] + " runs");
//
//        String path = args[0];
//        //int RUN_MODE = Integer.parseInt(args[1]);
//        int numCPU = Integer.parseInt(args[2]);
//        int numRuns = Integer.parseInt(args[3]);
//
//        pool = Executors.newFixedThreadPool(numCPU);
//        futures = new ArrayList<>(numRuns);
//
//        //parameters and operators for search
//        TypedProperties properties = new TypedProperties();
//
//        // Add description of the run
//        if(mode == RUN_MODE.AOS){
//            properties.setString("description","AOS with generalization and simplification operators");
//
//        }else if(mode == RUN_MODE.MOEA){
//            properties.setString("description","MOEA");
//
//        }
//
//        //setup for saving results
//        properties.setBoolean("saveQuality", true);
//        properties.setBoolean("saveCredits", true);
//        properties.setBoolean("saveSelection", true);
//
//        //search paramaters set here
//        int popSize = 200;
//        int maxEvals = 800;
//        properties.setInt("maxEvaluations", maxEvals);
//        properties.setInt("populationSize", popSize);
//
//        double crossoverProbability = 1.0;
//        double mutationProbability = 1.0;
//
//        Initialization initialization;
//        Problem problem;
//
//        //setup for epsilon MOEA
//        DominanceComparator comparator = new ParetoDominanceComparator();
//        double[] epsilonDouble = new double[]{0.05, 0.05, 1.5};
//        //final TournamentSelection selection = new TournamentSelection(2, comparator);
//
//        problem = new FeatureExtractionProblem(base, 1, MOEAParams.numberOfObjectives);
//        initialization = new FeatureExtractionInitialization(problem, popSize, "random");
//
//        //initialize population structure for algorithm
//        Population population = new Population();
//        EpsilonBoxDominanceArchive archive = new EpsilonBoxDominanceArchive(epsilonDouble);
//        ChainedComparator comp = new ChainedComparator(new ParetoObjectiveComparator());
//        TournamentSelection selection = new TournamentSelection(2, comp);
//
//        if(mode == RUN_MODE.AOS){
//
//            Variation mutation = new FeatureMutation(mutationProbability, base);
//            Variation smallMutation = new FeatureArgMutation(mutationProbability, base);
//            Variation crossover = new BranchSwapCrossover(crossoverProbability, base);
//
//            ArrayList<Variation> operators = new ArrayList();
//
//            //add domain-independent heuristics
//            operators.add(new CompoundVariation(crossover, mutation));
//            //operators.add(new CompoundVariation(crossover, smallMutation));
//
//            //operators.add(smallMutation);
//
//            //add logic operators
//            //operators.add(new InOrbit2Present(params, base));
//            //operators.add(new NotInOrbit2Absent(params, base));
//            //operators.add(new NotInOrbit2EmptyOrbit(params, base));
//            operators.add(new InstrumentSetGeneralizer(params, base));
//            operators.add(new OrbitGeneralizer(params, base));
//
////            operators.add(new CombineNotInOrbits(params, base));
////            operators.add(new CombineInOrbits(params, base));
//
//            properties.setDouble("pmin", 0.03);
//
//            //create operator selector
//            OperatorSelector operatorSelector = new AdaptivePursuit(operators, 0.8, 0.8, 0.03);
//
//            //create credit assigning
//            SetImprovementDominance creditAssignment = new SetImprovementDominance(archive, 1, 0);
//
//            AOSVariation aosStrategy = new AOSVariationSI(operatorSelector, creditAssignment, popSize);
//
//            //create AOS
//            EpsilonMOEA emoea = new EpsilonMOEA(problem, population, archive,
//                    selection, aosStrategy, initialization, comp);
//
//            AOSMOEA aos = new AOSMOEA(emoea, aosStrategy, true);
//
//            aos.setName("FeatureExtractionAOS");
//
//            InstrumentedSearch search = new InstrumentedSearch(aos, properties, path + File.separator + "results", aos.getName() + String.valueOf(0), base);
//
//            try {
//                search.call();
//
//            }catch(Exception ex){
//                ex.printStackTrace();
//            }
//
//
//        }else if(mode == RUN_MODE.MOEA){
//
//            for (int i = 0; i < numRuns; i++) {
//                Variation mutation  = new FeatureMutation(mutationProbability, base);
//                Variation crossover = new BranchSwapCrossover(crossoverProbability, base);
//                Variation gaVariation = new GAVariation(crossover, mutation);
//
//                problem = new FeatureExtractionProblem(base, 1, MOEAParams.numberOfObjectives);
//                initialization = new FeatureExtractionInitialization(problem, popSize, "random");
//
//                Algorithm eMOEA = new EpsilonMOEA(problem, population, archive, selection, gaVariation, initialization);
//
//                InstrumentedSearch run;
//
//                run = new InstrumentedSearch(eMOEA, properties, path + File.separator + "results",  String.valueOf(i), base);
//                futures.add(pool.submit(run));
//            }
//
//            for (Future<Algorithm> run : futures) {
//                try {
//                    run.get();
//
//                } catch (InterruptedException | ExecutionException ex) {
//                    Logger.getLogger(EOSSMOEA.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//
//        }
//
////        futures.add(pool.submit(search));
////
////        for (Future<Algorithm> run : futures) {
////            try {
////                run.get();
////
////            } catch (InterruptedException | ExecutionException ex) {
////                Logger.getLogger(EOSSMOEA_AOS.class.getName()).log(Level.SEVERE, null, ex);
////            }
////        }
////
////        pool.shutdown();
//
//    }
//
//    public enum RUN_MODE{
//        AOS,
//        MOEA;
//    }
//}
