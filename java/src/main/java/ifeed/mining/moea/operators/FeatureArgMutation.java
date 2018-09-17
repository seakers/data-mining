package ifeed.mining.moea.operators;

import ifeed.local.params.MOEAParams;
import ifeed.mining.moea.FeatureTreeSolution;
import ifeed.mining.moea.FeatureTreeVariable;

import ifeed.feature.Feature;
import ifeed.filter.AbstractFilterOperatorFetcher;
import ifeed.filter.FilterOperator;
import ifeed.feature.logic.Connective;
import ifeed.feature.logic.Literal;
import ifeed.mining.moea.MOEABase;
import org.moeaframework.core.PRNG;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;

public class FeatureArgMutation implements Variation{

    private double probability;
    private MOEABase base;
    private AbstractFilterOperatorFetcher filterOperatorFetcher;

    public FeatureArgMutation(double probability, MOEABase base){
        this.probability = probability;
        this.base = base;
        this.filterOperatorFetcher = this.base.getFeatureFetcher().getFilterOperatorFetcher();
    }

    @Override
    public Solution[] evolve(Solution[] parents){

        if( PRNG.nextDouble() > this.probability){
            return parents;
        }

        // Single child created
        Solution[] out = new Solution[1];

        // Select a single literal to modify
        FeatureTreeVariable tree = (FeatureTreeVariable) parents[0].getVariable(0);
        Connective root = tree.getRoot().copy();

        Literal randomNode = (Literal) base.getFeatureSelector().selectRandomNode(root, Literal.class);
        Connective parent = base.getFeatureSelector().findParentNode(root, randomNode);

        parent.removeNode(randomNode);

        FilterOperator op = filterOperatorFetcher.fetch(randomNode.getName());
        op.mutate();
        Feature modifiedFeature = base.getFeatureFetcher().fetch(op.toString());

        // Add new literal to the given node
        parent.addLiteral(modifiedFeature.getName(), modifiedFeature.getMatches());

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
