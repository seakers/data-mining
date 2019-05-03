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
import ifeed.feature.logic.IfThenStatement;
import ifeed.feature.logic.Literal;
import ifeed.feature.logic.LogicalConnectiveType;
import ifeed.local.params.MOEAParams;
import org.moeaframework.core.PRNG;

import java.util.ArrayList;
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

        double ifThenStatementProb = 0.0;

        Set<Integer> usedFeatureIndices = new HashSet<>();

        // Get the maximum number of literals (atomic formula) to be added to the feature tree
        int numLiterals = PRNG.nextInt(maxNumLiteralInit) + 1; // min: 1, max: maxNumLiteralInit

        int currentNumLiterals = 0;
        while(currentNumLiterals < numLiterals){

            if(PRNG.nextDouble() < ifThenStatementProb){ // Add a if-then statement
                List<Integer> indices = getUnselectedIndex(2, baseFeatures.size(), usedFeatureIndices);
                Feature conditionalFeature = baseFeatures.get(indices.get(0));
                Feature consequentFeature = baseFeatures.get(indices.get(1));
                IfThenStatement ifThen = new IfThenStatement(new ArrayList<>(), new ArrayList<>());
                ifThen.addToConditional(conditionalFeature.getName(), conditionalFeature.getMatches());
                ifThen.addToConsequent(consequentFeature.getName(), consequentFeature.getMatches());
                root.addNode(ifThen);
                currentNumLiterals += 2;

            }else{ // Simply add a literal
                int index = getUnselectedIndex(1, baseFeatures.size(), usedFeatureIndices).get(0);
                Feature featureToAdd = baseFeatures.get(index);
                root.addLiteral(featureToAdd.getName(), featureToAdd.getMatches());
                currentNumLiterals += 1;
            }
        }

        return root;
    }

    public List<Integer> getUnselectedIndex(int numToSelect, int maxIndex, Set<Integer> usedIndexList){
        List<Integer> out = new ArrayList<>();
        while(true){
            int test = PRNG.nextInt(maxIndex);
            if(!usedIndexList.contains(test)){
                out.add(test);
                usedIndexList.add(test);
                if(out.size() == numToSelect){
                    break;
                }
            }
        }
        return out;
    }
}
