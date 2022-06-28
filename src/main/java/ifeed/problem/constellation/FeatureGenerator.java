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
import java.util.List;

/**
 *
 * @author bang
 */
public class FeatureGenerator extends AbstractFeatureGenerator{

    private AbstractConstellationProblemParams params;
    private int numSats;
    private String[] orbitalParameters;

    public FeatureGenerator(BaseParams params){
        super(params);
        this.params = (AbstractConstellationProblemParams) params;
        this.numSats = this.params.getNumSats();
        this.orbitalParameters = this.params.getOrbitalParameters();
    }

    @Override
    public List<AbstractFilter> generateCandidates(){
        if(this.params.isNumSatsFixed()){
            return generateCandidates_fixedNumSats();
        }else{
            return generateCandidates_variableNumSats();
        }
    }

    public List<AbstractFilter> generateCandidates_variableNumSats(){

        ArrayList<AbstractFilter> candidate_features = new ArrayList<>();
        // Types
        // numPlanes: {name[num]},
        // altitudeRange: {name[lb,ub,cardinality]}
        // inclinationRange: {name[lb,ub,cardinality]}
        // meanDiffRAAN: ?

        Integer[] cardinalityRange = new Integer[2];


        // altitudeRange
        for(int i = 0; i < 8; i++){
            double lb = 400 + (100 * i);
            double ub = lb + 100;

            lb = (lb + 6370) * 1000;
            ub = (ub + 6370) * 1000;
            candidate_features.add(new AltitudeRange(params, lb, ub, cardinalityRange));
        }

        // inclinationRange
        for(int i = 0; i < 7; i++){
            double lb = 30 + (10 * i);
            double ub = lb + 10;

            lb = (lb / 180) * Math.PI;
            ub = (ub / 180) * Math.PI;
            candidate_features.add(new InclinationRange(params, lb, ub, cardinalityRange));
        }

        for(int i = 0; i < 9; i++){
            Integer a = i * 2 + 1;
            Integer b = a + 2;
            Integer[] numRange = {a, b};
            candidate_features.add(new NumSats(params, numRange));
        }

        System.out.println(candidate_features.size() + " base features generated");

        return candidate_features;
    }


    public List<AbstractFilter> generateCandidates_fixedNumSats(){
        
        ArrayList<AbstractFilter> candidate_features = new ArrayList<>();
        // Types
        // numPlanes: {name[num]},
        // altitudeRange: {name[lb,ub,cardinality]}
        // inclinationRange: {name[lb,ub,cardinality]}
        // meanDiffRAAN: ?


        Integer[] cardinality = new Integer[2];
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
