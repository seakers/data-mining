/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.edl;

import ifeed.feature.AbstractFeatureGenerator;
import ifeed.filter.AbstractFilter;
import ifeed.local.params.BaseParams;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author bang
 */
public class FeatureGenerator extends AbstractFeatureGenerator{

    private Params params;
    private int numVariables;

    public FeatureGenerator(BaseParams params){
        super(params);
        this.params = (Params) params;
        this.numVariables = this.params.getDecisionVarNames().size();
    }

    @Override
    public List<AbstractFilter> generateCandidates(){
        ArrayList<AbstractFilter> candidateFeatures = new ArrayList<>();
        for(int i = 0; i < this.numVariables; i++){
            int max = this.params.getMaxValues()[i];
            for(int j = 0; j < max + 1; j++){
                candidateFeatures.add(new DiscreteValueFilter(this.params, i, j));
            }
        }

        System.out.println(candidateFeatures.size());
        return candidateFeatures;
    }
}
