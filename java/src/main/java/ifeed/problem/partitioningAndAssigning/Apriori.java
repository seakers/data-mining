/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.partitioningAndAssigning;

import ifeed.architecture.AbstractArchitecture;
import ifeed.feature.Feature;
import ifeed.filter.AbstractFilter;
import ifeed.local.params.BaseParams;
import ifeed.mining.arm.AbstractApriori;
import ifeed.mining.arm.AbstractAssociationRuleMining;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.BitSet;
import java.util.List;
import java.util.stream.IntStream;

/**
 *
 * @author bang
 */

public class Apriori extends AbstractApriori {

    public Apriori(BaseParams params, List<AbstractArchitecture> architectures, List<Integer> behavioral, List<Integer> non_behavioral, double supp, double conf, double lift) {
        super(params, architectures, behavioral, non_behavioral, supp, conf, lift);
    }

    @Override
    public List<AbstractFilter> generateCandidates(){
        return new FeatureGenerator(params).generateCandidates();
    }

}