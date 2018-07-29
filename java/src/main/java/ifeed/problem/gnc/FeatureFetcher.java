package ifeed.problem.gnc;

import ifeed.architecture.AbstractArchitecture;
import ifeed.feature.AbstractFeatureFetcher;
import ifeed.feature.Feature;

import java.util.List;

public class FeatureFetcher extends AbstractFeatureFetcher {

    public FeatureFetcher(List<AbstractArchitecture> architectures){
        super(architectures, new FilterFetcher());
    }

    public FeatureFetcher(List<Feature> baseFeatures, List<AbstractArchitecture> architectures){
        super(baseFeatures, architectures, new FilterFetcher());
    }
}
