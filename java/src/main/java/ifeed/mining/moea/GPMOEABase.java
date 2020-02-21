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
import java.util.*;

public abstract class GPMOEABase extends AbstractMOEABase {
    /**
     * Generate the base features and store them
     */

    public GPMOEABase(BaseParams params, List<AbstractArchitecture> architectures,
                      List<Integer> behavioral, List<Integer> non_behavioral, AbstractFeatureFetcher fetcher){

        super(params, architectures, behavioral, non_behavioral, fetcher);
        super.setRandomFeatureGenerator(new GPRandomFeatureGenerator(super.baseFeatures));
    }
}