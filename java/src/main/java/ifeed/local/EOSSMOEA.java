///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package ifeed.local;
//
//import aos.aos.AOSMOEA;
//import aos.creditassignment.offspringparent.OffspringParentDomination;
//import aos.operator.AOSVariationOP;
//import aos.operatorselectors.OperatorSelector;
//import aos.operatorselectors.ProbabilityMatching;
//import ifeed.architecture.AbstractArchitecture;
//import ifeed.io.InputDatasetReader;
//import ifeed.local.params.BaseParams;
//import ifeed.local.params.MOEAParams;
//import ifeed.mining.moea.MOEABase;
//import ifeed.mining.moea.operators.gptype.BranchSwapCrossover;
//import ifeed.mining.moea.operators.FeatureMutation;
//import ifeed.mining.moea.search.InstrumentedSearch;
//import ifeed.mining.moea.FeatureExtractionInitialization;
//import ifeed.mining.moea.FeatureExtractionProblem;
//import org.moeaframework.algorithm.EpsilonMOEA;
//import org.moeaframework.core.*;
//import org.moeaframework.core.comparator.DominanceComparator;
//import org.moeaframework.core.comparator.ParetoDominanceComparator;
//import org.moeaframework.core.operator.GAVariation;
//import org.moeaframework.core.operator.TournamentSelection;
//import org.moeaframework.util.TypedProperties;
//import java.io.File;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.BitSet;
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
//public class EOSSMOEA {
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
//        BaseParams params = new ifeed.problem.assigning.Params();
//        MOEABase base = new ifeed.problem.assigning.MOEA(params, architectures, behavioral, non_behavioral);
//        System.out.println("Path set to " + args[0]);
//        System.out.println("Running mode " + args[1]);
//        System.out.println("Will get " + args[2] + " resources");
//        System.out.println("Will do " + args[3] + " runs");
//
//        String path = args[0];
//        int RUN_MODE = Integer.parseInt(args[1]);
//        int numCPU = Integer.parseInt(args[2]);
//        int numRuns = Integer.parseInt(args[3]);
//
//        pool = Executors.newFixedThreadPool(numCPU);
//        futures = new ArrayList<>(numRuns);
//
//        //parameters and operators for search
//        TypedProperties properties = new TypedProperties();
//
//        //search paramaters set here
//        int popSize = 400;
//        int maxEvals = 10000;
//        properties.setInt("maxEvaluations", maxEvals);
//        properties.setInt("populationSize", popSize);
//
//        double crossoverProbability = 1.0;
//        double mutationProbability = 0.1;
//
//        Initialization initialization;
//        Problem problem;
//
//        //setup for epsilon MOEA
//        DominanceComparator comparator = new ParetoDominanceComparator();
//        double[] epsilonDouble = new double[]{0.05, 0.05 , 1.5};
//        final TournamentSelection selection = new TournamentSelection(2, comparator);
//
//        //setup for saving results
////        properties.setBoolean("saveQuality", true);
////        properties.setBoolean("saveCredits", true);
////        properties.setBoolean("saveSelection", true);
//
//        switch (RUN_MODE) {
//
//            case 1: //Use epsilonMOEA
//
//                for (int i = 0; i < numRuns; i++) {
//                    Variation mutation  = new FeatureMutation(mutationProbability, base);
//                    Variation crossover = new BranchSwapCrossover(crossoverProbability, base);
//                    Variation gaVariation = new GAVariation(crossover, mutation);
//
//                    Population population = new Population();
//                    EpsilonBoxDominanceArchive archive = new EpsilonBoxDominanceArchive(epsilonDouble);
//
//                    problem = new FeatureExtractionProblem(base, 1, MOEAParams.numberOfObjectives);
//                    initialization = new FeatureExtractionInitialization(problem, popSize, "random");
//
//                    Algorithm eMOEA = new EpsilonMOEA(problem, population, archive, selection, gaVariation, initialization);
//
//                    InstrumentedSearch run;
//
//                    run = new InstrumentedSearch(eMOEA, properties, path + File.separator + "results",  String.valueOf(i), base);
//                    futures.add(pool.submit(run));
//                }
//
//                for (Future<Algorithm> run : futures) {
//                    try {
//                        run.get();
//
//                    } catch (InterruptedException | ExecutionException ex) {
//                        Logger.getLogger(EOSSMOEA.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                }
//                break;
//
//            case 2: // Adaptive Operator Selection
//
//                String origname = "AOS_" + System.nanoTime();
//
//                Population population = new Population();
//                EpsilonBoxDominanceArchive archive = new EpsilonBoxDominanceArchive(epsilonDouble);
//
//                problem = new FeatureExtractionProblem(base, 1, MOEAParams.numberOfObjectives);
//                initialization = new FeatureExtractionInitialization(problem, popSize, "random");
//
//                // Define operators
//                List<Variation> operators = new ArrayList<>();
//
//                Variation mutation = new FeatureMutation(mutationProbability, base);
//                Variation crossover = new BranchSwapCrossover(crossoverProbability, base);
//
//                operators.add(mutation);
//                operators.add(crossover);
//
//                // Create operator selector
//                OperatorSelector operatorSelector = new ProbabilityMatching(operators, 0.8, 0.1);
//
//                // Create credit assigning
//                OffspringParentDomination creditAssignment = new OffspringParentDomination(1, 0, 0);
//
//                // Create AOS strategy
//                AOSVariationOP aosStrategy = new AOSVariationOP(operatorSelector, creditAssignment, popSize);
//
//                EpsilonMOEA emoea = new EpsilonMOEA(problem, population, archive, selection, null, initialization);
//
//                AOSMOEA aos = new AOSMOEA(emoea, aosStrategy, true);
//
//                InstrumentedSearch search = new InstrumentedSearch(aos, properties, path + File.separator + "results",  String.valueOf(0), base);
//
//                futures.add(pool.submit(search));
//
//                for (Future<Algorithm> run : futures) {
//                    try {
//                        run.get();
//
//                    } catch (InterruptedException | ExecutionException ex) {
//                        Logger.getLogger(EOSSMOEA.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                }
//
//                pool.shutdown();
//                break;
//
//            case 3://innovization search
//
//
////                String fileName = "AIAA_innovize_" + System.nanoTime();
////                for (int i = 0; i < numRuns; i++) {
////                    try {
////                        problem = getFeatureExtractionProblem(false);
////
////                        ICreditAssignment creditAssignment = CreditDefFactory.getInstance().getCreditDef("SIDo", properties, problem);
////
////                        ArrayList<Variation> operators = new ArrayList();
////
////                        //add domain-independent heuristics
////                        Variation SingleCross = new CompoundVariation(new OnePointCrossover(crossoverProbability,2), new BitFlip(mutationProbability));
////                        operators.add(SingleCross);
////
////                        //set up OperatorReplacementStrategy
////                        EpochTrigger epochTrigger = new EpochTrigger(epochLength);
////                        EOSSOperatorCreator eossOpCreator = new EOSSOperatorCreator(crossoverProbability,mutationProbability);
////                        ArrayList<Variation> permanentOps = new ArrayList();
////                        permanentOps.add(SingleCross);
////                        RemoveNLowest operatorRemover = new RemoveNLowest(permanentOps, properties.getInt("nOpsToRemove", 2));
////                        OperatorReplacementStrategy ops = new OperatorReplacementStrategy(epochTrigger, operatorRemover, eossOpCreator);
////
////                        properties.setDouble("pmin", 0.03);
////
////                        //all other properties use default parameters
////                        INextOperator selector = AOSFactory.getInstance().getHeuristicSelector("AP", properties, operators);
////
////                        Population population = new Population();
////                        EpsilonBoxDominanceArchive archive = new EpsilonBoxDominanceArchive(epsilonDouble);
////
////                        initialization = new RandomFeatureSelector(problem, popSize, "random");
////
////                        AOSEpsilonMOEA hemoea = new AOSEpsilonMOEA(problem, population, archive, selection,
////                                initialization, selector, creditAssignment);
////
////                        AbstractPopulationLabeler labeler =  new NondominatedSortingLabeler(.25);
////                        InnovizationSearch run;
////                        if(i<numCPU){
////                            run = new InnovizationSearch(hemoea, properties, labeler, ops, path + File.separator + "result", fileName + i,true);
////                        }else{
////                            run = new InnovizationSearch(hemoea, properties, labeler, ops, path + File.separator + "result", fileName + i, false);
////                        }
////                        futures.add(pool.submit(run));
////                    } catch (IOException ex) {
////                        Logger.getLogger(MOEA.class.getName()).log(Level.SEVERE, null, ex);
////                    }
////
////                }
////
////                for (Future<Algorithm> run : futures) {
////                    try {
////                        AOSEpsilonMOEA hemoea = (AOSEpsilonMOEA) run.get();
////
////                    } catch (InterruptedException | ExecutionException ex) {
////                        Logger.getLogger(MOEA.class.getName()).log(Level.SEVERE, null, ex);
////                    }
////                }
////
////                pool.shutdown();
//                break;
//
//            default:
//                System.out.println("Choose a mode between 1 and 3");
//        }
//    }
//}
