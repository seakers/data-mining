package ifeed.problem.edl;

import ifeed.architecture.AbstractArchitecture;
import ifeed.feature.Feature;
import ifeed.filter.AbstractFilter;
import ifeed.local.params.BaseParams;
import ifeed.mining.AbstractDataMiningAlgorithm;

import ifeed.mining.moea.GPMOEABase;
import java.util.ArrayList;
import java.util.List;


public class GPMOEA extends GPMOEABase implements AbstractDataMiningAlgorithm {

    public GPMOEA(BaseParams params, List<AbstractArchitecture> architectures,
                  List<Integer> behavioral, List<Integer> non_behavioral){

        super(params, architectures, behavioral, non_behavioral, new FeatureFetcher(params, architectures));
    }

    @Override
    public List<AbstractFilter> generateCandidates(){
        return new ifeed.problem.edl.FeatureGenerator(super.params).generateCandidates();
    }
    @Override
    public List<Feature> run(){
        return new ArrayList<>();
    }

}
