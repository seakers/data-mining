package ifeed.problem.constellation;

import ifeed.Utils;
import ifeed.architecture.AbstractArchitecture;
import ifeed.feature.Feature;
import ifeed.feature.logic.Connective;
import ifeed.filter.AbstractFilter;
import ifeed.local.params.BaseParams;
import ifeed.local.params.MOEAParams;
import ifeed.mining.AbstractDataMiningAlgorithm;
import ifeed.mining.moea.FeatureExtractionInitialization;
import ifeed.mining.moea.FeatureExtractionProblem;
import ifeed.mining.moea.FeatureTreeVariable;
import ifeed.mining.moea.GPMOEABase;
import ifeed.mining.moea.operators.FeatureMutation;
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

public class GPMOEA extends GPMOEABase implements AbstractDataMiningAlgorithm {

    private String projectPath;
    private RUN_MODE mode;
    private int numCPU;
    private int numRuns;
    private AbstractConstellationProblemParams params;

    /**
     * pool of resources
     */
    private static ExecutorService pool;

    /**
     * List of future tasks to perform
     */
    private static ArrayList<Future<Algorithm>> futures;


    public GPMOEA(BaseParams params, List<AbstractArchitecture> architectures,
                  List<Integer> behavioral, List<Integer> non_behavioral){

        super(params, architectures, behavioral, non_behavioral, new FeatureFetcher(params, architectures));

        projectPath = System.getProperty("user.dir");
        mode = RUN_MODE.MOEA;
        numCPU = 1;
        numRuns = 1;
        this.params = (AbstractConstellationProblemParams) params;
    }

    public void setMode(RUN_MODE mode){
        this.mode = mode;
    }

    @Override
    public List<AbstractFilter> generateCandidates(){
        return new FeatureGenerator(super.params).generateCandidates();
    }

    @Override
    public List<Feature> run(){
        super.init();

        GPMOEABase base = this;

        System.out.println("Path set to " + projectPath);
        System.out.println("Running mode " + mode);
        System.out.println("Will get " + numCPU + " resources");
        System.out.println("Will do " + numRuns + " runs");

        pool = Executors.newFixedThreadPool(numCPU);
        futures = new ArrayList<>(numRuns);

        //parameters and operators for search
        TypedProperties properties = new TypedProperties();

        //search paramaters set here
        int popSize = 400;
        int maxEvals = 10000;
        properties.setInt("maxEvaluations", maxEvals);
        properties.setInt("populationSize", popSize);

        double crossoverProbability = 1.0;
        double mutationProbability = 0.9;

        Initialization initialization;
        Problem problem;

        //setup for epsilon GPMOEA
        DominanceComparator comparator = new ParetoDominanceComparator();
        double[] epsilonDouble = new double[]{0.02, 0.02, 1.1};
        final TournamentSelection selection = new TournamentSelection(2, comparator);

        Population population = new Population();
        EpsilonBoxDominanceArchive archive = new EpsilonBoxDominanceArchive(epsilonDouble);

        //setup for saving results
        if(this.isSaveResult()){
            properties.setBoolean("saveQuality", true);
            properties.setBoolean("saveCredits", true);
            properties.setBoolean("saveSelection", true);
        }

        Population outputPopulation = new Population();

        switch (mode) {

            case MOEA: //Use epsilonMOEA with GP-type crossover operator

                for (int i = 0; i < numRuns; i++) {
                    Variation mutation  = new FeatureMutation(mutationProbability, base);
                    Variation crossover = new ifeed.mining.moea.operators.GPType.BranchSwapCrossover(crossoverProbability, base);
                    Variation gaVariation = new GAVariation(crossover, mutation);

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
                        outputPopulation = ((AbstractEvolutionaryAlgorithm) alg).getArchive();

                    } catch (InterruptedException | ExecutionException ex) {
                        Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                    }
                }
                break;

            default:
                throw new IllegalArgumentException("Choose a mode between 1 and 3");
        }

        List<Feature> out = new ArrayList<>();
        for(int i = 0; i < outputPopulation.size(); i++){
            FeatureTreeVariable var = (FeatureTreeVariable) outputPopulation.get(i).getVariable(0);
            Connective root = var.getRoot();
            BitSet matches = root.getMatches();
            double[] metrics = Utils.computeMetrics(matches, base.getLabels(), base.getSamples().size(), 0.0);
            Feature thisFeature = new Feature(root.getName(), root.getMatches(), metrics[0], metrics[1], metrics[2], metrics[3], root.getDescendantLiterals().size());
            out.add(thisFeature);
        }

        return out;
    }

    public enum RUN_MODE {
        MOEA,
    }
}
