package ifeed.feature;

import ifeed.feature.logic.Connective;
import ifeed.feature.logic.Literal;
import ifeed.filter.AbstractFilter;
import ifeed.filter.AbstractFilterFetcher;

import java.util.BitSet;

public class TypicalityCalculator {

    public BitSet input;
    public Connective feature;
    public FeatureExpressionHandler expressionHandler;

    public AbstractFeatureFetcher featureFetcher;
    public AbstractFilterFetcher filterFetcher;

    public TypicalityCalculator(BitSet input, String feature, AbstractFeatureFetcher featureFetcher){
        this.input = input;
        this.featureFetcher = featureFetcher;
        this.filterFetcher = featureFetcher.getFilterFetcher();

        this.expressionHandler = new FeatureExpressionHandler(featureFetcher);
        this.expressionHandler.setSkipMatchCalculation(true);

        this.feature = this.expressionHandler.generateFeatureTree(feature);
    }

    public int[] run(){

        Connective cnf = expressionHandler.convertToCNF(this.feature);

        int total = 0;
        int satisfied = 0;

        for(Connective branch: cnf.getConnectiveChildren()){
            // Each branch is an OR node
            boolean at_least_one_satisfied = false;

            for(Literal leaf: branch.getLiteralChildren()){

                AbstractFilter filter = this.filterFetcher.fetch(leaf.getName());
                if(filter.apply(this.input)){
                    at_least_one_satisfied = true;
                    break;
                }
            }

            if(at_least_one_satisfied){
                satisfied++;
            }
            total++;
        }

        for(Literal leaf: cnf.getLiteralChildren()){
            AbstractFilter filter = this.filterFetcher.fetch(leaf.getName());
            if(filter.apply(this.input)){
                satisfied++;
            }
            total++;
        }

        int[] out = {satisfied, total};
        return out;
    }
}
