package ifeed.problem.eoss;

import ifeed.architecture.AbstractArchitecture;
import ifeed.feature.FeatureFetcher;
import ifeed.feature.Feature;
import ifeed.filter.Filter;

import java.util.List;
import java.util.BitSet;


public class EOSSFeatureFetcher extends FeatureFetcher {

    public EOSSFeatureFetcher(List<AbstractArchitecture> architectures){
        super(architectures, new EOSSFilterFetcher());
    }

    public EOSSFeatureFetcher(List<Feature> baseFeatures, List<AbstractArchitecture> architectures){
        super(baseFeatures, architectures, new EOSSFilterFetcher());
    }

}
