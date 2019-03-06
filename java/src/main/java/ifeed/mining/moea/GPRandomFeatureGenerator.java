/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.mining.moea;

/**
 *
 * @author hsbang
 */

import ifeed.local.params.MOEAParams;
import ifeed.feature.Feature;
import ifeed.feature.logic.Connective;
import ifeed.feature.logic.Literal;
import ifeed.feature.logic.Formula;
import ifeed.feature.logic.LogicalConnectiveType;
import org.moeaframework.core.PRNG;
import java.util.List;

public class GPRandomFeatureGenerator extends AbstractRandomFeatureGenerator{

    public GPRandomFeatureGenerator(List<Feature> baseFeatures){
        super(baseFeatures);
    }

    /**
     * Generates a random feature tree
     * @return
     */
    public Connective generateRandomFeature(){

        if(baseFeatures.isEmpty()){
            throw new IllegalStateException("BaseFeatures cannot be empty!");
        }

        Connective root;
        LogicalConnectiveType logic;

        // Select AND or OR as the logical connective used as a root node
        if(PRNG.nextInt(2) == 0){ // 0 or 1
            logic = LogicalConnectiveType.AND;
        }else{
            logic = LogicalConnectiveType.OR;
        }
        root = new Connective(logic);

        // Get the maximum number of literals (atomic formula) to be added to the feature tree
        int numLiterals = PRNG.nextInt(MOEAParams.maxNumLiteralInit) + 1; // min: 1, max: maxNumLiteralInit
        for(int i = 0; i < numLiterals; i++){

            // Select the feature to be added
            Feature featureToAdd = baseFeatures.get(PRNG.nextInt(baseFeatures.size()));

            if(i==0){
                root.addLiteral(featureToAdd.getName(), featureToAdd.getMatches());

            }else{
                List<Formula> candidates = root.getDescendantNodes(true);
                int rand = PRNG.nextInt(candidates.size());
                Formula node = candidates.get(rand);

                if(node instanceof Connective){
                    ((Connective) node).addLiteral(featureToAdd.getName(), featureToAdd.getMatches());

                }else{
                    // The selected node is a literal
                    Connective parent = (Connective) node.getParent();
                    Literal literal = (Literal) node;
                    parent.createNewBranch(literal, featureToAdd.getName(), featureToAdd.getMatches());
                }
            }
        }
        return root;
    }
}
