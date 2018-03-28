package ifeed.mining.moea.operators;

import ifeed.local.MOEAParams;
import ifeed.mining.moea.FeatureTreeSolution;
import ifeed.mining.moea.FeatureTreeVariable;

import ifeed.feature.Feature;
import ifeed.filter.FilterOperatorFetcher;
import ifeed.filter.Filter;
import ifeed.filter.FilterOperator;
import ifeed.architecture.AbstractArchitecture;
import ifeed.feature.logic.Connective;
import ifeed.feature.logic.Literal;
import ifeed.mining.moea.MOEABase;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;

import java.util.List;

public class FeatureArgMutation implements Variation{

    private MOEABase base;
    private List<AbstractArchitecture> architectures;
    private FilterOperatorFetcher filterOperatorFetcher;

    public FeatureArgMutation(List<AbstractArchitecture> architectures, MOEABase base){
        this.architectures = architectures;
        this.base = base;
        this.filterOperatorFetcher = this.base.getFeatureFetcher().getFilterOperatorFetcher();
    }

    @Override
    public Solution[] evolve(Solution[] parents){

        // Single child created
        Solution[] out = new Solution[1];

        // Select a single literal to modify
        FeatureTreeVariable tree = (FeatureTreeVariable) parents[0].getVariable(0);
        Connective root = tree.getRoot().copy();

        Literal randomNode = (Literal) base.getFeatureSelector().selectRandomNode(root, Literal.class);
        Connective parent = base.getFeatureSelector().findParentNode(root, randomNode);

        parent.getLiteralChildren().remove(randomNode);

        FilterOperator op = filterOperatorFetcher.fetch(randomNode.getName());
        op.mutate();
        Feature modifiedFeature = base.getFeatureFetcher().fetch(((Filter) op).getName());

        // Add new literal to the given node
        parent.addLiteral(modifiedFeature.getName(), modifiedFeature.getMatches());

        FeatureTreeVariable newTree = new FeatureTreeVariable(root, this.base);
        Solution sol = new FeatureTreeSolution(newTree, MOEAParams.numberOfObjectives);

        out[0] = sol;
        return out;
    }

    @Override
    public int getArity(){
        return 1;
    }
}
