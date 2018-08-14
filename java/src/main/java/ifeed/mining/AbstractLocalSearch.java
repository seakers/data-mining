package ifeed.mining;

import ifeed.Utils;
import ifeed.architecture.AbstractArchitecture;
import ifeed.feature.Feature;
import ifeed.feature.FeatureMetricComparator;
import ifeed.feature.FeatureMetric;
import ifeed.feature.logic.ConnectiveTester;
import ifeed.local.params.BaseParams;
import java.util.*;

public abstract class AbstractLocalSearch extends AbstractDataMiningBase implements AbstractDataMiningAlgorithm {

    private ConnectiveTester root;

    public AbstractLocalSearch(BaseParams params,
                               ConnectiveTester root,
                               List<AbstractArchitecture> architectures,
                               List<Integer> behavioral,
                               List<Integer> non_behavioral){

        super(params, architectures, behavioral, non_behavioral);
        this.root = root;
    }

    public void setRoot(ConnectiveTester root){
        this.root = root;
    }

    @Override
    public List<Feature> run(){
        List<Feature> baseFeatures = this.generateBaseFeatures();
        return this.run(baseFeatures);
    }

    /**
     * Runs local Search that extends a given feature
     *
     * */
    public List<Feature> run(List<Feature> baseFeatures){

        if(this.root == null){
            throw new IllegalStateException("Feature tree need to be defined to run local search");
        }

        long t0 = System.currentTimeMillis();

        System.out.println("Local Search initiated");

        List<Feature> extracted_features;
        List<Feature> minedFeatures = new ArrayList<>();

        // Add a base feature to the given feature, replacing the placeholder
        for(Feature feature:baseFeatures){

            // Define which feature will be add to the current placeholder location
            this.root.setNewNode(feature.getName(), feature.getMatches());

            BitSet matches = this.root.getMatches();
            double[] metrics = Utils.computeMetricsSetNaNZero(matches, super.labels, super.population.size());

            if(Double.isNaN(metrics[0])){
                continue;
            }

            String name = this.root.getName();
            Feature newFeature = new Feature(name, matches, metrics[0], metrics[1], metrics[2], metrics[3]);
            minedFeatures.add(newFeature);
        }

        FeatureMetricComparator comparator1 = new FeatureMetricComparator(FeatureMetric.FCONFIDENCE);
        FeatureMetricComparator comparator2 = new FeatureMetricComparator(FeatureMetric.RCONFIDENCE);
        List<Comparator> comparators = new ArrayList<>(Arrays.asList(comparator1,comparator2));

        extracted_features = Utils.getFeatureFuzzyParetoFront(minedFeatures,comparators,0);

        long t1 = System.currentTimeMillis();
        System.out.println("...[" + this.getClass().getSimpleName() + "] Total features found: " + minedFeatures.size() + ", Pareto front: " + extracted_features.size());
        System.out.println("...[" + this.getClass().getSimpleName() + "] Total data mining time : " + String.valueOf(t1 - t0) + " msec");
        return extracted_features;
    }
}
