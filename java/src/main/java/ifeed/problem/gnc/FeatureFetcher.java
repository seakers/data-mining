package ifeed.problem.gnc;

import ifeed.architecture.AbstractArchitecture;
import ifeed.feature.AbstractFeatureFetcher;
import ifeed.feature.Feature;
import ifeed.local.params.BaseParams;

import java.util.List;

public class FeatureFetcher extends AbstractFeatureFetcher {

    public FeatureFetcher(BaseParams params, List<AbstractArchitecture> architectures){
        super(params, architectures, new FilterFetcher(params));
    }

    public FeatureFetcher(BaseParams params, List<Feature> baseFeatures, List<AbstractArchitecture> architectures){
        super(params, baseFeatures, architectures, new FilterFetcher(params));
    }
}
