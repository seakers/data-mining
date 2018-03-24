package ifeed.mining.arm;

import ifeed.Utils;
import ifeed.architecture.AbstractArchitecture;
import ifeed.feature.CandidateFeatureGenerator;
import ifeed.feature.Feature;
import ifeed.feature.FeatureComparator;
import ifeed.feature.FeatureMetric;
import ifeed.feature.logic.ConnectiveTester;

import java.util.*;

public abstract class LocalSearch extends DataMining {

    public LocalSearch(CandidateFeatureGenerator candidateGenerator, List<Integer> behavioral, List<Integer> non_behavioral, List<AbstractArchitecture> architectures,
                       double supp, double conf, double lift){

        super(candidateGenerator, behavioral, non_behavioral, architectures, supp, conf, lift);
    }

    public List<Feature> run(ConnectiveTester root){
        List<Feature> baseFeatures = this.generateBaseFeatures(false);
        return this.run(root, baseFeatures);
    }

    /**
     * Runs local Search that extends a given feature
     *
     * @param root
     *
     * */
    public List<Feature> run(ConnectiveTester root, List<Feature> baseFeatures){

        long t0 = System.currentTimeMillis();

        System.out.println("Local Search initiated");

        List<Feature> extracted_features;
        List<Feature> minedFeatures = new ArrayList<>();

        // Add a base feature to the given feature, replacing the placeholder
        for(Feature feature:baseFeatures){

            // Define which feature will be add to the current placeholder location
            root.setPlaceholder(feature.getName(), feature.getMatches());

            BitSet matches = root.getMatches();

            double[] metrics = Utils.computeMetrics(matches,this.labels,super.population.size());

            if(Double.isNaN(metrics[0])){
                continue;
            }

            String name = root.getName();

            Feature newFeature = new Feature(name, matches, metrics[0], metrics[1], metrics[2], metrics[3]);
            minedFeatures.add(newFeature);
        }

        FeatureComparator comparator1 = new FeatureComparator(FeatureMetric.FCONFIDENCE);
        FeatureComparator comparator2 = new FeatureComparator(FeatureMetric.RCONFIDENCE);
        List<Comparator> comparators = new ArrayList<>(Arrays.asList(comparator1,comparator2));

        extracted_features = Utils.getFeatureFuzzyParetoFront(minedFeatures,comparators,0);

        long t1 = System.currentTimeMillis();
        System.out.println("...[EOSSDataMining] Total features found: " + minedFeatures.size() + ", Pareto front: " + extracted_features.size());
        System.out.println("...[EOSSDataMining] Total data mining time : " + String.valueOf(t1 - t0) + " msec");

        return extracted_features;
    }

}
