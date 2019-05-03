package ifeed.problem.assigning;

import ifeed.Utils;
import ifeed.architecture.AbstractArchitecture;
import ifeed.feature.Feature;
import ifeed.feature.logic.Connective;
import ifeed.filter.AbstractFilter;
import ifeed.local.params.BaseParams;
import ifeed.local.params.MOEAParams;
import ifeed.mining.AbstractDataMiningAlgorithm;
import ifeed.mining.moea.*;
import ifeed.mining.moea.operators.FeatureMutation;
import ifeed.ontology.OntologyManager;
import ifeed.problem.assigning.logicOperators.generalization.single.InstrumentGeneralizer;
import ifeed.problem.assigning.logicOperators.generalization.single.OrbitGeneralizer;
import org.moeaframework.algorithm.AbstractEvolutionaryAlgorithm;
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
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RuleSetMOEA extends RuleSetMOEABase implements AbstractDataMiningAlgorithm {

    private String projectPath;
    private RUN_MODE mode;
    private int numCPU;
    private int numRuns;
    private Params params;

    private OntologyManager ontologyManager;
    private String[] orbitList;
    private String[] instrumentList;

    /**
     * pool of resources
     */
    private static ExecutorService pool;

    /**
     * List of future tasks to perform
     */
    private static ArrayList<Future<Algorithm>> futures;


    public RuleSetMOEA(BaseParams params, List<AbstractArchitecture> architectures,
                       List<Integer> behavioral, List<Integer> non_behavioral){

        super(params, architectures, behavioral, non_behavioral, new FeatureFetcher(params, architectures));

        projectPath = System.getProperty("user.dir");
        mode = RUN_MODE.MOEA;
        numCPU = 1;
        numRuns = 1;
        this.params = (Params) params;
    }

    public void setMode(RUN_MODE mode){
        this.mode = mode;
    }

    public void setOrbitList(String[] orbitList) {
        this.orbitList = orbitList;
    }

    public void setInstrumentList(String[] instrumentList){
        this.instrumentList = instrumentList;
    }

    public void setOntologyManager(OntologyManager manager){
        this.ontologyManager = manager;
    }

    public OntologyManager getOntologyManager() {
        return ontologyManager;
    }

    @Override
    public List<AbstractFilter> generateCandidates(){
        return new FeatureGenerator(super.params).generateCandidates();
    }

    @Override
    public List<Feature> run(){

        RuleSetMOEA base = this;

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
        int maxEvals = 20000;
        properties.setInt("maxEvaluations", maxEvals);
        properties.setInt("populationSize", popSize);

        double crossoverProbability = 1.0;
        double mutationProbability = 0.9;
        double ifThenGenProbability = 0.3;

        Initialization initialization;
        Problem problem;

        //setup for epsilon MOEA_GP
        DominanceComparator comparator = new ParetoDominanceComparator();
        double[] epsilonDouble = new double[]{0.04, 0.04, 1};
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
                    Variation mutation  = new ifeed.mining.moea.operators.RuleSetType.RuleSetFeatureMutation(mutationProbability, base);
                    Variation crossover = new ifeed.mining.moea.operators.RuleSetType.CutAndSpliceCrossover(crossoverProbability, base);
                    Variation ifThenGen = new ifeed.mining.moea.operators.RuleSetType.GenerateIfThenStatement(ifThenGenProbability, base);
                    Variation gaVariation = new GAVariation(crossover, mutation);
                    Variation compoundVariation = new CompoundVariation(gaVariation, ifThenGen);

                    problem = new FeatureExtractionProblem(base, 1, MOEAParams.numberOfObjectives);
                    initialization = new FeatureExtractionInitialization(problem, popSize, "random");

                    Algorithm eMOEA = new EpsilonMOEA(problem, population, archive, selection, compoundVariation, initialization);

                    InstrumentedSearch run = new InstrumentedSearch(eMOEA, properties, this.projectPath + File.separator + "results",  String.valueOf(i), base);
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

            case AOS_with_generalization_operators: // Adaptive operator selection

                if(this.instrumentList == null || this.orbitList == null){
                    throw new IllegalStateException("Orbit list and instrument list need to be specified before running MOEA_AOS with single operators");
                }

                for (int i = 0; i < numRuns; i++) {

                    String origname = "AOS_" + System.nanoTime();

                    params.setOntologyManager(this.ontologyManager);
                    params.setLeftSet(this.instrumentList);
                    params.setRightSet(this.orbitList);

                    problem = new FeatureExtractionProblem(base, 1, MOEAParams.numberOfObjectives);
                    initialization = new FeatureExtractionInitialization(problem, popSize, "random");

                    // Define operators
                    List<Variation> operators = new ArrayList<>();
                    Variation mutation  = new FeatureMutation(mutationProbability, base);
                    Variation crossover = new ifeed.mining.moea.operators.RuleSetType.CutAndSpliceCrossover(crossoverProbability, base);
                    Variation gaVariation = new GAVariation(crossover, mutation);

                    operators.add(gaVariation);
//                    operators.add(new InOrbit2Present(params, base));
//                    operators.add(new SharedNotInOrbit2AbsentPlusCond(params, base));
//                    operators.add(new NotInOrbit2EmptyOrbit(params, base));
                    operators.add(new GAVariation(new InstrumentGeneralizer(params, base), mutation));
                    operators.add(new GAVariation(new OrbitGeneralizer(params, base), mutation));

                    properties.setDouble("pmin", 0.09);

                    // Create operator selector
                    OperatorSelector operatorSelector = new AdaptivePursuit(operators, 0.8, 0.8, 0.03);

                    // Create credit assigning
                    SetImprovementDominance creditAssignment = new SetImprovementDominance(archive, 1, 0);

                    // Create AOS strategy
                    AOSVariation aosStrategy = new AOSVariationSI(operatorSelector, creditAssignment, popSize);

                    EpsilonMOEA emoea = new EpsilonMOEA(problem, population, archive, selection, aosStrategy, initialization, comparator);

                    AOSMOEA aos = new AOSMOEA(emoea, aosStrategy, true);

                    InstrumentedSearch run = new InstrumentedSearch(aos, properties, this.projectPath + File.separator + "results", String.valueOf(i), base);

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

                pool.shutdown();
                break;

            default:
                throw new IllegalArgumentException("Choose a mode between 1 and 3");
        }

        List<Feature> out = new ArrayList<>();
        for(int i = 0; i < outputPopulation.size(); i++){
            FeatureTreeVariable var = (FeatureTreeVariable) outputPopulation.get(i).getVariable(0);
            Connective root = var.getRoot();
            BitSet matches = root.getMatches();
            double[] metrics = Utils.computeMetrics(matches, base.getLabels(), base.getPopulation().size(), 0.0);
            Feature thisFeature = new Feature(root.getName(), root.getMatches(), metrics[0], metrics[1], metrics[2], metrics[3], root.getDescendantLiterals(true).size());
            out.add(thisFeature);
        }

        return out;
    }

    public enum RUN_MODE {
        MOEA,
        AOS_with_generalization_operators;
    }
}
