package ifeed.problem.gnc;

import ifeed.architecture.AbstractArchitecture;
import ifeed.feature.logic.ConnectiveTester;
import ifeed.filter.Filter;
import ifeed.mining.LocalSearch;

import java.util.List;

public class GNCLocalSearch extends LocalSearch{

    public GNCLocalSearch(ConnectiveTester root, List<AbstractArchitecture> architectures, List<Integer> behavioral, List<Integer> non_behavioral){

        super(root, architectures, behavioral, non_behavioral);
    }

    @Override
    public List<Filter> generateCandidates(){
        return new GNCFeatureGenerator().generateCandidates();
    }
}
