package ifeed.problem.eoss;

import ifeed.architecture.AbstractArchitecture;
import ifeed.mining.arm.LocalSearch;

import java.util.List;

public class EOSSLocalSearch extends LocalSearch{

    public EOSSLocalSearch(List<Integer> behavioral, List<Integer> non_behavioral, List<AbstractArchitecture> architectures,
                           double supp, double conf, double lift){

        super(new EOSSFeatureGenerator(), behavioral, non_behavioral, architectures, supp, conf, lift);
    }

}
