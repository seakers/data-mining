/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.mining.moea;

/**
 *
 * @author hsbang
 */

import ifeed.architecture.AbstractArchitecture;
import ifeed.feature.AbstractFeatureFetcher;
import ifeed.feature.Feature;
import ifeed.feature.FeatureExpressionHandler;
import ifeed.local.params.BaseParams;
import ifeed.mining.AbstractDataMiningBase;
import ifeed.mining.AbstractLocalSearch;
import org.moeaframework.core.*;
import org.moeaframework.core.comparator.DominanceComparator;
import org.moeaframework.core.comparator.ParetoDominanceComparator;
import org.moeaframework.core.operator.TournamentSelection;
import org.moeaframework.util.TypedProperties;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public abstract class AbstractMOEABase extends AbstractDataMiningBase {
    /**
     * Generate the base features and store them
     */

    protected AbstractFeatureFetcher featureFetcher;
    protected FeatureExpressionHandler featureHandler;
    protected List<Feature> baseFeatures;
    protected AbstractRandomFeatureGenerator randomFeatureGenerator;
    protected List<FeatureRecord> recordedFeatures;
    protected AbstractLocalSearch localSearch;
    protected boolean saveResult;

    protected String projectPath;
    protected int numCPU;
    protected int numRuns;
    protected TypedProperties properties;
    protected int popSize;
    protected int maxEvals;
    protected Initialization initialization;
    protected Problem problem;
    protected Population population;
    protected EpsilonBoxDominanceArchive archive;
    protected TournamentSelection selection;
    protected DominanceComparator comparator;

    protected double crossoverProbability;
    protected double mutationProbability;
    protected double ifThenGenProbability;

    /**
     * pool of resources
     */
    protected static ExecutorService pool;

    /**
     * List of future tasks to perform
     */
    protected static ArrayList<Future<Algorithm>> futures;



    public AbstractMOEABase(BaseParams params, List<AbstractArchitecture> architectures,
                            List<Integer> behavioral, List<Integer> non_behavioral, AbstractFeatureFetcher fetcher){

        super(params, architectures, behavioral, non_behavioral);
        this.featureFetcher = fetcher;
        this.featureHandler = new FeatureExpressionHandler(this.featureFetcher);
        this.localSearch = null;

        saveResult = false;

        projectPath = System.getProperty("user.dir");
        numCPU = 1;
        numRuns = 1;

        pool = Executors.newFixedThreadPool(numCPU);
        futures = new ArrayList<>(numRuns);

        //parameters and operators for search
        properties = new TypedProperties();

        //search paramaters set here
        popSize = 500;
        maxEvals = 20000;
        properties.setInt("maxEvaluations", maxEvals);
        properties.setInt("populationSize", popSize);

        crossoverProbability = 1.0;
        mutationProbability = 0.9;
        ifThenGenProbability = 0.0;

        //setup for epsilon MOEA
        comparator = new ParetoDominanceComparator();
        double[] epsilonDouble = new double[]{0.04, 0.04, 1};
        selection = new TournamentSelection(2, comparator);
        population = new Population();
        archive = new EpsilonBoxDominanceArchive(epsilonDouble);

        //setup for saving results
        if(this.isSaveResult()){
            properties.setBoolean("saveQuality", true);
            properties.setBoolean("saveCredits", true);
            properties.setBoolean("saveSelection", true);
        }
    }

    public AbstractMOEABase init(List<Feature> baseFeatures){
        this.baseFeatures = baseFeatures;
        if(this.featureFetcher.getBaseFeatures().isEmpty()){
            this.featureFetcher.setBaseFeatures(this.baseFeatures);
        }
        this.getRandomFeatureGenerator().setBaseFeatures(this.baseFeatures);
        return this;
    }

    public AbstractMOEABase init(){
        if(this.baseFeatures == null){
            this.baseFeatures = super.generateBaseFeatures();
            if(this.featureFetcher.getBaseFeatures().isEmpty()){
                this.featureFetcher.setBaseFeatures(this.baseFeatures);
            }

            this.getRandomFeatureGenerator().setBaseFeatures(this.baseFeatures);
        }
        return this;
    }

    public void saveResult(){
        this.saveResult = true;
    }

    public boolean isSaveResult(){
        return this.saveResult;
    }

    public void setLocalSearch(AbstractLocalSearch localSearch){ this.localSearch = localSearch; }

    public AbstractLocalSearch getLocalSearch() {
        return localSearch;
    }

    public AbstractFeatureFetcher getFeatureFetcher() {
        return this.featureFetcher;
    }

    public FeatureExpressionHandler getFeatureHandler() {
        return this.featureHandler;
    }

    public void setRandomFeatureGenerator(AbstractRandomFeatureGenerator randomFeatureGenerator){
        this.randomFeatureGenerator = randomFeatureGenerator;
    }

    public AbstractRandomFeatureGenerator getRandomFeatureGenerator() {
        return this.randomFeatureGenerator;
    }

    public void resetBaseFeatures(){
        this.baseFeatures = super.generateBaseFeatures();
        this.featureFetcher.setBaseFeatures(this.baseFeatures);
        this.featureHandler = new FeatureExpressionHandler(this.featureFetcher);
    }

    public List<Feature> getBaseFeatures(){
        return this.baseFeatures;
    }

    public void recordFeature(String name, BitSet matches, double[] objectives){
        int index;
        if(this.recordedFeatures == null){
            this.recordedFeatures = new ArrayList<>();
            index = 0;
        }else{
            index = this.recordedFeatures.size();
        }
        this.recordedFeatures.add(new FeatureRecord(index, name, matches, objectives));
    }

    public List<FeatureRecord> getRecordedFeatures(){
        return this.recordedFeatures;
    }

    public class FeatureRecord{
        private int index;
        private String name;
        private BitSet matches;
        private double[] objectives;

        public FeatureRecord(int index, String name, BitSet matches, double[] objectives){
            this.index = index;
            this.name = name;
            this.matches = matches;
            this.objectives = objectives;
        }

        public int getIndex() {
            return index;
        }

        public String getName() {
            return name;
        }

        public BitSet getMatches() {
            return matches;
        }

        public double[] getObjectives() {
            return objectives;
        }

    }
}