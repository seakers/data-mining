package ifeed.problem.assigning;

import aos.aos.AOSMOEA;
import aos.creditassignment.offspringparent.OffspringParentDomination;
import aos.operator.AOSVariationOP;
import aos.operatorselectors.OperatorSelector;
import aos.operatorselectors.ProbabilityMatching;
import ifeed.Utils;
import ifeed.architecture.AbstractArchitecture;
import ifeed.feature.logic.Connective;
import ifeed.feature.logic.LogicalConnectiveType;
import ifeed.filter.AbstractFilter;
import ifeed.feature.Feature;
import ifeed.local.params.BaseParams;
import ifeed.local.params.MOEAParams;
import ifeed.mining.AbstractDataMiningAlgorithm;
import ifeed.mining.moea.FeatureExtractionInitialization;
import ifeed.mining.moea.FeatureExtractionProblem;
import ifeed.mining.moea.FeatureTreeVariable;
import ifeed.mining.moea.MOEABase;
import ifeed.mining.moea.operators.gptype.BranchSwapCrossover;
import ifeed.mining.moea.operators.FeatureMutation;
import ifeed.mining.moea.search.InstrumentedSearch;
import org.moeaframework.algorithm.AbstractEvolutionaryAlgorithm;
import org.moeaframework.algorithm.EpsilonMOEA;
import org.moeaframework.core.*;
import org.moeaframework.core.comparator.DominanceComparator;
import org.moeaframework.core.comparator.ParetoDominanceComparator;
import org.moeaframework.core.operator.GAVariation;
import org.moeaframework.core.operator.TournamentSelection;
import org.moeaframework.util.TypedProperties;

import java.io.File;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MOEA extends MOEABase implements AbstractDataMiningAlgorithm {

    private String projectPath;
    private int mode;
    private int numCPU;
    private int numRuns;

    /**
     * pool of resources
     */
    private static ExecutorService pool;

    /**
     * List of future tasks to perform
     */
    private static ArrayList<Future<Algorithm>> futures;


    public MOEA(BaseParams params, List<AbstractArchitecture> architectures,
                List<Integer> behavioral, List<Integer> non_behavioral){

        super(params, architectures, behavioral, non_behavioral, new FeatureFetcher(params, architectures));

        projectPath = "/Users/bang/workspace/daphne/data-mining";
        mode = 2;
        numCPU = 1;
        numRuns = 1;
    }

    @Override
    public List<AbstractFilter> generateCandidates(){
        return new FeatureGenerator(params).generateCandidates();
    }

    @Override
    public List<Feature> run(){

        MOEABase base = this;

        System.out.println("Path set to " + projectPath);
        System.out.println("Running mode " + mode);
        System.out.println("Will get " + numCPU + " resources");
        System.out.println("Will do " + numRuns + " runs");

        pool = Executors.newFixedThreadPool(numCPU);
        futures = new ArrayList<>(numRuns);

        //parameters and operators for search
        TypedProperties properties = new TypedProperties();

        //search paramaters set here
        int popSize = 500;
        int maxEvals = 12000;
        properties.setInt("maxEvaluations", maxEvals);
        properties.setInt("populationSize", popSize);

        double crossoverProbability = 1.0;
        double mutationProbability = 1.0;

        Initialization initialization;
        Problem problem;

        //setup for epsilon MOEA
        DominanceComparator comparator = new ParetoDominanceComparator();
        double[] epsilonDouble = new double[]{0.05, 0.05, 1.2};
        final TournamentSelection selection = new TournamentSelection(2, comparator);

        //setup for saving results
//        properties.setBoolean("saveQuality", true);
//        properties.setBoolean("saveCredits", true);
//        properties.setBoolean("saveSelection", true);

        Population pop = new Population();

        switch (mode) {

            case 1: //Use epsilonMOEA with GP-type crossover operator

                for (int i = 0; i < numRuns; i++) {
                    Variation mutation  = new FeatureMutation(mutationProbability, base);
                    Variation crossover = new ifeed.mining.moea.operators.gptype.BranchSwapCrossover(crossoverProbability, base);
                    Variation gaVariation = new GAVariation(crossover, mutation);

                    Population population = new Population();
                    EpsilonBoxDominanceArchive archive = new EpsilonBoxDominanceArchive(epsilonDouble);

                    problem = new FeatureExtractionProblem(base, 1, MOEAParams.numberOfObjectives);
                    initialization = new FeatureExtractionInitialization(problem, popSize, "random");

                    Algorithm eMOEA = new EpsilonMOEA(problem, population, archive, selection, gaVariation, initialization);

                    InstrumentedSearch run;

                    run = new InstrumentedSearch(eMOEA, properties, this.projectPath + File.separator + "results",  String.valueOf(i), base);
                    futures.add(pool.submit(run));
                }

                for (Future<Algorithm> run : futures) {
                    try {
                        Algorithm alg = run.get();
                        pop = ((AbstractEvolutionaryAlgorithm) alg).getArchive();

                    } catch (InterruptedException | ExecutionException ex) {
                        Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                    }
                }
                break;

            case 2: //Use epsilonMOEA with variable-length-chromosome-type operator

                for (int i = 0; i < numRuns; i++) {
                    Variation mutation  = new FeatureMutation(mutationProbability, base);
                    Variation crossover = new ifeed.mining.moea.operators.vlctype.CutAndSpliceCrossover(crossoverProbability, base, LogicalConnectiveType.AND);
                    Variation gaVariation = new GAVariation(crossover, mutation);

                    Population population = new Population();
                    EpsilonBoxDominanceArchive archive = new EpsilonBoxDominanceArchive(epsilonDouble);

                    problem = new FeatureExtractionProblem(base, 1, MOEAParams.numberOfObjectives);
                    initialization = new FeatureExtractionInitialization(problem, popSize, "random");

                    Algorithm eMOEA = new EpsilonMOEA(problem, population, archive, selection, gaVariation, initialization);

                    InstrumentedSearch run;

                    run = new InstrumentedSearch(eMOEA, properties, this.projectPath + File.separator + "results",  String.valueOf(i), base);
                    futures.add(pool.submit(run));
                }

                for (Future<Algorithm> run : futures) {
                    try {
                        Algorithm alg = run.get();
                        pop = ((AbstractEvolutionaryAlgorithm) alg).getArchive();

                    } catch (InterruptedException | ExecutionException ex) {
                        Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                    }
                }
                break;

            case 3: // Adaptive operator selection

                for (int i = 0; i < numRuns; i++) {

                    String origname = "AOS_" + System.nanoTime();

                    Population population = new Population();
                    EpsilonBoxDominanceArchive archive = new EpsilonBoxDominanceArchive(epsilonDouble);

                    problem = new FeatureExtractionProblem(base, 1, MOEAParams.numberOfObjectives);
                    initialization = new FeatureExtractionInitialization(problem, popSize, "random");

                    // Define operators
                    List<Variation> operators = new ArrayList<>();
                    Variation mutation = new FeatureMutation(mutationProbability, base);
                    Variation crossover = new BranchSwapCrossover(crossoverProbability, base);
                    operators.add(mutation);
                    operators.add(crossover);

                    // Create operator selector
                    OperatorSelector operatorSelector = new ProbabilityMatching(operators, 0.8, 0.1);

                    // Create credit assigning
                    OffspringParentDomination creditAssignment = new OffspringParentDomination(1, 0, 0);

                    // Create AOS strategy
                    AOSVariationOP aosStrategy = new AOSVariationOP(operatorSelector, creditAssignment, popSize);

                    EpsilonMOEA emoea = new EpsilonMOEA(problem, population, archive, selection, null, initialization);

                    AOSMOEA aos = new AOSMOEA(emoea, aosStrategy, true);

                    InstrumentedSearch run = new InstrumentedSearch(aos, properties, this.projectPath + File.separator + "results", String.valueOf(0), base);

                    futures.add(pool.submit(run));
                }

                for (Future<Algorithm> run : futures) {
                    try {
                        run.get();

                    } catch (InterruptedException | ExecutionException ex) {
                        Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                    }
                }

                pool.shutdown();
                break;

            default:
                throw new IllegalArgumentException("Choose a mode between 1 and 3");
        }

        List<Feature> out = new ArrayList<>();
        for(int i = 0; i < pop.size(); i++){
            FeatureTreeVariable var = (FeatureTreeVariable) pop.get(i).getVariable(0);
            Connective root = var.getRoot();
            BitSet matches = root.getMatches();
            double[] metrics = Utils.computeMetrics(matches, base.getLabels(), base.getPopulation().size(), 0.0);
            Feature thisFeature = new Feature(root.getName(), root.getMatches(), metrics[0], metrics[1], metrics[2], metrics[3], root.getDescendantLiterals(true).size());
            out.add(thisFeature);
        }

        return out;
    }
}