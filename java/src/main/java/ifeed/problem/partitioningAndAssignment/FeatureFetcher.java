package ifeed.problem.partitioningAndAssignment;

import ifeed.architecture.AbstractArchitecture;
import ifeed.feature.AbstractFeatureFetcher;
import ifeed.feature.Feature;
import ifeed.problem.assignment.FilterFetcher;
import ifeed.problem.assignment.FilterOperatorFetcher;

import java.util.List;


public class FeatureFetcher extends AbstractFeatureFetcher {

    public FeatureFetcher(List<AbstractArchitecture> architectures){
        super(architectures, new FilterFetcher());
        super.setFilterOperatorFetcher(new FilterOperatorFetcher());
    }

    public FeatureFetcher(List<Feature> baseFeatures, List<AbstractArchitecture> architectures){
        super(baseFeatures, architectures, new FilterFetcher());
        super.setFilterOperatorFetcher(new FilterOperatorFetcher());
    }

}
