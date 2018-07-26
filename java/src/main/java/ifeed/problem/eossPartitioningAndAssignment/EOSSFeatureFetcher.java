package ifeed.problem.eossPartitioningAndAssignment;

import ifeed.architecture.AbstractArchitecture;
import ifeed.feature.FeatureFetcher;
import ifeed.feature.Feature;
import ifeed.problem.eoss.EOSSFilterFetcher;
import ifeed.problem.eoss.EOSSFilterOperatorFetcher;

import java.util.List;


public class EOSSFeatureFetcher extends FeatureFetcher {

    public EOSSFeatureFetcher(List<AbstractArchitecture> architectures){
        super(architectures, new ifeed.problem.eoss.EOSSFilterFetcher());
        super.setFilterOperatorFetcher(new EOSSFilterOperatorFetcher());
    }

    public EOSSFeatureFetcher(List<Feature> baseFeatures, List<AbstractArchitecture> architectures){
        super(baseFeatures, architectures, new EOSSFilterFetcher());
        super.setFilterOperatorFetcher(new EOSSFilterOperatorFetcher());
    }

}
