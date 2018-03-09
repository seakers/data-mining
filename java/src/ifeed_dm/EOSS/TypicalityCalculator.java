package ifeed_dm.EOSS;

import ifeed_dm.FeatureExpressionHandler;
import ifeed_dm.FeatureFetcher;
import ifeed_dm.logic.Connective;
import ifeed_dm.logic.Literal;
import ifeed_dm.Filter;

import java.util.BitSet;

public class TypicalityCalculator {

    public BitSet input;
    public Connective feature;
    public FeatureExpressionHandler expressionHandler;
    public FeatureFetcher featureFetcher;

    public TypicalityCalculator(BitSet input, String feature, FeatureFetcher featureFetcher){
        this.input = input;
        this.featureFetcher = featureFetcher;
        this.expressionHandler = new FeatureExpressionHandler(featureFetcher);
        this.expressionHandler.setIgnoreMatchCalculation(true);
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

                Filter filter = this.fetchFilter(leaf.getName());
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
            Filter filter = this.fetchFilter(leaf.getName());
            if(filter.apply(this.input)){
                satisfied++;
            }
            total++;
        }

        int[] out = {satisfied, total};
        return out;
    }

    private Filter fetchFilter(String expression){

        String e = expression;
        if(e.startsWith("{") && e.endsWith("}")){
            e = e.substring(1,e.length()-1);
        }else{
            e = e;
        }

        if(e.split("\\[").length==1){
            throw new RuntimeException("Filter expression without brackets: " + expression);
        }

        String type = e.split("\\[")[0];
        String argsCombined = e.substring(0,e.length()-1).split("\\[")[1];
        String[] args = argsCombined.split(";");

        return this.featureFetcher.fetchFilter(type, args);
    }

}
