/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.mining.moea;

/**
 *
 * @author hsbang
 */

import ifeed.architecture.AbstractArchitecture;
import ifeed.feature.AbstractFeatureFetcher;
import ifeed.local.params.BaseParams;

import java.util.List;

public abstract class RuleSetMOEABase extends AbstractMOEABase {

    public RuleSetMOEABase(BaseParams params, List<AbstractArchitecture> architectures,
                      List<Integer> behavioral, List<Integer> non_behavioral, AbstractFeatureFetcher fetcher){

        super(params, architectures, behavioral, non_behavioral, fetcher);
        super.setRandomFeatureGenerator(new RuleSetRandomFeatureGenerator(super.baseFeatures));
    }
}