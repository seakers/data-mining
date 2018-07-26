package ifeed.problem.eossPartitioningAndAssignment;

import ifeed.architecture.AbstractArchitecture;
import ifeed.feature.logic.ConnectiveTester;
import ifeed.filter.Filter;
import ifeed.mining.LocalSearch;
import ifeed.problem.eoss.EOSSFeatureGenerator;

import java.util.List;

public class EOSSLocalSearch extends LocalSearch{

    public EOSSLocalSearch(ConnectiveTester root, List<AbstractArchitecture> architectures, List<Integer> behavioral, List<Integer> non_behavioral){
        super(root, architectures, behavioral, non_behavioral);
    }

    @Override
    public List<Filter> generateCandidates(){
        return new EOSSFeatureGenerator().generateCandidates();
    }
}
