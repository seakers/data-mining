package ifeed.mining.moea.operators;

import ifeed.mining.moea.AbstractMOEABase;
import ifeed.mining.moea.GPMOEABase;
import org.moeaframework.core.Variation;

public abstract class AbstractFeatureCrossover implements Variation{

    protected double probability;
    protected AbstractMOEABase base;

    public AbstractFeatureCrossover(double probability, AbstractMOEABase base){
        this.probability = probability;
        this.base = base;
    }

    @Override
    public int getArity(){
        return 2;
    }
}
