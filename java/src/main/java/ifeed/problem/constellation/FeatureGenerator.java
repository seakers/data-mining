/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.constellation;

import ifeed.feature.AbstractFeatureGenerator;
import ifeed.filter.AbstractFilter;
import ifeed.local.params.BaseParams;
import ifeed.problem.constellation.filters.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author bang
 */
public class FeatureGenerator extends AbstractFeatureGenerator{

    private Params params;
    private int numSats;
    private String[] orbitalParameters;

    public FeatureGenerator(BaseParams params){
        super(params);
        this.params = (Params) params;
        this.numSats = this.params.getNumSats();
        this.orbitalParameters = this.params.getOrbitalParams();
    }

    @Override
    public List<AbstractFilter> generateCandidates(){
        
        ArrayList<AbstractFilter> candidate_features = new ArrayList<>();
        // Types
        // numPlanes: {name[num]},
        // altitudeRange: {name[lb,ub,cardinality]}
        // inclinationRange: {name[lb,ub,cardinality]}
        // meanDiffRAAN: ?


        int[] cardinality = new int[2];
        for(int j = 0; j < 4; j++){
            int diff = 2;
            cardinality[0] = 5 + j;
            cardinality[1] = cardinality[0] + diff;

            // altitudeRange
            for(int i = 0; i < 8; i++){
                double lb = 400 + (100 * i);
                double ub = lb + 100;

                lb = (lb + 6370) * 1000;
                ub = (ub + 6370) * 1000;

                candidate_features.add(new AltitudeRange(params, lb, ub, cardinality));
            }

            // inclinationRange
            for(int i = 0; i < 7; i++){
                double lb = 30 + (10 * i);
                double ub = lb + 10;

                lb = (lb / 180) * Math.PI;
                ub = (ub / 180) * Math.PI;

                candidate_features.add(new InclinationRange(params, lb, ub, cardinality));
            }
        }

        System.out.println(candidate_features.size() + " base features generated");

        return candidate_features;
    }
}
