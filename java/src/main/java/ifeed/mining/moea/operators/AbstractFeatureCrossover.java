package ifeed.mining.moea.operators;

import ifeed.mining.moea.MOEABase;
import ifeed.feature.logic.LogicalConnectiveType;
import ifeed.feature.logic.Connective;
import ifeed.feature.logic.Literal;
import ifeed.feature.logic.Formula;
import org.moeaframework.core.Variation;

public abstract class AbstractFeatureCrossover implements Variation{

    protected double probability;
    protected MOEABase base;

    public AbstractFeatureCrossover(double probability, MOEABase base){
        this.probability = probability;
        this.base = base;
    }

    @Override
    public int getArity(){
        return 2;
    }
}
