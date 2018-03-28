package ifeed.problem.eoss;

import ifeed.architecture.AbstractArchitecture;
import ifeed.feature.FeatureFetcher;
import ifeed.filter.Filter;
import ifeed.mining.moea.MOEABase;
import java.util.List;

public class EOSSMOEA extends MOEABase {

    public EOSSMOEA(List<AbstractArchitecture> architectures,
                    List<Integer> behavioral, List<Integer> non_behavioral, FeatureFetcher fetcher){

        super(architectures, behavioral, non_behavioral, fetcher);
    }

    @Override
    public List<Filter> generateCandidates(){
        return new EOSSFeatureGenerator().generateCandidates();
    }

}
