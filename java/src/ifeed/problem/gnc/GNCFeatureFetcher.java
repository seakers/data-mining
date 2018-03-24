package ifeed.problem.gnc;

import ifeed.architecture.AbstractArchitecture;
import ifeed.feature.FeatureFetcher;
import ifeed.feature.Feature;
import ifeed.filter.Filter;
import ifeed.problem.gnc.filters.*;
import ifeed.architecture.DiscreteInputArchitecture;

import java.util.Arrays;
import java.util.List;
import java.util.BitSet;
import java.util.ArrayList;

public class GNCFeatureFetcher extends FeatureFetcher {

    public GNCFeatureFetcher(List<AbstractArchitecture> architectures){
        super(architectures, new GNCFilterFetcher());
    }

    public GNCFeatureFetcher(List<Feature> baseFeatures, List<AbstractArchitecture> architectures){
        super(baseFeatures, architectures, new GNCFilterFetcher());
    }
}
