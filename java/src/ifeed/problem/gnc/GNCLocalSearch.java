package ifeed.problem.gnc;

import ifeed.architecture.AbstractArchitecture;
import ifeed.mining.arm.LocalSearch;

import java.util.List;

public class GNCLocalSearch extends LocalSearch{

    public GNCLocalSearch(List<Integer> behavioral, List<Integer> non_behavioral, List<AbstractArchitecture> architectures,
                           double supp, double conf, double lift){

        super(new GNCFeatureGenerator(), behavioral, non_behavioral, architectures, supp, conf, lift);
    }

}
