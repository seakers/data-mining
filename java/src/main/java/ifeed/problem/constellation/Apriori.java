/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.constellation;

import ifeed.architecture.AbstractArchitecture;
import ifeed.filter.AbstractFilter;
import ifeed.local.params.BaseParams;
import ifeed.mining.arm.AbstractApriori;

import java.util.List;

/**
 *
 * @author bang
 */

public class Apriori extends AbstractApriori {

    public Apriori(BaseParams params, int maxFeatureLength, List<AbstractArchitecture> architectures, List<Integer> behavioral, List<Integer> non_behavioral, double supp, double conf, double lift) {
        super(params, maxFeatureLength, architectures, behavioral, non_behavioral, supp, conf, lift);
    }

    @Override
    public List<AbstractFilter> generateCandidates(){
        return new FeatureGenerator(super.params).generateCandidates();
    }
}