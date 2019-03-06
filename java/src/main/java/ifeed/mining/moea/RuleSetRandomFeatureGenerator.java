/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.mining.moea;

/**
 *
 * @author hsbang
 */

import ifeed.feature.Feature;
import ifeed.feature.logic.Connective;
import ifeed.feature.logic.LogicalConnectiveType;
import ifeed.local.params.MOEAParams;
import org.moeaframework.core.PRNG;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RuleSetRandomFeatureGenerator extends AbstractRandomFeatureGenerator{

    int maxNumLiteralInit;

    public RuleSetRandomFeatureGenerator(List<Feature> baseFeatures){
        super(baseFeatures);
        this.maxNumLiteralInit = MOEAParams.maxNumLiteralInit;
    }

    public RuleSetRandomFeatureGenerator(int maxNumLiteralInit, List<Feature> baseFeatures){
        super(baseFeatures);
        this.maxNumLiteralInit = maxNumLiteralInit;
    }

    /**
     * Generates a random rule set
     * @return
     */
    @Override
    public Connective generateRandomFeature(){

        if(baseFeatures.isEmpty()){
            throw new IllegalStateException("BaseFeatures cannot be empty!");
        }

        Connective root = new Connective(LogicalConnectiveType.AND);

        Set<String> addedFeatureSet = new HashSet<>();

        // Get the maximum number of literals (atomic formula) to be added to the feature tree
        int numLiterals = PRNG.nextInt(maxNumLiteralInit) + 1; // min: 1, max: maxNumLiteralInit
        for(int i = 0; i < numLiterals; i++){

            Feature featureToAdd;

            while(true){
                // Select the feature to be added
                featureToAdd = baseFeatures.get(PRNG.nextInt(baseFeatures.size()));
                String featureName = featureToAdd.getName();

                if(!addedFeatureSet.contains(featureName)){
                    addedFeatureSet.add(featureName);
                    break;
                }
            }

            root.addLiteral(featureToAdd.getName(), featureToAdd.getMatches());
        }

        return root;
    }
}
