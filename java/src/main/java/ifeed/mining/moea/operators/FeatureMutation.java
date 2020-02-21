package ifeed.mining.moea.operators;

import ifeed.local.params.MOEAParams;
import ifeed.mining.moea.AbstractMOEABase;
import ifeed.mining.moea.FeatureTreeSolution;
import ifeed.mining.moea.FeatureTreeVariable;
import ifeed.mining.moea.GPMOEABase;
import ifeed.feature.Feature;
import ifeed.feature.logic.Connective;
import ifeed.feature.logic.Literal;
import org.moeaframework.core.PRNG;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;


public class FeatureMutation implements Variation{

    private double probability;
    AbstractMOEABase base;

    public FeatureMutation(double probability, AbstractMOEABase base){
        this.probability = probability;
        this.base = base;
    }

    @Override
    public Solution[] evolve(Solution[] parents){

        if( PRNG.nextDouble() > this.probability){
            return parents;
        }

        Solution[] out = new Solution[1];
        int randInt = PRNG.nextInt(base.getBaseFeatures().size());
        Feature featureToAdd = base.getBaseFeatures().get(randInt);

        FeatureTreeVariable tree = (FeatureTreeVariable) parents[0].getVariable(0);
        Connective root = tree.getRoot().copy();

        while(true){
            Connective rootCopy = root.copy();
            Literal randomNode = (Literal) base.getFeatureHandler().selectRandomNode(rootCopy, Literal.class);
            Connective parent = (Connective) randomNode.getParent();

            parent.removeNode(randomNode);
            parent.addLiteral(featureToAdd.getName(), featureToAdd.getMatches());
            base.getFeatureHandler().repairFeatureTreeStructure(rootCopy);

            if(!parent.getChildNodes().isEmpty()){ // The parent may be empty due to repairFeatureTreeStructure() call
                root = rootCopy;
                break;
            }
        }

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