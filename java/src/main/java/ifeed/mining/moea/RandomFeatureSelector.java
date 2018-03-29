/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.mining.moea;

/**
 *
 * @author hsbang
 */

import ifeed.local.MOEAParams;
import ifeed.feature.Feature;
import ifeed.feature.logic.Connective;
import ifeed.feature.logic.Literal;
import ifeed.feature.logic.Formula;
import ifeed.feature.logic.LogicOperator;
import org.moeaframework.core.PRNG;
import java.util.ArrayList;
import java.util.List;

public class RandomFeatureSelector {
    /**
     * Generate the base features and store them
     */

    private List<Feature> baseFeatures;

    public RandomFeatureSelector(List<Feature> baseFeatures){
        this.baseFeatures = baseFeatures;
    }

    public Connective generateRandomFeature(){
        // Randomly generate a feature tree

        Connective root;
        LogicOperator logic;

        // Select AND or OR as the logical connective used as a root node
        if(PRNG.nextInt(2) == 0){ // 0 or 1
            logic = LogicOperator.AND;
        }else{
            logic = LogicOperator.OR;
        }
        root = new Connective(logic);

        int numLiterals = PRNG.nextInt(MOEAParams.maxNumLiteral) + 1; // min: 1, max: maxNumLiteral
        for(int i = 0; i < numLiterals; i++){

            int baseFeatureIndex = PRNG.nextInt(baseFeatures.size());
            Feature featureToAdd = baseFeatures.get(baseFeatureIndex);

            if(i==0){
                root.addLiteral(featureToAdd.getName(), featureToAdd.getMatches(), false);

            }else{
                Formula node = this.selectRandomNode(root, null);

                if(node instanceof Connective){
                    ((Connective) node).addLiteral(featureToAdd.getName(), featureToAdd.getMatches());

                }else{
                    Connective parent = this.findParentNode(root, node);
                    int index = parent.getLiteralChildren().indexOf(node);
                    parent.createNewBranch(featureToAdd.getName(), featureToAdd.getMatches(), index);
                }
            }
        }
        return root;
    }

    public Formula selectRandomNode(Connective root, Class type){

        int numOfNodes;
        if(type == Connective.class){
            numOfNodes = root.getDescendantConnectives(true).size();

        }else if(type == Literal.class){
            numOfNodes = root.getDescendantLiterals(true).size();

        }else{
            numOfNodes = root.getNumDescendantNodes(true);
        }

        int randInt = PRNG.nextInt(numOfNodes);
        return selectNodeOfGivenIndex(root, randInt, type);
    }

    private Formula selectNodeOfGivenIndex(Connective root, int targetIndex, Class type) {

        String ntype = "";
        int maxIndex = 999;

        if(type == Connective.class){
            ntype = "connective";
            maxIndex = root.getDescendantConnectives(true).size();

        }else if(type == Literal.class){
            ntype = "literal";
            maxIndex = root.getDescendantLiterals(true).size();

        }else{
            ntype = "both";
            maxIndex = root.getNumDescendantNodes(true);

        }

        if(targetIndex > maxIndex){
            throw new RuntimeException("Exception in " + this.getClass().getName() + ": target index larger than the number of nodes.");
        }

        switch (ntype){
            case "connective":
                List<Connective> connectiveNodes = root.getDescendantConnectives(true);
                for(int i = 0; i < connectiveNodes.size(); i++){
                    if( i == targetIndex){
                        return connectiveNodes.get(i);
                    }
                }
                break;

            case "literal":
                List<Literal> literalNodes = root.getDescendantLiterals(true);
                for(int i = 0; i < literalNodes.size(); i++){
                    if( i == targetIndex){
                        return literalNodes.get(i);
                    }
                }
                break;

            case "both":
                List<Formula> nodes = new ArrayList<>();
                nodes.addAll(root.getDescendantConnectives(true));
                nodes.addAll(root.getDescendantLiterals(true));
                for(int i = 0; i < nodes.size(); i++){
                    if( i == targetIndex){
                        return nodes.get(i);
                    }
                }
                break;

            default:
                break;
        }

        throw new RuntimeException("Exception in " + this.getClass().getName() + ": could not find node of a given target index");
    }

    /**
     * Finds the parent node the target node, given a feature tree
     * @param root given feature tree
     * @param target the target node of which the parent is searched for
     * @return
     */
    public Connective findParentNode(Connective root, Formula target){

        if(target instanceof Connective){
            for(Connective branch:root.getConnectiveChildren()){
                if(branch == target){
                    return root;
                }
            }

        }else{
            for(Literal literal:root.getLiteralChildren()){
                if(literal == target){
                    return root;
                }
            }
        }

        for(Connective branch: root.getConnectiveChildren()){
            Connective temp = findParentNode(branch, target);
            if(temp != null){
                return temp;
            }
        }

        return null;
    }
}
