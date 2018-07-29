package ifeed.problem.assignment;

import ifeed.architecture.AbstractArchitecture;
import ifeed.feature.logic.ConnectiveTester;
import ifeed.filter.AbstractFilter;
import ifeed.mining.AbstractLocalSearch;

import java.util.List;

public class LocalSearch extends AbstractLocalSearch {

    public LocalSearch(ConnectiveTester root, List<AbstractArchitecture> architectures, List<Integer> behavioral, List<Integer> non_behavioral){
        super(root, architectures, behavioral, non_behavioral);
    }

    @Override
    public List<AbstractFilter> generateCandidates(){
        return new FeatureGenerator().generateCandidates();
    }
}
