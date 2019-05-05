package ifeed.mining.moea.operators.RuleSetType;

import ifeed.feature.Feature;
import ifeed.feature.logic.*;
import ifeed.local.params.MOEAParams;
import ifeed.mining.moea.AbstractMOEABase;
import ifeed.mining.moea.FeatureTreeSolution;
import ifeed.mining.moea.FeatureTreeVariable;
import org.moeaframework.core.PRNG;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;

import java.util.ArrayList;
import java.util.List;


public class RuleSetFeatureMutation implements Variation{

    private double probability;
    AbstractMOEABase base;

    public RuleSetFeatureMutation(double probability, AbstractMOEABase base){
        this.probability = probability;
        this.base = base;
    }

    @Override
    public Solution[] evolve(Solution[] parents){

        if( PRNG.nextDouble() > this.probability){
            return parents;
        }
        Solution[] out = new Solution[1];
        FeatureTreeVariable tree = (FeatureTreeVariable) parents[0].getVariable(0);
        Connective root = tree.getRoot().copy();

        double probRemoveNode = 0.1;
        double probAddNode = 0.2;
        double randDouble = PRNG.nextDouble();

        if(root.getDescendantLiterals().size() < 2){
            probRemoveNode = 0.0;
        }

        if(randDouble < probRemoveNode){

//            // Remove node
//            List<Literal> candidateLiteral = new ArrayList<>();
//
//            if(root.getChildNodes().size() > 1 && !root.getLiteralChildren().isEmpty()){
//                candidateLiteral.addAll(root.getLiteralChildren());
//            }
//
//            for(IfThenStatement ifThen: root.getIfThenChildren()){
//                if(ifThen.getConditional().size() > 1){
//                    candidateLiteral.addAll(ifThen.getConditional());
//                }else if(ifThen.getConsequent().size() > 1){
//                    candidateLiteral.addAll(ifThen.getConsequent());
//                }
//            }
//
//            if(!candidateLiteral.isEmpty()){
//                Literal nodeToBeRemoved = candidateLiteral.get(PRNG.nextInt(candidateLiteral.size()));
//                FormulaWithChildren parent = nodeToBeRemoved.getParent();
//                if(parent instanceof Connective){
//                    root.removeLiteral(nodeToBeRemoved);
//                }else if(parent instanceof IfThenStatement){
//                    ((IfThenStatement) parent).removeNode(nodeToBeRemoved);
//                }else{
//                    throw new IllegalStateException("Only Connective or IfThenStatement node can be parent");
//                }
//            }

        }else if(randDouble >= probRemoveNode && randDouble < probRemoveNode + probAddNode){

            // Add a new node
            Feature featureToAdd = base.getBaseFeatures().get(PRNG.nextInt(base.getBaseFeatures().size()));

            List<Literal> allLiterals = root.getDescendantLiterals();
            int randIndex = PRNG.nextInt(allLiterals.size());
            Literal randomNode = allLiterals.get(randIndex);
            FormulaWithChildren parent = randomNode.getParent();

            if(parent instanceof Connective){
                ((Connective) parent).addLiteral(featureToAdd.getName(), featureToAdd.getMatches());
            }else if(parent instanceof IfThenStatement){
                ((IfThenStatement) parent).addToConsequent(featureToAdd.getName(), featureToAdd.getMatches());
            }else{
                throw new IllegalStateException("Parent can only be either Connective or IfThenStatement node");
            }

        }else{
            // Modify an existing literal
            Feature featureToAdd = base.getBaseFeatures().get(PRNG.nextInt(base.getBaseFeatures().size()));

            List<Literal> allLiterals = root.getDescendantLiterals();
            int randIndex = PRNG.nextInt(allLiterals.size());
            Literal randomNode = allLiterals.get(randIndex);
            FormulaWithChildren parent = randomNode.getParent();

            if(parent instanceof Connective){
                ((Connective) parent).removeNode(randomNode);
                ((Connective) parent).addLiteral(featureToAdd.getName(), featureToAdd.getMatches());
            }else if(parent instanceof IfThenStatement){
                IfThenStatement ifThen = (IfThenStatement) parent;
                if(ifThen.isInConditional(randomNode)){
                    ifThen.removeFromConditional(randomNode);
                    ifThen.addToConditional(featureToAdd.getName(), featureToAdd.getMatches());

                }else{
                    ifThen.removeFromConsequent(randomNode);
                    ifThen.addToConsequent(featureToAdd.getName(), featureToAdd.getMatches());
                }
            }else{
                throw new IllegalStateException("Parent can only be either Connective or IfThenStatement node");
            }
        }

        base.getFeatureHandler().repairFeatureTreeStructure(root);
        FeatureTreeVariable newTree = new FeatureTreeVariable(this.base, root);
        Solution sol = new FeatureTreeSolution(newTree, MOEAParams.numberOfObjectives);
        out[0] = sol;
        return out;
    }

    @Override
    public int getArity(){
        return 1;
    }
}
