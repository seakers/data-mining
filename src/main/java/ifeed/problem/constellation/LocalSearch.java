package ifeed.problem.constellation;

import ifeed.architecture.AbstractArchitecture;
import ifeed.feature.logic.LogicalConnectiveType;
import ifeed.filter.AbstractFilter;
import ifeed.local.params.BaseParams;
import ifeed.mining.AbstractLocalSearch;

import java.util.List;

public class LocalSearch extends AbstractLocalSearch {

    public LocalSearch(BaseParams params, String root, LogicalConnectiveType logic, List<AbstractArchitecture> architectures, List<Integer> behavioral, List<Integer> non_behavioral){
        super(params, root, logic, architectures, behavioral, non_behavioral, new FeatureFetcher(params, architectures));
    }

    @Override
    public List<AbstractFilter> generateCandidates(){
        return new FeatureGenerator(params).generateCandidates();
    }
}
